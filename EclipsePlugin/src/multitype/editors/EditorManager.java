package multitype.editors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import multitype.Activator;
import multitype.FEUSender;
import multitype.FrontEndUpdate;
import multitype.FrontEndUpdate.NotificationType;
import multitype.views.Dialog;
import multitype.views.SaveDialog;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * @author Azfar Khandoker & Ryan Miller
 *
 */
 
public class EditorManager
{
	private Map<Integer, Document> map;
	
	private final IPartListener PART_LISTENER = new IPartListener()
	{

		@Override
		public void partActivated(IWorkbenchPart part) {
			
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
			
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
			if (part instanceof IEditorPart)
			{
				IEditorPart closingEditor = (IEditorPart)part;	
				
				Iterator<Integer> iter = map.keySet().iterator();
				int id;
				while (iter.hasNext())
				{
					id = iter.next();
					IEditorPart openEditor = map.get(id).getEditor();
					if (closingEditor == openEditor)	// Object comparison, if equal then this is the tab thats closing
					{
						if (Activator.getDefault().isHost)
						{
							// Host closes a file
							
							// send out Close_Shared_File feu to server to stop sending updates
							FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(
									NotificationType.Close_Shared_File, 
									id,
									Activator.getDefault().userInfo.getUserid(),
									Activator.getDefault().sharedFiles.get(id));
							FEUSender.send(feu);
							
							// Tell editor manager to close tab with file with fileid (item.getFileid())
							removeDocumentDueToUserInput(id, true);
							
							// Remove from shared file list
							Activator.getDefault().fileList.removeSharedFile(id);
						}
						else
						{
							// Non-host closes a tab
							
							// send out Close_Client_File feu to server to stop receiving updates
							FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(
									NotificationType.Close_Client_File, 
									id,
									Activator.getDefault().userInfo.getUserid(),
									null);
							FEUSender.send(feu);
							
							/*Activator.getDefault().showDialogAsync("Debug", "Sending Close_Client_File with fileid: " + 
									id + " and filename: " + Activator.getDefault().sharedFiles.get(id) + " from user: "
									+ Activator.getDefault().userInfo.getUserid() + ": " 
									+ Activator.getDefault().userInfo.getUsername());*/

							
							// Tell editor manager to close tab with file with fileid (item.getFileid())
							removeDocumentDueToUserInput(id, true);
	
							// add to Shared Files list
							Activator.getDefault().fileList.addSharedFile(id,
									Activator.getDefault().sharedFiles.get(id));
							
							// remove from Open files list
							Activator.getDefault().fileList.removeOpenFile(id);
							
						} // else
						
						break;
						
					} // if objects are equal
				} // while
			} // if part is instanceof IEditorPart
			
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
			
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
			
		}
		
	};
	
	public EditorManager()
	{
	    map = new HashMap<Integer, Document>();
	    
	    getPage().addPartListener(PART_LISTENER);
	}
	
	public void openDocument(int fileID, String filePath)
	{
		IEditorPart[] editors = getPage().getDirtyEditors();
		for (int i = 0 ; i < editors.length ; i++)
		{
			if (filePath.endsWith(((IResource)editors[i].getEditorInput().getAdapter(IResource.class)).getFullPath().toOSString()))
			{
				map.put(fileID, new Document((ITextEditor)editors[i], fileID));
				
				return;
			}
		}
		
		Scanner scanner = null;
		
		try
		{
			scanner = new Scanner(new File(filePath));
		}
		catch (FileNotFoundException e)
		{
			System.err.println("*********************************FILE NOT FOUND: " + filePath);
			
			return;
		}
		
		String content = "";
		while (scanner.hasNext())
		{
			content += scanner.nextLine() + '\n';
		}
		
		scanner.close();
		
		IWorkbenchPage page = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IFileStore fs = EFS.getLocalFileSystem().getStore(new File(filePath).toURI());
		
		IEditorPart editor = null;
		try {
			editor = IDE.openEditorOnFileStore(page, fs);
		} catch (PartInitException e) {
			System.err.println("*********************************PART INIT EXCEPTION: " + e.getMessage());
			return;
		}
		
		map.put(fileID, new Document((ITextEditor)editor, fileID));
		
		map.get(fileID).setText(content);
	}
	
