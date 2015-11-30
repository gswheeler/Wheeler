/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data;

import java.awt.Desktop;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.JFrame;
import wheeler.generic.data.filters.BasicFilter;
import wheeler.generic.data.filters.FileIsFile;
import wheeler.generic.data.filters.FileIsFolder;
import wheeler.generic.data.filters.FileNameMatches;
import wheeler.generic.data.readers.FileReader;
import wheeler.generic.data.readers.FileWriter;
import wheeler.generic.error.AggregateException;
import wheeler.generic.error.LogicException;
import wheeler.generic.logging.Logger;
import wheeler.generic.structs.IStringList;
import wheeler.generic.structs.IStringNode;
import wheeler.generic.structs.StringList;
import wheeler.generic.structs.StringSimpleList;

/**
 * Contains static methods used to handle file I/O operations.
 * By using the functions in this class, most projects should not have any need to import something under java.io.
 */
public class FileHandler {
    
    /// Constructors
    protected FileHandler(){}
    
    
    
    /// Variables ///
    
    public static boolean forceFilesToBeInOrder = false;
    public static boolean forceFoldersToBeInOrder = false;
    public static boolean forceAllFilesToBeInOrder = false;
    public static boolean excludeSymbolicLinks = false;
    public static boolean printablePathsOnly = false;
    
    public static long deleteRetryInterval = 4*1000;
    public static int deleteDefaultTimeout = 10;
    
    protected static String wheelerFolder = "C:\\Program Files\\Wheeler";
    private static String wheelerDataFolder(){ return composeFilepath(wheelerFolder, "data"); }
    public static String wheelerBatchFolder(){ return composeFilepath(wheelerDataFolder(), "batch"); }
    
    
    
    /// Functions ///
    
    // Since we generally work within Program Files, need to make sure we have a folder with the right permissions
    protected static boolean testProgramFolder(String programFolder, JFrame caller) throws Exception{
        while(true){
            try{
                String path = programFolder;
                ensureFolderExists(path);
                path = composeFilepath(path, "test.txt");
                String text = "Permissions test";
                writeToFile(text, path);
                if (!readFile(path, false, false).equals(new StringList(text)))
                    throw new Exception("Did not read/write the correct file data");
                deleteFile(path);
                return true;
            }
            catch(Exception e){
                if (caller == null) throw e;
                String message = "Failed to access/create our folder under\n" + getParentFolder(programFolder);
                message += "\n\nPlease make sure the folder exists and your";
                message += "  user account has Full Control under Security/Permissions";
                message += "\n\nException details: " + LogicHandler.exToString(e, 1, 0);
                String[] options = {"Try again", "Close program"};
                if (0 != DialogFactory.customOption(caller, options, message, programFolder)) return false;
            }
        }
        
    }
    
    
    // Get a folder's files. Return in sorted order
    public static String[] getFiles(String path) throws Exception{
        String[] results = getContents(path, new FileIsFile());
        
        return (forceFilesToBeInOrder && !forceAllFilesToBeInOrder)
                ? StringHandler.sortStrings(results)
                : results;
    }
    
    
    // Get a folder's subfiles, including those in its subfolders. Return in sorted order
    public static String[] getFilesRecursive(String path) throws Exception{
        if (!folderExists(path)) throw new Exception("\"" + path + "\" is not a valid folder path");
        String[] contents = getContents(path);
        StringSimpleList collection = new StringSimpleList();
        for(String dir : contents){
            if(fileExists(dir)){
                collection.add(dir);
            }
            if(folderExists(dir)){
                collection.add(getFilesRecursive(dir));
            }
        }
        return collection.toArray();
    }
    
    
    // Get a folder's subfolders. Return in sorted order
    public static String[] getSubfolders(String path) throws Exception{
        String[] results = getContents(path, new FileIsFolder());
        
        return (forceFoldersToBeInOrder && !forceAllFilesToBeInOrder)
                ? StringHandler.sortStrings(results)
                : results;
    }
    
    
    // Get a folder's subfolders, including those in its subfolders. Return in sorted order
    public static String[] getSubfoldersRecursive(String path) throws Exception{
        if (!folderExists(path)) throw new Exception("\"" + path + "\" is not a valid folder path");
        String[] subfolders = getSubfolders(path);
        StringSimpleList collection = new StringSimpleList();
        for(String subfolder : subfolders){
            if(folderExists(subfolder)){
                collection.add(subfolder);
                collection.add(getSubfoldersRecursive(subfolder));
            }
        }
        return collection.toArray();
    }
    
    
    // Get all files within a folder that matches a given regular expression
    public static String[] getFilesByName(String path, String nameExpr) throws Exception{
        String[] results = getContents(path, new FileNameMatches(nameExpr));
        
        return (forceFilesToBeInOrder && !forceAllFilesToBeInOrder)
                ? StringHandler.sortStrings(results)
                : results;
    }
    
    
    // Get a folder's contents. Return in sorted order
    public static String[] getContents(String path) throws Exception{
        return getContents(path, new BasicFilter());
    }
    public static String[] getContents(String path, FileFilter filter) throws Exception{
        File file = new File(path);
        if (!folderExists(path)) throw new Exception("\"" + path + "\" is not a valid folder path");
        File[] files = file.listFiles(filter);
        String[] filePaths = new String[files.length];
        for (int i = 0; i < filePaths.length; i++) filePaths[i] = files[i].getPath();
        return (forceAllFilesToBeInOrder) ? StringHandler.sortStrings(filePaths) : filePaths;
    }
    
    
    // Get a folder's files and subfolders. Put in two arrays to distinguish the two
    public static String[][] getFilesAndFolders(String path) throws Exception{
        String[][] results = new String[2][];
        String[] folders = getSubfolders(path);
        String[] files = getFiles(path);
        results[0] = folders;
        results[1] = files;
        return results;
    }
    
    
    
