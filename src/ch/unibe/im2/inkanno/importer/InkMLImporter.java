package ch.unibe.im2.inkanno.importer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ch.unibe.eindermu.utils.XmlHandler;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.VirtualInkTraceViewContainer;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkMLComplianceException;
import ch.unibe.inkml.InkTrace;
import ch.unibe.inkml.InkTraceLeaf;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.TraceViewTreeManipulationException;
import ch.unibe.inkml.util.TraceVisitor;

public class InkMLImporter extends XmlHandler implements StrokeImporter {
	
	public InkMLImporter(File file) throws IOException {
		loadSchema();
		loadFromFile(file);
	}
	
	public InkMLImporter(InputStream stream) throws IOException {
        loadSchema();
        loadFromStream(stream);
    }
	
	private void loadSchema() throws IOException {
		String[] schemas = new String[]{"xml.xsd","inkml.xsd"};
		for(String schemaName : schemas){
			InputStream schema = InkInk.class.getResourceAsStream(schemaName);
			if(schema == null){
				throw new IOException(String.format("XML schema can not be found at %s.%s",InkInk.class.getCanonicalName(),schemaName));
			}
			addSchema(schema);
		}
	}

	@Override
	public void importTo(Document document) throws InvalidDocumentException {
		try {
			document.setInk(createInk());
		} catch (InkMLComplianceException e) {
		    e.printStackTrace();
			throw new InvalidDocumentException(e.getMessage());
		} catch (TraceViewTreeManipulationException e) {
            e.printStackTrace();
            throw new InvalidDocumentException("A View Tree ManipulationException has been raised, this should not happen.");
        }
	}
	
	public InkInk createInk() throws InkMLComplianceException{
	    InkInk ink = InkInk.loadFromXMLDocument(this.getDocument());
	    
	    if(ink.getTraces().size() > 0){
            VirtualInkTraceViewContainer tvc = new VirtualInkTraceViewContainer(ink);
            final List<InkTraceLeaf> leafs = new ArrayList<InkTraceLeaf>();
            
            //for each trace create a traceView and add it to the virutalInkTraceContainer
            for (InkTrace trace: ink.getTraces()){
                InkTraceViewLeaf viewLeaf = new InkTraceViewLeaf(ink, tvc);
                viewLeaf.setTraceDataRef(trace.getIdNow(InkTraceLeaf.ID_PREFIX));
                if(viewLeaf.isLeaf()){
                    leafs.add((InkTraceLeaf)trace);
                }
            }
            //remove all views that are allready present in a trace view tree
            for(InkTraceView root : ink.getViewRoots()){
                root.accept(new TraceVisitor(){
                    @Override
                    protected void visitHook(InkTraceViewLeaf leaf) {
                        if(leafs.contains(leaf.getTrace())){
                            leafs.remove(leaf.getTrace());
                        }
                    }});
            }
            tvc.setFinal(true);
            ink.addView(tvc);
            //add all the traces which are not present in any of the normal view trees
            //to the default view tree.
            for(InkTraceLeaf leaf  : leafs){
                InkTraceViewLeaf viewLeaf = new InkTraceViewLeaf(ink,ink.getViewRoot());
                viewLeaf.setTraceDataRef(leaf.getIdNow("t"));
                ink.getViewRoot().addTrace(viewLeaf);
            }
	    }
        
        return ink;
	}

}
