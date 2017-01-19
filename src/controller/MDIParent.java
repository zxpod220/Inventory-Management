package controller;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import database.GatewayException;
import models.InventoryItem;
import models.InventoryItemList;
import models.Parts;
import models.PartsList;
import models.Users;
import models.UserList;
import models.Warehouse;
import views.InventoryItemDetailView;
import views.InventoryItemListView;
import views.LoginView;
import views.PartsDetailView;
import views.PartsListView;
import views.UserDetailView;
import views.UserListView;
import models.WarehouseList;
import reports.ReportExcel;
import reports.ReportException;
import reports.ReportGatewayMySQL;
import reports.ReportPDF;
import utilities.UserSession;
import views.WarehouseCompositeView;
import views.WarehouseDetailView;
import views.WarehouseListView;

/**
 * MasterFrame : a little MDI skeleton that has communication from child to JInternalFrame 
 * 					and from child to the top-level JFrame (MasterFrame)  
 *
 */
public class MDIParent extends JFrame implements WindowListener {
	private static final long serialVersionUID = 1L;
	private JDesktopPane desktop;
	private int newFrameX = 0, newFrameY = 0; //used to cascade or stagger starting x,y of JInternalFrames
	
	//models and model-controllers
	private WarehouseList WarehouseList;
	private PartsList PartsList;
	private InventoryItemList inventoryitemList;
	private UserList userList;
	
	//for the session
	private UserSession userSession = null;
	
	//keep a list of currently open views
	//useful if the MDIParent needs to act on the open views or see if an instance is already open
	private List<MDIChild> openViews;
		
