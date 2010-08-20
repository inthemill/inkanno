/**
 * 
 */
package ch.unibe.im2.inkanno.gui;

import javax.swing.JOptionPane;

import ch.unibe.eindermu.Messenger.MessageSink;
import ch.unibe.im2.inkanno.Strings;

/**
 * @author emanuel
 *
 */
public class GUIMessenger implements MessageSink {

    @Override
    public void error(String message) {
        JOptionPane.showMessageDialog(null, message, Strings.getString("InkAnno.err_io"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$

    }

    @Override
    public void inform(String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void warn(String message) {
        // TODO Auto-generated method stub

    }

}
