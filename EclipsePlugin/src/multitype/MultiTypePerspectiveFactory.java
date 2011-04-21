package multitype;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class MultiTypePerspectiveFactory implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		
		/*
		 * [Project Explorer]		[Editor View]		[File List]
		 * 
		 * [MT ChatView]								[User List]
		 * 
		 * 							[Console]
		 */
		

		/*
		 * 
		 * Custom code to configure the MultiType perspective goes here.
		 * Otherwise, it is handled by org.eclipse.ui.perspectiveExtensions
		 * 
		 * 
		 */
	}

}
