package ch.unibe.im2.inkanno.imageExport;

import java.awt.Color;

import ch.unibe.eindermu.utils.GraphicsBackup;
import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.im2.inkanno.gui.color.RandomColorGenerator;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;

/**
 * Textblocks, Lists, Diagrams, Table and Drawing which parent is the root Document are  handled as
 * segment. Each segment gets a different color.
 * Every other content element which parent is root is ignored.
 * 
 */
public class ImageSimpleColorBlockDrawer extends RegisteredImageExportDrawer {

	private String[] consideredElements = {"Textblock","List","Diagram","Table","Drawing"};
	private RandomColorGenerator coloring = new RandomColorGenerator();
	   
	public String getDescription(){
        return "Each text zone gets and interesting color";
    }
    public String getId(){
        return "simple_color_block";
    }
    

	public void go(InkTraceView view){
		GraphicsBackup gb = new GraphicsBackup(getGraphics());
		getGraphics().setBackground(new Color(0,127,0));
		super.go(view);
		gb.reset();
	}
	
	@Override
	public void visitHook(InkTraceViewContainer container) {

			GraphicsBackup gb = new GraphicsBackup(getGraphics());
			if(changeColor(container)){
				container.delegateVisitor(this);
			}
			gb.reset();
	}
	
	private boolean changeColor(InkTraceViewContainer view){
		if(!view.isRoot() && view.getParent().isRoot()){
			boolean consider = false;
			for(String type : consideredElements){
				consider = consider || type.equals(view.getAnnotation(InkAnnoAnnotationStructure.TYPE));
			}
			if(consider){
				getGraphics().setColor(coloring.getNewColor());
				return true;
			}else{
				return false;
			}
		}
		return true;
	}


	
}
