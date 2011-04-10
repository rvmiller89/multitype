package multitype.views;

import java.util.ArrayList;

import multitype.Activator;
import multitype.FEUSender;
import multitype.FrontEndUpdate;
import multitype.FrontEndUpdate.NotificationType;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;


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

public class UserList extends ViewPart implements IWorkbenchWindowActionDelegate{

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "multitype.views.FileList";

	public TreeParent invisibleRoot;
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action hostRequest_action;
	private Action disconnect_action;
	private Action doubleClickAction;
	private IWorkbenchWindow window;
	private Button hostRequestButton;
	public int hostId = -1;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	/*TreeObject temp = new TreeObject("dummy");
	invisibleRoot.addChild(temp);
	//invisibleRoot.
	viewer.refresh(false);
	*/
	
	public class TreeObject implements IAdaptable {
		private String name;
		private int id;
		private TreeParent parent;
		
		public TreeObject(String name, int id) {
			this.name = name;
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public int getId() {
			return id;
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
			super(name, -1); //not an user
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
		//private TreeParent invisibleRoot;

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
			invisibleRoot = new TreeParent("");
			init(window);
		}
		
		
	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			ImageDescriptor descriptor = null;
			//String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			//if (obj instanceof TreeParent){
				//ImageData id = new ImageData("icon/sample.gif");
				//Activator.getImageDescriptor("icon/user.gif"); 
				
			//}
			//System.out.println("finding the host: "+((TreeObject) obj).id + " vs "+hostId);
			descriptor = Activator.getImageDescriptor("res/user.png");
			if (obj instanceof TreeObject && Activator.getDefault().userList.hostId != -1)
				if (((TreeObject) obj).id == hostId) {//Activator.getDefault().userInfo.getHost()) {
					descriptor = Activator.getImageDescriptor("res/host.png");
					//System.out.println("host found: "+((TreeObject) obj).name);
				}
			
			//Image i = new Image(Display.getDefault(),"icon/user.gif");
			
			
			//PlatformUI.getWorkbench().get
			//return i;
				//obtain the cached image corresponding to the descriptor
				   Image image = descriptor.createImage();//(Image)imageCache.get(descriptor);
				   /*if (image == null) {
				       image = descriptor.createImage();
				       imageCache.put(descriptor, image);
				   }*/
				   return image;
			   //imageKey = ISharedImages.IMG_OBJ_FOLDER;
			//imageKey = "res/user.gif";
			
			//Image i = ImageDescriptor.//PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			
			//return i;
			//return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			//PlatformUI.getWorkbench().getSharedImages().
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public UserList() {
	}

	public void addUserToList(final String name, final int id) {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	// Maintains list of users
		    	Activator.getDefault().connectedUsers.put(id, name);
		    	TreeObject temp = new TreeObject(name, id);
		    	invisibleRoot.addChild(temp);
		    	viewer.refresh();
		    }
		});
	}
	
	public void deleteUserFromList(final int id) {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	for (int i = 0; i < invisibleRoot.children.size(); i++) {
			    	if (((TreeObject) invisibleRoot.children.get(i)).id == id) {
			    		// Maintains list of users
			    		Activator.getDefault().connectedUsers.remove(id);
			    		invisibleRoot.children.remove(i);
			    		viewer.refresh();
			    		break;
			    	}
			    }
		    }
		});	
	}
	
	public void setButton(final boolean bool) {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	hostRequestButton.setEnabled(bool);
		    	hostRequest_action.setEnabled(bool);
		    	viewer.refresh();
		    }
		});	
	}
	
	public void clearList() {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	invisibleRoot.children.clear();
		    	viewer.refresh();
		    }
		});
	}
	
	private void requestToBeHost() {
		FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(
				NotificationType.Request_Host, -1, Activator.getDefault().userInfo.getUserid(), 
				Activator.getDefault().userInfo.getUsername());

		viewer.refresh();
		if (Activator.getDefault().isConnected) {
			FEUSender.send(feu);
			//hostRequestButton.setEnabled(false);
			setButton(false);
		}
	}
	
	public void disconnect() {
		if (Activator.getDefault().isConnected == true) {
			Activator.getDefault().disconnect();
			Activator.getDefault().isConnected = false;
			FrontEndUpdate feu = FrontEndUpdate.createNotificationFEU(
					NotificationType.User_Disconnected, -1, Activator.getDefault().userInfo.getUserid(), 
					Activator.getDefault().userInfo.getUsername());
			FEUSender.send(feu);
			Activator.getDefault().userList.setButton(false);
			Activator.getDefault().userList.clearList();
		}
		else {
			Activator.getDefault().showDialogAsync("Error", "Not connected to a server.");
		}
	}
	
	public void refresh() {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	viewer.refresh();
		    }
		});	
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new FormLayout());
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		Tree tree = viewer.getTree();
		FormData fd_tree = new FormData();
		fd_tree.top = new FormAttachment(0, 3);
		fd_tree.right = new FormAttachment(0, 594);
		fd_tree.left = new FormAttachment(0, 3);
		tree.setLayoutData(fd_tree);
		drillDownAdapter = new DrillDownAdapter(viewer);
		
		hostRequestButton = new Button(parent, SWT.NONE);
		hostRequestButton.setEnabled(false);
		fd_tree.bottom = new FormAttachment(100, -36);
		hostRequestButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				requestToBeHost();
			}
		});
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.bottom = new FormAttachment(100);
		fd_btnNewButton.left = new FormAttachment(tree, 0, SWT.LEFT);
		hostRequestButton.setLayoutData(fd_btnNewButton);
		hostRequestButton.setText("Request to be Host");
		
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
				UserList.this.fillContextMenu(manager);
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
		manager.add(hostRequest_action);
		manager.add(new Separator());
		manager.add(disconnect_action);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(hostRequest_action);
		manager.add(disconnect_action);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(hostRequest_action);
		manager.add(disconnect_action);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		hostRequest_action = new Action() {
			public void run() {
				requestToBeHost();
			}
		};
		hostRequest_action.setText("Request to be Host");
		hostRequest_action.setToolTipText("Request to be Host");
		hostRequest_action.setImageDescriptor(Activator.getImageDescriptor("res/host_text.png"));
		hostRequest_action.setEnabled(false);
		//action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
		//	getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		//if this can somehow call DisconnectAction it should be replaced
		disconnect_action = new Action() {
			public void run() {
				disconnect();
			}
		};
		disconnect_action.setText("Disconnect");
		disconnect_action.setToolTipText("Disconnect");
		disconnect_action.setImageDescriptor(Activator.getImageDescriptor("res/exit.png"));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
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
		
		// Capture reference to class in Activator for later use
		Activator.getDefault().userList = this;
		
	}
}