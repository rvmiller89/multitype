/**
 * @author Ryan Miller
 */

package multitype.views;

import multitype.Activator;
import multitype.FEUSender;
import multitype.FrontEndUpdate;
import multitype.FrontEndUpdate.NotificationType;

import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.*;

public class ConsoleManager implements IConsoleLineTracker{
	
	private org.eclipse.debug.ui.console.IConsole listenConsole;
	
	private static MessageConsole theConsole = null;

	public ConsoleManager() {
		
	}
	
	public final void addConsoleLine(String message)
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
		
	}

	@Override
	public void init(org.eclipse.debug.ui.console.IConsole console) {
		listenConsole = console;
		
	}

	@Override
	public void lineAppended(IRegion region) {
		try {
			// Grab line of output
			String line = listenConsole.getDocument().get(region.getOffset(), region.getLength());
			
			// Send line to other clients
			FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(NotificationType.Console_Message,
					0,
					Activator.getDefault().userInfo.getUserid(),
					line);
			FEUSender.send(feu);
			
			
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}
