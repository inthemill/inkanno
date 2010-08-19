package ch.unibe.im2.inkanno.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.JTextComponent;

import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.eindermu.utils.StringList;
import ch.unibe.eindermu.utils.StringSet;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.Selection;
import ch.unibe.inkml.AnnotationStructure;
import ch.unibe.inkml.InkAnnotatedElement;
import ch.unibe.inkml.AnnotationStructure.Annotation;
import ch.unibe.inkml.AnnotationStructure.Item;
import ch.unibe.inkml.AnnotationStructure.Annotation.AType;

@SuppressWarnings("serial")
public class AnnotationView extends JPanel implements Observer {
	
	private List<InkAnnotatedElement> elements;
	
	private Box box;
	
    private Item model;

    public StringList possibleNames;

    private RowContainer rowContainer;

    
	
	public AnnotationView(InkAnnotatedElement el){
		this();
		this.updateView(el);
	}
	
    public final Object empty = new Object(){
        public String toString(){
            return "";
        }
    };
    
    private Object unequal = new Object(){
        public String toString(){
            return "<diverse>";
        }
    };

	public AnnotationView() {
		
		this.setLayout(new BorderLayout());
		box = Box.createVerticalBox();
		box.setAlignmentX(Box.LEFT_ALIGNMENT);
		
		JScrollPane sp = new JScrollPane(box,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setMinimumSize(new Dimension(250, 50));
        sp.setMinimumSize(new Dimension(200, 50));
        this.setBorder(new TitledBorder(new LineBorder(MetalLookAndFeel.getControlShadow()), "Annotation:"));
		this.add(sp);
		
		GUI.getInstance().getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_SWITCH, this);
		GUI.getInstance().getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_PRE_SWITCH, this);
		
		rowContainer = new RowContainer(box);
		
