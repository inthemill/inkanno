package ch.unibe.im2.inkanno.imageExport;

import java.awt.Color;

import ch.unibe.eindermu.utils.GraphicsBackup;
import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.im2.inkanno.gui.color.RandomColorGenerator;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.InkTraceViewLeaf;

public class ImageSimpleColorTextlineDrawer extends RegisteredImageExportDrawer {
	
	private String[] consideredElements = {"Textline","Drawing","Arrow","Structure","Formula"};
	private RandomColorGenerator coloring = new RandomColorGenerator();
	
   public String getDescription(){
        return "Each text line gets and interesting color";
    }
    public String getId(){
        return "simple_color_textline";
    }
	
	public void go(InkTraceView view){
		GraphicsBackup gb = new GraphicsBackup(getGraphics());
		getGraphics().setBackground(new Color(0,127,0));
		super.go(view);
		gb.reset();
	}
	
	public void visitHook(InkTraceViewLeaf leaf) {
		if(!leaf.getParent().getAnnotation(InkAnnoAnnotationStructure.TYPE).equals("Table")){
			super.visitHook(leaf);
		}
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
		if(view.isRoot()){
			return true;
		}
		boolean consider = false;
		for(String type : consideredElements){
			consider = consider || type.equals(view.getAnnotation(InkAnnoAnnotationStructure.TYPE));
		}
		if(consider){
			if(!view.getParent().containsAnnotation(InkAnnoAnnotationStructure.TYPE) || !view.getParent().getAnnotation(InkAnnoAnnotationStructure.TYPE).equals("Textline")){
				getGraphics().setColor(coloring.getNewColor());
			}
		}else{
			return !(view.testAnnotation(InkAnnoAnnotationStructure.TYPE,"Marking"));
		}
		return true;
	}
}
