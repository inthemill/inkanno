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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.NotImplementedException;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.im2.inkanno.Selection;
import ch.unibe.inkml.InkTraceView;

@SuppressWarnings("serial")
public class TraceProperties extends JPanel implements Observer{
    
    private JComboBox group;
    
    private JButton submit;
    
    private JTextField transcription;
    
    private JButton delete;
    
    public TraceProperties() {
        this.setBorder(new TitledBorder(new LineBorder(MetalLookAndFeel.getControlShadow()), "Stroke Group Preferences"));
        GUI.getInstance().getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_SWITCH, new Observer(){
            public void notifyFor(Aspect event, Object subject) {
                if(GUI.getInstance().hasDocument()) {
                    GUI.getInstance().getCurrentDocument().getSelection().registerFor(Selection.ON_CHANGE, TraceProperties.this);
                }
            }
        });
        
        this.initialize();
        this.configureForDisable();
    }
    
    public void notifyFor(Aspect event, Object subject) {
        Selection s = (Selection) subject;
        List<InkTraceView> ss = new ArrayList<InkTraceView>();
        for(InkTraceView stroke : s.getContent()) {
            if(!stroke.isLeaf()) {
                ss.add(stroke);
            }
        }
        if(ss.size() == 0) {
            this.configureForDisable();
        } else if(ss.size() == 1) {
            this.configureForOne(ss.get(0));
        } else {
            this.configureForMore(ss);
        }
    }
    
    private void configureForDisable() {
        this.transcription.setText("");
        this.transcription.setEnabled(false);
        this.group.setEnabled(false);
        this.submit.setEnabled(false);
        this.delete.setEnabled(false);
    }
    
    private void configureForOne(InkTraceView trace) {
        this.transcription.setText(trace.getLabel());
        this.transcription.setEnabled(true);
        this.group.setEnabled(true);
        this.group.setSelectedItem(trace.getAnnotation("type"));
        this.submit.setEnabled(true);
        this.delete.setEnabled(true);
    }
    
    private void configureForMore(List<InkTraceView> ss) {
        this.transcription.setText("");
        this.transcription.setEnabled(false);
        this.group.setEnabled(true);
        String commonType = "";
        for(InkTraceView s : ss) {
            if(commonType == null || commonType == s.getAnnotation("type")) {
                commonType = s.getAnnotation("type");
            } else {
                commonType = null;
                break;
            }
        }
        this.group.setSelectedItem(commonType);
        this.submit.setEnabled(true);
        this.delete.setEnabled(true);
    }
    
    private JButton getSubmitButton() {
        this.submit = new JButton("Change");
        this.submit.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                List<InkTraceView> ss = getSelectedContainer();
                if(ss.size() == 1) {
                    ss.get(0).annotate("transcription",transcription.getText());
                    ss.get(0).annotate("type", (String) group.getSelectedItem());
                } else {
                    for(InkTraceView s : ss) {
                        s.annotate("type", (String) group.getSelectedItem());
                    }
                }
            }
        });
        return this.submit;
    }
    
    private List<InkTraceView> getSelectedContainer() {
        List<InkTraceView> ss = new ArrayList<InkTraceView>();
        for(InkTraceView s : getDocument().getSelection().getContent()) {
            if(!s.isLeaf()) {
                ss.add( s);
            }
        }
        return ss;
    }
    
    private JButton getDeleteButton() {
        this.delete = new JButton(new ImageIcon(this.getClass().getResource("images/Remove16.gif")));
        this.delete.setToolTipText("Delete the selected stroke group(s)");
        this.delete.setPreferredSize(new Dimension(40, 24));
        this.delete.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                for(InkTraceView s : getSelectedContainer()) {
                	throw new NotImplementedException();
                    //s.delete();
                }
                //getDocument().getCurrentViewRoot().releaseEvents();
            }
        });
        return this.delete;
    }
    
    private void initialize() {
        this.setSize(282, 175);
        this.setLayout(new GridBagLayout());
        
        GridBagConstraints tLabelC = new GridBagConstraints();
        tLabelC.gridx = 0;
        tLabelC.anchor = GridBagConstraints.EAST;
        tLabelC.gridy = 0;
        tLabelC.insets = new Insets(6, 12, 3, 6);
        this.add(new JLabel("Transcription"), tLabelC);
        
        GridBagConstraints tFieldC = new GridBagConstraints();
        tFieldC.gridx = 1;
        tFieldC.anchor = GridBagConstraints.WEST;
        tFieldC.gridy = 0;
        tFieldC.fill = GridBagConstraints.HORIZONTAL;
        tFieldC.weightx = 2.0;
        tFieldC.insets = new Insets(6, 6, 3, 12);
        this.transcription = new JTextField();
        this.add(this.transcription, tFieldC);
        
        GridBagConstraints tyLabelC = new GridBagConstraints();
        tyLabelC.gridx = 0;
        tyLabelC.gridy = 1;
        tyLabelC.gridheight = 1;
        tyLabelC.anchor = GridBagConstraints.NORTHWEST;
        tyLabelC.insets = new Insets(3, 12, 0, 6);
        this.add(new JLabel("Type"), tyLabelC);
        
        JComboBox group = this.getGroup();
        // for(int i=0;i<types.length;i++){
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(3, 0, -8, 12);
        // group.add(new JRadioButton(types[i]),types[i], c);
        // }
        this.add(group, c);
        
        GridBagConstraints dButtonC = new GridBagConstraints();
        dButtonC.gridx = 0;
        dButtonC.gridy = 2;
        dButtonC.insets = new Insets(15, 12, 12, 6);
        dButtonC.anchor = GridBagConstraints.WEST;
        this.add(this.getDeleteButton(), dButtonC);
        
        GridBagConstraints sButtonC = new GridBagConstraints();
        sButtonC.gridx = 1;
        sButtonC.insets = new Insets(15, 0, 12, 12);
        sButtonC.anchor = GridBagConstraints.WEST;
        this.add(this.getSubmitButton(), sButtonC);
    }
    
    private JComboBox getGroup() {
        this.group = new JComboBox();
        // this.group = new ExtButtonGroup();
        setComboBoxElements(this.group);
        
        /*GUI.getInstance().getConfigView().registerFor(AnnotationNames.ON_TYPE_LIST_CHANGE, new Observer(){
            public void notifyFor(Aspect event, Object subject) {
                setComboBoxElements(group);
            }
        });*/
        
        return this.group;
    }
    
    protected void setComboBoxElements(JComboBox cb) {
        cb.removeAllItems();
        cb.addItem("");
    	for(String item : InkAnnoAnnotationStructure.getTraceViewTypes(getDocument().getAnnotationStructure())) {
		    cb.addItem(item);
		}
	}
    
    private Document getDocument() {
        return GUI.getInstance().getCurrentDocument();
    }

    private class ExtButtonGroup extends ButtonGroup{
        private Map<String, AbstractButton> buttons = new HashMap<String, AbstractButton>();
        
        private JRadioButton none;
        
        private String value;
        
        private ActionListener l = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                ExtButtonGroup.this.value = ((JRadioButton) e.getSource()).getText();
            }
        };
        
        public ExtButtonGroup() {
            super();
            this.none = new JRadioButton();
            this.add(this.none);
            this.none.setVisible(false);
        }
        
        public String getValue() {
            return this.value;
        }
        
        public void add(AbstractButton button, String name, GridBagConstraints c) {
            super.add(button);
            this.buttons.put(name, button);
            button.setName(name);
            button.setText(name);
            button.addActionListener(this.l);
            TraceProperties.this.add(button, c);
        }
        
        public void select(String name) {
            if(name == null) {
                this.clear();
            } else {
                this.buttons.get(name).setSelected(true);
            }
        }
        
        public void clear() {
            this.none.setSelected(true);
        }
        
        public void enable(boolean b) {
            for(AbstractButton but : this.buttons.values()) {
                but.setEnabled(b);
            }
        }
    }
}
