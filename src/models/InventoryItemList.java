package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import database.InventoryItemTableGateway;
import database.GatewayException;

public class InventoryItemList extends Observable implements Observer {
	/**
	 * Collection of Inventory objects that this list holds
	 */
	private List<InventoryItem> myList;
	
	/**
	 * Identity map for determining if Inventory is already in this list
	 */
	private HashMap<Long, InventoryItem> myIdMap;
	
	/**
	 * Collection of newly added records to know when to update key in Identity map
	 */
	private ArrayList<InventoryItem> newRecords;
	
	/**
	 * Database connection for the InventoryList 
	 */
	private InventoryItemTableGateway gateway;
	
	/**
	 * for multiple object inserts and deletes, set to true to notifyObservers at very end (in loadFromGateway)
	 * be sure to set it back to false when done so that addToList and removeFromList will notify after setChanged
	 */
	private boolean dontNotify;
	
	public InventoryItemList() {
		myList = new ArrayList<InventoryItem>();
		myIdMap = new HashMap<Long, InventoryItem>();
		dontNotify = false;
		newRecords = new ArrayList<InventoryItem>();
	}
	
	/**
	 * Replaces list contents with new Inventory objects fetched from Gateway
	 * Insert objects that are not already in list
	 * 
	 * TODO: refresh stale object contents already in list (use a timestamp)
	 * OR only do this when opening ListView is the only Inventory view open 
	 */
	public void loadFromGateway() {
		//fetch list of objects from the database
		List<InventoryItem> inventory = null;
		
		try {
			inventory = gateway.fetchInventoryItems();
			//System.out.println(inventory);
		} catch (GatewayException e) {
			//e.printStackTrace();
			return;
		}
		
		//since this method does a lot of adding and removing
		//don't notify observers until all done
		dontNotify = true;
		
		//any person in our list that is NOT in the db needs to be removed from our list
		for(int i = myList.size() - 1; i >= 0; i--) {
			InventoryItem it = myList.get(i);
			boolean removeRecord = true;
			//don't remove a recently Added record that hasn't been saved yet
			if(it.getId() == InventoryItem.INVALID_ID) {
				removeRecord = false;
			} else {
				for(InventoryItem itCheck : inventory) {
					if(itCheck.getId() == it.getId()) {
						removeRecord = false;
						break;
					}
				}
			}
			//p not found in db people array so delete it
			if(removeRecord)
				removeInventoryItemFromList(it);
			//TODO: any detail view with p in it either needs to close or should have a lock to prevent this deletion
			//TODO: may also need to unregister all open views as observers of p
		}
		
		//for each object in fetched list, see if it is in the hashmap using Person.id
		//if not, add it to the list
		for(InventoryItem it : inventory) {
			if(!myIdMap.containsKey(it.getId())) {
				addInventoryItemToList(it);
			}
		}
		
		//tell all observers of this list to update
		this.notifyObservers();

		//turn this off
		dontNotify = false;
	}
	
	// check has over one record have same warehouse ID and part ID
	
	public boolean duplicate(InventoryItem inventory){
		for(int i = myList.size() - 1; i >= 0; i--) {
			InventoryItem it = myList.get(i);
			if( ( 	it.getWarehouseId() == inventory.getWarehouseId()) &&
					it.getPartId() == inventory.getPartId() &&
					it.getId() != inventory.getId() )
				return true;
		}
		
		return false;
	}
	
	// exist of warehouse or part
	public boolean existWarehousePart(Long wId, Long pId){
		
		if( wId > 0 ){
			// for warehouse
			for(int i = myList.size() - 1; i >= 0; i--) {
				InventoryItem it = myList.get(i);
				if( it.getWarehouseId() == wId )
					return true;
			}	
			
		}else{
			// for part
			for(int i = myList.size() - 1; i >= 0; i--) {
				InventoryItem it = myList.get(i);
				if( it.getPartId() == pId )
					return true;
			}
			
		}
		
		return false;
	}
	
	public InventoryItem findById(long id) {
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
	public void addInventoryItemToList(InventoryItem it) {
		myList.add(it);
		it.setGateway(this.gateway);
		it.addObserver(this);

		//add to identity map
		myIdMap.put(it.getId(), it);

		//tell all observers of this list to update
		this.setChanged();
		if(!dontNotify)
			this.notifyObservers();
	}

	/**
	 * Remove a person from the list and remove this as observer of p
	 * @return Person p if found in list, otherwise null
	 */
	public InventoryItem removeInventoryItemFromList(InventoryItem it) {
		if(myList.contains(it)) {
			myList.remove(it);
			//also remove from hash map
			myIdMap.remove(it.getId());

			//tell all observers of this list to update
			this.setChanged();
			if(!dontNotify)
				this.notifyObservers();

			return it;
		}
		return null;
	}
	
	// get Total Quality of a warehouse
	public Long getTotalQuantityWarehouseExceptCurrent(InventoryItem inventory){
		Long lTotalquality = 0L;
		for(int i = myList.size() - 1; i >= 0; i--) {
			InventoryItem in = myList.get(i);
			if( 	in.getWarehouseId() == inventory.getWarehouseId() &&
					in.getId() != inventory.getId() )
			{
				lTotalquality += in.getQuantity();
			}
		}	
		
		return lTotalquality;
	}
	
	// get capacity remain of specific warehoue
	public Long remainCapacityInWarehouse(Long capacity, InventoryItem inventory){
		
		return capacity  - (getTotalQuantityWarehouseExceptCurrent(inventory)+ inventory.getQuantity() );

	}
	/**
	 * Accessors
	 * @return
	 */
	public List<InventoryItem> getList() {
		return myList;
	}

	public void setList(List<InventoryItem> myList) {
		this.myList = myList;
	}

	public InventoryItemTableGateway getGateway() {
		return gateway;
	}

	public void setGateway(InventoryItemTableGateway gateway) {
		this.gateway = gateway;
	}

	/**
	 * adds new Inventory with invalid id to list of new records
	 * during update, if Inventory updating is a new record then will re-add it to the identity map
	 * @param d
	 */
	public void addToNewRecords(InventoryItem it) {
		newRecords.add(it);
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
		InventoryItem it = (InventoryItem) o;
		if(newRecords.contains(it)) {
			myIdMap.remove(InventoryItem.INVALID_ID);
			myIdMap.put(it.getId(), it);
			newRecords.remove(it);
		}
		
		this.setChanged();
		notifyObservers();
	}
}
