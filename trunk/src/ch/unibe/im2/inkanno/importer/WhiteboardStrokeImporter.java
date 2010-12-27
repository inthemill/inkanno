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
import ch.unibe.inkml.InkAffineMapping;
import ch.unibe.inkml.InkAnnoCanvas;
import ch.unibe.inkml.InkBrush;
import ch.unibe.inkml.InkCanvas;
import ch.unibe.inkml.InkCanvasTransform;
import ch.unibe.inkml.InkChannel;
import ch.unibe.inkml.InkChannelDouble;
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
import ch.unibe.inkml.util.TraceViewTreeManipulationException;

public class WhiteboardStrokeImporter extends XmlHandler implements StrokeImporter{
    
	/**
	 * List of strokes
	 */
    
    private InkTraceFormat format;
    private Map<String,InkBrush> brushes = new HashMap<String,InkBrush>();

	private InkCanvasTransform transform;

	private InkInkSource source;
	
	protected InkInk ink;
	
	private InkContext context;

    
    public WhiteboardStrokeImporter(File file) throws IOException, InvalidDocumentException {
    	//Parse XML
        super.loadFromFile(file);
        //Retrieve Strokes
        
        if(getDocument().getElementsByTagName("Stroke").getLength() == 0) {
            throw new InvalidDocumentException("The document contains no strokes. It's makes no sense to open it");
        }
    }

    /**
     * Return each XML Stroke converted into StrokeObject/TraceObjects
     * @return A list of Strokes
     * @throws InkMLComplianceException 
     */
    private List<InkTraceLeaf> getTraces() throws InkMLComplianceException {
        List<InkTraceLeaf> strokes = new LinkedList<InkTraceLeaf>();
        NodeList nl = this.getDocument().getElementsByTagName("Stroke");
        for(int i = 0; i < nl.getLength(); i++) {
            strokes.add(this.nodeToTrace(nl.item(i)));
        }
        return strokes;
    }
    
    /**
     * Convert XML node of a Stroke into StrokeObject/TraceObjects
     * @param strokeNode
     * @return
     * @throws InkMLComplianceException 
     */
    protected InkTraceLeaf nodeToTrace(Node strokeNode) throws InkMLComplianceException {
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
    
    public void initializeDefinitions() throws InkMLComplianceException{
    	InkDefinitions definition = new InkDefinitions(ink);
    	ink.setDefinitions(definition);
    	
        source = new InkInkSource(ink,"ebeamSource");
        source.setModel("eBeam");
        source.setManufacturer("Luidia");
        source.setDescription("http://www.e-beam.com");
        definition.enterElement(source);
        
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
        
        format.setFinal();
        
        definition.enterElement(format);
        InkCanvas canvas = new InkAnnoCanvas(ink); 
        definition.enterElement(canvas);
        transform = new InkCanvasTransform(ink);
        InkAffineMapping mapping = InkAffineMapping.createIdentityInkAffinMapping(ink,format,canvas.getTraceFormat());
        transform.setForewardMapping(mapping);
        
        //transform = InkCanvasTransform.getIdentityTransform(ink,"whiteboardToInkAnnoTransform",format,canvas.getTraceFormat());
        definition.enterElement(transform);
        
        context = new InkContext(ink,"maincontext");
        context.setInkSourceByRef(source);
        context.setTraceFormat(format);
        //context.setBrush(brush);
        context.setCanvas(canvas);
        context.setCanvasTransform(transform);
        ink.setCurrentContext(context);
    } 
    
    
    public void importTo(ch.unibe.im2.inkanno.Document doc) throws InvalidDocumentException {
    	ink = new InkInk();
    	doc.setInk(ink);
    	try {
    		initializeDefinitions();
    	
			this.getTraces();
		
        
			for(InkTrace trace : ink.getTraces()){
				((InkTraceLeaf) trace).createView(ink.getViewRoot());
			}
        } catch (InkMLComplianceException e) {
			throw new InvalidDocumentException(e.getMessage());
		} catch (TraceViewTreeManipulationException e) {
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
