package ch.unibe.im2.inkanno.util;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sun.security.action.GetLongAction;

import ch.unibe.eindermu.euclidian.Vector;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.inkml.InkBind;
import ch.unibe.inkml.InkCanvasTransform;
import ch.unibe.inkml.InkChannel;
import ch.unibe.inkml.InkContext;
import ch.unibe.inkml.InkMLComplianceException;
import ch.unibe.inkml.InkMapping;
import ch.unibe.inkml.InkTrace;
import ch.unibe.inkml.InkTraceContainer;
import ch.unibe.inkml.InkTraceFormat;
import ch.unibe.inkml.InkTraceLeaf;
import ch.unibe.inkml.InkTracePoint;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.util.ViewTreeManipulationException;

public class DocumentRepair {
	private Document doc;
	public DocumentRepair(Document d){
		doc = d;
	}

	
	public boolean run() throws InkMLComplianceException, ManualInteractionNeededException {
		boolean res = false;
		//res = repairCanvasTraceFormat() || res;
		//res = repairCanvasTransform() || res;
		//res = repairAxisMirroring() || res;
		
		//if(isAffectedByTimeIssue()){
		//    res = repairTimeIssue() || res;
		//    System.out.println("time issue resolved.");
		//}
		
		//res = repairTableIssue() || res;
		
		res = repairLabel() || res;
		//res = removeUnusedTranscription() || res;
		return res;
	}
	



    //-------test methods
	public boolean isAffectedByTimeIssue(){
		for(InkTrace t : doc.getFlatTraces()){
			double start = (Double) t.getPoints().get(0).get(InkChannel.Name.T);
			double end = (Double) t.getPoints().get(t.getPointCount()-1).get(InkChannel.Name.T);
			if(end - start < (t.getPointCount() * 0.013) - 0.040){
				return true;
			}
		}
		return false;
	}
	

	//-------repair methods
	private boolean repairTableIssue() throws ViewTreeManipulationException {
	    boolean res = false;
		List<InkTraceViewContainer> tables = new ArrayList<InkTraceViewContainer>();
		repairTableIssueHelper1(doc.getCurrentViewRoot(), tables);
		for(InkTraceViewContainer table : tables){
		    res = repairTableIssue(table);
		}
		return res;
	}
	
	public int  recognizeTableIssue(InkTraceViewContainer table){
        int wordCount = 0;
        for(InkTraceView v : table.getContent()){
            if(!v.isLeaf() && v.testAnnotation(InkAnnoAnnotationStructure.TYPE,"Textline")){
                InkTraceViewContainer textline = (InkTraceViewContainer) v;
                if(wordCount == 0){
                    wordCount = textline.getContent().size();
                }else{
                    if(wordCount != textline.getContent().size()){
                        wordCount = -1;
                    }
                }
            }
        }
        return wordCount;
	}
	
	public boolean repairTableIssue(InkTraceViewContainer table){
	    boolean res = false;
        int wordCount = recognizeTableIssue(table);
        if(wordCount == 1){
            System.out.println("Table is well formed.");
            return res;
        }
        if(wordCount == -1){
            System.out.println("Can't automaticly repair table.");
            return res;
        }
        List<InkTraceView> words = new ArrayList<InkTraceView>();
        for(InkTraceView v : table.getContent()){
            if(!v.isLeaf() && v.containsAnnotation(InkAnnoAnnotationStructure.TYPE) && v.getAnnotation(InkAnnoAnnotationStructure.TYPE).equals("Textline")){
                int i = 0;
                InkTraceViewContainer textline = (InkTraceViewContainer) v;
                for(InkTraceView v2 : textline.getContent()){
                    if(i != 0){
                        words.add(v2);
                    }else{
                        textline.annotate("transcription", v2.getAnnotation("transcription"));
                    }
                    i++;
                }
            }
        }
        for(InkTraceView word : words){
            List<InkTraceView> list = new ArrayList<InkTraceView>();
            list.add(word);
            InkTraceView newLine = table.createChildContainer(list);
            newLine.annotate(InkAnnoAnnotationStructure.TYPE, "Textline");
            newLine.annotate("transcription",word.getAnnotation("transcription"));
        }
        System.out.println("Table missformed: repaired.");
        return res;
	}
	
