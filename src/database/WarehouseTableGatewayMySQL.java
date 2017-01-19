package database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import models.InventoryItem;
import models.Parts;
import models.PartsList;
import models.Warehouse;



public class WarehouseTableGatewayMySQL implements WarehouseTableGateway {
	private static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final boolean DEBUG = true;

	/**
	 * external DB connection
	 */
	private Connection conn = null;
	
	/**
	 * Constructor: creates database connection
	 * @throws GatewayException
	 */
	public WarehouseTableGatewayMySQL() throws GatewayException {
		//read the properties file to establish the db connection
		DataSource ds = null;
		try {
			ds = getDataSource();
		} catch (RuntimeException | IOException e1) {
			throw new GatewayException(e1.getMessage());
		}
		if(ds == null) {
        	throw new GatewayException("Datasource is null!");
        }
		try {
        	conn = ds.getConnection();
			//default isolation level of allow Phantom Reads is ok for this application
			//conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE); //prevent even phantom reads
		} catch (SQLException e) {
			throw new GatewayException("SQL Error: " + e.getMessage());
		}
	}

	/**
	 * Returns new Warehouse instance from db using Warehouse's id
	 * @param id Id of the Warehouse in the db to fetch
	 * @return new Warehouse instance with that Warehouse's data from db
	 */
	public Warehouse fetchWarehouse(long id) throws GatewayException {
		Warehouse p = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch Warehouse
			st = conn.prepareStatement("select * from Warehouse where id = ? ");
			st.setLong(1, id);
			rs = st.executeQuery();
			//should only be 1
			rs.next();
			p = new Warehouse(rs.getLong("id"), rs.getString("warehouse_name"), rs.getString("warehouse_address"), rs.getString("warehouse_city"),rs.getString("warehouse_state"),rs.getString("warehouse_zip"),rs.getInt("warehouse_storage_capacity"));
						
		} catch (SQLException e) {
			//e.printStackTrace();
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(rs != null)
					rs.close();
				if(st != null)
					st.close();
			} catch (SQLException e) {
				throw new GatewayException("SQL Error: " + e.getMessage());
			}
		}
		return p;
	}

	/**
	 * determines if given first and last name already exist in the Warehouse table
	 * @param fn Warehouse's first name
	 * @param ln Warehouse's last name
	 * @return true if Warehouse exists in database, else false
	 */
	public boolean WarehouseAlreadyExists(long id, String wn) throws GatewayException {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch Warehouse
			st = conn.prepareStatement("select count(id) as num_records "
					+ " from Warehouse where warehouse_name = ? and id <> ? ");
			st.setString(1, wn);
			st.setLong(2, id);
			rs = st.executeQuery();
			//should only be 1
			rs.next();
			if(rs.getInt("num_records") > 0)
				return true;
			//give Warehouse object a reference to this gateway
			//NOTE: this is now the responsibility of the WarehouseList
			//p.setGateway(this);
		} catch (SQLException e) {
			//e.printStackTrace();
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(rs != null)
					rs.close();
				if(st != null)
					st.close();
			} catch (SQLException e) {
				throw new GatewayException("SQL Error: " + e.getMessage());
			}
		}
		return false;
	}
	
	/**
	 * Deletes Warehouse from the database
	 * Note: uses a transaction to perform deletion since it involves multiple tables
	 * @param id Id of the Warehouse in the db to fetch
	 */
	public void deleteWarehouse(long id) throws GatewayException {
		PreparedStatement st = null;
		try {
			//turn off autocommit to start the tx
			conn.setAutoCommit(false);
			
			
			//delete Warehouse
			
			//use this statement to force tx exception to see rollback
			//st = conn.prepareStatement("delete from Warehouses where id = ? ");
			st = conn.prepareStatement("delete from Warehouse where id = ? ");
			st.setLong(1, id);
			st.executeUpdate();
			
			//if we get here, everything worked without exception so commit the changes
			conn.commit();

		} catch (SQLException e) {
			//roll the tx back
			try {
				conn.rollback();
			} catch (SQLException e1) {
				throw new GatewayException(e1.getMessage());
			}
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(st != null)
					st.close();
				//turn autocommit on again regardless if commit or rollback
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				throw new GatewayException(e.getMessage());
			}
		}
	}

	/**
	 * Insert a new Warehouse into the database Warehouse table
	 * @param p Warehouse to insert into the Warehouse table
	 * @return the new Id of the inserted Warehouse
	 * @throws GatewayException
	 */
	public long insertWarehouse(Warehouse p) throws GatewayException {
		//init new id to invalid
		long newId = Warehouse.INVALID_ID;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("insert Warehouse (warehouse_name, warehouse_address, warehouse_city, warehouse_state, warehouse_zip, warehouse_storage_capacity) "
					+ " values ( ?, ?, ?, ?, ?, ? ) ", PreparedStatement.RETURN_GENERATED_KEYS);
			st.setString(1, p.getWarehouseName());
			st.setString(2,  p.getWarehouseAddress());
			st.setString(3,  p.getWarehouseCity());
			st.setString(4,  p.getWarehouseState());
			st.setString(5,  p.getWarehouseZip());
			st.setLong(6, p.getWarehouseSCap());
			st.executeUpdate();
			//get the generated key
			rs = st.getGeneratedKeys();
			if(rs != null && rs.next()) {
			    newId = rs.getLong(1);
			} else {
				throw new GatewayException("Could not fetch new record Id");
			}
		} catch (SQLException e) {
			//e.printStackTrace();
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(st != null)
					st.close();
			} catch (SQLException e) {
				throw new GatewayException("SQL Error: " + e.getMessage());
			}
		}
		return newId;
	}
	
	/**
	 * Saves existing Warehouse to database.
	 * To add a new Warehouse, call the insertWarehouse method 
	 * @param p
	 * @throws GatewayException
	 */
	public void saveWarehouse(Warehouse p) throws GatewayException {
		//execute the update and throw exception if any problem
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("update Warehouse "
					+ " set warehouse_name = ?, warehouse_address = ?, warehouse_city = ?, warehouse_state = ? ,  warehouse_zip = ?, warehouse_storage_capacity = ?"
					+ " where id = ?");
			st.setString(1, p.getWarehouseName());
			st.setString(2, p.getWarehouseAddress());
			st.setString(3, p.getWarehouseCity());
			st.setString(4, p.getWarehouseState());
			st.setString(5, p.getWarehouseZip());
			st.setLong(6, p.getWarehouseSCap());
			st.setLong(7, p.getId());
			
			
			st.executeUpdate();
		} catch (SQLException e) {
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(st != null)
					st.close();
			} catch (SQLException e) {
				throw new GatewayException("SQL Error: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Fetch a resultset of all Warehouse rows in db and populate a collection with them.
	 * All Warehouse instances are given a reference to this Gateway.
	 * @return a List of Warehouse objects (ArrayList)
	 * @throws GatewayException
	 */
	public List<Warehouse> fetchPeople() throws GatewayException {
		ArrayList<Warehouse> ret = new ArrayList<Warehouse>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch people
			st = conn.prepareStatement("select * from Warehouse");
			rs = st.executeQuery();
			//add each to list of people to return
			while(rs.next()) {
				Warehouse p = new Warehouse(rs.getLong("id"), rs.getString("warehouse_name"), rs.getString("warehouse_address"), rs.getString("warehouse_city"), rs.getString("warehouse_state"), rs.getString("warehouse_zip"), rs.getInt("warehouse_storage_capacity"));
				//give each Warehouse object a reference to this gateway
				//makes model-level saving much simpler (model can tell the gateway to save to the database)
				//instead of having to go back through the WarehouseList and then back to the model for notifying observers
				//p.setGateway(this);
				ret.add(p);
			}
		} catch (SQLException e) {
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(rs != null)
					rs.close();
				if(st != null)
					st.close();
			} catch (SQLException e) {
				throw new GatewayException("SQL Error: " + e.getMessage());
			}
		}
		
		return ret;
	}
	
	public void close() {
		if(DEBUG)
			System.out.println("Closing db connection...");
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * create a MySQL datasource with credentials and DB URL in db.properties file
	 * @return
	 * @throws RuntimeException
	 * @throws IOException
	 */
	private DataSource getDataSource() throws RuntimeException, IOException {
		//read db credentials from properties file
		Properties props = new Properties();
		FileInputStream fis = null;
        fis = new FileInputStream("db.properties");
        props.load(fis);
        fis.close();
        
        //create the datasource
        MysqlDataSource mysqlDS = new MysqlDataSource();
        mysqlDS.setURL(props.getProperty("MYSQL_DB_URL"));
        mysqlDS.setUser(props.getProperty("MYSQL_DB_USERNAME"));
        mysqlDS.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));
        return mysqlDS;
	}



	
}
