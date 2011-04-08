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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.unibe.eindermu.utils.AbstractObservable;
import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.Observable;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.eindermu.utils.StringList;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.DrawPropertyManager;
import ch.unibe.im2.inkanno.controller.CanvasController;
import ch.unibe.im2.inkanno.controller.Contr;
import ch.unibe.im2.inkanno.exporter.ExporterException;
import ch.unibe.im2.inkanno.exporter.FactoryException;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;

@SuppressWarnings("serial")
public class GUI extends JFrame implements Observable, Observer{
    
	public static MyFileChooser fileChooser = new MyFileChooser();
    
    private Map<Document, DocumentView> docs = new HashMap<Document, DocumentView>();
    
    private DocumentManager documentManager;
    
    private Menu menu;
    
    private TraceCanvas canvas;
    
    private CanvasController controller;
    
    private static GUI instance;
    
    private ControlPanel controlPanel;
    
    private JToolBar toolbar;
    
    private AbstractObservable observerSupporter = new AbstractObservable();

    private JSlider strokeWidthSlider;

    protected boolean autosave;
    
    public static GUI getInstance() {
        if(GUI.instance == null) {
            GUI.instance = new GUI(null);
        }
        return GUI.instance;
    }
    
    public static boolean hasInstance() {
        return GUI.instance != null;
    }    
    
