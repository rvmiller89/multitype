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

	private final String PROFILE = "multitype_profile";
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
	private void addProfile(String profileName, String username, String server, int port)
	{
		ps.setValue(count + PROFILE, profileName + "," + username + "," + server + "," + port);
	}
	
	/**
	 * Adds a profile to the preference store
	 * @param profileInfo
	 */
	private void addProfile(ProfileInfo profileInfo)
	{
		addProfile(profileInfo.getProfileName(), 
				profileInfo.getUsername(), 
				profileInfo.getServer(),
				profileInfo.getPort());
	}
	
	/**
	 * Refreshes profiles in preference store with new list
	 * @param list
	 */
	public void updateProfiles(List<ProfileInfo> list)
	{
		removeProfiles();
		
		for (ProfileInfo info : list)
		{
			addProfile(info);
			
			// add one to count
			count++;
		}
		ps.setValue(PROFILE_COUNT, count);
	}
	
	/**
	 * Removes all profiles from the preference store
	 */
	private void removeProfiles()
	{
		for (int i = 0; i < count; i++)
		{
			ps.setValue(i + PROFILE, "");
		}
		
		count = 0;
		ps.setValue(PROFILE_COUNT, count);
		
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
			String str = ps.getString(i + PROFILE);
			
			String[] values = str.split(",");
			
			if (values.length == 4)	// Should be 4 strings representing values
			{
				ProfileInfo info = new ProfileInfo(values[0],
					values[1],
					values[2],
					Integer.parseInt(values[3]));
			
				list.add(info);
			}
		}
		
		return list;
	}
}
