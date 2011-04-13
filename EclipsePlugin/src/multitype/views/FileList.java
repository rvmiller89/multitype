package multitype.views;

import java.util.ArrayList;

import multitype.Activator;
import multitype.FEUManager;
import multitype.FEUSender;
import multitype.FrontEndUpdate.NotificationType;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;

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
	private TreeParent invisibleRoot;
	

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
	
	public void removeSharedFile(final int fileid)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	for (int i = 0; i < sharedFiles.children.size(); i++) {
			    	if (((TreeObject) sharedFiles.children.get(i)).getFileID() == fileid) {
			    		sharedFiles.children.remove(i);
			    		viewer.refresh(false);
			    		break;
			    	}
			    }
		    }
		  });
	}
	
	public void addOpenFile(final int fileid, final String filename)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
				TreeObject newFile = new TreeObject(filename, fileid);
				openFiles.addChild(newFile);
				viewer.refresh(false);
				viewer.expandAll();	// To open up those folders if need be
		    }
		  });
	}
	
	public void removeOpenFile(final int fileid)
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	for (int i = 0; i < openFiles.children.size(); i++) {
			    	if (((TreeObject) openFiles.children.get(i)).getFileID() == fileid) {
			    		openFiles.children.remove(i);
			    		viewer.refresh(false);
			    		break;
			    	}
			    }
		    }
		  });
	}
	
	public void hideOpenFilesList()
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	invisibleRoot.removeChild(openFiles);
		    	viewer.refresh();
		    }
		  });
	}
	
	public void showOpenFilesList()
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	invisibleRoot.addChild(openFiles);
		    	viewer.refresh();
		    }
		  });
	}
	
	/**
	 * Removes all Open Files and Shared Files from the FileList view
	 */
	public void clearList()
	{
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	openFiles.children.clear();
		    	sharedFiles.children.clear();
		    	viewer.refresh();
		    }
		  });		
	}

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
		//contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				// Listener for menu going to show... only show if selection is a descendant of
				// "Shared Files" or "Open Files"
				
				// Grab item (if there is one)
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				TreeObject item = (TreeObject)obj;
				
				if (item != null)
				{
					boolean isHost = Activator.getDefault().isHost;
					if (item.parent.getName().equals("Shared Files"))
					{
						// If host, only show delete menu (Action 2)
						if (isHost)
						{
							FileList.this.displayAction2(manager);
						}
						else // If non-host, only show open menu (Action 1)
						{
							FileList.this.displayAction1(manager);
						}
					}
					else if (item.parent.getName().equals("Open Files"))
					{
						// Only non-hosts will have this
						FileList.this.displayAction2(manager);
					}
				}
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void displayAction1(IMenuManager manager) {
		manager.add(action1);
	}
	
	private void displayAction2(IMenuManager manager) {
		manager.add(action2);
	}
	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		//manager.add(new Separator());
		//drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		//manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				
				// Grab item
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				TreeObject item = (TreeObject)obj;
				
				if (item != null)
				{
					boolean isHost = Activator.getDefault().isHost;
					// if parent.getName() is "Shared Files" and _non-host_, signal EditorManager 
					// to Get_Shared_file and add file to "Open Files" (create if needed)
					if (item.parent.getName().equals("Shared Files"))
					{
						if (!isHost)	// only non-hosts can add to Open Files
						{
							// Send Get_Shared_File
							FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(NotificationType.Get_Shared_File,
									item.getFileID(),
									Activator.getDefault().userInfo.getUserid(),
									null);
							FEUSender.send(feu);
						}
					}
				}
			}
		};
		action1.setText("Open File");
		action1.setToolTipText("Start editing a shared file");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {

				// Action 2
				
				// Grab item
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				TreeObject item = (TreeObject)obj;
				
				if (item != null)
				{
					
					boolean isHost = Activator.getDefault().isHost;

					if (item.parent.getName().equals("Open Files"))
					{
						if (!isHost)	// if non_host and selection parent is "Open Files",
						{
							// send out Close_Client_File feu to server to stop receiving updates
							FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(
									NotificationType.Close_Client_File, 
									item.getFileID(),
									Activator.getDefault().userInfo.getUserid(),
									item.getName());
							FEUSender.send(feu);
							
							// Tell editor manager to close tab with file with fileid (item.getFileid())
							FEUManager.getInstance().editorManager.removeDocument(item.getFileID());
	
							// add to Shared Files list
							Activator.getDefault().fileList.addSharedFile(item.getFileID(),
									Activator.getDefault().sharedFiles.get(item.getFileID()));
							
							// remove from Open files list
							Activator.getDefault().fileList.removeOpenFile(item.getFileID());
						}
					}
					else if (item.parent.getName().equals("Shared Files"))
					{
						if (isHost) // if host and selection parent is "Shared Files"
						{
							// Send out Close_Shared_File FEU
							FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(
									NotificationType.Close_Shared_File, 
									item.getFileID(),
									Activator.getDefault().userInfo.getUserid(),
									item.getName());
							FEUSender.send(feu);
							
							// Tell editor manager to close file
							FEUManager.getInstance().editorManager.removeDocument(item.getFileID());
							
							// Remove file mapping
							Activator.getDefault().sharedFiles.remove(item.getFileID());
							
							// Remove from shared file list
							Activator.getDefault().fileList.removeSharedFile(item.getFileID());
							
							
						}
					}
				}
				
			}
		};
		action2.setText("Close File");
		action2.setToolTipText("Stop receiving updates on a shared file");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				// Handle double-click action on a treeview item

				
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

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void run(IAction action) {
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
		
		// Capture reference to class in Activator for later use
		Activator.getDefault().fileList = this;
		
	}
}