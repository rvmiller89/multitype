/**
 * Reads an input FEU from a socket to a client and hands it off to the
 * appropriate processor. 
 * @author John Lima
 */

import java.net.*;
import java.io.*;

import multitype.FrontEndUpdate;

public class InputProcessor implements Runnable {
	Socket sclient;
	
	//flag to terminate thread
	boolean done;
	FileUserManager fum;
	ObjectInputStream in;
	NotificationProcessor np;
	
	/**
	 * Constructor
	 * @param s The socket that this will be communicating with
	 */
	public InputProcessor(Socket s, FileUserManager f, NotificationProcessor n) {
		sclient = s;
		done = false;
		fum = f;
		np = n;
		
		try {
			in = new ObjectInputStream(sclient.getInputStream());
		} catch (IOException e) {
			System.err.println("InputProcessor(): " + e.toString());
		}
	}
	
	/**
	 * Reads and deserializes an FEU
	 * @return Gets a serialized FEU from the socket and returns it as an FEU 
	 * instance                                                 
	 */
	public FrontEndUpdate buildFEU() {

		try {
			
			FrontEndUpdate ret = (FrontEndUpdate) in.readObject();
						
			return ret;
		}
		catch (IOException ioe) {
			System.err.println("buildFEU(): " + ioe.toString());
			setDone(); //Kills this instance
		}
		catch (ClassNotFoundException cnfe) {
			System.err.println("buildFEU(): " + cnfe.toString());
		}
		return null;
	}
	
	public void run() {
		//Do input processing here
		while(!done) {
			FrontEndUpdate in_feu = buildFEU();
			if(in_feu == null) {
				continue;
			}
			
			System.out.println("Received: " + in_feu.toString());
			
			switch(in_feu.getUpdateType()) {
			case Markup:
				//TODO
				//call MarkupProcessor
				fum.addFEUToMarkup(in_feu);
				break;
			case Notification:
				//TODO
				//call NotificationProcessor
				np.addFEU(in_feu);
				break;
			default:
				System.err.println("InputProcessor: Unknown FEU type");
			}
		}
		
		try {
			in.close();
		} catch (IOException e) {
			System.err.println("InputProcessor - close: " + e.toString());
		}
	}
	
	/**
	 * Call this to terminate thread
	 */
	public void setDone() {
		done = true;
	}
}
