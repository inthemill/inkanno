/*
 * Created on 23.07.2007
 *
 * Copyright (C) 2007  Emanuel Indermühle <emanuel@inthemill.ch>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @author emanuel
 */

package ch.unibe.im2.inkanno;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ch.unibe.eindermu.utils.AbstractObservable;
import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.gui.GUITraceVisitor;
import ch.unibe.im2.inkanno.gui.tree.TreeElement;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.TraceViewTreeManipulationException;

public class Selection extends AbstractObservable implements TreeSelectionModel{
    
    public static final Aspect ON_CHANGE = new Aspect(){};
    
	private SortedSet<InkTraceView> content = new TreeSet<InkTraceView>();
	
	private List<TreePath> pathes = new ArrayList<TreePath>();
    
    private double lastEndTime = 0;
    
    private InkTraceView lastAdded;
    
    private TreePath lastPathAdded;
    
    private List<PropertyChangeListener> pcListener = new ArrayList<PropertyChangeListener>();
    private List<TreeSelectionListener > tsListener = new ArrayList<TreeSelectionListener>();
    
  

    
    private Document doc;

    private RowMapper mapper;
    
    public Selection(Document doc) {
        this.doc = doc;
        this.registerFor(ON_CHANGE, new Observer(){
            public void notifyFor(Aspect event, Object subject) {
                if(lastAdded != null && !content.contains(lastAdded)) {
                    lastAdded = null;
                }
                calculateEndTime();
            }
        });
    }
    
    public Document getDocument(){
        return doc;
    }
    
    /**
     * Toggle a trace view: if it is allready selected, it will be unselected.
     * @param trace trace to select or unselect
     */
    public void toggle(InkTraceView trace) {
        if(contains(trace)) {
            remove(trace);
        } else {
            add(trace);
        }
    }
    
    /**
     * adds trace view to the selection
     * @param view
     */
    public void add(InkTraceView view) {
        if(!this.content.contains(view)) {
            //add to trace set
            this.content.add(view);
            setNewLastAdded(view);
            
            
            //add to treepathset
            if(!view.isLeaf()){
                TreeElement el =TreeElement.createTreeElement(view);
                //test if treeElement is incomplete selected:
                //@see TreeElementsViewLeafs
                
                TreePath path = el.toTreePath();
                if(!pathes.contains(path)){
                    pathes.add(path);
                    TreeSelectionEvent e = new TreeSelectionEvent(this, path, true, lastPathAdded, path);
                    lastPathAdded = path;
                    sendEvent(e);
                }
            }
            this.notifyObserver(ON_CHANGE);
        }
    }
    
    
    
   private void sendEvent(TreeSelectionEvent e) {
        for(TreeSelectionListener l : tsListener){
            l.valueChanged(e);
        }
    }

    public void addAll(Collection<InkTraceView> l,InkTraceView last) {
        //determine last view of the set of added views
        TreePath lastPath = lastPathAdded;
        if(last == null){
            Iterator<InkTraceView> it = l.iterator(); 
            while(it.hasNext()){
                last = it.next();
                if(!last.isLeaf()){
                    lastPath = TreeElement.createTreeElement(last).toTreePath(); 
                }
            }
        }
        //add selected views
        for(InkTraceView v : l){
			if(!content.contains(v)) {
				content.add(v);
				if(!v.isLeaf()){
    				TreePath el = TreeElement.createTreeElement(v).toTreePath();
    	            if(!pathes.contains(el)){
    	                pathes.add(el);
    	                TreeSelectionEvent e = new TreeSelectionEvent(this, el, true, lastPathAdded, lastPath);
    	                sendEvent(e);
    	            }
				}
			}
		}
        /*
		SortedSet<InkTraceView> tmpset = new TreeSet<InkTraceView>(l);
		while(!tmpset.isEmpty()){
		    InkTraceView v = tmpset.first();
		    tmpset.remove(v);
    		TreeElement el =TreeElement.createTreeElement(v);
    		boolean missing = false;
            for(InkTraceView tv : el.getTraceViews()){
                if(tmpset.contains(tv)){
                    tmpset.remove(tv);
                    continue;
                }
                if(!content.contains(tv)){
                    missing=true;
                    break;
                }
            }
            if(missing){
                continue;
            }
            TreePath path = el.toTreePath();
            if(!pathes.contains(path)){
                pathes.add(path);
                TreeSelectionEvent e = new TreeSelectionEvent(this, path, true, lastPathAdded, path);
                lastPathAdded = path;
                sendEvent(e);
            }
        }*/
		
		lastAdded = last;
		lastPathAdded = lastPath;
        this.notifyObserver(ON_CHANGE);
	}
    
    
    public void replace(InkTraceView stroke) {
        clear();
        add(stroke);
    }
    
