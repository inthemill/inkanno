/**
 * 
 */
package ch.unibe.im2.inkanno.gui.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.inkml.InkTraceContainer;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.InkTraceView.TreeEvent;

public class AnnotationTreeModel implements TreeModel, Observer, TreeExpansionListener{

    private Document doc;
    
    private Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();
    private Set<Integer> expanded = new HashSet<Integer>();
    
    private Map<Set<InkTraceView>,TreeModelEvent> deleteEventBuffer = new HashMap<Set<InkTraceView>,TreeModelEvent>();
    
    private static Object empty = new Object(){
            public String toString() {
                return "empty";
            }
    	};
    private static AnnotationTreeModel emptyModel;
    	
    public static AnnotationTreeModel emptyModel() {
            if(emptyModel == null){
                emptyModel = new AnnotationTreeModel();
            }
            return emptyModel;
        }
    
    
    public AnnotationTreeModel(Document document) {
        doc = document;
        doc.getCurrentViewRoot().registerFor(InkTraceView.ON_CHANGE, this);
    }
    
    public AnnotationTreeModel() {
        doc = null;
    }

    public void notifyFor(Aspect event, Object subject) {
        if(subject instanceof TreeEvent){
            TreeEvent e = (TreeEvent) subject;
            if(e.aspect == InkTraceView.ON_CHILD_ADD){
                int total = 0;
                for(InkTraceView v: e.children){
                    if(!v.isLeaf()) total++;
                }
                int[] childIndices = new int[total];
                int i =0;
                int k = 0;
                for(InkTraceView v: (InkTraceViewContainer) e.target){
                    if(v.isLeaf()) continue;
                    if(e.children.contains(v)){
                        childIndices[k++]=i;
                    }
                    i++;
                }
                TreeModelEvent new_event = new TreeModelEvent(
                        this, 
                        TreeElement.createTreeElement(e.target).toTreePath(),
                        childIndices,
                        e.children.toArray());
                for(TreeModelListener listener : this.listeners) {
                    listener.treeNodesInserted(new_event);
                }
            }
            else if (e.aspect == InkTraceView.ON_CHILD_PRE_REMOVE){
                int total = 0;
                for(InkTraceView v: e.children){
                    if(!v.isLeaf()) total++;
                }
                int[] childIndices = new int[total];
                int i =0;
                int k = 0;
                for(InkTraceView v: (InkTraceViewContainer) e.target){
                    if(v.isLeaf()) continue;
                    if(e.children.contains(v)){
                        childIndices[k++]=i;
                    }
                    i++;
                }
                
                TreeModelEvent new_event = new TreeModelEvent(
                        this, 
                        TreeElement.createTreeElement(e.target).toTreePath(),
                        childIndices,
                        e.children.toArray());
                //store this event for to releas it when the children are acctually removed.
                deleteEventBuffer.put(e.children, new_event);
                
            }else if(e.aspect == InkTraceView.ON_CHILD_REMOVE){
                if(deleteEventBuffer.containsKey(e.children)){
                    for(TreeModelListener listener : this.listeners) {
                        listener.treeNodesRemoved(deleteEventBuffer.get(e.children));
                    }
                    deleteEventBuffer.remove(e.children);
                }
            }else if(e.aspect == InkTraceView.ON_NODE_CHANGE){
                TreeModelEvent new_event = new TreeModelEvent(this,TreeElement.createTreeElement(e.target).toTreePath());
                for(TreeModelListener listener : this.listeners) {
                    listener.treeNodesChanged(new_event);
                }
            }
        }

    }
    
    public void addTreeModelListener(TreeModelListener l) {
        this.listeners.add(l);
    }
    
    public InkTraceContainer c(Object p) {
        return (InkTraceContainer) p;
    }
    
    public void sendEvent(TreeModelEvent e) {
        //ControlPanel.this.tree.removeSelectionPaths(ControlPanel.this.tree.getSelectionPaths());
        for(TreeModelListener l : this.listeners) {
            l.treeStructureChanged(e);
        }
    }
    
    public Object getChild(Object parent, int index) {
        return ((TreeElement)parent).getChild((TreeElement) parent, index);
    }
    
    public int getChildCount(Object parent) {
        return ((TreeElement)parent).getChildCount();
    }
    
    public int getIndexOfChild(Object parent, Object child) {
        return ((TreeElement)parent).getIndexOfChild((TreeElement)child);
        /*
        int i = 0;
        for(InkTraceView s : c(parent).getContent()) {
            if(!s.isLeaf()) {
                if(s == child) {
                    return i;
                }
                i++;
            }
        }
        return -1;
        */
    }
    
    public Object getRoot() {
        if(doc == null){
            return null;
        }
        return new TreeElementInk(doc.getInk());
        /*if(doc == null || doc.getCurrentViewRoot() == null){
            return AnnotationTreeModel.empty;
        }else{
            return doc.getCurrentViewRoot();
        }*/
    }
    
    public boolean isLeaf(Object node) {
        return ((TreeElement)node).isLeaf();
        /*if(node == AnnotationTreeModel.empty) {
            return true;
        }
        if(!(node instanceof InkTraceView)){
        	return true;
        }
        for(InkTraceView s : c(node).getContent()) {
            if(!s.isLeaf()) {
                return false;
            }
        }
        return true;*/
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
        this.listeners.remove(l);
    }
    
    public void valueForPathChanged(TreePath path, Object newValue) {
    // TODO Auto-generated method stub
    
    }

    // Expantion code
    public void assignedTo(JTree tree) {
        restoreExpantion(tree);
        tree.addTreeExpansionListener(this);
    }

    private void restoreExpantion(JTree tree) {
        for(Integer c : expanded){
            tree.expandRow(c);
        }
    }


    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        JTree tree = (JTree)event.getSource(); 
        if(tree.getModel() != this){
            tree.removeTreeExpansionListener(this);
            return;
        }
        expanded.remove(tree.getRowForPath(event.getPath()));
    }


    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        JTree tree = (JTree)event.getSource(); 
        if(tree.getModel() != this){
            tree.removeTreeExpansionListener(this);
            return;
        }
        expanded.add(tree.getRowForPath(event.getPath()));
    }
}