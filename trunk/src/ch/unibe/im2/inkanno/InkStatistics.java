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
	private double h_mean;
	private double w_mean;
	private double w_var;
	private double h_var;
    
    public InkStatistics(InkInk ink){
        this.ink = ink;
        filters = new ArrayList<TraceViewFilter>();
    }
    
    public void addTraceFilter(TraceViewFilter filter){
        this.filters.add(filter);
    }
    
    public void calculate(){
        Histogram h = new Histogram((int) ink.getBounds().height);
        Histogram w = new Histogram((int) ink.getBounds().width);
        
        for(InkTraceView stroke : ink.getFlatTraceViewLeafs(null)){
            if(passFilters((InkTraceViewLeaf) stroke)){
                h.inc((int) stroke.getBounds().height);
                w.inc((int) stroke.getBounds().width);
            }
        }
        h.smooth(2);
        mostCommonTraceHeight = h.getMaxIndex();
        
        h_mean = h.getMean();
        h_var  = h.getVariance();
        w_mean = w.getMean();
        w_var  = w.getVariance();
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

	public double getH_mean() {
		return h_mean;
	}

	public double getW_mean() {
		return w_mean;
	}

	public double getW_var() {
		return w_var;
	}

	public double getH_var() {
		return h_var;
	}
}
