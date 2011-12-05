package ch.unibe.im2.inkanno.gui.color;

import java.awt.event.ActionListener;

public interface ColorizerCallback {

	public String getLabel();

	public boolean isSelected();

	public Colorizer getColorizerForSelection();

}
