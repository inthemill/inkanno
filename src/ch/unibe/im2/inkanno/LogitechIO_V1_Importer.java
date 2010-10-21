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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.unibe.eindermu.utils.XmlHandler;
import ch.unibe.im2.inkanno.importer.StrokeImporter;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
import ch.unibe.inkml.InkAnnoCanvas;
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
import ch.unibe.inkml.InkTraceLeaf;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.InkChannel.ChannelName;
import ch.unibe.inkml.util.TraceViewTreeManipulationException;

public class LogitechIO_V1_Importer extends XmlHandler implements StrokeImporter{
    
    private InkTraceFormat sourceFormat;
    private InkInkSource inkSource;
	private InkCanvasTransform transform;
	private InkInk ink;
	private InkContext context;
	
    public LogitechIO_V1_Importer(File file) throws IOException {
        super.loadFromFile(file);
        
        
    }
    
    public void importTo(Document doc) throws InvalidDocumentException {
    	ink = new InkInk();
    	doc.setInk(ink);
    	
    	InkDefinitions definition = new InkDefinitions(ink);
    	ink.setDefinitions(definition);
    	
    	//create InkSource for all Traces
    	inkSource = new InkInkSource(ink);
    	inkSource.setModel("io Digital Pen");
    	inkSource.setManufacturer("Logitech");
    	inkSource.setDescription("");     
        definition.enter(inkSource);
        
        
        try{
            sourceFormat = new InkTraceFormat(ink,"LogitechIOV1Format");
	        InkChannel x = new InkChannelDouble(ink);
	        x.setName(InkChannel.ChannelName.X);
	        x.setOrientation(InkChannel.Orientation.P);
	        x.setFinal();
	        sourceFormat.addChannel(x);
	        
	        InkChannel y = new InkChannelDouble(ink);
	        y.setName(InkChannel.ChannelName.Y);
	        y.setOrientation(InkChannel.Orientation.P);
	        y.setFinal();
	        sourceFormat.addChannel(y);
	
	        InkChannel t = new InkChannelDouble(ink);
	        t.setName(InkChannel.ChannelName.T);
	        t.setOrientation(InkChannel.Orientation.P);
	        t.setUnits("s");
	        t.setFinal();
	        sourceFormat.addChannel(t);
	        
	        InkChannel f = new InkChannelInteger(ink);
	        f.setName(InkChannel.ChannelName.F);
	        f.setOrientation(InkChannel.Orientation.P);
	        f.setIntermittent(true);
	        f.setFinal();
	        sourceFormat.addChannel(f);
	        sourceFormat.setFinal();
	        
	        definition.enter(sourceFormat);
	        
	        InkCanvas canvas = new InkAnnoCanvas(ink); 
	        definition.enter(canvas);
	        transform = InkCanvasTransform.getIdentityTransform(ink,"identityTransform",sourceFormat,canvas.getTraceFormat());
	        definition.enter(transform);
	        
	        context = new InkContext(ink,"maincontext");
	        context.setInkSourceByRef(inkSource);
	        context.setTraceFormat(sourceFormat);
	        context.setCanvas(canvas);
	        context.setCanvasTransform(transform);
	        ink.setCurrentContext(context);
	        
	        NodeList nl = this.getDocument().getElementsByTagName("Stroke");
	        for(int i = 0; i < nl.getLength(); i++) {
	            InkTraceLeaf str = nodeToStroke(nl.item(i));
	            if(str != null) {
	                ink.addTrace(str);
	            }
	        }
           for(InkTrace trace : ink.getTraces()){
                InkTraceViewLeaf l = new InkTraceViewLeaf(ink,ink.getViewRoot());
                l.setTraceDataRef(trace.getIdNow("t"));
                ink.getViewRoot().addTrace(l);
            }

        }catch(InkMLComplianceException e){
        	throw new InvalidDocumentException(e.getMessage());
        } catch (TraceViewTreeManipulationException e) {
            e.printStackTrace();
            throw new InvalidDocumentException("A View Tree ManipulationException has been raised, this should not happen.");
        }
    }
    
    private InkTraceLeaf nodeToStroke(Node traceNode) throws InkMLComplianceException {
        InkTraceLeaf stroke = new InkTraceLeaf(ink,null);
        final Element s = (Element) traceNode;
        
        final double start = Double.parseDouble(((Element) s.getElementsByTagName("StartS").item(0)).getTextContent())
         + 0.001 * Double.parseDouble(((Element) s.getElementsByTagName("StartMS").item(0)).getTextContent());
        final double duration = Integer.parseInt(((Element) s.getElementsByTagName("Dur").item(0)).getTextContent());
        final String[] x = ((Element) s.getElementsByTagName("X").item(0)).getTextContent().split(" ");
        stroke.addPoints(stroke.new PointConstructionBlock(x.length) {
            @Override
            public void addPoints() throws InkMLComplianceException {
                String[] y = ((Element) s.getElementsByTagName("Y").item(0)).getTextContent().split(" ");
                String[] f = ((Element) s.getElementsByTagName("F").item(0)).getTextContent().split(" ");
                
                for(int i = 0; i < x.length; i++) {
                    if(x[i].isEmpty() || y[i].isEmpty() || f[i].isEmpty()) {
                        reduce();
                        continue;
                    }
                    
                    set(ChannelName.X,Double.parseDouble(x[i]));
                    set(ChannelName.Y,Double.parseDouble(y[i]));
                    set(ChannelName.F,Double.parseDouble(f[i]));
                    if(i==0){
                        set(ChannelName.T,start);
                    }else if(i==x.length-1){
                        set(ChannelName.T,start + 0.001*duration);
                    }else{
                        set(ChannelName.T,start + (0.001*duration*i/(double)(x.length)));
                    }
                    next();
                }
                
            }
        });
       
        if(stroke.getPointCount() == 0) {
            return null;
        }
        return stroke;
    }
}
