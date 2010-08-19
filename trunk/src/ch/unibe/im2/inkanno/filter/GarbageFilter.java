/**
 * 
 */
package ch.unibe.im2.inkanno.filter;

import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.util.AbstractTraceFilter;

/**
 * @author emanuel
 *
 */
public class GarbageFilter extends AbstractTraceFilter {

    @Override
    public boolean pass(InkTraceView view) {
        return !(view.testAnnotationTree(InkAnnoAnnotationStructure.TYPE, "Garbage"));
    }

}
