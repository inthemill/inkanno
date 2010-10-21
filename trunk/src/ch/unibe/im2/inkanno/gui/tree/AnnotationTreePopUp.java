/**
 * 
 */
package ch.unibe.im2.inkanno.gui.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import ch.unibe.im2.inkanno.gui.GUI;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.util.TraceViewTreeManipulationException;

/**
 * @author emanuel
 *
 */
@SuppressWarnings("serial")
public class AnnotationTreePopUp extends JPopupMenu {
    
    public AnnotationTreePopUp(){
        JMenuItem menuItem = new JMenuItem("resect");
        menuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                TreePath tp = TreeListener.getInstance().getPopUpSelection();
                try {
                    TreeElement el = ((TreeElement)tp.getLastPathComponent());
                    if(el.isView()){
                        ((InkTraceViewContainer)el.getTraceViews().get(0)).resect();
                    }else{
                        //TODO
                    }
                } catch (TraceViewTreeManipulationException exception) {
                    exception.printStackTrace();
                    JOptionPane.showMessageDialog(
                            GUI.getInstance(),
                            String.format(
                                    "Could add this group: \n %s \n\n %s",
                                    exception.getMessage(),
                                    "This leafs the view tree corrput, please close the document with out saving and reopen it again." +
                                    " Be aware that changes until now will be lost, you might want to try saving it anyway." ), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }});
        add(menuItem);
        menuItem = new JMenuItem("remove");
        menuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                TreePath tp = TreeListener.getInstance().getPopUpSelection();
                try {
                    TreeElement el = ((TreeElement)tp.getLastPathComponent());
                    if(el.isView()){
                        for(InkTraceView v : el.getTraceViews()){
                            ((InkTraceViewContainer)v).remove();
                        }
                    }
                } catch (TraceViewTreeManipulationException exception) {
                    exception.printStackTrace();
                    JOptionPane.showMessageDialog(
                            GUI.getInstance(),
                            String.format(
                                    "Could add this group: \n %s \n\n %s",
                                    exception.getMessage(),
                                    "This leafs the view tree corrput, please close the document with out saving and reopen it again." +
                                    " Be aware that changes until now will be lost, you might want to try saving it anyway." ), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }});
        add(menuItem);
        menuItem = new JMenuItem("remove including strokes!");
        menuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(GUI.getInstance(), "Do you confirm to completely remove the selected traces?", "Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if(result == JOptionPane.OK_OPTION){
                    TreePath tp = TreeListener.getInstance().getPopUpSelection();
                    try {
                        ((InkTraceViewContainer)tp.getLastPathComponent()).removeCompletely();
                    } catch (TraceViewTreeManipulationException exception) {
                        exception.printStackTrace();
                        JOptionPane.showMessageDialog(
                                GUI.getInstance(),
                                String.format(
                                        "Could add this group: \n %s \n\n %s",
                                        exception.getMessage(),
                                        "This leafs the view tree corrput, please close the document with out saving and reopen it again." +
                                        " Be aware that changes until now will be lost, you might want to try saving it anyway." ), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }});
        
        add(menuItem);
    }

}
