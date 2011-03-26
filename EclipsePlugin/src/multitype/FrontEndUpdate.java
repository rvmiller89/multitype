package multitype;
/**
 * This is used as a representation of different events that each user
 * needs to be aware of. From the plugin's perspective, it sends FEUs 
 * to the server which is forwarding them to other clients.
 * @author Rodrigo
 *
 */
import java.io.Serializable;

public class FrontEndUpdate implements Serializable {
	
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
	private int revision;
	private String content, url, insertString;
	private UpdateType updateType;
	private MarkupType markupType;
	private NotificationType notificationType;
	
	/**
	 * Creates an Insert FrontEndUpdate
	 * @param fileId
	 * @param userId
	 * @param startLocation
	 * @param insertString
	 * @return an Insert FrontEndUpdate
	 */
	public static FrontEndUpdate createInsertFEU(int fileId, int userId, 
			int startLocation, String insertString) {
		FrontEndUpdate feu = new FrontEndUpdate(UpdateType.Markup);
		feu.setMarkupType(MarkupType.Insert);
		feu.setFileId(fileId);
		feu.setUserId(userId);
		feu.setStartLocation(startLocation);
		feu.setInsertString(insertString);
		return feu;
	}
	
	/**
	 * Creates a Delete FrontEndUpdate
	 * @param fileId
	 * @param userId
	 * @param startLocation
	 * @param endLocation
	 * @return a Delete FrontEndUpdate
	 */
	public static FrontEndUpdate createDeleteFEU(int fileId, int userId, 
			int startLocation, int endLocation) {
		FrontEndUpdate feu = new FrontEndUpdate(UpdateType.Markup);
		feu.setMarkupType(MarkupType.Delete);
		feu.setFileId(fileId);
		feu.setUserId(userId);
		feu.setStartLocation(startLocation);
		feu.setEndLocation(endLocation);
		return feu;
	}
	
	/**
	 * Creates a Highlight FrontEndUpdate
	 * @param fileId
	 * @param userId
	 * @param startLocation
	 * @param endLocation
	 * @return a Highlight FrontEndUpdate
	 */
	public static FrontEndUpdate createHighlightFEU(int fileId, int userId, 
			int startLocation, int endLocation) {
		FrontEndUpdate feu = new FrontEndUpdate(UpdateType.Markup);
		feu.setMarkupType(MarkupType.Highlight);
		feu.setFileId(fileId);
		feu.setUserId(userId);
		feu.setStartLocation(startLocation);
		feu.setEndLocation(endLocation);
		return feu;
	}

	/**
	 * Creates a Cursor FrontEndUpdate
	 * @param fileId
	 * @param userId
	 * @param startLocation
	 * @return a Cursor FrontEndUpdate
	 */
	public static FrontEndUpdate createCursorPosFEU(int fileId, int userId, 
			int startLocation) {
		FrontEndUpdate feu = new FrontEndUpdate(UpdateType.Markup);
		feu.setMarkupType(MarkupType.Cursor);
		feu.setFileId(fileId);
		feu.setUserId(userId);
		feu.setStartLocation(startLocation);
		return feu;
	}
	
	/**
	 * Creates a NotificationType FrontEndUpdate
	 * @param notificationType
	 * @param fileId use -1 if not used
	 * @param userId use -1 if not used
	 * @param content use null if not used
	 * @return a NotificationType FrontEndUpdate
	 */
	public static FrontEndUpdate createNotificationFEU(
			NotificationType notificationType, int fileId, int userId, 
			String content) {
		FrontEndUpdate feu = new FrontEndUpdate(UpdateType.Notification);
		feu.setNotificationType(notificationType);
		feu.setFileId(fileId);
		feu.setUserId(userId);
		feu.setContent(content);
		return feu;
	}
	
	private FrontEndUpdate(UpdateType updateType) {
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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
	public void setRevision(int revision) {
		this.revision = revision;
	}
	public int getRevision() {
		return revision;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getInsertString() {
		return insertString;
	}
	public void setInsertString(String insertString) {
		this.insertString = insertString;
	}
	
}
