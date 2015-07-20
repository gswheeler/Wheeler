/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.comm.sockets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import wheeler.generic.structs.StringList;

/**
 *
 * @author Greg
 */
public abstract class SocketBase implements Runnable{
    
    /// Variables
    protected boolean connected = false;
    protected boolean listening = false;
    protected boolean closed = false;
    protected PrintWriter out = null;
    protected BufferedReader in = null;
    private StringList messages = new StringList();
    private Semaphore messageLock = new Semaphore(1, true);
    private int maxStackSize;
    public Exception ex = null;
    
    
    
    /// Constructors
    private SocketBase(){}
    public SocketBase(int intMaxStackSize){
        maxStackSize = intMaxStackSize;
    }
    
    
    
    /// Functions
    
    // Loop on message reception, adding to the list of messages yet to be processed
    protected void listen(Socket socket, boolean isConnnected){
        try{
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = isConnnected;
            listening = true;
        }
        catch(Exception e){
            handleException(e); return;
        }
        
        while(listening){
            // Make sure the socket is still open
            if(socket.isClosed()){
                close(); return;
            }
            
            try{
                // Don't allow us to build a stack greater than X messages
                if(stackSize() >= maxStackSize){
                    try{Thread.sleep(50);}catch(Exception e1){try{Thread.sleep(50);}catch(Exception e2){}}
                    continue;
                }
                
                // Listen for an item to add to the stack
                String strLine = in.readLine();
                if(strLine != null){
                    messageLock.acquire();
                    messages.add(strLine);
                    messageLock.release();
                }else{
                    Thread.sleep(100);
                }
            }catch(Exception e){
                handleException(e);
            }
            
        }
    }
    
    
    // Get a message off the stack (removes it from the stack). Returns null if there is no message to return
    public String getMessage() throws Exception{
        // Watch for unreported socket issues (note: we need to check bool connected first)
        // Make sure we get ALL messages from the stack first
        if (!listening && !hasMessage()) throw new Exception("Message requested from stack after listening ceased.");
        
        // Get the topmost message if there is a message to get
        String message = null;
        if (hasMessage()){
            messageLock.acquire();
            message = messages.pullFirst();
            messageLock.release();
        }
        
        return message;
    }
    
    
    // Send a message across the sockets
    public void sendMessage(String message) throws Exception{
        // Watch for don't try sending anything before we've connected!
        if (!connected || closed) throw new Exception("Socket reports that it is not currently connected.");
        sendSocketMessage(message);
    }
    protected abstract void sendSocketMessage(String message);
    
    
    // Cleanly close the socket
    public void close(){
        listening = false;
        try{closeSocket();}catch(Exception e){handleException(e); return;}
        closed = true;
    }
    // Close the socket (specifically)
    protected abstract void closeSocket() throws Exception;
    
    
    // Handle an exception within the thread
    protected void handleException(Exception e){
        ex = e; closed = true; listening = false;
    }
    
    
    public boolean isConnected(){return connected;}
    public boolean isListening(){return listening;}
    public boolean isClosed(){return closed;}
    public int maxStackSize(){return maxStackSize;}
    public boolean hasMessage() throws Exception { return stackSize() > 0; }
    public int stackSize() throws Exception{
        messageLock.acquire(); int result = messages.length();
        messageLock.release(); return result;
    }
    
}
