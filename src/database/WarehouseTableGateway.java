package database;

import java.util.List;

import models.Warehouse;
import models.PartsList;
import models.Parts;





import models.InventoryItem;

public interface WarehouseTableGateway {
	public abstract Warehouse fetchWarehouse(long id) throws GatewayException;
	public abstract boolean WarehouseAlreadyExists(long id, String wn) throws GatewayException;
	public abstract void deleteWarehouse(long id) throws GatewayException;
	public abstract long insertWarehouse(Warehouse p) throws GatewayException;
	public abstract void saveWarehouse(Warehouse p) throws GatewayException;
	public abstract List<Warehouse> fetchPeople() throws GatewayException;


	public abstract void close();
}
