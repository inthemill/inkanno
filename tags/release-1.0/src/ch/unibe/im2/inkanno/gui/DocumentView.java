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

import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.gui.tree.AnnotationTreeModel;

public class DocumentView{
    
    private Document doc;
    
    private AnnotationTreeModel treeModel;
    
    private DocumentMenu menu;
    
    private GUI gui;

    private double widthSliderValue;
    
    public DocumentView(Document doc, GUI gui) {
        this.gui = gui;
        this.doc = doc;
        menu = new DocumentMenu(this, this.gui.getMenu());
        treeModel = new AnnotationTreeModel(doc);
        widthSliderValue = (int) (doc.getMostCommonTraceHeight() / 5.0);
    }
    
    public void activate() {
        this.menu.activate();
    }
    
    public Document getDocument() {
        return doc;
    }

    public AnnotationTreeModel getAnnotationTreeModel() {
        return treeModel;
    }

    /**
     * 
     */
    public double getStrokeWidth() {
        return widthSliderValue;
    }
    
    public void setStrokeWidth(double value) {
        widthSliderValue = value;
    }
}