    /**
     * removes the specified trace form the selection
     * @param trace
     */
    public void remove(InkTraceView trace) {
        content.remove(trace);
        setNewLastAdded(null);
        if(!trace.isLeaf()){
            TreePath path =TreeElement.createTreeElement(trace).toTreePath();
            if(pathes.contains(path)){
                pathes.remove(path);
                TreePath newLast = getNewLastPath(null);
                TreeSelectionEvent e = new TreeSelectionEvent(this, path, false, lastPathAdded, newLast);
                lastPathAdded = newLast;
                sendEvent(e);
            }
        }
        notifyObserver(ON_CHANGE);
    }
    
    private void setNewLastAdded(InkTraceView newLast){
        if(content.isEmpty()){
            lastAdded = null;
            return;
        }
        if(lastAdded == null || !content.contains(lastAdded)){
            if(newLast != null && content.contains(newLast)){
                lastAdded = newLast;
            }else{
                lastAdded = content.first();
            }
        }else{
            if(newLast != null && content.contains(newLast)){
                lastAdded = newLast;
            }
        }
    }
    
    private TreePath getNewLastPath(TreePath newPath){
        if(pathes.isEmpty()){
            return null;
        }
        if(lastPathAdded == null || !pathes.contains(lastPathAdded)){
            if(newPath != null && pathes.contains(newPath)){
                return newPath;
            }
            return pathes.get(0);
        }
        if(newPath != null && pathes.contains(newPath)){
            return newPath;
        }
        return lastPathAdded;
    }
    
    public boolean contains(InkTraceView stroke) {
        return content.contains(stroke);
    }
    
    public void clear() {
        if(content.size()==0 && pathes.size()==0){
            return;
        }
        content.clear();
        lastAdded = null;
        while(!pathes.isEmpty()){
            TreePath path = pathes.get(0);
            pathes.remove(0);
            sendEvent(new TreeSelectionEvent(this,path, false, lastPathAdded, null));
        }
        lastPathAdded = null;
        notifyObserver(ON_CHANGE);
    }
    
    /**
     * Groups the selected InkTraceViews into a new InkTraceViewContainer 
     * which is labeled with the transcription "text" and the type "type"
     * @param text transcription of the new InkTraceViewContainer
     * @param type type of the new InkTraceViewContainer
     * @throws TraceViewTreeManipulationException 
     */
    public void labelSelection(String text, String type) throws TraceViewTreeManipulationException {
        if(this.content.isEmpty()) {
            return;
        }
        //tries to find the parent of the new InkTraceViewContainer
        InkTraceViewContainer parent = null;
        for(InkTraceView s : this.content) {
            if(parent == null || parent == s.getParent()) {
                parent = s.getParent();
            } else {
                parent = (InkTraceViewContainer) parent.getCommonAncestor(s);
            }
        }
        
        if(parent == null){
        	InkTraceView sc = this.doc.getCurrentViewRoot().createChildContainer(this.content);
        	if(!text.isEmpty()){
        		sc.annotate("transcription",text);
        	}
        	if(!type.isEmpty()) {
                sc.annotate("type", type);
            }
        }else {
        	InkTraceView sc = parent.createChildContainer(this.content);
        	if(!text.isEmpty()){
        		sc.annotate("transcription",text);
        	}
            if(!type.isEmpty()) {
                sc.annotate("type", type);
            }
        }
        clear();
    }
    
