package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import database.UserTableGateway;
import database.GatewayException;


public class UserList extends Observable implements Observer {
private List<Users> myList;
	
	/**
	 * Identity map for determining if User is already in this list
	 */
	private HashMap<Long, Users> myIdMap;
	
	/**
	 * Collection of newly added records to know when to update key in Identity map
	 */
	private ArrayList<Users> newRecords;
	
	/**
	 * Database connection for the UserList 
	 */
	private UserTableGateway gateway;
	
	/**
	 * for multiple object inserts and deletes, set to true to notifyObservers at very end (in loadFromGateway)
	 * be sure to set it back to false when done so that addToList and removeFromList will notify after setChanged
	 */
	private boolean dontNotify;
	
	public UserList() {
		myList = new ArrayList<Users>();
		myIdMap = new HashMap<Long, Users>();
		dontNotify = false;
		newRecords = new ArrayList<Users>();
	}
	
	/**
	 * Replaces list contents with new User objects fetched from Gateway
	 * Insert objects that are not already in list
	 * 
	 * TODO: refresh stale object contents already in list (use a timestamp)
	 * OR only do this when opening ListView is the only User view open 
	 */
	public void loadFromGateway() {
		//fetch list of objects from the database
		List<Users> users = null;
		try {
			users = gateway.fetchUsers();
			
		} catch (GatewayException e) {
			e.printStackTrace();
			return;
		}
		
		//since this method does a lot of adding and removing
		//don't notify observers until all done
		dontNotify = true;
		
		//any person in our list that is NOT in the db needs to be removed from our list
		for(int i = myList.size() - 1; i >= 0; i--) {
			Users d = myList.get(i);
			boolean removeRecord = true;
			//don't remove a recently Added record that hasn't been saved yet
			if(d.getId() == Users.INVALID_ID) {
				removeRecord = false;
			} else {
				for(Users dCheck : users) {
					if(dCheck.getId() == d.getId()) {
						removeRecord = false;
						break;
					}
				}
			}
			//p not found in db people array so delete it
			if(removeRecord)
				removeUserFromList(d);
			//TODO: any detail view with p in it either needs to close or should have a lock to prevent this deletion
			//TODO: may also need to unregister all open views as observers of p
		}
		
		//for each object in fetched list, see if it is in the hashmap using Person.id
		//if not, add it to the list
		for(Users d : users) {
			if(!myIdMap.containsKey(d.getId())) {
				addUserToList(d);
			}
		}
		
		//tell all observers of this list to update
		this.notifyObservers();

		//turn this off
		dontNotify = false;
	}
	
	public Users findById(long id) {
		//check the identity map
		if(myIdMap.containsKey(new Long(id)))
			return myIdMap.get(new Long(id));
		return null;
	}
	
	/**
	 * Add a person object to the list's collection and set its gateway to this list's gateway
	 * Also add list as observer of p
	 * @param p Person instance to add to the collection
	 */
	public void addUserToList(Users d) {
		myList.add(d);
		d.setGateway(this.gateway);
		d.addObserver(this);

		//add to identity map
		myIdMap.put(d.getId(), d);

		//tell all observers of this list to update
		this.setChanged();
		if(!dontNotify)
			this.notifyObservers();
	}

	/**
	 * Remove a person from the list and remove this as observer of p
	 * @return Person p if found in list, otherwise null
	 */
	public Users removeUserFromList(Users d) {
		if(myList.contains(d)) {
			myList.remove(d);
			//also remove from hash map
			myIdMap.remove(d.getId());

			//tell all observers of this list to update
			this.setChanged();
			if(!dontNotify)
				this.notifyObservers();

			return d;
		}
		return null;
	}
	
	/**
	 * Accessors
	 * @return
	 */
	public List<Users> getList() {
		return myList;
	}

	public void setList(List<Users> myList) {
		this.myList = myList;
	}

	public UserTableGateway getGateway() {
		return gateway;
	}

	public void setGateway(UserTableGateway gateway) {
		this.gateway = gateway;
	}

	/**
	 * adds new User with invalid id to list of new records
	 * during update, if User updating is a new record then will re-add it to the identity map
	 * @param d
	 */
	public void addToNewRecords(Users d) {
		newRecords.add(d);
	}
	
	/**
	 * Notify list observers that an object has changed
	 * if Observed record is a new object and its Id has changed, re-add it to the hashmap
	 * @param o the observable that has changed
	 * @param arg
	 */
	@Override
	public void update(Observable o, Object arg) {
		//System.out.println("DEBUG: PersonList update");
		//if o is in the newRecords list, remove it from identity map
		//and add it back with new id
		Users d = (Users) o;
		if(newRecords.contains(d)) {
			myIdMap.remove(Users.INVALID_ID);
			myIdMap.put(d.getId(), d);
			newRecords.remove(d);
		}
		
		this.setChanged();
		notifyObservers();
	}
}
