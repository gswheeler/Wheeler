/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data.filters;

import java.io.File;

/**
 *
 * @author Greg
 */
public class FileIsFile extends BasicFilter {
    
    /// Constructor
    public FileIsFile(){}
    
    
    
    /// Functions
    
    // Accept override
    @Override
    public boolean accept(File file){
        return file.isFile() && fileMeetsCriteria(file);
    }
    
}
