package controller;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import database.PartsTableGateway;
import database.PartsTableGatewayMySQL;
import database.WarehouseTableGateway;
import database.WarehouseTableGatewayMySQL;
import database.PartsTableGateway;
import database.PartsTableGatewayMySQL;
import database.GatewayException;
import database.InventoryItemTableGateway;
import database.InventoryItemTableGatewayMySQL;
import database.WarehouseTableGateway;
import database.WarehouseTableGatewayMySQL;
import database.UserTableGateway;
import database.UserTableGatewayMySQL;
import models.InventoryItemList;
import models.PartsList;
import models.UserList;
import models.PartsList;
import models.WarehouseList;
import models.WarehouseList;


public class BlastOff {
	/**
	 * Configures and Launches initial view(s) of the application on the Event Dispatch Thread
	 */
	
	//Created by Dominik Garcia xzb387
	public static void createAndShowGUI() {		
		//create a model table gateways; abort if fails (need a db connection)
		WarehouseTableGateway ptg = null;
		PartsTableGateway dtg = null;
		InventoryItemTableGateway itg = null;
		UserTableGateway utg = null;
		try {
			//ptg = new WarehouseTableGateway();
			ptg = new WarehouseTableGatewayMySQL();
			dtg = new PartsTableGatewayMySQL();
			itg = new InventoryItemTableGatewayMySQL();
			utg = new UserTableGatewayMySQL();
			//ptg = new WarehouseTableGatewayRedis();
			
		} catch (GatewayException e) {
			JOptionPane.showMessageDialog(null, "Database is not responding. Please reboot your computer and maybe the database will magically appear (not really).", "Database Offline!", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		//init model(s); do an initial load from gateways 
		WarehouseList WarehouseList = new WarehouseList();
		WarehouseList.setGateway(ptg);
		WarehouseList.loadFromGateway();
		
		PartsList PartsList = new PartsList();
		PartsList.setGateway(dtg);
		PartsList.loadFromGateway();
		
		InventoryItemList inventoryitemList = new InventoryItemList();
		inventoryitemList.setGateway(itg);
		inventoryitemList.loadFromGateway();
		
		UserList userList = new UserList();
		userList.setGateway(utg);
		userList.loadFromGateway();
		
		
		MDIParent appFrame = new MDIParent("CS 4743 Assign5", WarehouseList, PartsList, inventoryitemList, userList);
		
		//use exit on close if you only want windowClosing to be called (can abort closing here also)
		//appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//use dispose on close if you want windowClosed to also be called
		appFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		//need to set initial size of MDI frame
		appFrame.setSize(640, 480);
		
		appFrame.setVisible(true);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