		model = null;
	}
	
	

	@Override
	public void notifyFor(Aspect event, Object subject) {
		if(event.equals(Selection.ON_CHANGE)){
			Selection s = (Selection) subject;
			if(s.getContent().isEmpty()){
			    updateView(s.getDocument().getInk());
			}else{
			    this.updateView(new ArrayList<InkAnnotatedElement>(s.getContent()));
			}
		}else if(event == DocumentManager.ON_DOCUMENT_SWITCH){
		    if(GUI.getInstance().hasDocument()){
                updateView(GUI.getInstance().getCurrentDocument().getInk());
                GUI.getInstance().getCurrentDocument().getSelection().registerFor(Selection.ON_CHANGE, this);
            }
		}else if(event == DocumentManager.ON_DOCUMENT_PRE_SWITCH){
		    if(GUI.getInstance().hasDocument()){
                GUI.getInstance().getCurrentDocument().getSelection().unregisterFor(Selection.ON_CHANGE, this);
            }
            updateView();
        }
	}
	
	

	private void updateView() {
		rowContainer.removeRows();
		elements = null;
		model = null;
		if(this.getParent()!= null && ((JComponent)this.getParent()).getBorder() != null){
			((TitledBorder)((JComponent)this.getParent()).getBorder()).setTitle("Annotation");
		}
		rowContainer.updateRows();
		box.revalidate();
		revalidate();
		repaint();
	}
	
	public void updateView(InkAnnotatedElement el) {
	    List<InkAnnotatedElement> els = new ArrayList<InkAnnotatedElement>();
		els.add(el);
		updateView(els);
	}
	
	private void updateView(List<InkAnnotatedElement> content) {
		if(content.size() == 0){
			updateView();
			return;
		}
		
		elements = content;
		
        populateModel();
		
		rowContainer.updateRows();
        
		this.repaint();
		if(this.getParent() !=null){
		    this.getParent().repaint();
		}
		box.revalidate();
		this.revalidate();
		
	}
	



    private void populateModel() {
        model = getAnnotationStructure().getItem(elements.get(0));
        for(int i = 1; i<elements.size(); i++){
            InkAnnotatedElement el = elements.get(i);
            if(getAnnotationStructure().getItem(el) != model){
                model = null;
                return;
            }
        }
            
    }



    public Object getValue(String key) {
        String result = null;
        if(elements.get(0).containsAnnotation(key)){
            result = elements.get(0).getAnnotation(key);
        }
        for(int i = 1; i<elements.size();i++){
            if(!elements.get(i).containsAnnotation(key)){
                if(result != null){
                    return unequal;
                }
            }else
            if(!elements.get(i).getAnnotation(key).equals(result)){
                return unequal;
            }
        }
        return (result==null)?empty:result;
    }
	
    protected void setValue(String key, Object value) {
        for(InkAnnotatedElement view : elements){
            if(value == empty || value == null || value.toString().isEmpty()){
                view.removeAnnotation(key);
            }else if(value != unequal && !value.toString().equals(unequal.toString()) ){
                if(!view.containsAnnotation(key) || !view.getAnnotation(key).equals(value.toString())){
                    view.annotate(key,value.toString());
                }
            }
        }
    }
	
	private AnnotationStructure getAnnotationStructure() {
        return GUI.getInstance().getCurrentDocument().getAnnotationStructure();
    }

	private class RowContainer{
	    private List<Row> rows;
	    private Box box;
	    
	    public RowContainer(Box box){
	        this.box = box;
	        rows = new ArrayList<Row>();
	    }
	    public void removeRows() {
            updateRows();
        }
        public void updateRows(){
	        int i = 0;
	        if(model!=null){
	            StringSet set = new StringSet();
	            if(elements.size() == 1){
	                set.addAll(elements.get(0).getAnnotationNames());
	            }
    	        for(Annotation a : model.annotations){
    	            if(rows.size() <= i){
    	                rows.add(new Row(box, a));
    	            }else{
                        rows.get(i).setAnnotation(a);
    	            }
    	            i++;
    	            set.remove(a.name);
    	        }
    	        if(elements.size() == 1){
                    for(String name : set){
                        Annotation a = new Annotation();
                        a.name = name;
                        a.valueType = Annotation.ValueType.FREE;
                        a.type = AType.ANNOTATION;
                        if(rows.size() <= i){
                            rows.add(new Row(box, a));
                        }else{
                            rows.get(i).setAnnotation(a);
                        }
                        i++;
                    }
                    set.addAll(elements.get(0).getAnnotationNames());
                }
	        }
	        
	        while (rows.size() > i){
	            rows.get(i).remove();
	            rows.remove(i);
	        }
	    }
	}
	
	
    private class Row implements ActionListener, DocumentListener{
        private JTextField name;
		private JTextComponent valueTF;
		private JComboBox valueCB;
		public Box box;
		private Box parentBox; 
		private Annotation annotation;
		
		
		
		public Row(Box parentBox, Annotation a){
		    this.parentBox = parentBox;
		    
            box = Box.createHorizontalBox();
            box.setAlignmentX(Box.LEFT_ALIGNMENT);
            
            box.setMinimumSize(new Dimension(150,25));
            box.setMaximumSize(new Dimension(500,25));
            this.name = new JTextField();
            this.name.setMinimumSize(new Dimension(70,25));
            this.name.setPreferredSize(new Dimension(70,25));

            box.add(this.name);
	        
	        setAnnotation(a);
	        
	        parentBox.add(box);
		}
		
		/**
         * @param a
         */
        public void setAnnotation(Annotation a) {
            annotation = a;
            setName(a.name);
        }

        private boolean isComboBox(){
		    return annotation.valueType == Annotation.ValueType.ENUM || annotation.valueType == Annotation.ValueType.PROPOSED;
		}
		
		private void setName(String key) {
            this.name.setText(key);
            this.name.setEditable(false);
            Object value = AnnotationView.this.getValue(key);
            if(isComboBox()){
                if(this.valueTF != null){
                    valueTF.getDocument().removeDocumentListener(this);
                    box.remove(this.valueTF);
                    this.valueTF = null;
                }
                if(valueCB == null){
                    this.valueCB = new JComboBox();
                    valueCB.setRenderer(new AnnotationCellRenderer());
                    this.box.add(valueCB);
                    valueCB.setEditable(false);
                    
                }else{
                    valueCB.removeActionListener(this);
                    valueCB.removeAllItems();
                }
                if(value == unequal){
                    valueCB.addItem(unequal);
                }
                valueCB.addItem(empty);
                for(String s : annotation.values){
                    valueCB.addItem(s);
                }
                if(annotation.valueType == Annotation.ValueType.PROPOSED){
                    valueCB.setEditable(true);
                }
                valueCB.setSelectedItem(value);
                valueCB.addActionListener(this);
            }else{
                if(this.valueCB != null){
                    valueCB.removeActionListener(this);
                    box.remove(this.valueCB);
                    this.valueCB = null;
                }
                String strValue = AnnotationView.this.getValue(key).toString();
                
                if(valueTF == null){
                    if(strValue.length() > 30){
                        valueTF = new JTextArea(strValue, 1+(strValue.length()/30), 30);
                    }else{
                        valueTF = new JTextField(strValue);
                    }
                    this.box.add(valueTF);
                        
                }else{
                    valueTF.getDocument().removeDocumentListener(this);
                    valueTF.setText(strValue);
                }
                valueTF.getDocument().addDocumentListener(this);
            }
            
        }
		
		@Override
        public void actionPerformed(ActionEvent e) {
	        setValue(name.getText(),valueCB.getSelectedItem());
	        if(annotation.triggerValue != null){
	            AnnotationView.this.updateView(AnnotationView.this.elements);
	        }
        }
		
        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }
        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }
        private void update(){
            AnnotationView.this.setValue(name.getText(),valueTF.getText());
            if(Row.this.valueTF instanceof JTextArea){
                ((JTextArea) Row.this.valueTF).setRows((Row.this.valueTF.getText().length()/30) + 1);
            }
        }
		

        public void remove() {
            if(valueTF != null){
                valueTF.getDocument().removeDocumentListener(this);
                box.remove(valueTF);
                valueTF = null;
            }
            if(valueCB != null){
                valueCB.removeActionListener(this);
                box.remove(valueCB);
                valueCB = null;
            }
            if(name != null){
                box.remove(name);
                name = null;
            }
            parentBox.remove(box);
            box = null;
        }
	}

    @SuppressWarnings("serial")
    private class AnnotationCellRenderer extends JLabel implements ListCellRenderer{

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            this.setText(value.toString());
            setForeground(Color.BLACK);
            setBackground(Color.WHITE);
            if(value == empty){
                setForeground(Color.GRAY);
            }else if(value == unequal){
                setBackground(Color.GRAY);
            }
            return this;
        }
        
    }



}
