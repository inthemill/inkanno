/**
 * 
 */
package ch.unibe.im2.inkanno;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;

import ch.unibe.eindermu.utils.Aspect;
import ch.unibe.eindermu.utils.Observer;
import ch.unibe.im2.inkanno.gui.GUI;
import ch.unibe.im2.inkanno.util.DocumentRepair;
import ch.unibe.inkml.InkAnnotatedElement;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.InkTraceViewContainer;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.AnnotationStructure.Annotation;
import ch.unibe.inkml.AnnotationStructure.Item;
import ch.unibe.inkml.AnnotationStructure.NodeNames;
import ch.unibe.inkml.AnnotationStructure.Annotation.ValueType;
import ch.unibe.inkml.util.TraceVisitor;

/**
 * @author emanuel
 *
 */
public class AnnotationStructureValidator extends TraceVisitor{

    
    private static AnnotationStructureValidator instance;

    public static AnnotationStructureValidator getInstance(){
        if(instance == null){
            instance = new AnnotationStructureValidator();
        }
        return instance;
    }
    
    /**
     * @author emanuel
     *
     */
    public class Errors {
        /**
         * @param autoCorrected
         */
        public E_type etype;
        public List<InkAnnotatedElement> concerns = new ArrayList<InkAnnotatedElement>();
        public String text;
        /**
         * @return
         */
        public Collection<InkTraceView> getViews() {
            List<InkTraceView> l = new ArrayList<InkTraceView>();
            for(InkAnnotatedElement a : concerns){
                if (a instanceof InkTraceView) {
                    l.add((InkTraceView) a);
                }
            }
            return l;
        } 
        
    }

    /**
     * @author emanuel
     *
     */
    public class Entry<T1, T2> {
        public T1 t1;
        public T2 t2;
        /**
         * @param item
         * @param a
         */
        public Entry(T1 o1, T2 o2) {
            t1 = o1;
            t2 = o2;
        }
        public boolean equals(Object other){
            return other instanceof Entry && other.hashCode() == hashCode();
        }
        public int hashCode(){
            if(t1 == t2 && t1 == null){
                return 0;
            }else
            if(t1 == null){
                return t2.hashCode() ^ "t2".hashCode();
            }else
            if(t2 == null){
                return t1.hashCode() ^ "t1".hashCode();
            }else{
                return t1.hashCode() ^ t2.hashCode();
            }
        }
    }
    
    public static class State{

        private States state = States.SEARCH_NEXT;
        
        public synchronized void error(){
            if(state != States.CANCEL){
                state = States.ERROR;
            }
        }
        public synchronized boolean change(States question, States doThis){
            if(this.state == question){
                state = doThis;
                return true;
            }
            return false;
        }
        /**
         * 
         */
        public synchronized void cancel() {
            state = States.CANCEL;
        }
        /**
         * @param cancel
         * @return
         */
        public boolean is(States s) {
            return state == s;
        }
    }

    public enum States {
        ERROR,
        SEARCH_NEXT,
        TEST_AGAIN,
        IGNORE_THIS,
        CANCEL
    }
    
    public enum E_type{
        AUTO_CORRECTED,
        WARNING,
        ERROR
    };
    
    
    public enum Test{
        MISSING_ANNOTATION, NOT_COUNTRYCODE, NO_DATE, NOT_ENUMERATED, NO_INTEGER, NO_LANGUAGECODE, EMPTY_TRACES, HIERARCHY
        ;
        public String desc(){
            switch(this){
            case MISSING_ANNOTATION:
                return "Element '%s' has no annotation '%s' as requested";
            case NOT_COUNTRYCODE:
                return "Annotation '%2$s' on element '%1$s' must contain country code, but contains '%3$s'";
            case NO_DATE:
                return "Annotation '%2$s' on element '%1$s', %3$s counld no be parsed as date";
            case NOT_ENUMERATED:
                return "Annotation '%2$s' on element '%1$s' must contain one of the enumerated values, not '%3$s'";
            case NO_INTEGER:
                return "Annotation '%2$s' on element '%1$s' must contain a number, not '%3$s'";
            case NO_LANGUAGECODE:
                return "Annotation '%2$s' on element '%1$s' must contain a language code, but contains '%3$s'";
            case EMPTY_TRACES:
                return "Element '%s' directly contains strokes, which is not allowed";
            case HIERARCHY:
                return "Element '%s' contains element '%s' which is not allowed";
            }
            return null;
        }
    }
    /**
     * @author emanuel
     *
     */
    


