/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.error;

/**
 * A handy exception for throwing if you want to signal yourself that an operation has been aborted.
 */
public class AbortException extends Exception{
    
    public AbortException(){
        super();
    }
    public AbortException(String message){
        super(message);
    }
    public AbortException(String message, Throwable cause){
        super(message, cause);
    }
    public AbortException(Throwable cause){
        super(cause);
    }
    
}
