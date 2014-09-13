/**
 * @(#)Surface2D.java
 *
 *
 * @author Mikael Murstam
 * @version 1.00 2011/10/15
 */


import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public abstract class Surface2D extends DrawingPanel {

	private double xMin;
	private double xMax;
	private double yMin;
	private double yMax;
	private double scaleX;
	private double scaleY;
	private double xRange;
	private double yRange;
	private int itsWidth;
	private int itsHeight;
	private double itsUnits;

	private Graphics2D surface;

    public Surface2D(int width, int height) {
    	super(width, height);
    	itsWidth=width;
    	itsHeight=height;
    	xMin=-10;
    	xMax=10;
    	yMin=-10;
    	yMax=10;
    	xRange=xMax-xMin;
    	yRange=yMax-yMin;
    	scaleX=(double)width/xRange;
		scaleY=(double)height/yRange;
		itsUnits=100;
		init();
    }

    public void setOrtho(double xmin, double xmax, double ymin, double ymax) {
		xMin=xmin;
    	xMax=xmax;
    	yMin=ymin;
    	yMax=ymax;
    	xRange=xMax-xMin;
    	yRange=yMax-yMin;
    	scaleX=(double)itsWidth/xRange;
		scaleY=(double)itsHeight/yRange;
		itsUnits=ymax;
    }

    public void setOrthoSquareUnit(double units) {
    	itsUnits=units;
		xMin=-units*((double)itsWidth/(double)itsHeight);
    	xMax=units*((double)itsWidth/(double)itsHeight);
    	yMin=-units;
    	yMax=units;
    	xRange=xMax-xMin;
    	yRange=yMax-yMin;
    	scaleX=(double)itsWidth/xRange;
		scaleY=(double)itsHeight/yRange;
    }

    public void resize() {
    	int newWidth=super.getWidth();
    	int newHeight=super.getHeight();
    	if(itsWidth!=newWidth || itsHeight!=newHeight){
	    	itsWidth=newWidth;
	    	itsHeight=newHeight;
			super.setDimensions(itsWidth,itsHeight);
			setOrthoSquareUnit(itsUnits);
    	}
    }

    public void resize(double units) {
		setDimensions(super.getWidth(),super.getHeight());
		setOrthoSquareUnit(units);
    }

    public void resize(double xmin, double xmax, double ymin, double ymax) {
		setDimensions(super.getWidth(),super.getHeight());
		setOrtho(xmin, xmax, ymin, ymax);
    }

    @Override
    public void paint(Graphics2D g2) {
    	surface=g2;
    	draw();
    }

    public void drawString(String msg, double x, double y, Color c) {
    	setColor(c);
    	surface.drawString(msg, Unit2CoordX(x), Unit2CoordY(y));
    }

    public Graphics2D getSurface() {
    	return surface;
    }

    int DeltaU2PX(double u) {
		return (int)Math.round(u*scaleX);
	}

	int DeltaU2PY(double u) {
		return (int)Math.round(u*scaleY);
	}

    int Unit2CoordX(double x) {
    	return DeltaU2PX(x-xMin);
	}

	int Unit2CoordY(double y) {
		return itsHeight-DeltaU2PY(y-yMin);
	}

	double CoordX2Unit(int px) {
		return ((double)px/scaleX)+xMin;
	}

	double CoordY2Unit(int py) {
		return ((double)(itsHeight-py)/scaleY)+yMin;
	}

	public void setColor(Color c) {
		surface.setColor(c);
	}

	public void drawLine(double x1, double y1, double x2, double y2, Color c) {
		setColor(c);
		surface.drawLine(Unit2CoordX(x1),Unit2CoordY(y1),Unit2CoordX(x2),Unit2CoordY(y2));
	}

	public void drawLine(double x1, double y1, double x2, double y2) {
		surface.drawLine(Unit2CoordX(x1),Unit2CoordY(y1),Unit2CoordX(x2),Unit2CoordY(y2));
	}

	public void drawCircle(double x, double y, double r, Color c) {
		setColor(c);
		surface.drawOval(Unit2CoordX(x-r),Unit2CoordY(y+r),DeltaU2PX(2*r),DeltaU2PY(2*r));
	}

	public double getRangeX() {
		return xRange;
	}

	public double getRangeY() {
		return yRange;
	}
	
	public class Poly2D {
		private Point2D.Double position;
		private ArrayList<Point2D.Double> vertex;
		private Polygon poly;
		
		Poly2D() {
			poly = new Polygon();
			vertex = new ArrayList<Point2D.Double>();
			position = new Point2D.Double(0,0);
		}

		public void addVertex(Point2D.Double v) {
			poly.addPoint(Unit2CoordX(v.x),Unit2CoordY(v.y));
			vertex.add(new Point2D.Double(v.x,v.y));
		}

		public void addVertex(double x, double y) {
			poly.addPoint(Unit2CoordX(x),Unit2CoordY(y));
			vertex.add(new Point2D.Double(x,y));
		}

		public Point2D.Double getPosition() {
			return position;
		}

		public void rotate(double angle) {
			if(angle > 2*Math.PI) angle -= 2*Math.PI;
			for(int i=0; i < vertex.size(); i++) {
				vertex.get(i).setLocation(
					vertex.get(i).x*Math.cos(angle)-vertex.get(i).y*Math.sin(angle),
					vertex.get(i).x*Math.sin(angle)+vertex.get(i).y*Math.cos(angle)
				);
			}
		}

		public ArrayList<Point2D.Double> getVertex() {
			return vertex;
		}

		public void move(double x, double y) {
			position.x+=x;
			position.y+=y;
		}

		public void setPosition(double x, double y) {
			position.x=x;
			position.y=y;
		}

		public void drawPoly() {
			int i;
			for(i=0; i < vertex.size()-1; i++) {
				drawLine(vertex.get(i).x+position.x,vertex.get(i).y+position.y,vertex.get(i+1).x+position.x,vertex.get(i+1).y+position.y);
			}
			drawLine(vertex.get(i).x+position.x,vertex.get(i).y+position.y,vertex.get(0).x+position.x,vertex.get(0).y+position.y);
		}

		public void drawFillPoly() {
			surface.fillPolygon(poly);
		}
		
		public void drawShadePoly(Color c) {
			GradientPaint gp = new GradientPaint((float)poly.getBounds().getLocation().getX(), (float)poly.getBounds().getLocation().getY(), c,
												 (float)poly.getBounds().getLocation().getX(), (float)poly.getBounds().getLocation().getY(), c, false);
			surface.setPaint(gp);
			surface.fill(poly);
		}

		public void drawPath() {
			for(int i=0; i < vertex.size()-1; i++) {
				drawLine(vertex.get(i).x+position.x,vertex.get(i).y+position.y,vertex.get(i+1).x+position.x,vertex.get(i+1).y+position.y);
			}
		}
	}

	public Poly2D createBezier(Point2D.Double P0, Point2D.Double P1, Point2D.Double P2) {
		Poly2D bezier = new Poly2D();
		Point2D.Double point = new Point2D.Double();

		double step=0.001f;

		for(double t=0; t<=1-step;) {
			point.x=(1-t)*(1-t)*P0.x+2*(1-t)*t*P1.x+t*t*P2.x;
			point.y=(1-t)*(1-t)*P0.y+2*(1-t)*t*P1.y+t*t*P2.y;
			bezier.addVertex(point);
			t+=step;
			point.x=(1-t)*(1-t)*P0.x+2*(1-t)*t*P1.x+t*t*P2.x;
			point.y=(1-t)*(1-t)*P0.y+2*(1-t)*t*P1.y+t*t*P2.y;
			bezier.addVertex(point);
		}

		return bezier;
	}

	public Poly2D createQuad(Point2D.Double P0, Point2D.Double P1, Point2D.Double P2, Point2D.Double P3) {
		Poly2D quad = new Poly2D();
		quad.addVertex(P0);
		quad.addVertex(P1);
		quad.addVertex(P2);
		quad.addVertex(P3);
		return quad;
	}

	public abstract void init();
        public abstract void draw();
}