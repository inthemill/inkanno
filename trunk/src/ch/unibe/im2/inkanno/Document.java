/*
 * Created on 23.07.2007
 *
 * Copyright (C) 2007  Emanuel Indermühle <eindermu@iam.unibe.ch>
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

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.unibe.eindermu.utils.AbstractObservable;
import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.NumberList;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.exporter.Exporter;
import ch.unibe.im2.inkanno.exporter.ExporterException;
import ch.unibe.im2.inkanno.exporter.ExporterFactory;
import ch.unibe.im2.inkanno.exporter.FactoryException;
import ch.unibe.im2.inkanno.filter.TimeSpanEraserFilter;
import ch.unibe.im2.inkanno.util.Histogram;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
import ch.unibe.inkml.AnnotationStructure;
import ch.unibe.inkml.InkCanvasTransform;
import ch.unibe.inkml.InkChannel;
import ch.unibe.inkml.InkContext;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkMLComplianceException;
import ch.unibe.inkml.InkTrace;
import ch.unibe.inkml.InkTraceLeaf;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.util.TraceViewFilter;

/**
 * Document handles access to inkml document. It has methods
 * to handle the Inkml document in an easy way if used as document in an application
 * @author emanuel
 *
 */
public class Document extends AbstractObservable{
    
    /**
     * This event will be callen every time this document will be saved according to its knowledge
     */
	public static final Aspect ON_SAVE = new Aspect();
	/**
     * This event will be callen every time this document will change from saved state into unsaved state
     */
    public static final Aspect ON_CHANGE = new Aspect();
	

    private File file;

    //private double averageTraceHeight;
    
    private boolean isSaved = true;
    private TraceViewFilter traceFilter;
    private Selection selection;

    private String name;
    
	private InkInk ink;

	private Rectangle bound;
	
	private DocumentRecognizer.FileType type;
	
	//DisplayInformation

	//private double strokeWeight = 15;

    private double mostCommonTraceHeight;

    
    private InkAnnoAnnotationStructure annotationStructure; 
    

	public Document(File file, InkAnnoAnnotationStructure structure) throws IOException, InvalidDocumentException {
    	//initialisation
        
	    annotationStructure = structure;
        //loading
    	DocumentRecognizer dr = new DocumentRecognizer();
        dr.getStrokeImporter(file).importTo(this);
        testInkAnnoCompatibility();
        this.name = file.getName();
        type = dr.getType(); 
        if(type == DocumentRecognizer.FileType.INKML){
        	this.file = file;
        	setSaved(true);
        }else{
        	this.file = null;
        	setSaved(false);
        }
        
        //stats
        this.generateTraceStatistics();
        
        
        //listen to changes
        this.ink.registerFor(InkInk.ON_CHANGE,new Observer(){
            public void notifyFor(Aspect event, Object subject) {
                setSaved(false);
            }});
    }
    
    
    /**
     * @throws InvalidDocumentException 
     * @throws  
     * 
     */
    private void testInkAnnoCompatibility() throws InvalidDocumentException{
        if(ink == null){
            
        }
        try {
            if(ink.getViewRoot().getCanvas() != null){
                InkAnno.getInstance().getCanvas().acceptAsCompatible(ink.getViewRoot().getCanvas(),true);
            }else if(InkAnno.getInstance().getCanvas().getTraceFormat() != null){
                InkAnno.getInstance().getCanvas().getTraceFormat().acceptAsCompatible(ink.getViewRoot().getContext().getSourceFormat(),true);
            }
        } catch (InkMLComplianceException e) {
            throw new InvalidDocumentException(e.getMessage());
        }
    }


    /**
     * @param b
     */
    private void setSaved(boolean b) {
        if(isSaved && !b){
            notifyObserver(ON_CHANGE,this);
        }else if(!isSaved && b){
            notifyObserver(ON_SAVE,this);
        }
        isSaved = b;
    }


    /**
     * Generates Statistic from this documents
     * later accessible through other methods
     */
    private void generateTraceStatistics(){
        Histogram h = new Histogram(this.getBounds().height);
        for(InkTrace stroke : this.getInk().getFlatTraces()){
            //this.averageTraceHeight += stroke.getBounds().height;
            h.inc((int) stroke.getBounds().height);
        }
        h.smooth(2);
    	//this.averageTraceHeight = this.averageTraceHeight / this.getInk().getFlatTraces().size();
    	List<Integer> indexes = h.getOrderedMaxima();
    	if(indexes.size() > 1){
    	    this.mostCommonTraceHeight = Math.min(indexes.get(0), indexes.get(1));
    	}else{
    	    this.mostCommonTraceHeight = indexes.get(0);
    	}
    	
    	
    	NumberList<Double> ds = new NumberList.Double();
    	for(InkTrace stroke : this.getInk().getFlatTraces()){
    	    if((int)mostCommonTraceHeight == (int) stroke.getBounds().height){
    	        ds.add(stroke.getBounds().height);
    	    }
        }
    	this.mostCommonTraceHeight = ds.getMean();
    }
    
 
    public void save(File selectedFile) throws ExporterException, FactoryException {
    	ExporterFactory factory = new ExporterFactory();
        Exporter exporter = factory.createExporter("ch.unibe.im2.inkanno.InkMLExporter");
        exporter.setDocument(this);
        exporter.setFile(selectedFile);
        exporter.export();
        file = selectedFile;
        name = file.getName();
        setSaved(true);
    }

