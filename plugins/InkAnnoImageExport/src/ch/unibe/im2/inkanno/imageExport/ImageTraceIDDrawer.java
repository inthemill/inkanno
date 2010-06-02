package ch.unibe.im2.inkanno.imageExport;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.unibe.eindermu.utils.GraphicsBackup;
import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewLeaf;

public class ImageTraceIDDrawer extends RegisteredImageExportDrawer {
    
    private Pattern numberPattern = Pattern.compile("t([0-9]+)");
    
    public void go(InkTraceView root){
        GraphicsBackup gb = new GraphicsBackup(getGraphics());
        getGraphics().setBackground(new Color(255,255,255));
        getGraphics().setColor(new Color(0,0,0));
        super.go(root);
        gb.reset();
    }
    
    public void visitHook(InkTraceViewLeaf leaf){
        GraphicsBackup gb = new GraphicsBackup(getGraphics());
        Matcher m= numberPattern.matcher(leaf.getTrace().getId());
        if(m.matches()){
            int id = Integer.parseInt(m.group(1));
            try {
                getGraphics().setColor(new Color(id >> 8,id %255,label(leaf)));
            } catch (IllegalArgumentException e) {
                getGraphics().setColor(new Color(id >> 8,id %255,255));
            }
        }
        super.visitHook(leaf);
        gb.reset();
    }

    @Override
    public String getDescription() {
        return "Draws just traces. For each trace, (R << 8 + G) is the trace ID, B is (0 = nontext, 1=text)";
    }

    @Override
    public String getId() {
        return "trace_identifier";
    }     
    
    public int label(InkTraceView current) throws IllegalArgumentException {
        int result = 0;
        boolean drawing = false;
        while(!current.isRoot()){
            if(current.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Drawing")){
                drawing = true;
            }
            if(current.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Textline")||
                    current.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Word")){
                result |= 1;
            }
            if(current.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Diagram")){
                result |= 2;
            }
            if(current.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Structure")
                    || current.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Arrow")){
                result |= 4;
            }
            if(current.testAnnotation(InkAnnoAnnotationStructure.TYPE, "List")
                    || current.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Table")){
                result |=8;
            }
            if(current.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Formula")){
                result |= 16;
                result |= 1;
            }
            current = current.getParent();
        }
        if(!drawing && result == 0){
            throw new IllegalArgumentException("Trace has been unexpectedly annotated, or none at all");
        }
        return result;
    }
}
