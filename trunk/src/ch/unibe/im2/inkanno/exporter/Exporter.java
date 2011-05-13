package ch.unibe.im2.inkanno.exporter;

import java.io.File;
import java.io.OutputStream;

import javax.swing.JFileChooser;

import ch.unibe.eindermu.utils.Config;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.Strings;
import ch.unibe.inkml.util.TraceViewFilter;

public interface Exporter {
    public final static String ERR_NO_DOCUMENT = Strings.getString("Exporter.err_no_document"); //$NON-NLS-1$;
    public static final String ERR_NO_FILE = Strings.getString("Exporter.err_no_file"); //$NON-NLS-1$;

    /**
     * Returns the id of the exporter. The id is used to refer to it in the command line.
     * @return
     */
    public String getID();
    
    /**
     * Returns a description string displayed in the command line help, or as tooltip later.
     * @return
     */
	public String getDescription();
	
	/**
	 * Add command line options to the Config options used to configurate the export.
	 * @param c
	 */
	public void addCommandLineOptions(Config c);
	
	
	public void setOptionsByCommandLineOptions(Config c);
	/**
	 * Sets to document which should be exported 
	 * @param document
	 * @throws ExporterException 
	 */
    public void setDocument(Document document) throws ExporterException;
    
    
    public void setFilter(TraceViewFilter filter);
    /**
     * Set iterator over a list of documents, which will by processed in the given order.
     * If possible the exported information will be stored in one output file.
     * Otherwise multiple output file will be generated, their filename are derived
     * from the name of the specified output file by adding "-<documentID>" after the filename (and befor a possible extension). 
     * @throws ExporterException 
     */
    public void setDocumentManager(DocumentManager documentManager) throws ExporterException;
    /**
     * Set File where the document should be exported to.   
     */
    public void setFile(File file);
    
    
    /**
     * Sets OutputStream where the export will be written to.
     * @param stream
     */
    public void setStream(OutputStream stream);
    /**
     * Determine output file by eather consider commandline arguments, the documents origine, or if GUI exists
     * ask the user directly.
     * @param doc
     * @return
     */
	//public File determineFile(Document doc) throws ExporterException;
	
	/**
	 * Export the document. The file to export to is internally determined by a call to @{link} determineFile
	 * @param doc
	 * @throws ExporterException
	 */
    public void export() throws ExporterException;

    /**
     * Returns a file chooser adapted to the needs of this exporter. So the
     * file chooser can be associated with a option panel, specific filter can be set
     * an so on. Note that the default directory should be configurated by the caller 
     * of this method, and will not be set by this method.
     * 
     * Note also, this method may return null, so caller, do your tests and 
     * have some default fileChooser in your hand.
     * 
     * @param doc The document where the export will be applied to, to gather some
     * imported information there.
     * @return said fileChooser
     */
    public JFileChooser getCustomFileChooser(Document doc);
    
    
     
}
