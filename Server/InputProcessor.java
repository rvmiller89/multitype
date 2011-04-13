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
	int uid;
	BufferedWriter writer;
	
	/**
	 * Constructor
	 * @param s The socket that this will be communicating with
	 */
	public InputProcessor(Socket s, FileUserManager f, NotificationProcessor n) {
		sclient = s;
		done = false;
		fum = f;
		np = n;
		uid = -1;
		
		
		try {
			in = new ObjectInputStream(sclient.getInputStream());
			writer = new BufferedWriter(new FileWriter("input.txt", true));
		} 
		catch (IOException e) {
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
			writer.write("IP: Received: " + ret.toLine());
			return ret;
		}
		catch (EOFException eofe) {
			//System.err.println("buildFEU(): " + eofe.toString());
			setDone(); //Kills this instance
			/*Server.dprint("This InputProcessor no longer as the will to live." +
					" Dropping client " + uid);
			if( uid >= 0 ) {
				fum.removeClient(uid);
			}*/
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
		catch (SocketException se) {
			//System.err.println("buildFEU(): " + eofe.toString());
			setDone(); //Kills this instance
			/*Server.dprint("This InputProcessor no longer as the will to live." +
					" Dropping client " + uid);
			if( uid >= 0 ) {
				fum.removeClient(uid);
			}*/
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
		catch (IOException ioe) {
			System.err.println("builfFEU(): " + ioe.toString());
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
				//call MarkupProcessor
				fum.addFEUToMarkup(in_feu);
				break;
			case Notification:
				//call NotificationProcessor
				np.addFEU(in_feu);
				break;
			default:
				System.err.println("InputProcessor: Unknown FEU type");
			}
		}
		
		try {
			in.close();
			writer.close();
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
	
	/**
	 * Sets this input processor's userid
	 * @param id
	 */
	public void setUID(int id) {
		uid = id;
	}
}
