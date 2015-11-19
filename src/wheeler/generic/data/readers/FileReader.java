/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data.readers;

import java.io.FileInputStream;
import java.io.InputStream;
import wheeler.generic.data.FileHandler;
import wheeler.generic.data.StringHandler;

/**
 * Used to read from files.
 * Handles opening, reading from, and closing input streams.
 */
public class FileReader {
    
    protected InputStream fStream;
    protected boolean closed = true;
    
    public FileReader(String path) throws Exception{
        // Make sure the file exists
        if (!FileHandler.fileExists(path))
            throw new Exception("File " + path + " does not exist");
        
        // Open the file
        try{
            closed = false;
            fStream = new FileInputStream(path);
        }
        catch(Exception e){
            try{
                close();
            }
            catch(Exception e2){
                // Already have an exception
            }
            throw e;
        }
    }
    protected FileReader(){}
    
    // More complicated than it looks; br.readLine() automatically removes newlines
    public String readLine(boolean trimNewlines) throws Exception{
        // The reader is closed if we hit end-of-file during the last read
        // (said read still returned a string, so now is the time to return a null)
        if (closed) return null;
        
        try{
            // Gather characters until a newline or end-of-file
            String strLine = "";
            int inChar;
            while(true){
                // Get the next character
                inChar = readChar();
                
                // If we hit the end of the file, close the reader and return the last of the data
                if(inChar == -1){
                    close();
                    break;
                }
                
                // Add the character. If we hit a newline, pause reading to return what we have
                strLine += (char)inChar;
                if ((char)inChar == '\n') break;
            }   
            
            // Return the line, trimming newlines if desired
            return trimNewlines
                ? StringHandler.trimNewlines(strLine)
                : strLine;
        }
        catch(Exception e){
            try{
                close();
            }
            catch(Exception e2){
                // Already have an exception
            }
            throw e;
        }
    }
    public String readTrimmedLine() throws Exception {
        String line = readLine(false);
        return (line != null) ? line.trim() : null;
    }
    public String readContentLine(boolean trimNewlines) throws Exception{
        String line = "";
        while(line.length() < 1){
            line = readLine(trimNewlines);
            if (line == null) return null;
        }
        return line;
    }
    
    public int readChar() throws Exception{
        if (closed) return -1;
        int c = fStream.read();
        if (c == -1) close();
        return c;
    }
    
    public final void close() throws Exception{
        if (closed) return;
        closed = true;
        fStream.close();
    }
    
}