    private void calculateEndTime() {
        if(content.isEmpty()) {
            return;
        }
        this.lastEndTime = content.last().getTimeSpan().end;
    }
    
    public void addNext() {
        double last = lastEndTime;
        int dist = Integer.MAX_VALUE;
        InkTraceView selected = null;
        for(InkTraceView view : doc.getTraceFilter().filter(doc.getInk())) {
           if(view.isLeaf() && !this.content.contains(view) && view.getTimeSpan().start >= last) {
                if(view.getTimeSpan().start - last < dist) {
                    selected = view;
                    dist = (int) (view.getTimeSpan().start - last);
                }
            }
        }
        if(selected != null) {
            add(selected);
        }
    }
    
    /**
     * Returns the label which would be the transcription of a InkTraceViewContainer containing all selected
     * TraceViews. 
     * @return
     */
    public String getSelectionLabel() {
        String res = "";
        for(InkTraceView view : content) {
            String label = view.getAnnotation("transcription"); 
            if(label != null && !label.isEmpty()) {
                res += label.trim() + " ";
            }
        }
        return res.trim();
    }
    
    public void removeLast() {
        if(!content.isEmpty()) {
            remove(content.last());
        }
    }
    
    /**
     * Selects all Traces that are created between the trace specified
     * by the first parameter and the trace, which is the latest added
     * to the selection. If there is no trace added to the selection
     * before, the trace "trace" is added to the selection.  
     * @param trace
     * @param list
     */
    public void selectBetween(InkTraceView trace, List<InkTraceViewLeaf> list) {
        InkTraceView la = this.lastAdded;
        Collections.sort(list);
        if(la != null) {
            InkTraceView first, last;
            if(trace.compareTo(la) < 0) {
                first = trace;
                last = la;
            } else {
                first = la;
                last = trace;
            }
            boolean in = false;
            for(InkTraceView cur : list){
                if(!in){
                    if(cur == first){
                        in = true;
                    }
                }
                if(in){
                    add(cur);
                    if(cur == last){
                        in = false;
                        continue;
                    }
                }
                if(!in){
                    remove(cur);
                }
            }
            remove(trace);
            add(trace);
        } else {
            add(trace);
        }
    }
    
    public void accept(GUITraceVisitor v) {
        for(InkTraceView s : this.content) {
            s.accept(v);
        }
    }
    
    public SortedSet<InkTraceView> getContent() {
        return content;
    }
    
    /**
     * Filters the selection by the document's trace filter
     * this has to be callen every time the time-boundary-sliders are moved.
     */
    public void reFilterSelection() {
        if(content.isEmpty()){
            return;
        }
    	List<InkTraceView> l = doc.getTraceFilter().filter(content);
    	
    	setNewLastAdded(null);
    	
    	TreePath new_last_path = getNewLastPath(null);
        
    	Iterator<InkTraceView> it = content.iterator();
    	while(it.hasNext()){
    	    InkTraceView cur = it.next();
    	    if(!l.contains(cur)){
    	        if(!cur.isLeaf()){
        	        TreePath path =TreeElement.createTreeElement(cur).toTreePath();
        	        if(pathes.contains(path)){
        	            pathes.remove(path);
        	            TreeSelectionEvent e = new TreeSelectionEvent(this, path, false, lastPathAdded, new_last_path);
        	            sendEvent(e);
        	        }
    	        }
    	        it.remove();
    	    }
    	}
    	lastPathAdded = new_last_path;
    	notifyObserver(ON_CHANGE);
    }

    /**
     * @return
     */
    public InkTraceView getLastAdded() {
        return lastAdded;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcListener.add(listener);
    }

