package multitype.editors;

import multitype.Activator;
import multitype.FEUSender;
import multitype.FrontEndUpdate;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * @author Azfar Khandoker
 *
 */
public class Document 
{
	private final int fileID;
	private ITextEditor editor;
	private IDocument doc;
	private String fileName; //TODO
	private static final IDocumentListener LISTENER = new IDocumentListener() {
		
		public void documentAboutToBeChanged(DocumentEvent event) {}
		
		public void documentChanged(DocumentEvent event) 
		{
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
		}
	};
	
	public int getFileID()
	{
		return fileID;
	}
	
	public Document(IEditorReference ref, int fileID)
	{
		this.fileID = fileID;
		editor = (ITextEditor)ref.getEditor(true);
		doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		doc.addDocumentListener(LISTENER);
		
		editor.getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) 
			{
				ITextSelection selection = (ITextSelection)event.getSelection();
				
				FEUSender.send(FrontEndUpdate.createHighlightFEU(getFileID(), 0, selection.getOffset(), selection.getLength()));
			}
		});
	}
	
	public void delete(final FrontEndUpdate feu)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		doc.removeDocumentListener(LISTENER);
		    		doc.replace(feu.getStartLocation(), feu.getEndLocation() - feu.getStartLocation(), "");
		    		Activator.getDefault().client.FEUProcessed(feu);
					doc.addDocumentListener(LISTENER); 
					System.out.println("Editor Deletion-- fromPos: " + feu.getStartLocation());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
		    }
		  });
		
	}
	
	public void insert(final FrontEndUpdate feu)
	{
    	Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		doc.removeDocumentListener(LISTENER);
					doc.replace(feu.getStartLocation(), 0, feu.getInsertString());
					Activator.getDefault().client.FEUProcessed(feu);
					doc.addDocumentListener(LISTENER);
					System.out.println("Editor Insertion-- fromPos: " + feu.getStartLocation() + " string: " + feu.getInsertString());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
		    }
		  });
	}
	
	public void highlight(final FrontEndUpdate feu)
	{
//		ISelectionProvider selectionProvider = editor.getSelectionProvider();
//	    ITextSelection selection = (ITextSelection) selectionProvider.getSelection();
//	    
//	    String text = selection.getText();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() 
			{
				editor.getSelectionProvider().setSelection(new TextSelection(doc, feu.getStartLocation(), feu.getEndLocation()));
			}
		});

	}
	
	public void cursorPos(final FrontEndUpdate feu)
	{
		// TODO: build 2
	}
}
