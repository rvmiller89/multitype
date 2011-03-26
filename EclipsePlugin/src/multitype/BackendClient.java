package multitype;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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
	private Vector<FrontEndUpdate> markupHistory;
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
		markupHistory = new Vector<FrontEndUpdate>();		
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
						if(feu.getUpdateType() == 
							FrontEndUpdate.UpdateType.Markup) {
							markupHistory.add(feu);
						}
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
		System.out.println(feu.getInsertString());
	}
	
	/**
	 * Gets a FrontEndUpdate from the FEU Queue
	 * @return next FEU from Queue
	 */
	public FrontEndUpdate getUpdate() {
		try {
			FrontEndUpdate update =  fromServerQueue.take();
			checkLocalHistory(update);
			return update;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
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
								update.getContent().equals(top.getContent()))
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
}
