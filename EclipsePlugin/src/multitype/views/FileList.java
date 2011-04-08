package multitype.views;

import java.util.ArrayList;

import multitype.Activator;
import multitype.FEUSender;
import multitype.FrontEndUpdate.NotificationType;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.*;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;

import multitype.editors.ActiveEditor;
import multitype.views.UserList.TreeParent;
import multitype.FrontEndUpdate;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class FileList extends ViewPart implements IWorkbenchWindowActionDelegate {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "multitype.views.FileList";
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	
	private IWorkbenchWindow window;
	
	private TreeParent openFiles;
	private TreeParent sharedFiles;
	

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class TreeObject implements IAdaptable {
		private String name;
		private int fileid;
		private TreeParent parent;
		
		public TreeObject(String name) {
			this.name = name;
		}
		public TreeObject(String name, int fileid) {
			this.name = name;
			this.fileid = fileid;
		}
		public String getName() {
			return name;
		}
		public int getFileID()
		{
			return fileid;
		}
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		public TreeParent getParent() {
			return parent;
		}
		public String toString() {
			return getName();
		}
		public Object getAdapter(Class key) {
			return null;
		}
	}
	
	class TreeParent extends TreeObject {
		private ArrayList children;
		public TreeParent(String name) {
			super(name);
			children = new ArrayList();
		}
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}
		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}
		public TreeObject [] getChildren() {
			return (TreeObject [])children.toArray(new TreeObject[children.size()]);
		}
		public boolean hasChildren() {
			return children.size()>0;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private TreeParent invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
/*
 * We will set up a dummy model to initialize tree heararchy.
 * In a real code, you will connect to a real model and
 * expose its hierarchy.
 */
		private void initialize() {
			/*TreeObject file1 = new TreeObject("File 1");
			TreeObject file2 = new TreeObject("File 2");
			TreeObject file3 = new TreeObject("File 3");*/
			
			openFiles = new TreeParent("Open Files");
			sharedFiles = new TreeParent("Shared Files");
			
			/*TreeObject to4 = new TreeObject("Leaf 4");
			TreeParent p2 = new TreeParent("Parent 2");
			p2.addChild(to4);*/
			//FrontEndUpdate fu;
			
			invisibleRoot = new TreeParent("");
			invisibleRoot.addChild(openFiles);
			invisibleRoot.addChild(sharedFiles);
			init(window);
		}
	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			if (obj instanceof TreeParent)
			{
				String imageKey = ISharedImages.IMG_OBJ_FOLDER;
				return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			}
			
			ImageDescriptor descriptor = null;
			
			descriptor = Activator.getImageDescriptor("res/file.png");
			
			Image image = descriptor.createImage();//(Image)imageCache.get(descriptor);
			return image;
			   
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public FileList() {
	}
	
	/**
	 * Adds a filename and id number to the FileList treeview under "Shared Files"
	 * @param fileid
	 * @param filename
	 */
	public void addSharedFile(final int fileid, final String filename)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
				TreeObject newFile = new TreeObject(filename, fileid);
				sharedFiles.addChild(newFile);
				viewer.refresh(false);
				viewer.expandAll();	// To open up those folders if need be
		    }
		  });
	}
	
	// TODO addOpenFile, removeSharedFile, removeOpenFile ...
	
	

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "MultiType.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				FileList.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				
				/*
				// Get a handle to the active editor (if any).
				if (window == null)
					showMessage("Window is null...");
				 IWorkbenchPage page = getSite().getPage();
				 
				
				String title = page.getActivePart().getTitle();
				
				
				showMessage(title);*/
				
				/*ViewManager vm = new ViewManager();
				FrontEndUpdate fu = 
					FrontEndUpdate.createNotificationFEU(FrontEndUpdate.NotificationType.Connection_Error, 
							0, 0, null);
					
				//	new FrontEndUpdate(FrontEndUpdate.UpdateType.Notification);
				fu.setNotificationType(FrontEndUpdate.NotificationType.Connection_Error);
				vm.receive(fu);*/
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				
				/*********************************************
				 * 
				 * Adding message to the console of everyone else....
				 * 
				 */
				
				FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(NotificationType.Console_Message, 
						0,
						Activator.getDefault().userInfo.getUserid(),
						"This is a test of the Console output capabilities.");
				FEUSender.send(feu);
				// test

			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				// Handle double-click action on a treeview item
				
				// Grab item
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				TreeObject item = (TreeObject)obj;
				
				// TODO implement these:
				
				boolean isHost = Activator.getDefault().isHost;
				// if parent.getName() is "Shared Files" and _non-host_, signal EditorManager 
				// to Get_Shared_file and add file to "Open Files" (create if needed)
				if (item.parent.getName().equals("Shared Files"))
				{
					if (!isHost)	// only non-hosts can add to Open Files
					{
						// TODO tell Azfar to Get_Shared_File
					}
				}
				
				
				
				// if parent.getName() is "Shared Files" and _host_, signal EditorManager
				// to Close_Shared_file and remove file from "Shared Files"
				
				// TODO remove file from "Shared Files" when _non_host_ and user closes editor tab
				
				showMessage("Double-click detected on " + 
						item.getName() + " with id: " + item.getFileID() + " and parent: " + item.parent.getName());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"File List",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		this.window = window;
		
		// Capture reference to class in Activator for later use
		Activator.getDefault().fileList = this;
		
	}
}