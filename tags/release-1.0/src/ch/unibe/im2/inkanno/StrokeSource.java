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

/*
public class StrokeSource implements Iterable<InkTrace>{
    
    private List<InkTrace> strokes = new LinkedList<InkTrace>();
    
    private InkTraceGroup root;
    
    private Document document;
    
    public StrokeSource(Document d) {
        this.document = d;
    }
    
    public void addAll(Collection<InkTrace> c) {
        this.strokes.addAll(c);
    }
    
    public void setRoot(InkTraceGroup root) {
        this.root = root;
        this.root.setDocument(this.document);
        this.root.releaseEvents();
    }
    
    public Iterator<InkTrace> iterator() {
        return this.strokes.iterator();
    }
    
    public InkTraceGroup getRoot() {
        return this.root;
    }
    
    public double getStrokeCount() {
        return this.strokes.size();
    }
    
    public void add(InkTrace stroke) {
        this.strokes.add(stroke);
    }
    
    public List<? extends InkTraceLike> getStrokes() {
        return this.strokes;
    }
}
*/