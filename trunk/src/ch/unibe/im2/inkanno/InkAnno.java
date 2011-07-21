/*
 * Created on 23.07.2007
 *
 * Copyright (C) 2007  Emanuel Indermühle <emanuel@inthemill.ch>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @author emanuel
 */

package ch.unibe.im2.inkanno;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.unibe.eindermu.Messenger;
import ch.unibe.eindermu.utils.Config;
import ch.unibe.im2.inkanno.DocumentRecognizer.FileType;
import ch.unibe.im2.inkanno.exporter.Exporter;
import ch.unibe.im2.inkanno.exporter.ExporterException;
import ch.unibe.im2.inkanno.exporter.ExporterFactory;
import ch.unibe.im2.inkanno.exporter.FactoryException;
import ch.unibe.im2.inkanno.gui.GUI;
import ch.unibe.im2.inkanno.gui.GUIMessenger;
import ch.unibe.im2.inkanno.importer.InkMLImporter;
import ch.unibe.im2.inkanno.util.DocumentRepair;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;
import ch.unibe.im2.inkanno.util.ManualInteractionNeededException;
import ch.unibe.inkml.InkCanvas;
import ch.unibe.inkml.InkMLComplianceException;

public class InkAnno extends AbstractInkAnnoMain {
    
    public static final String APP_NAME = "$name$"; //$NON-NLS-1$
    
    public static final String APP_VERSION = "$version$"; //$NON-NLS-1$
    
    public static final String CMD_OPT_VERSION = Strings.getString("InkAnno.cmd_opt_version"); //$NON-NLS-1$
    
    public static final String CMD_OPT_ACTION = Strings.getString("InkAnno.cmd_opt_action"); //$NON-NLS-1$
    public static final String CMD_OPT_ACTION_GUI = Strings.getString("InkAnno.cmd_opt_action_gui"); //$NON-NLS-1$
    public static final String CMD_OPT_ACTION_REPAIR = Strings.getString("InkAnno.cmd_opt_action_repair"); //$NON-NLS-1$
    public static final String CMD_OPT_NO_MARKING = Strings.getString("InkAnno.cmd_opt_no-marking"); //$NON-NLS-1$
    public static final String CMD_OPT_APPEND = Strings.getString("InkAnno.cmd_opt_append"); //$NON-NLS-1$
    public static final String CMD_OPT_INPUT_FILE = "input-list";
    
    private static InkAnno instance;
    
