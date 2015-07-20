/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data.filters;

import java.io.File;
import java.io.FileFilter;
import wheeler.generic.data.FileHandler;
import wheeler.generic.data.StringHandler;
import wheeler.generic.logging.Logger;

/**
 *
 * @author Greg
 */
public class BasicFilter implements FileFilter {
    
    @Override
    public boolean accept(File file){
        return fileMeetsCriteria(file);
    }
    
    protected boolean fileMeetsCriteria(File file){
        if (FileHandler.excludeSymbolicLinks && FileHandler.isSymbolicLink(file.getPath())) return false;
        if (FileHandler.printablePathsOnly && !StringHandler.isPrintable(file.getPath())) return false;
        if (!file.exists()) Logger.warn("BaseFileFilter", "Broken path detected: " + file.getPath());
        return true;
    }
    
}
