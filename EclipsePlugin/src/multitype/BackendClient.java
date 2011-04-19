package multitype;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import multitype.FrontEndUpdate.UpdateType;

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
	private Thread keepAliveThread;
	private Vector<FrontEndUpdate> fromServerQueue;
	private Vector<FrontEndUpdate> fromServerNotificationQueue;
	private BlockingQueue<FrontEndUpdate> toServerQueue;
	private ConcurrentLinkedQueue<FrontEndUpdate> screenHistory;
	private boolean done = false;
	private boolean serverAlive;
	//private int revisionNumber = 0;
	private String url;
	private int port;
	private int nextSentToFrontEndIndex = -1;
	private int userId = -1;
	//private int curFEUid = 0;
	private Map<Integer, Integer> revisionHistoryMap; // File id -> revision #
	private Map<Integer, Integer> FEUidMap; // Fild id -> FEU id
	private boolean readyToSendNext = true;
	
	/**
	 * Constructor for BackendClient
	 * @param url Url to connect to, can be domain name or ip
	 * @param port port to be used to connect to
	 */
	public BackendClient(String url, int port) {
		this.url = url;
		this.port = port;
		fromServerQueue = new Vector<FrontEndUpdate>();
		fromServerNotificationQueue = new Vector<FrontEndUpdate>();
		toServerQueue = new ArrayBlockingQueue<FrontEndUpdate>(5000);
		screenHistory = new ConcurrentLinkedQueue<FrontEndUpdate>();
		revisionHistoryMap = new HashMap<Integer, Integer>();
		FEUidMap = new HashMap<Integer, Integer>();
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
			fromServerNotificationQueue.add(f);
			return;
		} 
		
		receiveUpdateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!done) {
					try {
						while(fromServerQueue.size() == 1)
							Thread.sleep(10);
						FrontEndUpdate feu = 
							(FrontEndUpdate)in.readObject();
						System.err.println("Received: " + feu.toLine());
						if(deleteFromScreenHistoryIfOwn(feu)) {
							readyToSendNext  = true;
							continue;
						}
						if(feu.getUpdateType() == 
							FrontEndUpdate.UpdateType.Notification
							&& feu.getNotificationType() ==
								FrontEndUpdate.NotificationType.Keep_Alive) {
							serverAlive = true;
							sendUpdate(feu);
							continue;
						}
						feu = updateIncomingFEUWithScreenHistory(feu);
						addFEUToBegOfFromServerQueue(feu);
						parseForFileListUpdateOnReceive(feu);
					} catch (EOFException eofe) {
						System.err.println("Received EOF, closing.");
						try {
							serverSocket.close();
							FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
									FrontEndUpdate.NotificationType.Server_Disconnect, 
									-1, -1, "EOFException");
							fromServerNotificationQueue.add(f);
						} catch(IOException ioe) {
							System.err.println("Failed to close socket after EOF");
						}
						done = true;
					} catch (Exception e) {
						e.printStackTrace();
						done = true;
						String error = e.toString()+"\n";
						for(StackTraceElement se : e.getStackTrace())
							error = error+se.toString()+"\n";
						FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
							FrontEndUpdate.NotificationType.Server_Disconnect, 
							-1, -1, error);
						fromServerNotificationQueue.add(f);
					}			
				}
			}	
		});
		receiveUpdateThread.start();
		
		sendUpdateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						if(done) {
							while(toServerQueue.size() > 0) {
								FrontEndUpdate feu = toServerQueue.take();

								if(feu.getUpdateType() == 
									FrontEndUpdate.UpdateType.Markup) {
									while(!readyToSendNext)
										Thread.sleep(1);
									//old feu.setRevision(revisionNumber);	
									feu.setRevision(
											revisionHistoryMap.get(
													feu.getFileId()));
									//old feu.setFEUid(curFEUid);
									feu.setFEUid(
											FEUidMap.get(feu.getFileId()));
									//old curFEUid++;
									FEUidMap.put(feu.getFileId(), 
										(FEUidMap.get(
											feu.getFileId()).intValue()+1)
												%Integer.MAX_VALUE);
									readyToSendNext = false;
								}
								System.err.println("Sent: " + feu.toLine());
								out.writeObject(feu);
							}
							break;
						} 
						else {
							FrontEndUpdate feu = toServerQueue.take();
							
							if(feu.getUpdateType() == 
								FrontEndUpdate.UpdateType.Markup) {
								while(!readyToSendNext)
									Thread.sleep(1);
								//old feu.setRevision(revisionNumber);	
								feu.setRevision(
										revisionHistoryMap.get(
												feu.getFileId()));
								//old feu.setFEUid(curFEUid);
								feu.setFEUid(
										FEUidMap.get(feu.getFileId()));
								//old curFEUid++;
								FEUidMap.put(feu.getFileId(), 
									(FEUidMap.get(
										feu.getFileId()).intValue()+1)
											%Integer.MAX_VALUE);
								readyToSendNext = false;
							}
							System.err.println("Sent: " + feu.toLine());
							out.writeObject(feu);
						}
					} catch (Exception e) {
						e.printStackTrace();
						done = true;
						String error = e.toString()+"\n";
						for(StackTraceElement se : e.getStackTrace())
							error = error+se.toString()+"\n";
						FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
							FrontEndUpdate.NotificationType.Server_Disconnect, 
							-1, -1, error);
						fromServerNotificationQueue.add(f);
					}
				}
			}			
		});
		sendUpdateThread.start();
		
		keepAliveThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!done) {
					try {
						FrontEndUpdate feu = 
							FrontEndUpdate.createNotificationFEU(
									FrontEndUpdate.NotificationType.Keep_Alive, 
									-1, userId, "");
						Thread.sleep(7*1000);
						out.writeObject(feu);
						serverAlive = false;
						Thread.sleep(20*1000);
						if(serverAlive == false) {
							done = true;
							FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
									FrontEndUpdate.NotificationType.Server_Disconnect, 
									-1, -1, "SERVER_DEAD");
								fromServerNotificationQueue.add(f);
						}
					} catch(InterruptedException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
						done = true;
						String error = e.toString()+"\n";
						for(StackTraceElement se : e.getStackTrace())
							error = error+se.toString()+"\n";
						FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
							FrontEndUpdate.NotificationType.Server_Disconnect, 
							-1, -1, error);
						fromServerNotificationQueue.add(f);
					}			
				}
			}	
		});
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
		parseForFileListUpdateOnSend(feu);
		updateFromServerQueueWithSent(feu);
		if(feu.getUpdateType() == FrontEndUpdate.UpdateType.Markup)
			screenHistory.add(feu); // Concurrent-safe
		toServerQueue.add(feu); // Concurrent-safe
	}

	/**
	 * Gets a FrontEndUpdate from the FEU Queue
	 * @return next FEU from Queue
	 */
	public FrontEndUpdate getUpdate() {
		try {
			while(fromServerNotificationQueue.size() == 0 
					&& nextSentToFrontEndIndex == -1) {
				Thread.sleep(1);
			}
			if(fromServerNotificationQueue.size() > 0) {
				FrontEndUpdate update = fromServerNotificationQueue.firstElement();
				fromServerNotificationQueue.remove(update);
				System.err.print("GetUpdate: " + update.toLine());
				return update;
			}
			if (nextSentToFrontEndIndex > -1){
				assert(this.nextSentToFrontEndIndex > -1);
				/*while(this.nextSentToFrontEndIndex == -1) {
					Thread.sleep(1);				
				}*/
				FrontEndUpdate update = this.fromServerQueue.get(
						this.nextSentToFrontEndIndex);
				System.err.print("GetUpdate: " + update.toLine());
				this.nextSentToFrontEndIndex--; // getting added from the left
				return update;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	/**
	 * Used to notify the backendclient that the frontend has actually painted
	 * an feu
	 * @param feu that was just painted
	 */
	public void FEUProcessed(FrontEndUpdate feu) {
		this.fromServerQueue.remove(feu);
		updateScreenHistoryWithProcessed(feu);
		//old this.revisionNumber = feu.getRevision();
		revisionHistoryMap.put(feu.getFileId(), feu.getRevision());
		System.out.print("Processed "+feu.toLine());
	}
	
	/**
	 * Checks if the feu that was received belongs to us, and if so, we delete 
	 * it from the screenHistory queue, but we need to update revision #
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
					revisionHistoryMap.put(feu.getFileId(), feu.getRevision());
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
		/*System.out.println("Screen History Before update");
		System.out.print("Received "+newFEU.toLine());
		System.out.println("Screen History");
		for(FrontEndUpdate screenFEU : screenHistory) {
			System.out.print(screenFEU.toLine());
		}*/
		
		// END DEBUG
		
		
		for(FrontEndUpdate screenFEU : screenHistory) {
			assert(screenFEU.getRevision() < feu.getRevision());
			updateFEUgivenFEU(newFEU, screenFEU, false);
		}
		
		
		// DEBUG
		/*System.out.println("Screen History After update");
		System.out.print("Received "+newFEU.toLine());
		System.out.println("Screen History");
		for(FrontEndUpdate screenFEU : screenHistory) {
			System.out.print(screenFEU.toLine());
		}*/
		
		// END DEBUG
		return newFEU;
	}
	
	/**
	 * Updates the fromServerQueue with an FEU that we are going to send
	 * to the server
	 * @param feu given FEU passed from FrontEnd to be sent to the server
	 */
	private synchronized void updateFromServerQueueWithSent(FrontEndUpdate feu) {
		for(FrontEndUpdate fromServerFEU : fromServerQueue) {
			updateFEUgivenFEU(fromServerFEU, feu, false);
		}
	}
	
	/**
	 * Adding to a Vector needs to be synchronized, sole purpose of this func
	 * @param feu FEU to be added
	 */
	private synchronized void addFEUToBegOfFromServerQueue(FrontEndUpdate feu) {
		if(feu.getUpdateType() == UpdateType.Markup) {
			fromServerQueue.add(0, feu); // adding at the left
			nextSentToFrontEndIndex++;
		}
		else
			fromServerNotificationQueue.add(feu);
	}
	
	/**
	 * Updates the screenHistory queue with a FEU that has been painted on
	 * the FrontEnd
	 * @param feu FEU that was just processed, or just painted
	 */
	private void updateScreenHistoryWithProcessed(FrontEndUpdate feu) {
		for(FrontEndUpdate screenHistoryFEU : screenHistory) {
			updateFEUgivenFEUWithEqual(screenHistoryFEU, feu, false);
		}		
	}
	
	/**
	 * We need to make adjustments to map if a user is requesting for file, 
	 * or if as a host, they are sharing a new file, or for closing a file
	 * as a client, or for closing a shared file as a host
	 * @param feu
	 */
	private void parseForFileListUpdateOnSend(FrontEndUpdate feu) {
		if(feu.getUpdateType() != FrontEndUpdate.UpdateType.Notification)
			return;
		switch(feu.getNotificationType()) {
		case Close_Client_File: // Client does this, remove mapping
			FEUidMap.remove(feu.getFileId());
			revisionHistoryMap.remove(feu.getFileId());
			break;
		/*case Close_Shared_File: // Host does this, remove mapping
			FEUidMap.remove(feu.getFileId());
			revisionHistoryMap.remove(feu.getFileId());
			break;*/
		case New_Shared_File: // Host does this, create mapping
			FEUidMap.put(feu.getFileId(), 0);
			revisionHistoryMap.put(feu.getFileId(), 0);
			break;
		case Send_File: 
			// Host is sending new file to someone, attach rev #
			feu.setRevision(revisionHistoryMap.get(feu.getFileId()));
			break;
		case User_Connected:
			//keepAliveThread.start();
			break;
		}
	}
	
	/**
	 * We need to make adjustments to maps if a new file is received by the 
	 * client, or if the client receives that a host has closed a shared
	 * file
	 * @param feu
	 */
	private void parseForFileListUpdateOnReceive(FrontEndUpdate feu) {
		if(feu.getUpdateType() != FrontEndUpdate.UpdateType.Notification)
			return;
		switch(feu.getNotificationType()) {
		case Send_File: // Client does this, create mapping
			FEUidMap.put(feu.getFileId(), 0); //FEUids are local
			revisionHistoryMap.put(feu.getFileId(), feu.getRevision());
			break;
		case Close_Shared_File: // client side and host side, remove a mapping
			FEUidMap.remove(feu.getFileId());
			revisionHistoryMap.remove(feu.getFileId());
			break;
		}
	}
	
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
		if(toUpdate.getFileId() != given.getFileId())
			return;
		// don't update if the user is the same
		if(toUpdate.getUserId() == given.getUserId()) 
			return;
		if(given.getMarkupType() == FrontEndUpdate.MarkupType.Insert) {
			int insertAt = given.getStartLocation();
			int sizeOfInsert = given.getInsertString().length();
			
			if(toUpdate.getMarkupType() == FrontEndUpdate.MarkupType.Insert) {
				if(toUpdate.getStartLocation() > insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
							+sizeOfInsert);
				}
			}
			else if (toUpdate.getMarkupType() == 
				FrontEndUpdate.MarkupType.Delete){
				if(toUpdate.getStartLocation() > insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
							+sizeOfInsert);
					toUpdate.setEndLocation(toUpdate.getEndLocation()
							+sizeOfInsert);
				}				
			}
			else if(toUpdate.getMarkupType() == FrontEndUpdate.MarkupType.Cursor) {
				if(toUpdate.getStartLocation() > insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
							+sizeOfInsert);
				}
			}
		}
		else if(given.getMarkupType() == FrontEndUpdate.MarkupType.Delete) {
			int insertAt = given.getStartLocation();
			int sizeOfInsert = given.getEndLocation() - insertAt;
			if(toUpdate.getMarkupType() == FrontEndUpdate.MarkupType.Insert) {
				if(toUpdate.getStartLocation() > insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
							-sizeOfInsert);
				}
			}
			else if (toUpdate.getMarkupType() == 
				FrontEndUpdate.MarkupType.Delete){
				if(toUpdate.getStartLocation() > insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
							-sizeOfInsert);
					toUpdate.setEndLocation(toUpdate.getEndLocation()
							-sizeOfInsert);
				}				
			}
			else if(toUpdate.getMarkupType() == FrontEndUpdate.MarkupType.Cursor) {
				if(toUpdate.getStartLocation() > insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
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
	 * Updates an FEU given an FEU
	 * @param toUpdate
	 * @param given
	 */
	private void updateFEUgivenFEUWithEqual (FrontEndUpdate toUpdate, 
			FrontEndUpdate given, boolean updateRevision) {
		if(toUpdate == given) //don't update itself
			return;
		if(updateRevision)
			toUpdate.setRevision(given.getRevision());
		if(toUpdate.getFileId() != given.getFileId())
			return;
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
			else if(toUpdate.getMarkupType() == FrontEndUpdate.MarkupType.Cursor) {
				if(toUpdate.getStartLocation() >= insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
							+sizeOfInsert);
				}
			}
		}
		else if(given.getMarkupType() == FrontEndUpdate.MarkupType.Delete) {
			int insertAt = given.getStartLocation();
			int sizeOfInsert = given.getEndLocation() - insertAt;
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
			else if(toUpdate.getMarkupType() == FrontEndUpdate.MarkupType.Cursor) {
				if(toUpdate.getStartLocation() >= insertAt) {
					toUpdate.setStartLocation(toUpdate.getStartLocation()
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
		/*try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		done = true;
	}
}
