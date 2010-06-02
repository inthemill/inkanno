package ch.unibe.im2.inkanno.gui.color;

import java.awt.Color;

public class RandomColorGenerator{
	private int colorI; 
	public Color getNewColor() {
		int x = 1+(colorI++)%19;
		int r = 0;
        int g = 0;
        int b = 0;
        for(int i=0;i<8;i++) {
            r = (r<<1) | (x&1); x >>= 1;
            g = (g<<1) | (x&1); x >>= 1;
            b = (b<<1) | (x&1); x >>= 1;
        }
        return new Color(r,g,b);

	}
}
