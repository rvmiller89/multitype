/**
 * @author Ryan Miller
 */

package multitype.views;

import java.util.ArrayList;
import java.util.Iterator;

import multitype.Activator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class LoginView extends TitleAreaDialog {
	private static final int CREATE_PROFILE = IDialogConstants.CLIENT_ID + 2;
	private static final int BUTTON_LOGIN = IDialogConstants.CLIENT_ID + 1;
	private Text textfield_username;
	private Text textfield_host;
	private Text textfield_port;
	private Text textfield_password;
	private PreferenceManager prefManager;
	private ArrayList<ProfileInfo> profileList = null;
	private List listWidget = null;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public LoginView(Shell parentShell) {
		super(parentShell);
		
		prefManager = new PreferenceManager();
		
		setBlockOnOpen(true);
		open();
		// Don't dispose display (which would kill the instance of Eclipse)
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitleImage(ResourceManager.getPluginImage("MultiType", "res/multitype-med.png"));
		setTitle("Login to Server");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lblUsername = new Label(container, SWT.NONE);
		lblUsername.setBounds(159, 13, 87, 14);
		lblUsername.setText("Username:");
		
		Label lblHost = new Label(container, SWT.NONE);
		lblHost.setBounds(159, 80, 87, 14);
		lblHost.setText("Server:");
		
		textfield_username = new Text(container, SWT.BORDER);
		textfield_username.setBounds(252, 10, 290, 23);

		textfield_host = new Text(container, SWT.BORDER);
		textfield_host.setBounds(252, 77, 290, 23);
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setBounds(159, 109, 87, 14);
		lblNewLabel.setText("Port:");
		
		textfield_port = new Text(container, SWT.BORDER);
		textfield_port.setBounds(252, 106, 145, 23);
		
		textfield_password = new Text(container, SWT.BORDER | SWT.PASSWORD);
		textfield_password.setBounds(252, 39, 290, 23);
		
		Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setText("Password:");
		lblPassword.setBounds(159, 42, 87, 14);
		
		Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setBounds(159, 68, 383, 3);
		
		listWidget = new List(container, SWT.BORDER);
		
		//MessageDialog.openInformation(null, "Profile Count", "Profile count: " + prefManager.count);
		profileList = prefManager.getProfileList();
		for (ProfileInfo info : profileList)
		{
			//MessageDialog.openInformation(null, "Got profile", "Got profile: " + info.getProfileName());
			// Add each profile to the list...
			listWidget.add(info.getProfileName());
		}
		
		listWidget.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				// An item was clicked in the list
				// find the list entry with this name
				int index = listWidget.getSelectionIndex();

				if (index != -1)	// sanity check
				{
					String desiredProfile = listWidget.getItem(index);
					
					Iterator<ProfileInfo> iterator = profileList.iterator();
					
					ProfileInfo entry = null;
					while (iterator.hasNext())
					{
						ProfileInfo current = iterator.next();
						if (current.getProfileName().equals(desiredProfile))
						{
							entry = current;
							break;
						}
					}
					
					// load values from list into fields
					if (entry != null)
					{
						textfield_username.setText(entry.getUsername());
						textfield_host.setText(entry.getServer());
						textfield_port.setText(entry.getPort() + "");
					}
				}
				
			}
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				
				/*
				 * TODO BUILD 2:
				 * Here you can add code to check the formatting 
				 */
				
				
				Activator.getDefault().setupUser(textfield_username.getText(),
						textfield_password.getText(),
						textfield_host.getText(),
						Integer.parseInt(textfield_port.getText()));
										
				// Close the login dialog
				close();
				
			}
		});
		listWidget.setBounds(10, 13, 143, 145);
		container.setTabList(new Control[]{textfield_username, textfield_password, textfield_host, textfield_port});

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, BUTTON_LOGIN, "Login",
				true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				/*
				 * TODO BUILD 2:
				 * Here you can add code to check the formatting 
				 */
				
				
				Activator.getDefault().setupUser(textfield_username.getText(),
						textfield_password.getText(),
						textfield_host.getText(),
						Integer.parseInt(textfield_port.getText()));
										
				// Close the login dialog
				close();
				
			}
		});
		button.setSelection(true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		Button button_1 = createButton(parent, CREATE_PROFILE, "Create Profile", false);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// CREATE PROFILE button was clicked
				// add values to preference manager
				String profileName = "Default";
				
				InputDialog dialog = new InputDialog(null,"Add a Connection Profile...",
						"Profile Name:","",null);
				dialog.open();
				if (dialog.getReturnCode() != 1)	// Cancel button was not pressed
				{
					profileName = dialog.getValue();
					
					ProfileInfo newProfile = new ProfileInfo(profileName,
							textfield_username.getText(), 
							textfield_host.getText(), 
							Integer.parseInt(textfield_port.getText()));
					
					prefManager.addProfile(newProfile);
					
					// add to arraylist and list widget
					profileList.add(newProfile);
					listWidget.add(newProfile.getProfileName());
				}
			}
		});
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(552, 333);
	}
}
