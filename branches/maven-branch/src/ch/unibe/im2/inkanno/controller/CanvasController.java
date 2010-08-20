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

package ch.unibe.im2.inkanno.controller;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.NotImplementedException;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.Selection;
import ch.unibe.im2.inkanno.gui.GUI;
import ch.unibe.im2.inkanno.gui.TraceCanvas;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.util.TraceBound;
import ch.unibe.inkml.util.ViewTreeManipulationException;

public class CanvasController implements MouseListener, MouseMotionListener, KeyListener, Observer{
    
    private TraceCanvas canvas;
    
    private GUI gui;
    
    private DragBox mouseBox;
    
    public CanvasController(GUI gui, TraceCanvas canvas) {
        this.gui = gui;
        this.canvas = canvas;
        this.canvas.registerFor(TraceCanvas.ON_PAINT, this);
    }
    
    
    public void notifyFor(Aspect event,Object subject) {
        Graphics2D g = (Graphics2D) subject;
        if(this.mouseBox != null) {
            g.setColor(Color.RED);
            g.draw(this.mouseBox);
        }
    }
    
    private Selection getSelection() {
        return this.gui.getCurrentDocument().getSelection();
    }
    
    public void mouseClicked(MouseEvent e) {
        if(!this.gui.hasDocument()) {
            return;
        }
        InkTraceView trace = this.findStroke(e.getX(), e.getY());
        if(trace == null) {
            return;
        }
        if(e.getButton() == MouseEvent.BUTTON3) {
            try {
                Double.valueOf(JOptionPane.showInputDialog(this.gui, "Please enter the time from the beginning of the meeting" + "\n until this stroke was written, in Milliseconds"));
                //TODO this.gui.getCurrentDocument().getInk().setTime(stroke, time);
                throw new NotImplementedException();
            } catch(NumberFormatException ex) {
                JOptionPane.showMessageDialog(this.gui, "You have to enter a number");
            } catch(NullPointerException ex) {
                // its ok;
            }
            return;
        }
        if(e.getButton() == MouseEvent.BUTTON1 && e.isControlDown()) {
            this.getSelection().toggle(trace);
        } else {
            if(e.isShiftDown()) {
                this.getSelection().selectBetween(trace, this.gui.getCurrentDocument().getInk().getFlatTraceViewLeafs());
            } else {
                this.getSelection().replace(trace);
            }
        }
        this.gui.getControlPanel().grabInputFocus();
    }
    
    private InkTraceView findStroke(int px, int py) {
        Point p = this.canvas.retransform(px, py);
        InkTraceView stroke = null;
        double dist = Double.MAX_VALUE;
        double near = this.gui.getCurrentDocument().getMostCommonTraceHeight();
        for(InkTraceView s : this.gui.getCurrentDocument().getTraceFilter().getSelectableStrokes()) {
            if(s.isLeaf()) {
                double d = s.distance(p);
                if(d < dist) {
                    stroke = s;
                    dist = d;
                }
            } else {
            	TraceBound r = s.getBounds();
                if(r.growNew(near,near).contains(p)) {
                    int d = r.distanceToPoints(p);
                    if(d < dist) {
                        stroke = s;
                        dist = d;
                    }
                }
            }
        }
        return stroke;
    }
    
    public void mouseEntered(MouseEvent e) {
    // TODO Auto-generated method stub
    
    }
    
    public void mouseExited(MouseEvent e) {
    // TODO Auto-generated method stub
    
    }
    
    public void mousePressed(MouseEvent e) {
        this.mouseBox = new DragBox(e.getPoint());
        this.mouseBox.add = (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown()) || (e.getButton() == MouseEvent.BUTTON3 && !e.isControlDown());
        
    }
    
    public void mouseReleased(MouseEvent e) {
        if(!this.gui.hasDocument()) {
            this.mouseBox = null;
            return;
        }
        if(this.mouseBox.dragged) {
            Rectangle testrect = new Rectangle(this.canvas.retransform(this.mouseBox.x, this.mouseBox.y));
            testrect.add(this.canvas.retransform(this.mouseBox.x + this.mouseBox.width, this.mouseBox.y + this.mouseBox.height));
            if(!this.mouseBox.add) {
                this.getSelection().clear();
            }
            List<InkTraceView> l = new ArrayList<InkTraceView>();
            for(InkTraceView s : this.gui.getCurrentDocument().getTraceFilter().getSelectableStrokes()) {
                if(s.isLeaf() && testrect.contains(s.getCenterOfGravity())) {
                    l.add(s);
                }
            }
            if(l.size() > 0){
                this.getSelection().addAll(l,l.get(l.size()-1));
            }
        }
        this.mouseBox = null;
    }
    
    public void mouseDragged(MouseEvent e) {
        this.mouseBox.dragged = true;
        this.mouseBox.move(e.getPoint());
        this.canvas.repaint();
        
    }
    
    public void mouseMoved(MouseEvent e) {
    // TODO Auto-generated method stub
    
    }
    
    @SuppressWarnings("serial")
    private class DragBox extends Rectangle{
        public boolean dragged;
        
        public Point start;
        
        public boolean add;
        
        public DragBox(Point point) {
            super(point);
            this.start = point;
        }
        
        public void move(Point point) {
            this.setBounds(start.x, start.y, 0, 0);
            this.add(point);
        }
        
    }
    
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_DOWN && e.isControlDown()) {
            JComboBox c = this.gui.getControlPanel().getComboBox();
            if(c.getItemCount() > 1){
                c.setSelectedIndex((c.getSelectedIndex() + 1) % (c.getItemCount()));
                e.consume();
            }
        }
        if(e.getKeyCode() == KeyEvent.VK_UP && e.isControlDown()) {
            JComboBox c = this.gui.getControlPanel().getComboBox();
            c.setSelectedIndex(((c.getItemCount() + c.getSelectedIndex()) - 1) % (c.getItemCount()));
            e.consume();
        }
        if(!this.gui.hasDocument()) {
            return;
        }
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            this.addGroup();
            e.consume();
        }
        if(e.getKeyCode() == KeyEvent.VK_RIGHT && e.isControlDown()) {
            this.getSelection().addNext();
            e.consume();
        }
        if(e.getKeyCode() == KeyEvent.VK_LEFT && e.isControlDown()) {
            this.getSelection().removeLast();
            e.consume();
        }
        
    }
    
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_RIGHT && e.isControlDown()) {
            e.consume();
        }
        if(e.getKeyCode() == KeyEvent.VK_LEFT && e.isControlDown()) {
            e.consume();
        }
    }
    
    public void keyTyped(KeyEvent e) {

    }
    
    /**
     * Creates a traceViewContainer containing the selected trace views and add it to the view tree.
     */
    public void addGroup() {
        String type = (String) this.gui.getControlPanel().getComboBox().getSelectedItem();
        try{
	        this.getSelection().labelSelection(this.gui.getControlPanel().getInput(), type);
	        this.gui.getControlPanel().clearInputLabel();
	    } catch (ViewTreeManipulationException e) {
            JOptionPane.showMessageDialog(
                    GUI.getInstance(),
                    String.format("Could add this group: \n %s", e.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
        }
        this.gui.getControlPanel().grabInputFocus();
    }
    
}
