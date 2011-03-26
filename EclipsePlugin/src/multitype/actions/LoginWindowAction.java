package multitype.actions;

import multitype.Activator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class LoginWindowAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public LoginWindowAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		String username = "";
		String ip = "";
		int port = 0;
		
		InputDialog dialog = new InputDialog(null,"Lets try!",
      			"Please enter your name","",null); // new input dialog
		dialog.open();
		username = dialog.getValue();
		
		dialog = new InputDialog(null,"Lets try!",
	    			"Please enter the IP","",null); // new input dialog
		dialog.open();
		ip = dialog.getValue();
			
		dialog = new InputDialog(null,"Lets try!",
	    			"Please enter the port #","",null); // new input dialog
		dialog.open();
		port = Integer.parseInt(dialog.getValue());
		
		System.out.println(username + ip+port);
		
      /*if( dialog1.open()== IStatus.OK){ // open dialog and wait for return status code.
      					// If user clicks ok display message box
          String value = dialog1.getValue(); // fetch the value entered by the user.
          MessageBox box = new MessageBox(null,SWT.ICON_INFORMATION);
          box.setMessage("Hey there! You entered : " + value);
          box.open();
      }else{
          MessageBox box = new MessageBox(null,SWT.ICON_INFORMATION);
          box.setMessage("Bye!");
          box.open();
      }*/
     
		
		
		// Instantiate a FEUListener, which will also set up a BackendConnection
		Activator.getDefault().connect(username, ip, port);
		
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}