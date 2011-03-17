/**
 * This is used as a representation of different events that each user
 * needs to be aware of. From the plugin's perspective, it sends FEUs 
 * to the server which is forwarding them to other clients.
 * @author Rodrigo
 *
 */
public class FrontEndUpdate {
	public static enum UpdateType {
		Notification,
		Markup
	}
	public static enum MarkupType {
		Insert, 
		Delete,
		Cursor,
		Highlight
	}
	public static enum NotificationType {
		New_Connection,
		Connection_Error,
		Connection_Succeed,
		New_Shared_File,
		Close_Shared_File,
		Get_Shared_File,
		User_Connected,
		User_Disconnected,
		Request_Host,
		New_Host,
		Host_Disconnect,
		Server_Disconnect
	}
	private int startLocation, endLocation;
	private int fileId, userId;
	private String username, url, errorMessage, insert;
	private UpdateType updateType;
	private MarkupType markupType;
	private NotificationType notificationType;
	
	public FrontEndUpdate(UpdateType updateType) {
		this.updateType = updateType;
	}
	
	// Getters and Setters
	public int getStartLocation() {
		return startLocation;
	}
	public void setStartLocation(int startLocation) {
		this.startLocation = startLocation;
	}
	public int getEndLocation() {
		return endLocation;
	}
	public void setEndLocation(int endLocation) {
		this.endLocation = endLocation;
	}
	public int getFileId() {
		return fileId;
	}
	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getInsert() {
		return insert;
	}
	public void setInsert(String insert) {
		this.insert = insert;
	}

	public void setMarkupType(MarkupType markupType) {
		this.markupType = markupType;
	}

	public MarkupType getMarkupType() {
		return markupType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	// Purposely not having a setUpdateType
	public UpdateType getUpdateType() {
		return updateType;
	}
}
