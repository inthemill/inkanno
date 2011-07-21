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

package ch.unibe.im2.inkanno.controller;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ch.unibe.eindermu.Messenger;
import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.DrawPropertyManager;
import ch.unibe.im2.inkanno.InkAnno;
import ch.unibe.im2.inkanno.InkAnnoAnnotationStructure;
import ch.unibe.im2.inkanno.DocumentRecognizer.FileType;
import ch.unibe.im2.inkanno.exporter.Exporter;
import ch.unibe.im2.inkanno.exporter.ExporterException;
import ch.unibe.im2.inkanno.exporter.ExporterFactory;
import ch.unibe.im2.inkanno.exporter.FactoryException;
import ch.unibe.im2.inkanno.gui.AnnotationView;
import ch.unibe.im2.inkanno.gui.GUI;
import ch.unibe.im2.inkanno.gui.color.Colorizer;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
import ch.unibe.inkml.InkCanvasTransform;
import ch.unibe.inkml.InkContext;
import ch.unibe.inkml.InkMLComplianceException;

public class Contr implements ActionListener{


    /**
     * @author emanuel
     *
     */
    private static Contr inst;
    
    public static final String CLOSE = "CLOSE";
    
    public static final String ADD_GROUP = "ADD_GROUP";
    
    public static final String PRINT_AS_PDF = "PRINT_AS_PDF";
    
    public static final int NONE = 0;
    
    public static final int DD = 1;

	public static final String DMIRRORING = "DMIRRORING";

	public static final String EXPORT_AS_IMAGE = "EXPORT_AS_IMAGE";

    public static final String DOCUMENT_PROPERTIES = "DOCUMENT_PROPERTIES";

	
    private List<ActionSource> buttonsToEnable = new ArrayList<ActionSource>();
    
    public static Contr getInstance() {
        if(inst == null) {
            inst = new Contr();
        }
        return inst;
    }
    
