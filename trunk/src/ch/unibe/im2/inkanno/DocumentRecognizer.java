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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.unibe.im2.inkanno.importer.IAMonDBImporter;
import ch.unibe.im2.inkanno.importer.InkAnnoStrokeImporter;
import ch.unibe.im2.inkanno.importer.InkMLImporter;
import ch.unibe.im2.inkanno.importer.PGC_Text_Importer;
import ch.unibe.im2.inkanno.importer.StrokeImporter;
import ch.unibe.im2.inkanno.importer.WhiteboardStrokeImporter;
import ch.unibe.im2.inkanno.util.InvalidDocumentException;

public class DocumentRecognizer extends DefaultHandler{
    
	public enum FileType { 
		WHITEBOARD,
		IAM_ON_DB,
		LOGITECH_PEN_V1,  
		LOGITECH_PEN_V2, 
		LOGITECH_PEN_V2_TRANSFORMED , 
		INK_ANNO, 
		INKML, 
		PGC_CUSTOM};
    
    private FileType result;
    
    private int counter = 0;
    
    private SAXParser parser;
    
    public StrokeImporter getStrokeImporter(File file) throws IOException, InvalidDocumentException {
    	
    	InputStream stream  = null;
    	/*try{
	    	ZipFile zipfile = new ZipFile(file);
	    	ZipEntry file_in_zipfile = zipfile.getEntry(file.getName());
	    	if(file_in_zipfile != null){
	    		stream = zipfile.getInputStream(file_in_zipfile);
	    	}else{
	    		zipfile.close();
	    		throw new IOException("Inkanno input file may be ziped, then the zipfile must contain a file the same name as the zipfile");
	    	}
    	}catch (ZipException e) {
			// file seams not to be a zip file. this is ugly but i don't know an other method to test for a zip file.*/
    		stream = new FileInputStream(file);
		//}
    	BufferedInputStream bstream = new BufferedInputStream(stream);
    	bstream.mark(10000);
        this.loadFromFile(bstream);
        bstream.reset();
        
        switch (result){
        	case IAM_ON_DB:
        		return new IAMonDBImporter(file);
        	case WHITEBOARD:
        		return new WhiteboardStrokeImporter(file);
        	case INK_ANNO:
        		return new InkAnnoStrokeImporter(file);
        	case  LOGITECH_PEN_V1:
        		return new LogitechIO_V1_Importer(file);
        	case INKML:
        		return new InkMLImporter(file);
        	case PGC_CUSTOM:
        		return new PGC_Text_Importer(file);
        	default:
        		throw new InvalidDocumentException("'" + file.getPath() + "' has unknown format");
        }
        
    }
    
    public FileType getType(){
    	return result;
    }
    
    private void loadFromFile(BufferedInputStream bstream) throws IOException {
    	
        // Find a parser

    	if(this.textNonXMLFormat(bstream)){
    		return;
    	}
    	bstream.reset();
    	
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            this.parser = factory.newSAXParser();
            this.parser.parse(bstream, this);
            
        } catch(ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        } catch(SAXException e) {
            if(!e.getMessage().equals("success")) {
                throw new IOException(e.getMessage());
            }
        }finally{
        	this.parser = null;
        }
    }
    
    private boolean textNonXMLFormat(InputStream stream) throws IOException {
		Scanner scan = new Scanner(stream);
		if(scan.findWithinHorizon("Pen id:", 1000)!= null){
			this.result = FileType.PGC_CUSTOM;
			return true;
		}
		return false;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.counter++;
        if(qName.equals("WhiteboardCaptureSession")) {
            result = FileType.WHITEBOARD;
            //throw new SAXException("success");
        }
        if(result == FileType.WHITEBOARD && qName.equals("Transcription")){
        	result = FileType.IAM_ON_DB;
        	throw new SAXException("success");
        }
        if(result == FileType.WHITEBOARD && qName.equals("Stroke")){
        	throw new SAXException("success");
        }
        if(qName.equals("InkAnno")) {
            this.result = FileType.INK_ANNO;
            throw new SAXException("success");
        }
        if(qName.equals("ink")) {
            this.result = FileType.INKML;
            throw new SAXException("success");
            
        }
        if(qName.equals("Doc")) {
            if(attributes.getValue("version").equals("1.0")) {
                this.result = FileType.LOGITECH_PEN_V1;
                throw new SAXException("success");
            }
        }
        if(counter > 20) {
            throw new SAXException("Document not recognized");
        }
    }
    
}
