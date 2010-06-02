/**
 * 
 */
package ch.unibe.im2.inkanno.gui.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreePath;

import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.InkTraceViewLeaf;

public abstract class TreeElement{
    /**
     * @param element
     * @return 
     */
    public static TreeElement createTreeElement(InkTraceView element) {
        if(element.isLeaf()){
            return null;
        }else{
            return new TreeElementViewContainer((InkTraceViewContainer) element);
        }
        
    }
    
    
    public abstract String getLabel();
    public abstract TreeElement getChild(TreeElement parent,int index);
    public abstract int getIndexOfChild(TreeElement child);
    public abstract int getChildCount();
    public boolean isLeaf() {
        return getChildCount() == 0;
    }
    public abstract List<InkTraceView> getTraceViews();
    
    public TreePath toTreePath() {
        return new TreePath(getTreePathList().toArray());
    }
   
    public List<TreeElement> getTreePathList(){
        List<TreeElement> l = (isRoot()) 
            ? new ArrayList<TreeElement>() 
            : getParent().getTreePathList();
        l.add(this);
        return l;
    }
    
    public String toString(){
        return getLabel();
    }
    
    public abstract TreeElement getParent();

    public abstract boolean isRoot();


    /**
     * @return
     */
    public abstract boolean isView();
    
    public abstract boolean equals(Object other);
    public abstract int hashCode();
    public abstract Icon getIcon();

}