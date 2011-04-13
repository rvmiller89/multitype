package multitype.editors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import multitype.Activator;
import multitype.FrontEndUpdate;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.text.BadLocationException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * 
 * @author Azfar Khandoker
 *
 */
 
public class EditorManager
{
	private Map<Integer, Document> map;
	private int count = 0;
	
	public EditorManager()
	{
	    map = new HashMap<Integer, Document>();
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
		
		IWorkbenchPage page = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IFileStore fs = EFS.getLocalFileSystem().getStore(new File(filePath).toURI());
		
		try {
			IDE.openEditorOnFileStore(page, fs);
		} catch (PartInitException e) {
			System.err.println("*********************************PART INIT EXCEPTION: " + e.getMessage());
			return;
		}
		
		map.put(fileID, new Document(getReferences()[count++], fileID));
		map.get(fileID).setText(content);
	}
	
	public void newDocument(final int fileID, final String content)
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					Activator
							.getDefault()
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.openEditor(new StringEditorInput(content, fileID),
									"org.eclipse.ui.DefaultTextEditor");
					map.put(fileID, new Document(getReferences()[count++],
							fileID));
					map.get(fileID).setText(content);
				} catch (PartInitException e) {
					System.err
							.println("*********************************PART INIT EXCEPTION: "
									+ e.getMessage());
				}
			}
		});
	}
	
	public void removeDocument(int fileID)
	{
		// TODO maybe prompt to save files here?
		
		//TODO
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
	
	private IEditorReference[] getReferences()
	{
		return Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
	}
}
