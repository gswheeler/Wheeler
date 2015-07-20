/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data;

import javax.swing.JFrame;
import wheeler.generic.structs.IStringList;
import wheeler.generic.structs.IStringNode;
import wheeler.generic.structs.StringList;
import wheeler.generic.structs.StringSimpleList;

/**
 *
 * @author Greg
 */
public class BatchRunner extends BaseHandler {
    
    /// Non-static variables ///
    private String sessionName;
    private String sessionFile;
    private String handlerFile;
    private String runnerFile;
    private String runnerSignalFile;
    private String doneSignalFile;
    private boolean closed = true;
    
    
    /// Constructors ///
    
    protected BatchRunner(){}
    
    public BatchRunner(String sessName, String parentFolder) throws Exception{
        // Variables
        sessionName = sessName;
        String runnerName = sessionName + ".runner";
        sessionFile = getSignalFilepath(sessionName, parentFolder);
        handlerFile = getBatchFilepath(sessionName, parentFolder);
        runnerFile = getBatchFilepath(runnerName, parentFolder);
        runnerSignalFile = getSignalFilepath(runnerName, parentFolder);
        doneSignalFile = getMetaFilepath(runnerName, "done", parentFolder);
        
        // File work
        FileHandler.ensureDirectoryExists(parentFolder);
        FileHandler.deleteFile(sessionFile);
        FileHandler.deleteFile(runnerFile);
        FileHandler.deleteFile(runnerSignalFile);
        FileHandler.deleteFile(doneSignalFile);
        writeHandlerFile();
        FileHandler.ensureFileExists(sessionFile);
        LogicHandler.sleep(300);
        
        // Make it happen
        FileHandler.runBatchFile(handlerFile);
    }
    
    private void writeHandlerFile() throws Exception{
        StringSimpleList steps = new StringSimpleList();
        
        steps.add("@echo off");                     // Echo off
        steps.add(":cmd_check");                    // Go here each time we check for a new command
        steps.add("IF EXIST \"" + runnerSignalFile  // If the runnerSignal file is still around, back off
                   + "\" GOTO run_check");
        steps.add("IF NOT EXIST \"" + runnerFile    // If the runner file isn't around, don't run it
                   + "\" GOTO run_check");
        steps.add("call \"" + runnerFile + "\"");   // Run the runner
        steps.add(getEchoFileCommand(runnerSignalFile));// Signal that we're done
        steps.add(":run_check");                    // Go here each time we check if we're still running
        steps.add("timeout /t 1 > nul");            // Don't hog the cpu
        steps.add("IF EXIST \"" + sessionFile       // If we're still running, loop
                   + "\" GOTO cmd_check");
        steps.add(getEchoFileCommand(doneSignalFile));// Make sure the command window actually closes
        steps.add("exit");                          // Close the cmd window when done
        
        FileHandler.writeFile(steps, true, handlerFile);
        
        closed = false;
    }
    
    public void runCommand(String cmd, int timeout) throws Exception{
        String[] script = {cmd};
        runScript(script, timeout);
    }
    
    public void runScript(String[] script, int timeout) throws Exception{
        // Make sure we're still runnning
        if (closed) throw new Exception("Batch runner " + sessionName + " is not currently in operation");
        
        // Make sure the signal file exists so that the runner will not kick in prematurely
        if (!FileHandler.ensureFileExists(runnerSignalFile)) LogicHandler.sleep(200);
        
        // Write the runner file (don't worry; the signal file will prevent premature execution)
        StringSimpleList steps = new StringSimpleList();
        steps.add("@echo off");
        steps.add(script);
        steps.add("exit/B");
        FileHandler.writeFile(escapeCommands(steps), true, runnerFile);
        LogicHandler.sleep(300);
        
        // Remove the signal file to allow the runner to run
        FileHandler.deleteFile(runnerSignalFile);
        
        // Wait for the signal file to be created again
        FileHandler.waitForFile(runnerSignalFile, timeout);
        LogicHandler.sleep(300);
    }
    
    public void close() throws Exception{
        // Timing hole for when we close right after creation
        if (!FileHandler.fileExists(sessionFile)) LogicHandler.sleep(500);
        // Remove the main signal file to tell the handler to stop execution
        FileHandler.deleteFile(sessionFile);
        // Make sure the done-signal file appears
        FileHandler.waitForFile(doneSignalFile, 120);
        LogicHandler.sleep(300);
        closed = true;
    }
    public Exception safeClose(){
        try{
            close();
        }
        catch(Exception e){
            return e;
        }
        return null;
    }
    
    
    
