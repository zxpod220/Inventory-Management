package database;

import java.util.List;

import models.Parts;

public interface PartsTableGateway {
	
	public abstract Parts fetchParts(long id) throws GatewayException;
	public abstract boolean PartsNumberAlreadyExists(long id, String dn) throws GatewayException;
	boolean blockPart(long id, String userName) throws GatewayException;
	public abstract void deleteParts(long id) throws GatewayException;
	public abstract long insertParts(Parts d) throws GatewayException;
	public abstract void saveParts(Parts d) throws GatewayException;
	public abstract List<Parts> fetchPartss() throws GatewayException;
	public abstract void updateAccessTime(int i);
	public abstract void close();
}
