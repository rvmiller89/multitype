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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class ChatView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "multitype.views.ChatView";
	private Text text_sender;
	private Text text_room;

	/**
	 * The constructor.
	 */
	public ChatView() {
	}
	
	public void addMessage(final String username, final String text)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	text_room.append(username + ": " + text + "\n");
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
		
		text_room = new Text(sashForm, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		text_room.setEditable(false);
		
		text_sender = new Text(sashForm, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text_sender.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.CR)
				{
					// Add text to room
					text_room.append(Activator.getDefault().userInfo.getUsername() + 
							": " + text_sender.getText() + "\n");
					
					// Send out Chat FEU
					/*FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(NotificationType.Chat_Message, 
							fileId, 
							userId, content)*/
					
					// Remove text from box
					text_sender.setText("");
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