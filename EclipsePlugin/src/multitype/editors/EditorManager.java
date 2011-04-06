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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
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
	    
	    
		
		
//		Display.getDefault().asyncExec(new Runnable() {
//		    @Override
//		    public void run() {
//		    	try {
//		    		IEditorReference iEditorReference = Activator.getDefault()
//					.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//					.getEditorReferences()[0];
//		    		
//		    		ITextEditor ieditor = ((ITextEditor)iEditorReference.getEditor(true));
//		    		IDocument idoc = ieditor.getDocumentProvider().getDocument(ieditor.getEditorInput());
//		    		
//		    		for (int i = 0 ; i < 1E4 ; i++)
//					idoc.replace(0, 0, "hello\n");
//				} catch (BadLocationException e) {
//					e.printStackTrace();
//				}
//		    }
//		  });
	    
	    
//	    how to implement selection listeners:
//	    ((ITextEditor)iEditorReference.getEditor(true)).getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {
//			
//			@Override
//			public void selectionChanged(SelectionChangedEvent event) {
//				
//				System.out.println("here");
//				
//			}
//		});
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
			delete(feu.getFileId(), feu.getUserId(), feu.getStartLocation(), feu.getEndLocation(), feu);
			break;
		case Highlight:
			highlight(feu.getFileId(), feu.getUserId(), feu.getStartLocation(), feu.getEndLocation());
			break;
		case Insert:
			insert(feu.getFileId(), feu.getUserId(), feu.getStartLocation(), feu.getInsertString(), feu);
			break;
		default:
			throw new IllegalArgumentException("BAD FEU MARKUP TYPE: " + markupType);
		}
		
	}
	
	private void delete(int fileId, int userId, final int fromPos, final int toPos, final FrontEndUpdate feu)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		doc.removeDocumentListener(listener);
		    		doc.replace(fromPos, toPos - fromPos, "");
		    		Activator.getDefault().client.FEUProcessed(feu);
					doc.addDocumentListener(listener); 
					System.out.println("Editor Deletion-- fromPos: " + fromPos);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
		    }
		  });
		
	}
	
	private void insert(int fileId, int userId, final int fromPos, final String string, final FrontEndUpdate feu)
	{
    	Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		doc.removeDocumentListener(listener);
					doc.replace(fromPos, 0, string);
					Activator.getDefault().client.FEUProcessed(feu);
					doc.addDocumentListener(listener);
					System.out.println("Editor Insertion-- fromPos: " + fromPos + " string: " + string);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
		    }
		  });
	}
	
	private void highlight(int fileId, int userId, int fromPos, int toPos)
	{
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
int i = 0;
		@Override
		public void documentChanged(DocumentEvent event) {
			
//			Display.getDefault().asyncExec(new Runnable() {
//			    @Override
//			    public void run() {
//			    	try {
//			    		IEditorReference iEditorReference = Activator.getDefault()
//						.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//						.getEditorReferences()[i];
//			    		
//			    		i = i == 0 ? 1 : 0;
//			    		
//			    		ITextEditor ieditor = ((ITextEditor)iEditorReference.getEditor(true));
//			    		IDocument idoc = ieditor.getDocumentProvider().getDocument(ieditor.getEditorInput());
//			    		
//			    		idoc.removeDocumentListener(listener);
//						idoc.replace(0, 0, "hello\n");
//			    		idoc.addDocumentListener(listener);
//					} catch (BadLocationException e) {
//						e.printStackTrace();
//					}
//			    }
//			  });

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
