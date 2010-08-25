package ch.unibe.im2.inkanno.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import ch.unibe.im2.inkanno.Document;
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
import ch.unibe.inkml.util.ViewTreeManipulationException;

public class PGC_Text_Importer implements StrokeImporter {

	private File file;
	public PGC_Text_Importer(File file){
		this.file = file;
	}
	@Override
	public void importTo(Document doc) throws InvalidDocumentException {
		InkInk ink = new InkInk();
    	doc.setInk(ink);
    	
    	InkDefinitions definition = new InkDefinitions(ink);
    	ink.setDefinitions(definition);
    	try{
        	InkInkSource source = new InkInkSource(ink,"io2Source");
        	source.setModel("IO2");
        	source.setManufacturer("Logitech");
            definition.enter(source);
        
	        InkTraceFormat format = new InkTraceFormat(ink,"Logitechformat");
	        InkChannel x = new InkChannelDouble(ink);
	        x.setName(InkChannel.ChannelName.X);
	        x.setOrientation(InkChannel.Orientation.P);
	        format.addChannel(x);
	        
	        InkChannel y = new InkChannelDouble(ink);
	        y.setName(InkChannel.ChannelName.Y);
	        y.setOrientation(InkChannel.Orientation.P);
	        format.addChannel(y);
	        
	        InkChannel t = new InkChannelDouble(ink);
	        t.setName(InkChannel.ChannelName.T);
	        t.setOrientation(InkChannel.Orientation.P);
	        format.addChannel(t);
	        
	        InkChannel f = new InkChannelInteger(ink);
	        f.setName(InkChannel.ChannelName.F);
	        f.setOrientation(InkChannel.Orientation.P);
	        format.addChannel(f);
	        format.setFinal();
	        
	        definition.enter(format);
	        InkCanvas canvas = new InkAnnoCanvas(ink); 
	        definition.enter(canvas);
	        InkCanvasTransform transform = InkCanvasTransform.getIdentityTransform(ink,"pgcToInkAnnoTransform",format,canvas.getTraceFormat());
	        definition.enter(transform);
	        
	        InkContext context = new InkContext(ink,"maincontext");
	        context.setInkSourceByRef(source);
	        context.setTraceFormat(format);
	        //context.setBrush(brush);
	        context.setCanvas(canvas);
	        context.setCanvasTransform(transform);
			ink.setCurrentContext(context);
   
			Pattern doublePattern = Pattern.compile("-?[0-9]+([.,][0-9]+)?");
			long length = file.length();
			Scanner scan = new Scanner(this.file);
			if(scan.findInLine("Pen id: (.*)")!=null){
				ink.annotate("PenId", scan.match().group(1));
			}
			scan.nextLine();
			if(scan.findInLine("Page address: (.*)")!=null){
				ink.annotate("Page Address", scan.match().group(1));
			}
			scan.nextLine();
			if(scan.findInLine("Page bounds: (.*)")!=null){
				ink.annotate("Page Bounds", scan.match().group(1));
			}
			scan.nextLine();
			while(scan.hasNext("Color:")){
				InkTraceLeaf it = new InkTraceLeaf(ink,null);
				it.setCurrentContext(context);
				scan.nextLine();
				scan.next();
				double time = scan.nextInt();
				int millisec = scan.nextInt();
				time += millisec * 0.001;
				scan.next();
				scan.nextInt();
				final List<Double> xs = new ArrayList<Double>();
				final List<Double> ys = new ArrayList<Double>();
				final List<Double> ts = new ArrayList<Double>();
				final List<Integer> fs = new ArrayList<Integer>();
				while(scan.hasNext(doublePattern)){
					xs.add(Double.parseDouble(scan.findWithinHorizon(doublePattern,(int) length).replace(',', '.')));
					ys.add(Double.parseDouble(scan.findWithinHorizon(doublePattern,(int) length).replace(',', '.')));
					time = time + (scan.nextInt() * 0.001);
					ts.add(time);
					fs.add(scan.nextInt());
				}
				it.addPoints(it.new PointConstructionBlock(xs.size()) {
                    @Override
                    public void addPoints() throws InkMLComplianceException {
                        for(int i = 0;i<xs.size();i++){
                            set(ChannelName.X,xs.get(i));
                            set(ChannelName.Y,ys.get(i));
                            set(ChannelName.T,ts.get(i));
                            set(ChannelName.F,fs.get(i));
                            next();
                        }
                    }
                });
				ink.addTrace(it);
				scan.nextLine();
			}

			for(InkTrace trace : ink.getTraces()){
			    InkTraceViewLeaf l = new InkTraceViewLeaf(ink,ink.getViewRoot());
			    l.setTraceDataRef(trace.getIdNow(InkTraceLeaf.ID_PREFIX));
			    ink.getViewRoot().addTrace(l);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch( InkMLComplianceException e){
        	throw new InvalidDocumentException(e.getMessage());
        } catch (ViewTreeManipulationException e) {
            e.printStackTrace();
            throw new InvalidDocumentException("A View Tree ManipulationException has been raised, this should not happen.");
        }
		
	}
	

}
