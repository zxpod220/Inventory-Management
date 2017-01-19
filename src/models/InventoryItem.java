package models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import controller.MDIParent;
import database.GatewayException;
import database.InventoryItemTableGateway;


public class InventoryItem extends Observable {
	public static final String ERRORMSG_INVALID_ID = "Invalid id!";
	public static final String ERRORMSG_INVALID_INVENTORY_QUALITY = "Invalid item quantity!";
	public static final String ERRORMSG_INVALID_INVENTORY_WAREHOUSE_ID = "Invalid inventory Warehouse Id!";
	public static final String ERRORMSG_INVALID_INVENTORY_PART_ID = "Invalid inventory Part Id!";
	
	public static final String DEFAULT_EMPTY_INVENTORYNAME = "Unknown";
	public static final int INVALID_ID = 0;
	
	
	private long id;
	private Long parts_id, warehouse_id;
	private Long quantity;
	
	/**
	 * Database connection for the Inventory (same gateway used by PersonInventory) 
	 */
	private InventoryItemTableGateway gateway;
	
	public InventoryItem() {
		id = INVALID_ID;
		parts_id = warehouse_id = 0L;
		quantity = 0L;
	}

	
	public InventoryItem( long WarehouseID, long PartsID, Long iQuantity) {
		this();
		
		//validate parameters
		if(!validQuantity(iQuantity))
			throw new IllegalArgumentException(ERRORMSG_INVALID_INVENTORY_QUALITY);
		
		this.id = INVALID_ID;
		this.warehouse_id = WarehouseID;
		this.parts_id = PartsID;
		this.quantity = iQuantity;
	}
	
	public InventoryItem( long id, long WarehouseID, long PartsID, Long iQuantity) {
		this( WarehouseID, PartsID, iQuantity );
		this.id = id;
	}
	
	public void finishUpdate() throws GatewayException {
		InventoryItem orig = null;

		try {
			//if id is 0 then this is a new Inventory to insert, else its an update
			if(this.getId() == 0) {
				//set id to the long returned by insertPerson
				this.setId(gateway.insertInventoryItem(this));
				
			} else {
				//fetch dog from db table in case this fails
			
				orig = gateway.fetchInventoryItem(this.getId());
				
				//try to save to the database
				gateway.saveInventoryItem(this);
				
			}
			
			//if gateway ok then notify observers
			//System.out.println("DEBUG: Inventory notify");
			notifyObservers();
			
		} catch(GatewayException e) {
			//if fails then try to refetch model fields from the database
			if(orig != null) {
				this.setQuantity(orig.getQuantity());
			}
			throw new GatewayException("Error trying to save the Inventory object!");
		}
	}

	public void delete() throws GatewayException {
		//if id is 0 then nothing to do in the gateway (record has not been saved yet)
		if(this.getId() == 0) 
			return;
			try {
				gateway.deleteInventoryItem(this.getId());
			} catch (GatewayException e) {
				throw new GatewayException(e.getMessage());
			}
	}

	public WarehouseList getWarehouseList(MDIParent p){
		return p.getWarehouseList();
	}
	
	public PartsList getPartsList(MDIParent p){
		return p.getPartsList();
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}



	public Long getWarehouseId() {
		return this.warehouse_id;
	}

	
	/**
	 * Valid WarhouseID
	 * 
	 */
	public boolean validWarehouseId(Long iWarehouseId) {
		if(iWarehouseId == null)
			return false;
		return	( iWarehouseId < 1)? false:true;
		
	}
	
	/**
	 * Sets WarehouseId of Inventory
	 * inventoryId cannot be null
	 * @return
	 */
	public void setWarehouseId(long WarehouseId) {
		if(!validQuantity(WarehouseId))
			throw new IllegalArgumentException(ERRORMSG_INVALID_INVENTORY_WAREHOUSE_ID);
		this.warehouse_id = WarehouseId;
		//get ready to notify observers (notify is called in finishUpdate())
		setChanged();
	}
	
// part id
	
	/**
	 * Returns PartId of Inventory
	 * @return
	 */
	public Long getPartId() {
		return this.parts_id;
	}

	
	/**
	 * Determines if given PartId value is valid
	 * Rules: cannot be null, and >= 0.0
	 * 
	 */
	public boolean validPartId(Long iPartId) {
		
		if(iPartId == null)
			return false;
		
		return	( iPartId < 1)? false:true;
	}
	
	/**
	 * Sets PartId of Inventory
	 * PartID cannot be null
	 * @return
	 */
	public void setPartId(Long iPartId) {
		if(!validPartId(iPartId))
			throw new IllegalArgumentException(ERRORMSG_INVALID_INVENTORY_PART_ID);
		this.parts_id = iPartId;
		//get ready to notify observers (notify is called in finishUpdate())
		setChanged();
	}
	
	/**
	 * Returns quanlity of Inventory
	 * @return
	 */
	public Long getQuantity() {
		return this.quantity;
	}

	
	/**
	 * Determines if given quantity value is valid
	 * Rules: cannot be null, and >= 0.0
	 * 
	 */
	public boolean validQuantity(long iQuantity) {
		return (iQuantity >= 0)? true: false;
	}
	
	/**
	 * Sets Quanlity of Inventory
	 * inventoryId cannot be null
	 * @return
	 */
	public void setQuantity(Long iQuantity) {
		if(!validQuantity(iQuantity))
			throw new IllegalArgumentException(ERRORMSG_INVALID_INVENTORY_QUALITY);
		this.quantity = iQuantity;
		//get ready to notify observers (notify is called in finishUpdate())
		setChanged();
	}

	
	
	public InventoryItemTableGateway getGateway() {
		return gateway;
	}

	public void setGateway(InventoryItemTableGateway gateway) {
		this.gateway = gateway;
	}	
}