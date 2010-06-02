package ch.unibe.im2.inkanno.imageExport;

import ch.unibe.eindermu.utils.Config;
import ch.unibe.im2.inkanno.imageProcessing.TraceDrawVisitor;
import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.util.AbstractTraceFilter;

public abstract class RegisteredImageExportDrawer extends TraceDrawVisitor {
    public RegisteredImageExportDrawer(){
        this.addTraceFilter(new AbstractTraceFilter() {
            public boolean pass(InkTraceView view) {
                return !(view.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Marking") && Config.getMain().getB("no-marking"));
            }
        });
    }
    public abstract String getDescription();
    public abstract String getId();

}
