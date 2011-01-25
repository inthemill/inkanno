/**
 * 
 */
package ch.unibe.im2.inkanno.exporter;

import java.io.File;
import java.io.OutputStream;

import javax.swing.JFileChooser;

import ch.unibe.eindermu.utils.Config;
import ch.unibe.eindermu.utils.FileUtil;
import ch.unibe.eindermu.utils.FileUtil.FileInfo;
import ch.unibe.im2.inkanno.AbstractInkAnnoMain;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.InkAnno;

/**
 * @author emanuel
 *
 */
public abstract class AbstractExporter implements Exporter {
    
    private File            file;
    private OutputStream    stream;
    
    private DocumentManager docMan;


    @Override
    public void setDocument(Document document) throws ExporterException {
        DocumentManager dm = new DocumentManager();
        dm.addDocument(document,false,false);
        setDocumentManager(dm);
    }
    
    public DocumentManager getDocumentManager(){
        return docMan;
    }
    
    /**
     * returns the file that has been set by setFile
     * @return said file
     */
    public File getFile(){
        return file;
    }
    
    /**
     * returns the stream set by setStream
     * @return said stream
     */
    public OutputStream getStream(){
        return stream;
    }
    

    /**
     * String with filename that has been set as output to the command line
     * This may be empty,null,not a path, or a not accessable file
     * @return said string
     */
    public String output(){
        return Config.getMain().get(AbstractInkAnnoMain.OUTPUT);
    }
 
    @Override
    public void setFile(File file) {
        this.file = file;
    }


    @Override
    public void setStream(OutputStream stream) {
        this.stream = stream;
        
    }


    @Override
    public void setDocumentManager(DocumentManager documentsIterator)
            throws ExporterException {
        docMan = documentsIterator;
        
    }
    
    /**
     * Test if the string specified as output file is not null and not empty.
     * @return said test result as boolean
     */
    protected boolean isCmdOutputSet() {
        return Config.getMain().get(AbstractInkAnnoMain.OUTPUT) != null && !Config.getMain().get(AbstractInkAnnoMain.OUTPUT).isEmpty();
    }
    
    /**
     * Returns the name of the file associated to the document specified
     * @param doc The document where the file is extracted from.
     * @return said ID as string
     */
    public String getID(Document doc){
    	return doc.getInk().getId();
    }
    
    /**
     * returns true if on the commandline the exporter has been requested
     * to append the ouput to the output file rather than overwriting it.
     * @return
     */
    public boolean isAppendEnabled(){
        return Config.getMain().getB(InkAnno.CMD_OPT_APPEND);
    }
    
    /**
     * Decision tree: Says what case we have
     * 
     * The following table indicates the behaivour of this class
     * 
     * case nr  | #docs | setFile | setStream | output | append | possible action
     * ---------|-------|---------|-----------|--------|--------|---------
     * 0        | 1     | -       | -         | -      | *      | > <id>
     * 1        | 1     | -       | -         | yes    | no     | > output
     * 2        | 1     | -       | -         | yes    | yes    | >> output
     * 3        | 1     | -       | yes       | *      | *      | >> stream
     * 4        | 1     | yes     | *         | *      | no     | > file
     * 5        | 1     | yes     | *         | *      | yes    | >> file
     * 6        | n     | -       | -         | -      | no     | > <id1>, > <id2>f,...
     * 7        | n     | -       | -         | -      | yes    | error
     * 8        | n     | -       | -         | yes    | no     | > output-<id1>, > output-<id2>,...
     * 9        | n     | -       | -         | yes    | yes    | >> output
     * 10       | n     | -       | yes       | *      | *      | >> stream
     * 11       | n     | yes     | *         | *      | no     | > file-<id1>, > file-<id2>
     * 12       | n     | yes     | *         | *      | yes    | >> file
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
     * 
     * @return
     */
    public int getCase(){
        boolean append = isAppendEnabled();
        if(docMan.size() == 1){
            if(file == null){
                if(stream == null){
                    if(!isCmdOutputSet()){
                        return 0;
                    }else{
                        if(!append){
                            return 1;
                        }else{
                            return 2;
                        }
                    }
                }else{
                    return 3;
                }
            }else{
                if(!append){
                    return 4;
                }else{
                    return 5;
                }
            }
        }else if(docMan.size() > 1){
            if(file == null){
                if(stream == null){
                    if(!isCmdOutputSet()){
                        if(!append){
                            return 6;
                        }else{
                            return 7;
                        }
                    }else{
                        if(!append){
                            return 8;
                        }else{
                            return 9;
                        }
                    }
                }else{
                    return 10;
                }
            }else{
                if(!append){
                    return 11;
                }else{
                    return 12;
                }
            }
        }else{
            return -1;
        }
    }

    /**
     * Returns the file name extended by an id, an extension (eg. .pdf).
     * Both can be null. If id is null its ignored.
     * If extension is null, a default extension will be used. which is given by 
     * getDefaultExtension
     * @param filename
     * @param id
     * @param extension
     * @return
     */
    public String extendFileName(String filename, String id, String extension) {
        FileInfo i = FileUtil.getInfo(filename);
    
        String ext = "";
        
        if(extension != null){
            ext = extension;
        }else if(i.extension != null){
            ext = i.extension;
        }else{
            ext = getDefaultFileNameExtension();
        }
        
        String name = i.name;
        if(id != null && !id.isEmpty()){
            name += "-"+ id;
        }
        return FileUtil.combine(i.dir,name,ext);
    }
    
    /**
     * Returns the default extension for the output file of this exporter (e.g. pdf).
     * @return
     */
    public abstract String getDefaultFileNameExtension();
    
    
    @Override
    public JFileChooser getCustomFileChooser(Document doc) {
        return null;
    }
}
