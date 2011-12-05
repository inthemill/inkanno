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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.FileUtil;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.DrawPropertyManager;
import ch.unibe.im2.inkanno.controller.Contr;
import ch.unibe.im2.inkanno.gui.color.Colorizer;
import ch.unibe.im2.inkanno.gui.color.ColorizerCallback;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;

public class Menu implements Observer{
    private GUI gui;
    
    private JMenuBar bar;
    
    private JMenu program;
    
    private JMenu document;
    
    private JMenu window;
    
    public Menu(GUI gui) {
        this.gui = gui;
        this.init();
    }
    
    public void init() {
        this.bar = new JMenuBar();
        gui.setJMenuBar(this.bar);

        //Menu Programm
        this.program = new JMenu("Program");
        program.setMnemonic(KeyEvent.VK_P);
        this.bar.add(this.program);
        
        JMenuItem open = new JMenuItem("Open Document");
        open.addActionListener(new Contr.OpenDocument());
        open.setMnemonic(KeyEvent.VK_O);
        open.setIcon(new ImageIcon(this.getClass().getResource("images/Open16.gif")));
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        this.program.add(open);
        
        /*JMenuItem options = new JMenuItem("Preferences", new ImageIcon(this.getClass().getResource("images/Preferences16.gif")));
        Contr.getInstance().controll(options, Contr.OPTIONS);
        options.setMnemonic(KeyEvent.VK_P);
        options.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
        this.program.add(options);
        */
        
      //Menu view
        final JMenu view = new JMenu("View");
        view.setMnemonic(KeyEvent.VK_V);
        
        final JMenu changeColorizer = new JMenu("Colorization");
        ButtonGroup g = new ButtonGroup();
        final DrawPropertyManager cm = DrawPropertyManager.getInstance();
        
        for(ColorizerCallback cb : cm.getColorizerCallbacks()){
        	JRadioButtonMenuItem colorizerMenuItem = new JRadioButtonMenuItem(cb.getLabel());
        	g.add(colorizerMenuItem);
        	colorizerMenuItem.addActionListener(cm.new ColorizerActionListener(cb));
        	colorizerMenuItem.setSelected(cb.isSelected());
        	changeColorizer.add(colorizerMenuItem);
        }
        	
        /*for(Colorizer c : cm.getColorizers()){
        	JRadioButtonMenuItem colorizerMenuItem = new JRadioButtonMenuItem(c.getCaption());
        	g.add(colorizerMenuItem);
        	colorizerMenuItem.addActionListener(new Contr.ChangeColorizer(c));
        	colorizerMenuItem.setSelected(cm.getCurrentColorizer() == c);
        	
        	
        	changeColorizer.add(colorizerMenuItem);
        }*/
        
        
        gui.setInSyncWithDocumentPresence(changeColorizer);
        changeColorizer.setMnemonic(KeyEvent.VK_C);
        cm.registerFor(DrawPropertyManager.EVENT_NEW_COLORIZER,new Observer() {
			@Override
			public void notifyFor(Aspect event, Object subject) {
				for(Component cmi : changeColorizer.getMenuComponents()){
					JRadioButtonMenuItem rbmi = (JRadioButtonMenuItem) cmi;
					rbmi.setSelected(rbmi.getText().equals(cm.getCurrentColorizer().getCaption()));
				}
			}
		});
        view.add(changeColorizer);
        
        
        
        final JMenuItem  toggleContainerVisibility = new JCheckBoxMenuItem("Toggle Container Visibility");
        toggleContainerVisibility.setSelected(cm.getBProperty(DrawPropertyManager.IS_TRACE_GROUP_VISIBLE));
        toggleContainerVisibility.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cm.setProperty(DrawPropertyManager.IS_TRACE_GROUP_VISIBLE, ! cm.getBProperty(DrawPropertyManager.IS_TRACE_GROUP_VISIBLE));
			}
		});
        cm.registerFor(new DrawPropertyManager.PropertyChangeEventAspect(DrawPropertyManager.IS_TRACE_GROUP_VISIBLE), new Observer() {
			@Override
			public void notifyFor(Aspect event, Object subject) {
				toggleContainerVisibility.setSelected(cm.getBProperty(DrawPropertyManager.IS_TRACE_GROUP_VISIBLE));
			}
		});
        view.add(toggleContainerVisibility);
        
        this.bar.add(view);
        
        //Menu action
        JMenu action = new JMenu("Action");
        action.setMnemonic(KeyEvent.VK_A);
        Contr.getInstance().controll(action, "", Contr.DD);
        
        JMenuItem vmirroring = new JMenuItem("Flip verictal", new ImageIcon(this.getClass().getResource("images/VMirror16.png")));
        vmirroring.addActionListener(new Contr.VMirroring());
        gui.setInSyncWithDocumentPresence(vmirroring);
        vmirroring.setMnemonic(KeyEvent.VK_V);
        vmirroring.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
        action.add(vmirroring);
        
        JMenuItem hmirroring = new JMenuItem("Flip horizontal", new ImageIcon(this.getClass().getResource("images/HMirror16.png")));
        hmirroring.addActionListener(new Contr.HMirroring());
        gui.setInSyncWithDocumentPresence(hmirroring);
        hmirroring.setMnemonic(KeyEvent.VK_H);
        hmirroring.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK));
        action.add(hmirroring);
        
        JMenuItem dmirroring = new JMenuItem("Flip diagonal", new ImageIcon(this.getClass().getResource("images/DMirror16.png")));
        Contr.getInstance().controll(dmirroring, Contr.DMIRRORING, Contr.DD);
        dmirroring.setMnemonic(KeyEvent.VK_D);
        dmirroring.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK));
        action.add(dmirroring);
        
        JMenuItem zoomIn = new JMenuItem("Zoom in", new ImageIcon(this.getClass().getResource("images/ZoomIn16.gif")));
        zoomIn.addActionListener(new Contr.ZoomIn());
        gui.setInSyncWithDocumentPresence(zoomIn);
        zoomIn.setMnemonic(KeyEvent.VK_I);
        zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
        action.add(zoomIn);
        
        JMenuItem zoomOut = new JMenuItem("Zoom out", new ImageIcon(this.getClass().getResource("images/ZoomOut16.gif")));
        zoomOut.addActionListener(new Contr.ZoomOut());
        gui.setInSyncWithDocumentPresence(zoomOut);
        zoomOut.setMnemonic(KeyEvent.VK_O);
        zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
        action.add(zoomOut);
        
        this.bar.add(action);
        
        this.window = new JMenu("Window");
        this.bar.add(this.window);
        
        gui.getDocumentManager().registerFor(DocumentManager.ON_NEW_DOCUMENT, this);
        gui.getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_REMOVED, this);
        loadDocumentsMenu();
    }
    
    @Override
    public void notifyFor(Aspect event, Object subject) {
        if(event == DocumentManager.ON_NEW_DOCUMENT || event == DocumentManager.ON_DOCUMENT_REMOVED){
            window.removeAll();
            loadDocumentsMenu();
        }
    }
    
    /**
     * 
     */
    private void loadDocumentsMenu() {
        DocumentManager dm = gui.getDocumentManager(); 
        for(String path : dm.getFileNames()) {
            JMenuItem di = new JMenuItem(FileUtil.getInfo(path).name);
            di.addActionListener(new DocumentsSelectionListener(path));
            window.add(di);
        }
        if(!dm.hasCurrentDocument()){
            JMenu d = new JMenu("Document");
            d.setEnabled(false);
            setDocumentMenu(d);
        } 
        bar.revalidate();
        bar.repaint();
        
    }

    private class DocumentsSelectionListener implements ActionListener{
        private String path;
        
        public DocumentsSelectionListener(String path) {
            this.path = path;
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
                gui.getDocumentManager().setCurrentDocument(path);
            } catch (InvalidDocumentException e1) {
                JOptionPane.showMessageDialog(gui, String.format("Can't show document '%s': %s",path,e1.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void setDocumentMenu(JMenu menu) {
        if(this.document != null) {
            this.bar.remove(this.document);
        }
        this.document = menu;
        this.bar.add(this.document, 1);
        this.bar.revalidate();
        this.bar.repaint();
    }


    
}
