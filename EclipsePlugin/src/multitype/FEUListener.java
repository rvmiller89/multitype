/**
 * @author Ryan Miller
 */

package multitype;

import multitype.FrontEndUpdate.NotificationType;

public class FEUListener {

	private boolean done = false;
	private BackendClient bc;
	private FEUManager manager;

	public FEUListener(BackendClient bc) {
		this.bc = bc;
		manager = FEUManager.getInstance();
	}
	
	public void start()
	{
		Thread receiveUpdateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!done) {
					// Sends feu to FEUManager to dispatch 
					// to EditorManager or ViewManager
	
					FrontEndUpdate fu = bc.getUpdate();
					
					if (!(fu.getUserId() == Activator.getDefault().userInfo.getUserid()))
						manager.dispatchFEU(fu);
					else if (fu.getUpdateType() == FrontEndUpdate.UpdateType.Notification)
					{
						if (fu.getNotificationType() == NotificationType.Get_Shared_File
								|| fu.getNotificationType() == NotificationType.New_Host)
						{
							// Special cases, send FEU even though userid is your own
							manager.dispatchFEU(fu);
						}
					}
					
				}
			}			
		});
		receiveUpdateThread.start();
	}
	
	/**
	 * needs to be called in between sessions. We spun off thread, we need to 
	 * kill them
	 */
	public void finish() {
		done = true;
	}

}
