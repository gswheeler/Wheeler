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
 *
 * @author Greg
 */
public class FileHandler extends BaseHandler {
    
    /// Constructors
    protected FileHandler(){}
    
    
    
    /// Variables ///
    
    //public static int driveIdMedia = 150986;
    //public static String driveNameMedia = "HP Personal Media Drive";
    //public static int driveIdStorage = 344720;
    //public static String driveNameStorage = "HP Storage Drive";
    
    public static boolean forceFilesToBeInOrder = false;
    public static boolean forceFoldersToBeInOrder = false;
    public static boolean forceAllFilesToBeInOrder = false;
    public static boolean excludeSymbolicLinks = false;
    public static boolean printablePathsOnly = false;
    
    public static IStringList requiredFolders = new StringSimpleList();
    public static IStringList requiredFiles = new StringSimpleList();
    
    public static IStringList transientFiles = new StringSimpleList();
    public static IStringList transientFolders = new StringSimpleList();
    
    protected static String wheelerFolder = "C:\\Program Files\\Wheeler";
    private static String wheelerDataFolder(){ return composeFilepath(wheelerFolder, "data"); }
    public static String wheelerBatchFolder(){ return composeFilepath(wheelerDataFolder(), "batch"); }
    
    
    
    /// Functions ///
    
    // Initial setup for a file structure: make sure the program folders exist
    protected static boolean initializeInternal(JFrame caller){
        try{
            initializeWithErrorInternal();
        }
        catch(Exception e){
            DialogFactory.errorMsg(caller, "An error occurred while initializing the file structure:", e, 1, 0);
            return false;
        }
        return true;
    }
    protected static void initializeWithErrorInternal() throws Exception{
        initialize(requiredFolders.toArray(), requiredFiles.toArray());
    }
    private static void initialize(String[] folders, String[] files) throws Exception{
        for(int i = 0; i < folders.length; i++){
            ensureDirectoryExists(folders[i]);
        }
        for(int i = 0; i < files.length; i++){
            ensureFileExists(files[i]);
        }
    }
    
