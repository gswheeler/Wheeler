/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.error;

import wheeler.generic.data.StringHandler;

/**
 *
 * @author Greg
 */
public class AggregateException extends Exception {
    
    private AggregateException(String message){
        super(message);
    }
    
    public static AggregateException create(String message, String[] errors){
        for(int i = 0; i < errors.length; i++){
            message += "\n    " + StringHandler.replace(errors[i], "\n", "\n    ", true);
        }
        return new AggregateException(message);
    }
    
}
