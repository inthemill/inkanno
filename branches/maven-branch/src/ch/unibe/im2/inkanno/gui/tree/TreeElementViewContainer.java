/**
 * 
 */
package ch.unibe.im2.inkanno.gui.tree;

import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;

import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.im2.inkanno.gui.GUI;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;

public class TreeElementViewContainer extends TreeElement{
    InkTraceViewContainer c;

    public TreeElementViewContainer(InkTraceViewContainer container){
        c = container;
    }
    @Override
    public TreeElement getChild(TreeElement parent, int index) {
        int i = 0;
        //List<InkTraceView> leafs = new LinkedList<InkTraceView>();
        for(InkTraceView leaf : c.getContent()){
            if(leaf.isLeaf()){
                //leafs.add((InkTraceViewLeaf) leaf);
                continue;
            }
            if(i == index){
                return new TreeElementViewContainer((InkTraceViewContainer)leaf);
            }
            i++;
        }
        if(index == i){
            //return new TreeElementViewLeafs(leafs);
        }
        return null;
    }
    @Override
    public String getLabel() {
        if(c.containsAnnotation("transcription")){
            return c.getAnnotation("transcription");    
        }else{
            if(c.containsAnnotation(InkAnnoAnnotationStructure.TYPE)){
                return c.getAnnotation(InkAnnoAnnotationStructure.TYPE);
            }
            return c.getLabel();
        }
    }
    
    @Override
    public int getChildCount() {
        int children = 0;
        for(InkTraceView leaf : c.getContent()){
            if(!leaf.isLeaf()){
                children ++;
            }
        }
        return children;// + ((traces)?1:0);
    }

    public int getIndexOfChild(TreeElement child) {
        //boolean isLeaf = child instanceof TreeElementViewLeafs; 
        int i = 0;
        for(InkTraceView leaf : c.getContent()){
            if(leaf.isLeaf()){
                continue;
            }
            //if(!isLeaf && leaf == ((TreeElementViewContainer)child).c){
            //    return i;
            //}
            i++;
        }
        /*if(isLeaf){
            return i;
        }*/
        return -1;
    }
   @Override
    public List<InkTraceView> getTraceViews() {
        List<InkTraceView> l =  new LinkedList<InkTraceView>();
        l.add(c);
        return l;
    }
    @Override
    public TreeElement getParent() {
        if(c.isRoot()){
            return new TreeElementInk(c.getInk());
        }else{
            return new TreeElementViewContainer(c.getParent());
        }
    }
    @Override
    public boolean isRoot() {
        return false;
    }
    @Override
    public boolean isView() {
        return true;
    }
    @Override
    
    public boolean equals(Object other) {
        return other instanceof TreeElementViewContainer && 
            ((TreeElementViewContainer)other).c == c;
    }
    @Override
    public Icon getIcon() {
        if(c.isRoot()){
            return TreeElementInk.documentIcon;
        }else if(!c.containsAnnotation("type")){
            return null;
        }else{
            if(GUI.getInstance().hasDocument()){
                return  GUI.getInstance().getCurrentDocument().getAnnotationStructure().getIcon(c);
            }
        }
        return null;
    }
    
    public int hashCode(){
        return c.hashCode();
    }
    /**
     * @return
     */
    public InkTraceViewContainer getContainer() {
        return c;
    }
    

}