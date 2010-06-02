package ch.unibe.im2.inkanno.imageExport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.media.jai.JAI;

import ch.unibe.eindermu.utils.GraphicsBackup;
import ch.unibe.im2.inkanno.Document;
import ch.unibe.inkml.InkTraceViewLeaf;

import com.sun.media.jai.codec.ImageEncodeParam;
import com.sun.media.jai.codec.PNGEncodeParam;

public class SmoothingImage extends RegisteredImageExportDrawer{
	
	private boolean statsRun;
	private ValueStats valueStats;
	private TraceValueExtractor traceValueExtractor;
	private Document document;
    private double sigma = 2;
	
    
    
	public SmoothingImage(Dimension d, Document doc, TraceValueExtractor tf){
		setDimension(d);
		document = doc;
		traceValueExtractor = tf;
		this.setStrokeWidth(5);
	}
	
    @Override
    public String getDescription() {
        return "smothed image";
    }

    @Override
    public String getId() {
        return "smooth";
    }
	
	public void run(File outputFile){
		
		BufferedImage im  = new BufferedImage(this.getDimension().width,getDimension().height,BufferedImage.TYPE_INT_BGR);
		
		this.setGraphics(im.createGraphics());
		getGraphics().setBackground(Color.WHITE);
		statsRun = true;
		valueStats = new ValueStats();
		go(document.getCurrentViewRoot());
		valueStats.close();
		statsRun = false;
		go(document.getCurrentViewRoot());
	
		
		//create Convolution Kernel
		
        int range = 1+(int)(3.0*sigma);
        double[][] mask = new double[2*range+1][2*range+1];
        double sum = 0;
        for (int i = 0; i < range; i++) {
        	for (int j = 0; j < range; j++) {
	            double y = Math.exp(-(i*i + j*j)/2.0/(sigma*sigma));
	            sum += (i==0)?0:2*y;
	            sum += (j==0)?0:2*y;
	            sum += (j==0&&i==0)?y:0;
	            mask[range-i][range+j]= y;
	            mask[range-i][range-j]= y;
	            mask[range+i][range+j]= y;
	            mask[range+i][range-j]= y;
        	}
        }
        for (int i = 0; i < mask.length; i++) {
        	for (int j = 0; j < mask[i].length; j++) {
                  mask[i][j]/=sum;
        	}
        }
        
        Raster r = im.getData();
		
		int [] data = new int[3];
		int [] white = new int[]{0xff,0xff,0xff};
		BufferedImage out = new BufferedImage(getDimension().width, getDimension().height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster outRaster = out.getRaster();
		for(int x = 0; x<getDimension().width;x++){
			for(int y = 0; y<getDimension().height;y++){
				double sumWeight=0;
				double sumValue=0;
				double var = 0;
		        for (int i = -range ; i < range; i++) {
		        	for (int j = -range; j < range; j++) {
		        		if(x+i < 0 || y+j < 0 || x+i >= im.getWidth() ||  y+j >= im.getHeight()){
		        			continue;
		        		}
		        		r.getPixel(x+i, y+j, data);
		        		if(pack(data) == pack(white)){
			            	continue;
			            }
		        		sumWeight += mask[i+range][j+range];
		        		sumValue += mask[i+range][j+range]* pack(data);
		        	}
		        }
		        double mean = (sumWeight==0)?0:sumValue/sumWeight;
		        for (int i = -range ; i < range; i++) {
		        	for (int j = -range; j < range; j++) {
		        		if(x+i < 0 || y+j < 0 || x+i >= im.getWidth() ||  y+j >= im.getHeight()){
		        			continue;
		        		}
		        		r.getPixel(x+i, y+j, data);
		        		if(pack(data) == pack(white)){
			            	continue;
			            }
		        		var += mask[i+range][j+range] * Math.abs(pack(data)-mean);
		        	}
		        }
		        
		        var = (sumWeight==0)?0:var/sumWeight;
		        var = var*100/(valueStats.max-valueStats.min)/4;
		        double nw = (1-sumWeight) * 255;
		        outRaster.setSample(x, y, 0, Math.min(255, Math.sqrt(nw*nw+var)));
			}
		}
		

		
	
		ImageEncodeParam iep = new PNGEncodeParam.Gray();
	    JAI.create("filestore", out, outputFile.getAbsolutePath(), "PNG", iep);
		
	}
	
	private double pack(int[] data) {
		return (data[0]<<16) + (data[1] << 8) + (data[0]);
	}

	public void visitHook(InkTraceViewLeaf leaf) {
		
		GraphicsBackup gb = new GraphicsBackup(getGraphics());
		if(statsRun){
			valueStats.add(traceValueExtractor.getValue(leaf));
		}else{
			getGraphics().setColor(new Color(valueStats.transform(traceValueExtractor.getValue(leaf))));
			super.visitHook(leaf);
		}
		
		gb.reset();
	}
	
	abstract public static class TraceValueExtractor{
		abstract public double getValue(InkTraceViewLeaf leaf) ;
	}
	
	private class ValueStats{
		public double max = Double.NEGATIVE_INFINITY;
		public double min = Double.POSITIVE_INFINITY;
		public double mean;
		public double var;
		public int count;
		public int intMax = 0xffffff;
		public int intMin = 0;
		public void add(double v){
            count++;
            mean += v;
            var += v*v;
            if(max < v)
            	max = v;
            if(min > v)
            	min = v;
		}
		public int normalizeVariance(double var2) {
			return (int) Math.max(255,var*2*255/(double)0xffffff);

		}
		public void close(){
			mean = mean/(double) count;
			var = var/(count) - mean*mean;
		}
		public int transform(double v) {
			return (int) (((v - min) * (double)intMax)/(max-min));
		}
		
	}

    public void setSigma(double d) {
        sigma  = d;
    }


}
