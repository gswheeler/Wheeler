/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data.filters;

import java.io.File;
import wheeler.generic.logging.Logger;

/**
 *
 * @author Greg
 */
public class FileIsFolder extends BasicFilter {
    
    /// Constructor
    public FileIsFolder(){}
    
    
    
    /// Functions
    
    @Override
    public boolean accept(File file){
        if (!file.isDirectory()) return false;
        if (!fileMeetsCriteria(file)) return false;
        if(file.listFiles() == null){
            Logger.warn("FileIsFolderFilter", "Unusable folder detected: " + file.getPath());
            return false;
        }
        return true;
    }
    
}
