package multitype.editors;

import multitype.Activator;
import multitype.FEUSender;
import multitype.FrontEndUpdate;
import multitype.FrontEndUpdate.MarkupType;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * @author Azfar Khandoker
 *
 */
 
public class EditorManager
{
	private ITextEditor editor;
	private IDocumentProvider dp;
	private IDocument doc;
	private DocListener listener;
	
	public EditorManager()
	{
		editor = ActiveEditor.getEditor();
	    dp = editor.getDocumentProvider();
	    doc = dp.getDocument(editor.getEditorInput());
	    
	    // Add document listener
	    listener = new DocListener();
	    doc.addDocumentListener(listener);
	}
	
	public void receive(FrontEndUpdate feu)
	{
		//Activator.getDefault().showDialogAsync("Debug", feu.getUserId() + " ");		// DEBUG
		MarkupType markupType = feu.getMarkupType();
		
		switch(markupType)
		{
		case Cursor:
			cursorPos(feu.getStartLocation(), feu.getUserId());
			break;
		case Delete:
			delete(feu.getFileId(), feu.getUserId(), feu.getStartLocation(), feu.getEndLocation());
			break;
		case Highlight:
			highlight(feu.getFileId(), feu.getUserId(), feu.getStartLocation(), feu.getEndLocation());
			break;
		case Insert:
			insert(feu.getFileId(), feu.getUserId(), feu.getStartLocation(), feu.getInsertString());
			break;
		default:
			throw new IllegalArgumentException("BAD FEU MARKUP TYPE: " + markupType);
		}
		
	}
	
	private void delete(int fileId, int userId, final int fromPos, final int toPos)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		doc.removeDocumentListener(listener);
		    		doc.replace(fromPos, toPos - fromPos, "");
					doc.addDocumentListener(listener); 
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
		    }
		  });
		System.out.println("Editor Deletion-- fromPos: " + fromPos);
	}
	
	private void insert(int fileId, int userId, final int fromPos, final String string)
	{
    	Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		doc.removeDocumentListener(listener);
					doc.replace(fromPos, 0, string);
					doc.addDocumentListener(listener);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
		    }
		  });
    	
		System.out.println("Editor Insertion-- fromPos: " + fromPos + " string: " + string);
	}
	
	private void highlight(int fileId, int userId, int fromPos, int toPos)
	{
		// TODO: build 2
//		ISelectionProvider selectionProvider = editor.getSelectionProvider();
//	    ITextSelection selection = (ITextSelection) selectionProvider.getSelection();
//	    
//	    String text = selection.getText();
	}
	
	private void cursorPos(int pos, int userId)
	{
		// TODO: build 2
	}
	
	private class DocListener implements IDocumentListener
	{

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {

			
		}

		@Override
		public void documentChanged(DocumentEvent event) {	

			//key pressed....need to generate FEU
			
//				System.out.println("Rev. #" + event.fModificationStamp + ": INSERTED TEXT: " + event.fText);	// TEST
			//Activator.getDefault().userInfo.getUserid();
			FrontEndUpdate feu;
			
			if (event.fText.equals(""))
			{
				feu = FrontEndUpdate.createDeleteFEU(
						0, 
						Activator.getDefault().userInfo.getUserid(), 
						event.fOffset, 
						event.fLength + event.fOffset);
			}
			
			else
			{
				feu = FrontEndUpdate.createInsertFEU(
						0, 
						Activator.getDefault().userInfo.getUserid(),
						event.fOffset,
						event.fText);
			}
			
			FEUSender.send(feu);

			//event.
		}
		
	}
}
