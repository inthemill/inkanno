/**
 * 
 */
package ch.unibe.im2.inkanno.gui.tree;

import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import ch.unibe.im2.inkanno.InkAnno;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;

public class TreeElementInk extends TreeElement{
    private InkInk ink;
    protected static Icon documentIcon = new ImageIcon(InkAnno.class.getResource("icon/document16.png"));
    
    public TreeElementInk(InkInk ink){
        this.ink = ink;
    }
    @Override
    public TreeElement getChild(TreeElement parent, int index) {
        return new TreeElementViewContainer((InkTraceViewContainer) ink.getViewRoots().get(index));
    }
    @Override
    public int getChildCount() {
        return ink.getViewRoots().size();
    }
     @Override
    public int getIndexOfChild(TreeElement child) {
         return ink.getViewRoots().indexOf(((TreeElementViewContainer)child).c);
    }
   @Override
    public String getLabel() {
        return "Document";
    }
    @Override
    public List<InkTraceView> getTraceViews() {
        return new LinkedList<InkTraceView>();
    }
    @Override
    public TreeElement getParent() {
        return null;
    }
    @Override
    public boolean isRoot() {
        return true;
    }
    @Override
    public boolean isView() {
        return false;
    }
    @Override
    public boolean equals(Object other) {
        return other instanceof TreeElementInk && 
            ((TreeElementInk)other).ink == ink;
    }
    @Override
    public Icon getIcon() {
        return documentIcon;
    }
    public int hashCode(){
        return ink.hashCode();
    }
}