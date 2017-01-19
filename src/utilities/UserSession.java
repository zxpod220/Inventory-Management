package utilities;

import java.util.Random;

import models.Users;

public class UserSession {

	private boolean add;
	private boolean edit;
	private	boolean delete;
	private String userID;
	
	public UserSession() {
		add = edit = delete = false;
		userID = null;
	}
	
	public UserSession(Users user){
		
		Random rand = new Random();
		int randomNumber = rand.nextInt(100000); // 0-100000.
		
		this.userID = user.getUser() + randomNumber;
		this.add = user.getAdd().equals("yes")?true:false;
		this.edit = user.getEdit().equals("yes")?true:false;
		this.delete = user.getDelete().equals("yes")?true:false;
	}
	
	// check attribute
	public boolean checkAdd(){
		return this.add;
	}
	
	public boolean checkEdit(){
		return this.edit;
	}
	
	public boolean checkDelete(){
		return this.delete;
	}
	
	public String getUser(){
		return this.userID;
	}
	
}
