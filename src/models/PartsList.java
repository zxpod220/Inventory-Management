package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import database.PartsTableGateway;
import database.GatewayException;
import database.WarehouseTableGateway;

public class PartsList extends Observable implements Observer {
	/**
	 * Collection of Parts objects that this list holds
	 */
	private List<Parts> myList;
	
	/**
	 * Identity map for determining if Parts is already in this list
	 */
	private HashMap<Long, Parts> myIdMap;
	
	/**
	 * Collection of newly added records to know when to update key in Identity map
	 */
	private ArrayList<Parts> newRecords;
	
	/**
	 * Database connection for the PartsList 
	 */
	private PartsTableGateway gateway;
	
	/**
	 * for multiple object inserts and deletes, set to true to notifyObservers at very end (in loadFromGateway)
	 * be sure to set it back to false when done so that addToList and removeFromList will notify after setChanged
	 */
	private boolean dontNotify;
	
	public PartsList() {
		myList = new ArrayList<Parts>();
		myIdMap = new HashMap<Long, Parts>();
		dontNotify = false;
		newRecords = new ArrayList<Parts>();
	}
	
	/**
	 * Replaces list contents with new Parts objects fetched from Gateway
	 * Insert objects that are not already in list
	 * 
	 * TODO: refresh stale object contents already in list (use a timestamp)
	 * OR only do this when opening ListView is the only Parts view open 
	 */
	public void loadFromGateway() {
		//fetch list of objects from the database
		List<Parts> Partss = null;
		try {
			Partss = gateway.fetchPartss();
			
		} catch (GatewayException e) {
			e.printStackTrace();
			return;
		}
		
		//since this method does a lot of adding and removing
		//don't notify observers until all done
		dontNotify = true;
		
		//any Warehouse in our list that is NOT in the db needs to be removed from our list
		for(int i = myList.size() - 1; i >= 0; i--) {
			Parts d = myList.get(i);
			boolean removeRecord = true;
			//don't remove a recently Added record that hasn't been saved yet
			if(d.getId() == Parts.INVALID_ID) {
				removeRecord = false;
			} else {
				for(Parts dCheck : Partss) {
					if(dCheck.getId() == d.getId()) {
						removeRecord = false;
						break;
					}
				}
			}
			//p not found in db people array so delete it
			if(removeRecord)
				removePartsFromList(d);
			//TODO: any detail view with p in it either needs to close or should have a lock to prevent this deletion
			//TODO: may also need to unregister all open views as observers of p
		}
		
		//for each object in fetched list, see if it is in the hashmap using Warehouse.id
		//if not, add it to the list
		for(Parts d : Partss) {
			if(!myIdMap.containsKey(d.getId())) {
				addPartsToList(d);
			}
		}
		
		//tell all observers of this list to update
		this.notifyObservers();

		//turn this off
		dontNotify = false;
	}
	
	public Parts findById(long id) {
		//check the identity map
		if(myIdMap.containsKey(new Long(id)))
			return myIdMap.get(new Long(id));
		return null;
	}
	
	/**
	 * Add a Warehouse object to the list's collection and set its gateway to this list's gateway
	 * Also add list as observer of p
	 * @param p Warehouse instance to add to the collection
	 */
	public void addPartsToList(Parts d) {
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
	 * Remove a Warehouse from the list and remove this as observer of p
	 * @return Warehouse p if found in list, otherwise null
	 */
	public Parts removePartsFromList(Parts d) {
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
	public List<Parts> getList() {
		return myList;
	}

	public void setList(List<Parts> myList) {
		this.myList = myList;
	}

	public PartsTableGateway getGateway() {
		return gateway;
	}

	public void setGateway(PartsTableGateway gateway) {
		this.gateway = gateway;
	}

	/**
	 * adds new Parts with invalid id to list of new records
	 * during update, if Parts updating is a new record then will re-add it to the identity map
	 * @param d
	 */
	public void addToNewRecords(Parts d) {
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
		//System.out.println("DEBUG: WarehouseList update");
		//if o is in the newRecords list, remove it from identity map
		//and add it back with new id
		Parts d = (Parts) o;
		if(newRecords.contains(d)) {
			myIdMap.remove(Parts.INVALID_ID);
			myIdMap.put(d.getId(), d);
			newRecords.remove(d);
		}
		
		this.setChanged();
		notifyObservers();
	}

	public HashMap<Long, String> getPartsList() {
HashMap< Long, String> PnameList = new HashMap< Long, String>();
		
		for(int i = myList.size() - 1; i >= 0; i--) {
			Parts p = myList.get(i);
			PnameList.put(p.getId(), p.getPartNumber());
		}	
		
		return PnameList;
	}
}
