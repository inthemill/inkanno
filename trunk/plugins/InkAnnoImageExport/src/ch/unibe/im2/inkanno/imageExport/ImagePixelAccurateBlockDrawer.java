package ch.unibe.im2.inkanno.imageExport;

import java.awt.Color;

import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.inkml.InkTraceView;

public class ImagePixelAccurateBlockDrawer extends ImagePixelAccurateDrawer {

	public String getDescription(){
	    return "draw image in the segmentation format specified here: http://ocrocourse.iupr.com/layout-analysis" +
	    		" based on document zones, not text lines";
	}
	public String getId(){
        return "pixel_accurate_layout_block_presentation";
    }

	public void handle(InkTraceView view,Handler h){
		if(view.containsAnnotation(InkAnnoAnnotationStructure.TYPE)){
			String type = view.getAnnotation(InkAnnoAnnotationStructure.TYPE);
			
			if(type.equals("Textblock")){
				if(cT!=CT.LIST){
					blockCounter ++;
					getGraphics().setColor(new Color(columnCounter,blockCounter,0));
					h.goOn(CT.TEXTBLOCK);
				}else{
					h.goOn();
				}
			}else if(type.equals("List")){
				listCounter++;
				getGraphics().setColor(new Color(columnCounter,listCounter,0));
				h.goOn(CT.LIST);
			}else if(type.equals("Table")){
				tableCounter++;
				getGraphics().setColor(new Color(columnCounter,tableCounter,0));
				h.goOn(CT.TABLE);
			}else if(type.equals("Diagram")){
				diagramCounter++;
				getGraphics().setColor(new Color(diagramCounter,0,0));
				h.goOn(CT.DIAGRAM);
			}else if(type.equals("Drawing")){
				if(cT!=CT.DIAGRAM){
					drawingCounter ++;
					getGraphics().setColor(new Color(columnCounter,DRAWING_NR,drawingCounter));
					h.goOn(CT.DRAWING);
				}else{
					h.goOn();
				}
			}
			else if(type.equals("Formula")){
				if(cT!=CT.FORMULA){
					formulaCounter ++;
					getGraphics().setColor(new Color(columnCounter,FORMULA_NR,formulaCounter));
					h.goOn(CT.FORMULA);
				}else{
					h.goOn();
				}
			}else if(type.equals("Arrow")){
				if(cT!=CT.TEXTBLOCK & cT!=CT.DIAGRAM){
					getGraphics().setColor(NOISE_COLOR);
					h.goOn(CT.NOISE);
				}else{
					h.goOn();
				}
			}else if(type.equals("Structure") || type.equals("Garbage")){
				if(cT!=CT.DIAGRAM){
					getGraphics().setColor(NOISE_COLOR);
					h.goOn(CT.NOISE);
				}else{
					h.goOn();
				}
			}else{
				h.goOn();
			}
		}else{
			if(view.isRoot()){
				h.goOn();
			}else{
				if(parentEquals(view, "Table")){
					getGraphics().setColor(NOISE_COLOR);
					h.goOn(CT.NOISE);
				}else{
					h.goOn();
				}
			}
		}
		
	}

}