    /// Functions
    
    // Run a command via windows
    public static void runCommand(String command, String monicker, String parentFolder, int timeoutSeconds) throws Exception{
        String[] contents = {command};
        runScript(contents, monicker, parentFolder, timeoutSeconds);
    }
    
    
    // Write a batch file, run it, and return
    public static void runScript(String[] script, String batchName, String parentFolder, int timeoutSeconds) throws Exception{
        // Get the filepaths
        String batchFile = getBatchFilepath(batchName, parentFolder);
        String signalFile = getSignalFilepath(batchName, parentFolder);
        
        // Signal and echo-off components
        String[] firstStep = {"@echo off"};
        String[] lastSteps = { "echo > \"" + signalFile + "\"", "exit"};
        
        // Combine all steps into one array
        StringSimpleList steps = new StringSimpleList();
        steps.add(firstStep);
        steps.add(script);
        steps.add(lastSteps);
        
        // Write the batch file
        FileHandler.writeFile(escapeCommands(steps), true, batchFile);
        FileHandler.deleteFile(signalFile);
        
        // Run the batch file
        FileHandler.runBatchFile(batchFile);
        
        // Wait for the completion signal
        FileHandler.waitForFile(signalFile, timeoutSeconds);
        
        // Clean up the runners
        //FileHandler.deleteFile(batchFile);
        //FileHandler.deleteFile(signalFile);
    }
    
    
    // Write the batch file, adding no-echo and exit lines and escaping troublesome characters
    protected static void writeBatchFile(String[] script, boolean closeShell, String batchFile) throws Exception{
        writeBatchFile(new StringList(script), closeShell, batchFile);
    }
    protected static void writeBatchFile(IStringList script, boolean closeShell, String batchFile) throws Exception{
        StringSimpleList steps = new StringSimpleList();
        steps.add("@echo off");
        steps.add(script);
        steps.add((closeShell) ? "exit" : "exit/B");
        FileHandler.writeFile(escapeCommands(steps), true, batchFile);
    }
    
    // Escape any troublesome characters
    private static StringSimpleList escapeCommands(IStringList script){
        StringSimpleList result = new StringSimpleList();
        IStringNode node = script.getHeader();
        while((node = node.getNext()) != null){
            result.add(StringHandler.replace(node.getValue(), "%", "%%", true));
        }
        return result;
    }
    
    public static String getBatchFilepath(String batchName, String parentFolder){
        return FileHandler.composeFilepath(parentFolder, batchName + ".cmd");
    }
    public static String getSignalFilepath(String batchName, String parentFolder){
        return getMetaFilepath(batchName, "signal", parentFolder);
    }
    public static String getMetaFilepath(String batchName, String type, String parentFolder){
        return FileHandler.composeFilepath(parentFolder, batchName + "." + type + ".txt");
    }
    
    
    // Get a command to echo to the console
    public static String getEchoConsoleCommand(String text){
        text = StringHandler.replace(text, "&", "", true);
        String echo = "echo" + ((text.length() > 0) ? " " : ".");
        return echo + text;
    }
    
    // Get a command to echo into a temporary file
    public static String getEchoFileCommand(String filepath){
        return getEchoWriteLineCommand("", filepath);
    }
    // Echo something substantial into a file
    public static String getEchoWriteLineCommand(String text, String filepath){
        String echo = "echo" + ((text.length() > 0) ? " " : ".");
        return echo + text + ">\"" + filepath + "\"";
    }
    public static String getEchoConcatLineCommand(String text, String filepath){
        String echo = "echo" + ((text.length() > 0) ? " " : ".");
        return echo + text + ">>\"" + filepath + "\"";
    }
    
    
    // Get a command to delete files
    public static String[] getDeleteCommand(String name, String path){
        String[] cmd = new String[2];
        cmd[0] = "cd \"" + path + "\"";
        cmd[1] = "del \"" + name + "\"";
        return cmd;
    }
    public static String getDeleteCommand(String filepath){
        return "del \"" + filepath + "\"";
    }
    public static String[] getDeleteCommand(String[] filepaths){
        String[] cmd = new String[filepaths.length];
        for (int i = 0; i < filepaths.length; i++) cmd[i] = "del \"" + filepaths[i] + "\"";
        return cmd;
    }
    public static String[] getDeleteCommand(String[] names, String path){
        String[] cmd = new String[names.length + 1];
        cmd[0] = "cd \"" + path + "\"";
        for(int i = 0; i < names.length; i++){
            cmd[i+1] = "del \"" + names[i] + "\"";
        }
        return cmd;
    }
    
