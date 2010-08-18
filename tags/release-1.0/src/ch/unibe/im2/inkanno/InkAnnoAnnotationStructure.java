/**
 * 
 */
package ch.unibe.im2.inkanno;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import ch.unibe.eindermu.utils.Config;
import ch.unibe.eindermu.utils.StringList;
import ch.unibe.inkml.AnnotationStructure;
import ch.unibe.inkml.InkAnnotatedElement;
import ch.unibe.inkml.InkTraceView;


/**
 * @author emanuel
 *
 */
public class InkAnnoAnnotationStructure extends AnnotationStructure {

    /**
     * Name of the annotation which specifies the type of an annotated InkML element.
     */
    public static final String TYPE = "type";
    
    /**
     * @param config
     * @throws IOException 
     */
    public InkAnnoAnnotationStructure(Config config) throws IOException {
        super(config);
    }
    
    
    /**
     * Return values of the Annotation "Type" of TraceView elements which may contain
     * the TraceViews represented by the list of InkTraceView objects specified in this call.
     * @param listOfElements list of InkTraceViews for which the possible parents must be looked up.
     * @return
     */
    
    public StringList getParentTypes(Collection<? extends InkAnnotatedElement> listOfElements){
        List<Item> parents = getParents(listOfElements);
        StringList result = new StringList();
        for(Item i : parents){
            if(i.getAnnotation(TYPE) != null && i.getAnnotation(TYPE).triggerValue != null){
                result.addUnique(i.getAnnotation(TYPE).triggerValue);
            }
        }
        return result;
    }

    
    
    
    public StringList getTraceViewTypes(){
        return getTriggerValues(NodeNames.TRACEVIEW,TYPE);
    }
    
    
    public Item getTraceViewItem(String type) {
        return getItem(NodeNames.TRACEVIEW,new AnnotationStructure.TriggerQuery(TYPE,type));
    }
    
    
    public Icon getTraceViewIcon(String type) {
        Item item = getTraceViewItem(type);
        return getIcon(item);
    }
    
}
