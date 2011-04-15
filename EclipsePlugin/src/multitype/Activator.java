package multitype;

import java.util.HashMap;
import java.util.Map;

import multitype.FrontEndUpdate.NotificationType;
import multitype.views.ChatView;
import multitype.views.Dialog;
import multitype.views.FileList;
import multitype.views.UserList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "MultiType"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	public BackendClient client = null;	// also used by ViewDriver....
	private FEUListener feuListener;
	public UserInfo userInfo;
	public boolean isConnected;
	public boolean isHost;

	public UserList userList;
	public FileList fileList;
	
	public UserInfo getUserInfo() {
		return userInfo;
	}

	public ChatView chatView;
	
	public int fileIDMapping;
	
	public Map<Integer, String> connectedUsers;
	public Map<Integer, String> sharedFiles;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		userInfo = new UserInfo();
		// Start an unconnected client with userid = -2 (not -1 because Rodrigo's a douche)
		userInfo.setUserid(-2);
		isConnected = false;
		isHost = false;
		connectedUsers = new HashMap<Integer, String>();
		sharedFiles = new HashMap<Integer, String>();
		fileIDMapping = 0;
	}

	public void stop(BundleContext context) throws Exception {
		
		// Disconnect from server if connected
		if (isConnected)
		{
			FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(
					NotificationType.User_Disconnected, -1, Activator.getDefault().userInfo.getUserid(), 
					Activator.getDefault().userInfo.getUsername());
			FEUSender.send(feu);
		}

		plugin = null;
		super.stop(context);
	}
	
	public void setupUser(String username, String password, String host, int port)
	{
		userInfo.setUsername(username);
		userInfo.setPassword(password);
		userInfo.setHost(host);
		userInfo.setPort(port);
	}
	
	/**
	 * Instantiates a FEUListener and BackendConnection
	 * 
	 * PRECONDITION:  USER INFO WAS ENTERED BY LOGIN WINDOW (Activator.setupUser())
	 * @param url
	 * @param port
	 */
	public void connect()
	{
		// Construct a BackendClient
		client = new BackendClient(userInfo.getHost(), userInfo.getPort());
		
		// Construct a FEUListener and start thread
		// This also starts a FEUManager (which starts EditorManager and ViewManager)
		feuListener = new FEUListener(client);
		feuListener.start();
		
		// Start BackendClient
		client.connect();
	}
	
	public void disconnect() {
		client.finish();
		feuListener.finish();
	}
	
	public void showDialogAsync(final String title, final String message)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	Display display = Display.getCurrent();
				Shell shell = new Shell(display);
				Dialog dialog = new Dialog(shell, title, message);
		    }
		  });
	}
	

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
