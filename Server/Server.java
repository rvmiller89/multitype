/**The server processes and forwards FrontEndUpdates to clients
*@author John
*/

import java.io.IOException;
import java.net.*;

public class Server {
	private final int port;
	private ServerSocket ssocket;
	private boolean done;
	
	//Constructor
	public Server(int _port) {
		done = false;
		port = _port;
		try {
			ssocket = new ServerSocket(port);
		}
		catch (IOException ioe) {
			System.err.println("Server(): " + ioe.toString());
		}
		
	}
	
	public void serve() {
		//Spawn all appropriate threads here
		while(!done) {
			try {
				Socket client = ssocket.accept();
				
				//spawn an input processor for this client
				new Thread(new InputProcessor(client)).start();
				
				//spawn an output processor for this client?
			}
			catch (IOException ioe) {
				System.err.println("serve(): Error accepting client connection: " + ioe.toString());
			}
		}
	}
	
	public static void main(String[] args) {
		//parse input
		
		try {
			int port;
			Server serv;
			port = Integer.parseInt(args[0]);
			serv = new Server(port);
			serv.serve();
			
		}
		catch (NumberFormatException nfe) {
			System.err.println("First argument must be the port number.");
		}
		

		
	}
	
	//Host a socket connection
	
	//Run Input Manager (in a thread)

}
