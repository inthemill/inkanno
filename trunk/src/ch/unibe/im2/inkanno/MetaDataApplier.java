package ch.unibe.im2.inkanno;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ch.unibe.eindermu.utils.Config;
import ch.unibe.eindermu.utils.StringList;
import ch.unibe.eindermu.utils.StringMap;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
import ch.unibe.inkml.AnnotationStructure;
import ch.unibe.inkml.AnnotationStructure.Annotation;
import ch.unibe.inkml.AnnotationStructure.Annotation.ValueType;

public class MetaDataApplier extends AbstractInkAnnoMain {

    public static void main(String[] args) {
        MetaDataApplier i = new MetaDataApplier();
        i.go(args);
    }

    
    @Override
    protected void buildConfig() {
        Config c = getConfig();
        c.addStringOption('d', "database-directory", "","Directory where inkml file are stored");
        c.addBooleanOption("show-missing");
        c.addBooleanOption("create-sets");
    }

    @Override
    protected void start() throws Exception {
        if(getConfig().getB("show-missing")){
            showMissing();
        }else if(getConfig().getB("create-sets")){
            createSets();
        }else{
            loadMetaData();
        }
    }
    
    private void createSets() throws IOException, InvalidDocumentException {
        File dir = new File(getConfig().get("database-directory"));
        InkAnnoAnnotationStructure a = new InkAnnoAnnotationStructure(getConfig());
        Map<Integer,Integer> authorMap = new HashMap<Integer,Integer>();
        Map<Integer,Integer> authorMap2 = new HashMap<Integer,Integer>();
        Map<String,Integer> documentAuthorMap = new StringMap<Integer>();
        
        List<List<Integer>> sets = new ArrayList<List<Integer>>();
        sets.add(new ArrayList<Integer>());
        sets.add(new ArrayList<Integer>());
        sets.add(new ArrayList<Integer>());
        sets.add(new ArrayList<Integer>());
        sets.add(new ArrayList<Integer>());
        List<Integer> maxs = new ArrayList<Integer>();
        maxs.add(20);
        maxs.add(20);
        maxs.add(20);
        maxs.add(20);
        maxs.add(20);
        
        for(File f : dir.listFiles()){
            if(f.getName().toLowerCase().endsWith(".inkml")){
                Document d = new Document(f,a);
                if(d.getInk().containsAnnotation("authorId")){
                    int author = Integer.parseInt(d.getInk().getAnnotation("authorId"));
                    documentAuthorMap.put(f.getName(), author);
                    if(authorMap.containsKey(author)){
                        authorMap.put(author,authorMap.get(author)+1);
                        authorMap2.put(author,authorMap2.get(author)+1);
                    }else{
                        authorMap.put(author,1);
                        authorMap2.put(author,1);
                    }
                }
            }
        }
        
        while(authorMap.size() != 0){
            int max = 0;
            int seti = -1;
            for(int i = 0; i<sets.size();i++){
                if(maxs.get(i)-setSize(sets.get(i),authorMap2) > max){
                    seti = i;
                    max = 200-sets.get(i).size();
                }
            }
            max = 0;
            int auti = -1;
            List<Integer> autis = new ArrayList<Integer>();
            for(int i : authorMap.keySet()){
                if(authorMap.get(i)>max){
                    max = authorMap.get(i);
                    auti = i;
                    autis.clear();
                    autis.add(i);
                }else if(authorMap.get(i)==max){
                    autis.add(i);
                }
            }
            if(autis.size() > 1){
                auti = autis.get((int) Math.floor(Math.random()*autis.size()));
            }
            sets.get(seti).add(auti);
            authorMap.remove(auti);
        }
        System.out.println(String.format("Sizes: %d %d %d %d %d", setSize(sets.get(0),authorMap2),setSize(sets.get(1),authorMap2),setSize(sets.get(2),authorMap2),setSize(sets.get(3),authorMap2),setSize(sets.get(4),authorMap2)));
        
        for(int i = 0; i<sets.size();i++){
            File f = new File(dir,""+i+".set");
            PrintStream ps = new PrintStream(f);
            for(int author : sets.get(i)){
                for(String doc : documentAuthorMap.keySet()){
                    if(documentAuthorMap.get(doc) == author){
                        ps.println(doc);
                    }
                }
            }
            ps.close();
        }
    }
    
