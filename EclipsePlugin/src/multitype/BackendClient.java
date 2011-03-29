package multitype;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The BackendClient is used as means of communication with the 
 * BackendServer. 
 * @author Rodrigo
 *
 */
public class BackendClient {
	private Socket serverSocket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Thread receiveUpdateThread;
	private Thread sendUpdateThread;
	private BlockingQueue<FrontEndUpdate> fromServerQueue;
	private BlockingQueue<FrontEndUpdate> fromFrontEndQueue;
	private ConcurrentLinkedQueue<FrontEndUpdate> markupHistory;
	private boolean done = false;
	private int revisionNumber = 0;
	private String url;
	private int port;
	
	/**
	 * Constructor for BackendClient
	 * @param url Url to connect to, can be domain name or ip
	 * @param port port to be used to connect to
	 */
	public BackendClient(String url, int port) {
		this.url = url;
		this.port = port;
		fromServerQueue = new ArrayBlockingQueue<FrontEndUpdate>(5000);
		fromFrontEndQueue = new ArrayBlockingQueue<FrontEndUpdate>(5000);
		markupHistory = new ConcurrentLinkedQueue<FrontEndUpdate>();		
	}
	
	public void connect() {
		try {
			serverSocket = new Socket(url, port);
            out = new ObjectOutputStream(serverSocket.getOutputStream());
            in = new ObjectInputStream(serverSocket.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
				FrontEndUpdate.NotificationType.Connection_Error, -1, -1, null);
			fromServerQueue.add(f);
			return;
		} 
		
		receiveUpdateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!done) {
					try {
						FrontEndUpdate feu = 
							(FrontEndUpdate)in.readObject();
						if(feu.getUpdateType() == FrontEndUpdate.UpdateType.Markup)
							revisionNumber = feu.getRevision();
						fromServerQueue.add(feu);
					} catch (Exception e) {
						e.printStackTrace();
						done = true;
						FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
							FrontEndUpdate.NotificationType.Server_Disconnect, 
							-1, -1, null);
						fromServerQueue.add(f);
					}			
				}
			}			
		});
		receiveUpdateThread.start();
		
		sendUpdateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!done) {
					try {
						FrontEndUpdate feu = fromFrontEndQueue.take();
						feu.setRevision(revisionNumber);
						addToLocalHistory(feu);
						printFEU(0, feu); //TODO DEBUG
						out.writeObject(feu);
					} catch (Exception e) {
						e.printStackTrace();
						done = true;
						FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
							FrontEndUpdate.NotificationType.Server_Disconnect, 
							-1, -1, null);
						fromServerQueue.add(f);
					}
				}
			}			
		});
		sendUpdateThread.start();
	}
	
	/**
	 * Sends a FrontEndUpdate to the server
	 * @param feu Pre-constructed FrontEndUpdate to be sent
	 */
	public void sendUpdate(FrontEndUpdate feu) {
		fromFrontEndQueue.add(feu);
	}
	
	/**
	 * Gets a FrontEndUpdate from the FEU Queue
	 * @return next FEU from Queue
	 */
	public FrontEndUpdate getUpdate() {
		try {
			FrontEndUpdate update =  fromServerQueue.take();
			checkLocalHistory(update);
			printFEU(1, update); //TODO DEBUG
			return update;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void addToLocalHistory(FrontEndUpdate feu) {
		if(feu.getUpdateType() == 
			FrontEndUpdate.UpdateType.Markup) {
			markupHistory.add(feu);
		}
	}
	
	/**
	 * Used 
	 * @param update
	 */
	private void checkLocalHistory(FrontEndUpdate update) {
		for(FrontEndUpdate top : markupHistory) {
			if(update.getUpdateType() == FrontEndUpdate.UpdateType.Markup) {
				if(update.getMarkupType() == top.getMarkupType()) {
					if(update.getMarkupType() == FrontEndUpdate.MarkupType.Insert) {
						if(update.getStartLocation() == top.getStartLocation() &&
								update.getInsertString().equals(top.getInsertString()))
							markupHistory.remove(top);
						else {
							updateFEUgivenFEU(top, update);
							updateFEUgivenFEU(update, top);
							top.setRevision(update.getRevision());
						}
					}
					else { // its a delete
						if(update.getStartLocation() == top.getStartLocation() &&
								update.getEndLocation() == top.getEndLocation())
							markupHistory.remove(top);
						else {
							updateFEUgivenFEU(top, update);
							updateFEUgivenFEU(update, top);
							top.setRevision(update.getRevision());
						}
					}
				}
				else {
					updateFEUgivenFEU(top, update);
					updateFEUgivenFEU(update, top);
					top.setRevision(update.getRevision());
				}
			}
		}
	}
	
	/**
	 * Updates an FEU given an FEU
	 * @param toUpdate
	 * @param given
	 */
	private void updateFEUgivenFEU (FrontEndUpdate toUpdate, 
			FrontEndUpdate given) {
		if(toUpdate == given) //don't update itself
			return;
		if(given.getMarkupType() == FrontEndUpdate.MarkupType.Insert) {
			int insertAt = given.getStartLocation();
			int sizeOfInsert = given.getInsertString().length();
			
			if(toUpdate.getMarkupType() == FrontEndUpdate.MarkupType.Insert) {
				if(toUpdate.getStartLocation() >= insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
							+sizeOfInsert);
				}
			}
			else if (toUpdate.getMarkupType() == 
				FrontEndUpdate.MarkupType.Delete){
				if(toUpdate.getStartLocation() >= insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
							+sizeOfInsert);
					toUpdate.setEndLocation(toUpdate.getEndLocation()
							+sizeOfInsert);
				}				
			}
		}
		else if(given.getMarkupType() == FrontEndUpdate.MarkupType.Delete) {
			int insertAt = given.getStartLocation();
			int sizeOfInsert = given.getEndLocation() - insertAt + 1;
			if(toUpdate.getMarkupType() == FrontEndUpdate.MarkupType.Insert) {
				if(toUpdate.getStartLocation() >= insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
							-sizeOfInsert);
				}
			}
			else if (toUpdate.getMarkupType() == 
				FrontEndUpdate.MarkupType.Delete){
				if(toUpdate.getStartLocation() >= insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
							-sizeOfInsert);
					toUpdate.setEndLocation(toUpdate.getEndLocation()
							-sizeOfInsert);
				}				
			}
		}
		else { 
			// The markup doesn't affect other markups (cursor pos 
			// or highlight)
			return;
		}
	}

	/**
	 * needs to be called in between sessions. We spun off thread, we need to 
	 * kill them
	 */
	public void finish() {
		done = true;
	}
	
	/**
	 * Used for testing purposes
	 * @param i 
	 * @param feu FrontEndUpdate to print
	 */
	@SuppressWarnings("unused")
	private void printFEU(int i, FrontEndUpdate feu) {
		String output = "--------------------\n";
		switch(i) {
		case 0:
			output = output + "Sent ";
			break;
		case 1:
			output = output + "Received ";
			break;
		}
		switch(feu.getUpdateType()) {
		case Markup:
			output = output + "Markup FEU:\n";
			output = output + "file id: " + feu.getFileId() + "\n";
			output = output + "user id: " + feu.getUserId() + "\n";
			output = output + "rev: " + feu.getRevision() + "\n";
			switch(feu.getMarkupType()) {
			case Insert:
				output = output + "type: Insert\n";
				output = output + "start loc: " + feu.getStartLocation() + "\n";
				output = output + "insert : " + feu.getInsertString() + "\n";
				break;
			case Delete:
				output = output + "type: Delete\n";
				output = output + "start loc: " + feu.getStartLocation() + "\n";
				output = output + "end loc: " + feu.getEndLocation() + "\n";
				break;
			case Cursor:
				output = output + "type: Cursor\n";
				output = output + "start loc: " + feu.getStartLocation() + "\n";
				break;
			case Highlight:
				output = output + "type: Highlight\n";
				output = output + "start loc: " + feu.getStartLocation() + "\n";
				output = output + "end loc: " + feu.getEndLocation() + "\n";
				break;
			}
			break;
		case Notification:
			output = output + "Notification FEU:\n";
			output = output+ "type: "+feu.getNotificationType()+"\n";
			switch(feu.getNotificationType()) {
			case Connection_Succeed:
				output = output + "assign id: "+feu.getUserId()+"\n";
				break;
			case User_Connected:
				output = output + "user id: "+feu.getUserId()+"\n";
				output = output + "username: "+feu.getContent()+"\n";
				break;
			}
			break;
		}
		output = output+"---------------------";
		System.out.println(output);
	}
}
