/**
 * @author Yeong-ouk Kim and Ryan Miller
 */

package multitype.command;

import java.util.Iterator;
import multitype.Activator;
import multitype.FEUManager;
import multitype.FEUSender;
import multitype.FrontEndUpdate;
import multitype.FrontEndUpdate.NotificationType;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShareFile extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    
		if (!Activator.getDefault().isConnected)
		{
			Activator.getDefault().showDialogAsync("Error", "You must be connected to a MultiType server to use this feature.");
		}
		else if (!Activator.getDefault().isHost)
		{
			Activator.getDefault().showDialogAsync("Error", "You must be host to use this feature.");
		}
		else
		{
			IStructuredSelection selection = (IStructuredSelection) HandlerUtil
			.getActiveMenuSelection(event);
			
			filesToOpen(selection);
		}

		return null;
	}
	
	public void filesToOpen(IStructuredSelection selectionList)
	{
		Iterator<Object> iterator = selectionList.iterator();
		while (iterator.hasNext())
		{
			Object o = iterator.next();
			
			IResource resource; 
			
			if(o instanceof IResource) {
				resource = (IResource) o;
			}
			else {
		        resource =
		            (IResource) ((IAdaptable) o)
		                .getAdapter(IResource.class);
			}
			
			if (resource != null)
			{
				
				if ( resource.getType() == IResource.FILE)
				{
					IPath path = resource.getLocation();
					String filepath = path.toOSString();

					// Get Filename
					String filename = path.lastSegment();

					
					// Assign fileid / filename map (for host)
					int fileid = Activator.getDefault().fileIDMapping;
					
					Activator.getDefault().sharedFiles.put(fileid, filename);
					
					// Add one to fileIDMapping
					Activator.getDefault().fileIDMapping++;
					
					
					//Activator.getDefault().showDialogAsync("Filename", filename + " with ID: " + fileid);
				
					// Tell EditorManager to open a document
					FEUManager.getInstance().editorManager.openDocument(fileid, filepath);
					
					// Send FEU to notify all non-clients about a new shared file
					FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(NotificationType.New_Shared_File,
							fileid, Activator.getDefault().userInfo.getUserid(), filename);
					FEUSender.send(feu);
					
					// Add the file to "Shared Files"
					// Grab filename from fileid mapping before adding to Shared Files
					Activator.getDefault().fileList.addSharedFile(fileid, filename);
							
				}
			}
		}
		
	}
}
