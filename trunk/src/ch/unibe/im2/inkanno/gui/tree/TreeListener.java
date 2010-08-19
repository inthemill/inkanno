/*
 * Created on 23.07.2007
 *
 * Copyright (C) 2007  Emanuel Indermühle <emanuel@inthemill.ch>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @author emanuel
 */
package ch.unibe.im2.inkanno.gui.tree;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import ch.unibe.im2.inkanno.Selection;

public class TreeListener implements MouseListener, TreeSelectionListener, MouseMotionListener{
    
    private static TreeListener instance;
    
	private JPopupMenu popup;

	private TreePath popupSelection;
    
    public static TreeListener getInstance(JPopupMenu popup) {
        if(instance == null) {
            instance = new TreeListener(popup);
        }
        return instance;
    }
	public static TreeListener getInstance() {
		if(instance == null) {
            throw new NullPointerException("No Popup specified");
        }
        return instance;
	}
    
    private TreeListener(JPopupMenu popup) {
    	this.popup = popup;
    }
    
    public void mouseClicked(MouseEvent e) {

    }
    
    public void mouseEntered(MouseEvent e) {}
    
    public void mouseExited(MouseEvent e) {}
    
    public void mousePressed(MouseEvent e) {
    	maybeShowPopup(e);
    }
    
    public void mouseReleased(MouseEvent e) {
    	maybeShowPopup(e);
    }

    
    private void maybeShowPopup(MouseEvent e) {
		if(e.isPopupTrigger()){
			JTree tree = (JTree) e.getSource();
			this.popupSelection = tree.getClosestPathForLocation(e.getX(), e.getY());
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
		
	}
    
    public void valueChanged(TreeSelectionEvent e) {
        ((JTree)e.getSource()).scrollPathToVisible(e.getPath());
    }

	public void setSelectionContent(Selection s,JTree tree) {
	}

	
	public TreePath getPopUpSelection() {
		return popupSelection;
	}
    @Override
    public void mouseDragged(MouseEvent e) {
        System.err.println(e.getY());
        
    }
    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
}