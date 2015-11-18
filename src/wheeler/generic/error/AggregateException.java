/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.error;

import wheeler.generic.data.StringHandler;

/**
 * An exception for throwing when multiple errors exist.
 * Use the static create(String, String[]) method to create a new instance to throw.
 */
public class AggregateException extends Exception {
    
    private AggregateException(String message){
        super(message);
    }
    
    public static AggregateException create(String message, String[] errors){
        for(String error : errors){
            message += "\n    " + StringHandler.replace(error, "\n", "\n    ", true);
        }
        return new AggregateException(message);
    }
    
}
