/**
 * Reads an input FEU from a socket to a client and hands it off to the
 * appropriate processor. 
 * @author John Lima
 */

import java.net.*;
import java.io.*;

public class InputProcessor implements Runnable {
	Socket sclient;
	
	/**
	 * Constructor
	 * @param s The socket that this will be communicating with
	 */
	public InputProcessor(Socket s) {
		sclient = s;
	}
	
	/**
	 * Reads and deserializes an FEU
	 * @return Gets a serialized FEU from the socket and returns it as an FEU 
	 * instance                                                 
	 */
	public FrontEndUpdate buildFEU() {

		try {
			InputStream input = sclient.getInputStream();
            //OutputStream output = sclient.getOutputStream();
			
			ObjectInputStream in = new ObjectInputStream(input);
			
			FrontEndUpdate ret = (FrontEndUpdate) in.readObject();
			
			in.close();
			
			return ret;
		}
		catch (IOException ioe) {
			System.err.println("buildFEU(): " + ioe.toString());
		}
		catch (ClassNotFoundException cnfe) {
			System.err.println("buildFEU(): " + cnfe.toString());
		}
		return null;
	}
	
	public void run() {
		//Do input processing here
		FrontEndUpdate in_feu = buildFEU();
		switch(in_feu.getUpdateType()) {
		case Markup:
			//call MarkupProcessor
			break;
		case Notification:
			//call NotificationProcessor
			break;
		default:
			System.err.println("InputProcessor: Unknown FEU type");
		}
		
	}
}
