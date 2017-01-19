package controller;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * Subclassed JInternalFrame to give us access to internalFrameClosing event
 * allowing us to tell MDIChild panel to clean up (e.g., unregister from observed, etc.)
 *
 */
public class MDIChildFrame extends JInternalFrame implements InternalFrameListener {
	/**
	 * MDI child panel
	 */
	protected MDIChild myChild;

	/**
	 * Constructor from JInternalFrame
	 * also sets myChild instance variable and places the child panel in the center (should work for most layouts)
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public MDIChildFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable
			, MDIChild child) {
		super(title, resizable, closable, maximizable, iconifiable);
		
		//child frame will close itself in the closing event
		//this allows us to prevent closing an unsaved child window
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		
		myChild = child;
		this.add(child, BorderLayout.CENTER);
		
		//add itself as a window listener
		this.addInternalFrameListener(this);
	}
	
	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}

	/**
	 * Frame's method for telling MDI Child to clean up
	 * Having this method public allows MDIParent to access the frame's child and tell it to close
	 */
	public void cleanup() {
		//System.out.println("     *** In MDIChildFrame.cleanup");
		myChild.cleanup();
	}
	
	/**
	 * If child view has changed, prompts to save (Yes/No/Cancel)
	 * If user clicks Cancel then returns false
	 * If user clicks Yes and save fails, returns false
	 * If user clicks No returns true
	 * If user clicks Yes and save ok, returns true
	 * @return true if child frame is ok to close, else false
	 */
	public boolean okToClose() {
		if(myChild.isChanged()) {
			int option = JOptionPane.showConfirmDialog(myChild.getMasterParent(), "Do you want to save your changes first?", "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION);
            if(option == JOptionPane.CANCEL_OPTION)
            	return false;
            //if Yes, try to save. if error then abort closing
            if(option == JOptionPane.YES_NO_OPTION) {
            	if(!myChild.saveModel())
            		return false;
            }
		}
		return true;
	}
	
	/**
	 * This is called when user clicks the close button on the child frame title bar
	 * Allows app to save the view first if it has changed and possibly abort the close 
	 */
	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		//if child has changed then ask to save if No then continue closing
		//if yes, tell child to save
		//if cancel then return
		//System.out.println("     *** In MDIChildFrame.internalFrameClosing");
		if(!okToClose())
			return;
		
		// tell child to clean up (e.g., remove from MDIParent's openViews list)
		cleanup();
		//close this window. only need to do this here because it automatically gets closed when closing the MDIFrame
		dispose();
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

}
