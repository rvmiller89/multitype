/**The server processes and forwards FrontEndUpdates to clients
*@author John Lima
*/

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Server {
	private final int port;
	private ServerSocket ssocket;
	private boolean done;
	Vector<InputProcessor> inputProcs;
	Vector<OutputProcessor> outputProcs;
	
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
				InputProcessor thisInputProc = new InputProcessor(client);
				inputProcs.add(thisInputProc);
				new Thread(thisInputProc).start();
				
				//spawn an output processor for this client
				OutputProcessor thisOutputProc = new OutputProcessor(client);
				outputProcs.add(thisOutputProc);
				new Thread(thisOutputProc).start();
				
				//TODO Detect disconnect and kill the appropriate procs
				
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
