/**
 * 
 */
package ch.unibe.im2.inkanno.gui.tree;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import ch.unibe.im2.inkanno.InkAnno;
import ch.unibe.im2.inkanno.gui.GUI;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.util.ViewTreeManipulationException;

/**
 * @author emanuel
 *
 */
@SuppressWarnings("serial")
public class AnnotationTree extends JTree implements DropTargetListener, MouseMotionListener, Autoscroll {
    
    
    private Point dragLocation;

    public AnnotationTree(GUI gui){
        this(AnnotationTreeModel.emptyModel());
        setFont(new Font(getFont().getFontName(), Font.PLAIN, 10));
        if(gui.hasDocument()){
            setSelectionModel(gui.getCurrentDocument().getSelection());
        }
        addTreeSelectionListener(TreeListener.getInstance(new AnnotationTreePopUp()));
        addMouseListener(TreeListener.getInstance());
        addMouseMotionListener(this);
        addKeyListener(gui.getController());
        
        setScrollsOnExpand(true);
        setExpandsSelectedPaths(true);
        
        
        /*DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource
            .createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_MOVE,
                this);
        */
        setAutoscrolls(true);
        setDragEnabled(true);
        setDropMode(DropMode.USE_SELECTION);
        if(this.getDropTarget() == null){
            setDropTarget(new DropTarget(this,TransferHandler.MOVE,this));
        }
        setCellRenderer(new CellRenderer());
    }
    
    /**
     * @param model
     */
    public AnnotationTree(AnnotationTreeModel model) {
        super(model);
    }
/*
    public class AnnotationTreeDropTarget extends DropTarget implements Autoscroll{

        public AnnotationTreeDropTarget(Component annotationTree,
                int move, DropTargetListener annotationTree2) {
            super(annotationTree,move,annotationTree2);
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            System.err.println("eksit");
        }

        @Override
        public void autoscroll(Point cursorLocn) {
            System.err.println("loc");
            
        }

        @Override
        public Insets getAutoscrollInsets() {
            System.err.println("inset");
            return null;
        }
    }*/
    
    
    @SuppressWarnings("serial")
    public class CellRenderer extends DefaultTreeCellRenderer{
        @Override
        public Component getTreeCellRendererComponent(
                        JTree tree, Object value,boolean sel,
                        boolean expanded,boolean leaf, int row, boolean hasFocus){
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if(!( value instanceof TreeElement)){
                return this;
            }
            Icon i = ((TreeElement) value).getIcon();
            if(i != null){
                setIcon(i);
            }
            return this;
        }
    }
    

    @Override
    public void drop(DropTargetDropEvent dtde) {
        
        //get element that are dragged
        TreePath[] elementPaths = getSelectionPaths();
        //add them to a handy List
        List<InkTraceViewContainer> draggedElements = new ArrayList<InkTraceViewContainer>();
        List<InkTraceViewContainer> parents = new ArrayList<InkTraceViewContainer>();
        for(TreePath p : elementPaths){
            TreeElement el = (TreeElement) p.getLastPathComponent();
            if(el.isView()){
                InkTraceViewContainer c = ((TreeElementViewContainer)el).getContainer();
                //if they are not in this list
                if(!draggedElements.contains(c)){
                    draggedElements.add(c);
                }
                if(c.getParent() != null && !parents.contains(c.getParent())){
                    parents.add(c.getParent());
                }
            }
        }
        //Get Drop-location
        Point dropXY = dtde.getLocation();
        //get Tree element at this location
        TreePath targetPath = getClosestPathForLocation(dropXY.x, dropXY.y);
        TreeElement el = (TreeElement) targetPath.getLastPathComponent();
        /*if(!el.isView()){
            return;
        }*/
        
        InkTraceViewContainer target = ((TreeElementViewContainer)el).getContainer();
        
        //test if target is one of the dragged elements
        //test if target is a descendant of one of the dragged elements
        {
            InkTraceViewContainer  current = target;
            while(current!= null){
                if(draggedElements.contains(current)){
                    dtde.rejectDrop();
                    return;
                }
                current = current.getParent();
            }
        }
        
        //put dragged elements into this location
        try {
            for(InkTraceViewContainer s : draggedElements){
                target.addTrace(s);
            }
        } catch (ViewTreeManipulationException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    GUI.getInstance(),
                    String.format(
                            "Could add this group: \n %s \n\n %s",
                            e.getMessage(),
                            "This leafs the view tree corrput, please close the document with out saving and reopen it again." +
                            " Be aware that changes until now will be lost, you might want to try saving it anyway." ), "Error", JOptionPane.ERROR_MESSAGE);
        }
        {
            TreePath path = (new TreeElementViewContainer(target)).toTreePath();
            makeVisible(path);
            expandPath(path);
        }
        for(InkTraceViewContainer parent : parents){
            TreePath path = (new TreeElementViewContainer(parent)).toTreePath();
            makeVisible(path);
        }
        
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    private static final int autoScrollInsets = 20;
    @Override
    public void autoscroll(Point cursorLocn) {
        Rectangle r = getVisibleRect();
        int y = cursorLocn.y;
        if(y<r.y+autoScrollInsets){
            int move = autoScrollInsets-(y-r.y);
            scrollRectToVisible(new Rectangle(0,r.y-move/2,1,1));
        }else if(y>r.y+r.height-autoScrollInsets){
            int move = autoScrollInsets-(r.y+r.height-y);
            scrollRectToVisible(new Rectangle(0,r.y+r.height,1,move/2));
        }
    }

    @Override
    public Insets getAutoscrollInsets() {
        Rectangle r = getVisibleRect();
        Insets i = (Insets) getInsets();
        i.set(r.y+autoScrollInsets,i.left,(this.getHeight()-(r.y+r.height))+autoScrollInsets,i.right);
        return i;
    }
    
}
