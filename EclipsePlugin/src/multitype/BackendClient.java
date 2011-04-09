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
	private Vector<FrontEndUpdate> fromServerQueue;
	private BlockingQueue<FrontEndUpdate> toServerQueue;
	private ConcurrentLinkedQueue<FrontEndUpdate> screenHistory;
	private boolean done = false;
	private int revisionNumber = 0;
	private String url;
	private int port;
	private int nextSentToFrontEndIndex = -1;
	private int userId = 2;
	private int curFEUid = 0;
	
	/**
	 * Constructor for BackendClient
	 * @param url Url to connect to, can be domain name or ip
	 * @param port port to be used to connect to
	 */
	public BackendClient(String url, int port) {
		this.url = url;
		this.port = port;
		fromServerQueue = new Vector<FrontEndUpdate>();
		toServerQueue = new ArrayBlockingQueue<FrontEndUpdate>(5000);
		screenHistory = new ConcurrentLinkedQueue<FrontEndUpdate>();
	}
	
	/**
	 * Used to connect to the actual server. Must have a thread waiting for
	 * FEU's before calling this function
	 */
	public void connect() {
		try {
			serverSocket = new Socket(url, port);
            out = new ObjectOutputStream(serverSocket.getOutputStream());
            in = new ObjectInputStream(serverSocket.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			String error = e.toString()+"\n";
			for(StackTraceElement se : e.getStackTrace())
				error = error+se.toString()+"\n";
			FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
				FrontEndUpdate.NotificationType.Connection_Error, -1, -1, 
				error);
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
						System.err.println("Received: " + feu.toLine());
						if(deleteFromScreenHistoryIfOwn(feu)) {
							continue;
						}
						feu = updateIncomingFEUWithScreenHistory(feu);
						fromServerQueue.add(0, feu); // adding at the left
						nextSentToFrontEndIndex++;
					} catch (Exception e) {
						e.printStackTrace();
						done = true;
						String error = e.toString()+"\n";
						for(StackTraceElement se : e.getStackTrace())
							error = error+se.toString()+"\n";
						FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
							FrontEndUpdate.NotificationType.Server_Disconnect, 
							-1, -1, error);
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
						FrontEndUpdate feu = toServerQueue.take();
						feu.setRevision(revisionNumber);	
						feu.setFEUid(curFEUid);
						curFEUid++;
						out.writeObject(feu);
					} catch (Exception e) {
						e.printStackTrace();
						done = true;
						String error = e.toString()+"\n";
						for(StackTraceElement se : e.getStackTrace())
							error = error+se.toString()+"\n";
						FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
							FrontEndUpdate.NotificationType.Server_Disconnect, 
							-1, -1, error);
						fromServerQueue.add(f);
					}
				}
			}			
		});
		sendUpdateThread.start();
	}
	
	/**
	 * Must be called by the Front End to set the user id
	 * @param id
	 */
	public void setUserId(int id) {
		this.userId = id;
	}
	
	/**
	 * Sends a FrontEndUpdate to the server
	 * @param feu Pre-constructed FrontEndUpdate to be sent
	 */
	public void sendUpdate(FrontEndUpdate feu) {
		updateFromServerQueueWithSent(feu);
		screenHistory.add(feu); // Concurrent-safe
		toServerQueue.add(feu); // Concurrent-safe
	}

	/**
	 * Gets a FrontEndUpdate from the FEU Queue
	 * @return next FEU from Queue
	 */
	public FrontEndUpdate getUpdate() {
		try {
			assert(this.nextSentToFrontEndIndex >= -1);
			while(this.nextSentToFrontEndIndex == -1) {
				Thread.sleep(1);				
			}
			FrontEndUpdate update = this.fromServerQueue.get(
					this.nextSentToFrontEndIndex);
			System.err.print("GetUpdate: " + update.toLine());
			this.nextSentToFrontEndIndex--; // getting added from the left
			return update;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Used to notify the backendclient that the frontend has actually painted
	 * an feu
	 * @param feu that was just painted
	 */
	public void FEUProcessed(FrontEndUpdate feu) {
		this.fromServerQueue.remove(feu);
		updateScreenHistoryWithProcessed(feu);
		this.revisionNumber = feu.getRevision();
		System.out.print("Processed "+feu.toLine());
	}
	
	/**
	 * Checks if the feu that was received belongs to us, and if so, we delete 
	 * it from the screenHistory queue
	 * @param feu FEU to be checked
	 * @return true if feu belongs to us and was deleted, false if the feu
	 * did not belong to us
	 */
	private boolean deleteFromScreenHistoryIfOwn(FrontEndUpdate feu) {
		assert (this.userId != -1);
		if(feu.getUserId() == this.userId) {
			for(FrontEndUpdate screenHistoryFEU : screenHistory) {
				if(screenHistoryFEU.getFEUid() == feu.getFEUid()) {
					screenHistory.remove(screenHistoryFEU);
					return true;
				}					
			}
			assert (false);
		}
		return false;
	}	

	/**
	 * Updates an FEU coming from the server with FEUs in screenHistory (FEUs
	 * that are on the screen but haven't been sent to the server) before
	 * inserting it into the fromServerQueue
	 * @param feu FEU to be updated
	 * @return a new FEU that has been updated
	 */
	private FrontEndUpdate updateIncomingFEUWithScreenHistory(
			FrontEndUpdate feu) {

		FrontEndUpdate newFEU = feu;
		
		// DEBUG
		System.out.println("Screen History Before update");
		System.out.print("Received "+newFEU.toLine());
		System.out.println("Screen History");
		for(FrontEndUpdate screenFEU : screenHistory) {
			System.out.print(screenFEU.toLine());
		}
		
		// END DEBUG
		
		
		for(FrontEndUpdate screenFEU : screenHistory) {
			assert(screenFEU.getRevision() < feu.getRevision());
			updateFEUgivenFEU(newFEU, screenFEU, false);
		}
		
		
		// DEBUG
		System.out.println("Screen History After update");
		System.out.print("Received "+newFEU.toLine());
		System.out.println("Screen History");
		for(FrontEndUpdate screenFEU : screenHistory) {
			System.out.print(screenFEU.toLine());
		}
		
		// END DEBUG
		return newFEU;
	}
	
	/**
	 * Updates the fromServerQueue with an FEU that we are going to send
	 * to the server
	 * @param feu given FEU passed from FrontEnd to be sent to the server
	 */
	private void updateFromServerQueueWithSent(FrontEndUpdate feu) {
		for(FrontEndUpdate fromServerFEU : fromServerQueue) {
			updateFEUgivenFEU(fromServerFEU, feu, false);
		}
	}
	
	/**
	 * Updates the screenHistory queue with a FEU that has been painted on
	 * the FrontEnd
	 * @param feu FEU that was just processed, or just painted
	 */
	private void updateScreenHistoryWithProcessed(FrontEndUpdate feu) {
		for(FrontEndUpdate screenHistoryFEU : screenHistory) {
			updateFEUgivenFEU(screenHistoryFEU, feu, false);
		}		
	}
	
	/**
	 * Used 
	 * @param update
	 */
	/*private void checkLocalHistory(FrontEndUpdate update) {
		for(FrontEndUpdate top : markupHistory) {
			if(update.getUpdateType() == FrontEndUpdate.UpdateType.Markup) {
				if(update.getMarkupType() == top.getMarkupType()) {
					if(update.getMarkupType() == FrontEndUpdate.MarkupType.Insert) {
						if(update.getUserId() == top.getUserId() &&
								update.getStartLocation() == top.getStartLocation() &&
								update.getInsertString().equals(top.getInsertString()))
							markupHistory.remove(top);
						else {
							FrontEndUpdate updateClone = 
								FrontEndUpdate.createInsertFEU(
										update.getFileId(), 
										update.getUserId(), 
										update.getStartLocation(), 
										update.getInsertString());
							updateClone.setRevision(update.getRevision());
							updateFEUgivenFEU(update, top, false);
							updateFEUgivenFEU(top, updateClone, true);
						}
					}
					else { // its a delete
						if(update.getUserId() == top.getUserId() &&
								update.getStartLocation() == top.getStartLocation() &&
								update.getEndLocation() == top.getEndLocation())
							markupHistory.remove(top);
						else {
							FrontEndUpdate updateClone = 
								FrontEndUpdate.createDeleteFEU(
										update.getFileId(), 
										update.getUserId(), 
										update.getStartLocation(), 
										update.getEndLocation());
							updateClone.setRevision(update.getRevision());
							updateFEUgivenFEU(update, top, false);
							updateFEUgivenFEU(top, updateClone, true);
						}
					}
				}
				else {
					if(update.getMarkupType() == FrontEndUpdate.MarkupType.Insert) {
						FrontEndUpdate updateClone = 
							FrontEndUpdate.createInsertFEU(
									update.getFileId(), 
									update.getUserId(), 
									update.getStartLocation(), 
									update.getInsertString());
						updateClone.setRevision(update.getRevision());
						updateFEUgivenFEU(update, top, false);
						updateFEUgivenFEU(top, updateClone, true);
					}
					else { // It is a delete type
						FrontEndUpdate updateClone = 
							FrontEndUpdate.createDeleteFEU(
									update.getFileId(), 
									update.getUserId(), 
									update.getStartLocation(), 
									update.getEndLocation());
						updateClone.setRevision(update.getRevision());
						updateFEUgivenFEU(update, top, false);
						updateFEUgivenFEU(top, updateClone, true);
					}
				}
			}
		}
	}*/
	
	/**
	 * Updates an FEU given an FEU
	 * @param toUpdate
	 * @param given
	 */
	private void updateFEUgivenFEU (FrontEndUpdate toUpdate, 
			FrontEndUpdate given, boolean updateRevision) {
		if(toUpdate == given) //don't update itself
			return;
		if(updateRevision)
			toUpdate.setRevision(given.getRevision());
		// don't update if the user is the same
		if(toUpdate.getUserId() == given.getUserId()) 
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
	
	/*private void dumpMarkupHistory() {
		System.out.println("muh----------");
		int i=0; 
		for(FrontEndUpdate feu : markupHistory) {
			System.out.print(i + feu.toLine());
			i++;
		}
	}
	
	private void dumpFromFE() {
		System.out.println("from FE----------");
		int i=0; 
		for(FrontEndUpdate feu : fromFrontEndQueue) {
			System.out.print(i + feu.toLine());
			i++;
		}
	}
	
	private void dumpFromServer() {
		System.out.println("from Server----------");
		int i=0; 
		for(FrontEndUpdate feu : fromServerQueue) {
			System.out.print(i + feu.toLine());
			i++;
		}
	}*/

}
