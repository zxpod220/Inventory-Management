package database;

import java.util.List;

import models.Users;

public interface UserTableGateway {
	public abstract Users fetchUser(long id) throws GatewayException;
	public Users fetchLogin(Users u) throws GatewayException;	
	public abstract void deleteUser(long id) throws GatewayException;
	public abstract long insertUser(Users u) throws GatewayException;
	public abstract void saveUser(Users u) throws GatewayException;
	public abstract List<Users> fetchUsers() throws GatewayException;
	public abstract void close();
}