	private void repairTableIssueHelper1(InkTraceViewContainer container,List<InkTraceViewContainer> tables){
		for(InkTraceView v : container.getContent()){
			if(!v.isLeaf()){
				if(v.containsAnnotation(InkAnnoAnnotationStructure.TYPE) && v.getAnnotation(InkAnnoAnnotationStructure.TYPE).equals("Table")){
					tables.add((InkTraceViewContainer) v);
				}else{
					repairTableIssueHelper1((InkTraceViewContainer) v, tables);
				}
			}
		}
	}
	

	private boolean repairTimeIssue() throws InkMLComplianceException{
	    for(InkTrace t : doc.getFlatTraces()){
			int i = 0;
			double time = 0;
			double base = 0;
			for(InkTracePoint p : t.getPoints()){
				if(i == 0){
					time = (Double) p.get(InkChannel.Name.T);
					base = time;
				}else{
					time = time + (Double) p.get(InkChannel.Name.T) - base;
					p.set(InkChannel.Name.T, time);
				}
				i++;
			}
			((InkTraceLeaf) t).backTransformPoints();
		}
		return true;
	}
	
	private boolean repairCanvasTraceFormat() throws InkMLComplianceException{
	    boolean res = false;
		//repair inkAnno traceFormat
		if(doc.getInk().getDefinitions().containsKey("inkAnnoCanvasFormat")){
			InkTraceFormat f = (InkTraceFormat) doc.getInk().getDefinitions().get("inkAnnoCanvasFormat");
			if (f.getChannelCount() < 4){
				InkChannel.Name[] names = {InkChannel.Name.X,InkChannel.Name.Y,InkChannel.Name.T,InkChannel.Name.F};
				for(InkChannel.Name name : names){
					if(!f.containsChannel(name)){
						if(name == InkChannel.Name.F){
							f.addIntermittentChannel(InkChannel.channelFactory(InkChannel.Type.INTEGER, doc.getInk()).setName(name));
						}else{
							f.addChannel(InkChannel.channelFactory(InkChannel.Type.DECIMAL, doc.getInk()).setName(name));
						}
						res = true;
						System.out.println("repair canvasTrace format: channel "+name+" was missing");
					}
				}
			}
		}
		return res;
	}
		
	private boolean repairCanvasTransform(){
		//repair canvasTransform
		InkContext c = doc.getCurrentViewRoot().getContext();
		InkCanvasTransform tf = c.getCanvasTransform();
		boolean flip  = false;
		if(tf.getForwardMapping().getType() == InkMapping.Type.IDENTITY){
			if(tf.getForwardMapping().getBinds().size() > 0){
				for(InkBind b : tf.getForwardMapping().getBinds()){
					if(b.hasSource() && b.hasTarget() && b.source != b.target && (b.source == InkChannel.Name.X || b.source == InkChannel.Name.Y)){
						flip = true;
						break;
					}
				}
			}
		}
		if(flip){
			System.out.println("repair canvasTransform: invalid identity-mapping replaced by affine-mapping");
			tf.flipAxis(c.getSourceFormat(), c.getCanvasTraceFormat());
		}
		return flip;
	}
	
	private boolean repairAxisMirroring() throws InkMLComplianceException{
	    boolean res = false;
		InkContext c = doc.getCurrentViewRoot().getContext();
		InkCanvasTransform tf = c.getCanvasTransform();
		//repair axis mirroring
		List<InkChannel.Name> invertChannels = new ArrayList<InkChannel.Name>();
		for(InkChannel channel : c.getCanvasTraceFormat().getChannels()){
			if(channel.getOrientation() == InkChannel.Orientation.M){
				invertChannels.add(channel.getName());
				channel.setOrientation(InkChannel.Orientation.P);
			}
		}
		for(InkChannel.Name name: invertChannels){
			tf.invertAxis(c.getSourceFormat(), c.getCanvasTraceFormat(), name);
			System.out.println("repair canvas traceFormat: orientation -ve of channel "+name+" tranfered to canvasTranform.");
			res = true;
		}
		doc.getInk().reloadTraces();
		return res;
	}
	
