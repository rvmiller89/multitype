package multitype.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import multitype.Activator;
import multitype.BackendClient;
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
				// first save userid
				Activator.getDefault().userInfo.setUserid(feu.getUserId());
				
				BackendClient bc = Activator.getDefault().client;
				FrontEndUpdate connectedFEU = 
					FrontEndUpdate.createNotificationFEU(FrontEndUpdate.NotificationType.User_Connected, 
							-1, feu.getUserId(), null);
				bc.sendUpdate(connectedFEU);
				
				Activator.getDefault().showDialogAsync("Connection Success", "Successfully connected.");
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
			
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		
	}

	@Override
	public void setFocus() {
		
	}

}
