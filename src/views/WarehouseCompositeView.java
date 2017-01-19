package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import controller.MDIChild;
import controller.MDIParent;
import controller.WarehouseListController;
import models.Warehouse;


public class WarehouseCompositeView extends MDIChild {
private WarehouseListController listController;
	
	private WarehouseListView listView;
	private WarehouseDetailView detailView;
	
	public WarehouseCompositeView(String title, WarehouseListController lc, MDIParent m) {
		super(title, m);
		
		listController = lc;
		
		//this panel contains a list panel in West area and a detail panel in Central area
		//I think BorderLayout is the default but why guess
		this.setLayout(new BorderLayout());
		
		//JPanel westPanel = new JPanel();
		//westPanel.setBackground(Color.RED);
		listView = new WarehouseListView(title, listController, m);
		listView.getlistWarehouse().addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				//if(e.getClickCount() == 1) {
					int index = listView.getlistWarehouse().locationToIndex(e.getPoint());
					//get the Warehouse at that index
					Warehouse p = listController.getElementAt(index);

					//open a new detail view
					detailView.setMyWarehouse(p);
					detailView.refreshFields();
				//}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//westPanel.setPreferredSize(new Dimension(200, 200));
		this.add(listView, BorderLayout.WEST);
		
		//westPanel.setPreferredSize(new Dimension(200, 200));
		
		//HACK
		Warehouse p = listController.getElementAt(0);
    	detailView = new WarehouseDetailView(p.getFullName(), p, m);
		this.add(detailView, BorderLayout.CENTER);
		
		this.setPreferredSize(new Dimension(500, 280));
	}

}
