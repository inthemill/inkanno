/**
 * 
 */
package ch.unibe.im2.inkanno.imageProcessing;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ch.unibe.eindermu.utils.GraphicsBackup;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.util.TraceGraphVisitor;

/**
 * @author emanuel
 *
 */
public class TraceDrawVisitor extends TraceGraphVisitor {
	private Dimension dimension;
	private boolean plusY = true ,plusX = true;
	/**
	 * The factor of the size of the resulting canvas compared  to the size of the documents.
	 */
	private double factor;
    private AffineTransform transformation;
	
	public void go(InkTraceView root){
		GraphicsBackup gb = new GraphicsBackup(getGraphics());
		//clear Background
		getGraphics().setColor(getGraphics().getBackground());
		getGraphics().fillRect(0, 0, (int)dimension.getWidth(), (int)dimension.getHeight());
		gb.reset();
		initializeTransformation(root);
		
		super.go(root);
		gb.reset();
	}

	public void initializeTransformation(InkTraceView root){
	    if(dimension != null){
            Rectangle2D bound = (Rectangle2D) root.getBounds();
            factor = Math.min((dimension.height - 25) / bound.getHeight(), (dimension.width - 10) / (double) bound.getWidth());
            
            transformation = AffineTransform.getTranslateInstance(5, 20);
            transformation.concatenate(AffineTransform.getScaleInstance(factor, factor));
            transformation.concatenate(AffineTransform.getTranslateInstance(-bound.getX(), -bound.getY()));
            if(!plusY) {
                transformation.concatenate(AffineTransform.getScaleInstance(1, -1));
                transformation.concatenate(AffineTransform.getTranslateInstance(0, -(2 * bound.getY() + bound.getHeight())));
            }
            if(!plusX) {
                transformation.concatenate(AffineTransform.getScaleInstance(-1, 1));
                transformation.concatenate(AffineTransform.getTranslateInstance(-(2 * bound.getX() + bound.getWidth()), 0));
            }
            getGraphics().transform(transformation);
        }
	}
	
	public void setDimension(Dimension dimension) {
		this.dimension = dimension;
	}
	public Dimension getDimension(){
		return dimension;
	}
	public void setOrientation(boolean plusX, boolean plusY){
		this.plusX = plusX;
		this.plusY = plusY;
	}
	
	/**
	 * returns the factor applied to input size to correspond to the canvas elements size
	 * @return
	 */
	public double getFactor(){
	    return this.factor;
	}
	
	public AffineTransform getTransformation(){
	    return (AffineTransform) this.transformation.clone();
	}
}
