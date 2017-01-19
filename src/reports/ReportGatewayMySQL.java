package reports;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import database.GatewayException;

public class ReportGatewayMySQL implements ReportGateway {

	/**
	 * external DB connection
	 */
	private Connection conn = null;

	/**
	 * Constructor: creates database connection
	 * @throws GatewayException
	 */
	public ReportGatewayMySQL() throws GatewayException {
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
			
		} catch (SQLException e) {
			throw new GatewayException("SQL Error: " + e.getMessage());
		}
	}
	
	
	public List< HashMap<String, String> > fetchInventory() throws GatewayException {
		
		List< HashMap<String, String> > warehousePart = new ArrayList< HashMap<String, String> >();
		
		HashMap<String, String> record = null;
		
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			
			
			st = conn.prepareStatement("SELECT wh.warehouse_name as warehouse_name, par.Part_Number as Part_Number, par.Part_name as Part_name, inv.quantity as quantity, par.Unit_of_Quantity as Unit_of_Quantity "
					+ "FROM InventoryItem inv "
					+ "INNER JOIN Warehouse wh ON inv.warehouse_id = wh.id "
					+ "INNER JOIN Parts par ON inv.parts_id = par.id "
					+ "WHERE inv.quantity > 0 "
					+ "ORDER BY wh.warehouse_name, par.Part_name ");
			rs = st.executeQuery();
			
			//add each to list of people to return
			while(rs.next()) {
				record = new HashMap<String, String>();
				
				record.put("warehouse_name", rs.getString("warehouse_name"));
				record.put("Part_Number", rs.getString("Part_Number"));
				record.put("Part_name", rs.getString("Part_name"));
				record.put("quantity", rs.getString("quantity"));
				record.put("Unit_of_Quantity", rs.getString("Unit_of_Quantity"));
				
				warehousePart.add(record);
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
		return warehousePart;
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
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}