    public GUI(DocumentManager dm) {
    	GUI.instance = this;
    	JFrame.setDefaultLookAndFeelDecorated(true);
    	this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	this.getContentPane().setLayout(new BorderLayout());
    	
    	
    	this.setDocumentManager(dm);
        this.menu = new Menu(this);
        this.buildGUIContent();
        DrawPropertyManager.getInstance().registerFor(DrawPropertyManager.EVENT_NEW_COLORIZER, this);
        DrawPropertyManager.getInstance().registerFor(new DrawPropertyManager.PropertyChangeEventAspect(DrawPropertyManager.IS_TRACE_GROUP_VISIBLE), this);
        
        
        this.getContentPane().setMinimumSize(new Dimension(300, 300));
        this.pack();
        this.setVisible(true);
        this.setTitle("InkAnno");
        
        
        
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}            
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {
                StringList openDocuments = new StringList();
                for(Document d : getDocumentManager().getLoadedDocuments()){
                    if(!d.isSaved()){
                        openDocuments.add(d.getName());
                    }
                }
                if(openDocuments.isEmpty()){
                    dispose();
                    System.exit(0);
                }else{
                    int answer = JOptionPane.showConfirmDialog(GUI.this,"There are unsaved documents: \n'" +
                            openDocuments.join("','")+
                            "'\nDo you want to have them saved?","Save open documents",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
                    switch(answer){
                    case JOptionPane.CANCEL_OPTION:
                        return;
                    case JOptionPane.NO_OPTION:
                        dispose();
                        System.exit(0);
                        return;
                    case JOptionPane.YES_OPTION:
                        for(Document d : getDocumentManager().getLoadedDocuments()){
                            if(!d.isSaved()){
                                try {
                                    d.save(d.getFile());
                                } catch (ExporterException e1) {
                                    JOptionPane.showMessageDialog(GUI.this, "Was not able to save document '"+d.getName()+"'.\n"+e1.getMessage());
                                    return;
                                } catch (FactoryException e1) {
                                    //will not occure
                                }
                            }
                        }
                        dispose();
                        System.exit(0);
                    }
                }
            }
            @Override
            public void windowClosed(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
        });
    }
    
    
    public void setDocumentManager(DocumentManager dm){
        if(dm == null){
            dm = new DocumentManager();
        }
        documentManager = dm;
        dm.registerFor(DocumentManager.ON_NEW_DOCUMENT, this);
        dm.registerFor(DocumentManager.ON_DOCUMENT_REMOVED, this);
        dm.registerFor(DocumentManager.ON_DOCUMENT_PRESENT, this);
        dm.registerFor(DocumentManager.ON_DOCUMENT_ABSENT, this);
        dm.registerFor(DocumentManager.ON_DOCUMENT_PRE_SWITCH, this);
        dm.registerFor(DocumentManager.ON_DOCUMENT_UNLOADING, this);
    }
    

    @Override
    public void notifyFor(Aspect event, Object subject) {
        //on new documents
        if(event == DocumentManager.ON_NEW_DOCUMENT){
            docs.put((Document) subject, new DocumentView((Document) subject, this));
        }
        
        //on removed documents
        else if(event == DocumentManager.ON_DOCUMENT_REMOVED){
            if(subject instanceof Document){
                docs.remove((Document)subject);
            }else{
                for(Document d : docs.keySet()){
                    if(d.getFile() != null && d.getFile().getPath().equals(subject)){
                        docs.remove(d);
                        break;
                    }
                }
            }
        }
        
        //on current document changed
        else if(event == DocumentManager.ON_DOCUMENT_PRESENT){
            Document d = (Document)subject;
            d.registerFor(Document.ON_CHANGE, this);
            d.registerFor(Document.ON_SAVE, this);
            setTitle("InkAnno" + " - " + d.getName()+((d.isSaved())?"":"*"));
            strokeWidthSlider.setEnabled(true);
            strokeWidthSlider.setValue((int) (getCurrentDocumentView().getStrokeWidth()*4));
            docs.get((Document)subject).activate();
        }
        
        //if no document can be displayed
        else if(event == DocumentManager.ON_DOCUMENT_ABSENT){
            GUI.this.setTitle("InkANNO");
            strokeWidthSlider.setEnabled(false);
        }
        
        //if a document has been unloaded from memory
        else if(event == DocumentManager.ON_DOCUMENT_UNLOADING){
            if(autosave){
                if(!((Document)subject).isSaved()){
                    (new Contr.Save()).saveDocument((Document)subject,false);
                }
            }
        }
        
        //before a document will be removed from the screen.
        else if(event == DocumentManager.ON_DOCUMENT_PRE_SWITCH){
            if(subject != null){
                ((Document)subject).unregisterFor(Document.ON_CHANGE, this);
                ((Document)subject).unregisterFor(Document.ON_SAVE, this);
            }
        }
        
        //if the current document has changed its status from saved to unsaved 
        else if(event == Document.ON_CHANGE){
            setTitle("InkAnno" + " - " + ((Document)subject).getName()+"*");
        }
        
        //if the current document has changed its status from unsaved to saved
        else if(event == Document.ON_SAVE){
            setTitle("InkAnno" + " - " + ((Document)subject).getName());
        }
        //if colorizer has changed
        else if(event == DrawPropertyManager.EVENT_NEW_COLORIZER){
        	canvas.repaint();
        }else if(event instanceof DrawPropertyManager.PropertyChangeEventAspect){
        	canvas.repaint();
        }
    }
    
    
    private void buildGUIContent() {
        this.createToolBar();
        JPanel panel = new JPanel(new GridBagLayout());
        
        this.canvas = new TraceCanvas(this, panel);
        this.controller = new CanvasController(this, this.canvas);
        this.canvas.addMouseListener(this.controller);
        this.canvas.addMouseMotionListener(this.controller);
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.CENTER;
        panel.add(this.canvas, gc);
        
        JScrollPane scrollPane = new JScrollPane(panel){
            
        };
        scrollPane.setWheelScrollingEnabled(false);
        scrollPane.addMouseWheelListener(new CanvasMouseWheelListener(canvas,scrollPane));
        scrollPane.setPreferredSize(new Dimension(600, 400));
        scrollPane.setMinimumSize(new Dimension(400, 300));
        
        
        this.controlPanel = new ControlPanel(this);
        // this.getContentPane().add(this.controlPanel,BorderLayout.SOUTH);
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, this.controlPanel);
        sp.setDividerLocation(400);
        sp.setResizeWeight(1);
        this.getContentPane().add(sp);
    }
    
    private void createToolBar() {
        this.toolbar = new JToolBar("Toolbar");
        this.add(this.toolbar, BorderLayout.PAGE_START);
        this.toolbar.setFloatable(false);
        JButton b;
        // open
        b = new JButton(new ImageIcon(this.getClass().getResource("images/Open16.gif")));
        b.setToolTipText("Open an other file");
        b.addActionListener(new Contr.OpenDocument());
        this.toolbar.add(b);
        // save
        b = new JButton(new ImageIcon(this.getClass().getResource("images/Save16.gif")));
        b.setToolTipText("Save this annotation");
        b.addActionListener(new Contr.Save());
        setInSyncWithDocumentPresence(b);
        this.toolbar.add(b);
        // close
        b = new JButton(new ImageIcon(this.getClass().getResource("images/Close16.png")));
        b.setToolTipText("Close the current document");
        Contr.getInstance().controll(b, Contr.CLOSE, Contr.DD);
        this.toolbar.add(b);
        // separator
        this.toolbar.addSeparator();

        // zooming
        b = new JButton(new ImageIcon(this.getClass().getResource("images/ZoomIn16.gif")));
        b.setToolTipText("Zoom In");
        b.addActionListener(new Contr.ZoomIn());
        setInSyncWithDocumentPresence(b);
        this.toolbar.add(b);
        // zoomout
        b = new JButton(new ImageIcon(this.getClass().getResource("images/ZoomOut16.gif")));
        b.setToolTipText("Zoom Out");
        b.addActionListener(new Contr.ZoomOut());
        setInSyncWithDocumentPresence(b);
        this.toolbar.add(b);
        
        // vmirror
        b = new JButton(new ImageIcon(this.getClass().getResource("images/VMirror16.png")));
        b.setToolTipText("Mirror vertically");
        b.addActionListener(new Contr.VMirroring());
        setInSyncWithDocumentPresence(b);
        this.toolbar.add(b);
        // hmirror
        b = new JButton(new ImageIcon(this.getClass().getResource("images/HMirror16.png")));
        b.setToolTipText("Mirror horizontally");
        b.addActionListener(new Contr.HMirroring());
        setInSyncWithDocumentPresence(b);
        this.toolbar.add(b);
        // seperator
        this.toolbar.addSeparator();
        // Preferences
        
        // seperator
        this.toolbar.addSeparator();
        // strokeWeight
        toolbar.add(new JLabel("Stroke Width:"));
        
        // strokeWidthSlider
        strokeWidthSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 15);
        strokeWidthSlider.setPreferredSize(new Dimension(50, 25));
        strokeWidthSlider.setMaximumSize(new Dimension(100, 25));
        strokeWidthSlider.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
            	if(hasDocument()){
            	    getCurrentDocumentView().setStrokeWidth(strokeWidthSlider.getValue()/4.0);
            	}
                getCanvas().repaint(10);
            }
        });
        toolbar.add(strokeWidthSlider);
        
        b = new JButton("<-");
        b.setToolTipText("Open previous document.");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(getDocumentManager().hasPrev()){
                    try {
                        getDocumentManager().prevDocument();
                    } catch (InvalidDocumentException e1) {
                        JOptionPane.showMessageDialog(GUI.this,"Can't open previous message: "+e1.getMessage(),"",JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        toolbar.add(b);        
        

        
        JCheckBox cb = new JCheckBox();
        cb.setToolTipText("Autosave: save current document befor changing to the next one. Warning, does not ask!");
        cb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox jb = (JCheckBox)e.getSource();
                autosave = jb.isSelected();
            }
        });
        toolbar.add(cb);
        
        b = new JButton("->");
        b.setToolTipText("Open next document.");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(getDocumentManager().hasNext()){
                    try {
                        getDocumentManager().nextDocument();
                    } catch (InvalidDocumentException e1) {
                        JOptionPane.showMessageDialog(GUI.this,"Can't open next message: "+e1.getMessage(),"",JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        toolbar.add(b);
    }
    
    /*public void addDocument(Document document) {
        docs.put(document, new DocumentView(document, this));
        this.setToCurrentDocument(document);
    }*/
    
    public CanvasController getController() {
        return this.controller;
    }
    /*
    public void setToCurrentDocument(Document document) {
        this.notifyObserver(ON_DOCUMENT_PRE_SWITCH, this);
        this.currentDocument = document;
        this.docs.get(document).activate();
        this.notifyObserver(ON_DOCUMENT_SWITCH,this);
        if(currentDocument != null){
            notifyObserver(ON_DOCUMENT_PRESENT,document);
        }else{
            notifyObserver(ON_DOCUMENT_ABSENT,this);
        }
    }
    */
    
    public DocumentManager getDocumentManager(){
        return documentManager;
    }
    
    public Document getCurrentDocument() {
        return documentManager.getCurrentDocument();
    }
    
    /**
     * Returns true if a document is currently open
     * @return
     */
    public boolean hasDocument() {
        return documentManager.hasCurrentDocument();
    }
    
    public DocumentView getCurrentDocumentView() {
        return docs.get(this.getCurrentDocument());
    }
    
    /**
     * @param d
     */
    public void reloadDocument(Document d) {
        getDocumentManager().notifyObserver(DocumentManager.ON_DOCUMENT_PRESENT, d);
        getDocumentManager().notifyObserver(DocumentManager.ON_DOCUMENT_SWITCH, d);
        
    } 
    /*
    public void removeDocument(Document d) {
        docs.remove(d);
        if(docs.isEmpty()) {
            notifyObserver(ON_DOCUMENT_PRE_SWITCH, this);
            currentDocument = null;
            notifyObserver(ON_DOCUMENT_SWITCH,this);
            notifyObserver(ON_DOCUMENT_ABSENT,this);
        } else {
            if(currentDocument == d){
                setToCurrentDocument(docs.keySet().iterator().next());
            }
        }
    }
    */
    
    
    public Menu getMenu() {
        return this.menu;
    }
    
    public TraceCanvas getCanvas() {
        return this.canvas;
    }
    
    
    
    
    public ControlPanel getControlPanel() {
        return this.controlPanel;
    }
    
    /*
    public Set<Document> getDocuments() {
        return this.docs.keySet();
    }
    */
   
    
    /**
     * Sets the specified button in Sync with document presence. i.e. If 
     * a document is shown, the button is enabled, otherwise it is disabled.
     * @param button which is enabled when document is present and disabled if document is absent
     */
    public void setInSyncWithDocumentPresence(final AbstractButton button) {
        getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_PRESENT, new Observer() {
            @Override
            public void notifyFor(Aspect event, Object subject) {
                button.setEnabled(true);
            }
        });
        getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_ABSENT, new Observer() {
            @Override
            public void notifyFor(Aspect event, Object subject) {
                button.setEnabled(false);
            }
        });
        
    }
    
    


    public void registerFor(Aspect event, Observer o) {
        this.observerSupporter.registerFor(event, o);
    }
    
    public void notifyObserver(Aspect event,Object subject) {
        this.observerSupporter.notifyObserver(event,subject);
    }
    
    
    
}