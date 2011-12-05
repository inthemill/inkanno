package ch.unibe.im2.inkanno.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.unibe.inkml.InkTraceViewLeaf;

public class Segment extends ArrayList<InkTraceViewLeaf> {

	private String id;
	public Segment(){
		super();
	}
	public Segment(Collection<InkTraceViewLeaf> leafs) {
		super(leafs.size());
		addAll(leafs);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
/*
	public List<InkTraceView> getWords() {
		List<InkTraceView> words = new ArrayList<InkTraceView>();
		for(InkTraceViewLeaf leaf : this){
	        InkTraceView v = leaf.getParent();
	        if(v.isRoot()){
	        	continue;
	        }
	        //get most meaningful/smallest labeling container
	        while(!v.testAnnotation(InkAnnoAnnotationStructure.TYPE,"Symbol", "Word","Textline","Arrow","Drawing","Structure")){
	        	if(v.getParent().testAnnotation(InkAnnoAnnotationStructure.TYPE, "Textline")){
	        		Messenger.warn(String.format("Unexpected Object with type '%s' in Textline",v.getAnnotation(InkAnnoAnnotationStructure.TYPE)));
	        		break;
	        	}else if(v.getParent().isRoot()){
	        		Messenger.warn(String.format("Element is with type '%s' is not known.. it is skipped.",v.getAnnotation(InkAnnoAnnotationStructure.TYPE)));
	        		continue;
	            }else{
	            	v=v.getParent();
	            }
	        }
	        
	        if(v.testAnnotation(InkAnnoAnnotationStructure.TYPE,"Drawing","Structure")){
	            if(words.isEmpty() || words.get(words.size()-1) != v){
	            	words.add(v);
	            }
	        }else if(!words.contains(v)){
	        	words.add(v);
	        }
		}
		return words;
	}
	*/
}
