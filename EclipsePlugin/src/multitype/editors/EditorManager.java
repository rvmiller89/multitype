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
import multitype.views.SaveDialog;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * @author Azfar Khandoker
 *
 */
 
public class EditorManager
{
	private Map<Integer, Document> map;
	
	public EditorManager()
	{
	    map = new HashMap<Integer, Document>();
	    
	    getPage().addPartListener(new IPartListener() {
	    	public void partClosed(IWorkbenchPart part) {
				if (part instanceof IEditorPart)
				{
					//TODO disable default partlistener
					ITextEditor editor = (ITextEditor)part;					
					Iterator<Integer> iter = Activator.getDefault().sharedFiles.keySet().iterator();
					int id;
					while (iter.hasNext())
					{
						id = iter.next();
						if (Activator.getDefault().sharedFiles.get(id).equals(editor.getTitle()))
						{
							removeDocumentDueToUserInput(id);
							
							return;
						}
					}
				}	
			}
			
			public void partOpened(IWorkbenchPart part) {}
			
			public void partDeactivated(IWorkbenchPart part) {}
			
			public void partBroughtToTop(IWorkbenchPart part) {}
			
			public void partActivated(IWorkbenchPart part) {}
		});
	}
	
	public void openDocument(int fileID, String filePath)
	{
		IEditorPart[] editors = getPage().getDirtyEditors();
		for (int i = 0 ; i < editors.length ; i++)
		{
			//TODO if tab is edited, but not written to disk, tab is replaced with disk content....
			if (((IResource)editors[i].getEditorInput().getAdapter(IResource.class)).getFullPath().toOSString().equals(filePath))
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
	
	public void removeDocumentDueToHostAction(final int fileID)
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = new Shell(Display.getCurrent());
				SaveDialog dialog = new SaveDialog(shell, "Host Closed Shared File", "Would you like to save?", Activator.getDefault().sharedFiles.get(fileID));
				String filePath = dialog.getFilepath();
				
				if (filePath != null)
				{
					ITextEditor editor = (ITextEditor)map.get(fileID).getEditor();
					
					saveTab(editor.getDocumentProvider().getDocument(editor.getEditorInput()).get(), filePath);
				}
				
				map.remove(fileID).getTitle();
				
				getPage().closeEditor(map.get(fileID).getEditor(), false);
			}
		});		
	}
	
	private void saveTab(String content, String filePath)
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
	}
	
	public void removeDocumentDueToUserInput(final int fileID)
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (Activator.getDefault().isHost)
				{
					FEUSender.send(
							FrontEndUpdate.createNotificationFEU(
									FrontEndUpdate.NotificationType.Close_Shared_File,
									fileID,
									Activator.getDefault().userInfo.getUserid(),
									map.get(fileID).getTitle()));
				}
				
				Shell shell = new Shell(Display.getCurrent());
				SaveDialog dialog = new SaveDialog(shell, "Closing Shared File", "Would you like to save?", Activator.getDefault().sharedFiles.get(fileID));
				String filePath = dialog.getFilepath();
				
				if (filePath != null)
				{
					ITextEditor editor = (ITextEditor)map.get(fileID).getEditor();
					
					saveTab(editor.getDocumentProvider().getDocument(editor.getEditorInput()).get(), filePath);
				}

				map.remove(fileID).getTitle();
				
				getPage().closeEditor(map.get(fileID).getEditor(), false);
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
