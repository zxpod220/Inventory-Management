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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.MDIChild;
import controller.MDIParent;
import database.GatewayException;
import models.Users;
import utilities.Utility;


public class UserDetailView extends MDIChild implements Observer {
	/**
	 * User object shown in view instance
	 */
	private Users myUser;
	
	/**
	 * Fields for User data access
	 */
	private JLabel fldId;
	private JTextField fldUserName, fldPassword, fldFullName;
	private JTextField fldAdd, fldEdit, fldDelete;
		
	/**
	 * Constructor
	 * @param title
	 */
	public UserDetailView(String title, Users d, MDIParent m) {
		super(title, m);
		
		myUser = d;

		//register as an observer
		myUser.addObserver(this);
		
		//prep layout and fields
		JPanel panel = new JPanel(); 
		panel.setLayout(new GridLayout(12, 2, 5, 3));
		
		//init fields to record data
		panel.add(new JLabel("Id"));
		fldId = new JLabel("");
		panel.add(fldId);
		
		
		panel.add(new JLabel("UserName"));
		fldUserName = new JTextField("");
		fldUserName.addKeyListener(new TextfieldChangeListener());
		panel.add(fldUserName);
		
		panel.add(new JLabel("Old Password"));
		
		if( myUser != null && myUser.getPassword()!= null && myUser.getPassword().length() > 0){
			if(myUser.getPassword().length()> 15)
				panel.add(new JLabel(myUser.getPassword().substring(0, 15)+"..."));
			else
				panel.add(new JLabel(myUser.getPassword()));
		}else
			panel.add(new JLabel("..."));
			
		panel.add(new JLabel("New Password"));
		fldPassword = new JTextField("");
//		fldPassword.addKeyListener(new TextfieldChangeListener());
		panel.add(fldPassword);
		
		panel.add(new JLabel("Full Name"));
		fldFullName = new JTextField("");
		fldFullName.addKeyListener(new TextfieldChangeListener());
		panel.add(fldFullName);
		
		panel.add(new JLabel("Can Add (Yes/No)"));
		fldAdd = new JTextField("");
		fldAdd.addKeyListener(new TextfieldChangeListener());
		panel.add(fldAdd);
		
		panel.add(new JLabel("Can Edit (Yes/No)"));
		fldEdit = new JTextField("");
		fldEdit.addKeyListener(new TextfieldChangeListener());
		panel.add(fldEdit);
		
		panel.add(new JLabel("Can Delete (Yes/No)"));
		fldDelete = new JTextField("");
		fldDelete.addKeyListener(new TextfieldChangeListener());
		panel.add(fldDelete);
				

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
		this.setPreferredSize(new Dimension(400, 210));
	}
	
	/**
	 * Reload fields with model data
	 * Used when model notifies view of change
	 */
	public void refreshFields() {
		fldId.setText("" + myUser.getId());
		fldUserName.setText( myUser.getUser());
//		fldPassword.setText( myUser.getPassword());
		fldFullName.setText( myUser.getFullname());
		fldAdd.setText( "" + myUser.getAdd());
		fldEdit.setText( "" + myUser.getEdit());
		fldDelete.setText( myUser.getDelete());
		
		this.setTitle(myUser.getFullname());
		//flag as unchanged
		setChanged(false);
	}

	/**
	 * saves changes to the view's User model 
	 */
	//if any of them fail then no fields should be changed
	//and previous values reloaded
	//this is called rollback
	@Override
	public boolean saveModel() {
		//display any error message if field data are invalid
				String testName = fldUserName.getText().trim();
				if(!myUser.validUser(testName)) {
					parent.displayChildMessage("Invalid UserName!");
					refreshFields();
					return false;
				}
				
				String testPass = fldPassword.getText().trim();
				if( myUser != null && myUser.getPassword()!= null && myUser.getPassword().length() < 1 && testPass.length() < 1 ){
					parent.displayChildMessage("Invalid Password!");
					return false;
				}
				
				if( testPass.length() > 0 ){
					try {
						testPass = Utility.sha256(testPass);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					testPass = myUser.getPassword();
				}
				
				if(!myUser.validPassword(testPass)) {
					parent.displayChildMessage("Invalid Password!");
					refreshFields();
					return false;
				}
				
				
				String testFullName = fldFullName.getText().trim();
				if(!myUser.validFullName(testFullName)) {
					parent.displayChildMessage("Invalid Full Name!");
					refreshFields();
					return false;
				}
				
			// for add	
				String testAdd = fldAdd.getText().trim();
				if(!myUser.getValidAdd(testAdd)) {
					parent.displayChildMessage("Invalid Add (Yes or No)!");
					refreshFields();
					return false;
				}
			
			// for edit	
				String testEdit = fldEdit.getText().trim();
				if(!myUser.getValidEdit(testEdit)) {
					parent.displayChildMessage("Invalid Edit (Yes or No)!");
					refreshFields();
					return false;
				}
				
			// for delete	
				String testDelete = fldAdd.getText().trim();
				if(!myUser.getValidDelete(testDelete)) {
					parent.displayChildMessage("Invalid Delete (Yes or No)!");
					refreshFields();
					return false;
				}	
				
		//fields are valid so save to model
		try {
			myUser.setUser(testName);
			myUser.setPassword(testPass);
			myUser.setFullName(testFullName);
			myUser.setAdd(fldAdd.getText().trim());
			myUser.setEdit(fldEdit.getText().trim());
			myUser.setDelete(fldDelete.getText().trim());
			
		} catch(Exception e) {
			parent.displayChildMessage(e.getMessage());
			refreshFields();
			return false;
		}
		
		//tell model that update is done (in case it needs to notify observers
		try {
			myUser.finishUpdate();
			setChanged(false);
			
		} catch (GatewayException e) {
			//e.printStackTrace();
			//reset fields to db copy of user if save fails
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
		myUser.deleteObserver(this);
	}

	/**
	 * Called by Observable
	 */
	@Override
	public void update(Observable o, Object arg) {
		refreshFields();
	}

	public Users getMyUser() {
		return myUser;
	}

	public void setMyUser(Users myUser) {
		this.myUser = myUser;
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
