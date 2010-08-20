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

package ch.unibe.im2.inkanno.importer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.unibe.eindermu.utils.XmlHandler;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
import ch.unibe.inkml.InkAnnoCanvas;
import ch.unibe.inkml.InkBrush;
import ch.unibe.inkml.InkCanvas;
import ch.unibe.inkml.InkCanvasTransform;
import ch.unibe.inkml.InkChannel;
import ch.unibe.inkml.InkChannelDouble;
import ch.unibe.inkml.InkChannelInteger;
import ch.unibe.inkml.InkContext;
import ch.unibe.inkml.InkDefinitions;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkInkSource;
import ch.unibe.inkml.InkMLComplianceException;
import ch.unibe.inkml.InkTrace;
import ch.unibe.inkml.InkTraceFormat;
import ch.unibe.inkml.InkTraceGroup;
import ch.unibe.inkml.InkTraceLeaf;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.ViewTreeManipulationException;

public class WhiteboardStrokeImporter extends XmlHandler implements StrokeImporter{
    
	/**
	 * List of strokes
	 */
    private NodeList nl;
    
    private InkTraceFormat format;
    private Map<String,InkBrush> brushes = new HashMap<String,InkBrush>();

	private InkCanvasTransform transform;

	private InkInkSource source;
	
	private InkInk ink;
	
	private InkContext context;

    
    public WhiteboardStrokeImporter(File file) throws IOException, InvalidDocumentException {
    	//Parse XML
        super.loadFromFile(file);
        //Retrieve Strokes
        this.nl = this.getDocument().getElementsByTagName("Stroke");
        if(this.nl.getLength() == 0) {
            throw new InvalidDocumentException("The document contains no strokes. It's makes no sense to open it");
        }
    }

    /**
     * Return each XML Stroke converted into StrokeObject/TraceObjects
     * @return A list of Strokes
     * @throws InkMLComplianceException 
     */
    private List<InkTraceLeaf> getStrokes() throws InkMLComplianceException {
        List<InkTraceLeaf> strokes = new LinkedList<InkTraceLeaf>();
        for(int i = 0; i < this.nl.getLength(); i++) {
            strokes.add(this.nodeToStroke(this.nl.item(i)));
        }
        return strokes;
    }
    
    /**
     * Convert XML node of a Stroke into StrokeObject/TraceObjects
     * @param strokeNode
     * @return
     * @throws InkMLComplianceException 
     */
    private InkTraceLeaf nodeToStroke(Node strokeNode) throws InkMLComplianceException {
    	Element s = (Element) strokeNode;
    	InkBrush b = getBrush((s.getAttribute("colour")!= null)?s.getAttribute("colour"):"");
		final InkTraceLeaf trace = new InkTraceLeaf(this.ink,null);
		this.ink.addTrace(trace);
		trace.setCurrentContext(context);
		if(b!=null){
			trace.setBrush(b);
		}
		
		
        final NodeList l = s.getElementsByTagName("Point");
        trace.addPoints(trace.new PointConstructionBlock(l.getLength()){
            @Override
            public void addPoints() {
                for(int i = 0;i<l.getLength();i++){
                    for(InkChannel c : trace.getSourceFormat()){
                        set(c.getName(), c.parseToDouble(((Element)l.item(i)).getAttribute(channelNameToS(c.getName()))));
                    }
                    next();
                }
            }});
        return trace;
    }
    
    private String channelNameToS(InkChannel.ChannelName name){
    	switch (name){
    	case X: return "x";
    	case Y :return "y";
    	case T :return "time";
    	}
    	return "";
    }
    
    public InkTraceGroup getContainer() {
        return null;
    }
    
    public boolean hasContainers() {
        return false;
    }
    
    public void importTo(ch.unibe.im2.inkanno.Document doc) throws InvalidDocumentException {
    	ink = new InkInk();
    	doc.setInk(ink);
    	
    	InkDefinitions definition = new InkDefinitions(ink);
    	ink.setDefinitions(definition);
    	
    	
        try {
            source = new InkInkSource(ink,"ebeamSource");
            source.setModel("eBeam");
            source.setManufacturer("Luidia");
            source.setDescription("http://www.e-beam.com");
            definition.enter(source);
            
	        format = new InkTraceFormat(ink,"whiteboardFormat");
	        InkChannel x = new InkChannelDouble(ink);
	        x.setName(InkChannel.ChannelName.X);
	        x.setOrientation(InkChannel.Orientation.P);
	        x.setFinal();
			format.addChannel(x);
			
	        InkChannel y = new InkChannelDouble(ink);
	        y.setName(InkChannel.ChannelName.Y);
	        y.setOrientation(InkChannel.Orientation.P);
	        y.setFinal();
	        format.addChannel(y);
	        
	        InkChannel t = new InkChannelDouble(ink);
	        t.setName(InkChannel.ChannelName.T);
	        t.setOrientation(InkChannel.Orientation.P);
	        t.setFinal();
	        format.addChannel(t);
	        
	        InkChannel f = new InkChannelInteger(ink);
	        f.setName(InkChannel.ChannelName.F);
	        f.setOrientation(InkChannel.Orientation.P);
	        f.setIntermittent(true);
	        f.setFinal();
	        format.addChannel(f);
        
	        definition.enter(format);
	        InkCanvas canvas = new InkAnnoCanvas(ink); 
	        definition.enter(canvas);
	        transform = InkCanvasTransform.getIdentityTransform(ink,"whiteboardToInkAnnoTransform",format,canvas.getTraceFormat());
	        definition.enter(transform);
	        
	        context = new InkContext(ink,"maincontext");
	        context.setInkSourceByRef(source);
	        context.setTraceFormat(format);
	        //context.setBrush(brush);
	        context.setCanvas(canvas);
	        context.setCanvasTransform(transform);
	        this.getStrokes();
	        
           for(InkTrace trace : ink.getTraces()){
                InkTraceViewLeaf l = new InkTraceViewLeaf(ink,ink.getViewRoot());
                l.setTraceDataRef(trace.getIdNow("t"));
                ink.getViewRoot().addTrace(l);
            }

        
        } catch (InkMLComplianceException e) {
			throw new InvalidDocumentException(e.getMessage());
		} catch (ViewTreeManipulationException e) {
            e.printStackTrace();
            throw new InvalidDocumentException("A View Tree ManipulationException has been raised, this should not happen.");
        }
        
    }
    
    private InkBrush getBrush(String color){
        if(brushes.containsKey(color)){
            InkBrush brush =  brushes.get(color);
            if(this.context.getBrush().equals(brush)){
                return null;
            }
            return brush;
        }else if(ink.getDefinitions().containsKey("brush_"+color)){
            InkBrush brush =  (InkBrush) ink.getDefinitions().get("brush_"+color);
            brushes.put(color,brush);
            if(this.context.getBrush().equals(brush)){
                return null;
            }
        }
        InkBrush b;
        try {
            b = new InkBrush(this.ink,"brush_"+color);
        } catch (InkMLComplianceException e) {
            //We have already tested, will not occures
            throw new Error(e);
        }
        if(color.equals("erase")){
            b.annotate(InkBrush.COLOR, InkBrush.COLOR_ERASER);
        }else{
            b.annotate(InkBrush.COLOR,color);
        }
        brushes.put(color,b);
        if(this.context.getBrush()==null){
            this.context.setBrush(b);
        }
        return getBrush(color);
    }
    
}
