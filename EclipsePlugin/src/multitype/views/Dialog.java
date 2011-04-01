/**
 * @author Ryan Miller
 */

package multitype.views;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.Text;

public class Dialog extends TitleAreaDialog {
	private Text txtMessage;
	
	private String title;
	private String message;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public Dialog(Shell parentShell, String title, String message) {
		super(parentShell);
		this.title = title;
		this.message = message;
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
		setTitle(title);
		setTitleImage(ResourceManager.getPluginImage("MultiType", "res/multitype-med.png"));
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		txtMessage = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		txtMessage.setEditable(false);
		txtMessage.setText(message);
		txtMessage.setBounds(10, 10, 430, 90);

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 270);
	}

}
