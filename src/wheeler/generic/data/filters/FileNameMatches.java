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
public class FileNameMatches extends BasicFilter {
    
    /// Constructor
    public FileNameMatches(String expression){
        nameExpr = expression;
    }
    
    
    
    /// Variables
    
    private String nameExpr;
    
    
    
    /// Functions
    
    // Accept override
    @Override
    public boolean accept(File file){
        return new FileIsFile().accept(file) && file.getName().matches(nameExpr);
    }
    
}
