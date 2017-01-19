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

import controller.PartsListController;
import controller.MDIChild;
import controller.MDIParent;
import controller.MenuCommands;
import controller.WarehouseListController;
import database.GatewayException;
import models.Parts;
import models.InventoryItemList;
//import models.TransferableParts;



public class PartsListView extends MDIChild {
	/**
	 * GUI instance variables
	 */
	private JList<Parts> listPartss;
	private PartsListController myList;
	//saves reference to last selected model in JList
	//parent asks for this when opening a detail view
	private Parts selectedModel;
	private InventoryItemList myInventoryItemList;
	/**
	 * Constructor
	 * @param title Window title
	 * @param list WarehouseListController contains collection of Warehouse objects
	 * @param mdiParent MasterFrame MDI parent window reference
	 */
	public PartsListView(String title, PartsListController list, MDIParent m) {
		super(title, m);
		
		//set self to list's view (allows ListModel to tell this view to repaint when models change)
		//WarehouseListController is an observer of the models
		list.setMyListView(this);
		myInventoryItemList = m.getInventoryItemList();
		//prep list view
		myList = list;
		listPartss = new JList<Parts>(myList);
		//allow drag and drop from Parts list to Warehouse detail view Parts list
		listPartss.setDragEnabled(true);
		listPartss.setTransferHandler(new PartsDragTransferHandler());
		
		//use our custom cell renderer instead of default (don't want to use Warehouse.toString())
		listPartss.setCellRenderer(new PartsListCellRenderer());
		listPartss.setPreferredSize(new Dimension(200, 200));
		
		//add event handler for double click
		listPartss.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				//if double-click then get index and open new detail view with record at that index
		        if(evt.getClickCount() == 2) {
		        	// check User permission
		    		if( ! m.getUserSessionOption("edit") ){
		    			m.displayChildMessage("Sorry !! You don't have permisstion to Edit Part");
		    			return;
		    		}else{
		        	int index = listPartss.locationToIndex(evt.getPoint());
		        	//get the Warehouse at that index
		        	selectedModel = myList.getElementAt(index);
		        	// check lock element
		        	
		        	try {
						if(selectedModel.getGateway().blockPart( selectedModel.getId() , m.getUserSessionID())){
							m.displayChildMessage("Sorry !! This Part is blocked because another user is editing it");
							return;
						}
					} catch (GatewayException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        	//open a new detail view
		        	openDetailView();
		        }
		       }
		    }
		});
		
		//add to content pane
		this.add(new JScrollPane(listPartss));
		
		//add a Delete button to delete selected Warehouse
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton button = new JButton("Delete Parts");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteParts();
			}
		});
		panel.add(button);
		
		this.add(panel, BorderLayout.SOUTH);

		this.setPreferredSize(new Dimension(240, 200));
	}

	/**
	 * Tells MDI parent to delete the selected Parts. if none selected then ignore
	 */
	private void deleteParts() {
		//get the selected model and set as selectedWarehouse instance variable
		//mdi parent will ask for this when handling delete Warehouse call
		int idx = listPartss.getSelectedIndex();
		if(idx < 0)
			return;
		//idx COULD end up > list size so make sure idx is < list size
		if(idx >= myList.getSize())
			return;
		Parts d = myList.getElementAt(idx);
		if(d == null)
			return;
		selectedModel = d;
		
		if( myInventoryItemList.existWarehousePart( 0l, d.getId()) ){
			
			parent.displayChildMessage("Can not delete this part because it exists in Inventory table");
			return;
			
		}
		//ask user to confirm deletion
		String [] options = {"Yes", "No"};
		if(JOptionPane.showOptionDialog(myFrame
				, "Do you really want to delete " + d.getPartsName() + " ?"
				, "Confirm Deletion"
				, JOptionPane.YES_NO_OPTION
			    , JOptionPane.QUESTION_MESSAGE
			    , null
			    , options
				, options[1]) == JOptionPane.NO_OPTION) {
			return;
		}

		//tell the controller to do the deletion
		parent.doCommand(MenuCommands.DELETE_PARTS, this);
		
	}
	
	
	public void openDetailView() {
		parent.doCommand(MenuCommands.SHOW_DETAIL_PARTS, this);
	}
	
	
	public Parts getSelectedParts() {
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
	 * Accessors for PartsListController
	 * @return
	 */
	public PartsListController getMyList() {
		return myList;
	}

	public void setMyList(PartsListController myList) {
		this.myList = myList;
	}

	public JList<Parts> getListPartss() {
		return listPartss;
	}

	public void setListPartss(JList<Parts> listPeople) {
		this.listPartss = listPeople;
	}

	public Parts getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(Parts selectedModel) {
		this.selectedModel = selectedModel;
	}
	
	private class PartsDragTransferHandler extends TransferHandler {
		private int index = 0;

		public int getSourceActions(JComponent comp) {
	        return COPY_OR_MOVE;
	    }
		
		/*
		public Transferable createTransferable(JComponent comp) {
	        index = listPartss.getSelectedIndex();
	        if (index < 0 || index >= myList.getSize()) {
	            return null;
	        }
	        return new TransferableParts( (Parts) listPartss.getSelectedValue());
	    }
	    
	    public void exportDone(JComponent comp, Transferable trans, int action) {
	        if (action != MOVE) {
	            return;
	        }
	    }*/
	}
}
