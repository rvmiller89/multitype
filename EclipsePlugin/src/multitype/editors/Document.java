package multitype.editors;

import java.util.HashMap;

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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
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
	
	// Cusor marker mapping
	HashMap<String, Integer> cursorMap = new HashMap<String, Integer>();
	
	private final IDocumentListener DOCUMENT_LISTENER = new IDocumentListener() {
		
		public void documentAboutToBeChanged(DocumentEvent event) {}
		
		public void documentChanged(DocumentEvent event) 
		{
			FrontEndUpdate feu;
			
			if (event.fText.equals(""))
			{
				feu = FrontEndUpdate.createDeleteFEU(
						getFileID(), 
						Activator.getDefault().userInfo.getUserid(), 
						event.fOffset, 
						event.fLength + event.fOffset);
			}
			
			else
			{
				feu = FrontEndUpdate.createInsertFEU(
						getFileID(), 
						Activator.getDefault().userInfo.getUserid(),
						event.fOffset,
						event.fText);
			}
			
			FEUSender.send(feu);
		}
	};
	
	private final ISelectionChangedListener SELECTION_LISTENER = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) 
		{
//			FEUSender.send(
//					FrontEndUpdate.createHighlightFEU(
//							getFileID(), 
//							Activator.getDefault().userInfo.getUserid(),
//							((ITextSelection)event.getSelection()).getOffset(),
//							((ITextSelection)event.getSelection()).getLength()));
		}
	};
	
	public int getFileID()
	{
		return fileID;
	}
	
	public IEditorPart getEditor()
	{
		return editor;
	}
	
	public Document(IEditorReference ref, int fileID)
	{
		this.fileID = fileID;
		editor = (ITextEditor)ref.getEditor(true);
		doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		doc.addDocumentListener(DOCUMENT_LISTENER);
		editor.getSelectionProvider().addSelectionChangedListener(SELECTION_LISTENER);
		Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().addPostSelectionListener(new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection)
			{
				if (part.getSite().getId().equals(editor.getSite().getId()))
				{
					FEUSender.send(
							FrontEndUpdate.createCursorPosFEU(
									getFileID(), 
									Activator.getDefault().userInfo.getUserid(), 
									((ITextSelection)selection).getOffset()));
				}
			}
		});
	}
	
	public void setText(String text)
	{
		doc.removeDocumentListener(DOCUMENT_LISTENER);
		doc.set(text);
		doc.addDocumentListener(DOCUMENT_LISTENER);
	}
	
	public String getText()
	{
		return doc.get();
	}
	
	public void delete(final FrontEndUpdate feu)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		doc.removeDocumentListener(DOCUMENT_LISTENER);
		    		doc.replace(feu.getStartLocation(), feu.getEndLocation() - feu.getStartLocation(), "");
		    		Activator.getDefault().client.FEUProcessed(feu);
					doc.addDocumentListener(DOCUMENT_LISTENER); 
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
		    		doc.removeDocumentListener(DOCUMENT_LISTENER);
					doc.replace(feu.getStartLocation(), 0, feu.getInsertString());
					Activator.getDefault().client.FEUProcessed(feu);
					doc.addDocumentListener(DOCUMENT_LISTENER);
					System.out.println("Editor Insertion-- fromPos: " + feu.getStartLocation() + " string: " + feu.getInsertString());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
		    }
		  });
	}
	
	public void highlight(final FrontEndUpdate feu)
	{
		Display.getDefault().asyncExec(new Runnable() {
			public void run()
			{
				editor.getSelectionProvider().removeSelectionChangedListener(SELECTION_LISTENER);
				editor.getSelectionProvider().setSelection(new TextSelection(doc, feu.getStartLocation(), feu.getEndLocation()));
				editor.getSelectionProvider().addSelectionChangedListener(SELECTION_LISTENER);
			}
		});

	}
	
	public void cursorPos(final FrontEndUpdate feu)
	{
		System.out.println("received cursor at: " + feu.getStartLocation());
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run()
			{
				/*int offset = feu.getStartLocation();
				
				// TODO get line number from offset
				int lineNumber = 0;

				System.out.println("**** INSERTING CURSOR ON LINE " + lineNumber);
				
				MarkerUtilities.setLineNumber(cursorMap, lineNumber+1); //1-based line numbering
				MarkerUtilities.setMessage(cursorMap, "This is some sample warning.");
				cursorMap.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				try {
					IResource resource = (IResource)editor.getEditorInput().getAdapter(IResource.class);
				    MarkerUtilities.createMarker(resource, cursorMap, "myproblem");//.createMarker(f, map, "myproblem");
				} catch (CoreException e) {
				    //something went terribly wrong
					e.printStackTrace();
				}*/
			}
		});		
		
		
	}
}
