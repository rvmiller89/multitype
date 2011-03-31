package multitype.views;

public class ProfileInfo {
	
	private String profileName;
	private String username;
	private String server;
	private int port;

	public ProfileInfo(String profileName, String username, String server, int port) {
		this.profileName = profileName;
		this.username = username;
		this.server = server;
		this.port = port;
	}

	public String getProfileName() {
		return profileName;
	}

	public String getUsername() {
		return username;
	}

	public String getServer() {
		return server;
	}

	public int getPort() {
		return port;
	}

}
