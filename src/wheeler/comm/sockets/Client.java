/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.comm.sockets;

import java.net.Socket;

/**
 *
 * @author Greg
 */
public class Client extends SocketBase{
    
    /// Variables
    private String address;
    private int port;
    private Socket client;
    
    
    
    /// Constructors
    public Client(String strAddr, int intPort, int maxStackSize){
        super(maxStackSize);
        address = strAddr;
        port = intPort;
    }
    
    
    
    /// Functions
    
    // Start operations in a new thread
    @Override
    public void run(){
        // Create a socket and try for a connection to the specified address, then start receiving messages
        try{
            client = new Socket(address, port);
            listen(client, client.isConnected());
        }
        catch(Exception e){
            handleException(e);
        }
    }
    
    
    @Override
    protected void sendSocketMessage(String message){
        out.println(message);
        out.flush();
    }
    
    
    @Override
    protected void closeSocket() throws Exception{
        client.close();
    }
    
}
