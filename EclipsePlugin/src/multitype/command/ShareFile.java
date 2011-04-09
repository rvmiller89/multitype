package multitype.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShareFile extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
		.getActiveMenuSelection(event);
		//HandlerUtil.getActiveContexts(event).getClass().toString();
		if(selection.getFirstElement() instanceof IResource) {
			IResource file = (IResource) selection.getFirstElement();
			IPath path = file.getLocation();
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
				"Information", "ShareFile detected on "
				+ path.toPortableString());
		}
		else {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
					"Information", "ShareFile detected on "
					+ selection.getFirstElement().getClass().getName());
		}
		return null;
	}
}
