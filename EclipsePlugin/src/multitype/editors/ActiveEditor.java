/**
 * @author Ryan Miller
 */

package multitype.editors;

import multitype.Activator;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

public class ActiveEditor {

	// Prevent creation of instances of ActiveEditor
	private ActiveEditor(){}
	
	private static IEditorPart editor = null;

	public static final ITextEditor getEditor()
	{
		if (editor != null)
		{
			return (ITextEditor) editor;
		}
		
		IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
		    IWorkbenchPage page = window.getActivePage();
		    if (page != null) {
		        editor = page.getActiveEditor();
		    }
		}
		if (editor == null)
			return null;

		if (editor instanceof ITextEditor) {
			return (ITextEditor)editor;
		}
			
		return null;
	}
}
