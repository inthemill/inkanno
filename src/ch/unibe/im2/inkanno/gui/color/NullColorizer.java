/**
 * 
 */
package ch.unibe.im2.inkanno.gui.color;

import java.awt.Graphics2D;

import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.TraceViewFilter;

/**
 * @author emanuel
 *
 */
public class NullColorizer implements Colorizer {

	public static NullColorizer factory(){
		return new NullColorizer();
	}
	
    @Override
    public boolean isResponsible(String selection) {
        return selection.equals("black");
    }

    @Override
    public void setColor(Graphics2D graphics, InkTraceViewLeaf s) {
        return;
    }

    @Override
    public void setFilter(TraceViewFilter filter) {
        
    }

	@Override
	public String getCaption() {
		return "None";
	}
    
    

}
