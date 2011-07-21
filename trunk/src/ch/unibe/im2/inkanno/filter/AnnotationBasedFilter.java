package ch.unibe.im2.inkanno.filter;

import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.util.AbstractTraceFilter;

public class AnnotationBasedFilter extends AbstractTraceFilter {

	private String[] types;
	public AnnotationBasedFilter(String ... types){
		this.types = types;
	}
	
	@Override
	public boolean pass(InkTraceView view) {
		return !(view.testAnnotationTree(InkAnnoAnnotationStructure.TYPE, types));
	}

}
