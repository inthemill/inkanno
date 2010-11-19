/**
 * 
 */
package ch.unibe.im2.inkanno;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.unibe.eindermu.Messenger;
import ch.unibe.eindermu.utils.AbstractObservable;
import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.NotImplementedException;
import ch.unibe.eindermu.utils.StringList;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
import ch.unibe.inkml.AnnotationStructure;



/**
 * @author emanuel
 *
 */
public class DocumentManager extends AbstractObservable implements Iterable<Document>{
    
    public static final Aspect ON_DOCUMENT_CONSTRUCTED = new Aspect();
    public static final Aspect ON_NEW_DOCUMENT = new Aspect();
    /**
     * This event will be created if a new document has been set as the current document
     */
    public static final Aspect ON_DOCUMENT_SWITCH = new Aspect();

    /**
     * this event will be created befor a document will be removed.
     */
    public static final Aspect ON_DOCUMENT_PRE_SWITCH = new Aspect();

    public static final Aspect ON_DOCUMENT_PRESENT = new Aspect();

    public static final Aspect ON_DOCUMENT_ABSENT = new Aspect();
    public static final Aspect ON_DOCUMENT_REMOVED = new Aspect();
    /**
     * This event will be created before a document will be unloaded so it can be safed befor that.
     */
    public static final Aspect ON_DOCUMENT_UNLOADING = new Aspect();
    
    
    /**
     * List of the documents available throu DocumentManager.
     * DocumentManager loads documents lazy so some of the entries
     * might be NULL but they will be loaded when requested
     */
    private List<Document> documents;
    
    /**
     * List of filenames to the documents which should be loaden.
     */
    private List<String> files;
    
    /**
     * Indicates for every document if it will be removed from memory when
     * not current anymore.
     */
    private List<Boolean> keep;
    
    /**
     * Annotation structure on which the documents are based on.
     */
    private AnnotationStructure structure;
    
    /**
     * indicates the current document. -1 indicates that no document is current yet.
     */
    private int cursor = -1;
    
    /**
     * Instanciates the documentManager with a list of documents.
     * @param documentList
     */
    public DocumentManager(List<Document> documentList){
        boolean keepInMemory = true;
        documents = documentList;
        files = new ArrayList<String>();
        keep = new ArrayList<Boolean>();
        for(Document d: documents){
            structure = d.getAnnotationStructure();
            files.add(d.getFile().getPath());
            keep.add(keepInMemory);
        }
    }
    
    /**
     * Instaciates the DocumentManager with a list of filenames to documents.
     * A Annotation structure as to be specified the documents will be opened with.
     * The last parameter is a boolean indicating if only the current document should be load in memory
     * or all document should be kept in memory
     * @param fileList List of filenames
     * @param structure Annotation structure 
     * @param loadOnlyCurrent load only current document of keep them in memory when loaded.
     */
    public DocumentManager(List<String> fileList,InkAnnoAnnotationStructure structure, boolean loadOnlyCurrent){
        this.structure = structure;
        files = fileList;
        documents = new ArrayList<Document>();
        keep = new ArrayList<Boolean>();
        for(int i =0;i<files.size();i++){
            keep.add(!loadOnlyCurrent);
            documents.add(null);
        }
    }
    
    
    /**
     * Instanciate empty documentManager.
     */
    public DocumentManager() {
        documents = new ArrayList<Document>();
        files = new ArrayList<String>();
        keep = new ArrayList<Boolean>();
    }

    
    private Document loadCurrentDocument() throws InvalidDocumentException{
        if(documents.get(cursor) == null){
            try{
                documents.set(cursor, new Document(new File(files.get(cursor)),(InkAnnoAnnotationStructure) structure));
                notifyObserver(ON_DOCUMENT_CONSTRUCTED, documents.get(cursor));
                notifyObserver(ON_NEW_DOCUMENT, documents.get(cursor));
            } catch (IOException e) {
                throw new InvalidDocumentException(String.format("Can't load document '%s': %s",files.get(cursor),e.getMessage()));
            }
        }
        return documents.get(cursor);
    }
    
    /**
     * Return the next document if there is one, else null.
     * If the document, which was the current one befor calling this method
     * is not one to keep open, then it will be closed, which means it will no longer be stored
     * in the manager without beeing saved before. The next time it is requested, it will be created again.
     * @return
     * @throws IOException
     * @throws InvalidDocumentException
     */
    public Document nextDocument() throws InvalidDocumentException{
        if(!hasNext()){
            return null;
        }
        return setCurrentDocument(cursor+1);
    }
    /**
     * Return the previous document if there is one, else null.
     * If the document, which was the current one befor calling this method
     * is not one to keep open, then it will be closed, which means it will no longer be stored
     * in the manager. DocumentManager tokes no responsibility to save a document before 
     * it is set to null. The next time the removed is requested, it will be created again.
     * @return
     * @throws IOException
     * @throws InvalidDocumentException
     */

    public Document prevDocument() throws InvalidDocumentException{
        if(!hasPrev()){
            return null;
        }
        return setCurrentDocument(cursor -1);
    }
    
    /**
     * Sets specified document as current document in the documentManager. 
     * @param doc
     * @return
     */
    public Document setCurrentDocument(Document doc){
        int current = documents.indexOf(doc);
        if(current == -1){
            for(String file : files){
                if(doc.getFile().getPath().endsWith(file)){
                    current = files.indexOf(file);
                    if(documents.get(current)== null){
                        documents.set(current, doc);
                    }
                    return getCurrentDocument();
                }
            }
            addDocument(doc, true, false);
            return getCurrentDocument();
        }
        
        if(current == cursor){
            return getCurrentDocument();
        }
        try {
			return setCurrentDocument(current);
		} catch (InvalidDocumentException e) {
			// will now occure
		}
		return null;
    }
    