    private int setSize(List<Integer> set,Map<Integer,Integer> authorMap ){
        int result = 0;
        System.err.println(authorMap.size());
        for(int i : set){
            System.err.println(i);
            result += authorMap.get(i);
        }
        return result;
    }
    
    private void showMissing() throws IOException, InvalidDocumentException {
        File dir = new File(getConfig().get("database-directory"));
        InkAnnoAnnotationStructure a = new InkAnnoAnnotationStructure(getConfig());
        int count = 0;
        for(File f : dir.listFiles()){
            if(f.getName().toLowerCase().endsWith(".inkml")){
                Document d = new Document(f,a);
                if(!d.getInk().containsAnnotation("authorId")){
                    System.out.println(f.getName());
                }
                else{
                    count ++;
                }
            }
        }
        System.out.println("# correct file: "+count);
        
    }


    private void loadMetaData()throws Exception {
        Scanner sc = new Scanner(System.in);
        PrintStream out = System.out;
        String dir = getConfig().get("database-directory");
        InkAnnoAnnotationStructure as = new InkAnnoAnnotationStructure(getConfig());
        InkAnnoAnnotationStructure.Item item = as.getItem(AnnotationStructure.NodeNames.INK, null);
        String answer;
        do{
           out.print("Coma separated list of Files: ");
           String[] files = sc.nextLine().split(",");
           if(files.length == 1 && files[0].equals("")){
               break;
           }
           List<Document> documents = new ArrayList<Document>();
           for(String file : files){
               File f = new File(dir+"/"+file.trim()+".inkml");
               while(!f.exists()){
                   out.print(String.format("File '%s' don't exist, correct it or ignore:",f.getPath()) );
                   file = sc.nextLine().trim();
                   if(file.equals("")){
                       f = null;
                       break;
                   }
                   f = new File(dir+"/"+file.trim()+".inkml");
               }
               if(f==null){
                   continue;
               }
               Document d = new Document(f,as);
               documents.add(d);
               if(d.getInk().containsAnnotation("authorId")){
                   out.println(String.format("File '%s' has allready an author with id '%s':",f.getPath(),d.getInk().getAnnotation("authorId")) );
               }
           }
           for(Annotation an : item.annotations){
               String key = an.name;
               String value = "";
               if(an.valueType!=ValueType.ENUM){
                   out.print("enter value for '"+key+"':");
                   value = sc.nextLine().trim();
                   if(value.equals("")){
                       out.println("ignoring..");
                       continue;
                   }
               }else{
                   StringList match = new StringList();
                   do{
                       out.print("enter value for '"+key+"' ["+an.values.join(",")+"]:");
                       value = sc.nextLine().trim();
                       match.clear(); 
                       for(String proposed :an.values){
                           if(proposed.toLowerCase().startsWith(value.toLowerCase())){
                               match.add(proposed);
                           }
                       }
                       if(match.size() > 1){
                           out.println("'"+value+"' is ambigous.");
                       }else if(match.size() == 0){
                           out.println("'"+value+"' not given.");
                       }
                   }while(match.size() != 1);
                   value = match.get(0);
                   out.println("'"+value+"' is chosen");
               }
               for(Document d : documents){
                   d.getInk().annotate(key, value);
               }
           }
           out.print("save files ?[Y|n] :");
           answer = sc.nextLine();
           if(!answer.equals("") && answer.toLowerCase().charAt(0) == 'n'){
               out.println("Abort.");
               break;
           }
           out.print("save file ... ");
           for(Document d : documents){
               d.save(d.getFile());
               out.print(d.getFile().getName()+" ... ");
           }
           out.print("\ncontinue? [Y|n]: ");
           answer = sc.nextLine();
        }while(answer.equals("") || answer.toLowerCase().charAt(0) != 'n');

        out.println("exit");
    }

    @Override
    public String getApplicationDescription() {
        return "metadataapplier";
    }

    @Override
    public String getApplicationName() {
        return "metadataapplier";
    }

}
