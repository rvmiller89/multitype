package multitype;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
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
	private boolean done = false;
	
	/**
	 * Constructor for BackendClient
	 * @param url Url to connect to, can be domain name or ip
	 * @param port port to be used to connect to
	 */
	public BackendClient(String url, int port) {
		fromServerQueue = new ArrayBlockingQueue<FrontEndUpdate>(5000);
		fromFrontEndQueue = new ArrayBlockingQueue<FrontEndUpdate>(5000);
		try {
			serverSocket = new Socket(url, port);
            out = new ObjectOutputStream(serverSocket.getOutputStream());
            in = new ObjectInputStream(serverSocket.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			FrontEndUpdate f = FrontEndUpdate.createNotificationFEU(
				FrontEndUpdate.NotificationType.Connection_Error, 0, 0, null);
			f.setNotificationType(
					FrontEndUpdate.NotificationType.Connection_Error);
			fromServerQueue.add(f);			
		} 
		
		receiveUpdateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!done) {
					try {
						FrontEndUpdate feu = 
							(FrontEndUpdate)in.readObject();
						fromServerQueue.add(feu);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
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
						out.writeObject(feu);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
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
			return fromServerQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * needs to be called in between sessions. We spun off thread, we need to 
	 * kill them
	 */
	public void finish() {
		done = true;
	}
}