    // Get a command pair to change the active directory
    public static String[] getCdCommand(String path){
        StringList cmd = new StringList();
        if(StringHandler.contains(path, ":", true)){
            cmd.add(path.substring(0, path.indexOf(":") + 1));
        }
        return cmd.add("cd " + StringHandler.addQuotes(path)).toArray();
    }
    
    // Get a command to move a file
    public static String getMoveCommand(String src, String dst){
        return "move "
                + StringHandler.addQuotes(src) + " "
                + StringHandler.addQuotes(dst);
    }
    
    
    // Using a batch file, create symbolic links
    // Actually, just throw this part in the FileHandler
    /*public static void createSymlinks(String[] location, String[] target, boolean[] isFolder, JFrame caller) throws Exception{
        // Validate the inputs
        // For each link, must have a location, a target, and an isFolder bit
        if ((location.length != target.length) || (location.length != isFolder.length))
            throw new Exception("The number of locations (" + location.length
                    + "), targets (" + target.length + "), and isFile/Folders (" + isFolder.length
                    + ") must match"
                );
        
        // For each link, get the appropriate commands
        StringSimpleList steps = new StringSimpleList();
        for (int i = 0; i < location.length; i++)
            steps.add(getCreateSymlinkCommand(location[i], target[i], isFolder[i]));
        
        // Need to run this as an admin
        runScriptAsAdmin(steps.toArray(), caller);
    }*/
    
    
    public static String[] getCreateSymlinkCommand(String location, String target, boolean isFolder){
        StringSimpleList commands = new StringSimpleList();
        commands.add(getCdCommand(FileHandler.getParentFolder(location)));
        commands.add("mklink "
                + ((isFolder) ? "/d " : "")
                + StringHandler.addQuotes(FileHandler.getFileName(location)) + " "
                + StringHandler.addQuotes(target)
            );
        return commands.toArray();
    }
    
    
    // Write a batch file that will create the requested symbolic links
    /*public static void writeCreateSymlinks(String[] linkData, JFrame caller) throws Exception{
        // For each linkdata, write a command to create a symlink (path\ttarget)
        StringSimpleList steps = new StringSimpleList();
        for(int i = 0; i < linkData.length; i++){
            boolean isFolder = linkData[i].startsWith("1");
            String linkPath = linkData[i].substring(1, linkData[i].indexOf("\t"));
            String targetPath = linkData[i].substring(linkData[i].indexOf("\t") + 1);
            steps.add(getCdCommand(FileHandler.getParentFolder(linkPath)));
            steps.add(
                    "mklink "
                    + ((isFolder) ? "/d " : "")
                    + StringHandler.addQuotes(FileHandler.getFileName(linkPath)) + " "
                    + StringHandler.addQuotes(targetPath)
                );
        }
        runScriptAsAdmin(steps.toArray(), caller);
    }*/
    
    // Execute a batch file that requires admin privleges
    public static void runScriptAsAdmin(String[] script, JFrame caller) throws Exception{
        // Get a filepath; getBatchFilepath(StringHandler.getUnique(), FileHandler.wheelerBatchFolder());
        // Return the path of the batchfile
        String batchFile = getBatchFilepath(StringHandler.getUnique(), FileHandler.wheelerBatchFolder());
        FileHandler.ensureDirectoryExists(FileHandler.getParentFolder(batchFile));
        StringSimpleList steps = new StringSimpleList();
        steps.add("@echo off");
        steps.add(script);
        steps.add("exit/B");
        FileHandler.writeFile(escapeCommands(steps), true, batchFile);
        String[] options = {"Success", "Failed", "Open folder"};
        while(true){
            int userReturn = DialogFactory.customOption(caller, options,
                    "Please run the Wheeler batchfile " + FileHandler.getFileName(batchFile) + " as an administrator",
                    "Run " + FileHandler.getFileName(batchFile));
            if(userReturn == 2){
                FileHandler.openFile(FileHandler.getParentFolder(batchFile));
                continue;
            }
            if (userReturn == 0){
                FileHandler.deleteFile(batchFile);
                return;
            }else{
                if (DialogFactory.optionYesNo(caller,
                        "Delete batchfile?",
                        FileHandler.getFileName(batchFile)))
                    FileHandler.deleteFile(batchFile);
            }
            throw new Exception("Failed to execute admin batch file");
        }
    }
    
}