	   /**
     * @return
     */
    private boolean removeUnusedTranscription() {
        return removeUnusedTranscription_help(doc.getCurrentViewRoot());
    }
    private boolean removeUnusedTranscription_help(InkTraceView view) {
        boolean res = false;
        if(view.isLeaf()){
            return false;
        }
        InkTraceViewContainer c = (InkTraceViewContainer)view;
        for(InkTraceView v : c.getContent()){
            res = removeUnusedTranscription_help(v) || res;
        }
        if(!doc.getAnnotationStructure().getItem(c).annotations.containsKey("transcription")
                && c.containsAnnotation("transcription")){
            c.removeAnnotation("transcription");
            System.err.println("transcription removed from "+c.getAnnotation(InkAnnoAnnotationStructure.TYPE));
            res = true;
        }
        return res;
    }
	
	private boolean repairLabel() throws ManualInteractionNeededException{
		return repairLabel_rec(doc.getCurrentViewRoot());
	}
	private boolean repairLabel_rec(InkTraceView view) throws ManualInteractionNeededException{
	    boolean res = false;
		if(view.isLeaf()){
			return false;
		}
		InkTraceViewContainer c = (InkTraceViewContainer)view;
		for(InkTraceView v : c.getContent()){
			res = repairLabel_rec(v) || res;
		}
		if(c.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Textline")){
		    String lineTranscription = "";
		    if(c.getAnnotation("transcription") != null){
		        lineTranscription = c.getAnnotation("transcription");
		    }
			Rectangle2D r = null;
			Vector p = null;
			final Vector pv = new Vector(0.0,0.0);
			List<InkTraceViewContainer> words = new ArrayList<InkTraceViewContainer>();
			for(InkTraceView word : c.getContent()){
				if(word.isLeaf())
					continue;
				String s = word.getAnnotation(InkAnnoAnnotationStructure.TYPE);
				if(s==null){
				    System.out.println("view in Textline: '"+c.getAnnotation("transcription")+"' is not defined." );
				    continue;
				}
				if(s.equals("Word") || s.equals("Arrow") || s.equals("Symbol")){
					words.add((InkTraceViewContainer)word);
					if(r==null){
						p = new Vector(word.getCenterOfGravity());
						r = new Rectangle();
						r.setFrameFromDiagonal(p,p);
						
					}else{
						r.add(word.getCenterOfGravity());
						pv.setLocation(pv.plus((new Vector(word.getCenterOfGravity())).minus(p).norm()));
						p.setLocation(word.getCenterOfGravity());
					}
				}
				if(s.equals("Correction")){
				    System.out.println("can't correct Textline: it contains Correction'");
				    return false;
				}
			}
			pv.setLocation(pv.norm());
			if(words.size() == 1){
				if(!lineTranscription.trim().equals(repairLabel_getTrans(words.get(0)).trim())){
					System.out.println("change Textline: '"+lineTranscription+"' to '"+repairLabel_getTrans(words.get(0))+"'.");
					//c.annotate("transcription",repairLabel_getTrans(words.get(0)).trim());
					res = true;
				}
			}else{
				if(Math.max(r.getWidth(),r.getHeight())/Math.min(r.getWidth(),r.getHeight())<2){
				    System.out.println("Textline '"+lineTranscription+"' is weird.");
				    return res;
				}
				Collections.sort(words,new Comparator<InkTraceViewContainer>(){
					public int compare(InkTraceViewContainer o1,InkTraceViewContainer o2) {
						double d = (new Vector(o2.getCenterOfGravity())).minus(o1.getCenterOfGravity()).scalar(pv); 
						return (int) -(d/Math.abs(d));
					}});
				String str = "";
				for(InkTraceViewContainer word : words){
				    String s = repairLabel_getTrans(word);
				    if(!s.trim().equals("")){
				        str += s.trim() + " ";
				    }
				}
				str = str.trim();
				
				if(!lineTranscription.replace("  ", " ").trim().equals(str)){
					System.out.println("change Textline: '"+lineTranscription+"' to '"+str+"'.");
					//c.annotate("transcription",str);
					res =  true;
				}
			}
		}
		return res;
		
	}
	
	private String repairLabel_getTrans(InkTraceView v) throws ManualInteractionNeededException{
		if(v.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Word")){
		    if(!v.containsAnnotation("transcription")){
		        return "";
		    }
			return v.getAnnotation("transcription");
		}else if(v.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Symbol")){
			return "";//"<Symbol/>";
		}else if(v.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Arrow")){
		    if(!v.containsAnnotation("transcription")){
                throw new ManualInteractionNeededException("There is an arrow without transcription.");
            }
			return "";//"<Arrow orientation=\""+v.getAnnotation("transcription")+"\"/>";
		}
		return "";
	}
}