    @Override
    public void addSelectionPath(TreePath path) {
        TreeElement tl = (TreeElement) path.getLastPathComponent();
        if(tl.isView()){
            addAll(tl.getTraceViews(),null);
        }else{
            clear();
            pathes.add(path);
            TreeSelectionEvent e = new TreeSelectionEvent(this, path, true, lastPathAdded, path);
            lastPathAdded = path;
            sendEvent(e);
        }
    }

    @Override
    public void addSelectionPaths(TreePath[] paths) {
        for(TreePath path : paths){
            addSelectionPath(path);
        }
        
    }

    @Override
    public void addTreeSelectionListener(TreeSelectionListener x) {
        tsListener.add(x);
    }

    @Override
    public void clearSelection() {
        clear();
    }

    @Override
    public TreePath getLeadSelectionPath() {
        return lastPathAdded;
    }

    @Override
    public int getLeadSelectionRow() {
        return getRow(getLeadSelectionPath());
    }

    /**
     * @param leadSelectionPath
     * @return
     */
    private int getRow(TreePath path) {
        if(mapper == null){
            return -1;
        }
        return mapper.getRowsForPaths(new TreePath[]{path})[0];
    }

    @Override
    public int getMaxSelectionRow() {
        int max = -1;
        for(int i :getSelectionRows()){
            if (i>max){
                max = i;
            }
        }
        return max;
    }

    @Override
    public int getMinSelectionRow() {
        int min = 1000000;
        for(int i :getSelectionRows()){
            if (i<min){
                min = i;
            }
        }
        return min;
    }

    @Override
    public RowMapper getRowMapper() {
        return mapper;
    }

    @Override
    public int getSelectionCount() {
        return pathes.size();
    }

    @Override
    public int getSelectionMode() {
        return CONTIGUOUS_TREE_SELECTION;
    }

    @Override
    public TreePath getSelectionPath() {
        if(!pathes.isEmpty()){
            return pathes.get(0);
        }
        return null;
    }

    @Override
    public TreePath[] getSelectionPaths() {
        return pathes.toArray(new TreePath[pathes.size()]);
    }

    @Override
    public int[] getSelectionRows() {
        if(mapper == null){
            return new int[]{};
        }else{
            return mapper.getRowsForPaths(getSelectionPaths());
        }
    }

    @Override
    public boolean isPathSelected(TreePath path) {
        return pathes.contains(path);
    }

    @Override
    public boolean isRowSelected(int row) {
        for(int i : getSelectionRows()){
            if(i==row){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSelectionEmpty() {
        return pathes.isEmpty();
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcListener.remove(listener);
    }

    @Override
    public void removeSelectionPath(TreePath path) {
        if(path == null){
            return;
        }
        TreeElement el = (TreeElement) path.getLastPathComponent();
        if(el.isView()){
            for(InkTraceView v : el.getTraceViews()){
                remove(v);
            }
        }else{
            if(pathes.contains(path)){
                pathes.remove(path);
            }
            TreePath new_last_path = getNewLastPath(null);
            
            TreeSelectionEvent e = new TreeSelectionEvent(this, path, false, lastPathAdded, new_last_path);
            lastPathAdded = new_last_path;
            sendEvent(e);
        }
    }

    @Override
    public void removeSelectionPaths(TreePath[] paths) {
        for(TreePath path : paths){
            removeSelectionPath(path);
        }
    }

    @Override
    public void removeTreeSelectionListener(TreeSelectionListener x) {
        tsListener.remove(x);
        
    }

    @Override
    public void resetRowSelection() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRowMapper(RowMapper newMapper) {
        mapper = newMapper;
    }

    @Override
    public void setSelectionMode(int mode) {
        // not applicable
    }

    @Override
    public void setSelectionPath(TreePath path) {
        TreeElement el = (TreeElement) path.getLastPathComponent();
        if(el.isView()){
            clear();
            for(InkTraceView v : el.getTraceViews()){
                add(v);
            }
        }else{
            addSelectionPath(path);
        }
        
    }

    @Override
    public void setSelectionPaths(TreePath[] paths) {
        clear();
        for(TreePath path : paths){
            addSelectionPath(path);
        }
    }


}
