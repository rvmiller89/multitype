package multitype;

import multitype.editors.*;
import multitype.views.*;

public class FEUManager {
	
	private EditorManager editorManager;
	private ViewManager viewManager;

	public FEUManager() {
		editorManager = new EditorManager();
		viewManager = new ViewManager();
	}
	
	public void dispatchFEU(FrontEndUpdate feu)
	{
		if (feu.getUpdateType() == FrontEndUpdate.UpdateType.Markup)
			editorManager.receive(feu);
		else if (feu.getUpdateType() == FrontEndUpdate.UpdateType.Notification)
			viewManager.receive(feu);
	}
	

}
