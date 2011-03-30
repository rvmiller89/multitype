package multitype.views;

import multitype.Activator;

import org.eclipse.jface.dialogs.IDialogConstants;
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

public class LoginView extends TitleAreaDialog {
	private static final int BUTTON_LOGIN = IDialogConstants.CLIENT_ID + 1;
	private Text textfield_username;
	private Text textfield_host;
	private Text textfield_port;
	private Text textfield_password;
	private PreferenceManager prefManager;

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
		lblUsername.setBounds(10, 13, 87, 14);
		lblUsername.setText("Username:");
		
		Label lblHost = new Label(container, SWT.NONE);
		lblHost.setBounds(10, 80, 87, 14);
		lblHost.setText("Server:");
		
		textfield_username = new Text(container, SWT.BORDER);
		textfield_username.setBounds(103, 10, 290, 23);
		textfield_username.setText(prefManager.getPrevUsername());
		
		textfield_host = new Text(container, SWT.BORDER);
		textfield_host.setBounds(103, 77, 290, 23);
		textfield_host.setText(prefManager.getPrevHost());
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setBounds(10, 109, 87, 14);
		lblNewLabel.setText("Port:");
		
		textfield_port = new Text(container, SWT.BORDER);
		textfield_port.setBounds(103, 106, 145, 23);
		textfield_port.setText(prefManager.getPrevPort() + "");
		
		textfield_password = new Text(container, SWT.BORDER | SWT.PASSWORD);
		textfield_password.setBounds(103, 39, 290, 23);
		
		Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setText("Password:");
		lblPassword.setBounds(10, 42, 87, 14);
		
		Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setBounds(10, 68, 383, 3);
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
				
				prefManager.setPrevLoginSettings(textfield_username.getText(), textfield_host.getText(), Integer.parseInt(textfield_port.getText()));
						
				// Close the login dialog
				close();
				
			}
		});
		button.setSelection(true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
	
	/*public static void main (String[] args)
	{
		Display display = new Display();
		Shell shell = new Shell(display);
		LoginView login = new LoginView(shell);
		MessageDialog.openInformation(null, "Test", "Result: " + login.getReturnCode());
		
	}*/
}
