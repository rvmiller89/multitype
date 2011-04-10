/**
* @author Ryan Miller
*/

package multitype.views;

import multitype.Activator;
import multitype.FEUSender;
import multitype.FrontEndUpdate;
import multitype.FrontEndUpdate.NotificationType;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class ChatView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "multitype.views.ChatView";
	private Text text_sender;
	private StyledText text_room;

	/**
	 * The constructor.
	 */
	public ChatView() {
		Activator.getDefault().chatView = this;
	}
	
	public void enableChat()
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	text_sender.setEnabled(true);
		    }
		});
	}
	
	/**
	 * Adds a message to the MultiType chatroom
	 * @param username
	 * @param text message to be added
	 * @param isOwn whether or not you are the creator of the message
	 */
	public void addMessage(final String username, final String text, final boolean isOwn)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	int start = text_room.getText().length();
		    	int end = username.length();

		    	text_room.append(username + ": " + text);
		    	
		    	StyleRange sr1 = new StyleRange();
		        sr1.start = start;
		        sr1.length = end;
		        if (isOwn)
		        	sr1.foreground = Display.getDefault().getSystemColor(SWT.COLOR_RED);
		        sr1.fontStyle = SWT.BOLD;

		        text_room.setStyleRange(sr1);
		        text_room.setTopIndex(text_room.getLineCount());
		    }
		});
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		SashForm sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setOrientation(SWT.VERTICAL);
		
		text_room = new StyledText(sashForm, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		text_room.setBottomMargin(5);
		text_room.setTopMargin(5);
		text_room.setRightMargin(5);
		text_room.setLeftMargin(5);
		text_room.setEditable(false);
		text_room.setLineSpacing(4);
		
		text_sender = new Text(sashForm, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text_sender.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.CR)
				{
					if (Activator.getDefault().isConnected)
					{
						// Add text to room
						addMessage(Activator.getDefault().userInfo.getUsername(),
								text_sender.getText(), true);
						
						// Send out Chat FEU
						FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(NotificationType.Chat_Message, 
								-1, 
								Activator.getDefault().userInfo.getUserid(),
								text_sender.getText());
						FEUSender.send(feu);
						
						// Remove text from box
						text_sender.setText("");
					}
					else
					{
						Activator.getDefault().showDialogAsync("Chat Error", "Not connected to a server.");
					}
				}
			}
		});
		sashForm.setWeights(new int[] {419, 47});

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {

	}
}