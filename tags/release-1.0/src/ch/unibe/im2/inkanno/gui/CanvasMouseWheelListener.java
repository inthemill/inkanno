package ch.unibe.im2.inkanno.gui;

import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

public class CanvasMouseWheelListener implements MouseWheelListener {
    private TraceCanvas tc;
    private JScrollPane scrollPane;
    private Point newViewPosition;
    
    public CanvasMouseWheelListener(TraceCanvas canvas,JScrollPane pane) {
        tc = canvas;
        scrollPane = pane;
        //set componentListener which will apply the new viewport position
        //after scroll-zooming. This must be applied after the accutal resizing
        //as taken place therefor it is done by such a listener.
        tc.getParent().addComponentListener(new ComponentAdapter() {
            
            
            public void componentResized(ComponentEvent e) {
                if(newViewPosition != null){
                    scrollPane.getViewport().setViewPosition(newViewPosition);
                    newViewPosition = null;
                }
            }
        });
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        JScrollPane pane = (JScrollPane) e.getSource();
        JViewport vp = pane.getViewport();
        int vph = vp.getHeight(), vpw = vp.getWidth();
        int ch = tc.getHeight(), cw = tc.getWidth();
        Point p = vp.getViewPosition();
        if(newViewPosition != null){
            p = newViewPosition;
        }
        
        
        if(e.isControlDown()){
            zooming(e, vpw, vph, cw, ch, vp,p, pane);
        }else{
            if(!e.isShiftDown()){
                if(vph < ch){
                    p.translate(0, e.getWheelRotation()* 50 );
                    if(p.y < 0){
                        p.y = 0;
                    }
                    if(p.y + vph > ch){
                        p.y = -vph + ch;
                    }
                }
            }else{
                if(vpw < cw){
                    p.translate(e.getWheelRotation()* 50, 0);
                    if(p.x < 0){
                        p.x = 0;
                    }
                    
                    if(p.x + vpw > cw){
                        p.x = -vpw + cw;
                    }
                }
            }
            vp.setViewPosition(p);    
        }
        e.consume();
    }
    
    private synchronized void zooming(MouseWheelEvent e,int viewPortWidth, int viewPortHeight, int canvasWidth, int canvasHeight, JViewport viewPort,Point viewPortLocation, JScrollPane pane){
        int xOnVP = e.getX();
        int yOnVP = e.getY();
        int xOnV = (xOnVP + viewPortLocation.x - (Math.max(0,viewPortWidth-canvasWidth)/2));
        int yOnV = (yOnVP + viewPortLocation.y - (Math.max(0,viewPortHeight-canvasHeight)/2));
        
        
        //double xpartOnV = xOnV/(double)canvasWidth;
        //double ypartOnV = yOnV/(double)canvasHeight;
        double f = tc.zoom(e.getWheelRotation() < 0);
        
        double npx = Math.max(0,(f * xOnV) - xOnVP);
        double npy = Math.max(0,(f * yOnV) - yOnVP);
        //Set view Position for application after resizing the canvas
        //which will be done by the component listener defined in the constructor
        newViewPosition = new Point((int)npx,(int)npy);
        
        pane.revalidate();
        pane.repaint(0);
        //set view Position befor resizing for a little smoother application
        viewPort.setViewPosition(newViewPosition);
        
        //debug
        //System.out.format("on VP: %dx%d; on Canvas: %dx%d; VP pos: %dx%d; new VP pos: %dx%d; new VP acctually: %dx%d\n",xOnVP,yOnVP,xOnV,yOnV,viewPortLocation.x,viewPortLocation.y,newViewPosition.x,newViewPosition.y,viewPort.getViewPosition().x,viewPort.getViewPosition().y);
    }

}
