/**
 * Primarily handles associations between users and files
 * @author John Lima
 */

import java.util.*;
import java.net.*;

import multitype.FrontEndUpdate;
import multitype.FrontEndUpdate.NotificationType;

public class FileUserManager {

	Map< Integer, OutputProcessor> outprocs; //userid 
	Map< Integer, MarkupProcessor> markupprocs; //fileid -> MarkupProcessor
	Map< Integer, String> filemap; //fileid -> filename
	Map< Integer, String> usermap; //userid -> username
	
	//fileid -> list of userids that have it open
	Map< Integer, Vector<Integer> > fileusermap; 
	
	int nextUID;
	int hostid;
	
	boolean hostAlive;
	
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
		//spawn a new MarkupProcessor and add to Vector
	 	MarkupProcessor thisMarkupProc = new MarkupProcessor(this);
		markupprocs.put(fileid, thisMarkupProc);
		filemap.put(fileid, filename);
		fileusermap.put(fileid, new Vector<Integer>());
		//add host to this usermap
		fileusermap.get(fileid).add(this.getHost());
		new Thread(thisMarkupProc).start();
		
		Server.dprint("Added a new file. FID: " + fileid);
		
	}
	
	/**
	 * Called when a non-host client opens a shared file
	 * @param userid non-host client's userid
	 * @param fileid shared file
	 */
	public void openFile(int userid, int fileid) {
		fileusermap.get(fileid).add(userid);
	}
	
	/**
	 * Called when a non-host client closes a shared file
	 * @param feu
	 */
	public void clientClosedFile(FrontEndUpdate feu) {
		Server.dprint("User " + feu.getUserId() + " has closed file " + feu.getFileId());
		Vector<Integer> ulist = fileusermap.get(feu.getFileId());
		ulist.removeElement(feu.getUserId());
	}
	
	/**
	 * Adds a new user
	 * @param uid UserID for the client to be added (from the FEU with username)
	 * @param username Username to associate with this username
	 */
	public void addUser(int uid, String username) {
		//associate username with userid
		usermap.put(uid,username);
	}

	/**
	 * Sends all the connected users to the specified client
	 * @param uid userid of the client to send to
	 */
	public void sendUsersToClient(int uid) {
		Server.dprint("Sending " + usermap.size() + " users to client " + uid);
		for(Integer i : usermap.keySet()) {
			FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(
					FrontEndUpdate.NotificationType.User_Connected, -1, i,
					usermap.get(i));
			this.sendFEUToClient(uid, feu);
		}
	}
	
	/**
	 * Sends all the currently shared files to the specified client
	 * @param uid userid of the client to send to
	 */
	public void sendSharedFilesToClient(int uid) {
		for(Integer i : filemap.keySet()) {
			FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(
					FrontEndUpdate.NotificationType.New_Shared_File, 
					i, -1, filemap.get(i));
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
	 * Sends an FEU to the clients with the passed markupprocessor's file open
	 * @param m The markup processor who's file to send to
	 * @param feu The FEU to send.
	 */
	public void sendFEU(MarkupProcessor m, FrontEndUpdate feu) {
		
		//sendFEUToAll(feu);
		
		int fileid = -1;
		for(Integer i : markupprocs.keySet()) {
			if(markupprocs.get(i) == m) {
				fileid = i;
				sendFEUToFile(fileid, feu);
			}
		}
	}

	/**
	 * Sends an FEU to all connected users
	 * @param feu FEU to send
	 */
	public void sendFEUToAll(FrontEndUpdate feu) {
		for(OutputProcessor op : outprocs.values()) {
			op.addFEU(feu);
		}
	}
	
	/**
	 * Sends a FEU to a specific client 
	 * @param clientID userid of target client
	 * @param feu FEU to send
	 * @return True if the FEU was sent to the OP successfully
	 */
	public boolean sendFEUToClient(int clientID, FrontEndUpdate feu) {
		OutputProcessor clientOP = outprocs.get(clientID);
		if(clientOP != null) {
			clientOP.addFEU(feu);
			return true;
		}
		else {
			return false;
		}
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
		//spawn an OutputProcessor and associate it with userid
	 	OutputProcessor thisOutputProc = new OutputProcessor(s, this, nextUID);
		outprocs.put(nextUID, thisOutputProc);
		Thread op = new Thread(thisOutputProc);
		thisOutputProc.setThread(op);
		op.start();
		
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
	 * @return True if OutputProcessor exists to be removed
	 */
	public boolean removeClient(int uid) {
		//remove the OutputProcessor
		OutputProcessor clientOP = outprocs.remove(uid);
		
		//wait until server has processed all this client's updates
		//TODO
		
		
		//remove user from map
		usermap.remove(uid);
		
		closeUserFiles(uid);

		Server.dprint("Dropped client " + uid);
		
		if(clientOP != null) {
			clientOP.setDone();
			return true;
		}
		else {
			return false;
		}
		
	}
	
	/**
	 * Removes this user from the fileusermap
	 * @param uid
	 */
	public void closeUserFiles(int uid) {
		for(Vector<Integer> users : fileusermap.values()) {
			users.removeElement((Integer) uid);
		}
	}
	
	/**
	 * Removes the host - closes files and clears hostid
	 */
	public void removeHost() {
		if(this.hostid != -1) {
			//remove host status
			Server.dprint("Removing host: " + this.hostid);
			
			this.hostid = -1;
			
			//close all files
			for(MarkupProcessor mp : markupprocs.values()) {
				mp.setDone();
			}
			fileusermap.clear();
			filemap.clear();
		}
	}
	
	/**
	 * Sets the hostAlive flag
	 */
	public void setHostAlive() {
		this.hostAlive = true;
	}

	/**
	 * Clears the hostAlive flag
	 */
	public void clearHostAlive() {
		this.hostAlive = false;
	}

	/**
	 * Returns the value of the hostAlive flag
	 */
	public boolean getHostAlive() {
		return this.hostAlive;
	}
	
	/**
	 * Adds an FEU to the markup processor associated with its file
	 * @param feu FEU to add
	 */
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