    private GUI gui;
    private Selection selection;
    private Document document;
    private InkAnnoAnnotationStructure structure;
    private Set<Entry<Item,Object>> ignoring;
    private State state;
    private List<Errors> errors;
    private int errorIndex;
    
    private boolean active = false;
    private JFrame controlFrame;
    private JTextArea textarea;
    private Errors ce;
    private JLabel statErr;
    private JLabel statWar;
    private JLabel statAut;
    

    
    
    public void validateDocument(Document doc,Selection sel,GUI guiarg){
        
        if(document != doc){
            document = doc;
            selection = sel;
            structure = doc.getAnnotationStructure();
            ignoring = new HashSet<Entry<Item,Object>>();
            if(gui != guiarg){
                gui = guiarg;
                loadControlFrame();
            }
        }
        
        errors = new ArrayList<Errors>();
        errorIndex = 0;
        state = new State();
        
        active = true;
        testElement(doc.getInk());
        go(doc.getCurrentViewRoot());
        controlFrame.pack();
        controlFrame.setVisible(true);
        refreshView();
    }
    
    
    /**
     * 
     */
    private void loadControlFrame() {
        
        gui.getDocumentManager().registerFor(DocumentManager.ON_DOCUMENT_PRESENT, new Observer() {
            @Override
            public void notifyFor(Aspect event, Object subject) {
                if(!active){
                    return;
                }
                Document ndoc = (Document) subject;
                if(ndoc != document){
                    validateDocument(ndoc, ndoc.getSelection(), gui);
                }
            }
        });
        JFrame.setDefaultLookAndFeelDecorated(true);
        controlFrame = new JFrame("Validation",gui.getGraphicsConfiguration()); 
        controlFrame.getContentPane().setLayout(new BorderLayout());
        controlFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        controlFrame.setUndecorated(true);
        controlFrame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        textarea = new JTextArea();
        textarea.setPreferredSize(new Dimension(300,100));
        textarea.setLineWrap(true);
        textarea.setEditable(false);
        textarea.setWrapStyleWord(true);
        controlFrame.add(textarea,BorderLayout.CENTER);
        
        
        Box stats = Box.createVerticalBox();
        statErr = new JLabel("Errors: 0");
        statErr.setForeground(Color.red);
        statWar = new JLabel("Warn's: 0");
        statWar.setForeground(Color.ORANGE);
        statAut = new JLabel("Autos : 0");
        statAut.setForeground(Color.GREEN);
        stats.add(statErr);
        stats.add(statWar);
        stats.add(statAut);
        
        controlFrame.add(stats,BorderLayout.WEST);
        Box b = Box.createVerticalBox();
        controlFrame.add(b,BorderLayout.EAST);
        
        JButton next = new JButton("Next");
        next.addActionListener(new ActionListener() {
            @Override
            public synchronized void  actionPerformed(ActionEvent e) {
                if(errorIndex < errors.size()-1){
                    errorIndex++;
                }
                refreshView();
            }
        });
        b.add(next);
        
        JButton previous  = new JButton("Previous");
        previous.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(errorIndex > 0){
                    errorIndex--;
                }
                refreshView();
            }
        });
        b.add(previous);
        
        JButton refresh  = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                validateDocument(document, selection, gui);
                errorIndex = 0;
                refreshView();
            }
        });
        b.add(refresh);
        controlFrame.addWindowListener(new WindowListener(){

            @Override
            public void windowActivated(WindowEvent e) {
                //ignore
            }

            @Override
            public void windowClosed(WindowEvent e) {
                //
            }

            @Override
            public void windowClosing(WindowEvent e) {
                active = false;
                controlFrame.dispose();
                controlFrame.setVisible(false);
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
              //ignore
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
              //ignore
            }

            @Override
            public void windowIconified(WindowEvent e) {
              //ignore
            }

            @Override
            public void windowOpened(WindowEvent e) {
              //ignore
            }});
    }
        
    
    public void refreshView(){
        Errors err = null;
        int er=0,w=0,a=0;
        if(!errors.isEmpty()){
            err = errors.get(errorIndex);
            for(Errors e: errors){
                switch(e.etype){
                case AUTO_CORRECTED:
                    a++;
                    break;
                case ERROR:
                    er++;
                    break;
                case WARNING:
                    w++;
                    break;
                }
            }
        }
        statErr.setText("Errors: "+er);
        statWar.setText("Warn's: "+w);
        statAut.setText("Autos : "+a);
        selection.clear();
        if(err != null){
            textarea.setText((errorIndex+1)+"/"+errors.size()+" "+err.text);
            selection.addAll(err.getViews(), null);
        }else{
            textarea.setText("No errors found");
        }
        
    }
    
    /**
     * @param ink
     */
    private void testElement(InkAnnotatedElement element) {
        if(state.is(States.CANCEL)){
            return;
        }
        Item item  = structure.getItem(element);

        //test annotations
        for(Annotation a : item.annotations){
            Entry<Item,Object> entry = new Entry<Item,Object>(item,a);
            do{
                
                if(!ignoring.contains(entry)){
                    ce = new Errors();
                    if(element instanceof InkTraceView){
                        ce.concerns.add(element);
                    }
                    testAnnotation(element,item,a);    
                }
                
                if(state.is(States.ERROR)){
                    error();
                }
                if(state.is(States.CANCEL)){
                    return;
                }
                if(state.change(States.IGNORE_THIS, States.SEARCH_NEXT)){
                    ignoring.add(entry);
                }
            }while(state.change(States.TEST_AGAIN, States.SEARCH_NEXT));
        }
        
        item  = structure.getItem(element);
        
        if(item.node == NodeNames.TRACEVIEW){
            InkTraceViewContainer container  = (InkTraceViewContainer) element;
            
            do{
                Entry<Item,Object> entry = new Entry<Item, Object>(item, "empty trace");
                if(!ignoring.contains(entry)){
                    ce = new Errors();
                    testEmptyTraces(container,item);
                }
                
                if(state.is(States.ERROR)){
                    error();
                }
                
                if(state.is(States.SEARCH_NEXT)){
                    for(InkTraceView view : container.getContent()){
                        if(view.isLeaf()){
                            continue;
                        }
                        Item child = structure.getItem(view);
                        entry = new Entry<Item,Object>(item,child);
                        
                        if(!ignoring.contains(entry)){
                            ce = new Errors();
                            testTraceChild(container,view,item,child);
                        }
                        if(state.is(States.ERROR)){
                            error();
                        }
                        if(!state.is(States.SEARCH_NEXT)){
                            break;
                        }
                    }
                }
                
                if(state.is(States.SEARCH_NEXT)){
                    testTableSanity(container,item);
                }
                
                if(state.is(States.ERROR)){
                    error();
                }
                if(state.is(States.CANCEL)){
                    return;
                }
                if(state.change(States.IGNORE_THIS, States.SEARCH_NEXT)){
                    ignoring.add(entry);
                    continue;
                }
                if(state.change(States.TEST_AGAIN, States.SEARCH_NEXT)){
                    continue;
                }
                if(state.is(States.SEARCH_NEXT)){
                    break;
                }
            }while(false);
        }
    }
    /**
     * @param container
     * @param item
     */
    private void testTableSanity(InkTraceViewContainer container, Item item) {
        if(!container.testAnnotation(InkAnnoAnnotationStructure.TYPE, "Table")){
            return;
        }
        DocumentRepair repair = new DocumentRepair(null);
        int i = repair.recognizeTableIssue(container);
        if(i==1){
            return;
        }else if(i==-1){
            if(state.change(States.SEARCH_NEXT, States.ERROR)){
                ce.etype = E_type.ERROR;
                ce.concerns.add(container);
                ce.text = String.format("Table is not well formed, can't automaticly correct it");
            }
        }else{
            repair.repairTableIssue(container);
            if(state.change(States.SEARCH_NEXT, States.ERROR)){
                ce.etype = E_type.AUTO_CORRECTED;
                ce.concerns.add(container);
                ce.text = String.format("Table was not well formed, it was automatically corrected.");
            }
        }
    }


    /**
     * @param container
     * @param view
     * @param item
     * @param child
     */
    private void testTraceChild(InkTraceViewContainer container,
            InkTraceView view, Item item, Item child) {
        if(!item.containItem(child)){
            if(state.change(States.SEARCH_NEXT, States.ERROR)){
                if(!autoCorrect(Test.HIERARCHY, container, item, child, null)){
                    ce.etype = E_type.ERROR;
                    ce.concerns.add(view);
                    ce.text = String.format(Test.HIERARCHY.desc(),item.getLabel(),child.getLabel());
                }
            }
        }
    }


    /**
     * @param container
     * @param item
     */
    private void testEmptyTraces(InkTraceViewContainer container, Item item) {
        if(item.containsTraces){
            return;
        }
        boolean tfound = false;
        
        for(InkTraceView view : container.getContent()){
            if(view.isLeaf()){
                tfound = true;
                ce.concerns.add(view);
            }
        }
        if(tfound){
            if(state.change(States.SEARCH_NEXT, States.ERROR)){
                if(!autoCorrect(Test.EMPTY_TRACES, container, item, null, null)){
                    ce.etype = E_type.ERROR;
                    ce.text = String.format(Test.EMPTY_TRACES.desc(),container.getLabel());
                }
            }
        }     
    }


    /**
     * @param element
     * @param item
     * @param a
     */
    private void testAnnotation(InkAnnotatedElement element, Item item,
            Annotation a) {
        
        String value= element.getAnnotation(a.name);
        if(!element.containsAnnotation(a.name) && a.valueType != Annotation.ValueType.FREE && a.valueType != Annotation.ValueType.PROPOSED && !a.optional){
            if(state.change(States.SEARCH_NEXT, States.ERROR)){
                if(!autoCorrect(Test.MISSING_ANNOTATION,element,item,a,null)){
                    ce.etype = E_type.ERROR;
                    ce.text = String.format(Test.MISSING_ANNOTATION.desc(),element.getLabel(),a.name); 
                }
            }
        }else if(!element.containsAnnotation(a.name) && a.optional){
            // every thing is ok;
        }else if(a.valueType == ValueType.COUNTRYCODE){
            boolean ccfound = false;
            for(String cc : Locale.getISOCountries()){
                if(value.equals(cc)){
                    ccfound = true;
                }
            }
            if(ccfound == false){
                if(state.change(States.SEARCH_NEXT, States.ERROR)){
                    if(!autoCorrect(Test.NOT_COUNTRYCODE, element, item, a,value)){
                        ce.etype = E_type.ERROR;
                        ce.text = String.format(Test.NOT_COUNTRYCODE.desc(),element.getLabel(),a.name,value);
                    }
                }
            }
        }else if(a.valueType == ValueType.DATE){
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date d = sdf.parse(value);
            } catch (ParseException e) {
                if(state.change(States.SEARCH_NEXT, States.ERROR)){
                    if(!autoCorrect(Test.NO_DATE, element, item, a,value)){
                        ce.etype = E_type.ERROR;
                        ce.text = String.format(Test.NO_DATE.desc(),element.getLabel(),a.name,value);
                    }
                }
            }
        }else if(a.valueType == ValueType.ENUM){
            boolean efound = false;
            for(String must : a.values){
                if(must.equals(value)){
                    efound = true;
                }
            }
            if(!efound){
                if(state.change(States.SEARCH_NEXT, States.ERROR)){
                    if(!autoCorrect(Test.NOT_ENUMERATED, element, item, a,value)){
                        ce.etype = E_type.ERROR;
                        ce.text = String.format(Test.NOT_ENUMERATED.desc(),element.getLabel(),a.name,value);    
                    }
                    
                }
            }
        }else if(a.valueType == ValueType.INTEGER){
            try{
                Integer.parseInt(value);
            }catch(NumberFormatException e){
                if(state.change(States.SEARCH_NEXT, States.ERROR)){
                    if(!autoCorrect(Test.NO_INTEGER, element, item, a,value)){
                        ce.etype = E_type.ERROR;
                        ce.text = String.format(Test.NO_INTEGER.desc(),element.getLabel(),a.name,value);
                    }
                }
            }
        }else if(a.valueType == ValueType.LANGUAGECODE){
            boolean ccfound = false;
            for(String cc : Locale.getISOLanguages()){
                if(value.toLowerCase().equals(cc.toLowerCase())){
                    ccfound = true;
                }
            }
            if(ccfound == false){
                if(state.change(States.SEARCH_NEXT, States.ERROR)){
                    if(!autoCorrect(Test.NO_LANGUAGECODE, element, item, a ,value)){
                        ce.etype = E_type.ERROR;
                        ce.text = String.format(Test.NO_LANGUAGECODE.desc(),element.getLabel(),a.name,value);    
                    }
                }
            }
        }
        
    }

    /**
     * @param missingAnnotation
     * @param element
     * @param item
     * @param a
     * @return
     */
    private boolean autoCorrect(Test testType, InkAnnotatedElement element, Item item, Object a,String value) {
        switch(testType){
        case MISSING_ANNOTATION:
            if (element instanceof InkTraceView) {
                InkTraceView view = (InkTraceView) element;
                if(view.isRoot() && ((Annotation)a).name.equals(InkAnnoAnnotationStructure.TYPE)){
                    element.annotate(InkAnnoAnnotationStructure.TYPE, "Document");
                    ce.text = "Type of view root has been set to 'Document'";
                    ce.etype = E_type.AUTO_CORRECTED;
                    return true;
                }
                
            }
            break;
        case NO_LANGUAGECODE:
            for(Locale l: Locale.getAvailableLocales()){
                if(l.getDisplayLanguage().toLowerCase().equals(value.toLowerCase()) 
                        || l.getDisplayLanguage(Locale.US).toLowerCase().equals(value.toLowerCase())
                        ){
                    element.annotate(((Annotation)a).name, l.getLanguage());
                    ce.text = "Language has been normalized from "+value+" to "+l.getLanguage();
                    ce.etype = E_type.AUTO_CORRECTED;
                    return true;
                }
            }
            break;
        case NOT_COUNTRYCODE:
            for(Locale l: Locale.getAvailableLocales()){
                if(l.getDisplayCountry().toLowerCase().equals(value.toLowerCase()) 
                        || l.getDisplayCountry(Locale.US).toLowerCase().equals(value.toLowerCase())
                        ){
                    element.annotate(((Annotation)a).name, l.getCountry());
                    ce.text = "Country has been normalized from "+value+" to "+l.getCountry();
                    ce.etype = E_type.AUTO_CORRECTED;
                    return true;
                }
            }
            break;
        }
        
        
        return false;
    }


    /**
     * @param format
     * @param element
     */
    private void error() {
        errors.add(ce);
        state.change(States.ERROR, States.SEARCH_NEXT);
    }


    @Override
    protected void visitHook(InkTraceViewLeaf leaf) {
        //testElement(leaf);
    }
    
    @Override
    protected void visitHook(InkTraceViewContainer container) {
        testElement(container);
        super.visitHook(container);
    }
    
}
