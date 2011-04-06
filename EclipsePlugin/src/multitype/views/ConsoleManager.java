/**
 * @author Ryan Miller
 */

package multitype.views;

import multitype.Activator;

import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.*;

public class ConsoleManager implements IConsoleLineTracker{
	
	private static MessageConsole theConsole = null;

	public ConsoleManager() {
		
	}
	
	public final void setConsoleMessage(String message)
	{	
		//Activator.getDefault().showDialogAsync("Test", "here");
		
		if (theConsole == null)
		{
			// Link to plugin's console
			String name = "MultiType Console";
			
			ConsolePlugin plugin = ConsolePlugin.getDefault();
			IConsoleManager conMan = plugin.getConsoleManager();
			IConsole[] existing = conMan.getConsoles();
			for (int i = 0; i < existing.length; i++)
				if (name.equals(existing[i].getName()))
					theConsole = (MessageConsole) existing[i];
			
			// If theConsole is still null, create the Multitype Console
		 	MessageConsole multitypeConsole = new MessageConsole(name, null);
		 	conMan.addConsoles(new IConsole[]{multitypeConsole});
		 	theConsole = multitypeConsole;
		}
		
		// Clear the console
		theConsole.clearConsole();
		
		// Now you can write to it
		
		MessageConsoleStream out = theConsole.newMessageStream();
		out.println(message);
		
		// Make sure this view is presented on screen
		// TODO this doesn't quite work...
		IWorkbenchWindow window= 
			Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		
		if (window != null) {
		    IWorkbenchPage page = window.getActivePage();
		    if (page != null) {
		       
				String id = IConsoleConstants.ID_CONSOLE_VIEW;
				
				IConsoleView view = null;
				try {
					view = (IConsoleView) page.showView(id);
				} catch (PartInitException e) {
					Activator.getDefault().showDialogAsync("Error", e.toString());
					e.printStackTrace();
				}
				view.display((IConsole)theConsole);
		    }
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(org.eclipse.debug.ui.console.IConsole arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lineAppended(IRegion arg0) {
		// TODO Auto-generated method stub
		Activator.getDefault().showDialogAsync("Revd", "Got Console Line");
	}

}
