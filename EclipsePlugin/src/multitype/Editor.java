package multitype;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.*;
import org.eclipse.jface.text.*;

//import org.eclipse.ui


public class Editor {

	public Editor() {
		// TODO Auto-generated constructor stub
	}
	
	public void getOffset()
	{
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
