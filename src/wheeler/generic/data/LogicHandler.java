/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import wheeler.generic.error.QuietException;
import wheeler.generic.logging.Logger;
import wheeler.generic.structs.StringList;

/**
 *
 * @author Greg
 */
public class LogicHandler {
    
    /**Have the current thread sleep for a bit.
     * Captures and logs exceptions.
     * Does not trigger "sleep called in a loop" warnings.
     * @param millis The number of milliseconds to sleep for.
     */
    public static void sleep(long millis){
        if (millis < 0) return;
        try{
            Thread.sleep(millis);
        }
        catch(Exception e){
            // Hopefully this never gets called; if it's in a loop the log will likely fill up fast
            Logger.error("Thread.sleep threw an error", e, 1, 0);
        }
    }
    
    
    /**Kicks off a runnable object to run in its own thread
     * @param runner The runnable object whose run() function is to be run in its own thread
     * @return A reference to the thread running the runnable object
     */
    public static Thread startThread(Runnable runner){
        Thread thread = new Thread(runner);
        thread.start();
        return thread;
    }
    
    
    private static final Semaphore _lockStringSemaphore = new Semaphore(1, true);
    private static final StringList _lockStringDict = new StringList();
    
    public static void lock(String lock, int timeout) throws Exception{
        if (!tryLock(lock, timeout))
            throw new Exception("Failed to lock " + lock + " within " + timeout + " seconds");
    }
    public static boolean tryLock(String lock, int timeout) throws Exception{
        return tryLock(lock, 0, timeout);
    }
    private static boolean tryLock(String lock, int holdTime, int timeout) throws Exception{
        // When should we stop? If timeout is zero will only give it one try
        long deadline = TimeHandler.ticks() + ((long)(timeout * 1000));
        
        // Keep trying while there's still time. If timeout is zero, will only try once
        do{
            lock(_lockStringSemaphore, (timeout > 0) ? timeout : 10);
            boolean gotLock = false;
            Exception ex = null;
            try{
                /*
                // When we've written StringToStringDictionary, allow strings to be locked for only so long
                String currLock = _lockStringDict.get(lock);
                if(currLock == null){
                    // No existing lock; it's ours
                    gotLock = true;
                }else if(currLock.isEmpty){
                    // A lock with no timeout; hands off
                    gotLock = false;
                }else if(Long.valueOf(currLock) < TimeHandler.ticks()){
                    // A lock that has expired; grab it
                    gotLock = true;
                }else{
                    // A lock that has not yet expired; hands off
                    gotLock = false;
                }
                */
                // For now, there not being a claim is enough
                gotLock = !_lockStringDict.contains(lock);
                if (gotLock)
                    /*
                    _lockStringDict.set(lock, (hold > 0)
                            ? Long.toString(TimeHandler.ticks() + ((long)(holdTime*1000)))
                            : ""
                        );
                    */
                    _lockStringDict.add(lock);
            }
            catch(Exception e){
                ex = e;
            }
            release(_lockStringSemaphore);
            if (ex != null) throw ex;
            if (gotLock){ return true; }else{ sleep(10); }
        }while(deadline > TimeHandler.ticks());
        
        // Timed out, never got it
        return false;
    }
    public static boolean release(String lock, int timeout) throws Exception{
        lock(_lockStringSemaphore, timeout);
        Exception ex = null;
        boolean result = false;
        try{
            result = _lockStringDict.remove(lock) > 0;
        }
        catch(Exception e){
            ex = e;
        }
        release(_lockStringSemaphore);
        if (ex != null) throw ex;
        return result;
    }
    
    
    public static void lock(Semaphore lock, int timeout) throws Exception{
        if (!tryLock(lock, timeout))
            throw new Exception("Failed to lock a semaphore within " + timeout + " seconds");
    }
    public static boolean tryLock(Semaphore lock, int timeout){
        try{
            return lock.tryAcquire(timeout, TimeUnit.SECONDS);
        }
        catch(Exception e){
            throw new QuietException("acquireLock(Semaphore) threw an Exception", e);
        }
    }
    public static void release(Semaphore lock){
        lock.release();
    }
    
    
    public static String[] addToArray(String[] array, String item){
        String[] newArray = new String[array.length + 1];
        int index;
        for (index = 0; index < array.length; index++) newArray[index] = array[index];
        newArray[index] = item;
        return newArray;
    }
    
    
    public static int[] addToArray(int[] array, int item){
        int[] newArray = new int[array.length + 1];
        int index;
        for (index = 0; index < array.length; index++) newArray[index] = array[index];
        newArray[index] = item;
        return newArray;
    }
    
    
    public static int getArrayIndex(String[] array, String str, boolean checkCase){
        for(int i = 0; i < array.length; i++)
            if(StringHandler.areEqual(str, array[i], checkCase))
                return i;
        return -1;
    }
    
    
    public static String[] concatArrays(String[] arrayA, String[] arrayB){
        String[] newArray = new String[arrayA.length + arrayB.length];
        int arrayIndex = 0;
        for (String item : arrayA) newArray[arrayIndex++] = item;
        for (String item : arrayB) newArray[arrayIndex++] = item;
        return newArray;
    }
    
    
    public static String[] concatArrays(String[][] arrays){
        // Get the total length
        int length = 0; for (String[] array : arrays) length += array.length;
        String[] newArray = new String[length];
        
        int arrayIndex = 0;
        for(String[] array : arrays){
            for (String item : array)
                newArray[arrayIndex++] = item;
        }
        return newArray;
    }
    
    
    public static String[] reverseArray(String[] arrayA){
        String[] arrayB = new String[arrayA.length];
        for (int i = 0; i < arrayA.length; i++) arrayB[i] = arrayA[arrayA.length - i - 1];
        return arrayB;
    }
    
    
    public static boolean arrayContains(String[] array, String str, boolean caseSensitive){
        for(String item : array){
            if (StringHandler.areEqual(item, str, caseSensitive)) return true;
        }
        return false;
    }
    
    
    public static String[] createSingularArray(String str){
        String[] array = new String[1]; array[0] = str; return array;
    }
    
    
    /**Create an array by copying the contents of an existing array.
     * Does not incur the "use System.arraycopy" warning.
     * @param array The array whose data is to be copied into the new array
     * @return An array with the same data as the original
     */
    public static String[] duplicateArray(String[] array){
        String[] result = new String[array.length];
        int index = 0;
        for (String str : array) result[index++] = str;
        return result;
    }
    
    
    // Shuffle an array of strings
    public static String[] shuffleArray(String[] array){
        String[] result = duplicateArray(array);
        for(int i = 0; i < array.length - 1; i++){
            // Randomly populate each index with one of the values of the array not already chosen.
            // With each index, the remaining "pool" will be all values at or after the index.
            // Skip the last index; will only have the one value to choose from.
            swap(result, i, MathHandler.getRandomNumber(i, result.length - 1));
        }
        return result;
    }
    // Swap two strings in an array
    public static void swap(String[] array, int pos1, int pos2){
        if (pos1 == pos2) return;
        String swap = array[pos1]; // Store A
        array[pos1] = array[pos2]; // B to A
        array[pos2] = swap;        // A to B
    }
    
    
    // Copy a string to the clipboard
    public static void copyToClipboard(String str){
        StringSelection strSel = new StringSelection(str);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(strSel, null);
    }
    
    
    // Was a click event a right-click?
    public static boolean clickIsRightClick(java.awt.event.MouseEvent evt){
        return evt.getButton() == MouseEvent.BUTTON3;
    }
    
    
    // Populate a drop-down menu
    public static void setDropdownContents(JComboBox dropdown, Object[] data){
        dropdown.setModel(new DefaultComboBoxModel(data));
    }
    
    
    // What should happen when the window is closed?
    // Drop the window
    public static void setDisposeOnClose(JFrame window){
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
    // Exit the program or do nothing
    public static void setExitOnClose(JFrame window, boolean exit){
        window.setDefaultCloseOperation(exit ? WindowConstants.EXIT_ON_CLOSE : WindowConstants.DISPOSE_ON_CLOSE);
    }
    
    
    /**Convert an Exception into a String that actually has a stacktrace
     * @param e The exception we're converting to a string
     * @param traceLevel 1 for relevant, 0 for none, -1 for FULL
     * @param indirection number of nested calls between the catch block and the call to this function (zero if called directly from the catch block, negatives don't work)
     * @return 
     */
    public static String exToString(Exception e, int traceLevel, int indirection){
        if (traceLevel == 0) return e.toString();
        
        // This prints out a proper error message
        // Line 1 is the exception type and message
        // All subsequent lines are the lines of the stacktrace
        //  (starting with the one that threw the exception)
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.flush();
        String stackTrace = writer.toString();
        
        if(traceLevel > 0){
            // Cut off everything after the method that caught this exception
            try{
                // Split the trace into individual lines
                String caller = getCallingMethod(1 + indirection);
                String[] trace = StringHandler.parseIntoArray(stackTrace, "\n");
                int length = trace.length;
                
                // Find the last line with the calling method
                for(int i = trace.length; i > 0; i--){
                    if (!StringHandler.contains(trace[i-1], caller, false)) continue;
                    length = i;
                    break;
                }
                
                // Put all the lines back together in a smaller stacktrace
                if(length < trace.length && length > 1){
                    stackTrace = trace[0];
                    for(int i = 1; i < length; i++){
                        stackTrace += "\n" + trace[i];
                    }
                    stackTrace = StringHandler.trimNewlines(stackTrace);
                }
            }
            catch(Exception e2){
                Logger.error("LogicHandler.exToString", e2.toString());
            }
        }
        String result = StringHandler.replace(stackTrace, "\t", "        ", true);
        
        Throwable innerException = e.getCause();
        if((innerException != null) && (innerException instanceof Exception)){
            result += "\n--------------------\n"
                    + exToString((Exception)innerException, traceLevel, indirection+1);
        }
        
        return result;
    }
    
    
    /**Get the method signature of the function that called this one
     * @param indirection Zero if the method with the desired signature calls this function directly, otherwise the number of calls between that method and the call to this one
     * @return The period-delineated classpath of the method's containing class, followed by a period and the method name
     * @throws Exception Not explicit, but it's best that we not assume here given that this is often called in error-recovery situations.
     */
    public static String getCallingMethod(int indirection) throws Exception{
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        StackTraceElement element = ste[2 + indirection];
        return element.getClassName() + "." + element.getMethodName();
    }
    
    
    protected static long _processId = -1;
    
    // Get the process ID
    public static long getProcessId(){
        if (_processId != -1) return _processId;
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        String runtimeName = runtimeBean.getName(); // pid@machinename
        if (!runtimeName.matches("[0-9]+@.+"))
            throw new QuietException("RuntimeName did not match expected format (" + runtimeName + ")");
        long pid = Long.valueOf(runtimeName.substring(0, runtimeName.indexOf("@")));
        _processId = pid;
        return pid;
    }
    
    
}