    /**
     * Returns the document which is currently active
     * @return
     */
    public Document getCurrentDocument() {
        return documents.get(cursor);
    }

    /**
     * Returns the filename of the document which is currently active.
     * @return
     */
    public String getCurrentFilename() {
        return files.get(cursor);
    }

    
    /**
     * @param path
     * @throws InvalidDocumentException 
     */
    public Document setCurrentDocument(String path) throws InvalidDocumentException {
        if(files.contains(path)){
            return setCurrentDocument(files.indexOf(path));
        }else{
            throw new InvalidDocumentException(String.format("Document with path '%s' is not managed any more.",path));
        }
    }
    
    public Document setCurrentDocument(int index) throws InvalidDocumentException{
        //Check preconditions
        if(index > size()-1 || index < -1){
            throw new IndexOutOfBoundsException();
        }
        //special handle for -1 (no document selected)
        if(index == -1){
            rewind();
            return null;
        }
        //check if there has to be done something
        if( index == cursor){
            return null;
        }
        int former = cursor;
        //notify befor change
        notifyObserver(ON_DOCUMENT_PRE_SWITCH, (hasCurrentDocument())?getCurrentDocument():null);
        //set cursor
        cursor = index;
        try{
            Document d = loadCurrentDocument();
            handleDocumentLostFocus(former);
            notifyObserver(ON_DOCUMENT_SWITCH, d);
            notifyObserver(ON_DOCUMENT_PRESENT,d);
            return d;
        }catch(InvalidDocumentException e){
            //restore old document
            cursor = former;
            throw e;
        }
    }

    
    private void handleDocumentLostFocus(int index){
        if(index > -1 && index < size()){
            if(!keep.get(index)){
                if(documents.get(index).getFile() != null){
                    files.set(index, documents.get(index).getFile().getPath());
                }
                notifyObserver(ON_DOCUMENT_UNLOADING,documents.get(index));
                documents.set(index, null);
            }
        }
        
    }
    
    /**
     * True if there is a previous document
     * @return a boolean
     */
    public boolean hasPrev() {
        return cursor > 0;
    }
    
    /**
     * True if there is a next document
     * @return a boolean
     */
    public boolean hasNext(){
        return cursor < size()-1;
    }
    
    
    public void addDocument(Document doc,boolean setCurrent,boolean keepInMemory){
        documents.add(doc);
        keep.add(keepInMemory);
        if(doc.getFile() != null){
            files.add(doc.getFile().getPath());
        }else{
            Messenger.warn("Document without filename attached is added to DocumentManager, some application my depend on the filename");
            files.add("");
        }
        notifyObserver(ON_NEW_DOCUMENT, doc);
        if(setCurrent){
            setCurrentDocument(doc);
        }
    }
    
    public void removeCurrentDocument() throws InvalidDocumentException{
        removeDocument(cursor);
    }
    
    public void removeDocument(int index) throws InvalidDocumentException{
        try {
            if(index == cursor){
                if(hasPrev()){
                    prevDocument();
                }else if(hasNext()){
                    nextDocument();
                }else{
                    rewind();
                }
            }
        }catch(InvalidDocumentException e){
            rewind();
            throw e;
        }finally{
            Document d = documents.get(index);
            String name = files.get(index);
            documents.remove(index);
            files.remove(index);
            keep.remove(index);
            if(cursor > index){
                cursor --;
            }
            notifyObserver(ON_DOCUMENT_SWITCH, null);
            if(d == null){
                notifyObserver(ON_DOCUMENT_REMOVED, name);
            }else{
                notifyObserver(ON_DOCUMENT_REMOVED, d);
            }
        }
    }

    /**
     * 
     */
    public void rewind() {
        int current = cursor;
        cursor = -1;
        notifyObserver(ON_DOCUMENT_ABSENT,this);
        handleDocumentLostFocus(current);
    }

    /**
     * @return
     */
    public boolean hasCurrentDocument() {
        return cursor > -1 && cursor < size();
    }

    /**
     * @return
     */
    public List<String> getFileNames() {
        return new StringList(files);
    }

    /**
     * @return
     */
    public int size() {
        return documents.size();
    }

    /**
     * Returns all documents which are loaded into memory by the document manager.
     * @return
     */
    public List<Document> getLoadedDocuments() {
        List<Document> l = new ArrayList<Document>();
        for(Document d : documents){
            if(d != null){
                l.add(d);
            }
        }
        return l;
    }

    /**
     * This methods allows to itterate over all documents of DocumentManager.
     * Be aware that for now it is changing the internal state of DocumentManager. So 
     * be sure not to use this method if the document manager is in use at the same time on 
     * an other place.
     * 
     * Some generic information on this method: 
     * {@inheritDoc}
     */
    @Override
    public Iterator<Document> iterator() {
        return new Iterator<Document>() {

            @Override
            public boolean hasNext() {
                return DocumentManager.this.hasNext();
            }

            @Override
            public Document next() {
                try {
                    return DocumentManager.this.nextDocument();
                } catch (InvalidDocumentException e) {
                    return null;
                }
            }

            @Override
            public void remove() {
                throw new NotImplementedException();
            }
        };
    }


    /**
     * @return
     */
    public AnnotationStructure getAnnotationStructure() {
        return this.structure;
    }
}



