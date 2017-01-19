package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.MDIChild;
import controller.MDIParent;
import database.GatewayException;
import models.Parts;
//import models.Money;
import models.Warehouse;




public class PartsDetailView extends MDIChild implements Observer {
	/**
	 * Parts object shown in view instance
	 */
	private Parts myParts;
	
	/**
	 * Fields for Parts data access
	 */
	private JLabel fldId;
	private JTextField fldPartsName, fldPnum;

	//TODO: the below fields are temporary. they will change to DDLBs
	private JTextField fldPartsUOQ, fldVendor;
	private JTextField fldVendorPnum;
		
	/**
	 * Constructor
	 * @param title
	 */
	public PartsDetailView(String title, Parts d, MDIParent m) {
		super(title, m);
		
		myParts = d;

		//register as an observer
		myParts.addObserver(this);
		
		//prep layout and fields
		JPanel panel = new JPanel(); 
		panel.setLayout(new GridLayout(6, 2));
		
		//init fields to record data
		panel.add(new JLabel("Id"));
		fldId = new JLabel("");
		panel.add(fldId);
		
		panel.add(new JLabel("Part number"));
		fldPnum = new JTextField("");
		fldPnum.addKeyListener(new TextfieldChangeListener());
		panel.add(fldPnum);
		
		panel.add(new JLabel("Part name"));
		fldPartsName = new JTextField("");
		fldPartsName.addKeyListener(new TextfieldChangeListener());
		panel.add(fldPartsName);
		
		panel.add(new JLabel("Unit of Quantity"));
		fldPartsUOQ = new JTextField("");
		fldPartsUOQ.addKeyListener(new TextfieldChangeListener());
		panel.add(fldPartsUOQ);

		panel.add(new JLabel("Vendor"));
		fldVendor = new JTextField("");
		fldVendor.addKeyListener(new TextfieldChangeListener());
		panel.add(fldVendor);

		panel.add(new JLabel("Vendor Parts Number"));
		fldVendorPnum = new JTextField("");
		fldVendorPnum.addKeyListener(new TextfieldChangeListener());
		panel.add(fldVendorPnum);

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
		
		this.add(panel, BorderLayout.SOUTH);

		//load fields with model data
		refreshFields();
		
		//can't call this on JPanel
		//this.pack();
		this.setPreferredSize(new Dimension(360, 210));
	}
	
	/**
	 * Reload fields with model data
	 * Used when model notifies view of change
	 */
	public void refreshFields() {
		fldId.setText("" + myParts.getId());
		fldPartsName.setText(myParts.getPartsName());
		fldPnum.setText("" + myParts.getPartNumber());
		fldPartsUOQ.setText("" + myParts.getUnitOQ());
		fldVendor.setText("" + myParts.getVendor());
		fldVendorPnum.setText("" + myParts.getVendorPNum());
		//update window title
		this.setTitle(myParts.getPartsName());
		//flag as unchanged
		setChanged(false);
	}

	/**
	 * saves changes to the view's Parts model 
	 */
	//if any of them fail then no fields should be changed
	//and previous values reloaded
	//this is called rollback
	@Override
	public boolean saveModel() {
		//display any error message if field data are invalid
		
		String testPNum = fldPnum.getText().trim();
		
		if(!myParts.validPartNum(testPNum)) {
			parent.displayChildMessage(Parts.ERRORMSG_INVALID_PARTNUMBER);
			//when inserting new record, don't clear the fields
			if(myParts.getId() != Parts.INVALID_ID)
				refreshFields();
			return false;
		}
		
		String testName = fldPartsName.getText().trim();
		
		if(!myParts.validPartsName(testName)) {
			parent.displayChildMessage(Parts.ERRORMSG_INVALID_PARTSNAME);
			//when inserting new record, don't clear the fields
			if(myParts.getId() != Parts.INVALID_ID)
				refreshFields();
			return false;
		}
		
		
		String testUOQ = fldPartsUOQ.getText().trim();
		
		if(!myParts.validUnitofQuantity(testUOQ)) {
			parent.displayChildMessage(Parts.ERRORMSG_INVALID_UNITOFQUANTITY);
			//when inserting new record, don't clear the fields
			if(myParts.getId() != Parts.INVALID_ID)
				refreshFields();
			return false;
		}

		String testVendor = fldVendor.getText().trim();
		
		if(!myParts.validVendor(testVendor)) {
			parent.displayChildMessage(Parts.ERRORMSG_INVALID_VENDOR);
			//when inserting new record, don't clear the fields
			if(myParts.getId() != Parts.INVALID_ID)
				refreshFields();
			return false;
		}
		
		String testVPNum = fldVendorPnum.getText().trim();
		
		if(!myParts.validVPNum(testVPNum)) {
			parent.displayChildMessage(Parts.ERRORMSG_INVALID_VENDORPARTNUMBER);
			//when inserting new record, don't clear the fields
			if(myParts.getId() != Parts.INVALID_ID)
				refreshFields();
			return false;
		}

		//fields are valid so save to model
		try {
			myParts.setPartNumber(testPNum);
			myParts.setPartsName(testName);
			myParts.setUnitOQ(testUOQ);
			myParts.setVendor(testVendor);
			myParts.setVendorPNum(testVPNum);
		} catch(Exception e) {
			parent.displayChildMessage(e.getMessage());
			refreshFields();
			return false;
		}
		
		//tell model that update is done (in case it needs to notify observers
		try {
			myParts.finishUpdate();
			setChanged(false);
			
		} catch (GatewayException e) {
			//e.printStackTrace();
			//reset fields to db copy of Parts if save fails
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
		
		try {
			myParts.getGateway().blockPart( myParts.getId() , null);
		} catch (GatewayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//unregister from observable
		myParts.deleteObserver(this);
	}

	/**
	 * Called by Observable
	 */
	@Override
	public void update(Observable o, Object arg) {
		refreshFields();
	}

	public Parts getMyParts() {
		return myParts;
	}

	public void setMyParts(Parts myParts) {
		this.myParts = myParts;
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
