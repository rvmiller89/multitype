package multitype.views;

import multitype.Activator;

import org.eclipse.jface.preference.IPreferenceStore;


public class PreferenceManager
{
	private IPreferenceStore ps;
	private final String PREVIOUS_USERNAME_KEY = "multitype_previous_username";
	private final String PREVIOUS_HOST_KEY = "multitype_pervious_host";
	private final String PREVIOUS_PORT_KEY = "multitype_previous_port";
	
	public PreferenceManager()
	{
		ps = Activator.getDefault().getPreferenceStore();
	}

	public String getPrevUsername()
	{
		return ps.getString(PREVIOUS_USERNAME_KEY);
	}
	
	public String getPrevHost()
	{
		return ps.getString(PREVIOUS_HOST_KEY);
	}
	
	public int getPrevPort()
	{
		return ps.getInt(PREVIOUS_PORT_KEY);
	}
	
	/**
	 * 
	 * @param username
	 * @param host
	 * @param port
	 */
	public void setPrevLoginSettings(String username, String host, int port)
	{
		ps.setValue(PREVIOUS_USERNAME_KEY, username);
		ps.setValue(PREVIOUS_HOST_KEY, host);
		ps.setValue(PREVIOUS_PORT_KEY, port);
	}
}
