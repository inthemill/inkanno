package ch.unibe.im2.inkanno.imageExport;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.imageExport.ImageExporter.FileType;

public class ImageExportGUI extends JPanel implements PropertyChangeListener{
	private static final long serialVersionUID = 1L;
	private JComboBox fileTypeComboBox;
	private JComboBox outputTypeComboBox;
	private JFormattedTextField xField;
	private JFormattedTextField yField;
	private ImageExporter ie;
	public ImageExportGUI(final Document doc, ImageExporter imageExporter) {
			this.ie = imageExporter;
			final double factor = doc.getBounds().getWidth() / doc.getBounds().getHeight();
	        setPreferredSize(new Dimension(155, 150));
	        setMaximumSize(new Dimension(155, 700));
	        Box mainBox = Box.createVerticalBox();
	        
	        mainBox.add(new JLabel("File Type:  "));
	        fileTypeComboBox= new JComboBox();
	        for(FileType f: FileType.values()){
	        	fileTypeComboBox.addItem(f);
	        }
	        fileTypeComboBox.setSelectedItem(ie.getFileType());
	        fileTypeComboBox.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					ie.setFileType((FileType)fileTypeComboBox.getSelectedItem());
				}});
	        mainBox.add(fileTypeComboBox);
	        //Box otBox = Box.createHorizontalBox();
	        mainBox.add(new JLabel("Output Type:"));
	        outputTypeComboBox = new JComboBox();
	        for(RegisteredImageExportDrawer o : ie.getDrawers()){
	        	outputTypeComboBox.addItem(o.getId());
	        }
	        outputTypeComboBox.setSelectedItem(ie.getOutputType());
	        outputTypeComboBox.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					ie.setOutputType((String) outputTypeComboBox.getSelectedItem());
				}});
	        mainBox.add(outputTypeComboBox);
	        //mainBox.add(otBox);
	        //Box sizeBox = Box.createHorizontalBox();
	        mainBox.add(new JLabel("set Size:"));
	        xField = new JFormattedTextField(NumberFormat.getIntegerInstance());
	        xField.setValue(doc.getBounds().width);
	        
	        yField = new JFormattedTextField(NumberFormat.getIntegerInstance());
	        yField.setValue(doc.getBounds().height);
	        xField.addFocusListener(new FocusListener(){
				public void focusGained(FocusEvent e) {}
				@Override
				public void focusLost(FocusEvent e) {
					try{
						xField.commitEdit();
						yField.setValue((int)(((Number)xField.getValue()).intValue() / factor));
						yField.commitEdit();
					} catch (ParseException e1) {
						yField.setValue(yField.getValue());
						xField.setValue(xField.getValue());
					}
					ie.setDimension(new Dimension(((Number)xField.getValue()).intValue(),((Number)yField.getValue()).intValue()));
				}
	        });
	        yField.addFocusListener(new FocusListener(){
				public void focusGained(FocusEvent e) {}
				@Override
				public void focusLost(FocusEvent e) {
					try {
						yField.commitEdit();
						xField.setValue((int)(((Number)yField.getValue()).intValue() * factor));
						xField.commitEdit();
					} catch (ParseException e1) {
						yField.setValue(yField.getValue());
						xField.setValue(xField.getValue());
					}
					ie.setDimension(new Dimension(((Number)xField.getValue()).intValue(),((Number)yField.getValue()).intValue()));
				}
	        });
	        mainBox.add(xField);
	        mainBox.add(new JLabel("x"));
	        mainBox.add(yField);
	        //mainBox.add(sizeBox);
	        add(mainBox);
	        this.revalidate();
    }
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}
	
}