    private Contr() {
        GUI.getInstance().getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_SWITCH, new Observer(){
            public void notifyFor(Aspect event, Object subject) {
                boolean enabled = GUI.getInstance().hasDocument();
                for(ActionSource b : Contr.this.buttonsToEnable) {
                    b.setEnabled(enabled);
                }
            }
        });
    }
    
    public void controll(ActionSource b, String actionCommand, int disableBehaviour){
    	b.setActionCommand(actionCommand);
        b.addActionListener(Contr.getInstance());
        if(disableBehaviour == DD) {
            Contr.getInstance().buttonsToEnable.add(b);
        }
    }
    
    public void controll(AbstractButton b, String actionCommand, int disableBehaviour) {
    	controll(new ActionSourceWrapper(b),actionCommand,disableBehaviour);
    }
    
    
    public class ActionSourceWrapper implements ActionSource{
    	private AbstractButton b;
    	public ActionSourceWrapper(AbstractButton b){
    		this.b = b;
    	}
    	public void setActionCommand(String command){
    		b.setActionCommand(command);
    	}
    	public void addActionListener(ActionListener l){
    		b.addActionListener(l);
    	}
		@Override
		public void setEnabled(boolean enabled) {
			b.setEnabled(enabled);
		}
    	
    	
    }
    
    public void controll(AbstractButton b, String cmd) {
        controll(b, cmd, NONE);
    }
    
    public void actionPerformed(ActionEvent e) {
        try{
        String c = e.getActionCommand();
        if(c == CLOSE)
            this.closeDocument(null);
        else if(c == DMIRRORING)
            this.dMirroring();
        else if(c == ADD_GROUP)
            GUI.getInstance().getController().addGroup();
        else if(c == PRINT_AS_PDF)
        	this.printAsPDF();
        else if(c == EXPORT_AS_IMAGE)
        	exportAsImage();
        else if(c == DOCUMENT_PROPERTIES){
            editDocumentProperties();
        }
        }catch(Error error){
            JOptionPane.showMessageDialog(GUI.getInstance(), "Could execute this action: \n" + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editDocumentProperties() {
        JDialog d = new JDialog(GUI.getInstance());
        d.add(new AnnotationView(GUI.getInstance().getCurrentDocument().getInk()));
        d.setModalityType(ModalityType.APPLICATION_MODAL);
        d.pack();
        d.setVisible(true);
    }

    private void exportAsImage() {
       	Exception failed = new Exception();
    	Document d = GUI.getInstance().getCurrentDocument();
    	ExporterFactory ef = null;
    	Exporter ex = null;
    	try{
    		ef = new ExporterFactory();
    		ex = ef.createExporter("ch.unibe.im2.inkanno.imageExport.ImageExporter");
	    }catch (FactoryException e) {
	    	JOptionPane.showMessageDialog(GUI.getInstance(), e.getMessage());
	    	return;
		}
	    
    
    
        while(failed != null) {
            failed = null;
            try {
                ex.setDocument(d);
                ex.setFilter(d.getTraceFilter());
                JFileChooser fc = ex.getCustomFileChooser(d);
                if(fc == null){
                    fc = GUI.fileChooser;
                }
                fc.setCurrentDirectory(d.getFile().getParentFile());
                boolean retry = true;
                File tmpFile = null;
                while(retry){
                    retry = false;
                    if(fc.showSaveDialog(GUI.getInstance()) == JFileChooser.APPROVE_OPTION){
                        tmpFile = fc.getSelectedFile();
                        if (tmpFile.exists()){
                            if(JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(GUI.getInstance(), "File exists already, do you like to replace it", "File exists already", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                                retry = true;
                            }
                        }
                    }else{
                        throw new ExporterException("cancel");
                    }
                }
                ex.setFile(tmpFile);
                ex.export();
            } catch(ExporterException e) {
                failed = e;
            }
            if(failed != null) {
                if(failed.getMessage().equals("cancel")){
                    break;
                }
                if(JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(GUI.getInstance(), failed.getMessage()
                        + "\n do you wish to try it again?", "Error while saving", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR)) {
                    break;
                }
            }
        }
	}

	private void dMirroring() {
    	InkContext context = GUI.getInstance().getCurrentDocument().getCurrentViewRoot().getContext();
    	InkCanvasTransform transform = context.getCanvasTransform();
    	transform.flipAxis(context.getSourceFormat(),context.getCanvasTraceFormat());
    	try {
    		Document d = GUI.getInstance().getCurrentDocument();
			d.getInk().reloadTraces();
			GUI.getInstance().reloadDocument(d);
		} catch (InkMLComplianceException e) {
			transform.flipAxis(context.getSourceFormat(),context.getCanvasTraceFormat());
			JOptionPane.showMessageDialog(GUI.getInstance(), "Could not flip axis because of: \n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void printAsPDF() {
    	if(GUI.fileChooser.showSaveDialog(GUI.getInstance()) == JFileChooser.APPROVE_OPTION) {
	    	File f = GUI.fileChooser.getSelectedFile();
	        Document d = GUI.getInstance().getCurrentDocument();
	        Exception fail = null;
	        try {
	        	ExporterFactory factory = new ExporterFactory();
	        	Exporter ex = factory.createExporter("ch.unibe.im2.inkanno.pdfExport.PDFExporter");
	        	ex.setDocument(d);
	        	ex.setFile(f);
				ex.export();
			} catch (ExporterException e) {
				e.printStackTrace();
				fail = e;
			} catch (FactoryException e) {
				e.printStackTrace();
				fail = e;
			}
			if(fail != null){
				JOptionPane.showMessageDialog(GUI.getInstance(), "Error while exporting to PDF: "+ fail.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
			}
    	}
	}

   
    public void closeDocument(Document d) {
        if(d == null){
            d = GUI.getInstance().getCurrentDocument();
        }
        if(!d.isSaved()) {
            int answer = JOptionPane.showConfirmDialog(GUI.getInstance(), "Document " + d.getName() + " has not been saved, do you want do save it now?", "Document not saved",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
            if(answer == JOptionPane.CANCEL_OPTION) {
                return;
            } else if(answer == JOptionPane.OK_OPTION) {
                Save saver =  new Save();
                if(!saver.saveDocument(d,false)) {
                    return;
                }
            }
        }
        d.close();
        try {
            GUI.getInstance().getDocumentManager().removeCurrentDocument();
        } catch (InvalidDocumentException e) {
            JOptionPane.showMessageDialog(GUI.getInstance(), String.format("Document is closed, but: %s",e.getMessage()));
        }
    }
    
    public static class Save implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            saveDocument(GUI.getInstance().getCurrentDocument(),false);
        }
        
        public boolean saveDocument(Document d, boolean askFile){
        	Exception failed = new Exception();
        	if(d == null){
        	   d = GUI.getInstance().getCurrentDocument();
        	}
            while(failed != null) {
                failed = null;
                if(d.getFile() != null){
                    if(!askFile && d.getType() == FileType.INKML){
                        try {
                            if(d.isSaved()){
                                Messenger.warn("Saving unchanged document!");
                            }
                            d.save(d.getFile().getAbsoluteFile());
                            Messenger.inform(String.format("Document '%s' has been saved to %s.",d.getName(),d.getFile().getPath()));
                            return true;
                        } catch (ExporterException e) {
                            failed = e;
                        } catch (FactoryException e) {
                            failed = e;
                        }
                    }
                	GUI.fileChooser.setCurrentDirectory(d.getFile().getAbsoluteFile().getParentFile());
                	GUI.fileChooser.setSelectedFile(d.getFile().getAbsoluteFile());
                }
                if(GUI.fileChooser.showSaveDialog(GUI.getInstance()) == JFileChooser.APPROVE_OPTION) {
                	if (GUI.fileChooser.getSelectedFile().exists() && !GUI.fileChooser.getSelectedFile().equals(d.getFile().getAbsoluteFile())){
                		if(JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(GUI.getInstance(), "File exists already, do you like to replace it", "File exists already", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                			failed = new Exception();
                            continue;
                        }
                	}
                    try {
                        d.save(GUI.fileChooser.getSelectedFile());
                    } catch(ExporterException e) {
                        failed = e;
                    } catch (FactoryException e) {
    					failed = e;
    				}
                    if(failed != null) {
                        if(JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(GUI.getInstance(), "There was an Error while saving Document: \n" + failed.getMessage()
                                + "\n do you wish to try it again?", "Error while saving", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }
 
    /**
     * @author emanuel
     *
     */
    public static class SaveAs extends Save {

        @Override
        public void actionPerformed(ActionEvent e) {
            saveDocument(GUI.getInstance().getCurrentDocument(),true);
        }

    }
    
    public static class OpenDocument implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            GUI g = GUI.getInstance();
            GUI.fileChooser.setMultiSelectionEnabled(true);
            int rv = GUI.fileChooser.showOpenDialog(g);
            if(rv == JFileChooser.APPROVE_OPTION) {
                for(File file : GUI.fileChooser.getSelectedFiles()) {
                    try {
                        g.getDocumentManager().addDocument(new Document(file,new InkAnnoAnnotationStructure(InkAnno.config())),true,true);
                    } catch(IOException e1) {
                        // TODO Auto-generated catch block
                        JOptionPane.showMessageDialog(g, "Could not read specified file. \n" + e1.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
                    } catch(InvalidDocumentException e2) {
                        JOptionPane.showMessageDialog(g, "File has an unexpected property:. \n" + e2.getMessage(), "Document Error", JOptionPane.ERROR_MESSAGE);
                        ;
                    }
                }
            }
            GUI.fileChooser.setMultiSelectionEnabled(false);
        }
        
    }
    
    public static class VMirroring implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent event) {
            try{
                Document d = GUI.getInstance().getCurrentDocument();
                d.invertYAxis();
                GUI.getInstance().reloadDocument(d);
            } catch (InkMLComplianceException e) {
                JOptionPane.showMessageDialog(GUI.getInstance(), "Could not invert Y-axis because of: \n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
    }
    
    public static class HMirroring implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            try{
                Document d = GUI.getInstance().getCurrentDocument();
                d.invertXAxis();
                GUI.getInstance().reloadDocument(d);
            } catch (InkMLComplianceException e) {
                JOptionPane.showMessageDialog(GUI.getInstance(), "Could not invert X-axis because of: \n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }
    
    public static class ZoomIn implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent event) {
            GUI.getInstance().getCanvas().zoom(true);
        }
        
    }
    
    public static class ZoomOut implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent event) {
            GUI.getInstance().getCanvas().zoom(false);
        }
        
    }
    
    public static class ChangeColorizer implements ActionListener{

        private Colorizer colorizer;

		public ChangeColorizer(Colorizer c) {
			super();
			this.colorizer = c;
		}

		@Override
        public void actionPerformed(ActionEvent event) {
        	DrawPropertyManager.getInstance().setColorizer(colorizer);
        }
        
    }
    
    
    }
