/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.error;

/**
 *
 * @author Greg
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
