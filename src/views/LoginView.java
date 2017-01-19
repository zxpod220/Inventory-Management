package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.MDIChild;
import controller.MDIParent;
import controller.MenuCommands;
import database.GatewayException;
import database.UserTableGateway;
import models.Users;

import utilities.Utility;

public class LoginView extends MDIChild {
	
	private Users myUser;
	
	/**
	 * Fields for Part data access
	 */
	private JTextField fldUser, fldPass;
	
	private UserTableGateway gateway;
	
	MDIParent mParent;
		
	/**
	 * Constructor
	 * @param title
	 */
	public LoginView(String title, UserTableGateway userGateway, MDIParent m) {
		super(title, m);
		
		gateway = userGateway;
		
		mParent = m;
		
		myUser = new Users();
		
		//prep layout and fields
		JPanel panel = new JPanel(); 
		
		panel.setLayout(new GridLayout( 3, 2, 5, 3));
		
		panel.add(new JLabel("User Name"));
		fldUser = new JTextField("");		
		panel.add(fldUser);
		
		panel.add(new JLabel("Password"));
		fldPass = new JTextField("");		
		panel.add(fldPass);
		
		this.add(panel, BorderLayout.CENTER);
		
		//add a Save button to write field changes back to model data
		panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton button = new JButton("Login");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveModel();
			}
		});
		panel.add(button);

		
		this.add(panel, BorderLayout.SOUTH);
		
		//can't call this on JPanel
		//this.pack();
		this.setPreferredSize(new Dimension(400, 260));
	}
	
	
	/**
	 * saves changes to the view's Part model 
	 */
	//if any of them fail then no fields should be changed
	//and previous values reloaded
	//this is called rollback
	@Override
	public boolean saveModel() {
		//display any error message if field data are invalid
		
		String testUser = fldUser.getText().trim();
		if(!myUser.validUser(testUser)) {
			parent.displayChildMessage("User error");
			return false;
		}
		
		String testPass = fldPass.getText().trim();
		if(!myUser.validPassword(testPass)) {
			parent.displayChildMessage("Password error");			
			return false;
		}
		
		//fields are valid so save to model
		try {
			myUser.setUser(testUser);
			myUser.setPassword( Utility.sha256(testPass));
		} catch(Exception e) {
			parent.displayChildMessage(e.getMessage());			
			return false;
		}
		
				
		try {
			Users new_user =	gateway.fetchLogin(myUser);
			if( new_user!= null ){
				// save to session
				mParent.setUserSession(new_user);
				this.getMDIChildFrame().dispose();
			}else{
				String message = "Unable to login \n\n"
						+ "Please input with some corrected values below:\n\n"
						+ "      'Bob' and 'bob' \n"
						+ "      'Sue' and 'sue' \n"
						+ "      'Ragnar' and 'ragnar'\n";
				parent.displayChildMessage(message);
				
				fldUser.setText("");
				fldPass.setText("");
				return false;
			}
		} catch (GatewayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		parent.displayChildMessage( myUser.getUser() + " has successfully logged in");
		return true;
	}
	
	/**
	 * Subclass-specific cleanup
	 */
	@Override
	protected void cleanup() {
		//let superclass do its thing
		super.cleanup();

	}
}
