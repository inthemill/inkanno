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

package ch.unibe.im2.inkanno.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.InkAnno;
import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.im2.inkanno.Selection;
import ch.unibe.im2.inkanno.controller.Contr;
import ch.unibe.im2.inkanno.gui.tree.AnnotationTree;
import ch.unibe.im2.inkanno.gui.tree.AnnotationTreeModel;
import ch.unibe.im2.inkanno.gui.tree.TreeElement;
import ch.unibe.im2.inkanno.gui.tree.TreeElementViewContainer;
import ch.unibe.im2.inkanno.gui.tree.TreeListener;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkTraceContainer;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.AnnotationStructure.Annotation;
import ch.unibe.inkml.AnnotationStructure.Item;
import ch.unibe.inkml.AnnotationStructure.Annotation.ValueType;
import ch.unibe.inkml.util.Timespan;
import ch.unibe.inkml.util.ViewTreeManipulationException;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel implements Observer{
    
    private JTextField input;
    boolean inputIsTextField = true;
    private JComboBox inputComboBox;
    
    private JTree tree;
    
    private JComboBox typeComboBox;
    
    private GUI gui;
    
    private AnnotationView strokeProperty;
    
    private JSlider timeSliderRight;
    
    private JSlider timeSliderLeft;

	private Box inputBox;
    
    public ControlPanel(GUI gui) {
        this.gui = gui;
        
        GUI.getInstance().getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_SWITCH,this);
        GUI.getInstance().getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_PRE_SWITCH,this);
        
        this.input = new JTextField();
        this.input.addKeyListener(gui.getController());
        
        this.setPreferredSize(new Dimension(400, 250));
        this.setMinimumSize(new Dimension(300, 250));
        this.setLayout(new GridBagLayout());
        
        addSlider();
        
        addTypeComboBox();
        
        addInputPanel();
        
        addTree();
        
        addStrokePropertyView();
        
        addSelectionDetailView();
    }
    
    private void addTypeComboBox() {
        typeComboBox = new JComboBox();
        typeComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String selectedType = (String) typeComboBox.getSelectedItem();
				Item i = getAnnotationStructure().getTraceViewItem(selectedType);
				Annotation a = null;
				if(i!=null){
				   a = i.getAnnotation("transcription");    
				}
				if(a != null && a.valueType != ValueType.FREE && a.valueType != ValueType.DATE){
					String selected = (String) inputComboBox.getSelectedItem();
					inputComboBox.removeAllItems();
					inputComboBox.setSelectedIndex(-1);
					for(String s : a.values){
						inputComboBox.addItem(s);
						if(selected != null && s.equals(selected)){
							inputComboBox.setSelectedItem(s);
						}
							
					}
					inputComboBox.setEditable(a.valueType == ValueType.PROPOSED);
					if(inputIsTextField){
						inputBox.removeAll();
						inputBox.add(inputComboBox);
						inputBox.repaint();
						
					}
					inputComboBox.grabFocus();
					inputIsTextField = false;
				}else{
					if(!inputIsTextField){
						inputBox.removeAll();
						inputBox.add(input);
						inputBox.repaint();
					}
					input.grabFocus();
					inputIsTextField = true;
					//input.setEnabled(a!=null);
				}
			}});
        typeComboBox.setRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        	Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        	JLabel label = new JLabel();
        			    
        	label.setOpaque(true);
        	label.setForeground(comp.getForeground());
        	label.setBackground(comp.getBackground());
        	Icon icon;
        	if(value != null && getAnnotationStructure() != null){
        	    icon = getAnnotationStructure().getTraceViewIcon((String)value);
    			if(icon != null){
            		label.setIcon(icon);
            	}
        	}
        	label.setText((String)value);
        	return label;
            }
        });
        
        inputComboBox = new JComboBox();
        //inputComboBox.setEditable(true);
        inputComboBox.addKeyListener(gui.getController());
        inputComboBox.getEditor().getEditorComponent().addKeyListener(gui.getController());
	}

	private void addSelectionDetailView() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 2;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(3, 3, 6, 6);
        this.add(new SelectionView(), gc);
    }
    
    private void addSlider() {
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridBagLayout());
        
        timeSliderRight = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        timeSliderRight.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                GUI.getInstance().getCurrentDocument().getTraceFilter().setCurrentTimeEnd(timeSliderRight.getValue());
                GUI.getInstance().getCurrentDocument().getSelection().reFilterSelection();
                GUI.getInstance().getCanvas().repaint();
            }
        });
        
        timeSliderLeft = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        timeSliderLeft.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                GUI.getInstance().getCurrentDocument().getTraceFilter().setCurrentTimeStart(timeSliderLeft.getValue());
                GUI.getInstance().getCurrentDocument().getSelection().reFilterSelection();
                GUI.getInstance().getCanvas().repaint();
            }
        });
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 0, 3, 0);
        sliderPanel.add(timeSliderLeft, gc);
        
        gc = new GridBagConstraints();
        gc.weightx = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 3, 0, 0);
        sliderPanel.add(timeSliderRight, gc);
        
        gc = new GridBagConstraints();
        gc.weightx = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(6, 3, 6, 6);
        gc.gridwidth = 3;
        this.add(sliderPanel, gc);
    }
    
    
    private void sliderRecofiguration(){
    	Document doc = GUI.getInstance().getCurrentDocument();
    	Timespan t = doc.getInk().getTimeSpan(); 

    	double cet = doc.getTraceFilter().getCurrentTimeEnd();
        double cst = doc.getTraceFilter().getCurrentTimeStart();
        
        if(cet > t.end){
        	cet = t.end;
        }else if(cet < t.start){
        	cet = t.start;
        }
        if(cst > t.end){
        	cst = t.end;
        }else if(cst < t.start){
        	cst = t.start;
        }
        
        timeSliderRight.setMinimum((int) (t.start) - 10);
        timeSliderLeft.setMinimum((int) (t.start) - 10);
        timeSliderRight.setMaximum((int) (t.end) + 10);
        timeSliderLeft.setMaximum((int) (t.end) + 10);
        
        timeSliderRight.setValue((int)cet+1);
        timeSliderLeft.setValue((int)cst);
    }
    
    public void notifyFor(Aspect event, Object subject) {
        if(event.equals(DocumentManager.ON_DOCUMENT_SWITCH)){
            if(GUI.getInstance().hasDocument()) {
            	Document d = GUI.getInstance().getCurrentDocument();
                //Slider things
                timeSliderRight.setEnabled(true);
                timeSliderLeft.setEnabled(true);
                sliderRecofiguration();
                d.getInk().registerFor(InkInk.ON_CHANGE, this);
                
                //Selection registration
                d.getSelection().registerFor(Selection.ON_CHANGE, this);
                
                //View Tree registration
                
                tree.setModel(gui.getCurrentDocumentView().getAnnotationTreeModel());
                tree.setSelectionModel(gui.getCurrentDocument().getSelection());
                tree.updateUI();
                gui.getCurrentDocumentView().getAnnotationTreeModel().assignedTo(tree);
            }
        }else if(event == DocumentManager.ON_DOCUMENT_PRE_SWITCH){
            if(subject != null) {
                Document d = (Document) subject;
                d.getInk().unregisterFor(InkInk.ON_CHANGE, this);
                d.getSelection().unregisterFor(Selection.ON_CHANGE, this);
                
                timeSliderRight.setEnabled(false);
                timeSliderLeft.setEnabled(false);
                
                tree.setModel(AnnotationTreeModel.emptyModel());
                tree.setSelectionModel(null);
                tree.updateUI();
            }
        }else if (event.equals(Selection.ON_CHANGE)){
            if(GUI.getInstance().hasDocument()) {
                Selection s = (Selection) subject;
                setInputLabel(s.getSelectionLabel());
                TreeListener.getInstance().setSelectionContent(s,tree);
                setItemsOfTypeComboBox();
            }
        }else if(event.equals(InkInk.ON_CHANGE)){
        	sliderRecofiguration();
        }
    }
    
    
    
    private void addInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(6, 6, 3, 3);
        inputPanel.add(this.typeComboBox, gc);
        
        inputBox = Box.createHorizontalBox();
        
        gc = new GridBagConstraints();
        gc.weightx = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(6, 3, 3, 3);
        inputBox.add(input);
        inputPanel.add(inputBox, gc);
        
        
        JButton button = new JButton(new ImageIcon(this.getClass().getResource("images/Add16.gif")));
        button.setToolTipText("Combine the selected strokes in a group and assign the value in the text field on the leftside of this button");
        button.setPreferredSize(new Dimension(25, 25));
        Contr.getInstance().controll(button, Contr.ADD_GROUP, Contr.DD);
        gc = new GridBagConstraints();
        gc.weightx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 3, 3, 6);
        inputPanel.add(button, gc);
        
        gc = new GridBagConstraints();
        gc.gridwidth = 3;
        gc.weightx = 1;
        gc.gridy = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        this.add(inputPanel, gc);
    }
    
    private void addTree() {
        
        tree = new AnnotationTree(gui);
    	
        JScrollPane sp = new JScrollPane(tree);
        sp.setPreferredSize(new Dimension(150, 150));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 2;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.insets = new Insets(3, 6, 6, 3);
        gc.fill = GridBagConstraints.BOTH;
        this.add(sp, gc);
    }
    
    protected void setItemsOfTypeComboBox() {
        if(gui.getCurrentDocument().getSelection().getContent().size() > 0){
        	String selected = (String) typeComboBox.getSelectedItem();
        	typeComboBox.removeAllItems();
    		for(String s : getAnnotationStructure().getParentTypes(GUI.getInstance().getCurrentDocument().getSelection().getContent())){
    			typeComboBox.addItem(s);
    			if(selected != null && selected.equals(s)){
    				typeComboBox.setSelectedItem(s);
    			}
    		}
        }
    }
    
    private void addStrokePropertyView() {
    	if(strokeProperty != null) {
            remove(strokeProperty);
        }
    	if(gui.hasDocument()){
    		strokeProperty = new AnnotationView(gui.getCurrentDocument().getInk());
    	}else{
    		strokeProperty = new AnnotationView();
    	}
    	GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 2;
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(3, 3, 6, 6);
        //JScrollPane sp = new JScrollPane(strokeProperty);
        //sp.setMinimumSize(new Dimension(200, 50));
        //sp.setBorder(new TitledBorder(new LineBorder(MetalLookAndFeel.getControlShadow()), "Annotation:"));
        add(strokeProperty,gc);
        revalidate();
    }
    /*
    private void addStrokePropertyView() {
        if(strokeProperty != null) {
            this.remove(strokeProperty);
        }
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 2;
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(3, 3, 6, 6);
        strokeProperty = new StrokeProperties();
        this.add(strokeProperty, gc);
        this.revalidate();
    }
    */
    
    public String getInput() {
        if(this.inputIsTextField){
        	return this.input.getText();
        }else{
        	if(this.inputComboBox.isEditable()){
        		return this.inputComboBox.getEditor().getItem().toString();
        	}else{
        		return (String) this.inputComboBox.getSelectedItem();
        	}
        }
    }
    
    public JComboBox getComboBox() {
        return this.typeComboBox;
    }
    
    /**
     * Sets input label (mostly by information retrieved from the selection)
     * @param label new Text label
     */
    public void setInputLabel(String label) {
        //if(label != "") {
            this.input.setText(label);
        //}
    }
    public void clearInputLabel() {
        this.input.setText("");
    }

	public void grabInputFocus() {
		this.getInputComponent().grabFocus();
	}

	private JComponent getInputComponent() {
		if(this.inputIsTextField){
        	return this.input;
        }else{
        	return this.inputComboBox;
        }
	}
	
	private InkAnnoAnnotationStructure getAnnotationStructure(){
	    if(gui.hasDocument()){
	        return gui.getCurrentDocument().getAnnotationStructure();
	    }
	    return  null;
	}

    
}
