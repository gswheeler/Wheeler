/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.vbbin;

import wheeler.generic.data.FileHandler;
import wheeler.generic.data.StringHandler;

/**
 *
 * @author Greg
 */
public class SoundPlayer {
    
    // Filepaths
    private final String exePath = "C:\\Program Files\\Wheeler\\vbbin\\soundplayer.exe";
    private final String autorun = "C:\\Program Files\\Wheeler\\Sound Player\\autorun.cmd";
    private final String callersFolder = "C:\\Program Files\\Wheeler\\Sound Player\\data\\callers";
    private final String requestsFolder = "C:\\Program Files\\Wheeler\\Sound Player\\data\\requests";
    private final String tempFolder = "C:\\Program Files\\Wheeler\\Sound Player\\data\\temp";
    private final String version = "1.0";
    private String sessTag = null;
    
    
    // Set up this session
    public SoundPlayer(String identifier) throws Exception{
        // Make sure the paths are present
        FileHandler.ensureDirectoryExists(callersFolder);
        FileHandler.ensureDirectoryExists(requestsFolder);
        FileHandler.ensureDirectoryExists(tempFolder);
        
        // Get a tag for our session
        sessTag = StringHandler.toAlphaNumeric(System.currentTimeMillis());
        FileHandler.writeToFile(identifier, sessFile(sessTag));
        
        // If ours is the only game in town, start the service
        if(FileHandler.getFiles(callersFolder).length == 1){
            // Write the file if not already present
            if (!FileHandler.fileExists(autorun)){
                FileHandler.writeToFile("@echo off\r\n\"" + exePath + "\"\r\nexit \\B", autorun);
            }
            // Run the batch file
            FileHandler.runBatchFile(autorun);
        }
    }
    
    
    // Play a sound
    public void playSound(Sound req) throws Exception{playSound(req.wavFile, req.millis, req.force);}
    public void playSound(String wavFile, int millis) throws Exception{playSound(wavFile, millis, false);}
    public void playSound(String wavFile, int millis, boolean force) throws Exception{
        // Skip the exception; just don't play it, like we do with logging
        if (sessTag == null) return;
        //if (sessTag == null) throw new Exception("Tried to play a sound before SoundPlayer session was created");
        
        // Form the request string
        String request = version
            + "," + Integer.toString(millis)
            + "," + (force ? "1" : "0")
            + "," + wavFile;
        
        // Compose the filenames
        String filename = sessTag + "-" + StringHandler.toAlphaNumeric(System.currentTimeMillis()) + ".txt";
        String tmpFile = FileHandler.composeFilepath(tempFolder, filename);
        String reqFile = FileHandler.composeFilepath(requestsFolder, filename);
        
        // Write the request into a file, then move it to where it'll be found
        FileHandler.writeToFile(request, tmpFile);
        FileHandler.moveFile(tmpFile, reqFile);
    }
    
    
    // Close this session
    public void closeSession() throws Exception{
        if (sessTag == null) return;
        FileHandler.deleteFile(sessFile(sessTag));
        sessTag = null;
    }
    
    
    // Get the session file for a given tag
    private String sessFile(String tag){
        return FileHandler.composeFilepath(callersFolder, tag + ".txt");
    }
    
    
    // A convenient object for hard-coding sound requests
    public class Sound{
        public String wavFile;
        public int millis;
        public boolean force;
        private SoundPlayer player;
        
        protected Sound(String file, int time, boolean now, SoundPlayer handler){
            wavFile = file; millis = time; force = now; player = handler;
        }
        
        public void play() throws Exception{
            player.playSound(this);
        }
    }
    public Sound sound(String file, int time){return sound(file, time, false);}
    public Sound sound(String file, int time, boolean force){
        return new Sound(file, time, force, this);
    }
    
    
}
