/**
 * 
 */
package ch.unibe.im2.inkanno;

import java.util.List;

import org.w3c.dom.Element;

import ch.unibe.eindermu.utils.NotImplementedException;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkMLComplianceException;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.ViewTreeManipulationException;

/**
 * @author emanuel
 *
 */
public class VirtualInkTraceViewContainer extends InkTraceViewContainer {

    /**
     * @param ink
     */
    public VirtualInkTraceViewContainer(InkInk ink) {
        super(ink);
    }
    
    @Override
    public void buildFromXMLNode(Element node) throws InkMLComplianceException {
        // A virtual InkTraceViewContainer will never be stored to XML, hence it should not be build from it.
        throw new NotImplementedException();
    }

    @Override
    public void exportToInkML(Element parent) throws InkMLComplianceException {
        // do nothing: 
        // a virtual InkTraceViewContainer will never be stored to InkML see class description
    }
    
    public void add(List<InkTraceView> viewList) throws ViewTreeManipulationException {
        throw new ViewTreeManipulationException("The virtual view tree can not be manipulated, its here to access traces which are" +
        		" hidden in other view trees.");
    }
    
    public void addTrace(InkTraceView tv) throws ViewTreeManipulationException {
        throw new ViewTreeManipulationException("The virtual view tree can not be manipulated, its here to access traces which are" +
        " hidden in other view trees.");
    }

    public void addByBackdoor(InkTraceViewLeaf viewLeaf) throws ViewTreeManipulationException {
        super.addTrace(viewLeaf);
    } 
    

}
