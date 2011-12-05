/**
 * 
 */
package ch.unibe.im2.inkanno.gui.color;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Vector;

import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.DrawPropertyManager;
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

	@Override
	public Collection<? extends ColorizerCallback> getCallbacks() {
		Vector<ColorizerCallback> cbs = new Vector<ColorizerCallback>();
		
		cbs.add(new ColorizerCallback() {
			
			@Override
			public boolean isSelected() {
				return DrawPropertyManager.getInstance().isSelectedColorizer(NullColorizer.this);
			}
			
			@Override
			public String getLabel() {
				return getCaption();
			}
			
			@Override
			public Colorizer getColorizerForSelection() {
				return NullColorizer.this;
			}
		});
		return cbs;
	}

	@Override
	public void initialize(Document subject) {
		//nothing to do;
	}
    
    

}
