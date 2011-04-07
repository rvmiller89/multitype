/**
 * @author Ryan Miller
 */

package multitype.views;

import multitype.Activator;
import multitype.FEUSender;
import multitype.FrontEndUpdate;
import multitype.UserInfo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class ViewManager extends ViewPart{
	
	public ConsoleManager consoleManager = null;

	public ViewManager() {
		consoleManager = new ConsoleManager();
	}
	
	public void receive(FrontEndUpdate feu)
	{
		switch (feu.getNotificationType())
		{
			case New_Connection:
				break;
			case Connection_Error:
				Activator.getDefault().showDialogAsync("Connection Error", "Unable to connect.\n\n" + feu.getContent());
				break;
			case Connection_Succeed:

				// Save userid and respond with User_Connected
				Activator.getDefault().userInfo.setUserid(feu.getUserId());
				
				// TODO This value is already set from the LoginView...
				// probably remove this call
				Activator.getDefault().userInfo.setUsername(feu.getContent());
				
				FrontEndUpdate connectedFEU = 
					FrontEndUpdate.createNotificationFEU(FrontEndUpdate.NotificationType.User_Connected, 
							-1, feu.getUserId(), null);
				FEUSender.send(connectedFEU);
				
				Activator.getDefault().isConnected = true;
				Activator.getDefault().showDialogAsync("Connection Success", "Successfully connected. You are user: " + Activator.getDefault().userInfo.getUserid());
				
				// TODO run asynchronously
				Activator.getDefault().userList.hostRequestButton.setEnabled(true);
				// TODO take this function out of Activator
				// TODO run asynchronously
				Activator.getDefault().addUserToList(feu);
				break;
			case New_Shared_File:
				break;
			case Close_Shared_File:
				break;
			case Get_Shared_File:
				break;
			case User_Connected:
				// TODO take this function out of Activator
				// TODO run asynchronously
				Activator.getDefault().addUserToList(feu);
				break;
			case User_Disconnected:
				// TODO take this function out of Activator
				// TODO run asynchronously
				Activator.getDefault().deleteUserFromList(feu);
				break;
			case Request_Host:
				break;
			case New_Host:
				
				// TODO check on this... first of all, is content (a string) containing the userid of the new host?
				// second, is the userid associated with this FEU the same as yourself (which means it's being ignored)
				Activator.getDefault().userInfo.setHost(feu.getContent());
				// TODO run asynchronously
				Activator.getDefault().userList.hostRequestButton.setEnabled(false);
				break;
			case Console_Message:
				// Console message received, have ConsoleManager add it to the view
				consoleManager.addConsoleLine(feu.getContent());
				
				break;
			case Chat_Message:
				// if there is time
				break;
			case Host_Disconnect:
				Activator.getDefault().showDialogAsync("Connection Error", "Host disconnected.");

				Activator.getDefault().isConnected = false;
				break;
			case Server_Disconnect:
				Activator.getDefault().showDialogAsync("Connection Error", "Server disconnected.");

				Activator.getDefault().isConnected = false;
				break;
			default:
				Activator.getDefault().showDialogAsync("FrontEndUpdate Error", "Unknown FrontEndUpdate receieved.");
				break;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		
	}

	@Override
	public void setFocus() {
		
	}

}
