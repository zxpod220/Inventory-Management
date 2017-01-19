package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Custome menu to use in the MDIParent 
 *
 */
public class MDIMenu extends JMenuBar {
	/**
	 * Containing JFrame
	 */
	private MDIParent parent;
	
	public MDIMenu(MDIParent p) {
		super();
		
		this.parent = p;
		
		JMenu menu = new JMenu("File");
		JMenuItem menuItem = new JMenuItem("Quit");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.doCommand(MenuCommands.APP_QUIT, null);
			}
		});
		menu.add(menuItem);
		this.add(menu);
		
		menu = new JMenu("Warehouse");
		menuItem = new JMenuItem("Warehouse List");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.doCommand(MenuCommands.SHOW_LIST_WAREHOUSE, null);
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Add Warehouse");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.doCommand(MenuCommands.ADD_WAREHOUSE, null);
			}
		});
		menu.add(menuItem);
		this.add(menu);		

		menu = new JMenu("Parts");
		menuItem = new JMenuItem("Parts List");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.doCommand(MenuCommands.SHOW_LIST_PARTS, null);
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Add Part");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.doCommand(MenuCommands.ADD_PARTS, null);
			}
		});
		menu.add(menuItem);
		this.add(menu);		

		menu = new JMenu("InventoryItem");
		menuItem = new JMenuItem("InventoryItem List");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.doCommand(MenuCommands.SHOW_LIST_INVENTORY, null);
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Add InventoryItem");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.doCommand(MenuCommands.ADD_INVENTORY, null);
			}
		});
		menu.add(menuItem);
		this.add(menu);	

		// for User
				menu = new JMenu("User");
				menuItem = new JMenuItem("Users");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						parent.doCommand(MenuCommands.SHOW_LIST_USERS, null);
					}
				});
				menu.add(menuItem);

				menuItem = new JMenuItem("Add User");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						parent.doCommand(MenuCommands.ADD_USER, null);
					}
				});
				menu.add(menuItem);
				this.add(menu);
				
				// for access
				
				menu = new JMenu("Access");
				menuItem = new JMenuItem("Login");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						parent.doCommand(MenuCommands.SHOW_LOGIN, null);
					}
				});
				menu.add(menuItem);	
				
				menuItem = new JMenuItem("Logout");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						parent.doCommand(MenuCommands.SHOW_LOGOUT, null);
					}
				});
				menu.add(menuItem);	
	
		this.add(menu);
		
		this.add(menu);
		
		menu = new JMenu("Reports");
		menuItem = new JMenuItem("PDF Report");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.doCommand(MenuCommands.REPORT_PDF, null);
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Excel Report");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.doCommand(MenuCommands.REPORT_EXCEL, null);
			}
		});
		menu.add(menuItem);

		this.add(menu);	
	}
}
