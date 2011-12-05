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
import java.util.Collection;
import java.util.SortedSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.NotImplementedException;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.Selection;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.TraceBound;

@SuppressWarnings("serial")
public class SelectionView extends JPanel implements Observer{
    
    private JLabel nrOfStrokes;
    
    private JLabel startTime;
    
    private JLabel endTime;

    private JLabel topleft;

    private JLabel dimension;
    
    public SelectionView() {
        this.setBorder(new TitledBorder(new LineBorder(MetalLookAndFeel.getControlShadow()), "Selection Details"));
        GUI.getInstance().getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_SWITCH, new Observer(){
            public void notifyFor(Aspect event, Object subject) {
                if(GUI.getInstance().hasDocument()) {
                    GUI.getInstance().getCurrentDocument().getSelection().registerFor(Selection.ON_CHANGE, SelectionView.this);
                }
            }
        });
        
        this.initialize();
        // this.configureForDisable();
        this.setPreferredSize(new Dimension(210, 250));
        this.setMinimumSize(new Dimension(210, 250));
    }
    
    private void initialize() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(3, 3, 3, 3);
        // nr of strokes
        JLabel l = new JLabel("Traces:");
        this.add(l, gc.clone());
        
        nrOfStrokes = new JLabel("(0)");
        this.add(nrOfStrokes, gc.clone());
        
        // time
        l = new JLabel("From (s):");
        
        gc.gridy = 1;
        this.add(l, gc.clone());
        
        startTime = new JLabel();
        //startTime.setPreferredSize(new Dimension(110, 20));
        this.add(startTime, gc.clone());
        
        // time
        l = new JLabel("To (s):");
        gc.gridy++;
        this.add(l, gc.clone());
        
        endTime = new JLabel();
        this.add(endTime, gc.clone());
        
        // bounding box
        l = new JLabel("Box:");
        gc.gridy++;
        this.add(l, gc.clone());
        
        topleft = new JLabel();
        this.add(topleft, gc.clone());
        
        l = new JLabel("Box-dim:");
        gc.gridy++;
        this.add(l, gc.clone());
        
        dimension = new JLabel();
        this.add(dimension, gc.clone());
        
        // button
        JButton b = new JButton("Change");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                Collection<InkTraceView> s = GUI.getInstance().getCurrentDocument().getSelection().getContent();
                GUI.getInstance().getCurrentDocument();
                if(s.size() > 0) {
                	throw new NotImplementedException();
                    //d.setTime(s.get(0), Double.valueOf(startTime.getText()));
                    //notifyFor(null, null);
                }
            }
        });
        gc.insets = new Insets(15, 3, 3, 3);
        gc.gridy++;
        gc.gridx = 1;
        this.add(b, gc);
    }
    
    public void notifyFor(Aspect event, Object subject) {
        SortedSet<InkTraceView> s = GUI.getInstance().getCurrentDocument().getSelection().getContent();
        if(s.size() > 0) {
            double zero = s.first().getInk().getViewRoot().getTimeSpan().start;
            startTime.setText(doubleValue(s.first().getTimeSpan().start-zero));
            startTime.setToolTipText("relative to: "+doubleValue(zero));
            endTime.setText(doubleValue(s.last().getTimeSpan().end-zero));
            endTime.setToolTipText("Delta: "+ "("+doubleValue(s.last().getTimeSpan().end-s.first().getTimeSpan().start)+")");
            TraceBound tb = new TraceBound(s.first().getBounds());
            String traces = "("+s.size()+")";
            String names = "";
            for(InkTraceView trace : s){
                tb.add(trace.getBounds());
                if(trace.isLeaf()){
                    names +=","+((InkTraceViewLeaf)trace).getTrace().getId();
                }else{
                    names +=","+trace.getId();
                }
            }
            nrOfStrokes.setToolTipText(names);
            if(s.size() < 4){
                nrOfStrokes.setText(traces+names);
            }else{
                nrOfStrokes.setText(traces);
            }
            topleft.setText(""+tb.x+"x"+tb.y);
            dimension.setText("\n"+tb.width+"x"+tb.height);
        } else {
            nrOfStrokes.setText("(0)");
            startTime.setText("");
            endTime.setText("");
            topleft.setText("");
        }
    }
    
    private String doubleValue(double d) {
        long l = (long) (d * 1000);
        String str = "";
        long z = 10;
        if(l < 0){
            str+="-";
        }
        l = Math.abs(l);
        if(l==0){
            str+="0";
        }
        while(l > 0) {
            long t = l % z;
            l = l - t;
            str = Long.toString(t * 10 / z) + str;
            if(z == 1000) {
                str = "." + str;
            }
            z = z * 10;
        }
        return str;
    }
    
}
