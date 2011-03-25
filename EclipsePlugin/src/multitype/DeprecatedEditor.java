
/*****************************************************
 * 
 * 
 * DEPRECATED:
 * 
 * This file is no longer being used.
 * 
 * See ActiveEditor.java
 * 
 * 
 * 
 * 
 * 
 * 
 
 * 
 * 
 * @author rvmiller89
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */







//import org.eclipse.ui
package multitype;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;


public class DeprecatedEditor implements IWorkbenchWindowActionDelegate{
	
	private IWorkbenchWindow window;

	public DeprecatedEditor() {
		// TODO Auto-generated constructor stub
		//initWorkbench();
	}
	
	public int initWorkbench()
	{
		WorkbenchAdvisor workbenchAdvisor = new MyWorkbenchAdvisor();
		Display display = PlatformUI.createDisplay();
		
		try {
		  int returnCode = PlatformUI.createAndRunWorkbench(display, workbenchAdvisor);
		  if (returnCode == PlatformUI.RETURN_RESTART)
		    return IPlatformRunnable.EXIT_RESTART;
		  else
		    return IPlatformRunnable.EXIT_OK;
		  }
		  finally {
		    //display.dispose();
		  }
	}
	
	public String printText()
	{
		// Get a handle to the active editor (if any).
		
		/*IEditorPart editorPart = window.getActivePage().getActiveEditor();
		ITextEditor textEditor = testEditorPart(editorPart);
		if (textEditor == null) return "";*/
		IEditorPart editorPart =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ITextEditor textEditor = testEditorPart(editorPart);

		// Get the selected text.
		IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
		ISelectionProvider selectionProvider = textEditor.getSelectionProvider();
	    ITextSelection selection = (ITextSelection) selectionProvider.getSelection();
	    String text = selection.getText();
	    return text;
		
	}
	
	public void getOffset()
	{
		
		
		//PlatformUI.
		IEditorPart editor =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		if (editor instanceof ITextEditor) {
		  ISelectionProvider selectionProvider = ((ITextEditor)editor).getSelectionProvider();
		  ISelection selection = selectionProvider.getSelection();
		  if (selection instanceof ITextSelection) {
		    ITextSelection textSelection = (ITextSelection)selection;
		    int offset = textSelection.getOffset(); // etc.
		    System.out.println("OFFSET: " + offset);
		  }
		}
	}
	
	public String getEditor()
	{
		IEditorPart editor = null;
		String result = null;
		IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
		    IWorkbenchPage page = window.getActivePage();
		    if (page != null) {
		        editor = page.getActiveEditor();
		    }
		}
		if (editor == null)
			return "NULL";

		if (editor instanceof ITextEditor) {
			  ISelectionProvider selectionProvider = ((ITextEditor)editor).getSelectionProvider();
			  ISelection selection = selectionProvider.getSelection();
			  if (selection instanceof ITextSelection) {
			    ITextSelection textSelection = (ITextSelection)selection;
			    //int offset = textSelection.getOffset(); // etc.
			    //System.out.println("OFFSET: " + offset);
			    return textSelection.getText();
			  }
		}
		return "GOOD";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello world");
		
		DeprecatedEditor e = new DeprecatedEditor();
		//e.getOffset();
		
		//e.getEditor();
		System.out.println(PlatformUI.isWorkbenchRunning());
		
		//e.printText();
		
	}
	
	/**
	 * 
	 * @param editorPart
	 * @return
	 */
	private ITextEditor testEditorPart(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor) {
			return (ITextEditor) editorPart;
		} else {
			return null;
		}
	}

	private class MyWorkbenchAdvisor extends WorkbenchAdvisor
	{
		public MyWorkbenchAdvisor()
		{
			super();
		}

		@Override
		public String getInitialWindowPerspectiveId() {
			// TODO Auto-generated method stub
			return "Java";
		}
		
	}

	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		this.window = window;
	}
}
