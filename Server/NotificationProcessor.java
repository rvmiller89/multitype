import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import multitype.FrontEndUpdate;


public class NotificationProcessor implements Runnable {

	private BlockingQueue<FrontEndUpdate> notificationQueue;
	private boolean done = false;
	private FileUserManager fileUserManager;
	
	/**
	 * Constructor for NotificationProcessor
	 * @param outs A list of the outputprocessor to output data to
	 */
	public NotificationProcessor(FileUserManager fileUserManager) {
		this.fileUserManager = fileUserManager;
		notificationQueue = new ArrayBlockingQueue<FrontEndUpdate>(5000);
	}
	
	/**
	 * Runs the notificationProcessor
	 */
	@Override
	public void run() {
		while(!done) {
			FrontEndUpdate feu = getTopItem();
			processFEU(feu);
		}		
	}
	
	/**
	 * Used to kill the notificationProcessor, see run()
	 */
	public void setDone() {
		done = true;
	}
	
	/**
	 * Adds an FEU to the end of the queue vector
	 * @param f The FrontEndUpdate to be added
	 */
	public void addFEU(FrontEndUpdate feu) {
		notificationQueue.add(feu);
	}

	/**
	 * Gets the next item in the notificationQueue
	 */
	private FrontEndUpdate getTopItem() {
		FrontEndUpdate feu = null;
		try {
			feu = notificationQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return feu;
	}

	private void processFEU(FrontEndUpdate feu) {
		switch(feu.getNotificationType()) {
		case New_Connection: // taken care of by the server
			break;
		case New_Shared_File:
			fileUserManager.sendFEUToAll(feu);
			break;
		case Close_Shared_File:
			fileUserManager.sendFEUToAll(feu);
			fileUserManager.removeFile(feu.getFileId());
			break;
		case Get_Shared_File:
			// fileUserManager.sendFEUToClient(clientID, feu) TODO send to host id
			break;
		case User_Connected: 
			int uid = feu.getUserId();
			String username = feu.getContent();
			fileUserManager.addUser(uid, username);
			fileUserManager.sendFEUToAll(feu);
			break;
		case User_Disconnected:
			fileUserManager.sendFEUToAll(feu);
			fileUserManager.removeClient(feu.getUserId());
			break;
		case Request_Host: 
			// TODO no function
			fileUserManager.sendFEUToAll(FrontEndUpdate.createNotificationFEU(FrontEndUpdate.NotificationType.New_Host, -1, feu.getUserId(), feu.getContent()));
			break;
		case New_Host: // Sent only from server to clients I believe
			break;
		case Host_Disconnect:
			// TODO no function
			// JSL - need to clear out the FUM info here?
			break;
		case Server_Disconnect: // Only sent from server to clients
			break;
		case Console_Message:
			fileUserManager.sendFEUToAll(feu);
			break;
		case Chat_Message:
			fileUserManager.sendFEUToAll(feu);
			break;
		}
	}
}
