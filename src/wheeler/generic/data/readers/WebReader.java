/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data.readers;

import java.net.URL;

/**
 * Used to read from a web address.
 * Handles opening, reading from, and closing the input stream.
 */
public class WebReader extends FileReader {
    
    public WebReader(String url) throws Exception{
        closed = false;
        fStream = new URL(url).openStream();
    }
    
}
