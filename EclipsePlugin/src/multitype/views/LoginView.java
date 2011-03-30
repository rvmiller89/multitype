package multitype.views;

import multitype.Activator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class LoginView extends TitleAreaDialog {
	private static final int BUTTON_LOGIN = IDialogConstants.CLIENT_ID + 1;
	public Text textfield_username;
	public Text textfield_host;
	public Text textfield_port;
	public Text textfield_password;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public LoginView(Shell parentShell) {
		super(parentShell);
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
		setTitleImage(ResourceManager.getPluginImage("MultiType", "res/multitype.png"));
		setTitle("");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lblUsername = new Label(container, SWT.NONE);
		lblUsername.setBounds(10, 13, 59, 14);
		lblUsername.setText("Username:");
		
		Label lblHost = new Label(container, SWT.NONE);
		lblHost.setBounds(10, 69, 59, 14);
		lblHost.setText("Host:");
		
		textfield_username = new Text(container, SWT.BORDER);
		textfield_username.setBounds(75, 10, 290, 19);
		
		textfield_host = new Text(container, SWT.BORDER);
		textfield_host.setBounds(75, 66, 290, 19);
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setBounds(10, 94, 59, 14);
		lblNewLabel.setText("Port:");
		
		textfield_port = new Text(container, SWT.BORDER);
		textfield_port.setBounds(75, 91, 145, 19);
		
		textfield_password = new Text(container, SWT.BORDER | SWT.PASSWORD);
		textfield_password.setBounds(75, 35, 290, 19);
		
		Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setText("Password:");
		lblPassword.setBounds(10, 38, 59, 14);
		
		Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setBounds(10, 60, 355, 2);

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
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
	
	public static void main (String[] args)
	{
		Display display = new Display();
		Shell shell = new Shell(display);
		LoginView login = new LoginView(shell);
		MessageDialog.openInformation(null, "Test", "Result: " + login.getReturnCode());
		
	}
}
