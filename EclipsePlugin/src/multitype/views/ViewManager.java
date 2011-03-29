package multitype.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import multitype.Activator;
import multitype.BackendClient;
import multitype.FEUSender;
import multitype.FrontEndUpdate;

public class ViewManager extends ViewPart{

	public ViewManager() {

	}
	
	public void receive(FrontEndUpdate feu)
	{
		switch (feu.getNotificationType())
		{
			case New_Connection:
				break;
			case Connection_Error:
				Activator.getDefault().showDialogAsync("Connection Error", "Unable to connect.");
				break;
			case Connection_Succeed:

				// Save userid and respond with User_Connected
				Activator.getDefault().userInfo.setUserid(feu.getUserId());

				FrontEndUpdate connectedFEU = 
					FrontEndUpdate.createNotificationFEU(FrontEndUpdate.NotificationType.User_Connected, 
							-1, feu.getUserId(), null);
				FEUSender.send(connectedFEU);
				
				Activator.getDefault().isConnected = true;
				Activator.getDefault().showDialogAsync("Connection Success", "Successfully connected. You are user: " + Activator.getDefault().userInfo.getUserid());

				break;
			case New_Shared_File:
				break;
			case Close_Shared_File:
				break;
			case Get_Shared_File:
				break;
			case User_Connected:
				break;
			case User_Disconnected:
				break;
			case Request_Host:
				break;
			case New_Host:
				break;
			case Host_Disconnect:
				Activator.getDefault().showDialogAsync("Connection Error", "Host disconnected.");
				break;
			case Server_Disconnect:
				Activator.getDefault().showDialogAsync("Connection Error", "Server disconnected.");
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
