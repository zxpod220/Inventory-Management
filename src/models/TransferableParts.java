package models;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class TransferableParts implements Transferable {
	
	private Parts parts;
	
	public static final DataFlavor PART_FLAVOR = new DataFlavor(Parts.class, "A Part Object");

	protected static DataFlavor[] supportedFlavors = {PART_FLAVOR, DataFlavor.stringFlavor};
	    
	public TransferableParts(Parts p) { 
		parts = p;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor.equals(PART_FLAVOR) || flavor.equals(DataFlavor.stringFlavor)) 
			return true;
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(PART_FLAVOR))
	         return parts;
	     else if (flavor.equals(DataFlavor.stringFlavor)) 
	         return parts.toString();
	     else 
	         throw new UnsupportedFlavorException(flavor);
	}

}
