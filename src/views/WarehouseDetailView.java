package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

import controller.MDIChild;
import controller.MDIParent;
import controller.MenuCommands;
import database.GatewayException;
import models.Parts;
import models.Warehouse;



public class WarehouseDetailView extends MDIChild implements Observer  {
	/**
	 * Warehouse object shown in view instance
	 */
	private Warehouse myWarehouse;
	
	/**
	 * Fields for Warehouse data access
	 */
	private JLabel fldId;
	private JTextField fldWarehouseName, fldWarehouseAddress, fldWarehouseCity, fldWarehouseState, fldWarehouseZip, fldWarehouseSCap;


	/**
	 * WarehouseParts GUI components
	 */
	//private JList<WarehouseParts> listMyPartss;
	//private WarehousePartsListModel lmMyParts;
	//saves reference to last selected model in JList
	//parent asks for this when opening a detail view
	//private WarehouseParts selectedModel;
	
	/**
	 * Constructor
	 * @param title
	 */
	public WarehouseDetailView(String title, Warehouse Warehouse, MDIParent m) {
		super(title, m);
		
		myWarehouse = Warehouse;

		//register as an observer
		myWarehouse.addObserver(this);
		
		//prep layout and fields
		JPanel panel = new JPanel(); 
		panel.setLayout(new GridLayout(7, 2));
		
		//init fields to record data
		panel.add(new JLabel("Id"));
		fldId = new JLabel("");
		panel.add(fldId);
		
		panel.add(new JLabel("Warehouse Name"));
		fldWarehouseName = new JTextField("");
		//disable this field for drag and drop
		fldWarehouseName.setDropTarget(null);
		fldWarehouseName.addKeyListener(new TextfieldChangeListener());
		panel.add(fldWarehouseName);
		
		panel.add(new JLabel("Address"));
		fldWarehouseAddress = new JTextField("");
		fldWarehouseAddress.setDropTarget(null);
		fldWarehouseAddress.addKeyListener(new TextfieldChangeListener());
		panel.add(fldWarehouseAddress);
		
		panel.add(new JLabel("City"));
		fldWarehouseCity = new JTextField("");
		fldWarehouseCity.setDropTarget(null);
		fldWarehouseCity.addKeyListener(new TextfieldChangeListener());
		panel.add(fldWarehouseCity);
		
		panel.add(new JLabel("State"));
		fldWarehouseState = new JTextField("");
		fldWarehouseState.setDropTarget(null);
		fldWarehouseState.addKeyListener(new TextfieldChangeListener());
		panel.add(fldWarehouseState);
		
		panel.add(new JLabel("Zip"));
		fldWarehouseZip = new JTextField("");
		fldWarehouseZip.setDropTarget(null);
		fldWarehouseZip.addKeyListener(new TextfieldChangeListener());
		panel.add(fldWarehouseZip);

		panel.add(new JLabel("Storage Capactiy"));
		fldWarehouseSCap = new JTextField("");
		fldWarehouseSCap.setDropTarget(null);
		fldWarehouseSCap.addKeyListener(new TextfieldChangeListener());
		panel.add(fldWarehouseSCap);
		
		this.add(panel, BorderLayout.CENTER);
		
		//********************************************************
		//add Warehouse's Parts list on EAST
		//lmMyParts = new WarehousePartsListModel();
		//listMyPartss = new JList(lmMyParts);
		//set up Parts list to allow drag and dropping of Partss on it
		//listMyPartss.setDropMode(DropMode.INSERT);
		//listMyPartss.setDropTarget(listMyPartss);
		//listMyPartss.setTransferHandler(new PartsDropTransferHandler());
		
		//add event handler for double click
		//listMyPartss.addMouseListener(new MouseAdapter() {
			/*commented out for this assignment
			 * public void mouseClicked(MouseEvent evt) {
			 
				//if double-click then get index and open new detail view with record at that index
		        if(evt.getClickCount() == 2) {
		        	int index = listMyPartss.locationToIndex(evt.getPoint());
		        	//get the Warehouse at that index
		        	selectedModel = lmMyParts.getElementAt(index);
		        	
		        	//open a new detail view
		        	openWarehousePartsDetailView();
		        } else {
		        	//determine if user clicked the row's delete button
		        	Point pt = evt.getPoint();
		        	int index = listMyPartss.locationToIndex(pt);
		        	
		        	//if click after last row then don't try to process delete button
		        	Rectangle rect = listMyPartss.getCellBounds(listMyPartss.getModel().getSize() - 1, listMyPartss.getModel().getSize() - 1);
		        	double listRowMaxY = rect.getY() + rect.getHeight();
		        	if(evt.getY() > listRowMaxY)
		        		return;
		        	
		        	WarehousePartsListCellRenderer dlcr = (WarehousePartsListCellRenderer) listMyPartss.getCellRenderer();
		        	if(dlcr.mouseOnButton(evt)) {
			        	selectedModel = lmMyParts.getElementAt(index);
		        		//prompt to delete
		        		//ask user to confirm deletion
		        		String [] options = {"Yes", "No"};
		        		if(JOptionPane.showOptionDialog(myFrame
		        				, "Do you really want to delete " + selectedModel.getParts().getPartsName() + " ? Note: this does not delete the Parts, just the relationship between Warehouse and Parts."
		        				, "Confirm Deletion"
		        				, JOptionPane.YES_NO_OPTION
		        			    , JOptionPane.QUESTION_MESSAGE
		        			    , null
		        			    , options
		        				, options[1]) == JOptionPane.NO_OPTION) {
		        			return;
		        		}
		        		
		        		selectedModel.getOwner().deleteParts(selectedModel);
		        	}
		        }
		    }
		}); 

		//listMyPartss.setCellRenderer(new WarehousePartsListCellRenderer());
		
		listMyPartss.setPreferredSize(new Dimension(200, 200));
		panel = new JPanel();
		panel.add(new JScrollPane(listMyPartss));
		this.add(panel, BorderLayout.EAST);
		*/
		//********************************************************
		//add other buttons and things to SOUTH
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
		
		this.add(panel, BorderLayout.SOUTH);

		//load fields with model data
		refreshFields();
		
		//can't call this on JPanel
		//this.pack();
		this.setPreferredSize(new Dimension(500, 230));
	}
	