    public File getFile() {
        return this.file;
    }

    public boolean isSaved() {
        return isSaved ;
    }

    public void close() {
        // May be here has to be done more than the garbage collector does?
    }

    public String getName() {
        return this.name;
    }


    public TraceViewFilter getTraceFilter(){
        if(traceFilter == null){
            traceFilter = new TimeSpanEraserFilter(this);
        }
        return traceFilter;
    }
    
    public void setTraceFilter(TraceViewFilter filter){
        traceFilter = filter;
    }
    
    public Selection getSelection(){
        if(selection == null){
            selection = new Selection(this);
        }
        return selection;
    }

	public List<InkTrace> getTraces() {
		return this.getInk().getTraces();
	}
	public List<InkTrace> getFlatTraces() {
		return this.getInk().getFlatTraces();
	}
	/*
	public List<InkTraceViewLeaf> getVirtualViews() {
		return this.getInk().getVirtualViews();
	}
    */
    
	public void setInk(InkInk ink){
		this.ink = ink;
	}
	public InkInk getInk(){
		return this.ink;
	}


	public InkTraceViewContainer getCurrentViewRoot() {
		return this.getInk().getViewRoot();
	}


	public Rectangle getBounds() {
		//if(this.bound==null){
			this.bound = new Rectangle();
			for(InkTrace t: this.getFlatTraces()){
				this.bound.add(t.getBounds());
			}
		//}
		return this.bound;
	}


	public boolean isVMirroring() {
        for(InkTrace t : this.getInk().getFlatTraces()){
    		return ((InkTraceLeaf) t).getCanvasFormat().getChannel(InkChannel.ChannelName.Y).getOrientation() == InkChannel.Orientation.M;
        }
        return false;
	}
	public boolean isHMirroring() {
        for(InkTrace t : this.getInk().getFlatTraces()){
    		return ((InkTraceLeaf) t).getCanvasFormat().getChannel(InkChannel.ChannelName.X).getOrientation() == InkChannel.Orientation.M;
        }
        return false;
	}


	public List<InkTraceView> getFlattenedViews() {
		List<InkTraceView> result = new ArrayList<InkTraceView>();
		for(InkTraceView v: this.getInk().getViewRoot().getFlattenedViews()){
				result.add(v);
		}
		return result;
	}
	
	public DocumentRecognizer.FileType getType() {
		return type;
	}


    public double getMostCommonTraceHeight() {
        return this.mostCommonTraceHeight;
    }


    public void setAnnotationStructure(InkAnnoAnnotationStructure annotationStructure) {
        this.annotationStructure = annotationStructure;
    }
    
    public InkAnnoAnnotationStructure getAnnotationStructure(){
        return annotationStructure;
    }


    /**
     * Invert the documents X axis, which is the same as horizontally mirroring the document.
     * 
     * If a problem occurs during inverting the axis an Exception is thrown. Befor that, the axis is put to
     * the original valid state. 
     *  
     * @throws InkMLComplianceException if there has been a problem inverting the X axis. 
     * 
     */
    public void invertXAxis() throws InkMLComplianceException {
        invertAxis(InkChannel.ChannelName.X);
    }
    
    /**
     * Invert the documents Y axis, which is the same as vertically mirroring the document.
     * 
     * If a problem occurs during inverting the axis an Exception is thrown. Befor that, the axis is put to
     * the original valid state. 
     *  
     * @throws InkMLComplianceException if there has been a problem inverting the Y axis. 
     * 
     */
    public void invertYAxis() throws InkMLComplianceException {
        invertAxis(InkChannel.ChannelName.Y);
    }

    private void invertAxis(InkChannel.ChannelName axis) throws InkMLComplianceException{
        InkContext context = getCurrentViewRoot().getContext();
        InkCanvasTransform transform = context.getCanvasTransform();
        transform.invertAxis(context.getSourceFormat(),context.getCanvasTraceFormat(),axis);
        try {
            getInk().reloadTraces();
        } catch (InkMLComplianceException e) {
            transform.invertAxis(context.getSourceFormat(),context.getCanvasTraceFormat(),axis);
            throw e;
        }
    }
}
