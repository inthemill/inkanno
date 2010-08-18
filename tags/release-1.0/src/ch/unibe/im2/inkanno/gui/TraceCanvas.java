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
package ch.unibe.im2.inkanno.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

import ch.unibe.eindermu.utils.AbstractObservable;
import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.GraphicsBackup;
import ch.unibe.eindermu.utils.Observable;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.Selection;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.util.AbstractTraceFilter;
import ch.unibe.inkml.util.TraceGraphVisitor;

@SuppressWarnings("serial")
public class TraceCanvas extends Component implements Observable, Observer{
    
    public static final Aspect ON_PAINT = new Aspect(){};

	private TraceGraphVisitor strokeVisitor;
    
    private AffineTransform af;
    
    private JPanel parent;
    
    public int w, h;
    
    private AbstractObservable observerSupporter = new AbstractObservable();
    
    public double factor;
    
    //private Document currentDocument;
    
    public TraceCanvas() {
        this(null,null);
    }
    public TraceCanvas(GUI gui, JPanel parent) {
        this.parent = parent;
        this.setPreferredSize(new Dimension(500, 500));
        this.w = 500;
        this.h = 500;
        TraceGraphVisitor tr = new GUITraceVisitor();
        tr.addTraceFilter(new AbstractTraceFilter(){
			@Override
			public boolean pass(InkTraceView view) {
				return getDocument().getTraceFilter().pass(view);
			}});
        setTraceVisitor(tr);
        if(gui!=null){
            gui.getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_SWITCH, this);
            gui.getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_PRE_SWITCH, this);
        }
        
    }
    
    public TraceGraphVisitor getTraceVisitor() {
		return strokeVisitor;
	}
	public void setTraceVisitor(TraceGraphVisitor strokeVisitor) {
		this.strokeVisitor = strokeVisitor;
	}
    
    public void notifyFor(Aspect event, Object subject) {
        if(event == Selection.ON_CHANGE || event == InkTraceView.ON_CHANGE){
            repaint();
        }else if(event == DocumentManager.ON_DOCUMENT_PRE_SWITCH){
            if(hasDocument()){
                getDocument().getCurrentViewRoot().unregisterFor(InkTraceView.ON_CHANGE, this);
                getDocument().getSelection().unregisterFor(Selection.ON_CHANGE, this);
            }
        }else if(event == DocumentManager.ON_DOCUMENT_SWITCH){
            Document d = ((Document)subject);
            calculate();
            if (d != null){
                d.getCurrentViewRoot().registerFor(InkTraceView.ON_CHANGE, this);
                d.getSelection().registerFor(Selection.ON_CHANGE, this);
                
            }
        }
    }
    
    
    public boolean hasDocument(){
        return GUI.getInstance().hasDocument();
    }
    
    public Document getDocument(){
        return GUI.getInstance().getCurrentDocument();
    }
    
    public double zoom(boolean in) {
        double f = (in)?4/3.0:3/4.0;
        this.w = (int) (this.w * f);
        this.h = (int) (this.h * f);
        calculate();
        return f;
    }
    
    public void calculate() {
        if(hasDocument()) {
            Rectangle bound = getDocument().getBounds();
            factor = Math.min((this.h - 25) / (double) bound.height, (this.w - 10) / (double) bound.width);
            this.setPreferredSize(new Dimension((int) (bound.width * factor) + 10, (int) (bound.height * factor) + 25));
            
            af = AffineTransform.getTranslateInstance(5, 20);
            af.concatenate(AffineTransform.getScaleInstance(factor, factor));
            af.concatenate(AffineTransform.getTranslateInstance(-bound.x, -bound.y));
            if(getDocument().isVMirroring()) {
                af.concatenate(AffineTransform.getScaleInstance(1, -1));
                af.concatenate(AffineTransform.getTranslateInstance(0, -(2 * bound.y + bound.height)));
            }
            if(getDocument().isHMirroring()) {
                af.concatenate(AffineTransform.getScaleInstance(-1, 1));
                af.concatenate(AffineTransform.getTranslateInstance(-(2 * bound.x + bound.width), 0));
            }
        } else {
            this.h = 500;
            this.w = 500;
            this.setPreferredSize(new Dimension(500, 500));
        }
        
        if(parent!=null){
            parent.revalidate();
        }
        repaint();
    }
    
    public void paint(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        GraphicsBackup gb = new GraphicsBackup(g);
        if(hasDocument() && af != null) {
            g.drawString(getDocument().getName(), 5, 15);
            g.transform(af);
            strokeVisitor.setStrokeWidth(getStrokeWeight());
            strokeVisitor.setGraphics(g);
            //strokeVisitor.visitAll(getDocument().getVirtualViews());
            strokeVisitor.go(getDocument().getCurrentViewRoot());
        }
        gb.reset();
        this.observerSupporter.notifyObserver(ON_PAINT,g);
    }
    
    public AffineTransform getAffineTransform() {
        return this.af;
    }
    
    public Point transform(final int x, final int y) {
        Point2D ptDst = new Point2D.Double();
        this.getAffineTransform().transform(new Point2D(){
            public double getX() {
                return (double) x;
            }
            
            public double getY() {
                return (double) y;
            }
            
            public void setLocation(double x, double y) {}
        }, ptDst);
        return new Point((int) ptDst.getX(), (int) ptDst.getY());
    }
    
    public Point retransform(final int x, final int y) {
        Point2D ptDst = new Point2D.Double();
        try {
            this.getAffineTransform().inverseTransform(new Point2D(){
                public double getX() {
                    return (double) x;
                }
                
                public double getY() {
                    return (double) y;
                }
                
                public void setLocation(double x, double y) {}
            }, ptDst);
        } catch(NoninvertibleTransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new Point((int) ptDst.getX(), (int) ptDst.getY());
    }

    public void registerFor(Aspect event, Observer o) {
        this.observerSupporter.registerFor(event, o);
    }
    public double getStrokeWeight() {
        return GUI.getInstance().getCurrentDocumentView().getStrokeWidth();
    }

}
