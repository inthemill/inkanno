/**
 * 
 */
package ch.unibe.im2.inkanno.gui.color;

import java.awt.Graphics2D;

import javax.swing.Icon;

import ch.unibe.inkml.InkTraceViewLeaf;
import ch.unibe.inkml.util.TraceViewFilter;

/**
 * This is an interface for classes used to apply differnt colors for the traces
 * of a document drawn on the screen. 
 * @author emanuel
 *
 */
public interface Colorizer {

    /**
     * The ColorizerManager tries to identify the currentColorizer by this method.
     * So this method can be used to specify if this class is responsilbe as colorizer
     * if the string selection maches its specification.
     * 
     * Note that with this mechanic, some parameters can be given to the colorizer.
     * @param selection
     * @return
     */
    boolean isResponsible(String selection);

    /**
     * The actual method for setting the color to the trace
     * @param graphics
     * @param s
     */
    void setColor(Graphics2D graphics, InkTraceViewLeaf s);

    /**
     * @see ColorizerManager.setFilter()
     * @param filter
     */
    void setFilter(TraceViewFilter filter);

    /**
     * Reutns the caption this colorizer is identified by. For dispay in menu
     * @return Caption
     */
	public String getCaption();
}
