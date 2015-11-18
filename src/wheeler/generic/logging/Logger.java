/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.logging;

import wheeler.generic.data.FileHandler;
import wheeler.generic.data.LogicHandler;
import wheeler.generic.data.TimeHandler;

/**
 * Contains static functions that can be used to log output from a program.
 * Set the log directory and name, then start logging.
 * If left unset, will print log statements to standard-out.
 * Also contains functions for printing to standard-out.
 */
public class Logger {
    
    private static String logName = null;
    private static String logDir = null;
    private final static String logExt = ".log";
    private final static String lastWriteExtension = ".lastWrite.txt";
    private static String lastWriteTag = null;
    
    public static boolean infoEnabled = true;
    public static boolean debugEnabled = false;
    public static boolean warnEnabled = true;
    public static boolean errorEnabled = true;
    
    public static void setPath(String logFolder, String baseName) throws Exception{
        // If a null, we're disabling logging and should handle it accordingly
        if((logFolder == null) || (baseName == null)){
            logName = null;
            logDir = null;
            return;
        }
        
        // Make sure the path is valid
        FileHandler.ensureFolderExists(logFolder);
        logDir = logFolder;
        logName = baseName;
        
        // Get the datetime of the last logging, move the last log file if necessary
        String lastWritePath = getLastWriteFile();
        if (FileHandler.fileExists(lastWritePath))
            lastWriteTag = FileHandler.readFile(lastWritePath, true, false).pullFirst();
        checkLogDates();
    }
    public static boolean loggingEnabled(){ return ((logDir != null) && (logName != null)); }
    
    
    public static void info (String msg){ if (infoEnabled)  log("INFO ", msg); }
    public static void debug(String msg){ if (debugEnabled) log("DEBUG", msg); }
    public static void warn (String msg){ if (warnEnabled)  log("WARN ", msg); }
    public static void error(String msg){ if (errorEnabled) log("ERROR", msg); }
    public static void info (String caller, String msg){ info (caller + " : " + msg); }
    public static void debug(String caller, String msg){ debug(caller + " : " + msg); }
    public static void warn (String caller, String msg){ warn (caller + " : " + msg); }
    public static void error(String caller, String msg){ error(caller + " : " + msg); }
    
    public static void error(String msg, Exception e, int traceLevel, int indirection){
        error(msg + "\n" + LogicHandler.exToString(e, traceLevel, 1 + indirection));
    }
    
    
    private static void log(String type, String msg){
        // Do NOT allow exceptions to percolate up from here; logging should have no impact on actual operations
        try{
            logWorker(type, msg);
        }
        catch(Exception e){
            // What are we going to do, log it?
            System.out.println("Got an exception trying to log. Details: " + LogicHandler.exToString(e, 1, 0));
        }
    }
    private static void logWorker(String type, String msg) throws Exception{
        // Check the inputs
        if (type == null) throw new Exception("Logger.log got a null type argument");
        if (msg == null) throw new Exception("Logger.log got a null message argument");
        
        // Can't log without a logfile
        // Don't make it an exception or print anything because it may have been voluntary
        // Let's try redirecting logging to standard output when logging isn't enabled; make the data visible
        if ((logDir == null) || (logName == null)){
            print(composeLogLine(type, msg));
            return;
        }
        
        // Handle the whole date thing. Don't allow exceptions here to block the actual logging
        try{
            checkLogDates();
        }
        catch(Exception e){
            String eLine = "LOGGER_ERROR checkLogDates threw an exception: " + LogicHandler.exToString(e, 1, 0);
            try{
                FileHandler.appendToFile(eLine, true, composeLogfilePath(""));
            }
            catch(Exception e2){
                System.out.println(eLine);
                System.out.println(e2.toString());
            }
        }
        
        // Write the darn thing
        FileHandler.appendToFile(composeLogLine(type, msg), true, composeLogfilePath(""));
    }
    private static String composeLogLine(String type, String msg){
        return TimeHandler.getTimestamp("hh:mm:ss.zzz") + " " + type + " " + msg;
    }
    
    
    public static void print(String msg){
        System.out.println(msg);
    }
    public static void print(Exception e, int traceLevel, int indirection){
        print(LogicHandler.exToString(e, traceLevel, 1 + indirection));
    }
    
    
    private static void checkLogDates() throws Exception{
        // If the last date is null or up-to-date, nothing to do
        String nowDate = TimeHandler.getTimestamp("yyyyMMdd");
        String currFile = composeLogfilePath("");
        if(lastWriteTag == null){
            FileHandler.writeToFile(nowDate, getLastWriteFile());
            if (!FileHandler.fileExists(currFile))
                FileHandler.writeToFile("", currFile);
            lastWriteTag = nowDate;
            return;
        }
        if (lastWriteTag.equals(nowDate)) return;
        
        // The last write date is outdated; move the logfile (if appropriate) and update the last-write file
        String oldFile = composeLogfilePath(lastWriteTag);
        if (FileHandler.fileExists(currFile) && FileHandler.getFileSize(currFile) > 0)
            FileHandler.moveFile(currFile, oldFile);
        if (!FileHandler.fileExists(currFile))
            FileHandler.writeToFile("", currFile);
        FileHandler.writeToFile(nowDate, getLastWriteFile());
        lastWriteTag = nowDate;
    }
    
    
    private static String composeLogfilePath(String tag){
        if (tag.length() > 0) tag = "." + tag;
        return FileHandler.composeFilepath(logDir, logName + tag + logExt);
    }
    private static String getLastWriteFile(){
        return FileHandler.composeFilepath(logDir, logName + lastWriteExtension);
    }
    
}