    // Get the roots of the filesystem
    public static String[] getRoots(){
        File[] roots = File.listRoots();
        String[] results = new String[roots.length];
        for (int i = 0; i < roots.length; i++) results[i] = roots[i].getPath();
        return results;
    }
    
    
    // Is the provided path a root?
    public static boolean isRoot(String path){
        String[] roots = getRoots();
        for (String root : roots)
            if (StringHandler.trimTrailingCharacters(path, "\\").equalsIgnoreCase(
                    StringHandler.trimTrailingCharacters(root, "\\"))
                ) return true;
        return false;
    }
    
    
    // Is the given path absolute?
    public static boolean isAbsolute(String path){
        return fileObject(path).isAbsolute();
    }
    
    
    public static long getUsableSpaceOnDrive(String path){
        File drive = new File(
                (StringHandler.contains(path, ":"))
                    ? path.substring(0, path.indexOf(":") + 1)
                    : path
            );
        return drive.getUsableSpace();
    }
    
    
    public static long getTotalSpaceOnDrive(String path){
        File drive = new File(
                (StringHandler.contains(path, ":"))
                    ? path.substring(0, path.indexOf(":") + 1)
                    : path
            );
        return drive.getTotalSpace();
    }
    
    
    // Rename a file. Make sure the two files are in the same folder
    public static void renameFile(String path, String name) throws Exception{
        File src = new File(path);
        if (!fileExists(path)) throw new Exception("The file \"" + path + "\" does not exist");
        File dst = new File(src.getParentFile().getPath() + "\\" + name);
        if (StringHandler.contains(name, "\\")) throw new Exception("Rename does not allow directory changes");
        if (fileExists(dst.getPath())) throw new Exception("The file \"" + dst.getPath() + "\" already exists");
        Files.move(src.toPath(), dst.toPath());
    }
    
    
    // Rename a file. Make sure the two files are in the same folder
    public static void renameFolder(String path, String name) throws Exception{
        File src = new File(path);
        if (!folderExists(path)) throw new Exception("The directory \"" + path + "\" does not exist");
        File dst = new File(src.getParentFile().getPath() + "\\" + name);
        if (StringHandler.contains(name, "\\")) throw new Exception("Rename does not allow directory changes");
        if (folderExists(dst.getPath())) throw new Exception("The directory \"" + dst.getPath() + "\" already exists");
        Files.move(src.toPath(), dst.toPath());
    }
    
    
    // Move a file by changing its path
    public static void moveFile(String srcPath, String dstPath) throws Exception{
        File src = new File(srcPath);
        if (!fileExists(srcPath)) throw new Exception("The file \"" + srcPath + "\" does not exist");
        File dst = new File(dstPath);
        if (fileExists(dstPath)) throw new Exception("The file \"" + dstPath + "\" already exists");
        Files.move(src.toPath(), dst.toPath());
    }
    
    
    // Copy a file into another location
    public static void copyFile(String srcPath, String dstPath, JFrame caller) throws Exception{
        if(fileExists(dstPath)){
            if(DialogFactory.optionYesNo(
                    caller,
                    "File\n" + dstPath + "\nalready exists. Replace it with\n" + srcPath + "\n?",
                    "Replace existing file?"))
                copyFile(srcPath, dstPath, true);
        }else{
            copyFile(srcPath, dstPath, false);
        }
    }
    public static void copyFile(String srcPath, String dstPath, boolean overwriteExisting) throws Exception{
        // Check existance
        if (!fileExists(srcPath))
            throw new Exception("The source file \"" + srcPath + "\" does not exist");
        if (!overwriteExisting && fileExists(dstPath))
            throw new Exception("The destination file \"" + dstPath + "\" already exists");
        if (!folderExists(getParentFolder(dstPath)))
            throw new Exception("The parent folder for the destination file \"" + dstPath + "\" does not exist");
        
        // Perform copy
        if (overwriteExisting)
            Files.copy(new File(srcPath).toPath(), new File(dstPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
        else
            Files.copy(new File(srcPath).toPath(), new File(dstPath).toPath());
    }
    
    
    // Copy a file to a folder
    protected static void copyFileToFolder(String srcPath, String destDir, boolean overwriteExisting) throws Exception{
        // Make sure the destination folder exists
        if (!folderExists(destDir))
            throw new Exception("The destination folder \"" + destDir + "\" does not exist");
        
        // Get the new filename
        String destPath = composeFilepath(destDir, getFileName(srcPath));
        
        // Copy the file
        copyFile(srcPath, destPath, overwriteExisting);
    }
    
    
    // Copy a folder and its contents from one place to another
    public static void copyFolderWithContents(String srcPath, String dstPath, boolean overwriteExisting) throws Exception{
        // Check existance(s)
        if(!folderExists(srcPath))
            throw new Exception("The source folder \"" + srcPath + "\" does not exist");
        if (!overwriteExisting && folderExists(dstPath))
            throw new Exception("The destination folder \"" + dstPath + "\" already exists");
        if (!folderExists(getParentFolder(dstPath)))
            throw new Exception("The parent folder for the destination folder \"" + dstPath + "\" does not exist");
        
        // Perform copy, recursively
        //   Create the containing folder
        //   Copy over any files
        //   Copy over any subfolders and any files they contain
        createFolder(dstPath);
        String[] subfiles = getFiles(srcPath);
        for(String subfile : subfiles){
            String filename = getFileName(subfile);
            String newPath = composeFilepath(dstPath, filename);
            copyFile(subfile, newPath, overwriteExisting);
        }
        String[] subfolders = getSubfolders(srcPath);
        for(String subfolder : subfolders){
            String filename = getFileName(subfolder);
            String newPath = composeFilepath(dstPath, filename);
            copyFolderWithContents(subfolder, newPath, overwriteExisting);
        }
    }
    
    
    // Copy a file to a folder
    protected static void copyFolderWithContentsToFolder(String srcPath, String destDir, boolean overwriteExisting) throws Exception{
        // Make sure the destination folder exists
        if (!folderExists(destDir))
            throw new Exception("The destination folder \"" + destDir + "\" does not exist");
        
        // Get the new filename
        String destPath = composeFilepath(destDir, getFileName(srcPath));
        
        // Copy the file
        copyFolderWithContents(srcPath, destPath, overwriteExisting);
    }
    
    
    // Make sure the file exists, populated or otherwise
    public static boolean ensureFileExists(String path) throws Exception{
        try{
            ensureFolderExists(getParentFolder(path));
        }
        catch(Exception e){
            throw new Exception("Failed to create the parent folder for file \"" + path + "\"", e);
        }
        if(!fileExists(path)){
            writeToFile("", path);
            waitForFile(path, 10);
            return false;
        }
        return true;
    }
    
    /**
     * Wait for a file to be created. Does not create the file.
     * @param path The file to wait for
     * @param timeoutSeconds Length of time to wait in seconds (zero for no timeout)
     * @throws Exception if the specified timeout period elapses before the file is found
     */
    public static void waitForFile(String path, long timeoutSeconds) throws Exception{
        long deadline = TimeHandler.ticks() + (timeoutSeconds * 1000);
        while(!FileHandler.fileExists(path)){
            if ((timeoutSeconds > 0) && (TimeHandler.ticks() > deadline))
                throw new Exception("File \"" + path + "\" failed to be created within " + timeoutSeconds + " seconds");
            LogicHandler.sleep(20);
        }
    }
    
    /**
     * Wait for a file to be deleted. Does not delete the file.
     * @param path The file to wait on
     * @param timeoutSeconds Length of time to wait in seconds (zero for no timeout)
     * @throws Exception if the specified timeout period elapses before the file is no longer found
     */
    public static void waitForFileDelete(String path, long timeoutSeconds) throws Exception{
        long deadline = TimeHandler.ticks() + (timeoutSeconds * 1000);
        while(FileHandler.fileExists(path)){
            if((timeoutSeconds > 0) && (TimeHandler.ticks() > deadline))
                throw new Exception("File \"" + path + "\" failed to be deleted within " + timeoutSeconds + " seconds");
            LogicHandler.sleep(20);
        }
    }
    
    
    // Create a new folder at the specified path
    // Returns true if the file is newly created, false if it is already there
    // Throws an exception if the folder could not be created
    public static boolean createFolder(String path) throws Exception{
        if (folderExists(path)) return false;
        if (!folderExists(getParentFolder(path)))
            throw new Exception("The destination folder for new folder \"" + path + "\" does not exist");
        Files.createDirectory(new File(path).toPath());
        long deadline = TimeHandler.ticks() + (10*1000);
        while (!folderExists(path))
            if (TimeHandler.ticks() > deadline)
                throw new Exception("Failed to create folder \"" + path + "\"");
            else
                LogicHandler.sleep(5);
        return true;
    }
    
    
    // Make a folder exist, including the creation of parent folders if necessary
    public static boolean ensureFolderExists(String path) throws Exception{
        // If the folder exists, don't have to create anything
        if (folderExists(path)) return true;
        
        // Make sure the parent exists first
        StringSimpleList parentsToCreate = new StringSimpleList();
        String parent = getParentFolder(path);
        while((parent != null) && !folderExists(parent)){
            parentsToCreate.add(parent);
            parent = getParentFolder(parent);
        }
        // At this point, parent will be the uppermost existing parent or null if the path's root is non-existant
        if (parent == null)
            throw new Exception("Failed to find an existing parent folder for folder \"" + path + "\"");
        while(parentsToCreate.any()){
            createFolder(parentsToCreate.pullLast());
        }
        
        // The parent now exists; create the folder itself
        createFolder(path);
        return false;
    }
    
    
    // Delete a file. Timeout in seconds; zero for no timeout
    public static boolean deleteFile(String path) throws Exception { return deleteFile(path, deleteDefaultTimeout); }
    public static boolean deleteFile(String path, int timeoutSeconds) throws Exception {
        String[] paths = {path}; return deleteFiles(paths, timeoutSeconds);
    }
    public static boolean deleteFiles(String[] paths) throws Exception { return deleteFiles(paths, deleteDefaultTimeout); }
    public static boolean deleteFiles(String[] paths, int timeoutSeconds) throws Exception {
        // Delete the files
        boolean somethingWasDeleted = false;
        for(String filepath : paths){
            if (!fileExists(filepath)) continue;
            fileObject(filepath).delete();
            somethingWasDeleted = true;
        }
        if (!somethingWasDeleted) return false;
        
        // Make sure the files got deleted
        long deadline = TimeHandler.ticks() + (timeoutSeconds * 1000);
        long deleteTime = TimeHandler.ticks() + deleteRetryInterval;
        while(true){
            String existingFile = null;
            boolean deleting = deleteTime > TimeHandler.ticks();
            
            // Look for existing files; if we're deleting this time around, delete
            for(String filepath : paths){
                if(fileExists(filepath)){
                    if (existingFile == null) existingFile = filepath;
                    if (deleting) fileObject(filepath).delete();
                }
            }
            if (existingFile == null) return true;
            
            // If we deleted this time around, advance the count. Otherwise, make sure we haven't run out of time
            if(deleting){
                deleteTime += deleteRetryInterval;
            }else if((timeoutSeconds > 0) && (TimeHandler.ticks() > deadline)){
                throw new Exception("Failed to delete file \"" + existingFile + "\" within " + timeoutSeconds + " seconds");
            }
            LogicHandler.sleep(5);
        }
    }
    
    
    // Delete a folder's contents (including subfolders), then delete the folder itself
    // Timeout in seconds; zero for no timeout
    public static boolean deleteFolders(String[] paths) throws Exception { return deleteFolders(paths, deleteDefaultTimeout); }
    public static boolean deleteFolders(String[] paths, int timeoutSeconds) throws Exception {
        boolean anyDeleted = false;
        for (String folder : paths) if (deleteFolder(folder, timeoutSeconds)) anyDeleted = true;
        return anyDeleted;
    }
    public static boolean deleteFolder(String path) throws Exception { return deleteFolder(path, deleteDefaultTimeout); }
    public static boolean deleteFolder(String path, int timeoutSeconds) throws Exception {
        // Check if it doesn't exist
        if (!folderExists(path)) return false;
        // If this is a symbolic link, only delete the LINK
        if(isSymbolicLink(path)){
            fileObject(path).delete();
            return true;
        }
        
        // Clear away everything in the folder
        clearFolder(path);
        
        // Delete the folder itself
        fileObject(path).delete();
        long deleteTime = TimeHandler.ticks() + deleteRetryInterval;
        long deadline = TimeHandler.ticks() + (timeoutSeconds * 1000);
        while(folderExists(path)){
            if((timeoutSeconds > 0) && (TimeHandler.ticks() > deadline))
                throw new Exception("Failed to delete folder \"" + path + "\" within " + timeoutSeconds + " seconds");
            if(TimeHandler.ticks() > deleteTime){
                fileObject(path).delete();
                deleteTime += deleteRetryInterval;
            }
            LogicHandler.sleep(5);
        }
        return true;
    }
    
    
    // Delete whatever may exist at the current directory
    public static boolean deleteDirectories(String[] paths) throws Exception { return deleteDirectories(paths, deleteDefaultTimeout); }
    public static boolean deleteDirectories(String[] paths, int timeoutSeconds) throws Exception {
        boolean anyDeleted = false;
        for (String folder : paths) if (deleteDirectory(folder, timeoutSeconds)) anyDeleted = true;
        return anyDeleted;
    }
    public static boolean deleteDirectory(String path) throws Exception{ return deleteDirectory(path, deleteDefaultTimeout); }
    public static boolean deleteDirectory(String path, int timeoutSeconds) throws Exception{
        // If there's nothing there, don't bother
        if (!directoryExists(path)) return false;
        
        // Delete it, whatever it may be
        if(fileExists(path)){
            // It's a file; delete it
            deleteFile(path, timeoutSeconds);
        }else if(folderExists(path)){
            // It's a folder; delete it and anything inside it
            deleteFolder(path, timeoutSeconds);
        }else{
            // It's SOMETHING; just issue a delete command
            fileObject(path).delete();
            long deleteTime = TimeHandler.ticks() + deleteRetryInterval;
            long deadline = TimeHandler.ticks() + (timeoutSeconds * 1000);
            while(directoryExists(path)){
                if((timeoutSeconds > 0) && (TimeHandler.ticks() > deadline))
                    throw new Exception("Failed to delete directory \"" + path + "\" within " + timeoutSeconds + " seconds");
                if(TimeHandler.ticks() > deleteTime){
                    fileObject(path).delete();
                    deleteTime += deleteRetryInterval;
                }
            }
        }
        return true;
    }
    
    // Delete the contents of a folder
    public static void clearFolder(String path) throws Exception{ clearFolder(path, deleteDefaultTimeout); }
    public static void clearFolder(String path, int timeoutSeconds) throws Exception{
        if (!folderExists(path)) throw new Exception("The folder \"" + path + "\" does not exist");
        // Recursively delete all subfolders
        deleteFolders(getSubfolders(path), timeoutSeconds);
        // Delete all files in this folder
        deleteFiles(getFiles(path), timeoutSeconds);
        // If there's anything left, handle as appropriate
        deleteDirectories(getContents(path), timeoutSeconds);
    }
    
    
    /**
     * Check if a file exists
     * @param path The path being checked
     * @return True if a file exists at the specified path
     */
    public static boolean fileExists(String path){
        return new File(path).isFile();
    }
    
    
    /**
     * Check if a (valid) folder exists
     * @param path The path being checked
     * @return True if a valid folder exists at the specified path (it exists, is a folder, and can hold subitems)
     */
    public static boolean folderExists(String path){
        File file = fileObject(path);
        
        // If the filesystem says it isn't a folder, believe it
        if (!file.isDirectory()) return false;
        
        // If the folder can't have subitems, it isn't a usable folder
        if(file.listFiles() == null){
            Logger.warn("FileHandler.folderExists", "Unusable folder detected: " + path);
            return false;
        }
        
        // All checks passed; it is a usable folder
        return true;
    }
    
    
    /**
     * Check if there is anything there at all
     * @param path The path being checked
     * @return True if anything is present, be it a file, folder, symbolic link, anything
     */
    public static boolean directoryExists(String path){
        // The obvious check
        if (fileObject(path).exists()) return true;
        
        // Sometimes a directory, such as a broken symbolic link, can be present but not "exist"
        // To guard against this, see if the parent exists and then see if the subject is listed as one of the parent's subitems
        String parent = getParentFolder(path);
        if (parent == null) return false;
        if (!folderExists(parent)) return false;
        try{
            return new StringList(getContents(parent)).contains(path);
        }
        catch(Exception e){
            // GetContents throws if the parent folder doesn't exist
            // This is checked above, but the compiler doesn't know that
            Logger.error("FileHandler.directoryExists tried to call getContents on a folder it knew didn't exist", e, 1, 0);
            return false;
        }
    }
    
    
    // Answer the question; is this path a symbolic link?
    // Just answer no if it doesn't exist
    public static boolean isSymbolicLink(String path){
        return Files.isSymbolicLink(fileObject(path).toPath());
    }
    
    // Create a symbolic link using a batch file and the user's admin privleges
    public static void createSymbolicLink(String path, String target, boolean isFolder, JFrame caller) throws Exception{
        String[] paths = {path};
        String[] targets = {target};
        boolean areFolders[] = {isFolder};
        createSymbolicLinks(paths, targets, areFolders, caller);
    }
    
    // Create multiple symbolic links
    public static void createSymbolicLinks(String[] paths, String[] targets, boolean[] areFolders, JFrame caller) throws Exception{
        // Collect ALL errors
        StringList linkData = new StringList();
        StringSimpleList errors = new StringSimpleList();
        boolean[] newLink = new boolean[paths.length];
        
        // Sanity-check for the arrays
        if ((paths.length != targets.length) || (targets.length != areFolders.length))
            throw new Exception(
                    "The paths, targets, and areFolders arrays were different sizes ("
                    + paths.length + ", " + targets.length + ", " + areFolders.length + ")"
                );
        
        // Check each path while collecting linkData lines
        for(int i = 0; i < paths.length; i++){
            if(directoryExists(paths[i])){
                // The directory already exists. Create a stink about it ONLY if it doesn't match the desired properties
                if (isSymbolicLink(paths[i])
                        && StringHandler.areEqual(inspectSymbolicLink(paths[i]), targets[i], false)
                        && ((!areFolders[i] && fileExists(paths[i])) || (areFolders[i] && folderExists(paths[i])))
                    ) continue;
                errors.add("The path " + paths[i] + " already exists");
            }else{
                // Compose a dataString for this link, mark that it is being newly created
                linkData.add((areFolders[i] ? "1" : "0") + paths[i] + "\t" + targets[i]);
                newLink[i] = true;
            }
        }
        
        // If we don't have any valid links, don't bother trying to create anything
        if(linkData.isEmpty()){
            if (errors.any())
                throw AggregateException.create("Symbolic link creation failed", errors.toArray());
            return;
        }
        
        // Call on the BatchRunner to see us through
        StringSimpleList script = new StringSimpleList();
        while(linkData.any()){
            String link = linkData.pullFirst();
            script.add(BatchRunner.getCreateSymlinkCommand(
                    link.substring(1, link.indexOf("\t")),
                    link.substring(link.indexOf("\t") + 1),
                    link.startsWith("1")
                ));
        }
        BatchRunner.runScriptAsAdmin(script.toArray(), caller);
        
        // Make sure everything went alright
        for(int i = 0; i < paths.length; i++){
            // If we didn't make this link we should already have an error for it
            if (!newLink[i]) continue;
            
            // Analyze the links
            if(!isSymbolicLink(paths[i])){
                // If a symbolic link doesn't exist at this location, we goofed
                errors.add("Failed to create a link at " + paths[i]);
            }else if(!StringHandler.areEqual(targets[i], inspectSymbolicLink(paths[i]), false)){
                // If the link has the wrong data, we did something wrong
                errors.add("Created link " + paths[i] + " with the wrong target");
            }else if(
                    (fileObject(paths[i]).isFile() || fileObject(paths[i]).isDirectory())
                    && (areFolders[i] == fileObject(paths[i]).isFile())){
                // If we can tell whether the link is a file or folder, make sure it's the right one
                errors.add("Created link " + paths[i] + " as the wrong type");
            }
        }
        if (errors.any())
            throw AggregateException.create("Symbolic link creation failed", errors.toArray());
    }
    
    // Where does the symbolic link point? Throw if not a link
    public static String inspectSymbolicLink(String path) throws Exception{
        if (!directoryExists(path))
            throw new Exception("The path " + path + " does not exist");
        if (!isSymbolicLink(path))
            throw new Exception("The path " + path + " is not a symbolic link");
        return Files.readSymbolicLink(fileObject(path).toPath()).toString();
    }
    
    // What is the actual target of the symbolic link? Throw if not a link
    public static String resolveSymbolicLinkTarget(String path) throws Exception{
        // Get the link target (also checks that it is, in fact, a symbolic link)
        String target = inspectSymbolicLink(path);
        
        // Find out what it's pointing at
        // Note: relative filepaths resolve from the parent folder of the link
        // Can't do anything else at this point, lest we hit a recursive loop
        return resolveRelativeFilepath(getParentFolder(path), target);
    }
    
    // Delete the directory if and only if it is a symbolic link. Spare the link's target
    public static boolean deleteSymbolicLink(String path) throws Exception{
        if (!directoryExists(path)) return false;
        if (!isSymbolicLink(path))
            throw new Exception("The path " + path + " is not a symbolic link");
        fileObject(path).delete();
        return true;
    }
    
    
    // Get the size of the file
    public static long getFileSize(String path) throws Exception{
        File file = new File(path);
        if (!file.isFile()) throw new Exception("\"" + path + "\" is not a valid filepath");
        return file.length();
    }
    
    
    // Get the file's date-modified value
    public static long getDateModified(String path) throws Exception{
        if(!fileExists(path)) throw new Exception("\"" + path + "\" is not a valid filepath");
        return fileObject(path).lastModified();
    }
    
    
    // Get the name component of the specified file
    public static String getFileName(String path){
        return new File(path).getName();
    }
    
    
    // Get the filename components of a set of filepaths
    public static String[] getFileNames(String[] paths){
        String[] names = new String[paths.length];
        for (int i = 0; i < paths.length; i++) names[i] = getFileName(paths[i]);
        return names;
    }
    
    
    // Get the parent folder of the specified file
    public static String getParentFolder(String path){ return fileObject(path).getParent(); }
    
    
    // Does the specified folder contain the specified file?
    public static boolean folderAcontainsB(String folder, String subfile){
        String A = StringHandler.trimTrailingCharacters(folder, "\\").toLowerCase();
        String B = StringHandler.trimTrailingCharacters(subfile, "\\").toLowerCase();
        return (A.equals(B) || B.startsWith(A + "\\"));
    }
    
    
    // Is the specified folder the parent of the specified file?
    public static boolean folderAparentOfB(String folder, String subfile){
        String A = StringHandler.trimTrailingCharacters(folder, "\\").toLowerCase();
        String B = StringHandler.trimTrailingCharacters(subfile, "\\").toLowerCase();
        if (!StringHandler.contains(B, "\\", true)) return false;
        B = B.substring(0, B.lastIndexOf("\\"));
        return A.equals(B);
    }
    
    
    // Read a file's contents in their entirety
    public static StringList readFile(String path, boolean trimNewlines, boolean dropTailIfEmpty) throws Exception{
        if (!fileExists(path)) throw new Exception("The file \"" + path + "\" does not exist");
        FileReader reader = new FileReader(path);
        try{
            // Get a simple dynamic list of strings
            StringSimpleList contents = new StringSimpleList();
            String strLine;
            while((strLine = reader.readLine(trimNewlines)) != null){
                contents.add(strLine);
            }
            
            // If we're dropping empty trailing lines, pull empty lines until we find a non-empty one
            // Don't worry about performance on the "last" bit; SimpleLists store in reverse order
            if (dropTailIfEmpty)
                while (contents.any() && StringHandler.isEmpty(contents.getLast(), true))
                    contents.pullLast();
            
            // Return the results as a StringList for maximum operational flexibility
            return contents.toStringList();
        }
        finally{
            reader.close();
        }
    }
    
    
    // Write a string to a file
    public static void writeToFile(String str, String path) throws Exception{
        String[] contents = {str};
        writeFile(contents, false, path);
    }
    // Write the contents of an array to a file
    public static void writeFile(String[] contents, boolean appendNewlines, String path) throws Exception{
        writeFile(new StringList(contents), appendNewlines, path);
    }
    // Write the contents of a StringList to a file
    public static void writeFile(IStringList contents, boolean appendNewlines, String path) throws Exception{
        // Make sure the data is writable
        ensureDataIsWriteable(contents);
        
        // Write the file
        FileWriter writer = new FileWriter(path);
        writer.write("");
        writeFile(contents, appendNewlines, writer);
    }
    private static void ensureDataIsWriteable(IStringList contents) throws Exception{
        IStringNode node = contents.getHeader(); int count = 0;
        while((node = node.getNext()) != null){
            count++;
            if (!StringHandler.isWritable(node.getValue()))
                throw new Exception(
                        "Line " + count + " was not writable:\n" + StringHandler.escape(node.getValue())
                    );
        }
    }
    private static void writeFile(IStringList contents, boolean appendNewlines, FileWriter writer) throws Exception{
        try{
            IStringNode node = contents.getHeader();
            while((node = node.getNext()) != null)
                if (appendNewlines)
                    writer.writeLine(node.getValue());
                else
                    writer.write(node.getValue());
            writer.close();
        }
        catch(Exception e){
            try{
                writer.close();
            }
            catch(Exception e2){
                Logger.error("An error occurred while closing FileWriter after a write error", e2, 1, 0);
            }
            throw e;
        }
    }
    
    
    // Append a line to a file
    public static void appendToFile(String line, boolean appendNewline, String path) throws Exception{
        String[] contents = {line};
        appendToFile(contents, appendNewline, path);
    }
    public static void appendToFile(String[] contents, boolean appendNewlines, String path) throws Exception{
        appendToFile(new StringList(contents), appendNewlines, path);
    }
    public static void appendToFile(IStringList contents, boolean appendNewlines, String path) throws Exception{
        // Make sure the data is writable
        ensureDataIsWriteable(contents);
        
        // Write the data
        FileWriter writer = new FileWriter(path, true);
        writeFile(contents, appendNewlines, writer);
    }
    
    
    // Open a file using the Desktop
    public static void openFile(String path) throws Exception{
        Desktop.getDesktop().open(new File(path));
    }
    
    
    // Call batch file (can only be called on .cmd's)
    public static void runBatchFile(String path) throws Exception{
        if (!path.endsWith(".cmd")) throw new Exception("runBatchFile called with a non-.cmd (" + path + ")");
        if (!fileExists(path)) throw new Exception("The batch file " + path + " does not exist");
        File batchFile = new File(path);
        Runtime.getRuntime().exec("cmd /c start " + batchFile.getName(), null, batchFile.getParentFile());
    }
    
    
    // Provide a File object to non-FileHandler entities
    public static File fileObject(String path){
        if (path == null) return null;
        return new File(path);
    }
    
    
    // Form a file path from a folder and file name (manages trailing slashes)
    public static String composeFilepath(String folder, String name){
        // Remove any leading/trailing separator characters
        folder = StringHandler.trimTrailingCharacters(folder, "\\");
        name = StringHandler.trimLeadingCharacters(name, "\\");
        
        // Perform any "folder-up" operations denoted by the path addition
        while(name.startsWith("..\\")){
            folder = getParentFolder(folder);
            name = name.substring(3);
        }
        
        // Concatenate the two parts using a separator character
        return folder + "\\" + name;
    }
    
    // Take a file from one of its containing folders and put it into another folder
    public static String reparentFilepath(String path, String containingFolder, String newFolder) throws Exception{
        // Sanity check
        if (!folderAcontainsB(containingFolder, path))
            throw new LogicException("Folder " + containingFolder + " does not contain path " + path);
        // Get the new filepath
        return composeFilepath(newFolder, path.substring(containingFolder.length()));
    }
    
    // Using a base filepath, resolve a relative filepath
    public static String resolveRelativeFilepath(String path, String target){
        // If the target is absolute, just return that
        if (isAbsolute(target)) return target;
        
        // Starts with a slash, goes to the root
        if(target.startsWith("\\")){
            while (getParentFolder(path) != null) path = getParentFolder(path);
            target = StringHandler.trimLeadingCharacters(target, "\\");
        }
        
        // Go folder-by-folder, handling special directories as appropriate
        String[] folders = StringHandler.parseIntoArray(target, "\\");
        for(String folder : folders){
            // Double-slash, just skip it
            if (folder.equals("")) continue;
            // "This" directory
            if (folder.equals(".")) continue;
            // "Parent" directory
            if(folder.equals("..")){
                String parent = getParentFolder(path);
                if (parent != null) path = parent;
                continue;
            }
            // Subdirectory
            path = composeFilepath(path, folder);
        }
        
        // And done; return the newly-created filepath
        return path;
    }
    
    
    
    /// Getters and Setters
    
    public static boolean setForceFilesToBeInOrder(boolean newVal){
        boolean prevVal = forceFilesToBeInOrder; forceFilesToBeInOrder = newVal; return prevVal;
    }
    public static boolean setForceFoldersToBeInOrder(boolean newVal){
        boolean prevVal = forceFoldersToBeInOrder; forceFoldersToBeInOrder = newVal; return prevVal;
    }
    public static boolean setForceAllFilesToBeInOrder(boolean newVal){
        boolean prevVal = forceAllFilesToBeInOrder; forceAllFilesToBeInOrder = newVal; return prevVal;
    }
    public static boolean setExcludeSymbolicLinks(boolean newVal){
        boolean prevVal = excludeSymbolicLinks; excludeSymbolicLinks = newVal; return prevVal;
    }
    public static boolean setPrintablePathsOnly(boolean newVal){
        boolean prevVal = printablePathsOnly; printablePathsOnly = newVal; return prevVal;
    }
    
}
