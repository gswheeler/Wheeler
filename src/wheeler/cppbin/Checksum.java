/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.cppbin;

import javax.swing.JFrame;
import wheeler.generic.data.DialogFactory;
import wheeler.generic.data.FileHandler;
import wheeler.generic.data.StringHandler;

/**
 *
 * @author Greg
 */
public class Checksum {
    
    // Filepaths
    private static String version = "1.3";
    private static String chksumExe(String vStr){
        String path = "C:\\Program Files\\Wheeler\\cppbin\\checksum";
        if (versioningEnabled && (vStr != null)) path += " " + vStr;
        return path + ".exe";
    }
    
    // Operational variables
    public static long getMaxPartialLength(){ return positionStep; }
    protected static long positionStep = 1073741824; // First power of two below Integer_MAX
    protected static boolean versioningEnabled = true;
    
    // Get a command line for a batch file
    public static String getCommandLine(String resultPath, String initPath, int[] chkBases, int printCount, long startIndex, long length, String[] filepaths, JFrame caller) throws Exception{
        // Be forgiving with our versions
        String chksumExe = chksumExe(version);
        if (!FileHandler.fileExists(chksumExe)) chksumExe = chksumExe(null);
        
        // Start with the basics; we'll append arguments from here
        String line = StringHandler.addQuotes(chksumExe);
        
        // Check that the checksum executable is actually on the system
        if(!FileHandler.fileExists(chksumExe) && (caller == null || !DialogFactory.optionYesNo(caller,
                "Cannot find the checksum .exe on the system. Continue?")))
            throw new Exception("Could not find the checksum executable");
        
        // First the path the results will be written to
        if(resultPath == null || resultPath.length() == 0){
            throw new Exception("Did not provide a legitimate filepath (null or empty)");
        }else if(!FileHandler.folderExists(FileHandler.getParentFolder(resultPath))){
            throw new Exception("The parent folder of result path " + resultPath + " does not exist");
        }else{
            line += " -o " + StringHandler.addQuotes(resultPath);
        }
        
        // Now an optional path with data with which to initialize the checksum(s)
        if(initPath != null){
            if(initPath.length() == 0){
                throw new Exception("Did not provide a legitimate filepath (was empty)");
            }else if(!FileHandler.folderExists(FileHandler.getParentFolder(initPath))){
                // Cannot check for the existence of the init file itself; may be put in place by another part of the batch file
                throw new Exception("The parent folder of init path " + initPath + " does not exist");
            }else{
                line += " -i " + StringHandler.addQuotes(initPath);
            }
        }
        
        // Now any G arguments, if specified
        if(chkBases != null){
            line += " -g " + getNumString(chkBases);
            for (int i = 0; i < chkBases.length; i++) if (chkBases[i] < 2)
                throw new Exception("Cannot accept zero or one as a G argument (do the modular math)");
        }
        
        // Now the printout count
        line += " -c " + printCount;
        
        // Now the position/length values, if specified
        if(!((startIndex < 0) || (length < 0))){
            if (length > positionStep)
                throw new Exception("Not currently allowing partial reads over " + positionStep);
            
            line += " -p";
            line += " " + positionStep;
            line += " " + (startIndex / positionStep);
            line += " " + (startIndex % positionStep);
            line += " " + length;
        }
        
        // Now the version string
        line += " -v " + version;
        
        // Now all the filepaths
        if(filepaths == null || filepaths.length == 0){
            throw new Exception("Target filepaths was invalid (null or empty)");
        }else{
            for(int i = 0; i < filepaths.length; i++){
                if(filepaths[i] == null || filepaths[i].length() == 0){
                    throw new Exception("Got an invalid filepath (null or empty)");
                }else if(!FileHandler.fileExists(filepaths[i])){
                    throw new Exception("Could not find target filepath " + filepaths[i]);
                }else{
                    line += " " + StringHandler.addQuotes(filepaths[i]);
                }
            }
        }
        
        // That's it; return it
        return line;
    }
    
    
    // Turn an array of integers into a string of three-digit numbers
    public static String getNumString(int[] nums) throws Exception{
        String line = "";
        if(nums.length == 0){
            throw new Exception("Cannot have a number set with zero values");
        }else{
            for(int i = 0; i < nums.length; i++){
                if(nums[i] > 999 || nums[i] < 0){
                    throw new Exception("One of the numerical values was out of range (" + nums[i] + ")");
                }
                line += StringHandler.leadingZeroes(nums[i], 3);
            }
        }
        return line;
    }
    
}
