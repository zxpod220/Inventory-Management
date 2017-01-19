package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.MDIChild;
import controller.MDIParent;
import database.GatewayException;
import models.Warehouse;
import models.Parts;
import models.InventoryItem;
import models.InventoryItemList;

public class InventoryItemDetailView extends MDIChild implements Observer {
	
	private InventoryItem myInventory;
	private InventoryItemList inventoryList;
	
	/**
	 * Fields for Inventory data access
	 */
	private JLabel fldId;
	private JTextField fldQuantity;
	private JComboBox comBoxWarehouseId, comBoxPartId;
		
	private HashMap<Long, String> warehouseList, partList;
	
	private MDIParent mdiparent;
	
	
	public static final String DEFAULT_VALUE = "--UnKnown--";
	/**
	 * Constructor
	 * @param title
	 */
	public InventoryItemDetailView(String title, InventoryItem inv, MDIParent p) {
		super(title, p);
		
		myInventory = inv;
		mdiparent = p;

		inventoryList = p.getInventoryItemList();
		
		//register as an observer
		myInventory.addObserver(this);
		
		//prep layout and fields
		JPanel panel = new JPanel(); 
		
		panel.setLayout(new GridLayout( 5, 2, 5, 3));
		//init fields to record data
		panel.add(new JLabel("Id"));
		fldId = new JLabel("");
		panel.add(fldId);
		
	// warehouse	
		panel.add(new JLabel("Warehouse"));
		// get warehouse list
		warehouseList = myInventory.getWarehouseList(p).getWarehouseList();
		
		
	// get size of warehouse List
		int size = warehouseList.size();
		String [] wNameList = new String[size+1];
		
		
		int i =0;
		wNameList[0] = DEFAULT_VALUE;
		
		for ( Long key : warehouseList.keySet() ) {
	 	    	wNameList[++i] = warehouseList.get(key);
	    }

	    comBoxWarehouseId = new JComboBox(wNameList);

		panel.add(comBoxWarehouseId);
		
	// for part List
		panel.add(new JLabel("Part"));	
		// get warehouse list
		partList = myInventory.getPartsList(p).getPartsList();
		
		// get size of part List
		size = partList.size();
		String [] pNameList = new String[size+1];
		
		i =0;
		pNameList[0] = DEFAULT_VALUE;
		
		for ( Long key : partList.keySet() ) {
	 	    	pNameList[++i] = partList.get(key);
	    }
		
		comBoxPartId = new JComboBox(pNameList);
	    if( partList.containsKey(myInventory.getPartId()) ){
	    	comBoxPartId.getModel().setSelectedItem(partList.get(myInventory.getPartId()));
	    }
	    
		panel.add(comBoxPartId);
	
		
		panel.add(new JLabel("Quantity"));
		fldQuantity = new JTextField("");
		fldQuantity.addKeyListener(new TextfieldChangeListener());
		panel.add(fldQuantity);
		
		this.add(panel, BorderLayout.CENTER);
		
		//add a Save button to write field changes back to model data
		panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton button = new JButton("Save Record");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveModel();
			}
		});
		panel.add(button);
		
		/*
		 * table
		 */
		
