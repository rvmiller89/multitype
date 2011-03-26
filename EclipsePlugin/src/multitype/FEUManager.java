package multitype;

import multitype.editors.*;
import multitype.views.*;

public class FEUManager {
	
	private EditorManager editorManager;
	private ViewManager viewManager;
	
	private static FEUManager instance;

	private FEUManager() {
		editorManager = new EditorManager();
		viewManager = new ViewManager();
	}
	
	public static FEUManager getInstance()
	{
		if (instance != null)
		{
			return instance;
		}
		
		instance = new FEUManager();
		
		return instance;
	}
	
	public void dispatchFEU(FrontEndUpdate feu)
	{
		if (feu.getUpdateType() == FrontEndUpdate.UpdateType.Markup)
			editorManager.receive(feu);
		else if (feu.getUpdateType() == FrontEndUpdate.UpdateType.Notification)
			viewManager.receive(feu);
	}
	

}