	public MDIParent(String title, WarehouseList pList, PartsList dList, InventoryItemList iList,  UserList uList) {
		super(title);
		userSession = new UserSession();
		
		WarehouseList = pList;
		PartsList = dList;
		inventoryitemList = iList;
		userList = uList;
		//init the view list
		openViews = new LinkedList<MDIChild>();
		
		//create menu for adding inner frames
		MDIMenu menuBar = new MDIMenu(this);
		setJMenuBar(menuBar);
		   
		//create the MDI desktop
		desktop = new JDesktopPane();
		add(desktop);
		
		this.addWindowListener(this);

		//add shutdown hook to clean up properly even when VM quits (e.g., Command-Q)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cleanup();
			}
		});

	}
	
	/**
	 * responds to menu events or action calls from child windows (e.g., opening a detail view)
	 * @param cmd Command to perform (e.g., show detail of Warehouse object)
	 * @param caller Calling child window reference in case Command requires more info from caller (e.g., selected Warehouse)
	 */
	public void doCommand(MenuCommands cmd, Container caller) {
		switch(cmd) {
			case APP_QUIT :
				//close all child windows first
				//closeChildren();
				this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
				break;
				
			case SHOW_LIST_WAREHOUSE :
				//sync Warehouse list with db contents using id map to avoid duplicating or overwriting exist Warehouses
				//already in the list (Identity Map)
				WarehouseList.loadFromGateway();
				
				WarehouseListView v1 = new WarehouseListView("Warehouse List", new WarehouseListController(WarehouseList), this);
				//v1.setSingleOpenOnly(true);
				openMDIChild(v1);
				
				break;
			case SHOW_DETAIL_WAREHOUSE :
				Warehouse p = ((WarehouseListView) caller).getSelectedWarehouse();

				//fetch Warehouse's Parts objects using gateway
				//make sure we only use object references that are already loaded
				//p.fetchMyPartss(WarehouseList, PartsList);
				
		    	WarehouseDetailView v = new WarehouseDetailView(p.getFullName(), p, this);
				openMDIChild(v);
				break;
				
			case ADD_WAREHOUSE :
				//if there is a new unsaved Warehouse already in the list then show message and don't add a new one
				for(Warehouse pCheck : WarehouseList.getList()) {
					if(pCheck.getId() == Warehouse.INVALID_ID || pCheck.getFullName().equalsIgnoreCase(Warehouse.DEFAULT_EMPTY_FULLNAME)) {
						this.displayChildMessage("Please save changes to new Warehouse \"" + pCheck.getFullName() + "\" before trying to add another.");
						return;
					}
				}
				//make a new Warehouse instance
				Warehouse pAdd = new Warehouse();
				
				//add the new Warehouse to the model list and set its gateway
				WarehouseList.addWarehouseToList(pAdd);
				WarehouseList.addToNewRecords(pAdd);
				
				//lastly open a new Warehouse detail using the newly added Warehouse
				WarehouseDetailView vAdd = new WarehouseDetailView(pAdd.getFullName(), pAdd, this);
				openMDIChild(vAdd);
				
				break;
				
			case DELETE_WAREHOUSE :
				//remove the model from the model list
				Warehouse pDelete = ((WarehouseListView) caller).getSelectedWarehouse();
				WarehouseList.removeWarehouseFromList(pDelete);
				
				//close all details that are based on this object
				//NOTE: closing the detail view changes the openViews collection so traverse it in reverse
				for(int i = openViews.size() - 1; i >= 0; i--) {
					MDIChild c = openViews.get(i);
					if(c instanceof WarehouseDetailView) {
						//if detail view is showing the deleted object then close the detail view without asking
						if(((WarehouseDetailView) c).getMyWarehouse().getId() == pDelete.getId())
							c.closeFrame();
					}
				}
				
				//lastly, delete the Warehouse from the db
				//NOTE: this will also delete all Parts/owner relationships
				try {
					pDelete.delete();
					this.displayChildMessage("Warehouse deleted.");
				} catch (GatewayException e) {
					System.err.println(e.getMessage());
					this.displayChildMessage("Error trying to delete Warehouse.");
				}
				break;

				
			case SHOW_LIST_PARTS :
				//sync Warehouse list with db contents using id map to avoid duplicating or overwriting exist Warehouses
				//already in the list (Identity Map)
				PartsList.loadFromGateway();
				
				PartsList.getGateway().updateAccessTime(30);
				
				PartsListView dv1 = new PartsListView("Parts List", new PartsListController(PartsList), this);
				//v1.setSingleOpenOnly(true);
				openMDIChild(dv1);
				
				break;

			case SHOW_DETAIL_PARTS :
				Parts d = ((PartsListView) caller).getSelectedParts();
		    	PartsDetailView vParts = new PartsDetailView(d.getPartsName(), d, this);
				openMDIChild(vParts);
				break;

			case ADD_PARTS :
				
				// check User permission
				if( ! this.getUserSessionOption("add") ){
					this.displayChildMessage("Sorry !! You don't have permisstion to Add Part");
					return;
				}
				
				//if there is a new unsaved Warehouse already in the list then show message and don't add a new one
				for(Parts dCheck : PartsList.getList()) {
					if(dCheck.getId() == Parts.INVALID_ID || dCheck.getPartsName().equalsIgnoreCase(Parts.DEFAULT_EMPTY_PARTSNAME)) {
						this.displayChildMessage("Please save changes to new Parts \"" + dCheck.getPartsName() + "\" before trying to add another.");
						return;
					}
				}
				//make a new Parts instance
				Parts dAdd = new Parts();
				
				//add the new Parts to the model list and set its gateway
				PartsList.addPartsToList(dAdd);
				PartsList.addToNewRecords(dAdd);
				
				//lastly open a new Warehouse detail using the newly added Warehouse
				PartsDetailView vPartsAdd = new PartsDetailView(dAdd.getPartsName(), dAdd, this);
				openMDIChild(vPartsAdd);
				
				break;

			case DELETE_PARTS :
				
				// check User permission
				if( ! this.getUserSessionOption("delete") ){
					this.displayChildMessage("Sorry !! You don't have permisstion to Delete Part");
					return;
				}
				//remove the model from the model list
				Parts dDelete = ((PartsListView) caller).getSelectedParts();
				PartsList.removePartsFromList(dDelete);
				
				//close all details that are based on this object
				//NOTE: closing the detail view changes the openViews collection so traverse it in reverse
				for(int i = openViews.size() - 1; i >= 0; i--) {
					MDIChild c = openViews.get(i);
					if(c instanceof PartsDetailView) {
						//if detail view is showing the deleted object then close the detail view without asking
						if(((PartsDetailView) c).getMyParts().getId() == dDelete.getId())
							c.closeFrame();
					}
				}
				
				//lastly, delete the Parts from the db
				//NOTE: this will also delete all Parts/owner relationships
				try {
					dDelete.delete();
					this.displayChildMessage("Parts deleted.");
				} catch (GatewayException e) {
					System.err.println(e.getMessage());
					this.displayChildMessage("Error trying to delete Parts.");
				}
				break;

			case SHOW_LIST_INVENTORY :
				//sync person list with db contents using id map to avoid duplicating or overwriting exist persons
				//already in the list (Identity Map)

				inventoryitemList.loadFromGateway();
				InventoryItemListView inv1 = new InventoryItemListView("Inventory List", new InventoryItemListController(inventoryitemList), this);
				//v1.setSingleOpenOnly(true);
				openMDIChild(inv1);
				
				break;

			case SHOW_DETAIL_INVENTORY :
				InventoryItem inv = ((InventoryItemListView) caller).getSelectedInventory();
				InventoryItemDetailView vinv = new InventoryItemDetailView(""+inv.getId(), inv, this);
				openMDIChild(vinv);
				break;

			case ADD_INVENTORY :
				//if there is a new unsaved person already in the list then show message and don't add a new one
				for(InventoryItem dCheck : inventoryitemList.getList()) {
					if(dCheck.getId() == InventoryItem.INVALID_ID) {
						this.displayChildMessage("Please save changes to new Inventory \"" + dCheck.getId() + "\" before trying to add another.");
						return;
					}
				}
				//make a new Part instance
				InventoryItem iAdd = new InventoryItem();
				
				//add the new part to the model list and set its gateway
				inventoryitemList.addInventoryItemToList(iAdd);
				inventoryitemList.addToNewRecords(iAdd);
				
				//lastly open a new person detail using the newly added Person
				InventoryItemDetailView vInventoryItemAdd = new InventoryItemDetailView(""+ iAdd.getId(), iAdd, this);
				openMDIChild(vInventoryItemAdd);
				
				break;

			case DELETE_INVENTORY :
				//remove the model from the model list
				InventoryItem iDelete = ((InventoryItemListView) caller).getSelectedInventory();
				inventoryitemList.removeInventoryItemFromList(iDelete);
				
				//close all details that are based on this object
				//NOTE: closing the detail view changes the openViews collection so traverse it in reverse
				for(int i = openViews.size() - 1; i >= 0; i--) {
					MDIChild c = openViews.get(i);
					if(c instanceof InventoryItemDetailView) {
						//if detail view is showing the deleted object then close the detail view without asking
						if(((InventoryItemDetailView) c).getMyInventory().getId() == iDelete.getId())
							c.closeFrame();
					}
				}
				
			case SHOW_LOGOUT:
				
				userSession = new UserSession();
				setTitle("No user is active now");
			break;
			
		case SHOW_LOGIN :
			//sync person list with db contents using id map to avoid duplicating or overwriting exist persons
			//already in the list (Identity Map)
			
			LoginView dv2 = new LoginView("Welcome to Login page", userList.getGateway(), this);

			openMDIChild(dv2);
			
			break;	
	
	// for USER
		case SHOW_LIST_USERS :
			//sync person list with db contents using id map to avoid duplicating or overwriting exist persons
			//already in the list (Identity Map)
			userList.loadFromGateway();
			
			UserListView user1 = new UserListView("User List", new UserListController(userList), this);
			//v1.setSingleOpenOnly(true);
			openMDIChild(user1);
			
			break;

		case SHOW_DETAIL_USER :
			Users u = ((UserListView) caller).getSelectedUser();
	    	UserDetailView vUser = new UserDetailView(u.getFullname(), u, this);
			openMDIChild(vUser);
			break;

		case ADD_USER :
			//if there is a new unsaved person already in the list then show message and don't add a new one
			for(Users dCheck : userList.getList()) {
				if(dCheck.getId() == Users.INVALID_ID ) {
					this.displayChildMessage("Please save changes to new User \"" + dCheck.getFullname() + "\" before trying to add another.");
					return;
				}
			}
			//make a new User instance
			Users uAdd = new Users();
			
			//add the new user to the model list and set its gateway
			userList.addUserToList(uAdd);
			userList.addToNewRecords(uAdd);
			
			//lastly open a new person detail using the newly added Person
			UserDetailView vUserAdd = new UserDetailView(uAdd.getFullname(), uAdd, this);
			openMDIChild(vUserAdd);
			
			break;

		case DELETE_USER :
			//remove the model from the model list
			Users uDelete = ((UserListView) caller).getSelectedUser();
			userList.removeUserFromList(uDelete);
			
			//close all details that are based on this object
			//NOTE: closing the detail view changes the openViews collection so traverse it in reverse
			for(int i = openViews.size() - 1; i >= 0; i--) {
				MDIChild c = openViews.get(i);
				if(c instanceof UserDetailView) {
					//if detail view is showing the deleted object then close the detail view without asking
					if(((UserDetailView) c).getMyUser().getId() == uDelete.getId())
						c.closeFrame();
				}
			}
			
			//lastly, delete the user from the db
			//NOTE: this will also delete all user/owner relationships
			try {
				uDelete.delete();
				this.displayChildMessage("user deleted.");
			} catch (GatewayException e) {
				System.err.println(e.getMessage());
				this.displayChildMessage("Error trying to delete user.");
			}
			break;
			
		case REPORT_PDF :
			try {
				String fileName_pdf = "report.pdf";
				
				ReportPDF report = new ReportPDF(new ReportGatewayMySQL());
				report.generateReport();
				report.outputReportToFile( fileName_pdf );
				report.close();
			} catch (GatewayException | ReportException e) {
				this.displayChildMessage(e.getMessage());
				return;
			} 

		case REPORT_EXCEL :
			try {
				
				String fileName_xls = "report.xls";
				
				ReportExcel report = new ReportExcel(new ReportGatewayMySQL());
				report.generateReport();
				report.outputReportToFile( fileName_xls );
				report.close();
			} catch (GatewayException | ReportException e) {
				this.displayChildMessage(e.getMessage());
				return;
			} 

		}
	}
	
	/**
	 * This method will always be called when the app quits since it hooks into the JVM
	 * Force all MDI child frames to call cleanup methods
	 * Children are NOT allowed to abort closing here
	 * NOTE: there is NO NEED to close any MDIChildFrames here. 
	 */
	public void cleanup() {
		//System.out.println("     *** In MDIParent.cleanup");

		//iterate through all child frames
		JInternalFrame [] children = desktop.getAllFrames();
		for(int i = children.length - 1; i >= 0; i--) {
			if(children[i] instanceof MDIChildFrame) {
				MDIChildFrame cf = (MDIChildFrame) children[i];
				//tell child frame to cleanup which then tells its child view to clean up
				cf.cleanup();
			}
		}
		//close any model table gateways
		WarehouseList.getGateway().close();
		PartsList.getGateway().close();
	}

	/**
	 * create the child panel, insert it into a JInternalFrame and show it
	 * @param child
	 * @return
	 */
	public JInternalFrame openMDIChild(MDIChild child) {
		//first, if child's class is single open only and already open,
		//then restore and show that child
		//System.out.println(openViewNames.contains(child));
		if(child.isSingleOpenOnly()) {
			for(MDIChild testChild : openViews) {
				if(testChild.getClass().getSimpleName().equals(child.getClass().getSimpleName())) {
					try {
						testChild.restoreWindowState();
					} catch(PropertyVetoException e) {
						e.printStackTrace();
					}
					JInternalFrame c = (JInternalFrame) testChild.getMDIChildFrame();
					return c;
				}
			}
		}
		
		//create then new frame and set it in child
		MDIChildFrame frame = new MDIChildFrame(child.getTitle(), true, true, true, true, child);
		child.setMyFrame(frame);
				
		//pack works but the child panels need to use setPreferredSize to tell pack how much space they need
		//otherwise, MDI children default to a standard size that I find too small
		frame.pack();
		frame.setLocation(newFrameX, newFrameY);
		
		//tile its position
		newFrameX = (newFrameX + 10) % desktop.getWidth(); 
		newFrameY = (newFrameY + 10) % desktop.getHeight(); 
		desktop.add(frame);
		//show it
		frame.setVisible(true);
		
		//add child to openViews
		openViews.add(child);
				
		return frame;
	}
	
	//display a child's message in a dialog centered on MDI frame
	public void displayChildMessage(String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}
	
	/**
	 * When MDIChild closes, we need to unregister it from the list of open views
	 * @param child
	 */
	public void removeFromOpenViews(MDIChild child) {
		openViews.remove(child);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method is called when we select Quit on menu OR click the close button on the window title bar
	 * Check each MDIChild to see if its changed and we cannot close. If MDIChild says we can't close then abort close. 
	 * @param e
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		//ask each MDIChild if it is ok to close 
		//System.out.println("     *** In MDIParent.windowClosing");

		//iterate through all child frames
		JInternalFrame [] children = desktop.getAllFrames();
		for(int i = children.length - 1; i >= 0; i--) {
			if(children[i] instanceof MDIChildFrame) {
				MDIChildFrame cf = (MDIChildFrame) children[i];
				if(!cf.okToClose())
					return;
			}
		}
		
		//if we get here then ok to close MDI parent (also closes all child frames)
		this.dispose();
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
	
	public WarehouseList getWarehouseList(){
		return WarehouseList;
	}
	
	// get part list
	public PartsList getPartsList(){
		return PartsList;
	}
	
	// get inventory list
		public InventoryItemList getInventoryItemList(){
			return inventoryitemList;
		}
		public void setUserSession(Users u){
			setTitle("User Access control of " + u.getUser()+ " ADD: "+u.getAdd() + " | EDIT: "+u.getEdit() + "| DELETE: "+u.getDelete());
			this.userSession = new UserSession(u);
		}
		
		public boolean getUserSessionOption(String type){
			
			boolean value = false;
			
			type = type.toLowerCase();
			
			if( type.equals("add") ){			
				value = userSession.checkAdd();			
			}else if( type.equals("edit") ){			
						value = userSession.checkEdit();					
					} else if( type.equals("delete") ){
								value = userSession.checkDelete();
							}					
			
			return value;
		}
		public String getUserSessionID(){
			return userSession.getUser();
		}
}
