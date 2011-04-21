/**
 * @author Ryan Miller
 */

package multitype.actions;

import multitype.Activator;
import multitype.views.LoginView;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
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
	 * @wbp.parser.entryPoint
	 */
	public LoginWindowAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 * @wbp.parser.entryPoint
	 */
	public void run(IAction action) {

		if (Activator.getDefault().isConnected)
		{
			// Already connected to a server
			Activator.getDefault().showDialogAsync("Server Status", "You are already connected to server " +
					Activator.getDefault().userInfo.getHost() + ":" + Activator.getDefault().userInfo.getPort());
			
		}
		else if (!Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().isPartVisible(Activator.getDefault().userList) 
				|| !Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().isPartVisible(Activator.getDefault().fileList)) 
		{
			//Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().setPartState(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(Activator.getDefault().userList.ID), IStackPresentationSite.STATE_RESTORED);
			//Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().setPartState(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(Activator.getDefault().fileList.ID), IStackPresentationSite.STATE_RESTORED);
		
			Activator.getDefault().showDialogAsync("Prerequisites before Connection", "Please Open both User List and File List.\n(in Window->Show View->Other...->MultiType)");
		}
		else
		{
			Display display = Display.getCurrent();
			Shell shell = new Shell(display);
			LoginView login = new LoginView(shell);
			
			if (login.getReturnCode() == 1)
			{
				// Cancel pressed, do nothing
			}
			else
			{
				// Instantiate a FEUListener, which will also set up a BackendConnection
				Activator.getDefault().connect();
			}
		}
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 * @wbp.parser.entryPoint
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
	 * @wbp.parser.entryPoint
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}