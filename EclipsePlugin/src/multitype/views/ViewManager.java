package multitype.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import multitype.Activator;
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
				//MessageDialog.openError(null, "Connection Error", "Unable to connect.");
				//System.out.println("CONNECTION ERROR");
				Activator.getDefault().showDialogAsync("Connection Error", "Unable to connect.");
				break;
			case Connection_Succeed:
				MessageDialog.openError(null, "Connection Success", "Successfully connected.");
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
				MessageDialog.openError(null, "Connection Error", "Host disconnected.");
				break;
			case Server_Disconnect:
				MessageDialog.openError(null, "Connection Error", "Server disconnected.");
				break;
			
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
