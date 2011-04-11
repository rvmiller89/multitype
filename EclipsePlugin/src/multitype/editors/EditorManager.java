package multitype.editors;

import java.util.HashMap;
import java.util.Map;

import multitype.Activator;
import multitype.FrontEndUpdate;

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
	    map.put(0, new Document(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences()[0], 0));
	}
	
	public void newDocument(int fileID)
	{
		if (Activator.getDefault().isHost)
		{
			/*
			 * use file i/o to open
			 */
		}
		
		else
		{
			//something else
		}
	}
	
	public void removeDocument(int fileID)
	{
		// TODO maybe prompt to save files here?
		
		//TODO
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
			case Highlight:
				map.get(feu.getFileId()).highlight(feu);
				break;
			case Insert:
				map.get(feu.getFileId()).insert(feu);
				break;
			default:
				throw new IllegalArgumentException("BAD FEU MARKUP TYPE: " + feu.getMarkupType());
		}
	}
}
