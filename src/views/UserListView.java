package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

import controller.UserListController;
import controller.MDIChild;
import controller.MDIParent;
import controller.MenuCommands;
import models.Users;
import models.TransferableUser;



public class UserListView extends MDIChild {
	private JList<Users> listUsers;
	private UserListController myList;
	
	//saves reference to last selected model in JList
	private Users selectedModel;
	
	/**
	 * Constructor
	 * @param title Window title
	 * @param list PersonListController contains collection of Person objects
	 * @param mdiParent MasterFrame MDI parent window reference
	 */
	public UserListView(String title, UserListController list, MDIParent m) {
		super(title, m);
		
	
		list.setMyListView(this);
		
		
		myList = list;
		listUsers = new JList<Users>(myList);
		//allow drag and drop from user list to person detail view user list
		listUsers.setDragEnabled(true);
		listUsers.setTransferHandler(new UserDragTransferHandler());
		
		//use our custom cell renderer instead of default (don't want to use Person.toString())
		listUsers.setCellRenderer(new UserListCellRenderer());
		listUsers.setPreferredSize(new Dimension(200, 200));
		
		//add event handler for double click
		listUsers.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				//if double-click then get index and open new detail view with record at that index
		        if(evt.getClickCount() == 2) {
		        	int index = listUsers.locationToIndex(evt.getPoint());
		        	//get the Person at that index
		        	selectedModel = myList.getElementAt(index);
		        	
		        	//open a new detail view
		        	openDetailView();
		        }
		    }
		});
		
		//add to content pane
		this.add(new JScrollPane(listUsers));
		
		//add a Delete button to delete selected person
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		
		JButton button = new JButton("Delete User");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteUser();
			}
		});
		panel.add(button);
		
		
		this.add(panel, BorderLayout.SOUTH);

		this.setPreferredSize(new Dimension(350, 200));
	}
	

	/**
	 * Tells MDI parent to delete the selected user. if none selected then ignore
	 */
	private void deleteUser() {
		//get the selected model and set as selectedPerson instance variable
		//mdi parent will ask for this when handling delete person call
		int idx = listUsers.getSelectedIndex();
		if(idx < 0)
			return;
		//idx COULD end up > list size so make sure idx is < list size
		if(idx >= myList.getSize())
			return;
		Users d = myList.getElementAt(idx);
		if(d == null)
			return;
		selectedModel = d;

		//ask user to confirm deletion
		String [] options = {"Yes", "No"};
		if(JOptionPane.showOptionDialog(myFrame
				, "Do you really want to delete " + d.getFullname() + " ?"
				, "Confirm Deletion"
				, JOptionPane.YES_NO_OPTION
			    , JOptionPane.QUESTION_MESSAGE
			    , null
			    , options
				, options[1]) == JOptionPane.NO_OPTION) {
			return;
		}

		//tell the controller to do the deletion
		parent.doCommand(MenuCommands.DELETE_USER, this);
		
	}
	
	/**
	 * Opens a UserDetailView with the given User object
	 */
	public void openDetailView() {
		parent.doCommand(MenuCommands.SHOW_DETAIL_USER, this);
	}
	
	/**
	 * returns selected person in list
	 * @return
	 */
	public Users getSelectedUser() {
		return selectedModel;
	}

	/**
	 * Subclass-specific cleanup
	 */
	@Override
	protected void cleanup() {
		//let superclass do its thing
		super.cleanup();
				
		//unregister from observables
		myList.unregisterAsObserver();
	}

	/**
	 * Accessors for UserListController
	 * @return
	 */
	public UserListController getMyList() {
		return myList;
	}

	public void setMyList(UserListController myList) {
		this.myList = myList;
	}

	public JList<Users> getListUsers() {
		return listUsers;
	}

	public void setListUsers(JList<Users> listPeople) {
		this.listUsers = listPeople;
	}

	public Users getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(Users selectedModel) {
		this.selectedModel = selectedModel;
	}
	
	private class UserDragTransferHandler extends TransferHandler {
		private int index = 0;

		public int getSourceActions(JComponent comp) {
	        return COPY_OR_MOVE;
	    }
				
		public Transferable createTransferable(JComponent comp) {
	        index = listUsers.getSelectedIndex();
	        if (index < 0 || index >= myList.getSize()) {
	            return null;
	        }
	        return new TransferableUser( (Users) listUsers.getSelectedValue());
	    }
	    
	    public void exportDone(JComponent comp, Transferable trans, int action) {
	        if (action != MOVE) {
	            return;
	        }
	    }
	}

}
