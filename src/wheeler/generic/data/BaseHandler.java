/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data;

/**
 *
 * @author Greg
 */
public abstract class BaseHandler {
    
    protected static void sleep(long millis){
        LogicHandler.sleep(millis);
    }
    
    protected static String[] concatArrays(String[] arrayA, String[] arrayB){
        return LogicHandler.concatArrays(arrayA, arrayB);
    }
    
}
