package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataListener;

import models.Warehouse;
import models.WarehouseList;

public class WarehouseListController extends AbstractListModel<Warehouse> implements Observer {
private WarehouseList myList;
	
	/**
	 * GUI container housing this object's list controller's JList
	 * Allows this controller to tell the view to repaint() if models in list change
	 */
	private MDIChild myListView;
	
	public WarehouseListController(WarehouseList pl) {
		super();
		myList = pl;
		
		//register as observer to Warehouse list
		pl.addObserver(this);
	}
	
	@Override
	public int getSize() {
		return myList.getList().size();
	}

	@Override
	public Warehouse getElementAt(int index) {
		if(index >= getSize())
			throw new IndexOutOfBoundsException("Index " + index + " is out of list bounds!");
		return myList.getList().get(index);
	}

	public MDIChild getMyListView() {
		return myListView;
	}

	public void setMyListView(MDIChild myListView) {
		this.myListView = myListView;
	}

	/**
	 * unregister with Warehouse list as observer
	 */
	public void unregisterAsObserver() {
		myList.deleteObserver(this);
	}

	//model tells this observer that it has changed
	//so tell JList's view to repaint itself now
	@Override
	public void update(Observable o, Object arg) {
		//System.out.println("WarehouseListController update");
		fireContentsChanged(this, 0, getSize());
		myListView.repaint();
	}
}
