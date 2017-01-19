package models;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class TransferableUser implements Transferable {
private Users users;
	
	public static final DataFlavor DATA_FLAVOR = new DataFlavor(Users.class, "A User Object");

	protected static DataFlavor[] supportedFlavors = {DATA_FLAVOR, DataFlavor.stringFlavor};
	    
	public TransferableUser(Users u) { 
		users = u;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor.equals(DATA_FLAVOR) || flavor.equals(DataFlavor.stringFlavor)) 
			return true;
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(DATA_FLAVOR))
	         return users;
	     else if (flavor.equals(DataFlavor.stringFlavor)) 
	         return users.toString();
	     else 
	         throw new UnsupportedFlavorException(flavor);
	}

}