//		Object rowData[][] = { { "Row1-Column1", "Row1-Column2", "Row1-Column3" },
//			        { "Row2-Column1", "Row2-Column2", "Row2-Column3" } };
//		
//	    Object columnNames[] = { "Column One", "Column Two", "Column Three" };
//	    JTable table = new JTable(rowData, columnNames);
//		panel.add(table);
			    
		this.add(panel, BorderLayout.SOUTH);

		//load fields with model data
		refreshFields();
		
		//can't call this on JPanel
		//this.pack();
		this.setPreferredSize(new Dimension(400, 260));
	}
	
	/**
	 * Reload fields with model data
	 * Used when model notifies view of change
	 */
	public void refreshFields() {
		fldId.setText("" + myInventory.getId());
		if( myInventory.getId() >0 ){
			
			// for warehouse
			if( warehouseList.containsKey(myInventory.getWarehouseId()) ){
		    	comBoxWarehouseId.getModel().setSelectedItem(warehouseList.get(myInventory.getWarehouseId()));
		    }
			
			// for part
			if( partList.containsKey(myInventory.getPartId()) ){
		    	comBoxPartId.getModel().setSelectedItem(partList.get(myInventory.getPartId()));
		    }
		}else{
			comBoxWarehouseId.getModel().setSelectedItem(DEFAULT_VALUE);
			comBoxPartId.getModel().setSelectedItem(DEFAULT_VALUE);
		}

		fldQuantity.setText(""+myInventory.getQuantity());
		//update window title
		this.setTitle("Inventory " +myInventory.getId());
		//flag as unchanged
		setChanged(false);
	}

	/**
	 * saves changes to the view's Inventory model 
	 */
	//if any of them fail then no fields should be changed
	//and previous values reloaded
	//this is called rollback
	@Override
	public boolean saveModel() {
		//display any error message if field data are invalid
		
		if ( comBoxWarehouseId.getSelectedItem().equals(DEFAULT_VALUE) ){
			parent.displayChildMessage("Invalid Warehouse Id!");
			refreshFields();
			return false;
		}
		
		if ( comBoxPartId.getSelectedItem().equals(DEFAULT_VALUE) ){
			parent.displayChildMessage("Invalid Part Id!");
			refreshFields();
			return false;
		}
		
		Long quantity = 0L;
		try {
			quantity = Long.parseLong(fldQuantity.getText().trim());
			if(!myInventory.validQuantity(quantity)) {
				parent.displayChildMessage("Invalid Quantity!");
				refreshFields();
				return false;
			}
			
			
		} catch(Exception e) {
			parent.displayChildMessage("Invalid Quantity!");
			refreshFields();
			return false;
		}

		//fields are valid so save to model
		try {
			
			for ( Long key : warehouseList.keySet() ) {
	 	    	if( warehouseList.get(key).equals(comBoxWarehouseId.getSelectedItem()) ){
	 	    		myInventory.setWarehouseId( key );
	 	    		break;
	 	    	}
			}
			
			for ( Long key : partList.keySet() ) {
	 	    	if( partList.get(key).equals(comBoxPartId.getSelectedItem()) ){
	 	    		myInventory.setPartId( key);
	 	    		break;
	 	    	}
			}
			
			if( inventoryList.duplicate(myInventory)){
				parent.displayChildMessage("Could not have more than one record which has the same Warehouse and Part !");
				refreshFields();
				return false;
			}
	
			// check for quatity not over capacity of warehouse
			// check storage capacity of this warehouse
			
			
			Warehouse warehouse = mdiparent.getWarehouseList().findId(myInventory.getWarehouseId());
			Long totalCapacity = warehouse.getWarehouseSCap();
			Long oldQuanlity = myInventory.getQuantity(); 
			myInventory.setQuantity(quantity);
			
			if( inventoryList.remainCapacityInWarehouse( totalCapacity, myInventory) < 0 ){
				parent.displayChildMessage(" Warehouse "+ comBoxWarehouseId.getSelectedItem() +" 's Storage Capacity Left is "+ (totalCapacity-inventoryList.getTotalQuantityWarehouseExceptCurrent(myInventory)) );
				myInventory.setQuantity(oldQuanlity);
				refreshFields();
				return false;
			}
			
		} catch(Exception e) {
			parent.displayChildMessage(e.getMessage());
			refreshFields();
			return false;
		}
		
		//tell model that update is done (in case it needs to notify observers
		try {
			myInventory.finishUpdate();
			setChanged(false);
			
		} catch (GatewayException e) {
			//e.printStackTrace();
			//reset fields to db copy of inventory if save fails
			refreshFields();
			parent.displayChildMessage(e.getMessage());
			return false;
		}
		
		parent.displayChildMessage("Changes saved");
		return true;
	}

	/**
	 * Subclass-specific cleanup
	 */
	@Override
	protected void cleanup() {
		//let superclass do its thing
		super.cleanup();
				
		//unregister from observable
		myInventory.deleteObserver(this);
	}

	/**
	 * Called by Observable
	 */
	@Override
	public void update(Observable o, Object arg) {
		refreshFields();
	}

	public InventoryItem getMyInventory() {
		return myInventory;
	}

	public void setMyInventoryItem(InventoryItem myInventory) {
		this.myInventory = myInventory;
	}
	
	private class TextfieldChangeListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
			//any typing in a text field flags view as having changed
			setChanged(true);
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}

}
