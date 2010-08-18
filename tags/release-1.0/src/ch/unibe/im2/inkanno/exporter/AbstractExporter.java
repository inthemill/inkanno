/**
 * 
 */
package ch.unibe.im2.inkanno.exporter;

import java.io.File;
import java.io.OutputStream;

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
        dm.addDocument(document,false);
        setDocumentManager(dm);
    }
    
    public DocumentManager getDocumentManager(){
        return docMan;
    }
    
    public File getFile(){
        return file;
    }
    
    public OutputStream getStream(){
        return stream;
    }
    

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
     * @return
     */
    protected boolean isCmdOutputSet() {
        return Config.getMain().get(AbstractInkAnnoMain.OUTPUT) != null && !Config.getMain().get(AbstractInkAnnoMain.OUTPUT).equals("");
    }
    
    
    public String getID(Document doc){
        return FileUtil.getInfo(doc.getFile()).name;
    }
    
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

    public String extendFileName(String filename, String id, String extension) {
        FileInfo i = FileUtil.getInfo(filename);
    
        String ext = "";
        
        if(extension != null){
            ext = extension;
        }else if(i.extension != null){
            ext = i.extension;
        }else{
            ext = getDefaultExtension();
        }
        
        String name = i.name;
        if(id != null && !id.equals("")){
            name += "-"+ id;
        }
        return FileUtil.combine(i.dir,name,ext);
    }
    
    public abstract String getDefaultExtension();
    
}
