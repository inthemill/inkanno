package ch.unibe.im2.inkanno.imageExport;

import java.awt.Color;

import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.inkml.InkTraceView;
/**
 * Pixel-Accurate Layout Presentation
 * as defined <a href="http://ocrocourse.iupr.com/layout-analysis">here</a>
 * R channel "columns": (Is not implemented in InkAnno: will always be 1)
 * 	R=0 reserved
 *  R=1...31: the column number of the pixel
 *  R=32..64: its a diagram
 * 
 * G channel "paragraph": (Number of InkAnno (Textblock | List | Diagram | Table))
 * 	G=0..63 : its a Textblock (if R=32..64 its actually a Diagram containing Textlines)
 *  G=64..127: its a List
 *  G=128..159 : its a Table
 *  G=160 : formula
 *  G=254: drawing (if R=32..64 its within a diagram)
 *  
 *B channel "line numbers"
 * if(G=0..159 ){
 *  B=1..255: number of line within Textblock, List| number of cell within Table
 * }
 * if(G=160,254){
 *	  B=1..255: number of the element within the document whos type is specified with G	
 * }
 * 255,255,0 : noise pixels (corresponds to InkAnno-Garbage type | Structuring element | arrow)
 * 255,255,255 : Background color
 * 
 * InkAnno Element, that are ignored:
 *    Marking and its children
 */
public class ImagePixelAccurateLineDrawer extends ImagePixelAccurateDrawer {
	
	/*
	 * number of Textline within block
	 */
	private int lineCounter = 0;
	
   public String getDescription(){
        return "draw image in the segmentation format specified here: http://ocrocourse.iupr.com/layout-analysis";
    }
    public String getId(){
        return "pixel_accurate_layout_line_presentation";
    }
	
	protected void handle(InkTraceView view,Handler h){
		if(view.containsAnnotation(InkAnnoAnnotationStructure.TYPE)){
			String type = view.getAnnotation(InkAnnoAnnotationStructure.TYPE);
			
			//diagram
			if(type.equals("Diagram")){
				diagramCounter ++;
				blockCounter ++;
				lineCounter = 0;
				h.goOn(CT.DIAGRAM);
			}
			else if(type.equals("Textblock")){
				if(cT!=CT.LIST || cT!=CT.TEXTBLOCK){
					blockCounter ++;
					lineCounter = 0;
					h.goOn(CT.TEXTBLOCK);
				}else{
					h.goOn();
				}
			}
			else if(type.equals("List")){
				listCounter ++;
				lineCounter = 0;
				h.goOn(CT.LIST);
			}
			else if(type.equals("Table")){
				tableCounter ++;
				lineCounter = 0;
				h.goOn(CT.TABLE);
			}
			else if(type.equals("Drawing")){
				drawingCounter++;
				if(cT == CT.DIAGRAM){
					getGraphics().setColor(new Color(diagramCounter ,DRAWING_NR,drawingCounter));
				}else{
					getGraphics().setColor(new Color(columnCounter ,DRAWING_NR,drawingCounter));
				}
				h.goOn(CT.DRAWING);
			}
			else if(type.equals("Formula")){
				formulaCounter++;
				if(cT == CT.DIAGRAM){
					getGraphics().setColor(new Color(diagramCounter ,FORMULA_NR,formulaCounter));
				}else{
					getGraphics().setColor(new Color(columnCounter,FORMULA_NR,formulaCounter));
				}
				h.goOn(CT.FORMULA);
			}else if(type.equals("Arrow")){
				if(cT==CT.TEXTLINE){
					h.goOn();
				}else{
					drawingCounter++;
					if(cT==CT.DIAGRAM){
						getGraphics().setColor(new Color(diagramCounter ,DRAWING_NR,drawingCounter));
					}else{
						getGraphics().setColor(new Color(columnCounter ,DRAWING_NR,drawingCounter));
					}
					h.goOn(CT.DRAWING);
				}
			}else if(type.equals("Structure") || type.equals("Garbage")){
				getGraphics().setColor(NOISE_COLOR);
				h.goOn(CT.NOISE);
			}else if(type.equals("Textline")){
				lineCounter ++;
				if(cT==CT.DIAGRAM){
					getGraphics().setColor(new Color(diagramCounter,blockCounter,lineCounter));
				}else if(cT==CT.LIST){
					getGraphics().setColor(new Color(columnCounter,listCounter,lineCounter));
				}else if(cT==CT.TABLE){
					getGraphics().setColor(new Color(columnCounter,tableCounter,lineCounter));
				}else{
					getGraphics().setColor(new Color(columnCounter,blockCounter,lineCounter));
				}
				h.goOn(CT.TEXTLINE);
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
