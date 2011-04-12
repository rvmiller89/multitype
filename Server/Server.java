/**The server processes and forwards FrontEndUpdates to clients
*@author John Lima
*/

import java.io.*;
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
		
		//fum.addFile(0, "test.java");
		
		try {
			ssocket = new ServerSocket(port);
		}
		catch (IOException ioe) {
			System.err.println("Server(): " + ioe.toString());
		}
		
		fum.add_debug(this); //DEBUG
		
	}
	
	public void serve() {
		/* new Thread(new Runnable() { public void run() {
				System.out.println("ASDF");
				while(!done) {
					System.err.println("Dump: \n" + dump());
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();*/
		
		//Spawn all appropriate threads here
		
		while(!done) {
			try {
				Socket client = ssocket.accept();
				dprint("Accepted Client");
				//spawn an input processor for this client
				InputProcessor thisInputProc = new InputProcessor(client, fum, np);
				inputProcs.add(thisInputProc);
				
				//spawn an output processor for this client
				/*
				 * This has been moved to inside FileUserManager 
			 	OutputProcessor thisOutputProc = new OutputProcessor(client);
				outputProcs.add(thisOutputProc);
				new Thread(thisOutputProc).start();*/
				
				thisInputProc.setUID(fum.addClient(client));
				
				new Thread(thisInputProc).start();
				
				dprint("Client Added");
				
				
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
				dprint("Server Running.");
				serv.serve();
				
			}
			catch (NumberFormatException nfe) {
				System.err.println("First argument must be the port number.");
			}
		}
		else {
			System.err.println("Needs a port number as the first and only argument.");
		}

		
	}
	
	public static void dprint(String s) {
		System.err.println(s);
	}
	
	public void dump() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("dump.txt", true));
			writer.write(fum.dump());
			writer.write("\n\n\n\n\n\n");
			writer.close();
		}
		catch (Exception e) {
			System.err.println("Error in dump");
		}
		
	}
}
