package ch.unibe.im2.inkanno;

import java.io.File;
import java.io.OutputStream;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import ch.unibe.eindermu.utils.Config;
import ch.unibe.eindermu.utils.XmlHandler;
import ch.unibe.im2.inkanno.exporter.Exporter;
import ch.unibe.im2.inkanno.exporter.ExporterException;
import ch.unibe.inkml.InkMLComplianceException;

public class InkMLExporter extends XmlHandler implements Exporter {
	private Document doc;
	private File file;
	private OutputStream stream;
	
	public void setDocument(Document document) {
		this.doc = document;
	}
	public void setFile(File file){
	    this.file = file;
	}
	
    @Override
    public void setStream(OutputStream stream) {
        this.stream = stream;
    }
	
	
	private void buildXMLDocument() throws ParserConfigurationException, InkMLComplianceException{
		this.createNewXMLDocument();
		this.doc.getInk().exportToInkML(this.getDocument());
	}


    @Override
    public void addCommandLineOptions(Config c) {
    }

    @Override
    public String getDescription() {
        return Strings.getString("InkAnno.cmd_opt_action_export_inkml_desc"); //$NON-NLS-1$
    }

    @Override
    public String getID() {
        return Strings.getString("InkAnno.cmd_opt_action_export_inkml"); //$NON-NLS-1$
    }

    @Override
    public void export() throws ExporterException{
        if(doc == null){
            throw new ExporterException(Exporter.ERR_NO_DOCUMENT);
        }
        
        file = determineFile(doc);
        
        if(file == null && stream == null){
            throw new ExporterException(Exporter.ERR_NO_FILE);
        }
        
        try {
            this.buildXMLDocument();
        } catch (ParserConfigurationException e) {
            throw new ExporterException(e.getMessage());
        } catch (InkMLComplianceException e) {
            throw new ExporterException(e.getMessage());
        }
        try {
            if(file != null){
                saveToFile(file);
            }else{
                saveToStream(stream);
            }
        } catch (TransformerException e) {
            throw new ExporterException(e.getMessage());
        }
    }
    /**
     * @param doc2
     * @return
     */
    private File determineFile(Document doc2) {
        if(file != null){
            return file;
        }else{
            return null;
        }
    }
    @Override
    public void setDocumentManager(DocumentManager documentsIterator) throws ExporterException {
        throw new ExporterException("This exporter does not support multiple documents");        
    }
    @Override
    public JFileChooser getCustomFileChooser(Document doc) {
        return null;
    }

}
