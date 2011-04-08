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
		
		if (editor == null)
		{
			System.err.println("ERROR: WE CANNOT START THE PLUG-IN WITH NO FILE OPEN....YET");
			System.err.println("THIS MEANS THAT YOU CANNOT EDIT FOR THIS SESSION!!!");
			return;
		}
		
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
			cursorPos(feu);
			break;
		case Delete:
			delete(feu);
			break;
		case Highlight:
			highlight(feu);
			break;
		case Insert:
			insert(feu);
			break;
		default:
			throw new IllegalArgumentException("BAD FEU MARKUP TYPE: " + markupType);
		}
		
	}
	
	private void delete(final FrontEndUpdate feu)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		doc.removeDocumentListener(listener);
		    		doc.replace(feu.getStartLocation(), feu.getEndLocation() - feu.getStartLocation(), "");
		    		Activator.getDefault().client.FEUProcessed(feu);
					doc.addDocumentListener(listener); 
					System.out.println("Editor Deletion-- fromPos: " + feu.getStartLocation());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
		    }
		  });
		
	}
	
	private void insert(final FrontEndUpdate feu)
	{
    	Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		doc.removeDocumentListener(listener);
					doc.replace(feu.getStartLocation(), 0, feu.getInsertString());
					Activator.getDefault().client.FEUProcessed(feu);
					doc.addDocumentListener(listener);
					System.out.println("Editor Insertion-- fromPos: " + feu.getStartLocation() + " string: " + feu.getInsertString());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
		    }
		  });
	}
	
	private void highlight(final FrontEndUpdate feu)
	{
//		ISelectionProvider selectionProvider = editor.getSelectionProvider();
//	    ITextSelection selection = (ITextSelection) selectionProvider.getSelection();
//	    
//	    String text = selection.getText();
	}
	
	private void cursorPos(final FrontEndUpdate feu)
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
//						.getEditorReferences()[i = i == 0 ? 1 : 0];
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
			
			for (int j = 0 ; j < Activator.getDefault()
			.getWorkbench().getActiveWorkbenchWindow().getActivePage()
			.getEditorReferences().length ; j++)
			System.err.println(Activator.getDefault()
			.getWorkbench().getActiveWorkbenchWindow().getActivePage()
			.getEditorReferences()[j].getName() + " " + i++);

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
