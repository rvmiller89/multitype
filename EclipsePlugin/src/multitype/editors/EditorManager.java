package multitype.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import multitype.FrontEndUpdate;
import multitype.FrontEndUpdate.MarkupType;

public class EditorManager
{
	private ITextEditor editor;
	private IDocumentProvider dp;
	private IDocument doc;
	
	public EditorManager()
	{
		editor = ActiveEditor.getEditor();
	    dp = editor.getDocumentProvider();
	    doc = dp.getDocument(editor.getEditorInput());
	    
	    // Add document listener
	    DocListener listener = new DocListener();
	    doc.addDocumentListener(listener);
	}
	
	public void receive(FrontEndUpdate feu)
	{
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
	
	private void delete(int fileId, int userId, int fromPos, int toPos)
	{
		try {
			doc.replace(fromPos, toPos - fromPos, "");
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private void insert(int fileId, int userId, int fromPos, String string)
	{
	    try {
			doc.replace(fromPos, 0, string);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
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
			// TODO Auto-generated method stub
			
			
		}

		@Override
		public void documentChanged(DocumentEvent event) {
			// TODO Auto-generated method stub
			System.out.println("INSERTED TEXT: " + event.fText);	// TEST
			//event.
		}
		
	}
}
