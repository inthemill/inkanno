package ch.unibe.im2.inkanno.imageExport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.JAI;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import ch.unibe.eindermu.utils.Config;
import ch.unibe.eindermu.utils.FileUtil;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.DocumentManager;
import ch.unibe.im2.inkanno.exporter.AbstractExporter;
import ch.unibe.im2.inkanno.exporter.Exporter;
import ch.unibe.im2.inkanno.exporter.ExporterException;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;

import com.sun.media.jai.codec.BMPEncodeParam;
import com.sun.media.jai.codec.ImageEncodeParam;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;

public class ImageExporter extends AbstractExporter {

	
	private FileType fileType = FileType.PNG;

    private String outputType;

    private Dimension forcedDimension;
	
	private static List<RegisteredImageExportDrawer> drawers;
	
	public enum FileType{
		PNG,
		JPEG,
		TIFF,
		BMP;
		public String toString(){
			return super.toString().toLowerCase();
		}
		public static FileType getValue(String name){
			if(name == null){
				return null;
			}
			for(FileType t : FileType.values()){
				if(t.toString().equalsIgnoreCase(name)){
					return t;
				}
			}
			if(name.equalsIgnoreCase("jpg")){
				return JPEG;
			}
			if(name.equalsIgnoreCase("tif")){
				return TIFF;
			}
			return null;
		}
	};
	
	public ImageExporter(){
	    drawers = new ArrayList<RegisteredImageExportDrawer>();
	    drawers.add(new ImageSimpleDrawer());
	    drawers.add(new ImagePixelAccurateBlockDrawer());
	    drawers.add(new ImagePixelAccurateLineDrawer());
	    drawers.add(new ImageSimpleColorBlockDrawer());
	    drawers.add(new ImageSimpleColorTextlineDrawer());
	    drawers.add(new ImageTraceIDDrawer());
	 
	}
	
	
    @Override
    public String getID() {
        return "export_image";
    }


    @Override
    public String getDescription() {
        return "The input file will be exported to as image";
    }
	
    
    public String getDefaultFileNameExtension(){
        if(getFileType() == null){
            FileType.PNG.toString();
        }
        return getFileType().toString();
    }
	
	
	public File determineFile(final Document doc) throws ExporterException {
	    switch (getCase()){
	        case 0: 
	        case 6:
	            return new File(extendFileName(doc.getFile().getPath(),null,getDefaultFileNameExtension()));
	        case 1:
	            return new File(output());
	        case 2: 
	        case 5:
	        case 7:
	        case 9:
	        case 12:
	            throw new ExporterException("Appending image to existing file is not possible, do not use --append cmd-line option");
	        case 3:
	        case 10:
	            throw new ExporterException("Can not export image to stream, only files are valid.");
	        case 4:
	            return getFile();
	        case 8:
	            return new File(extendFileName(output(),getID(doc),null));
	        case 11:
	            return new File(extendFileName(getFile().getPath(),getID(doc),null));
	        default:
	            throw new ExporterException("Exporter in unknown state");
	    }
	}
	
	
	public static Dimension determDimension(final Document doc){
		String size = Config.getMain().get("image_size");
		Rectangle r = doc.getBounds();
		if(size!=null && !size.isEmpty()){
			Dimension dim = new Dimension();
			if(size.matches("[0-9]+x[0-9]+")){
				String[] res = size.split("x");
				int w = Integer.parseInt(res[0]);
				int h = Integer.parseInt(res[1]);
				double f = Math.min(w/(double)r.width, h/(double)r.height);
				dim.width = (int) (doc.getBounds().width * f);
				dim.height = (int) (doc.getBounds().height * f);
			}else{
				System.err.println("--image_size does not satisfy the regexp pattern [0-9]+x[0-9]+ , default size will be taken");
			}
			return dim ;
		}
		return new Dimension(r.width,r.height);
	}
	

