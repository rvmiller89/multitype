/**
 * @author Ryan Miller
 */

package multitype.views;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

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
		Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		button.setSelection(true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 270);
	}
	
	/**
	 * Center the dialog box on screen
	 */
	@Override
	protected Point getInitialLocation(Point initialSize)
	{
		Shell shell = this.getShell();
        Monitor primary = shell.getMonitor();
        Rectangle bounds = primary.getBounds ();
        int x = bounds.x + (bounds.width - initialSize.x) / 2;
        int y = bounds.y + (bounds.height - initialSize.y) / 2;
		return new Point(x, y);
	}

}
