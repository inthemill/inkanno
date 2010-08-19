/**
 * 
 */
package ch.unibe.inkml;

/**
 * @author emanuel
 *
 */
public class InkAnnoTraceFormat extends InkTraceFormat {

    /**
     * @param ink
     * @throws InkMLComplianceException 
     */
    public InkAnnoTraceFormat(InkInk ink) throws InkMLComplianceException {
        super(ink,"inkAnnoCanvasFormat");
        try {
            InkChannel channelX = InkChannel.channelFactory(InkChannel.Type.DECIMAL, ink);
            channelX.setName(InkChannel.ChannelName.X);
            channelX.setOrientation(InkChannel.Orientation.P);
            channelX.setFinal();
            addChannel(channelX);
        
            InkChannel channelY = InkChannel.channelFactory(InkChannel.Type.DECIMAL, ink);
            channelY.setName(InkChannel.ChannelName.Y);
            channelY.setOrientation(InkChannel.Orientation.P);
            channelY.setFinal();
            addChannel(channelY);
            
            InkChannel channelT = InkChannel.channelFactory(InkChannel.Type.DECIMAL, ink);
            channelT.setName(InkChannel.ChannelName.T);
            channelT.setOrientation(InkChannel.Orientation.P);
            channelT.setUnits("s");
            channelT.setFinal();
            addChannel(channelT);
            
            InkChannel channelF = InkChannel.channelFactory(InkChannel.Type.INTEGER, ink);
            channelF.setName(InkChannel.ChannelName.F);
            channelF.setOrientation(InkChannel.Orientation.P);
            channelF.setMin("0");
            channelF.setMax("255");
            channelF.setIntermittent(true);
            channelF.setFinal();
            addChannel(channelF);
            setFinal();
        } catch (InkMLComplianceException e) {
            System.err.println("Its a Bug, please fix it, or contact developer");
            e.printStackTrace();
            //Will not happen here, unless it is a bug
        }
    }
}