	@Override
	public void export() throws ExporterException {
	    if(getDocumentManager() == null){
	        throw new ExporterException(Exporter.ERR_NO_DOCUMENT);
	    }
	    int cas = getCase();
	    
	    DocumentManager docMan = getDocumentManager();
	    
	    while(docMan.hasNext()){
	        Document doc;
            try {
                doc = docMan.nextDocument();
            } catch (InvalidDocumentException e) {
                throw new ExporterException(e.getMessage());
            }
	        File file = determineFile(doc);
	        if(cas == 3 || cas == 10 || file == null){
	            throw new ExporterException("Image export only supports files but not streams as output");
	        }
	        if(isAppendEnabled()){
	            throw new ExporterException("Image export only possible to individual files, --append is not applicable");
	        }
	        if(fileType == null){
	            fileType = getTypeByExtension(file);
	        }else{
	            if(fileType != getTypeByExtension(file)){
	                file = new File(file.getPath()+"."+fileType.toString());
	            }
	        }
	        Dimension dimension = forcedDimension;
	        if (dimension == null){
	            dimension = determDimension(doc);
	        }
	        
	        BufferedImage i = new BufferedImage((int)dimension.getWidth(),(int)dimension.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = i.createGraphics();
            g2.setBackground(Color.WHITE);
            g2.setColor(Color.BLACK);
            RegisteredImageExportDrawer drawer = null;
            
            for(RegisteredImageExportDrawer d : drawers){
                if(getOutputType().equals(d.getId())){
                    drawer = d;
                }
            }
            
            if(drawer == null){
                throw new IllegalArgumentException("There is no image type called '"+getOutputType());
            }
            
            drawer.setDimension(dimension);
            drawer.setStrokeWidth(15 * doc.getMostCommonTraceHeight()/100.0);
            drawer.addTraceFilter(doc.getTraceFilter());
            drawer.setOrientation(!doc.isHMirroring(),!doc.isVMirroring());
            drawer.setGraphics(g2);
            drawer.go(doc.getCurrentViewRoot());
            
            ImageEncodeParam iep = null;
            switch(fileType){
                case PNG:
                    PNGEncodeParam pep = new PNGEncodeParam.RGB();
                    iep = pep;
                    break;
                case JPEG:
                    JPEGEncodeParam jep = new JPEGEncodeParam();
                    jep.setQuality(70);
                    iep = jep;
                    break;
                case BMP:
                    BMPEncodeParam bep = new BMPEncodeParam();
                    bep.setCompressed(true);
                    iep = bep;
                    break;
                case TIFF:
                    TIFFEncodeParam tip = new TIFFEncodeParam();
                    tip.setCompression(TIFFEncodeParam.COMPRESSION_PACKBITS);
                    iep = tip;
                    break;
                    
            }
            JAI.create("filestore", i, file.getAbsolutePath(), fileType.toString().toUpperCase(), iep);
	    }
	}
	
	private FileType getTypeByExtension(File file){
		return FileType.getValue(FileUtil.getInfo(file).extension);
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public FileType getFileType() {
	    if (fileType == null){
	        fileType = FileType.getValue(Config.getMain().get("image_format"));
	    }
		return fileType;
	}
	
	public String getOutputType(){
		if(outputType == null){
            outputType = Config.getMain().get("image_type");
        }
        return outputType;
	}
	
	public List<String> getOutputTypes(){
	    List<String> result = new ArrayList<String>();
	    for(RegisteredImageExportDrawer drawer: drawers){
	        result.add(drawer.getId());
	    }
        return result;
    }


    @Override
    public void addCommandLineOptions(Config c) {
        c.addStringOption("image_format","png","If --action=export_image, this specifies the image format \nit may be one of: png (default), jpg, tiff, bmp.");
        
        String typeDescription = "";
        for(RegisteredImageExportDrawer drawer : drawers){
            typeDescription += "\n   - "+ drawer.getId() + ": " + drawer.getDescription();
        }
        
        c.addStringOption("image_type", drawers.get(0).getId(),"If --action=export_image, this is the type of image produced. It may be one of:" + typeDescription);

        c.addStringOption("image_size","","If --action=export_image, this is the size of the image, in the format WIDTHxHEIGHT\n" +
                "ratio will be kept, this specification is the maximum size that will be reached.\n" +
                "If not given, default size will be used.");        
    }


    public List<RegisteredImageExportDrawer> getDrawers() {
        return drawers;
    }


    @Override
    public JFileChooser getCustomFileChooser(Document doc) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter(){
            @Override
            public boolean accept(File f) {
                return FileType.getValue(FileUtil.getInfo(f).extension) != null;
            }
            public String getDescription() {
                return "image files";
            }
        });
        ImageExportGUI fp = new ImageExportGUI(doc,this);
        fc.setAccessory(fp);
        fc.addPropertyChangeListener(fp);
        return fc;
    }


    /**
     * @param dimension
     */
    public void setDimension(Dimension dimension) {
        forcedDimension = dimension;
    }
}
