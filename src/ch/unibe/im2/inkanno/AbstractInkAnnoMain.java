package ch.unibe.im2.inkanno;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import ch.unibe.eindermu.AbstractMain;
import ch.unibe.eindermu.Messenger;
import ch.unibe.eindermu.utils.StringList;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;


public abstract class AbstractInkAnnoMain extends AbstractMain {

    public static final String INPUT = "input";
    public static final String OUTPUT = "output";
    
    protected Document createDocument() {
        Document document = null;
        if(getConfig().get(INPUT)!= null && !getConfig().get(INPUT).isEmpty()){
            String errormessage = "Input file "+new File(getConfig().get(INPUT))+" can not be loaden";
            File file = new File(getConfig().get(INPUT));
            boolean ok = true;
            if(file.exists()){
                try {
                    document = new Document(file,new InkAnnoAnnotationStructure(getConfig()));
                } catch (IOException e) {
                    ok = false;
                    e.printStackTrace();
                    errormessage = errormessage + ": " + e.getMessage();
                } catch (InvalidDocumentException e) {
                    ok = false;
                    e.printStackTrace();
                    errormessage = errormessage + ": " + e.getMessage();
                }
            }else{
                ok = false;
            }
            if(!ok){
                Messenger.error(errormessage);
                System.exit(1);
            }
        }
        return document;
    }
    /*
    protected int eachDocument(Block<Document,Boolean> block){
        if(getConfig().get(INPUT)== null || getConfig().get(INPUT).isEmpty()){
            return 0;
        }
        StringList documentFileList = new StringList();
        if(getConfig().getB(InkAnno.CMD_OPT_INPUT_FILE)){
            File file = new File(getConfig().get(INPUT));
            try {
                Scanner sc = new Scanner(file);
                while(sc.hasNextLine()){
                    documentFileList.add(sc.nextLine());
                }
            } catch (FileNotFoundException e) {
                showError(String.format("File %s containing list of input file does not exits",getConfig().get(INPUT)));
                e.printStackTrace();
                System.exit(1);
            }
        }else{
            documentFileList = getConfig().getMultiple(INPUT);
        }
        String errormessage = "";
        boolean cont = true;
        int counter = 0;
        for(String fileName : documentFileList){
            File file = new File(fileName);
            boolean ok = true;
            if(file.exists()){
                try {
                    cont = block.yield(new Document(file,new InkAnnoAnnotationStructure(getConfig())));
                    counter ++;
                } catch (IOException e) {
                    ok = false;
                    e.printStackTrace();
                    errormessage = errormessage + ": " + e.getMessage();
                } catch (InvalidDocumentException e) {
                    ok = false;
                    e.printStackTrace();
                    errormessage = errormessage + ": " + e.getMessage();
                }
            }else{
                ok = false;
            }
            if(!ok){
                showError(errormessage);
                System.exit(1);
            }
            if(!cont){
                return counter;
            }
        }
        return counter;
        
    }
    */
    /**
     * @return a document Manager filled with the documents given by the command line
     * @throws IOException 
     */
    public DocumentManager getDocumentManager(boolean loadOnlyCurrent) throws IOException {
        final StringList documentFileList = new StringList();
        if(!(getConfig().get(INPUT)== null || getConfig().get(INPUT).isEmpty())){
            if(getConfig().getB(InkAnno.CMD_OPT_INPUT_FILE)){
                File file = new File(getConfig().get(INPUT));
                try {
                    Scanner sc = new Scanner(file);
                    while(sc.hasNextLine()){
                        documentFileList.add(sc.nextLine());
                    }
                    sc.close();
                } catch (FileNotFoundException e) {
                    Messenger.error(String.format("File %s containing list of input file does not exits",getConfig().get(INPUT)));
                    e.printStackTrace();
                    System.exit(1);
                }
            }else{
                documentFileList.addAll(getConfig().getMultiple(INPUT));
            }
        }
        return new DocumentManager(documentFileList,new InkAnnoAnnotationStructure(getConfig()),loadOnlyCurrent);       
    }
}
