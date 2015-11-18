/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.error;

/**
 * A generic exception for when the code gets itself someplace that doesn't make sense.
 */
public class LogicException extends Exception {
    
    public LogicException(){
        super();
    }
    public LogicException(String message){
        super(message);
    }
    public LogicException(String message, Throwable cause){
        super(message, cause);
    }
    public LogicException(Throwable cause){
        super(cause);
    }
    
}
