/**
 * @author Ryan Miller
 */

package multitype.views;

import multitype.Activator;
import multitype.FEUManager;
import multitype.FEUSender;
import multitype.FrontEndUpdate;
import multitype.FrontEndUpdate.NotificationType;
import multitype.UserInfo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import java.util.Iterator;

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

				Activator.getDefault().userList.setHostButton(true);
				Activator.getDefault().userList.setDisconnectButton(true);

				Activator.getDefault().userList.addUserToList(Activator.getDefault().userInfo.getUsername(), Activator.getDefault().userInfo.getUserid());
				
				
				break;
			case New_Shared_File:
				// Non-host clients receive this, add to Shared FileList
				// fileid, content = filename
				Activator.getDefault().fileList.addSharedFile(feu.getFileId(), feu.getContent());
				
				// Add to fileid/filename mapping
				Activator.getDefault().sharedFiles.put(feu.getFileId(), feu.getContent());
				
				
				break;
			case Close_Shared_File:
				// Non-hosts receive this to indicate that the host has stopped sharing a file
				// fileid
				Activator.getDefault().fileList.removeOpenFile(feu.getFileId());
				Activator.getDefault().fileList.removeSharedFile(feu.getFileId());
				
				// Tell editor manager to remove Document for the file with this fileid
				FEUManager.getInstance().editorManager.removeDocumentDueToHostAction(feu.getFileId(),
						Activator.getDefault().sharedFiles.get(feu.getFileId()));
				
				// Editor manager removes from "sharedFiles" map, too
				
				break;
				
			case Get_Shared_File:
				// Host receives this
				// userid of requester, fileid
				// immediately send out Send_File FEU to requesting non_host client with content from Editor
				
				String content = FEUManager.getInstance().editorManager.getTextOfFile(feu.getFileId());
				
				FrontEndUpdate sentFeu = FrontEndUpdate.createNotificationFEU(NotificationType.Send_File, 
						feu.getFileId(),
						feu.getUserId(),
						content);

				FEUSender.send(sentFeu);
				
				break;
			case Send_File:
				// Non-host receives this
				// userid (own), fileid, content

				Activator.getDefault().fileList.removeSharedFile(feu.getFileId());
				
				// Grab filename from fileid mapping before adding to Open Files
				Activator.getDefault().fileList.addOpenFile(feu.getFileId(),
						Activator.getDefault().sharedFiles.get(feu.getFileId()));
				
				// Add a document for the file with this fileid
				FEUManager.getInstance().editorManager.newDocument(feu.getFileId(), feu.getContent());

				
				
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
						// Hide "Open Files" icon for hosts
						Activator.getDefault().fileList.hideOpenFilesList();
					}

					Activator.getDefault().userList.setHostButton(false); //host already exists
				}
				break;
			case Console_Message:
				// Console message received, have ConsoleManager add it to the view
				consoleManager.addConsoleLine(feu.getContent());
				
				break;
			case Chat_Message:
				// Ignore if Chat View is not opened
				ChatView chatView = Activator.getDefault().chatView;
				if (chatView != null)
				{
					// Otherwise, display message in the chat window
					chatView.addMessage(Activator.getDefault().connectedUsers.get(feu.getUserId()), feu.getContent(), false);
				}
				break;
			case Host_Disconnect:
				Activator.getDefault().showDialogAsync("Host Disconnected", "Your files are no longer being shared.");
				Activator.getDefault().userList.deleteUserFromList(feu.getUserId());
				Activator.getDefault().userList.hostId = -1;
				Activator.getDefault().userList.setHostButton(true); //no host anymore
				Activator.getDefault().fileList.clearList();

				// TODO Tell John to stop sending Closed_Shared_file on host_disconnect
				FEUManager.getInstance().editorManager.clearSharedFiles();
				
				break;
			case Server_Disconnect:
				Activator.getDefault().showDialogAsync("Server Disconnected", "Your files are no longer being shared.");
				Activator.getDefault().userList.clearList();
				Activator.getDefault().fileList.clearList();
				if (Activator.getDefault().isHost)
					Activator.getDefault().fileList.showOpenFilesList();
				Activator.getDefault().isConnected = false;
				
				// Clear editor manager documents
				FEUManager.getInstance().editorManager.clearSharedFiles();

				// Clear all fileid/filename mappings
				Activator.getDefault().sharedFiles.clear();
				
				// Clear all userid/username mappings
				Activator.getDefault().connectedUsers.clear();
				
				Activator.getDefault().userList.hostId = -1;
				
				Activator.getDefault().isHost = false;
				
				Activator.getDefault().userList.setHostButton(false);
				Activator.getDefault().userList.setDisconnectButton(false);
				
				// Finish backendclient and feulistener
				Activator.getDefault().disconnect();
				
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
