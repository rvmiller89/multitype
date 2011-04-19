/**
 * Enqueues and sends FEUs to a client
 * @author John Lima
 *
 */

import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.nio.channels.ClosedByInterruptException;

import multitype.FrontEndUpdate;

public class OutputProcessor implements Runnable {

	//Queue to hold the updates waiting to be sent
	BlockingQueue<FrontEndUpdate> outQueue;
	
	//[Output] socket to client 
	Socket sclient;
	
	//flag to allow thread to exit
	boolean done;
	
	//FUM for alerting everyone if a write to socket fails
	FileUserManager fum;
	
	//ID needed for above
	int uid;
	
	/**
	 * Constructor for OutputProcessor
	 * @param client A socket connection to the client who will receive
	 * the updates
	 */
	public OutputProcessor(Socket client, FileUserManager f, int i) {
		sclient = client;
		outQueue = new LinkedBlockingQueue<FrontEndUpdate>();
		done = false;
		fum = f;
		uid = i;
	}
	
	/**
	 * Adds an FEU to the queue of outgoing updates
	 * @param f FEU to be added to the queue
	 */
	public void addFEU(FrontEndUpdate f) {
		try {
			outQueue.put(f);
		} catch (InterruptedException e) {
			System.err.println("addFEU(): " + e.toString());
		}
	}
	
	@Override
	public void run() {

		try {
            OutputStream output = sclient.getOutputStream();
			
			ObjectOutputStream out = new ObjectOutputStream(output);
			while(!done) {
				
				FrontEndUpdate toWrite = outQueue.take();
				
				if(toWrite == null) {
					continue;
				}
				try {
					Server.dprint("Sending: " + toWrite.toString());
					
					out.writeObject(toWrite);
				}
				catch (IOException ioe) {
					System.err.println("OutputProcessor: writeObject failed: "
							+ ioe.toString());

					if(uid == fum.getHost()) {
						fum.sendFEUToAll(FrontEndUpdate.createNotificationFEU(
								FrontEndUpdate.NotificationType.Host_Disconnect,-1,
								uid, ""));
						fum.removeHost();
					}
					else {
						fum.sendFEUToAll(FrontEndUpdate.createNotificationFEU(
								FrontEndUpdate.NotificationType.User_Disconnected,-1,
								uid, ""));
					}
					fum.removeClient(uid);
				}
			}
			
			out.close();
			
		}
		catch (InterruptedException ie) {
			System.err.println("OutputProcessor: Interrupted... exiting." + ie.toString());
			return;
		}
		catch (ClosedByInterruptException cbie) {
			System.err.println("OutputProcessor: ClosedByInterrupt... exiting.");
			return;
		}
		catch (IOException ioe) {
			System.err.println("OutputProcessor(): " + ioe.toString());
		}
	}
	
	public String dump() {
		StringBuilder sb = new StringBuilder();
		
		FrontEndUpdate[] oqa = this.outQueue.toArray(new FrontEndUpdate[0]);
		for(int i = 0; i < oqa.length; i++) {
			sb.append("FEU " + i + ": " + oqa[i].toLine());
		}
		
		return sb.toString();
	}
	
	
	/**
	 * Call this to terminate thread
	 */
	public void setDone() {
		done = true;
		//Thread.interrupt();

	}

}