	public void newDocument(final int fileID, final String content)
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					IEditorPart editor = Activator
							.getDefault()
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.openEditor(new StringEditorInput(content, fileID),
									"org.eclipse.ui.DefaultTextEditor");
					map.put(fileID, new Document((ITextEditor)editor, fileID));
					map.get(fileID).setText(content);
				} catch (PartInitException e) {
					System.err
							.println("*********************************PART INIT EXCEPTION: "
									+ e.getMessage());
				}
			}
		});
	}
	
	public void removeDocumentDueToHostAction(final int fileID, final String filename)
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = new Shell(Display.getCurrent());
				Dialog dialog = new Dialog(shell, "Server Notification", "Host closed shared file: " + filename);
				
				if (map.get(fileID) != null)
				{
	                map.get(fileID).disableListeners();
	                
	                // Prompt to save and close tab
					//getPage().saveEditor( map.get(fileID).getEditor(), true);
	                
	                map.get(fileID).getEditor().doSaveAs();

                	// Remove part listener so that removing from the FileList doesnt trigger the same
                	// action as removing by X-ing out the tab
            	    getPage().removePartListener(PART_LISTENER);

                	getPage().closeEditor( map.get(fileID).getEditor(), false);

            	    getPage().addPartListener(PART_LISTENER);
	
					map.remove(fileID);
				}
				
				// Remove file mapping (this file is no longer needed)
				Activator.getDefault().sharedFiles.remove(fileID);
				
			}
		});		
	}
	
	public void clearSharedFiles() {
		Iterator<Integer> iter = map.keySet().iterator();
		int id;
		while (iter.hasNext())
		{
			id = iter.next();
			map.get(id).disableListeners();
			// call to removeDocumentDueToHost...
		}
		map.clear();
	}
	
	/*private void saveTab(String content, String filePath)
	{
		PrintWriter writer = null;
		
		try {
			writer = new PrintWriter(new File(filePath));
		} catch (FileNotFoundException e) {
			System.err.println("*********************************FILE NOT FOUND EXCEPTION: " + e.getMessage());
			e.printStackTrace();
			
			return;
		}
		
		writer.print(content);
		
		writer.flush();
		writer.close();
	}*/
	
	/**
	 * When a host or non host right-clicks an item from their file list and
	 * clicks "Close File".
	 * 
	 * Hosts will send out a Close_Shared_File FEU.
	 * 
	 * Both host and non-hosts remove the file mapping locally
	 * 
	 * @param fileID the id of the file to close
	 * @param isTabClose whether or not a tab close is what's causing this remove document
	 */
	public void removeDocumentDueToUserInput(final int fileID, final boolean isTabClose)
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				
				// NOTE: FileList sends out the FEU for hosts (Close_Shared_File)
                
                // Prompt to save and close tab
                if (!isTabClose)
                {

                	if (!Activator.getDefault().isHost)
                		map.get(fileID).getEditor().doSaveAs();
                	
                    map.get(fileID).disableListeners();
                	
                	// Remove part listener so that removing from the FileList doesnt trigger the same
                	// action as removing by X-ing out the tab
            	    getPage().removePartListener(PART_LISTENER);

            	    if (Activator.getDefault().isHost)
            	    	getPage().closeEditor( map.get(fileID).getEditor(), true);
            	    else
            	    	getPage().closeEditor( map.get(fileID).getEditor(), false);

            	    getPage().addPartListener(PART_LISTENER);

                }
                //getPage().closeEditor( map.get(fileID).getEditor(), false);

				map.remove(fileID);
				
				// Remove file mapping (only for hosts... they will have to re-share to start it up again
				// Non-hosts can still start receiving updates again though
				if (Activator.getDefault().isHost)
					Activator.getDefault().sharedFiles.remove(fileID);
			}
		});
	}
	
	public String getTextOfFile(int fileID)
	{
		return map.get(fileID).getText();
	}
	
	public void receive(FrontEndUpdate feu)
	{
		switch(feu.getMarkupType())
		{
			case Cursor:
				map.get(feu.getFileId()).cursorPos(feu);
				break;
			case Delete:
				map.get(feu.getFileId()).delete(feu);
				break;
//			case Highlight:
//				map.get(feu.getFileId()).highlight(feu);
//				break;
			case Insert:
				map.get(feu.getFileId()).insert(feu);
				break;
			default:
				throw new IllegalArgumentException("BAD FEU MARKUP TYPE: " + feu.getMarkupType());
		}
	}
	
	private IWorkbenchWindow getWindow()
	{
		return Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	private IWorkbenchPage getPage()
	{
		return Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
	
	private IEditorReference[] getReferences()
	{
		return Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
	}
}
