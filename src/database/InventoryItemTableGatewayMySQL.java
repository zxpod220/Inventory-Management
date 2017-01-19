package database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import models.InventoryItem;

public class InventoryItemTableGatewayMySQL implements InventoryItemTableGateway{
	
	private static final boolean DEBUG = true;

	/**
	 * external DB connection
	 */
	private Connection conn = null;
	
	/**
	 * Constructor: creates database connection
	 * @throws GatewayException
	 */
	public InventoryItemTableGatewayMySQL() throws GatewayException {
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

	@Override
	public InventoryItem fetchInventoryItem(long id) throws GatewayException {
		InventoryItem p = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch person
			st = conn.prepareStatement("select * from InventoryItem where id = ? ");
			st.setLong(1, id);
			rs = st.executeQuery();
			//should only be 1
			rs.next();
			p = new InventoryItem( rs.getLong("id"), rs.getLong("warehouse_id"), rs.getLong("parts_id"), rs.getLong("quantity") );
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

	@Override
	public void deleteInventoryItem(long id) throws GatewayException {
		PreparedStatement st = null;
		try {
			//turn off autocommit to start the tx
			conn.setAutoCommit(false);
			
			//st = conn.prepareStatement("delete from INVENTORYs where id = ? ");
			st = conn.prepareStatement("delete from InventoryItem where id = ? ");
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

	@Override
	public long insertInventoryItem(InventoryItem p) throws GatewayException {
		//init new id to invalid
		long newId = InventoryItem.INVALID_ID;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			
			st = conn.prepareStatement("INSERT INTO InventoryItem ( "
					+ " warehouse_id, parts_id, quantity )"
					+ " VALUES ( ?, ?, ? )", PreparedStatement.RETURN_GENERATED_KEYS);
			
			st.setLong(1, p.getWarehouseId());
			st.setLong(2, p.getPartId());
			st.setLong(3, p.getQuantity());
			
			st.executeUpdate();
			//get the generated key
			rs = st.getGeneratedKeys();
			if(rs != null && rs.next()) {
			    newId = rs.getLong(1);
			} else {
				throw new GatewayException("Could not fetch new record Id");
			}
		} catch (SQLException e) {
//			System.out.println("testing");
			e.printStackTrace();
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

	@Override
	public void saveInventoryItem(InventoryItem p) throws GatewayException {
		//execute the update and throw exception if any problem
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("update InventoryItem "
					+ " set warehouse_id = ?, parts_id = ?, quantity = ? "
					+ " where id = ? ");
			st.setLong(1, p.getWarehouseId());
			st.setLong(2, p.getPartId());
			st.setLong(3, p.getQuantity());
			st.setLong(4, p.getId());	
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

	@Override
	public List<InventoryItem> fetchInventoryItems() throws GatewayException {
		ArrayList<InventoryItem> ret = new ArrayList<InventoryItem>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch INVENTORYs
			st = conn.prepareStatement("select * from InventoryItem");
			rs = st.executeQuery();
			//add each to list of dogs to return
			while(rs.next()) {
				InventoryItem p = new InventoryItem( rs.getLong("id"), rs.getLong("warehouse_id"), rs.getLong("parts_id"), rs.getLong("quantity")  );
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
}
