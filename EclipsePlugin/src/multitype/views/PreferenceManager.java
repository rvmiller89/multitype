/**
 * @author Ryan Miller
 * @author Azfar Khandoker
 */

package multitype.views;

import java.util.ArrayList;
import java.util.List;

import multitype.Activator;
import multitype.UserInfo;

import org.eclipse.jface.preference.IPreferenceStore;


public class PreferenceManager
{
	private IPreferenceStore ps;
	private final String PROFILE_KEY = "multitype_profile";
	private final String USERNAME_KEY = "multitype_username";
	private final String SERVER_KEY = "multitype_server";
	private final String PORT_KEY = "multitype_port";
	private final String PROFILE_COUNT = "multitype_profile_count";
	private int count;
	
	public PreferenceManager()
	{
		ps = Activator.getDefault().getPreferenceStore();
		count = ps.getInt(PROFILE_COUNT);
	}

	/**
	 * Adds a profile to preference store
	 * @param username
	 * @param server
	 * @param port
	 */
	public void addProfile(String profileName, String username, String server, int port)
	{
		ps.setValue(PROFILE_KEY + count, profileName);
		ps.setValue(USERNAME_KEY + count, username);
		ps.setValue(SERVER_KEY + count, server);
		ps.setValue(PORT_KEY + count, port);

		// add one to count
		count++;
		ps.setValue(PROFILE_COUNT, count);
	}
	
	/**
	 * Adds a profile to the preference store
	 * @param profileInfo
	 */
	public void addProfile(ProfileInfo profileInfo)
	{
		addProfile(profileInfo.getProfileName(), 
				profileInfo.getUsername(), 
				profileInfo.getServer(),
				profileInfo.getPort());
	}
	
	/**
	 * Returns a list of ProfileInfo objects constructed from the preference store
	 * @return ArrayList<ProfileInfo>
	 */
	public ArrayList<ProfileInfo> getProfileList()
	{
		ArrayList<ProfileInfo> list = new ArrayList<ProfileInfo>();
		for (int i = 0; i < count; i++)
		{
			// grab each set of values and add as a ProfileInfo object to list
			ProfileInfo info = new ProfileInfo(ps.getString(PROFILE_KEY + i),
					ps.getString(USERNAME_KEY + i),
					ps.getString(SERVER_KEY + i),
					ps.getInt(PORT_KEY + i));
			
			list.add(info);
		}
		
		return list;
	}
}
