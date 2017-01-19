package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import controller.MDIChild;
import controller.MDIParent;
import controller.MenuCommands;
import controller.WarehouseListController;
import models.InventoryItemList;
import models.Warehouse;

public class WarehouseListView extends MDIChild {

	private JList<Warehouse> listWarehouse;
	private WarehouseListController myList;
	//saves reference to last selected model in JList
	//parent asks for this when opening a detail view
	private Warehouse selectedModel;
	private InventoryItemList myInventoryItemList;
	/**
	 * Constructor
	 * @param title Window title
	 * @param list WarehouseListController contains collection of Warehouse objects
	 * @param mdiParent MasterFrame MDI parent window reference
	 */
	public WarehouseListView(String title, WarehouseListController list, MDIParent m) {
		super(title, m);
		
		//set self to list's view (allows ListModel to tell this view to repaint when models change)
		//WarehouseListController is an observer of the models
		list.setMyListView(this);
		
		myInventoryItemList = m.getInventoryItemList();
		//prep list view
		myList = list;
		listWarehouse = new JList<Warehouse>(myList);
		//use our custom cell renderer instead of default (don't want to use Warehouse.toString())
		listWarehouse.setCellRenderer(new WarehouseListCellRenderer());
		listWarehouse.setPreferredSize(new Dimension(200, 200));
		
		//add event handler for double click
		listWarehouse.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				//if double-click then get index and open new detail view with record at that index
		        if(evt.getClickCount() == 2) {
		        	int index = listWarehouse.locationToIndex(evt.getPoint());
		        	//get the Warehouse at that index
		        	selectedModel = myList.getElementAt(index);
		        	
		        	//open a new detail view
		        	openDetailView();
		        }
		    }
		});
		
		//add to content pane
		this.add(new JScrollPane(listWarehouse));
		
		//add a Delete button to delete selected Warehouse
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton button = new JButton("Delete Warehouse");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteWarehouse();
			}
		});
		panel.add(button);
		
		this.add(panel, BorderLayout.SOUTH);

		this.setPreferredSize(new Dimension(240, 200));
	}

	/**
	 * Tells MDI parent to delete the selected Warehouse. if none selected then ignore
	 */
	private void deleteWarehouse() {
		//get the selected model and set as selectedWarehouse instance variable
		//mdi parent will ask for this when handling delete Warehouse call
		int idx = listWarehouse.getSelectedIndex();
		if(idx < 0)
			return;
		//idx COULD end up > list size so make sure idx is < list size
		if(idx >= myList.getSize())
			return;
		Warehouse p = myList.getElementAt(idx);
		if(p == null)
			return;
		selectedModel = p;
		if( myInventoryItemList.existWarehousePart( p.getId(), 0l) ){
			
			parent.displayChildMessage("Can not delete this warehouse because it exists in Inventory table");
			return;
			
		}
		
		//ask user to confirm deletion
		String [] options = {"Yes", "No"};
		if(JOptionPane.showOptionDialog(myFrame
				, "Do you really want to delete " + p.getFullName() + " ?"
				, "Confirm Deletion"
				, JOptionPane.YES_NO_OPTION
			    , JOptionPane.QUESTION_MESSAGE
			    , null
			    , options
				, options[1]) == JOptionPane.NO_OPTION) {
			return;
		}

		//tell the controller to do the deletion
		parent.doCommand(MenuCommands.DELETE_WAREHOUSE, this);
		
	}
	
	/**
	 * Opens a WarehouseDetailView with the given Warehouse object
	 */
	public void openDetailView() {
		parent.doCommand(MenuCommands.SHOW_DETAIL_WAREHOUSE, this);
	}
	
	/**
	 * returns selected Warehouse in list
	 * @return
	 */
	public Warehouse getSelectedWarehouse() {
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
	 * Accessors for WarehouseListController
	 * @return
	 */
	public WarehouseListController getMyList() {
		return myList;
	}

	public void setMyList(WarehouseListController myList) {
		this.myList = myList;
	}

	public JList<Warehouse> getlistWarehouse() {
		return listWarehouse;
	}

	public void setlistWarehouse(JList<Warehouse> listWarehouse) {
		this.listWarehouse = listWarehouse;
	}

	public Warehouse getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(Warehouse selectedModel) {
		this.selectedModel = selectedModel;
	}
}
