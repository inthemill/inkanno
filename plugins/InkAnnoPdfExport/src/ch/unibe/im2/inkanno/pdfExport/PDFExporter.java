package ch.unibe.im2.inkanno.pdfExport;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFileChooser;

import ch.unibe.eindermu.utils.Config;
import ch.unibe.eindermu.utils.FileUtil;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.exporter.AbstractExporter;
import ch.unibe.im2.inkanno.exporter.Exporter;
import ch.unibe.im2.inkanno.exporter.ExporterException;
import ch.unibe.im2.inkanno.imageProcessing.TraceDrawVisitor;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Exports the document(s) to pdf file(s).
 * The following table indicates the behaivour of this class
 * 
 * case nr  | #docs | setFile | setStream | output | append | action
 * ---------|-------|---------|-----------|--------|--------|---------
 * 0        | 1     | -       | -         | -      | *      | > <id>.pdf
 * 1        | 1     | -       | -         | yes    | no     | > output.pdf
 * 2        | 1     | -       | -         | yes    | yes    | >> output.pdf
 * 3        | 1     | -       | yes       | *      | *      | >> stream
 * 4        | 1     | yes     | *         | *      | no     | > file.pdf
 * 5        | 1     | yes     | *         | *      | yes    | >> file.pdf
 * 6        | n     | -       | -         | -      | no     | > <id1>.pdf, > <id2>.pdf,...
 * 7        | n     | -       | -         | -      | yes    | error
 * 8        | n     | -       | -         | yes    | no     | > output-<id1>.pdf, > output-<id2>.pdf,...
 * 9        | n     | -       | -         | yes    | yes    | >> output.pdf
 * 10       | n     | -       | yes       | *      | *      | >> stream
 * 11       | n     | yes     | *         | *      | no     | > file-<id1>.pdf, > file-<id2>.pdf
 * 12       | n     | yes     | *         | *      | yes    | >> file.pdf
 * 
 * legend:
 * #docs    : number of documents 1: used setDocument , n: used setDocumentIterator()
 * setFile  : File has been specified by setFile()
 * setStream: Stream has been specified by setStream(), * = dont' care
 * output   : output file has been specified by -o or --output on command line, * = dont' care
 * append   : if the output should be appended, specifyed by --append on command line, * = dont' care
 * action   :
 *  > xxx   : write to file or stream xxx
 *  >>      : append to file or stream xxx
 * 
 * @author emanuel
 *
 */
public class PDFExporter extends AbstractExporter {

    @Override
    public String getID() {
        return "export_pdf";
    }


    @Override
    public String getDescription() {
        return "The input file will be exportet to PDF format";
    }
	
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void export() throws ExporterException {
        if(getDocumentManager() == null){
            throw new ExporterException(Exporter.ERR_NO_DOCUMENT);
        }
		try {
			internal_export();
		} catch (FileNotFoundException e) {
			throw new ExporterException(e.getMessage());
		} catch (DocumentException e) {
			throw new ExporterException(e.getMessage());
		} catch (IOException e) {
            e.printStackTrace();
            throw new ExporterException(e.getMessage());
        } catch (InvalidDocumentException e) {
            e.printStackTrace();
            throw new ExporterException(e.getMessage());
        }
	}

	private void internal_export() throws DocumentException, IOException, ExporterException, InvalidDocumentException{
	    com.lowagie.text.Document pdfDoc = null;
	    
	    PdfWriter writer = null;
	    PdfContentByte cb = null;
	    
	    OutputStream stream = null;
        
	    DocumentManager docMan = getDocumentManager();
        
        int cas = getCase();
        
        boolean first = true;
	    while(docMan.hasNext()){
	        Document document = docMan.nextDocument();
	        File file = determineFile(document);
	        
	        if(! (cas == 2 || cas == 5 || cas == 9 || cas == 12)  || first){
	            if(cas == 3 || cas == 10 ){ // if stream is specified
                    stream = getStream();
                }else{
                    stream = new FileOutputStream(file);
                }
                writer = PdfWriter.getInstance(pdfDoc,stream);
                cb = writer.getDirectContent();
	            pdfDoc = new com.lowagie.text.Document();
	            if((cas == 2 || cas == 5 || cas == 9 || cas == 12) && file.exists()){ // if we append to existing pdf
	                File f = File.createTempFile("export-backup", "pdf");
	                FileUtil.copyFile(getFile(),f);
	                PdfReader pdfReader = new PdfReader(f.getPath());
	                int total_pages = pdfReader.getNumberOfPages();
	                int currentPageNr = 0;
	                pdfDoc.newPage();
	                while(currentPageNr < total_pages){
	                    PdfImportedPage page = writer.getImportedPage(pdfReader, currentPageNr);
	                    cb.addTemplate(page, 0, 0);
	                    pdfDoc.newPage();
	                }
	                pdfReader.close();
	            }
	        }
	    
	        //set up graphics;
	        Graphics2D g2 = cb.createGraphics(PageSize.A4.getWidth(),PageSize.A4.getHeight());
	        g2.translate(50,50);
	        g2.setFont(new java.awt.Font("arial", 0, 18));
	        
	        // initialize visitor
	        TraceDrawVisitor drawer = new TraceDrawVisitor();
	        drawer.setDimension(new Dimension((int) PageSize.A4.getWidth()-100,(int) PageSize.A4.getHeight()-100));
	        drawer.addTraceFilter(document.getTraceFilter());
	        drawer.setStrokeWidth(15 * document.getMostCommonTraceHeight()/100.0);
	        drawer.setGraphics(g2);
	        drawer.go(document.getCurrentViewRoot());
	        g2.dispose();
	        if(docMan.hasNext() && (cas == 2 || cas == 5 || cas == 9 || cas == 12)){
	            pdfDoc.newPage();
	        }else{
	            pdfDoc.close();        
	        }
	        first = false;
	    }
        
	}
	
    
    public String getDefaultFileNameExtension(){
        return "pdf";
    }

    
    
	private File determineFile(Document doc) throws ExporterException {
	    switch(getCase()){
	        case 0 : // <id>
	        case 6 :
	            return new File(extendFileName(doc.getFile().getPath(),null,"pdf"));
	        case 1 : // output
            case 2 :
            case 9 :
                return new File(output());
	        case 3 : // stream
	        case 10:
	            return null;
	        case 4 : // file
	        case 5 :
	        case 12:
	            return getFile();
	        case 7 : // error
	            throw new ExporterException("Can't append output of multiple documents to an unspecified file");
	        case 8 : // output-<id1>, output-<id2>,...
	            return new File(extendFileName(output(), getID(doc),null));
	        case 11: // file-<id1>, file-<id2>,...
	            return new File(extendFileName(getFile().getPath(), getID(doc), null));
            default :
                return null;
	    }
    }


    @Override
    public void addCommandLineOptions(Config c) {
        //no cmd args
    }


    @Override
    public JFileChooser getCustomFileChooser(Document doc) {
        // TODO Auto-generated method stub
        return null;
    }

}
