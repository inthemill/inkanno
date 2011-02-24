package ch.unibe.im2.inkanno.gui;

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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Polygon;
import java.util.List;

import ch.unibe.eindermu.utils.GraphicsBackup;
import ch.unibe.im2.inkanno.gui.color.ColorizerManager;
import ch.unibe.inkml.InkChannel;
import ch.unibe.inkml.InkTracePoint;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.TraceGraphVisitor;

public class GUITraceVisitor extends TraceGraphVisitor{
    public static final Color RED = Color.RED;
    public static final Color REDISH = new Color(255, 128, 128);
    public static final Color BLACK = Color.BLACK;
    public static final Color GRAY = Color.gray;
    
    private int depth = 0;
    
    private boolean selection = false;
    
    public void go(InkTraceView root){
        ColorizerManager.getInstance().setFilter(getTraceFilter());
    	GraphicsBackup gb = new GraphicsBackup(getGraphics());
    	getGraphics().setColor(BLACK);
    	selection = false;
    	root.accept(this);
    	getGraphics().setColor(RED);
    	selection = true;
    	GUI.getInstance().getCurrentDocument().getSelection().accept(this);
		gb.reset();	
    }
    
    private void paintContainer(InkTraceView c){
        if(!selection)
            getGraphics().setColor(GRAY);
        if(c.isEmpty() || c.isRoot()){
    		return;
    	}
    	getGraphics().draw(c.getBounds());
    }
    
    
    protected void paintLeaf(InkTraceViewLeaf s) {
        if(!selection){
            ColorizerManager.getInstance().setColor(getGraphics(),s);
        }
        BasicStroke stroke = new BasicStroke((float) getStrokeWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        if(s.getBrush() != null && s.getBrush().isEraser()) {
            getGraphics().setColor(getGraphics().getBackground());
            stroke = new BasicStroke((float) getStrokeWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
        getGraphics().setStroke(stroke);
        
        double w = getStrokeWidth();
        List<InkTracePoint> ps  = s.getPoints();
        //if the Channel Force is present, we change the stroke width according to the writing force
        if(s.getContext().getCanvasTraceFormat().containsChannel(InkChannel.ChannelName.F)){
            for(int i = 1; i<s.getPoints().size();i++){
                double f = (ps.get(i-1).get(InkChannel.ChannelName.F) + ps.get(i).get(InkChannel.ChannelName.F)) / 2.0;
                if(Double.isNaN(f)){
                	f = 127;
                }
                getGraphics().setStroke(new BasicStroke((float)(w*(0.5+(f/100.0))),BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                getGraphics().drawLine((int)ps.get(i-1).getX(),(int) ps.get(i-1).getY(), (int)ps.get(i).getX(), (int)ps.get(i).getY());
            }
        }else{
            Polygon p = s.getPolygon();
            getGraphics().drawPolyline(p.xpoints, p.ypoints, p.npoints);
        }
    }
    
	@Override
	public void visit(InkTraceViewContainer container) {
		if(pass(container)){
		    GraphicsBackup gb = new GraphicsBackup(getGraphics());
			paintContainer(container);
			gb.reset();
			gb = new GraphicsBackup(getGraphics());
			if(selection && depth >= 1){
				getGraphics().setColor(REDISH);
			}
	
			depth ++;
			container.delegateVisitor(this);
			depth --;
			gb.reset();
		}
	}
}
