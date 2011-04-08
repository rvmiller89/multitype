package multitype;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
	//private ConcurrentLinkedQueue<FrontEndUpdate> dumbUIThreadQueue;
	private boolean done = false;
	private int revisionNumber = 0;
	private String url;
	private int port;
	
	// DEBUG variables
	BufferedWriter writer;
	
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
		//dumbUIThreadQueue = new ConcurrentLinkedQueue<FrontEndUpdate>();
		
		// DEBUG
		/*try {
			writer = new BufferedWriter(new FileWriter("/home/rharagut/Desktop/dump.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
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
						fromServerQueue.add(feu);
						// DEBUG
						/*writer.write("\n-----------------\n");
						writer.write("Incoming FEU: " + feu.toLine() + "\n");
						dumpMarkupHistory();*/
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
						FrontEndUpdate feu = fromFrontEndQueue.take();
						feu.setRevision(revisionNumber);
						addToLocalHistory(feu);
						
						// DEBUG
						System.out.println("Sending FEU " + feu.toLine());
						dumpFromFE();
						dumpFromServer();
						dumpMarkupHistory();
						System.out.println("xxxxxxxxxxx\nxxxxxxxxxxx");
						
						
						
						
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
	 * Sends a FrontEndUpdate to the server
	 * @param feu Pre-constructed FrontEndUpdate to be sent
	 */
	public void sendUpdate(FrontEndUpdate feu) {
		for(FrontEndUpdate f : fromServerQueue)
			updateFEUgivenFEU(f, feu, false);
		fromFrontEndQueue.add(feu);
	}
	
	/**
	 * Gets a FrontEndUpdate from the FEU Queue
	 * @return next FEU from Queue
	 */
	public FrontEndUpdate getUpdate() {
		try {
			FrontEndUpdate update;
			while((update = fromServerQueue.peek())==null) {
				Thread.sleep(10);
			}
			System.out.println("before local update");
			System.out.println("Received FEU " + update.toLine());
			dumpFromFE();
			dumpFromServer();
			dumpMarkupHistory();
			checkLocalHistory(update);
			System.out.println("after local update");
			System.out.println("Received FEU " + update.toLine());
			dumpFromFE();
			dumpFromServer();
			dumpMarkupHistory();
			System.out.println("xxxxxxxxxxx\nxxxxxxxxxxx");
			//System.out.print("Received FEU\n"); //TODO DEBUG
			//System.out.println(update.toString());
			//dumbUIThreadQueue.add(update);
			return update;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void FEUProcessed(FrontEndUpdate feu) {
		revisionNumber = feu.getRevision();
		//dumbUIThreadQueue.remove(feu);
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
							updateFEUgivenFEU(top, update, true);
							updateFEUgivenFEU(update, top, false);
							top.setRevision(update.getRevision());
						}
					}
					else { // its a delete
						if(update.getStartLocation() == top.getStartLocation() &&
								update.getEndLocation() == top.getEndLocation())
							markupHistory.remove(top);
						else {
							updateFEUgivenFEU(top, update, true);
							updateFEUgivenFEU(update, top, false);
							top.setRevision(update.getRevision());
						}
					}
				}
				else {
					updateFEUgivenFEU(top, update, true);
					updateFEUgivenFEU(update, top, false);
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
		
		//DEBUG
		/*try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	private void dumpMarkupHistory() {
		System.out.println("muh----------");
		int i=0; 
		for(FrontEndUpdate feu : markupHistory) {
			System.out.println(i + feu.toLine());
			i++;
		}
	}
	
	private void dumpFromFE() {
		System.out.println("from FE----------");
		int i=0; 
		for(FrontEndUpdate feu : fromFrontEndQueue) {
			System.out.println(i + feu.toLine());
			i++;
		}
	}
	
	private void dumpFromServer() {
		System.out.println("from Server----------");
		int i=0; 
		for(FrontEndUpdate feu : fromServerQueue) {
			System.out.println(i + feu.toLine());
			i++;
		}
	}

}
