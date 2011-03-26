package multitype;

public class FEUListener {

	private boolean done = false;
	private BackendClient bc;
	private FEUManager manager;

	public FEUListener(BackendClient bc) {
		this.bc = bc;
		manager = new FEUManager();
	}
	
	public void start()
	{
		Thread receiveUpdateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!done) {
					bc.getUpdate();
					
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
