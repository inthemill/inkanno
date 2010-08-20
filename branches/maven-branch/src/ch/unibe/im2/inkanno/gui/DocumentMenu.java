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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import ch.unibe.im2.inkanno.AnnotationStructureValidator;
import ch.unibe.im2.inkanno.controller.Contr;

public class DocumentMenu{
    
    @SuppressWarnings("unused")
    private DocumentView view;
    
    private Menu menu;
    
    private JMenu root;
    
    public DocumentMenu(DocumentView view, Menu menu) {
        this.view = view;
        this.menu = menu;
    }
    
    private void init() {
        this.root = new JMenu("Document");
        this.root.setMnemonic(KeyEvent.VK_D);
        JMenuItem save = new JMenuItem("Save", new ImageIcon(this.getClass().getResource("images/Save16.gif")));
        save.setMnemonic(KeyEvent.VK_S);
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        save.addActionListener(new Contr.Save());
        this.root.add(save);
        
        JMenuItem saveAs = new JMenuItem("Save As", new ImageIcon(this.getClass().getResource("images/Save16.gif")));
        saveAs.addActionListener(new Contr.SaveAs());
        this.root.add(saveAs);
        
        JMenuItem close = new JMenuItem("Close", new ImageIcon(this.getClass().getResource("images/Close16.png")));
        close.setActionCommand(Contr.CLOSE);
        close.setMnemonic(KeyEvent.VK_C);
        close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_MASK));
        close.addActionListener(Contr.getInstance());
        this.root.add(close);
        
        JMenuItem printAsPdf = new JMenuItem("Export as PDF", new ImageIcon(this.getClass().getResource("images/Close16.png")));
        printAsPdf.setActionCommand(Contr.PRINT_AS_PDF);
        printAsPdf.setMnemonic(KeyEvent.VK_P);
        printAsPdf.addActionListener(Contr.getInstance());
        this.root.add(printAsPdf);
        
        JMenuItem exportAsImage = new JMenuItem("Export as Image");
        exportAsImage.setActionCommand(Contr.EXPORT_AS_IMAGE);
        exportAsImage.addActionListener(Contr.getInstance());
        this.root.add(exportAsImage);
        
        JMenuItem validate = new JMenuItem("Validate");
        validate.setActionCommand("validate");
        validate.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                AnnotationStructureValidator.getInstance().validateDocument(view.getDocument(),view.getDocument().getSelection() , GUI.getInstance());
            }
        });
        this.root.add(validate);
        
        JMenuItem documentProperties = new JMenuItem("Properties");
        documentProperties.setActionCommand(Contr.DOCUMENT_PROPERTIES);
        documentProperties.addActionListener(Contr.getInstance());
        this.root.add(documentProperties);
        
        
    }
    
    public void activate() {
        if(this.root == null) {
            this.init();
        }
        this.menu.setDocumentMenu(this.getMenu());
    }
    
    public JMenu getMenu() {
        return this.root;
    }
}
