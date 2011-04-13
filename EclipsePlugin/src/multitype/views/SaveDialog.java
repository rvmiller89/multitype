/**
 * @author Ryan Miller
 */

package multitype.views;

import multitype.Activator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.console.MessageConsole;

public class SaveDialog extends TitleAreaDialog {
	private static final int CustomYes = IDialogConstants.CLIENT_ID + 2;
	private static final int CustomNo = IDialogConstants.CLIENT_ID + 1;
	private Text txtMessage;
	
	public String filepath;
	
	private String title;
	private String message;
	private String filename;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public SaveDialog(Shell parentShell, String title, String message, String filename) {
		super(parentShell);
		this.title = title;
		this.message = message;
		setBlockOnOpen(true);
		filepath = null;
		this.filename = filename;
		open();
		// Don't dispose display (which would kill the instance of Eclipse)
	}
	
	/**
	 * Gets the filepath of the location that a user would like to save a file
	 * @return filepath if a location was selected or null if not desired to save
	 */
	public String getFilepath()
	{
		return filepath;
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
		Button button_1 = createButton(parent, CustomNo, "No", false);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// "No" clicked
				close();
			}
		});
		Button button = createButton(parent, CustomYes, "Yes",
				true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// "Yes" clicked
				
		        FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
		        dlg.setFilterNames(new String[]{"All Files (*.*)"});
		        dlg.setFilterExtensions(new String[]{"*.*"});
		        dlg.setOverwrite(true);
		        dlg.setFileName(filename);
		        String path = dlg.open();
		        //Activator.getDefault().showDialogAsync("Filepath chosen", path);
		        filepath = path;
		        if (path != null)
		        	close();
			}
		});
		button.setSelection(true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 270);
	}

}
