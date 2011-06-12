package multitype.editors;

import multitype.Activator;
import multitype.FEUSender;
import multitype.FrontEndUpdate;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * @author Azfar Khandoker & Ryan Miller
 *
 */
public class Document 
{
	private final int fileID;
	private ITextEditor editor;
	private IDocument doc;
	private String fileName; //TODO
	public final IResource resource;

	private final IDocumentListener DOCUMENT_LISTENER = new IDocumentListener() {
		
		public void documentAboutToBeChanged(DocumentEvent event) {}
		
		public void documentChanged(DocumentEvent event) 
		{
			if (event.getLength() > 0)
			{
				FEUSender.send(
						FrontEndUpdate.createDeleteFEU(
								getFileID(), 
								Activator.getDefault().getUserInfo().getUserid(), 
								event.getOffset(), 
								event.getLength() + event.getOffset()));
			}
			
			if (!event.getText().equals(""))
			{
				FEUSender.send(
						FrontEndUpdate.createInsertFEU(
								getFileID(), 
								Activator.getDefault().getUserInfo().getUserid(), 
								event.getOffset(), 
								event.getText()));
			}
		}
	};
	
	private final ISelectionListener CURSOR_LISTENER = new ISelectionListener() {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) 
		{
			/* Cursor Positions
			 * TODO Breaks in BackendClient after closing a file...
			 */
			if (part.getSite().getId().equals(editor.getSite().getId()))
			{
				/* LOCAL DEBUG only
				FrontEndUpdate feu = FrontEndUpdate.createCursorPosFEU(getFileID(), 
						Activator.getDefault().userInfo.getUserid(), 
						((ITextSelection)selection).getOffset());
				cursorPos(feu);
				*/
				
				/* Disabled until we have a fix for BackendClient and a new resource model */
				/*FEUSender.send(
						FrontEndUpdate.createCursorPosFEU(
								getFileID(), 
								Activator.getDefault().userInfo.getUserid(), 
								((ITextSelection)selection).getOffset()));*/
			}
			
		}
		
	};
	
	private final ISelectionChangedListener SELECTION_LISTENER = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) 
		{
			/*
			 * Future implementation: Highlighting mode
		
				FEUSender.send(
					FrontEndUpdate.createHighlightFEU(
							getFileID(), 
							Activator.getDefault().userInfo.getUserid(),
							((ITextSelection)event.getSelection()).getOffset(),
							((ITextSelection)event.getSelection()).getLength()));			
			*/
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
	
	public Document(final ITextEditor editor, int fileID, String content)
	{
		this.fileID = fileID;
		this.editor = editor;
		
		// TODO client editors (currently using StringEditorInput) have no associated resource
		// and this will lead to NullPointerException's
		this.resource = (IResource)editor.getEditorInput().getAdapter(IResource.class);

		doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		
		if (content != null)
		{
			doc.set(content);
		}
		
		// Register listeners
		doc.addDocumentListener(DOCUMENT_LISTENER);
		editor.getSelectionProvider().addSelectionChangedListener(SELECTION_LISTENER);
		Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().
			getActivePage().addPostSelectionListener(CURSOR_LISTENER);
	}
	
	public void disableListeners()
	{
		doc.removeDocumentListener(DOCUMENT_LISTENER);
		editor.getSelectionProvider().removeSelectionChangedListener(SELECTION_LISTENER);
		Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().
			getActivePage().removePostSelectionListener(CURSOR_LISTENER);		
	}
	
	public String getTitle()
	{
		return editor.getTitle();
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
				// Find any previous cursor marker from this user by their userid
				String type = "multitype.cursorMarker";
				
				IMarker[] markers = null;
				
				try {
					markers = resource.findMarkers(type, true, IResource.DEPTH_INFINITE);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
				// Delete these markers
				for (IMarker m : markers)
				{
					try {
						if ((Integer)m.getAttribute("uid") == feu.getUserId())
						{
							// Match found
							m.delete();
							break;
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
				
				// Add a new marker
				
				int offset = feu.getStartLocation();
				
				int lineNumber = 0;
				try {
					lineNumber = doc.getLineOfOffset(offset);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				
				System.out.println("which is line: " + lineNumber);
				
				lineNumber +=1;	// Cursor markers use line numbers starting at 1 for some reason
				
				IMarker marker;
				try {
					marker = resource.createMarker("multitype.cursorMarker");
					String user = "Bob";
						//Activator.getDefault().connectedUsers.get(feu.getUserId());
					marker.setAttribute(IMarker.MESSAGE, user);
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
					marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
					marker.setAttribute("user", user);
					marker.setAttribute("uid", feu.getUserId());
					marker.setAttribute("cursorPos", feu.getStartLocation());
				} catch (CoreException e1) {
					e1.printStackTrace();
				}

			}
		});		
		
		
	}
}
