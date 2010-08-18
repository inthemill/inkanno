package ch.unibe.im2.inkanno;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.unibe.eindermu.utils.XmlHandler;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkMLComplianceException;
import ch.unibe.inkml.InkTrace;
import ch.unibe.inkml.InkTraceLeaf;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.TraceVisitor;
import ch.unibe.inkml.util.ViewTreeManipulationException;

public class InkMLImporter extends XmlHandler implements StrokeImporter {
	
	public InkMLImporter(File file) throws IOException {
		super.loadFromFile(file);
	}

	@Override
	public void importTo(Document document) throws InvalidDocumentException {
		try {
			InkInk ink = InkInk.loadFromXMLDocument(this.getDocument());
			document.setInk(ink);
			
			VirtualInkTraceViewContainer tvc = new VirtualInkTraceViewContainer(ink);
			ink.addView(tvc);
			final List<InkTraceLeaf> leafs = new ArrayList<InkTraceLeaf>();
			for (InkTrace trace: document.getInk().getTraces()){
			    InkTraceViewLeaf viewLeaf = new InkTraceViewLeaf(ink, tvc);
			    viewLeaf.setTraceDataRef(trace.getIdNow("t"));
			    tvc.addByBackdoor(viewLeaf);
			    if(viewLeaf.isLeaf()){
			    	leafs.add((InkTraceLeaf)trace);
			    }
			}
			ink.getViewRoot().accept(new TraceVisitor(){
				@Override
				protected void visitHook(InkTraceViewLeaf leaf) {
					if(leafs.contains(leaf.getTrace())){
						leafs.remove(leaf.getTrace());
					}
				}});
			for(InkTraceLeaf leaf  : leafs){
				InkTraceViewLeaf viewLeaf=new InkTraceViewLeaf(ink,ink.getViewRoot());
				viewLeaf.setTraceDataRef(leaf.getIdNow("t"));
				ink.getViewRoot().addTrace(viewLeaf);
			}
		} catch (InkMLComplianceException e) {
		    e.printStackTrace();
			throw new InvalidDocumentException(e.getMessage());
		} catch (ViewTreeManipulationException e) {
            e.printStackTrace();
            throw new InvalidDocumentException("A View Tree ManipulationException has been raised, this should not happen.");
        }
	}

}
