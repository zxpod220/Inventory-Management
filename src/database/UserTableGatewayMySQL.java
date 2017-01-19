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

import models.Users;

public class UserTableGatewayMySQL implements UserTableGateway {
	private static final boolean DEBUG = true;

	/**
	 * external DB connection
	 */
	private Connection conn = null;
	
	/**
	 * Constructor: creates database connection
	 * @throws GatewayException
	 */
	public UserTableGatewayMySQL() throws GatewayException {
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
	public Users fetchUser(long id) throws GatewayException {
		Users d = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch person
			st = conn.prepareStatement("select * from Users where id = ? ");
			st.setLong(1, id);
			rs = st.executeQuery();
			//should only be 1
			rs.next();
			
			d = new Users(rs.getLong("id"), rs.getString("user"), rs.getString("password"),rs.getString("fullName"),rs.getString("add_act"),rs.getString("edit_act"),rs.getString("delete_act"));
			
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
	
	
	public Users fetchLogin(Users u) throws GatewayException {
		Users d = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch person
			st = conn.prepareStatement("select * from Users where user = ? and password=? ");
			st.setString(1, u.getUser());
			st.setString(2, u.getPassword());
			
			rs = st.executeQuery();
			
			//should only be 1
			if(rs.next()){
				d = new Users(rs.getLong("id"), rs.getString("user"), rs.getString("password"),rs.getString("fullName"),rs.getString("add_act"),rs.getString("edit_act"),rs.getString("delete_act"));
			}
			
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
	public void deleteUser(long id) throws GatewayException {
		PreparedStatement st = null;
		try {
			//turn off autocommit to start the tx
			conn.setAutoCommit(false);
			
			//use this statement to force tx exception to see rollback
			//st = conn.prepareStatement("delete from dogs where id = ? ");
			st = conn.prepareStatement("delete from Users where id = ? ");
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
	public long insertUser(Users u) throws GatewayException {
		//init new id to invalid
		long newId = Users.INVALID_ID;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("insert Users (user, password, fullName, add_act, edit_act, delete_act) "
					+ " values ( ?, ?, ?, ?, ?, ? ) ", PreparedStatement.RETURN_GENERATED_KEYS);
			
			st.setString(1, u.getUser());
			st.setString(2, u.getPassword());
			st.setString(3, u.getFullname());
			st.setString(4, u.getAdd());
			st.setString(5, u.getEdit());
			st.setString(6, u.getDelete());
			
			st.executeUpdate();
			//get the generated key
			rs = st.getGeneratedKeys();
			if(rs != null && rs.next()) {
			    newId = rs.getLong(1);
			} else {
				throw new GatewayException("Could not fetch new record Id");
			}
		} catch (SQLException e) {
//			System.out.print(e.getMessage());
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
	public void saveUser(Users u) throws GatewayException {
		//execute the update and throw exception if any problem
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("update Users "
					+ " set user = ?, password = ?, fullName = ?, add_act = ?, edit_act = ?, delete_act = ? "
					+ " where id = ? ");
			
			st.setString(1, u.getUser() );
			st.setString(2, u.getPassword() );
			st.setString(3, u.getFullname() );
			st.setString(4, u.getAdd() );
			st.setString(5, u.getEdit() );
			st.setString(6, u.getDelete() );					
			st.setLong(7, u.getId());
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
	public List<Users> fetchUsers() throws GatewayException {
		ArrayList<Users> ret = new ArrayList<Users>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch USERs
			st = conn.prepareStatement("select * from Users");
			rs = st.executeQuery();
			//add each to list of USERs to return
			while(rs.next()) {
				Users u = new Users(rs.getLong("id"), rs.getString("user"), rs.getString("password"),rs.getString("fullName"),rs.getString("add_act"),rs.getString("edit_act"),rs.getString("delete_act"));
				
				ret.add(u);
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
