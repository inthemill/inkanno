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

package ch.unibe.im2.inkanno;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.unibe.eindermu.utils.XmlHandler;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
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
import ch.unibe.inkml.InkTraceFormat;
import ch.unibe.inkml.InkTraceLeaf;
import ch.unibe.inkml.InkTracePoint;
import ch.unibe.inkml.InkTracePoint;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.InkChannel.Name;
import ch.unibe.inkml.util.ViewTreeManipulationException;
public class InkAnnoStrokeImporter extends XmlHandler implements StrokeImporter{
    
    private String defaultColor = "blue";
    private InkTraceFormat sourceFormat;
    private Map<String,InkBrush> brushes = new HashMap<String,InkBrush>();
	private InkCanvasTransform transform;
	private InkInkSource inkSource;
	private InkInk ink;
	private InkDefinitions definition;
	private InkContext context;
    
    public InkAnnoStrokeImporter(File file) throws IOException {
        super.loadFromFile(file);
    }
    
    public File getSourceFile() {
        Element sf = (Element) this.getDocument().getElementsByTagName("Sourcefile").item(0);
        File af = new File(sf.getAttribute("absolutepath"));
        File rf = new File(sf.getAttribute("relativepath"));
        if(af.exists()) {
            return af;
        }
        return rf;
    }
    
    
    private void loadStrokes() throws InkMLComplianceException {
        Node strokes = this.getDocument().getElementsByTagName("Strokes").item(0);
        Node s = strokes.getFirstChild();
        while(s != null) {
            if(s.getNodeName().toLowerCase().equals("stroke")) {
            	this.nodeToStroke((Element) s);
                //this.strokes.put(((Element) s).getAttribute("id"), stroke);
            }
            s = s.getNextSibling();
        }
    }
    
    private InkTraceLeaf nodeToStroke(final Element s) throws InkMLComplianceException {
    	InkBrush b = getBrush((s.getAttribute("color")!= null)?s.getAttribute("color"):this.defaultColor);
    	InkTraceLeaf trace = new InkTraceLeaf(ink,null);
    	trace.setId(s.getAttribute("id"));
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
                    for(InkChannel c : sourceFormat){
                        set(c.getName(), c.parseToDouble(((Element)l.item(i)).getAttribute(channelNameToS(c.getName()))));
                    }
                    next();
                }
            }});
        return trace;
    }
    
    private String channelNameToS(InkChannel.Name name){
    	switch (name){
    	case X: return "x";
    	case Y :return "y";
    	case T :return "time";
    	}
    	return "";
    }
    
    
    private void loadContainers() throws ViewTreeManipulationException {
        Node translation = this.getDocument().getElementsByTagName("Transcription").item(0);
        InkTraceViewContainer root = new InkTraceViewContainer(ink,null);
        ink.addView(root);
        this.loadContainer((Element) translation, root);
        //root.releaseEvents();
	}
    
    private void loadContainer(Element parent, InkTraceViewContainer container) throws ViewTreeManipulationException {
        Node child = parent.getFirstChild();
        while(child != null) {
            if(child.getNodeName().toLowerCase().equals("attribute")) {
                container.annotate(((Element) child).getAttribute("type"), child.getTextContent());
            } else if(child.getNodeName().toLowerCase().equals("strokeref")) {
            	InkTraceViewLeaf l = new InkTraceViewLeaf(ink,container);
            	l.setTraceDataRef(child.getTextContent());
            	container.addTrace(l);
            } else if(child.getNodeName().toLowerCase().equals("item")) {
                InkTraceViewContainer cc = new InkTraceViewContainer(ink,container);
                this.loadContainer((Element) child, cc);
                container.addTrace(cc);
                
            }
            child = child.getNextSibling();
        }
    }
    
    public void importTo(Document doc) throws InvalidDocumentException {
    	
       	ink = new InkInk();
    	doc.setInk(ink);
    	try{
    	definition = new InkDefinitions(ink);
    	ink.setDefinitions(definition);
    	
    	inkSource = new InkInkSource(ink,"Version 1.0");
    	inkSource.setModel("InkAnno");
    	inkSource.setManufacturer("iam.unibe.ch/fki/inkanno");
    	//inkSource.setDescription("");
        definition.enter(inkSource);
        
        sourceFormat = new InkTraceFormat(ink,"inkannoTraceFormat");
        InkChannel x = new InkChannelDouble(ink);
        x.setName(InkChannel.Name.X);
        sourceFormat.addChannel(x);
        
        InkChannel y = new InkChannelDouble(ink);
        y.setName(InkChannel.Name.Y);
        sourceFormat.addChannel(y);
        
        InkChannel t = new InkChannelDouble(ink);
        t.setName(InkChannel.Name.T);
        sourceFormat.addChannel(t);
        
        /*InkChannel f = new InkChannelInteger(ink);
        f.setName(InkChannel.Name.F);
        sourceFormat.addIntermittentChannel(f);
        */
        definition.enter(sourceFormat);
        InkCanvas canvas = InkCanvas.createInkAnnoCanvas(ink); 
        definition.enter(canvas);
        transform = InkCanvasTransform.getIdentityTransform(ink,"InkAnnoV1_2_InkAnnoV2Transform",sourceFormat,canvas.getTraceFormat());
        definition.enter(transform);
        
        context = new InkContext(ink,"maincontext");
        context.setInkSource(inkSource);
        context.setTraceFormat(sourceFormat);
        //context.setBrush(brush);
        context.setCanvas(canvas);
        context.setCanvasTransform(transform);
        
    	this.loadStrokes();
    	try {
            this.loadContainers();
        } catch (ViewTreeManipulationException e) {
            e.printStackTrace();
            throw new InvalidDocumentException("While building the document a ViewTreeManipulation Exception has been raised, this should not happen.");
        }
    	}catch(InkMLComplianceException e){
    		throw new InvalidDocumentException(e.getMessage());
    	}       
    }

    private InkBrush getBrush(String color){
    	if(brushes.containsKey(color)){
    		InkBrush brush =  brushes.get(color);
    		if(this.context.getBrush().equals(brush)){
    			return null;
    		}
    		return brush;
    	}
    	InkBrush b = new InkBrush(this.ink,"brush_"+color);
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
