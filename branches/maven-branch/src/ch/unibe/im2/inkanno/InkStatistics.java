/**
 * 
 */
package ch.unibe.im2.inkanno;

import java.util.ArrayList;
import java.util.List;

import ch.unibe.im2.inkanno.util.Histogram;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.TraceViewFilter;

/**
 * @author emanuel
 *
 */
public class InkStatistics {
 
    private InkInk ink;
    private List<TraceViewFilter> filters;
    private int mostCommonTraceHeight;
    
    public InkStatistics(InkInk ink){
        this.ink = ink;
        filters = new ArrayList<TraceViewFilter>();
    }
    
    public void addTraceFilter(TraceViewFilter filter){
        this.filters.add(filter);
    }
    
    public void calculate(){
        Histogram h = new Histogram((int) ink.getBounds().height);
        for(InkTraceView stroke : ink.getFlatTraceViewLeafs()){
            if(passFilters((InkTraceViewLeaf) stroke)){
                h.inc((int) stroke.getBounds().height);
            }
        }
        h.smooth(2);
        this.mostCommonTraceHeight = h.getMaxIndex();
    }

    /**
     * @param stroke
     * @return
     */
    private boolean passFilters(InkTraceViewLeaf stroke) {
        for(TraceViewFilter tf : filters){
            if(!tf.pass(stroke)){
                return false;
            }
        }
        return true;
    }
    
    public int getMostCommonTraceHeight(){
        return mostCommonTraceHeight;
    }
}
