/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.error;

/**
 * An exception that can be put in the code without requiring a throws declaration.
 */
public class QuietException extends RuntimeException{
    public QuietException(){
        super();
    }
    public QuietException(String message){
        super(message);
    }
    public QuietException(String message, Throwable cause){
        super(message, cause);
    }
    public QuietException(Throwable cause){
        super(cause);
    }
}
