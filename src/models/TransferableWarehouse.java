package models;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


public class TransferableWarehouse implements Transferable{

private Warehouse warehouse;
	
	public static final DataFlavor WAREHOUSE_FLAVOR = new DataFlavor(Warehouse.class, "A Warehouse Object");

	protected static DataFlavor[] supportedFlavors = {WAREHOUSE_FLAVOR, DataFlavor.stringFlavor};
	    
	public TransferableWarehouse(Warehouse w) { 
		warehouse = w;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor.equals(WAREHOUSE_FLAVOR) || flavor.equals(DataFlavor.stringFlavor)) 
			return true;
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(WAREHOUSE_FLAVOR))
	         return warehouse;
	     else if (flavor.equals(DataFlavor.stringFlavor)) 
	         return warehouse.toString();
	     else 
	         throw new UnsupportedFlavorException(flavor);
	}

}
