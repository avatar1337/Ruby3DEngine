/**
 * @(#)DrawingBoard.java
 *
 *
 * @author Mikael Murstam
 * @version 1.00 2011/10/15
 */

import java.awt.*;

public class DrawingBoard extends DrawingPanel {

	private double angle;

    public DrawingBoard(int width, int height) {
    	super(width, height);
    	setAntiAliasing(true);
    	setLineThickness(2);
    	angle=0;
    }

    @Override
    public void paint(Graphics2D g2) {
    	clearScreen(Color.black);
		g2.setColor(Color.red);
		int xPos=(int)Math.round(150+Math.cos(angle)*100);
		int yPos=(int)Math.round(150+Math.sin(angle)*100);
		g2.drawOval(xPos,yPos,200,200);
		angle+=0.005f;
		if(angle > 2*Math.PI) angle -= 2*Math.PI;
    }
}