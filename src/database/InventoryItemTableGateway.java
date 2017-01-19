package database;

import java.util.List;

import models.InventoryItem;



public interface InventoryItemTableGateway {

	public abstract InventoryItem fetchInventoryItem(long id) throws GatewayException;
	//public abstract boolean dogAlreadyExists(long id, String dn) throws GatewayException;
	public abstract void deleteInventoryItem(long id) throws GatewayException;
	public abstract long insertInventoryItem(InventoryItem i) throws GatewayException;
	public abstract void saveInventoryItem(InventoryItem i) throws GatewayException;
	public abstract List<InventoryItem> fetchInventoryItems() throws GatewayException;
	
	public abstract void close();
}
