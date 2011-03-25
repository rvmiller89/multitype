package multitype;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

public class ActiveEditor {

	public static ITextEditor getEditor()
	{
		IEditorPart editor = null;
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
