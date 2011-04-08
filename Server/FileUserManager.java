/**
 * Primarily handles associations between users and files
 * @author John Lima
 */

import java.util.*;
import java.net.*;

import multitype.FrontEndUpdate;

public class FileUserManager {

	Map< Integer, OutputProcessor> outprocs; //userid 
	Map< Integer, MarkupProcessor> markupprocs; //fileid -> MarkupProcessor
	Map< Integer, String> filemap; //fileid -> filename
	Map< Integer, String> usermap; //userid -> username
	
	//fileid -> list of userids that have it open
	Map< Integer, Vector<Integer> > fileusermap; 
	
	int nextUID;
	
	int hostid;
	
	public FileUserManager() {
		outprocs = new HashMap<Integer, OutputProcessor>();
		markupprocs = new HashMap<Integer, MarkupProcessor>();
		filemap = new HashMap<Integer,String>();
		usermap = new HashMap<Integer,String>();
		fileusermap = new HashMap<Integer, Vector<Integer> >();
		
		nextUID = 0;
		hostid = -1;
		
	}
	
	/**
	 * Adds a new shared file
	 * @param fileid FileID for the file to be added (generated by host client).
	 * @param filename Filename to associate with this file.
	 */
	public void addFile(int fileid, String filename) {
		//TODO
		//spawn a new MarkupProcessor and add to Vector
	 	MarkupProcessor thisMarkupProc = new MarkupProcessor(this);
		markupprocs.put(fileid, thisMarkupProc);
		filemap.put(fileid, filename);
		fileusermap.put(fileid, new Vector<Integer>());
		new Thread(thisMarkupProc).start();
		
		Server.dprint("Added a new file. FID: " + fileid);
		
	}
	
	
	public void openFile(int userid, int fileid) {
		fileusermap.get(fileid).add(userid);
	}
	
	/**
	 * Adds a new user
	 * @param uid UserID for the client to be added (from the FEU with username)
	 * @param username Username to associate with this username
	 */
	public void addUser(int uid, String username) {
		//TODO
		//associate username with userid
		usermap.put(uid,username);
		
		//TODO Will need to send this as a notification to the other users
	}

	public void sendUsersToClient(int uid) {
		for(Integer i : usermap.keySet()) {
			FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(
					FrontEndUpdate.NotificationType.User_Connected, -1, i,
					usermap.get(i));
			this.sendFEUToClient(uid, feu);
		}
	}
	
	/**
	 * Sets the given userid to the host
	 * @param userid
	 */
	public void setHost(int userid) {
		hostid = userid;
	}
	
	public int getHost() {
		return hostid;
	}
	
	/**
	 * Sends a Markup FEU to all clients associated with that file
	 * @param feu The FEU to send.
	 */
	public void sendFEU(MarkupProcessor m, FrontEndUpdate feu) {
		
		sendFEUToAll(feu);
		
		//NEEDS USER LIST!!!
		/* int fileid = -1;
		for(Integer i : markupprocs.keySet()) {
			if(markupprocs.get(i) == m) {
				fileid = i;
				sendFEUToFile(fileid, feu);
			}
		}*/
	}

	public void sendFEUToAll(FrontEndUpdate feu) {
		for(OutputProcessor op : outprocs.values()) {
			op.addFEU(feu);
		}
	}
	
	/**
	 * Sends a FEU to a specific client 
	 * @param clientID userid of target client
	 * @param feu FEU to send
	 */
	public void sendFEUToClient(int clientID, FrontEndUpdate feu) {
		outprocs.get(clientID).addFEU(feu);
	}
	
	/**
	 * Sends an FEU to all clients with the specified file open
	 * @param fileID The File ID of the file
	 * @param feu The FEU to send
	 */
	public void sendFEUToFile(int fileID, FrontEndUpdate feu) {
		Vector<Integer> ulist = fileusermap.get(fileID);
		for(Integer i : ulist) {
			sendFEUToClient(i,feu);
		}
	}
	
	/**
	 * Adds a new client to the system
	 * Spawns a new output processor and responds to the client with its UserID
	 * @param s Socket to communicate with the client on
	 */
	public int addClient(Socket s) {
		//TODO
		//spawn an OutputProcessor and associate it with userid
	 	OutputProcessor thisOutputProc = new OutputProcessor(s);
		outprocs.put(nextUID, thisOutputProc);
		new Thread(thisOutputProc).start();
		
		//create a ConnnectionSucceeded Notification FEU
		//send it back to client immediately
		FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(
				FrontEndUpdate.NotificationType.Connection_Succeed, -1, nextUID, null);
		thisOutputProc.addFEU(feu);
		
		Server.dprint("Sent client UID: " + nextUID);
		
		//generate next userid
		return nextUID++;
		
	}
	
	/**
	 * "Closes" a file by removing it from the appropriate maps
	 * @param fileid
	 */
	public void removeFile(int fileid) {
		fileusermap.remove(fileid);
		filemap.remove(fileid);
		markupprocs.remove(fileid).setDone(); //kills the markup processor for this file
		
		Server.dprint("Removed file " + fileid);
		//May need to wait for revisions to this file to go out??
	}
	
	/**
	 * Called when a client has disconnected or been dropped.
	 * @param uid
	 */
	public void removeClient(int uid) {
		//remove the OutputProcessor
		outprocs.remove(uid);
		
		//wait until server has processed all this client's updates
		//TODO
		
		
		//remove user from map
		usermap.remove(uid);
		
		Server.dprint("Dropped client " + uid);
	}
	
	public synchronized void addFEUToMarkup(FrontEndUpdate feu) {
		//get the FEU's fileID
		int fileid = feu.getFileId();
		if(fileid != -1) {
			//add this FEU to the file's markup processor
			markupprocs.get(fileid).addFEU(feu); 
		}
	}
	
	/**
	 * For the debug dumps only!
	 * @param s server instance
	 */
	public void add_debug(Server s) {
		for(MarkupProcessor m : markupprocs.values()) {
			m.debug_server=s;
		}
	}
	
	/**
	 * Returns a string representing the contents of the FUM and children's queues.
	 * @return String
	 */
	public synchronized String dump() {
		StringBuilder sb = new StringBuilder();
		for(Integer k : markupprocs.keySet()) {
			sb.append("MP " + k + ": \n"+ markupprocs.get(k).dump() + "\n");
		}
		for(Integer k : outprocs.keySet()) {
			sb.append("OP " + k + ": \n"+ outprocs.get(k).dump() + "\n");
		}
		
		return sb.toString();
	}

}
