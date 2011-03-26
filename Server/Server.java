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
	//Vector<OutputProcessor> outputProcs;
	FileUserManager fum;
	NotificationProcessor np;
	
	//Constructor
	public Server(int _port) {
		done = false;
		port = _port;
		fum = new FileUserManager();
		np = new NotificationProcessor(fum);
		inputProcs = new Vector<InputProcessor>();
		
		new Thread(np).start();
		
		fum.addFile(0, "test.java");
		
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
				InputProcessor thisInputProc = new InputProcessor(client, fum, np);
				new Thread(thisInputProc).start();
				inputProcs.add(thisInputProc);
				
				//spawn an output processor for this client
				/*
				 * This has been moved to inside FileUserManager 
			 	OutputProcessor thisOutputProc = new OutputProcessor(client);
				outputProcs.add(thisOutputProc);
				new Thread(thisOutputProc).start();*/
				
				fum.addClient(client);
				
				System.out.println("Client Added");
				
				
				//TODO Detect disconnect and kill the appropriate procs
				
			}
			catch (IOException ioe) {
				System.err.println("serve(): Error accepting client connection: " + ioe.toString());
			}
		}
		
	}
	
	public static void main(String[] args) {
		//parse input
		
		if(args.length == 1) {
			try {
				int port;
				Server serv;
				port = Integer.parseInt(args[0]);
				serv = new Server(port);
				System.out.println("Server Running.");
				serv.serve();
				
			}
			catch (NumberFormatException nfe) {
				System.err.println("First argument must be the port number.");
			}
		}

		
	}
	
	//Host a socket connection
	
	//Run Input Manager (in a thread)

}
