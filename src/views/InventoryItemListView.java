package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import controller.InventoryItemListController;
import controller.MDIChild;
import controller.MDIParent;
import controller.MenuCommands;
import models.InventoryItem;
import models.TransferableInventoryItem;

public class InventoryItemListView extends MDIChild {

	private JList<InventoryItem> listInventoryItems;
	private InventoryItemListController myList;
	//saves reference to last selected model in JList
	//parent asks for this when opening a detail view
	private InventoryItem selectedModel;
	
	/**
	 * Constructor
	 * @param title Window title
	 * @param list PersonListController contains collection of Person objects
	 * @param mdiParent MasterFrame MDI parent window reference
	 */
	public InventoryItemListView(String title, InventoryItemListController list, MDIParent p) {
		super(title, p);
		
		//set self to list's view (allows ListModel to tell this view to repaint when models change)
		//PersonListController is an observer of the models
		list.setMyListView(this);
		
		//prep list view
		myList = list;
		listInventoryItems = new JList<InventoryItem>(myList);
		//allow drag and drop from inventoy list to person detail view inventoy list
		listInventoryItems.setDragEnabled(true);
		listInventoryItems.setTransferHandler(new InventoryDragTransferHandler());
		
		//use our custom cell renderer instead of default (don't want to use Person.toString())
		listInventoryItems.setCellRenderer(new InventoryItemListCellRenderer());
		listInventoryItems.setPreferredSize(new Dimension(200, 200));
		
		//add event handler for double click
		listInventoryItems.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				//if double-click then get index and open new detail view with record at that index
		        if(evt.getClickCount() == 2) {
		        	int index = listInventoryItems.locationToIndex(evt.getPoint());
		        	//get the Person at that index
		        	selectedModel = myList.getElementAt(index);
		        	
		        	//open a new detail view
		        	openDetailView();
		        }
		    }
		});
		
		//add to content pane
		this.add(new JScrollPane(listInventoryItems));
		
		//add a Delete button to delete selected person
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton button = new JButton("Delete Inventory");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteInventoryItem();
			}
		});
		panel.add(button);
		
		this.add(panel, BorderLayout.SOUTH);

		this.setPreferredSize(new Dimension(240, 200));
	}

	/**
	 * Tells MDI parent to delete the selected inventoy. if none selected then ignore
	 */
	private void deleteInventoryItem() {
		//get the selected model and set as selectedPerson instance variable
		//mdi parent will ask for this when handling delete person call
		int idx = listInventoryItems.getSelectedIndex();
		if(idx < 0)
			return;
		//idx COULD end up > list size so make sure idx is < list size
		if(idx >= myList.getSize())
			return;
		InventoryItem it = myList.getElementAt(idx);
		if(it == null)
			return;
		selectedModel = it;
		
		//ask user to confirm deletion
		String [] options = {"Yes", "No"};
		if(JOptionPane.showOptionDialog(myFrame
				, "Do you really want to delete " + it.getId() + " ?"
				, "Confirm Deletion"
				, JOptionPane.YES_NO_OPTION
			    , JOptionPane.QUESTION_MESSAGE
			    , null
			    , options
				, options[1]) == JOptionPane.NO_OPTION) {
			return;
		}

		//tell the controller to do the deletion
		parent.doCommand(MenuCommands.DELETE_INVENTORY, this);
		
	}
	
	/**
	 * Opens a InventoryDetailView with the given Inventory object
	 */
	public void openDetailView() {
		parent.doCommand(MenuCommands.SHOW_DETAIL_INVENTORY, this);
	}
	
	/**
	 * returns selected person in list
	 * @return
	 */
	public InventoryItem getSelectedInventory() {
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
	 * Accessors for InventoryListController
	 * @return
	 */
	public InventoryItemListController getMyList() {
		return myList;
	}

	public void setMyList(InventoryItemListController myList) {
		this.myList = myList;
	}

	public JList<InventoryItem> getListInventorys() {
		return listInventoryItems;
	}

	public void setListInventorys(JList<InventoryItem> listWarehouse) {
		this.listInventoryItems = listWarehouse;
	}

	public InventoryItem getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(InventoryItem selectedModel) {
		this.selectedModel = selectedModel;
	}
	
	private class InventoryDragTransferHandler extends TransferHandler {
		private int index = 0;

		public int getSourceActions(JComponent comp) {
	        return COPY_OR_MOVE;
	    }
				
		public Transferable createTransferable(JComponent comp) {
	        index = listInventoryItems.getSelectedIndex();
	        if (index < 0 || index >= myList.getSize()) {
	            return null;
	        }
	        return new TransferableInventoryItem( (InventoryItem) listInventoryItems.getSelectedValue());
	    }
	    
	    public void exportDone(JComponent comp, Transferable trans, int action) {
	        if (action != MOVE) {
	            return;
	        }
	    }
	}
}
