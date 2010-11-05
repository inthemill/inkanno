package ch.unibe.im2.inkanno.exporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JFileChooser;

import ch.unibe.eindermu.utils.Config;
import ch.unibe.eindermu.utils.StringList;
import ch.unibe.eindermu.utils.StringMap;
import ch.unibe.im2.inkanno.AbstractInkAnnoMain;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.InkAnno;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.TraceVisitor;

public class StatisticsExporter extends TraceVisitor implements Exporter {

    private DocumentManager documentIterator;
    
    private File file;

    private OutputStream stream;
    
    private Document currentDocument;
    
    private Properties stats = new Properties();
    
    private StringMap<Integer> ints;



    /**
     * accumulates author ids to count different authors
     */
    private StringList authors;
    
    private StringMap<Integer> educationalsDegrees;
    
    private StringMap<Integer> professions;

    
    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @param stream the stream to set
     */
    public void setStream(OutputStream stream) {
        this.stream = stream;
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void addCommandLineOptions(Config c) {
        //no commandline args needed
    }

    /**
     * 
     * {@inheritDoc}
     * @throws ExporterException 
     */
    public void determineFile(Document doc) throws ExporterException {
        if(stream != null){
            return;
        }
        if(Config.getMain().get(AbstractInkAnnoMain.OUTPUT) != null && !Config.getMain().get(AbstractInkAnnoMain.OUTPUT).isEmpty()){
            file = new File(Config.getMain().get(AbstractInkAnnoMain.OUTPUT));
            
            if(Config.getMain().getB(InkAnno.CMD_OPT_APPEND) && file != null){
                try {
                    stats.load(new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    System.err.println(String.format("File '%s' not found, will try to create it.",file.getPath()));
                } catch (IOException e) {
                    throw new ExporterException(String.format("File '%s' could not be accessed: '%s'",file.getPath(),e.getMessage()));
                }
            }
            
            try {
                stream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new ExporterException(String.format("Can't write to file '%s': '%s'",file.getPath(),e.getMessage()));
            }
        }
        if(file == null && stream == null){
            stream = System.out;
        }
        if(stream == null){
            throw new ExporterException(Exporter.ERR_NO_FILE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void export() throws ExporterException {
        //preconditions
        if(documentIterator == null){
            throw new ExporterException(Exporter.ERR_NO_DOCUMENT);
        }
        //initial setup
        ints = new StringMap<Integer>();
        authors = new StringList();
        educationalsDegrees = new StringMap<Integer>();
        professions = new StringMap<Integer>();
        
        int nrDocuments = 0;
        
        //iterativ extraction process
        while(documentIterator.hasNext()){
            try {
                currentDocument = documentIterator.nextDocument();
            } catch (InvalidDocumentException e) {
                throw new ExporterException(e.getMessage());
            }
            nrDocuments += 1;
            determineFile(currentDocument());
            go();
        }
        
        //postcondition
        if(nrDocuments < 1){
            throw new ExporterException(Exporter.ERR_NO_DOCUMENT);
        }
        
        //prepair for output
        for(String key : ints.keyList()){
            if(!stats.containsKey(key)){
                stats.put(key, ints.get(key).toString());
            }else{
                stats.put(key, Integer.toString((Integer.parseInt((String) stats.get(key))+ints.get(key))));
            }
        }
        String edulist = "";
        for(String edu : educationalsDegrees.keyList()){
            edulist += edu + "(" +educationalsDegrees.get(edu)+ ")";
        }
        stats.put("authorEducation", edulist);
        
        String proflist = "";
        for(String prof : professions.keyList()){
            proflist += prof + "(" +professions.get(prof)+ ")";
        }
        stats.put("authorProfession", proflist);

        //print output
        try {
            stats.store(stream, "");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new ExporterException(String.format("Error while writing to file '%s': '%s'",file.getPath(),e.getMessage()));
        }
    }
    
    
    public void go(){
        Document d = currentDocument();
        
        //count documents 
        inc("document-count");
        
        //count authors
        if(d.getInk().containsAnnotation("authorId")){
            String id = d.getInk().getAnnotation("authorId");
            if(!authors.contains(id)){
                inc("author-count");
                authors.add(id);
                
                //count female authors
                if(d.getInk().containsAnnotation("authorSex")){
                    if(d.getInk().getAnnotation("authorSex").equals("female")){
                        inc("female-author-count");
                    }else{
                        inc("male-author-count");
                    }
                }else{
                    inc("missing-authorSex-count");
                }
                
                
              //collect education information
                if(d.getInk().containsAnnotation("authorEducation")){
                    String edu = d.getInk().getAnnotation("authorEducation");
                    if(!educationalsDegrees.containsKey(edu)){
                        educationalsDegrees.put(edu, 0);
                    }
                    educationalsDegrees.put(edu, educationalsDegrees.get(edu)+1);
                }
                
                //collect professions
                if(d.getInk().containsAnnotation("authorProfession")){
                    String prof = d.getInk().getAnnotation("authorProfession");
                    if(!professions.containsKey(prof)){
                        professions.put(prof, 0);
                    }
                    professions.put(prof, professions.get(prof)+1);
                }
                
                
            }
        }else{
            inc("missing-author-count");
        }
        
        
        
        //count traces
        go(currentDocument().getCurrentViewRoot());
    }
    

    /**
     * @return
     */
    private Document currentDocument() {
        return currentDocument;
    }

    @Override
    public String getDescription() {
        return "returns statisics of one or several documents";
    }

    @Override
    public String getID() {
        return "stats";
    }


    @Override
    public void setDocument(Document document) {
        DocumentManager i = new DocumentManager();
        i.addDocument(document,false,false);
        setDocumentManager(i);
    }


    @Override
    public void setDocumentManager(DocumentManager documentsIterator) {
        documentIterator = documentsIterator;
    }
    
     

    @Override
    protected void visitHook(InkTraceViewContainer container) {
        //word Counter
        if(container.testAnnotation("type", "Word") || container.testAnnotation("type", "Symbol")){
            inc("word-count");
        }else if(container.testAnnotation("type", "Textblock")){
            inc("textblock-count");
        }else if(container.testAnnotation("type", "Textline") && container.getParent().testAnnotation("type","Textblock")){
            inc("line-count");
        }else if(container.testAnnotation("type", "Diagram")){
            inc("diagram-count");
        }else if(container.testAnnotation("type", "Table")){
            inc("table-count");
        }else if(container.testAnnotation("type", "List")){
            inc("list-count");
        }else if(container.testAnnotation("type", "Drawing") && !container.getParent().testAnnotation("type","Diagram")){
            inc("drawing-count");
        }else if(container.testAnnotation("type", "Drawing") && container.getParent().testAnnotation("type","Diagram")){
            inc("diagram-drawing-count");
        }else if(container.testAnnotation("type", "Textline") && container.getParent().testAnnotation("type","Table")){
            inc("table-cell-count");
        }else if(container.testAnnotation("type", "Textline") && container.getParent().testAnnotation("type","List")){
            inc("list-items-count");
        }else if(container.testAnnotation("type", "Textline") && container.getParent().testAnnotation("type","Diagram")){
            inc("diagram-label-count");
        }else if(container.testAnnotation("type", "Formula")){
            inc("formula-count");
        }else if(!container.isRoot() && container.getParent().testAnnotation("type","Marking")){
            inc("marking-element-count");
        }else if(container.testAnnotation("type","Marking")){
            inc("markings-count");
        }else  if((container.testAnnotation("type","Structure") || container.testAnnotation("type","Arrow")) && container.getParent().testAnnotation("type","Table")){
            inc("table-structure-count");
        }else  if((container.testAnnotation("type","Structure") || container.testAnnotation("type","Arrow")) && container.getParent().testAnnotation("type","Diagram")){
            inc("diagram-structure-count");
        }else if((container.testAnnotation("type","Structure") || container.testAnnotation("type","Arrow")) && container.getParent().isRoot()){
            inc("structure-count");
        }else if(container.testAnnotation("type","Garbage")){
            inc("garbage-count");
        }else if(container.testAnnotation("type","Correction")){
            inc("correction-count");
        }else if(container.testAnnotation("type","Document")){
            //ignore
        }else {
            if(container.containsAnnotation("type")){
                System.err.println("not Counted: "+container.getAnnotation("type"));
            }
        }
        super.visitHook(container);
    }
    
    public void inc(String key){
        if(!ints.containsKey(key)){
            ints.put(key,0);
        }
        ints.put(key,ints.get(key)+1);
    }

    @Override
    protected void visitHook(InkTraceViewLeaf leaf) {
        if(!leaf.testAnnotationTree("type", "Garbage")){
            inc("stroke-count");
        }
        if((leaf.testAnnotationTree("type","Textline") && !leaf.testAnnotationTree("type","Correction")) || leaf.testAnnotationTree("type","Formula")){
            inc("text-stroke-count");
        }
    }

    @Override
    public JFileChooser getCustomFileChooser(Document doc) {
        return null;
    }

    
    
}
