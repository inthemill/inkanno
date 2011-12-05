package ch.unibe.im2.inkanno.gui.color;

import java.awt.Graphics2D;
import java.lang.ref.WeakReference;

import ch.unibe.im2.inkanno.Document;
import ch.unibe.im2.inkanno.gui.GUI;
import ch.unibe.inkml.InkInk;
import ch.unibe.inkml.InkTraceViewLeaf;

public abstract class AbstractColorizer implements Colorizer {


	private WeakReference<InkInk> ink;
	private int mutex;
	
	public abstract void initByInk(InkInk ink);
	
	@Override
	public final void setColor(Graphics2D graphics, InkTraceViewLeaf leaf) {
		/*if(doInitialization(leaf.getInk())){
			this.initialize(leaf.getInk());
		}*/
        internalSetColor(graphics, leaf);
	}


	private synchronized boolean doInitialization(InkInk ink) {
		switch (mutex) {
		case 0:
			mutex = (this.ink == null || this.ink.get() != ink)?1:0;
			return mutex == 1;
		case 1:
			return false;
		default:
			return false;
		}
	}
	
	private synchronized void initialize(final InkInk ink) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				AbstractColorizer.this.ink = null;
				AbstractColorizer.this.ink = new WeakReference<InkInk>(ink);
				initByInk(ink);
				mutex = 0;
				GUI.getInstance().repaint();
			}
		});
		t.start();
	}
	
	public abstract void internalSetColor(Graphics2D graphics, InkTraceViewLeaf s);

	@Override
	public void initialize(Document subject) {
		initialize(subject.getInk());
	}
	
}
