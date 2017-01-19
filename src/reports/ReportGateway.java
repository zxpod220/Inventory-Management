package reports;

import java.util.HashMap;
import java.util.List;

import database.GatewayException;

public interface ReportGateway {
	public abstract List< HashMap<String, String> > fetchInventory() throws GatewayException;
	public abstract void close();
}
