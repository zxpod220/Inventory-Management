package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import database.GatewayException;
import database.WarehouseTableGateway;


public class WarehouseList  extends Observable implements Observer {
	/**
	 * Collection of Warehouse objects that this list holds
	 */
	private List<Warehouse> myList;
	
	/**
	 * Identity map for determining if Warehouse is already in this list
	 */
	private HashMap<Long, Warehouse> myIdMap;
	
	/**
	 * Collection of newly added records to know when to update key in Identity map
	 */
	private ArrayList<Warehouse> newRecords;

	/**
	 * Database connection for the WarehouseList 
	 * TODO: needs to be abstracted to a tagging interface
	 */
	private WarehouseTableGateway gateway;
	
	/**
	 * for multiple object inserts and deletes, set to true to notifyObservers at very end (in loadFromGateway)
	 * be sure to set it back to false when done so that addToList and removeFromList will notify after setChanged
	 */
	private boolean dontNotify;
	
	public WarehouseList() {
		myList = new ArrayList<Warehouse>();
		myIdMap = new HashMap<Long, Warehouse>();
		dontNotify = false;
		newRecords = new ArrayList<Warehouse>();
	}
	
	/**
	 * Replaces list contents with new Warehouse objects fetched from Gateway
	 * Insert objects that are not already in list
	 * 
	 * TODO: refresh stale object contents already in list (use a timestamp)
	 * OR only do this when opening ListView is the only Warehouse view open 
	 */
	public void loadFromGateway() {
		//fetch list of objects from the database
		List<Warehouse> people = null;
		try {
			people = gateway.fetchPeople();
		} catch (GatewayException e) {
			//TODO: handle exception here
			return;
		}
		
		//since this method does a lot of adding and removing
		//don't notify observers until all done
		dontNotify = true;
		
		//any Warehouse in our list that is NOT in the db needs to be removed from our list
		for(int i = myList.size() - 1; i >= 0; i--) {
			Warehouse p = myList.get(i);
			boolean removeWarehouse = true;
			//don't remove a recently Added record that hasn't been saved yet
			if(p.getId() == Warehouse.INVALID_ID) {
				removeWarehouse = false;
			} else {
				for(Warehouse pCheck : people) {
					if(pCheck.getId() == p.getId()) {
						removeWarehouse = false;
						break;
					}
				}
			}
			//p not found in db people array so delete it
			if(removeWarehouse)
				removeWarehouseFromList(p);
			//TODO: any detail view with p in it either needs to close or should have a lock to prevent this deletion
			//TODO: may also need to unregister all open views as observers of p
		}
		
		//for each object in fetched list, see if it is in the hashmap using Warehouse.id
		//if not, add it to the list
		for(Warehouse p : people) {
			if(!myIdMap.containsKey(p.getId())) {
				addWarehouseToList(p);
			}
		}
		
		//tell all observers of this list to update
		this.notifyObservers();

		//turn this off
		dontNotify = false;
	}
	
	/**
	 * Add a Warehouse object to the list's collection and set its gateway to this list's gateway
	 * Also add list as observer of p
	 * @param p Warehouse instance to add to the collection
	 */
	public void addWarehouseToList(Warehouse p) {
		myList.add(p);
		p.setGateway(this.gateway);
		p.addObserver(this);
		//add record to identity map
		myIdMap.put(p.getId(), p);

		//tell all observers of this list to update
		this.setChanged();
		if(!dontNotify)
			this.notifyObservers();
	}

	/**
	 * Remove a Warehouse from the list and remove this as observer of p
	 * @return Warehouse p if found in list, otherwise null
	 */
	public Warehouse removeWarehouseFromList(Warehouse p) {
		if(myList.contains(p)) {
			myList.remove(p);
			//also remove from hash map
			myIdMap.remove(p.getId());

			//tell all observers of this list to update
			this.setChanged();
			if(!dontNotify)
				this.notifyObservers();

			return p;
		}
		return null;
	}
	
	/**
	 * Accessors
	 * @return
	 */
	public List<Warehouse> getList() {
		return myList;
	}

	public void setList(List<Warehouse> myList) {
		this.myList = myList;
	}

	public WarehouseTableGateway getGateway() {
		return gateway;
	}

	public void setGateway(WarehouseTableGateway gateway) {
		this.gateway = gateway;
	}

	/**
	 * adds new Warehouse with invalid id to list of new records
	 * during update, if Warehouse updating is a new record then will re-add it to the identity map
	 * @param d
	 */
	public void addToNewRecords(Warehouse p) {
		newRecords.add(p);
	}

	/**
	 * Notify list observers that an object has changed
	 * if Observed record is a new object and its Id has changed, re-add it to the hashmap
	 * @param o the observable that has changed
	 * @param arg
	 */
	@Override
	public void update(Observable o, Object arg) {
		//System.out.println("WarehouseList update");
		Warehouse p = (Warehouse) o;
		if(newRecords.contains(p)) {
			myIdMap.remove(Warehouse.INVALID_ID);
			myIdMap.put(p.getId(), p);
			newRecords.remove(p);
		}

		setChanged();
		notifyObservers();
	}

	public HashMap<Long, String> getWarehouseList() {
HashMap< Long, String> WnameList = new HashMap< Long, String>();
		
		for(int i = myList.size() - 1; i >= 0; i--) {
			Warehouse w = myList.get(i);
			WnameList.put(w.getId(), w.getWarehouseName());
		}	
		
		return WnameList;
	}

	public Warehouse findId(Long warehouseId) {
		//check the identity map
				if(myIdMap.containsKey(new Long(warehouseId)))
					return myIdMap.get(new Long(warehouseId));
				return null;
	}
}
