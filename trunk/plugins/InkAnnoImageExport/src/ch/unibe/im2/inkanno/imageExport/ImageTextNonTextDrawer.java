package ch.unibe.im2.inkanno.imageExport;

import java.awt.Color;
import java.util.regex.Matcher;

import ch.unibe.eindermu.utils.GraphicsBackup;
import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewLeaf;

public class ImageTextNonTextDrawer extends RegisteredImageExportDrawer {

	@Override
	public String getDescription() {
		return "Text (blue), non-text (green)";
	}

	@Override
	public String getId() {
		return "tnt";
	}
	
    public void go(InkTraceView root){
        GraphicsBackup gb = new GraphicsBackup(getGraphics());
        getGraphics().setBackground(new Color(255,255,255));
        super.go(root);
        gb.reset();
    }

    public void visitHook(InkTraceViewLeaf leaf){
        GraphicsBackup gb = new GraphicsBackup(getGraphics());
        if(leaf.testAnnotationTree(InkAnnoAnnotationStructure.TYPE,"Textline","Formula")){
        	getGraphics().setColor(Color.blue);
        }else{
        	getGraphics().setColor(Color.green);
        }
        super.visitHook(leaf);
        gb.reset();
    }
	
}
