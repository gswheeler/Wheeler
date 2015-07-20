/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.comm.sockets;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author Greg
 */
public class Server extends SocketBase{
    
    /// Variables
    private ServerSocket server = null;
    private int port;
    
    
    
    /// Constructors
    public Server(int maxStackSize) throws IOException{
        super(maxStackSize);
        while((port = getPossiblePort()) > 0){
            try{
                server = new ServerSocket(port);
                port = server.getLocalPort();
                return;
            }
            catch(Exception ex){
                // SWALLOW IT!
            }
        }
        throw new IOException("Could not establish a listen port");
    }
    
    
    
    /// Functions
    
    // Start operations in a new thread
    @Override
    public void run(){
        // Accept a connection request and start receiving messages
        try{
            listen(server.accept(), true);
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
        server.close();
    }
    
    
    // Get a swathe of possible port numbers
    private int lastPortIndex = 0;
    private int getPossiblePort(){
        int port = 4321; int count = lastPortIndex++;
        int lAdvance = count / 625; count = count % 625;
        int kAdvance = count / 125; count = count % 125;
        int cAdvance = count / 25; count = count % 25;
        int dAdvance = count / 5; count = count % 5;
        int uAdvance = count;
        
        if (lAdvance > 4) return -1;
        
        port += lAdvance * 10000;
        port += kAdvance * 1000;
        port += cAdvance * 100;
        port += dAdvance * 10;
        return port + uAdvance;
    }
    
    
    // Get the port to which the server is bound
    public int getPort(){return port;}
    
}
