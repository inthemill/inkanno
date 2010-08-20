package ch.unibe.im2.inkanno.controller;

import java.awt.event.ActionListener;

public interface ActionSource {
	public void setActionCommand(String command);
	public void addActionListener(ActionListener l);
	public void setEnabled(boolean enabled);
}
