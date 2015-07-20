/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data.readers;

import java.io.OutputStream;
import java.nio.file.Files;
import wheeler.generic.data.FileHandler;
import wheeler.generic.data.StringHandler;
import wheeler.generic.logging.Logger;

/**
 *
 * @author Greg
 */
public class FileWriter {
    
    protected OutputStream writeOut;
    protected java.io.FileWriter appender;
    protected boolean closed = true;
    protected boolean appending;
    
    public FileWriter(String path) throws Exception{
        this(path, false);
    }
    public FileWriter(String path, boolean appendOnly) throws Exception{
        appending = appendOnly;
        // Make sure the file's parent folder exists
        if (!FileHandler.folderExists(FileHandler.getParentFolder(path)))
            throw new Exception("Containing folder of file " + path + " does not exist");
        
        // Open the file
        try{
            closed = false;
            if(appending)
                appender = new java.io.FileWriter(FileHandler.fileObject(path), true);
            else
                writeOut = Files.newOutputStream(FileHandler.fileObject(path).toPath());
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
    
    public void writeLine(String strLine) throws Exception{ write(strLine + "\r\n"); }
    public void write(String strLine) throws Exception{
        if (closed) throw new Exception("Tried to write to FileWriter after it was closed");
        
        try{
            if (!StringHandler.isWritable(strLine))
                throw new Exception("Tried to write non-writable data:\n" + StringHandler.escape(strLine));
            if(appending)
                if (appender != null) appender.write(strLine);
                else throw new Exception("Was appending lines but appender was null");
            else
                if (writeOut != null) writeOut.write(strLine.getBytes());
                else throw new Exception("Was writing the file in full but writeOut was null");
        }
        catch(Exception e){
            try{
                close();
            }
            catch(Exception e2){
                // Already have an exception
                Logger.error("Encountered an error closing the FileWriter after a write error", e2, 1, 0);
            }
            throw e;
        }
    }
    
    public void write(int c) throws Exception{
        if (closed) throw new Exception("Tried to write to FileWriter after it was closed");
        
        try{
            if ((c > 255) || (c < 0))
                throw new Exception("Tried to write a character with a value of " + c + ", which cannot be read back out");
            if(appending)
                if (appender != null) appender.write(c);
                else throw new Exception("Was appending lines but appender was null");
            else
                if (writeOut != null) writeOut.write(c);
                else throw new Exception("Was writing the file in full but writeOut was null");
        }
        catch(Exception e){
            try{
                close();
            }
            catch(Exception e2){
                // Already have an exception
                Logger.error("Encountered an error closing the FileWriter after a write error", e2, 1, 0);
            }
            throw e;
        }
    }
    
    public final void close() throws Exception{
        if (closed) return;
        closed = true;
        if (writeOut != null) writeOut.close();
        if (appender != null) appender.close();
    }
    
}
