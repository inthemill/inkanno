/*
 * Created on 25.02.2008
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
 * 
 * @author emanuel
 * StrokeFilter.java
 */
package ch.unibe.im2.inkanno.filter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.HashMap;
import java.util.List;

import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkTracePoint;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.AbstractTraceFilter;
import ch.unibe.inkml.util.Timespan;
import ch.unibe.inkml.util.TraceBound;

/**
 * This filter passes only the trace which have been created in the given timespan.
 * Additionally the traces must not be coverd by an erasor stroke which has been created
 * in the given timespan.
 * @author emanuel
 *
 */

public class TimeSpanEraserFilter extends AbstractTraceFilter{
    private double startTime = 0;
    private double endTime = Double.MAX_VALUE;
    private Document doc;
    private HashMap<InkTraceViewLeaf, BufferedImage> buffs = new HashMap<InkTraceViewLeaf, BufferedImage>();
    private float factor;
    
    private List<InkTraceView> selectable;
    private InkInk inkCache;
    
    public TimeSpanEraserFilter(Document document){
        this.doc = document;
        this.generateErasers();
        doc.getCurrentViewRoot().registerFor(InkTraceView.ON_CHANGE, new Observer(){
            public void notifyFor(Aspect event, Object subject) {
                selectable = filterSelectableStrokes();
            }
        });
        Timespan t = doc.getInk().getTimeSpan();
        startTime = t.start;
        endTime = t.end;
        selectable = filterSelectableStrokes();
        inkCache = doc.getInk();
        
    }
    
    
    public void setCurrentTimeEnd(int value) {
        endTime = value;
        this.selectable = filterSelectableStrokes();
    }
    
    public void setCurrentTimeStart(int value) {
        startTime = value;
        this.selectable = filterSelectableStrokes();
    }
    
    public double getCurrentTimeEnd() {
        return endTime;
    }
    
    public double getCurrentTimeStart() {
        return startTime;
    }
    
    public List<InkTraceView> getSelectableStrokes() {
        return this.selectable;
    }
    
    
    
    private void generateErasers() {
        TraceBound b = doc.getInk().getBounds();
        factor = Math.min(490 / (float) b.width, 490 / (float) b.height);
        AffineTransform af = AffineTransform.getScaleInstance(factor, factor);
        af.concatenate(AffineTransform.getTranslateInstance(-b.x, -b.y));
        byte[] arr = new byte[] { (byte) 0, (byte) 255 };
        for(InkTraceView s : doc.getInk().getViewRoot().getFlattenedTraceLeafs()) {
            if(s.getBrush()!= null && s.getBrush().isEraser()) {
                BufferedImage bf = new BufferedImage(500, 500, BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1, 2, arr,arr,arr));
                Graphics2D g = (Graphics2D) bf.getGraphics();
                g.setTransform(af);
                BasicStroke stroke = new BasicStroke((float) 500.0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                g.setColor(Color.WHITE);
                g.setStroke(stroke);
                g.drawPolyline(s.getPolygon().xpoints, s.getPolygon().ypoints, s.getPolygon().npoints);
                buffs.put((InkTraceViewLeaf) s, bf);
            }
        }
    }
    
    public List<InkTraceView> filterSelectableStrokes() {
    	return  filter(doc.getFlattenedViews()); 
    }    

	/**
     * Filters the list to leave only the selectable traces.
     * @param list
     * @return
     */
  
    private boolean passTimeConstraint(InkTraceView s){
        return !(s.getTimeSpan()==null || (s.getTimeSpan().start > endTime || s.getTimeSpan().end < startTime));
    }
    
    private boolean isEraser(InkTraceView s){
        return s.getBrush()!= null && s.getBrush().isEraser();
    }
    
    
	public boolean pass(InkTraceView s) {
	    // is no eraseStroke
	    if(isEraser(s)) {
            return false;
        }
	    // is in requested timeframe
		if(!passTimeConstraint(s)){
		    return false;
		}
		// if containers: ok
        if(!s.isLeaf()){
            return true;
        }
        // for leafes:
        // remove the trace that are erased by the eraser traces. 
        float toErase = 0;
        int[] is = new int[1];
        //for each eraser trace
        for(InkTraceViewLeaf eraser : buffs.keySet()) {
            //if this eraser trace is applicable
            if(passTimeConstraint(eraser) && eraser.getTimeSpan().start > s.getTimeSpan().end) {
                BufferedImage bf = buffs.get(eraser);
                //for each point in the stroke
                for(InkTracePoint p : ((InkTraceViewLeaf) s).pointIterable()) {
                    bf.getRaster().getPixel((int) ((p.getX() - s.getBounds().x) * factor), (int) ((p.getY() - s.getBounds().y) * factor), is);
                    if(is[0] != 0) {
                        toErase += 1 / (float) ((InkTraceViewLeaf) s).getPoints().size();
                    }
                }
                if(toErase > 0.5) {
                    break;
                }
            }
        }
        return toErase <= 0.5;
	}

	public List<InkTraceView> filter(InkInk ink){
        if(inkCache != ink){
            return super.filter(ink);
        }else{
            return getSelectableStrokes();
        }
    }
}
