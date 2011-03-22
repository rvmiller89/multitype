/**
 * Enqueues and sends FEUs to a client
 * @author John Lima
 *
 */

import java.util.concurrent.*;
import java.io.*;
import java.net.*;

public class OutputProcessor implements Runnable {

	//Queue to hold the updates waiting to be sent
	BlockingQueue<FrontEndUpdate> outQueue;
	
	//[Output] socket to client 
	Socket sclient;
	
	//flag to allow thread to exit
	boolean done;
	
	/**
	 * Constructor for OutputProcessor
	 * @param client A socket connection to the client who will receive
	 * the updates
	 */
	public OutputProcessor(Socket client) {
		sclient = client;
		outQueue = new LinkedBlockingQueue<FrontEndUpdate>();
		done = false;
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
					out.writeObject(outQueue.take());
				}
				catch (IOException ioe) {
					System.err.println("OutputProcessor: writeObject failed: "
							+ ioe.toString());
				}
			}
			
			out.close();
			
		}
		catch (InterruptedException ie) {
			System.err.println("OutputProcessor(): " + ie.toString());
		}
		catch (IOException ioe) {
			System.err.println("OutputProcessor(): " + ioe.toString());
		}
	}
	
	/**
	 * Call this to terminate thread
	 */
	public void setDone() {
		done = true;
		try {
			outQueue.put(null);  //to unblock the queue
		} catch (InterruptedException e) {
			System.err.println("setDone(): " + e.toString());
		}
	}

}