    public static InkAnno getInstance(){
       if(InkAnno.instance == null){
           InkAnno.instance = new InkAnno();
       }
       return InkAnno.instance;
    }
    
    
    /**
     * Main method of the application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        InkAnno i = InkAnno.getInstance();
        i.go(args);
    }
    

    private List<Exporter> exporters = new ArrayList<Exporter>();
    
    private InkCanvas defaultCanvas;
    
    
    @Override
    public String getApplicationDescription() {
        return Strings.getString("InkAnno.app_desc");  //$NON-NLS-1$
    }

    @Override
    public String getApplicationName() {
        return APP_NAME;
    }

    @Override
    protected void buildConfig() {
        Config c = getConfig();
        c.addBooleanOption('v',CMD_OPT_VERSION,String.format(Strings.getString("InkAnno.cmd_opt_version_desc"),APP_NAME));
        // initialize different exporters
        exporters = (new ExporterFactory()).loadAvailableExporters();
        
        //add the exporters action strings
        String actions = "";  //$NON-NLS-1$
        for(Exporter e : exporters){
            actions += String.format(Strings.getString("InkAnno.cmd_opt_act_desc_structure"),e.getID(),e.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        c.addStringOption('a', CMD_OPT_ACTION, CMD_OPT_ACTION_GUI,String.format(Strings.getString("InkAnno.cmd_opt_action_desc")+  //$NON-NLS-1$ //$NON-NLS-2$
                actions,
                CMD_OPT_ACTION_GUI,
                CMD_OPT_ACTION_REPAIR));
        
       
        c.addStringOption('i', INPUT, "",Strings.getString("InkAnno.cmd_opt_input_desc")); //$NON-NLS-1$ //$NON-NLS-2$
        c.addBooleanOption(CMD_OPT_INPUT_FILE, "if specified, the input file is interpreted as a list of input files");
        c.addStringOption('o', OUTPUT, "", Strings.getString("InkAnno.cmd_opt_output_desc")); //$NON-NLS-1$ //$NON-NLS-2$
        c.addBooleanOption(CMD_OPT_APPEND, Strings.getString("InkAnno.cmd_opt_append_desc")); //$NON-NLS-1$
        c.addStringOption("colorizer","black","method how to choose color for the traces");
        c.addStringOption("colorizer-args","","arguments for the colorizer to work properly");
        //add other exporter command line options
        for(Exporter e : exporters){
            e.addCommandLineOptions(c);
        }
        
        c.addBooleanOption(CMD_OPT_NO_MARKING, Strings.getString("InkAnno.cmd_opt_no-marking_desc")); //$NON-NLS-1$
        c.nameOtherArg(INPUT);
    }

    @Override
    protected void start() throws FileNotFoundException, IOException{
    	Config c =getConfig(); 
        if(c.getB(CMD_OPT_VERSION)){
            System.out.println(APP_VERSION);
            System.exit(0);
        }
        loadInkAnnoCanvas();
        String command =  c.get(CMD_OPT_ACTION);
        if(command.equals(CMD_OPT_ACTION_GUI)){
            startGui();
        }else if(command.equals(CMD_OPT_ACTION_REPAIR)){
        	startRepair();
        }else{
            boolean executed = false;
            for(final Exporter exporter : exporters){
                if(exporter.getID().equals(command)){
                    try {
                        DocumentManager it = getDocumentManager(true);
                        exporter.setDocumentManager(it);
                        /*if(it.size() == 0){
                            Messenger.error("No input file has been specified"); 
                            System.exit(1);
                        }*/
                        exporter.setOptionsByCommandLineOptions(c);
                        exporter.export();
                    } catch (ExporterException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Messenger.error(String.format(Strings.getString("InkAnno.action_export_err"),e.getMessage())); //$NON-NLS-1$
                        System.exit(1);
                    }
                    executed = true;
                }
            }
            if(!executed){
                Messenger.error(String.format(Strings.getString("InkAnno.cmd_opt_action_err_unkown"),command)); //$NON-NLS-1$
                System.exit(1);
            }
        }
    }
    
    /**
     * loads the canvas provided by inkanno. Which is located in the file 'InkAnnoInkMLCanvas.xml' 
     * in the same package as this class.
     * InkML sources that should be read by inkanno must be at least conform this canvas and its traceFormat.
     */
    private void loadInkAnnoCanvas(){
        try {
            InkMLImporter importer = new InkMLImporter((getClass().getResourceAsStream("InkAnnoInkMLCanvas.inkml")));
            defaultCanvas = (InkCanvas) importer.createInk().getDefinitions().get("inkAnnoCanvas");
        } catch (Throwable e) {
            Messenger.error(String.format("Can't load inkanno canvas: %s",e.getMessage()));
            e.printStackTrace();
            System.exit(1);
        } 
    }
    

    
    private void startRepair() throws IOException{
        DocumentManager it = getDocumentManager(true);
        while(it.hasNext()){
            Document d;
            try {
                d = it.nextDocument();
            } catch (InvalidDocumentException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
                continue;
            }
            if(d.getType() != FileType.INKML){
                Messenger.error(Strings.getString("InkAnno.action_repair_err_input")); //$NON-NLS-1$
                System.exit(1);
            }
            DocumentRepair tc = new DocumentRepair(d);
            boolean res = false;
            try {
                System.out.println("checking "+d.getFile().getName());
                res = tc.run();
            } catch (InkMLComplianceException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                System.exit(1);
            } catch (ManualInteractionNeededException e) {
                Messenger.error(e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            if(!res){
                Messenger.inform(Strings.getString("InkAnno.action_repair_msg_no_job")); //$NON-NLS-1$
            }
            File output = null;
            if(getConfig().get(OUTPUT) != null && !getConfig().get(OUTPUT).isEmpty()){ //$NON-NLS-1$
                output = new File(getConfig().get(OUTPUT));
            }else{
                if(!res){
                    continue;
                }
                output = d.getFile();
            }
            try {
                d.save(output);
            } catch (ExporterException e) {
                e.printStackTrace();
            } catch (FactoryException e) {
                e.printStackTrace();
            }
        }
    	
    }
    


	private void startGui(){
        try {
            Messenger.add(new GUIMessenger());
            DocumentManager it = getDocumentManager(true);
            new GUI(it);
            if(it.hasNext()){
                it.nextDocument();
            }
        } catch (IOException e) {
            Messenger.error(String.format("Can't load document manager: %s",e.getMessage()));
            System.exit(1);
        } catch (InvalidDocumentException e) {
            Messenger.error(String.format("Can't load first document: %s",e.getMessage()));
            System.exit(1);
        }
    }
    
	

	public static Config config() {
		return InkAnno.getInstance().getConfig();
	}
	
	public InkCanvas getCanvas(){
	    return this.defaultCanvas;
	}
}
