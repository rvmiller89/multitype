package multitype.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import multitype.FrontEndUpdate;

public class ViewManager extends ViewPart{

	public ViewManager() {
		// TODO Auto-generated constructor stub
	}
	
	public void receive(FrontEndUpdate feu)
	{
		switch (feu.getNotificationType())
		{
			case Connection_Error:
				MessageDialog.openError(null, "Connection Error", "Unable to connect.");
				break;
			case Connection_Succeed:
				MessageDialog.openError(null, "Connection Success", "Successfully connected.");
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
