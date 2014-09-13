/**
 * @(#)DrawingPanel.java
 *
 *
 * @author Mikael Murstam
 * @version 1.00 2011/10/15
 */

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public abstract class DrawingPanel extends JPanel {

	//private BufferedImage bi;
	private VolatileImage vi;
	private Graphics2D backBuffer;
	private Graphics2D screen;
	private boolean antiAliasing;
	private int width;
	private int height;
	private boolean willSave;
	private String name;

    public DrawingPanel(int width, int height) {
    	super();
    	this.width=width;
    	this.height=height;
    	setPreferredSize(new Dimension(width,height));
    	antiAliasing=false;
    	willSave=false;
    }

    public void setScreen(Graphics2D g2) {
    	screen=g2;
    }

    public void setDimensions(int w, int h) {
    	width=w;
		height=h;
		do{ vi = createVolatileImage(width, height); }while(vi==null);
        vi.setAccelerationPriority(1.0f);
        backBuffer = vi.createGraphics(); // Double buffering!
    }

    private void createImage(){
        if(vi == null || vi.validate(getGraphicsConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE){
            do{ vi = createVolatileImage(width, height); }while(vi==null);
        	vi.setAccelerationPriority(1.0f);
        	backBuffer = vi.createGraphics(); // Double buffering!
        }
    }

    @Override
    public void show() {
    	screen.drawImage(vi,0,0,null);
    }

    public void saveImage(String name) {
    	this.name=name;
    	willSave=true;
    }

    private void save() {
    	if(willSave) {
	    try {
                File outputfile = new File(name);
                ImageIO.write(vi.getSnapshot(), "png", outputfile);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Couldn't save "+name);
            }
    	}
    	willSave=false;
    }

    public boolean isAccelerated() {
    	createImage();
    	return vi.getCapabilities().isAccelerated();
    }

    public void setLineThickness(int t) {
    	backBuffer.setStroke(new BasicStroke(t));
    }

    public void setAntiAliasing(boolean b) {
    	antiAliasing=b;
    }

    public void clearScreen(Color c) {
    	backBuffer.setColor(c);
		backBuffer.fillRect(0,0,getWidth(),getHeight());
    }

    @Override
	protected void paintComponent(Graphics g) { //paintComponent
    	super.paintComponent(g);
    	setScreen((Graphics2D)g);
    	createImage();
  
    	if(antiAliasing) {
    	 	backBuffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    		backBuffer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    	}
    	else {
    		backBuffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    		backBuffer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    	}

	backBuffer.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
	backBuffer.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
	backBuffer.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
	backBuffer.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
	    
    	paint(backBuffer);
    	show();
    	save();
    	repaint();
    }

    public abstract void paint(Graphics2D g2);
}