/**
 * @author Ryan Miller
 */

package multitype.views;

import multitype.Activator;
import multitype.FEUSender;
import multitype.FrontEndUpdate;
import multitype.FrontEndUpdate.NotificationType;
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
				
				FrontEndUpdate connectedFEU = 
					FrontEndUpdate.createNotificationFEU(FrontEndUpdate.NotificationType.User_Connected, 
							-1, feu.getUserId(), Activator.getDefault().userInfo.getUsername());
				FEUSender.send(connectedFEU);

				// Tell backendclient our userid
				Activator.getDefault().client.setUserId(feu.getUserId());
				
				Activator.getDefault().isConnected = true;
				Activator.getDefault().showDialogAsync("Connection Success", "Successfully connected. You are user: " + Activator.getDefault().userInfo.getUserid());

				Activator.getDefault().userList.setButton(true);

				Activator.getDefault().userList.addUserToList(Activator.getDefault().userInfo.getUsername(), Activator.getDefault().userInfo.getUserid());
				
				// Ignore if Chat View is not opened
				ChatView chatView = Activator.getDefault().chatView;
				if (chatView != null)
				{
					// Otherwise, enable the ability to send chat messages
					chatView.enableChat();
				}
				
				
				break;
			case New_Shared_File:
				// Non-host clients receive this, add to Shared FileList
				// fileid, content = filename
				Activator.getDefault().fileList.addSharedFile(feu.getFileId(), feu.getContent());
				
				break;
			case Close_Shared_File:
				break;
			case Get_Shared_File:
				// Host receives this
				// userid of requester, fileid
				// immediately send out Send_File FEU to requesting non_host client with content from Editor

				// TODO Azfar - have this grab the content from the associated <<Document>> with feu.getFileId()
				
				String content = "Hello world.";
				
				FrontEndUpdate sentFeu = FrontEndUpdate.createNotificationFEU(NotificationType.Send_File, 
						feu.getFileId(),
						feu.getUserId(),
						content);

				FEUSender.send(sentFeu);
				
				break;
			case Send_File:
				// Non-host receives this
				// userid (own), fileid, content

				// TODO Azfar - open editor on screen with feu.getContent() for feu.getFileID();
				
				Activator.getDefault().fileList.removeSharedFile(feu.getFileId());
				Activator.getDefault().fileList.addOpenFile(feu.getFileId(), "Debug.txt");
				// TODO grab filename from fileid mapping before adding to Open Files
				
				break;
			case User_Connected:
				Activator.getDefault().userList.addUserToList(feu.getContent(), feu.getUserId());
				break;
			case User_Disconnected:
				Activator.getDefault().userList.deleteUserFromList(feu.getUserId());
				break;
			case Request_Host:
				break;
			case New_Host:
				if (feu.getUserId() != -1) {
					Activator.getDefault().userList.hostId = feu.getUserId();
					if (Activator.getDefault().userInfo.getUserid() == feu.getUserId())
					{
						Activator.getDefault().isHost = true;
						Activator.getDefault().fileList.removeOpenFilesFolder();
					}
					Activator.getDefault().userList.setButton(false); //host already exists
				}
				break;
			case Console_Message:
				// Console message received, have ConsoleManager add it to the view
				consoleManager.addConsoleLine(feu.getContent());
				
				break;
			case Chat_Message:
				// Ignore if Chat View is not opened
				chatView = Activator.getDefault().chatView;
				if (chatView != null)
				{
					// Otherwise, display message in the chat window
					chatView.addMessage(Activator.getDefault().connectedUsers.get(feu.getUserId()), feu.getContent(), false);
				}
				break;
			case Host_Disconnect:
				Activator.getDefault().showDialogAsync("Connection Error", "Host disconnected.");
				Activator.getDefault().userList.deleteUserFromList(feu.getUserId());
				Activator.getDefault().userList.hostId = -1;
				Activator.getDefault().userList.setButton(true); //no host anymore
				//Activator.getDefault().isConnected = false;
				break;
			case Server_Disconnect:
				Activator.getDefault().showDialogAsync("Connection Error", "Server disconnected.");
				Activator.getDefault().userList.clearList();
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
