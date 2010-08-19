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

package ch.unibe.im2.inkanno.gui;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;

import ch.unibe.im2.inkanno.InkAnno;

@SuppressWarnings("serial")
public class MyFileChooser extends JFileChooser{
    private boolean directoryForced = false; 
	
	public void setCurrentDirectory(File dir){
		super.setCurrentDirectory(dir);
		directoryForced = true;
	}
	
    public int showDialog(Component parent, String approveButtonText) {
        if(!directoryForced && !InkAnno.config().get("file_chooser_dir").isEmpty()) {
            this.setCurrentDirectory(new File(InkAnno.config().get("file_chooser_dir")));
        }
        int result = super.showDialog(parent, approveButtonText);
        InkAnno.config().set("file_chooser_dir", this.getCurrentDirectory().getAbsolutePath());
        try {
        	InkAnno.config().save();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
