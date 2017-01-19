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


import models.Parts;
import models.Warehouse;

public class PartsTableGatewayMySQL implements PartsTableGateway {
	private static final boolean DEBUG = true;

	/**
	 * external DB connection
	 */
	private Connection conn = null;
	
	/**
	 * Constructor: creates database connection
	 * @throws GatewayException
	 */
	public PartsTableGatewayMySQL() throws GatewayException {
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
	public Parts fetchParts(long id) throws GatewayException {
		Parts d = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch person
			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ); //default
			conn.setAutoCommit(false);
			st = conn.prepareStatement("select * from Parts where id = ? ");
			st.setLong(1, id);
			rs = st.executeQuery();
			//should only be 1
			rs.next();
			d = new Parts(rs.getLong("id"), rs.getString("Part_Number"), rs.getString("Part_name"), 
					rs.getString("Unit_of_Quantity"), rs.getString("Vendor")
					, rs.getString("Vendor_Parts_Number"));
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
		return d;
	}
	@Override
	public boolean blockPart(long id, String userName) throws GatewayException {
		
		boolean value = false;
		
		if(id < 1) return value;
		
		Parts p = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch person
			st = conn.prepareStatement("select * from Parts where id = ? FOR UPDATE");
			st.setLong(1, id);
			rs = st.executeQuery();
			//should only be 1
			rs.next();
			
			p = new Parts( rs.getLong("id"), rs.getString("Part_Number"), rs.getString("Part_name"), rs.getString("Unit_of_Quantity"), rs.getString("Vendor"), rs.getString("Vendor_Parts_Number") );
			if(userName == null){
				saveAccess( p.getId(), userName );
			}else{
				if( rs.getString("user_access") == null || (rs.getString("user_access").trim()).equals("")){
					// save part with new access user
					saveAccess( p.getId(), userName );
				}else if( !rs.getString("user_access").equals(userName)){
					value = true;
				}
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
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
		
		return value;
	}

	@Override
	public void deleteParts(long id) throws GatewayException {
		PreparedStatement st = null;
		try {
			//turn off autocommit to start the tx
			conn.setAutoCommit(false);
			
		
			//use this statement to force tx exception to see rollback
			//st = conn.prepareStatement("delete from Partss where id = ? ");
			st = conn.prepareStatement("delete from Parts where id = ? ");
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
	public long insertParts(Parts d) throws GatewayException {
		//init new id to invalid
		long newId = Parts.INVALID_ID;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("insert Parts (Part_Number, Part_name, Unit_of_Quantity, Vendor, Vendor_Parts_Number) "
					+ " values ( ?, ?, ?, ?, ? ) ", PreparedStatement.RETURN_GENERATED_KEYS);
			st.setString(1, d.getPartNumber());
			st.setString(2, d.getPartsName());
			st.setString(3, d.getUnitOQ());
			st.setString(4, d.getVendor());
			st.setString(5, d.getVendorPNum());
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

	@Override
	public void saveParts(Parts d) throws GatewayException {
		//execute the update and throw exception if any problem
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("update Parts "
					+ " set Part_Number = ?,  Part_name = ?, Unit_of_Quantity = ?, Vendor = ?, Vendor_Parts_Number = ? "
					+ " where id = ? ");
			st.setString(1, d.getPartNumber());
			st.setString(2, d.getPartsName());
			st.setString(3, d.getUnitOQ());
			st.setString(4, d.getVendor());
			st.setString(5, d.getVendorPNum());
			st.setLong(6, d.getId());
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
	
	public void saveAccess(long id, String userName) throws GatewayException {
		//execute the update and throw exception if any problem
		String datetime = "datetime = NULL";
		if(userName!=null)
			datetime = "datetime = now()";
		
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("update Parts "
					+ " set user_access = ?, "+ datetime
			 		+ " where id = ? ");
			
			st.setString(1, userName);
			st.setLong(2, id);
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
public void updateAccessTime(int second){
		
		int minute = 60 * second;
		
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("update Parts "
					+ " set user_access = NULL, datetime = NULL"
					+ " where ( (now() - datetime ) > "+minute+" )");
			
			st.executeUpdate();
			
		} catch (SQLException e) {
			try {
				throw new GatewayException(e.getMessage());
			} catch (GatewayException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			//clean up
			try {
				if(st != null)
					st.close();
			} catch (SQLException e) {
				try {
					throw new GatewayException("SQL Error: " + e.getMessage());
				} catch (GatewayException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	@Override
	public List<Parts> fetchPartss() throws GatewayException {
		ArrayList<Parts> ret = new ArrayList<Parts>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch Partss
			st = conn.prepareStatement("select * from Parts");
			rs = st.executeQuery();
			//add each to list of Partss to return
			while(rs.next()) {
				Parts d = new Parts(rs.getLong("id"), rs.getString("Part_Number"), rs.getString("Part_name"), 
						  rs.getString("Unit_of_Quantity"), rs.getString("Vendor"),
						  rs.getString("Vendor_Parts_Number"));
				ret.add(d);
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

	@Override
	public boolean PartsNumberAlreadyExists(long id, String dn) throws GatewayException {
		// TODO Auto-generated method stub
		return false;
	}
}
