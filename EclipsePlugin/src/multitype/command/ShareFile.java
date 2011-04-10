/**
 * @author Yeong-ouk Kim and Ryan Miller
 */

package multitype.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import multitype.Activator;

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
	    

		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
		.getActiveMenuSelection(event);
		
		filesToOpen(selection);

		return null;
	}
	
	public void filesToOpen(IStructuredSelection selectionList)
	{
		List<String> filePaths = new ArrayList<String>();

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
					filePaths.add(filepath);
				
					// Debug
					Activator.getDefault().showDialogAsync("Debug", "Filepath: " + filepath);
				}
			}
		}
		
		// TODO
		// Now call EditorManager foreach filepath in list to open
		
	}
}
