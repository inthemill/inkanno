package ch.unibe.im2.inkanno.importer;



import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import ch.unibe.eindermu.Messenger;
import ch.unibe.eindermu.utils.XmlHandler;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkMLComplianceException;
import ch.unibe.inkml.InkTraceLeaf;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.TraceViewTreeManipulationException;

public class IAMonDBImporter extends WhiteboardStrokeImporter implements StrokeImporter {

	
	private File file;

	public IAMonDBImporter(File file) throws IOException,
			InvalidDocumentException {
		super(file);
		this.file = file;
	}

	@Override
	public void importTo(Document document) throws InvalidDocumentException {
    	ink = new InkInk();
    	document.setInk(ink);
    	Pattern idPattern  = Pattern.compile("^([^-]+)-(\\d+)([a-z]?)-([^-]+)$");
    	File dbdir = file.getAbsoluteFile().getParentFile().getParentFile().getParentFile().getParentFile();
    	try {
    		initializeDefinitions();
    	
    		InkTraceViewContainer textBlock = new InkTraceViewContainer(ink, ink.getViewRoot());
    		
    		{
    			NodeList textnode = getDocument().getElementsByTagName("Text");
    			textBlock.annotate(InkAnnoAnnotationStructure.TYPE, "Textblock");
    			textBlock.annotate("transcription", ((Element)textnode.item(0)).getTextContent());
    		}
    		
    		NodeList nl = getDocument().getElementsByTagName("TextLine");
		
    		for(int lineI = 0; lineI < nl.getLength(); lineI++) {
    			Element e = (Element)nl.item(lineI);
    			
    			InkTraceViewContainer textLine = new InkTraceViewContainer(ink,textBlock);
    			textLine.annotate(InkAnnoAnnotationStructure.TYPE, "Textline");
    			textLine.annotate("transcription",e.getAttribute("text").replace("&quot;","\"").replace("&apos;","'"));
    			
                String id = e.getAttribute("id");
                Matcher m = idPattern.matcher(id);
                if(m.matches()){
                	File linefile = new File(dbdir.getPath()+File.separator+"lineStrokes"
                		+File.separator+m.group(1)
                		+File.separator+m.group(1)+"-"+m.group(2)
                		+File.separator+m.group(1)+"-"+m.group(2)+m.group(3)+"-"+m.group(4)+".xml");
                	if(!linefile.exists()){
                	    linefile = new File(dbdir.getPath()+File.separator+"lineStrokes"
                            +File.separator+m.group(1)
                            +File.separator+m.group(1)+"-"+m.group(2)
                            +File.separator+m.group(1)+"-"+m.group(2)+"z-"+m.group(4)+".xml");
                	}
                	LineDoc ld= new LineDoc();
                	ld.loadFromFile(linefile);
                	NodeList lineStrokeNodes = ld.getDocument().getElementsByTagName("Stroke");
                	for(int strokeI = 0; strokeI < lineStrokeNodes.getLength();strokeI ++){
                		InkTraceLeaf trace = nodeToTrace(lineStrokeNodes.item(strokeI));
                		ink.addTrace(trace);
                		InkTraceViewLeaf l = trace.createView(textLine);              		
        			}
                }else{
                	Messenger.warn(String.format("Unrecognized id '%s' for text line",id));
                }
            }
        
			
        } catch (InkMLComplianceException e) {
			throw new InvalidDocumentException(e.getMessage());
		} catch (TraceViewTreeManipulationException e) {
            e.printStackTrace();
            throw new InvalidDocumentException("A View Tree ManipulationException has been raised, this should not happen.");
        } catch (IOException e) {
        	e.printStackTrace();
        	throw new InvalidDocumentException(e.getMessage());
		}

	}
	public class LineDoc extends XmlHandler{
		
	}
}