    // Post-execution cleanup
    protected static boolean teardownInternal(JFrame caller){
        try{
            teardownWithErrorInternal();
        }
        catch(Exception e){
            DialogFactory.errorMsg(caller, "An error occurred while tearing down the file structure:", e, 1, 0);
            return false;
        }
        return true;
    }
    protected static void teardownWithErrorInternal() throws Exception{
        teardown(transientFolders.toArray(), transientFiles.toArray());
    }
    private static void teardown(String[] folders, String[] files) throws Exception{
        for(int i = 0; i < files.length; i++){
            deleteFile(files[i]);
        }
        for(int i = 0; i < folders.length; i++){
            deleteFolder(folders[i]);
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
        for(int i = 0; i < contents.length; i++){
            if(fileExists(contents[i])){
                collection.add(contents[i]);
            }
            if(folderExists(contents[i])){
                collection.add(getFilesRecursive(contents[i]));
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
        for(int i = 0; i < subfolders.length; i++){
            if(folderExists(subfolders[i])){
                collection.add(subfolders[i]);
                collection.add(getSubfoldersRecursive(subfolders[i]));
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
        for (int i = 0; i < roots.length; i++)
            if (StringHandler.trimTrailingCharacters(path, "\\").equalsIgnoreCase(
                    StringHandler.trimTrailingCharacters(roots[i], "\\"))
                ) return true;
        return false;
    }
    
    
    // Is the given path absolute?
    public static boolean isAbsolute(String path){
        return fileObject(path).isAbsolute();
    }
    
    
    // Get drives registered with our drive IDs (stored in a root-level file named !driveId.gsw)
    /*public static String[] getRegisteredDrives(boolean includeSpace, boolean includeName){
        String[] roots = getRoots();
        StringList drives = new StringList();
        for (int i = 0; i < roots.length; i++){
            String regPath = composeFilepath(roots[i], "!driveId.gsw");
            if (fileExists(regPath)){
                try{
                    String driveSpace = getSpaceUsableOfTotal(roots[i]);
                    String driveName = driveIdToName(Integer.valueOf(readFile(regPath,true,false).pullFirst()));
                    if (driveName != null) drives.add(
                            roots[i]
                                + ((includeSpace) ? "\t(" + driveSpace + ")" : "")
                                + ((includeName) ? "\t" + driveName : "")
                        );
                }
                catch(Exception e){
                    //System.out.println("FileHandler.getRegisteredDrives: " + e.toString());
                }
            }
        }
        return drives.toArray();
    }*/
    private static String getSpaceUsableOfTotal(String path){
        File drive = new File((path.indexOf(":") != -1)
                ? path.substring(0, path.indexOf(":") + 1)
                : path);
        long usable = drive.getUsableSpace();
        long total = drive.getTotalSpace();
        return StringHandler.toReadableFileSize(usable) + " free of " + StringHandler.toReadableFileSize(total);
    }
    /*private static String driveIdToName(int id){
        if (id == driveIdMedia) return driveNameMedia;
        if (id == driveIdStorage) return driveNameStorage;
        return null;
    }*/
    
    
    // Rename a file. Make sure the two files are in the same folder
    public static void renameFile(String path, String name) throws Exception{
        File src = new File(path);
        if (!fileExists(path)) throw new Exception("The file \"" + path + "\" does not exist");
        File dst = new File(src.getParentFile().getPath() + "\\" + name);
        if (name.indexOf("\\") != -1) throw new Exception("Rename does not allow directory changes");
        if (fileExists(dst.getPath())) throw new Exception("The file \"" + dst.getPath() + "\" already exists");
        Files.move(src.toPath(), dst.toPath());
    }
    
    
    // Rename a file. Make sure the two files are in the same folder
    public static void renameFolder(String path, String name) throws Exception{
        File src = new File(path);
        if (!folderExists(path)) throw new Exception("The directory \"" + path + "\" does not exist");
        File dst = new File(src.getParentFile().getPath() + "\\" + name);
        if (name.indexOf("\\") != -1) throw new Exception("Rename does not allow directory changes");
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
        
        // Perform copy, recursively
        //   Create the containing folder
        //   Copy over any files
        //   Copy over any subfolders and any files they contain
        createFolder(dstPath);
        String[] subfiles = getFiles(srcPath);
        for(int i = 0; i < subfiles.length; i++){
            String filename = getFileName(subfiles[i]);
            String newPath = composeFilepath(dstPath, filename);
            copyFile(subfiles[i], newPath, overwriteExisting);
        }
        String[] subfolders = getSubfolders(srcPath);
        for(int i = 0; i < subfolders.length; i++){
            String filename = getFileName(subfolders[i]);
            String newPath = composeFilepath(dstPath, filename);
            copyFolderWithContents(subfolders[i], newPath, overwriteExisting);
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
        ensureDirectoryExists(getParentFolder(path));
        if(!fileExists(path)){
            writeToFile("", path);
            waitForFile(path, 10);
            return false;
        }
        return true;
    }
    
    public static void waitForFile(String path, long timeout) throws Exception{
        if (!(timeout > 0)) timeout = 60;
        long deadline = TimeHandler.ticks() + (timeout * 1000);
        while(!FileHandler.fileExists(path)){
            if (TimeHandler.ticks() > deadline)
                throw new Exception("File \"" + path + "\" failed to be created within " + timeout + " seconds");
            sleep(20);
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
        //fileObject(path).mkdir();
        long deadline = System.currentTimeMillis() + 10000;
        while (!folderExists(path))
            if (System.currentTimeMillis() > deadline)
                throw new Exception("Failed to create folder \"" + path + "\"");
            else
                try{Thread.sleep(5);}catch(Exception e){}
        return true;
    }
    
    
    // Make a folder exist, including the creation of parent folders if necessary
    public static boolean ensureDirectoryExists(String path) throws Exception{
        if (folderExists(path)) return true;
        String parent = getParentFolder(path);
        if (!folderExists(parent)) ensureDirectoryExists(parent);
        createFolder(path);
        return false;
    }
    /*public static boolean makeDir(String path) throws Exception { return createDirectory(path); }
    public static boolean mkDir(String path) throws Exception { return makeDir(path); }*/
    
    
    // Make a set of folders exist; use to create an entire tree if desired
    /*public static void createDirectories(String[] paths) throws Exception{
        for(int i = 0; i < paths.length; i++)
            createDirectory(paths[i]);
    }
    public static void makeDirs(String[] paths) throws Exception { createDirectories(paths); }
    public static void mkDirs(String[] paths) throws Exception { makeDirs(paths); }*/
    
    
    // Delete a folder's contents (including subfolders), then delete the folder itself
    // Timeout in seconds. -1 for no timeout, 0 for one-chance-only
    public static boolean deleteFile(String path) throws Exception { return deleteFile(path, 60); }
    public static boolean deleteFile(String path, int timeoutSeconds) throws Exception {
        String[] paths = {path}; return deleteFiles(paths, timeoutSeconds);
    }
    public static boolean deleteFiles(String[] paths) throws Exception { return deleteFiles(paths, 120); }
    public static boolean deleteFiles(String[] paths, int timeoutSeconds) throws Exception {
        // Delete the files
        boolean somethingWasDeleted = false;
        for(int i = 0; i < paths.length; i++){
            if (!fileExists(paths[i])) continue;
            fileObject(paths[i]).delete();
            somethingWasDeleted = true;
        }
        if (!somethingWasDeleted) return false;
        // Make the files got deleted
        long timeoutTime = System.currentTimeMillis() + (timeoutSeconds * 1000);
        String existingFile = "";
        long retryInterval = (4*1000);
        long deleteTime = TimeHandler.ticks() + retryInterval;
        while(System.currentTimeMillis() < timeoutTime){
            sleep(5);
            existingFile = null;
            boolean deleting = deleteTime > TimeHandler.ticks();
            for(int i = 0; i < paths.length; i++){
                if(fileExists(paths[i])){
                    if (existingFile == null) existingFile = paths[i];
                    if (deleting) fileObject(paths[i]).delete();
                }
            }
            if (existingFile == null) return true;
            if (deleting) deleteTime += retryInterval;
        }
        throw new Exception("Failed to delete file \"" + existingFile + "\" within " + timeoutSeconds + " seconds");
    }
    
    
    // Delete a folder's contents (including subfolders), then delete the folder itself
    // Timeout in seconds. -1 for no timeout, 0 for one-chance-only
    public static boolean deleteFolders(String[] paths) throws Exception { return deleteFolders(paths, 60); }
    public static boolean deleteFolders(String[] paths, int timeoutSeconds) throws Exception {
        boolean anyDeleted = false;
        for (int i = 0; i < paths.length; i++) if (deleteFolder(paths[i], timeoutSeconds)) anyDeleted = true;
        return anyDeleted;
    }
    public static boolean deleteFolder(String path) throws Exception { return deleteFolder(path, 60); }
    public static boolean deleteFolder(String path, int timeoutSeconds) throws Exception {
        // Check if it doesn't exist
        if (!folderExists(path)) return false;
        // If this is a symbolic link, only delete the LINK
        if(isSymbolicLink(path)){
            new File(path).delete();
            return true;
        }
        // Delete all files in this folder
        String[] files = getFiles(path);
        for(int i = 0; i < files.length; i++){ deleteFile(files[i], timeoutSeconds); }
        // Recursively delete all subfolders
        String[] folders = getSubfolders(path);
        for(int i = 0; i < folders.length; i++){ deleteFolder(folders[i], timeoutSeconds); }
        // Make sure everything got deleted
        long timeoutTime = System.currentTimeMillis() + (timeoutSeconds * 1000);
        String[] existingFiles;
        while((existingFiles = getContents(path)).length > 0){
            if(System.currentTimeMillis() > timeoutTime)
                throw new Exception("Failed to delete file/folder \"" + existingFiles[0] + "\" within " + timeoutSeconds + " seconds");
            Thread.sleep(5);
        }
        // Delete this folder
        new File(path).delete();
        while(folderExists(path)){
            if(System.currentTimeMillis() > timeoutTime)
                throw new Exception("Failed to delete folder \"" + path + "\" within " + timeoutSeconds + " seconds");
            Thread.sleep(5);
        }
        return true;
    }
    
    
    // Delete the contents of a folder
    public static void clearFolder(String path) throws Exception{
        if (!folderExists(path)) throw new Exception("The folder \"" + path + "\" does not exist");
        String[] contents = getFiles(path);
        for (int i = 0; i < contents.length; i++) deleteFile(contents[i]);
        contents = getSubfolders(path);
        for (int i = 0; i < contents.length; i++) deleteFolder(contents[i]);
    }
    
    
    // Check if a file exists
    public static boolean fileExists(String path){
        return new File(path).isFile();
        /*try{
            String[] subfiles = getFiles(getFileParent(path));
            for (int i = 0; i < subfiles.length; i++)
                if (path.equalsIgnoreCase(subfiles[i]))
                    return new File(path).isFile();
            return false;
        }
        catch(Exception e){
            return false;
        }*/
    }
    
    
    // Check if a folder exists
    public static boolean folderExists(String path){
        File file = fileObject(path); return file.isDirectory() && (file.listFiles() != null);
        //return new File(path).isDirectory();
        /*try{
            if (isRoot(path)) return true;
            String[] subfolders = getSubfolders(getFileParent(path));
            for (int i = 0; i < subfolders.length; i++)
                if (path.equalsIgnoreCase(subfolders[i]))
                    return new File(path).isDirectory();
            return false;
        }
        catch(Exception e){
            return false;
        }*/
    }
    
    
    // Check if there is anything there (at all)
    // Protected so we don't get confused with folderExists
    protected static boolean directoryExists(String path) throws Exception{
        if (fileObject(path).exists()) return true;
        String parent = getParentFolder(path);
        if (parent == null) return false;
        if (!folderExists(parent)) return false;
        return new StringList(getContents(parent)).contains(path);
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
            
            // If we're dropping empty trailing lines, pull lines until we find a non-empty one
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
