/**
 * 
 */
package ch.unibe.inkml;

/**
 * @author emanuel
 *
 */
public class InkAnnoCanvas extends InkCanvas {
    public InkAnnoCanvas(InkInk ink) throws InkMLComplianceException {
        super(ink,"inkAnnoCanvas");
        setInkTraceFormat(new InkAnnoTraceFormat(ink));
    }
}