	/**
	 * Reload fields with model data
	 * Used when model notifies view of change
	 */
	public void refreshFields() {
		fldId.setText("" + myWarehouse.getId());
		fldWarehouseName.setText(myWarehouse.getWarehouseName());
		fldWarehouseAddress.setText(myWarehouse.getWarehouseAddress());
		fldWarehouseCity.setText(myWarehouse.getWarehouseCity());
		fldWarehouseState.setText(myWarehouse.getWarehouseState());
		fldWarehouseZip.setText(myWarehouse.getWarehouseZip());
		fldWarehouseSCap.setText("" + myWarehouse.getWarehouseSCap());
		
		this.setTitle(myWarehouse.getWarehouseName());
		setChanged(false);
	}

	/**
	 * saves changes to the view's Warehouse model 
	 */
	//if any of them fail then no fields should be changed
	//and previous values reloaded
	//this is called rollback
	@Override
	public boolean saveModel() {
		//display any error message if field data are invalid
		String testWN = fldWarehouseName.getText().trim();
		
		if(!myWarehouse.validWarehouseName(testWN)) {
			parent.displayChildMessage(Warehouse.ERRORMSG_INVALID_WAREHOUSENAME);
			//when inserting new Warehouse, don't clear the fields
			if(myWarehouse.getId() != Warehouse.INVALID_ID)
				refreshFields();
			return false;
		}
		String testADD = fldWarehouseAddress.getText().trim();
		if(!myWarehouse.validWarehouseAddress(testADD)) {
			parent.displayChildMessage(Warehouse.ERRORMSG_INVALID_ADDRESS);
			//when inserting new Warehouse, don't clear the fields
			if(myWarehouse.getId() != Warehouse.INVALID_ID)
				refreshFields();
			return false;
		}
		String testWC = fldWarehouseCity.getText().trim();
		if(!myWarehouse.validWarehouseCity(testWC)) {
			parent.displayChildMessage(Warehouse.ERRORMSG_INVALID_CITY);
			//when inserting new Warehouse, don't clear the fields
			if(myWarehouse.getId() != Warehouse.INVALID_ID)
				refreshFields();
			return false;
		}
		
		String testWS = fldWarehouseState.getText().trim();
		if(!myWarehouse.validWarehouseState(testWS)) {
			parent.displayChildMessage(Warehouse.ERRORMSG_INVALID_STATE);
			//when inserting new Warehouse, don't clear the fields
			if(myWarehouse.getId() != Warehouse.INVALID_ID)
				refreshFields();
			return false;
		}
		
		String testZP = fldWarehouseZip.getText().trim();
		if(!myWarehouse.validWarehouseZip(testZP)) {
			parent.displayChildMessage(Warehouse.ERRORMSG_INVALID_ZIP);
			//when inserting new Warehouse, don't clear the fields
			if(myWarehouse.getId() != Warehouse.INVALID_ID)
				refreshFields();
			return false;
		}
		
		int testSCap = 0;
		try {
			testSCap = Integer.parseInt(fldWarehouseSCap.getText());
		} catch(Exception e) {
			parent.displayChildMessage(Warehouse.ERRORMSG_INVALID_STORAGECAP);
			//when inserting new Warehouse, don't clear the fields
			if(myWarehouse.getId() != Warehouse.INVALID_ID)
				refreshFields();
			return false;
		}
		if(!myWarehouse.validWarehouseSCap(testSCap)) {
			parent.displayChildMessage(Warehouse.ERRORMSG_INVALID_STORAGECAP);
			//when inserting new Warehouse, don't clear the fields
			if(myWarehouse.getId() != Warehouse.INVALID_ID)
				refreshFields();
			return false;
		}

		
		//if name already exists in database and id does not match this record's
		//then name is a duplicate so display message and abort
		if(myWarehouse.WarehouseAlreadyExists(myWarehouse.getId(), testWN, testADD, testWC, testWS, testZP, testSCap)) {
			parent.displayChildMessage(Warehouse.ERRORMSG_NAME_ALREADY_EXISTS);
			return false;
		}
		
		//fields are valid so save to model
		//Note: newly added Warehouse objects will fetch a new id from the database and refresh the id field after save
		try {
			myWarehouse.setWarehouseName(testWN);
			myWarehouse.setWarehouseAddress(testADD);
			myWarehouse.setWarehouseCity(testWC);
			myWarehouse.setWarehouseState(testWS);
			myWarehouse.setWarehouseZip(testZP);
			myWarehouse.setWarehouseSCap(testSCap);
		} catch(Exception e) {
			parent.displayChildMessage(e.getMessage());
			refreshFields();
			return false;
		}
		
		//tell model that update is done (in case it needs to notify observers
		try {
			myWarehouse.finishUpdate();
			setChanged(false);
			
		} catch (GatewayException e) {
			//e.printStackTrace();
			//reset fields to db copy of Warehouse if save fails
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
		myWarehouse.deleteObserver(this);
	}

	/**
	 * Called by Observable
	 */
	@Override
	public void update(Observable o, Object arg) {
		refreshFields();
		//update Parts list
		//lmMyParts.refreshContents();
	}

	public Warehouse getMyWarehouse() {
		return myWarehouse;
	}

	public void setMyWarehouse(Warehouse myWarehouse) {
		this.myWarehouse = myWarehouse;
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
