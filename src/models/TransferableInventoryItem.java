package models;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class TransferableInventoryItem implements Transferable{

private InventoryItem inventoryitem;
	
	public static final DataFlavor INVENTORY_FLAVOR = new DataFlavor(InventoryItem.class, "An Inventory Object");

	protected static DataFlavor[] supportedFlavors = {INVENTORY_FLAVOR, DataFlavor.stringFlavor};
	    
	public TransferableInventoryItem(InventoryItem it) { 
		inventoryitem = it;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor.equals(INVENTORY_FLAVOR) || flavor.equals(DataFlavor.stringFlavor)) 
			return true;
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(INVENTORY_FLAVOR))
	         return inventoryitem;
	     else if (flavor.equals(DataFlavor.stringFlavor)) 
	         return inventoryitem.toString();
	     else 
	         throw new UnsupportedFlavorException(flavor);
	}
}
