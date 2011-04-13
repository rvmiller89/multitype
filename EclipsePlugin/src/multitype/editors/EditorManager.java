package multitype.editors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import multitype.Activator;
import multitype.FrontEndUpdate;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.text.BadLocationException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
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
			
			@Override
			public void partOpened(IWorkbenchPart part) {
				System.out.println(part.getTitle() + "opened");
				
			}
			
			@Override
			public void partDeactivated(IWorkbenchPart part) {
				System.out.println(part.getTitle() + "deactivated");
				
			}
			
			@Override
			public void partClosed(IWorkbenchPart part) {
				System.out.println(part.getTitle() + "closed");
				
			}
			
			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
				System.out.println(part.getTitle() + "brought to top");
				
			}
			
			@Override
			public void partActivated(IWorkbenchPart part) {
				System.out.println(part.getTitle() + "activated");
				
			}
		});
	    
	    Activator.getDefault().getWorkbench().addWindowListener(new IWindowListener() {
			
			@Override
			public void windowOpened(IWorkbenchWindow window) {
				System.out.println("window opened");
				
			}
			
			@Override
			public void windowDeactivated(IWorkbenchWindow window) {
				System.out.println("window deactivated");
				
			}
			
			@Override
			public void windowClosed(IWorkbenchWindow window) {
				System.out.println("window closed");
				
			}
			
			@Override
			public void windowActivated(IWorkbenchWindow window) {
				System.out.println("window activated");
				
			}
		});
	}
	
	public void openDocument(int fileID, String filePath)
	{
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
		
		//TODO CHECK IF TAB IS OPEN ALREADY
		
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
		
		//TODO sync refs
		
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
										
					//TODO sync refs
					
					map.get(fileID).setText(content);
				} catch (PartInitException e) {
					System.err
							.println("*********************************PART INIT EXCEPTION: "
									+ e.getMessage());
				}
			}
		});
	}
	
	public void removeDocument(final int fileID)
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				getPage().saveEditor(map.get(fileID).getEditor(), true);
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
	
	private void syncRefs()
	{
		Collection<Document> editors = new ArrayList<Document>(map.values());
		IEditorReference[] refs = getReferences();
		int size = editors.size();
		
		for (int i = 0 ; i < refs.length ; i++)
		{
			for (int j = 0 ; j < size ; j++)
			{
				
			}
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
