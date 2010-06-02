package ch.unibe.im2.inkanno.imageExport;

import java.awt.Dimension;
import java.io.File;

import ch.unibe.eindermu.utils.Config;
import ch.unibe.im2.inkanno.AbstractInkAnnoMain;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.imageExport.SmoothingImage.TraceValueExtractor;
import ch.unibe.inkml.InkTraceViewLeaf;

public class SmoothFeatureImage extends AbstractInkAnnoMain {

	public static void main(String[] args) {
		SmoothFeatureImage i = new SmoothFeatureImage();
        i.go(args);
    }
	
	@Override
	protected void buildConfig() {
		Config c = getConfig();
		c.addStringOption('c',"feature", null,"<feature (set) name>,<number(in set)>");
		c.addStringOption('i', "input", "","Input file, that will be open on start up.");
        c.addStringOption('o', "output", "", "File where the output will be written to.");
        c.addStringOption("image-format","png","Specifies the image format \nit may be one of: png (default), jpg, tiff, bmp.");
        c.addStringOption("image-size","","Size of the image, in the format WIDTHxHEIGHT\n" +
                "ratio will be kept, this specification is the maximum size that will be reached.\n" +
                "If not given, default size will be used.");
        c.addBooleanOption("no-marking", "ignore marking.");
        c.addStringOption('s', "sigma", "2.0", "Sigma of the Gaussian blur.");
        
        c.nameOtherArg("input");
        c.nameOtherArg("output");


	}

	@Override
	protected void start() throws Exception {
		Document d = this.createDocument();
		Dimension dim = ImageExporter.determDimension(d);
		SmoothingImage si = new SmoothingImage(dim, d, new TraceValueExtractor(){
			@Override
			public double getValue(InkTraceViewLeaf leaf) {
				return (double)((leaf.getTimeSpan().end - leaf.getTimeSpan().start)/2.0) + leaf.getTimeSpan().start;
			}});
		si.setSigma(getConfig().getD("sigma"));
		si.run(new File(getConfig().get("output")));

	}

	@Override
	public String getApplicationDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApplicationName() {
		// TODO Auto-generated method stub
		return "smoothFeatureImage";
	}

}
