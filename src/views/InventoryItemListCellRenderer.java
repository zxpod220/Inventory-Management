package views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import models.Parts;
import models.InventoryItem;

public class InventoryItemListCellRenderer implements ListCellRenderer<InventoryItem> {

	/**
	 * Can use default rendered to keep the visual parts I like (e.g., row height, highlight color, etc.)
	 */
	private final DefaultListCellRenderer DEFAULT_RENDERER = new DefaultListCellRenderer();
	
	private JButton button;
	
	@Override
	public Component getListCellRendererComponent(JList<? extends InventoryItem> list, InventoryItem value, int index,
			boolean isSelected, boolean cellHasFocus) {

		JLabel renderer = (JLabel) DEFAULT_RENDERER.getListCellRendererComponent(list, value.getPartId(), index, isSelected, cellHasFocus);
		return renderer;
	}

	
}
