package multitype.editors;

import multitype.Activator;
import multitype.FEUSender;
import multitype.FrontEndUpdate;
import multitype.FrontEndUpdate.MarkupType;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;
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
	private volatile boolean processingFEU;
	
	public EditorManager()
	{
		editor = ActiveEditor.getEditor();
	    dp = editor.getDocumentProvider();
	    doc = dp.getDocument(editor.getEditorInput());
	    
	    processingFEU = false;
	    
	    // Add document listener
	    DocListener listener = new DocListener();
	    doc.addDocumentListener(listener);
	}
	
	public void receive(FrontEndUpdate feu)
	{
		processingFEU = true;
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
		
		processingFEU = false;
	}
	
	private void delete(int fileId, int userId, final int fromPos, final int toPos)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	//	doc.replace(fromPos, toPos - fromPos, "");  /* FOR BUILD 2... */
		    	try {
					doc.replace(fromPos, 1, "");
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
		    	//	doc.replace(fromPos, toPos - fromPos, "");  /* FOR BUILD 2... */
		    	try {
					doc.replace(fromPos, 0, string);
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
			// TODO Auto-generated method stub			
			if (!processingFEU && Activator.getDefault().isConnected)
			{
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
							event.fLength);
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
			}

			//event.
		}
		
	}
}
