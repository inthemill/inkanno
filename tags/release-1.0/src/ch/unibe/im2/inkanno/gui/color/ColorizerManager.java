/**
 * 
 */
package ch.unibe.im2.inkanno.gui.color;

import java.awt.Graphics2D;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import ch.unibe.eindermu.utils.Config;
import ch.unibe.im2.inkanno.InkAnno;
import ch.unibe.im2.inkanno.exporter.Exporter;
import ch.unibe.im2.inkanno.exporter.FactoryException;
import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.TraceViewFilter;

/**
 * ColorizerManager manages different colorizers, which can be added in additional
 * jars. There you have to create the file "colorizer_implementation.properties" in the packages
 * "ch.unibe.im2.inkanno.gui.color" in this file you can specify a list of classes which implement the "Colorizer" interface.
 * This list must be in the following format:
 * <code>
 * colorizer.whatevername=package.of.class.ClassImplementingColorizerInterface
 * colorizer.anothername=package.of.class.AnotherClassImplementingTheColorizerInterface
 * </code>
 * The Colorizer interface selects then one of this colorizers as the default colorizer. This can be changed by the 
 * command line option --colorizer.
 * The selected colorizer will the be used to choose the color of the displayed strokes. 
 * @author emanuel
 *
 */
public class ColorizerManager {

    private static ColorizerManager cm;
    
    private List<Colorizer> colorizers;

    private Colorizer currentColorizer;

    /**
     * Returns the singleton of the Colorizer manager
     * @return
     */
    public static ColorizerManager getInstance() {
        if(cm == null){
            cm = new ColorizerManager();
        }
        return cm;
    }

    /**
     * Only one instance allowed, use getInstance()
     */
    private ColorizerManager(){
        colorizers = loadAvailableColorizer();
        if(colorizers.size() == 0){
            currentColorizer = new NullColorizer();
        }else
        if(Config.getMain().get("colorizer")!= null && !Config.getMain().get("colorizer").equals("")){
            String selection = Config.getMain().get("colorizer");
            for(Colorizer col : colorizers){
                if(col.isResponsible(selection)){
                    currentColorizer = col;
                    break;
                }
            }
            
        }
        if(currentColorizer == null){
            currentColorizer = new NullColorizer();
        }
    }
    
    /**
     * This method is used to set a color for a specified InkTraceViewLeaf "s".
     * The color must be set to graphics. As you may see. You also have the oportunity to change other  
     * setting to graphics. These settings are only valid for this view. And then the default settings are restored.
     * The Settings restored by the class ch.unibe.eindermu.utils.GraphicsBackup. So do not change settings which
     * are not restored by this class.
     * @param graphics
     * @param s
     */
    public void setColor(Graphics2D graphics, InkTraceViewLeaf s) {
        currentColorizer.setColor(graphics,s);
        
    }
    /**
     * This sets a filter to the colorizer. This is useful if for the initialization of the colorizer
     * information of all strokes are needed. 
     * @param filter
     */
    public void setFilter(TraceViewFilter filter){
        if(currentColorizer != null){
            currentColorizer.setFilter(filter);
        }
    }
    
    
    private List<Colorizer> loadAvailableColorizer(){
        List<Colorizer> l = new ArrayList<Colorizer>();
        try {
            Enumeration<URL> en  = ClassLoader.getSystemClassLoader().getResources("ch/unibe/im2/inkanno/gui/color/colorizer_implementation.properties");
            while(en.hasMoreElements()){
                Properties p = new Properties();
                URL url = en.nextElement();
                p.load(url.openStream());
                for(Object objstr : p.keySet()){
                    String str = (String)objstr;
                    if(str.equals("colorizer") || str.startsWith("colorizer.")){
                        if(p.getProperty(str) != null){
                            l.add(createPlugin(p.getProperty(str)));
                        }        
                    }
                }
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        return l;
    }
    
    
    private Colorizer createPlugin(String name) throws FactoryException{
        Class c = null;
        try {
            c = Class.forName(name);
        } catch (ClassNotFoundException e1) {
            throw new FactoryException("Class '"+name+"' could not be found.");
        }
        if(c != null){
            Colorizer x = null;
            try {
                x = (Colorizer) c.newInstance();
            } catch (InstantiationException e) {
                throw new FactoryException("Class '"+name+"' is not valid.");
            } catch (IllegalAccessException e) {
                throw new FactoryException("Class '"+name+"' is not valid.");
            }
            return x;
        }else{
            throw new FactoryException("Class '"+name+"' could not be found.");
        }
    }
    
}
