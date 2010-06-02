/**
 * 
 */
package ch.unibe.im2.inkanno;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



import ch.unibe.eindermu.utils.AbstractObservable;
import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.eindermu.utils.StringList;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;



/**
 * @author emanuel
 *
 */
public class DocumentManager extends AbstractObservable{
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
    
    
    
    private List<Document> documents;
    private List<String> files;
    private List<Boolean> keep;
    
    private InkAnnoAnnotationStructure structure;
    
    private int cursor = -1;
    
    public DocumentManager(List<Document> documentList){
        documents = documentList;
        files = new ArrayList<String>();
        keep = new ArrayList<Boolean>();
        for(Document d: documents){
            files.add(d.getFile().getPath());
            keep.add(true);
        }
    }
    
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
     * 
     */
    public DocumentManager() {
        documents = new ArrayList<Document>();
        files = new ArrayList<String>();
        keep = new ArrayList<Boolean>();
    }

    private Document createCurrentDocument() throws InvalidDocumentException{
        if(documents.get(cursor) == null){
            try{
                documents.set(cursor, new Document(new File(files.get(cursor)),structure));
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
    
    public Document setCurrentDocument(Document doc){
        int current = documents.indexOf(doc);
        if(current == -1){
            throw new IllegalArgumentException("Can not set unknown document do current document in documentManager");
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
     * @param path
     * @throws InvalidDocumentException 
     */
    public void setCurrentDocument(String path) throws InvalidDocumentException {
        if(files.contains(path)){
            setCurrentDocument(files.indexOf(path));
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
            Document d = createCurrentDocument();
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
    
    
    public void addDocument(Document doc,boolean setCurrent){
        documents.add(doc);
        keep.add(true);
        if(doc.getFile() != null){
            files.add(doc.getFile().getPath());
        }else{
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
}

