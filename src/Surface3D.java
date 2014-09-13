/**
 * @(#)Surface3D.java
 *
 *
 * @author Mikael Murstam
 * @version 1.00 2011/10/16
 */

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public abstract class Surface3D extends Surface2D {

	private ArrayList<Object3D> surfaceModels;
	private ArrayList<Poly3D> surfacePolygons;

	private Point3D cameraPos;
	private double camRotPhi;
	private double camRotPsi;
	private double camRotTheta;
	private double [] SIN;
	private double [] COS;

	private int numberOfPolys=0;
	private int numberOfModels=0;

	private boolean outline;

	class Point3D extends MathVector implements Cloneable {
		private double lastPhi;
		private double lastPsi;
		private double lastTheta;

		Point3D(double x0, double y0, double z0){
			oldX=x=x0;
			oldY=y=y0;
			oldZ=z=z0;

			lastPhi=0;
			lastPsi=0;
			lastTheta=0;
		}

		Point3D(){
			oldX=x=0;
			oldY=y=0;
			oldZ=z=0;

			lastPhi=0;
			lastPsi=0;
			lastTheta=0;
		}

            @Override
		public Point3D clone() throws CloneNotSupportedException {
			return (Point3D)super.clone();
		}

		public void setLocation(double x0, double y0, double z0) {
			oldX=x=x0;
			oldY=y=y0;
			oldZ=z=z0;
		}

		public void setLocation(Point3D p) {
			oldX=x=p.x;
			oldY=y=p.y;
			oldZ=z=p.z;
		}

		public void move(double x0, double y0, double z0) {
			oldX=x+=x0;
			oldY=y+=y0;
			oldZ=z+=z0;
		}

		/*public void rotateX(double phi) {
			if(phi > 2*Math.PI) phi -= 2*Math.PI;
			double deltaPhi = phi-lastPhi;
			setLocation(x,z*sin(deltaPhi)+y*cos(deltaPhi),z*cos(deltaPhi)-y*sin(deltaPhi));
			lastPhi=phi;
		}

		public void rotateY(double psi) {
			if(psi > 2*Math.PI) psi -= 2*Math.PI;
			double deltaPsi = psi-lastPsi;
			setLocation(x*cos(deltaPsi)+z*sin(deltaPsi),y,-x*sin(deltaPsi)+z*cos(deltaPsi));
			lastPsi=psi;
		}

		public void rotateZ(double theta) {
			if(theta > 2*Math.PI) theta -= 2*Math.PI;
			double deltaTheta = theta-lastTheta;
			setLocation(x*cos(deltaTheta)-y*sin(deltaTheta),x*sin(deltaTheta)+y*cos(deltaTheta),z);
			lastTheta=theta;
		}*/

		public void rotateX(double phi) {
			if(phi > 2*Math.PI) phi -= 2*Math.PI;
			setLocation(x,z*sin(phi)+y*cos(phi),z*cos(phi)-y*sin(phi));
		}

		public void rotateY(double psi) {
			if(psi > 2*Math.PI) psi -= 2*Math.PI;
			setLocation(x*cos(psi)+z*sin(psi),y,-x*sin(psi)+z*cos(psi));
		}

		public void rotateZ(double theta) {
			if(theta > 2*Math.PI) theta -= 2*Math.PI;
			setLocation(x*cos(theta)-y*sin(theta),x*sin(theta)+y*cos(theta),z);
		}

		public void rotate(double phi, double psi, double theta) {
			rotateX(phi);
			rotateY(psi);
			rotateZ(theta);
		}

		public void rotateEgo(double phi, double psi, double theta) {
			//oldX=x; oldY=y; oldZ=z;
			phi=-phi;
			setLocation((y * cos(phi) - (z * cos(psi) - x * sin(psi)) * sin(phi)) * sin(theta) + (z * sin(psi) + x * cos(psi)) * cos(theta),
						(y * cos(phi) - (z * cos(psi) - x * sin(psi)) * sin(phi)) * cos(theta) - (z * sin(psi) + x * cos(psi)) * sin(theta),
						(y * sin(phi) + (z * cos(psi) - x * sin(psi)) * cos(phi)));
			//x=oldX; y=oldY; z=oldZ;
		}


//		public Point3D _rotate(double phi, double psi, double theta) {
//			Point3D p = new Point3D(x,y,z);
//			p.rotateX(phi);
//			p.rotateY(psi);
//			p.rotateZ(theta);
//			return p;
//		}

		public double getDistanceTo(Point3D p) {
			return Math.sqrt((p.x-x)*(p.x-x)+(p.y-y)*(p.y-y)+(p.z-z)*(p.z-z));
		}

		public double getDistanceTo(double x0, double y0, double z0) {
			return Math.sqrt((x-x0)*(x-x0)+(y-y0)*(y-y0)+(z-z0)*(z-z0));
		}

		public double getDistanceToOrigo() {
			return Math.sqrt(x*x+y*y+z*z);
		}

		private double oldX;
		private double oldY;
		private double oldZ;
	}

    public Surface3D(int width, int height) {
    	super(width, height);
    	surfaceModels = new ArrayList<Object3D>();
		surfacePolygons = new ArrayList<Poly3D>();
		SIN = new double[6283186];
		COS = new double[6283186];
		for(int i=0; i < 6283186; i++) {
			double angle = i/1000000.0;
			SIN[i] = Math.sin(angle);
			COS[i] = Math.cos(angle);
		}
		//light=false;
		outline=false;
		initScene();
    }

    public double sin(double angle) {
    	while(angle > 2*Math.PI) {
            angle-=2*Math.PI;
        }
    	while(angle < 0) {
            angle+=2*Math.PI;
        }
    	int index = (int)(angle*1000000.0);
    	return SIN[index];
    }

    public double cos(double angle) {
    	while(angle > 2*Math.PI) angle-=2*Math.PI;
    	while(angle < 0) angle+=2*Math.PI;
    	int index = (int)(angle*1000000.0);
    	return COS[index];
    }

    public void setOutline(boolean b) {
    	outline=b;
    }

    public void project(Point3D p3D, Point2D.Double p2D) {
    	if(p2D == null) p2D = new Point2D.Double();
    	//p3D.z=200; //no perspective
    	p2D.x=(p3D.z>1.0f)? getRangeY()*p3D.x/p3D.z : getRangeY()*p3D.x;
    	p2D.y=(p3D.z>1.0f)? getRangeY()*p3D.y/p3D.z : getRangeY()*p3D.y;
    }

    public int getNumberOfPolygons() {
    	return numberOfPolys;
    }

    public int getNumberOfModels() {
    	return numberOfModels;
    }

    public class Plane3D {
    	private Point3D p0;
    	private MathVector n;

    	public Plane3D(Point3D p0, Point3D n) {
    		this.p0=p0;
    		this.n=n;
    	}

    	public Plane3D() {
    		p0=new Point3D();
    		n=new MathVector();
    	}

    	public void setPoint(Point3D p0) {
    		this.p0=p0;
    	}

    	public Point3D getPoint() {
    		return p0;
    	}

    	public void calcNormal(MathVector v0, MathVector v1) {
    		n=MathVector.getNormal(v0,v1);
    	}

    	public MathVector getNormal() {
    		return n;
    	}
    	
    	public void setNormal(MathVector v) {
    		n=v;
    	}
    }

    public class Poly3D extends Plane3D implements Cloneable {
    	private Point3D position;
    	private ArrayList<Point3D> vertex;
    	private Color itsColor;
    	private boolean visible;

    	Poly3D(Color c) {
			vertex = new ArrayList<Point3D>();
			position = new Point3D(0,0,0);
			//normalCalculated=false;
			visible=true;
			itsColor=c;
		}

		Poly3D() {
			vertex = new ArrayList<Point3D>();
			position = new Point3D(0,0,0);
			//normalCalculated=false;
			visible=true;
			itsColor=new Color(240,240,240);
		}

		/** We want to clone this in order to manipulate the polygons in for instance polygon clipping,
		 *	and then later restore it to its original shape. I.e only manipulate the clone.
		 */
        @Override
		public Poly3D clone() throws CloneNotSupportedException {
			Poly3D PolyClone = (Poly3D)super.clone();
			try{
				ArrayList<Point3D> newVertex = new ArrayList<Point3D>(); // Creating a new vector because it now contains a clone of references...
				for(Point3D p:vertex) newVertex.add(p.clone());
				PolyClone.setVertex(newVertex);
			}catch(CloneNotSupportedException e){}
			return PolyClone;
		}

		public void empty() {
			vertex.clear();
		}

		public boolean isVisible() {
			return visible;
		}

		public void calcNormal() {
			if(vertex.size()<3) return;
			//Point3D c = getCentroid();
			//setPoint(c);
			//c.decrease(position);
			MathVector v0 = new MathVector(vertex.get(0).minus(vertex.get(1)));
			MathVector v1 = new MathVector(vertex.get(1).minus(vertex.get(2)));
			/*int index=0;
			for(int i=1; i < vertex.size(); i++) {
				if(!vertex.get(i).equals(vertex.get(0))){
					index=i;
					break;
				}
			}*/
			
			super.calcNormal(v0,v1);
    	}

    	public double getCosAngleToView() {
    		Point3D p = getCentroid();
    		p.normalize();
    		MathVector normal=getNormal();
    		return normal.dot(p);
    	}

    	public double getCosAngleToZ() {
    		MathVector e_z = new MathVector(0,0,1);
    		MathVector normal=getNormal();
    		return normal.dot(e_z);
    	}

		public void setVisible(boolean b) {
			visible=b;
		}

		public void addVertex(Point3D v) {
			vertex.add(new Point3D(v.x,v.y,v.z));
		}

		public void addVertex(double x, double y, double z) {
			vertex.add(new Point3D(x,y,z));
			//normal.setVector(x,y,z);
		}

		public Point3D getCentroid() {
			double x=0;
			double y=0;
			double z=0;
			for(int i=0; i < vertex.size(); i++) {
				x+=vertex.get(i).x;
				y+=vertex.get(i).y;
				z+=vertex.get(i).z;
			}
			x/=vertex.size();
			y/=vertex.size();
			z/=vertex.size();

			x+=position.x;
			y+=position.y;
			z+=position.z;

			return new Point3D(x,y,z);
		}

		public ArrayList<Point3D> getVertex() {
			return vertex;
		}

		public void setVertex(ArrayList<Point3D> newVertex) {
			vertex=newVertex;
		}

		public double meanZ() {
			double z=0;
			for(int i=0; i < vertex.size(); i++) {
				z+=vertex.get(i).z;
			}
			return (z/(double)vertex.size())+position.z;
		}

		public double distanceCamera() {
			Point3D centroid = getCentroid();
			return centroid.getDistanceToOrigo(); // Will replace with getDistanceTo(camera);
		}

		public double distanceMaxDepth() {
			double dist=0, len=0;
			for(Point3D p : vertex) {
				len=p.plus(position).length();
				if(len > dist) {
					dist=len;
				}
			}
			return dist;
		}

		/**
         *
         * @return
         */
                public double getMaxDepth() {
			double maxZ=0;
			for(Point3D p : vertex)
				if((p.z+position.z) > maxZ) maxZ=p.z+position.z;
			return maxZ;
		}

		public double getMinDepth() {
			double minZ=vertex.get(0).z+position.z;
			for(Point3D p : vertex)
				if((p.z+position.z) < minZ) minZ=p.z+position.z;
			return minZ;
		}
		
		public double getMaxX() {
			double maxX=0;
			for(Point3D p : vertex)
				if((p.x+position.x) > maxX) maxX=p.x+position.x;
			return maxX;
		}
		
		public double getMinX() {
			double minX=vertex.get(0).x+position.x;
			for(Point3D p : vertex)
				if((p.x+position.x) < minX) minX=p.x+position.x;
			return minX;
		}
				
		public double getMaxY() {
			double maxY=0;
			for(Point3D p : vertex)
				if((p.y+position.y) > maxY) maxY=p.y+position.y;
			return maxY;
		}
		
		public double getMinY() {
			double minY=vertex.get(0).y+position.y;
			for(Point3D p : vertex)
				if((p.y+position.y) < minY) minY=p.y+position.y;
			return minY;
		}

		public void setPolyColor(Color c) {
			itsColor=c;
		}

		public Color getPolyColor() {
			return itsColor;
		}

		public Point3D getPosition() {
			return position;
		}

		public void setPosition(double x, double y, double z) {
			position.x=x;
			position.y=y;
			position.z=z;
		}

		public void setPosition(Point3D p) {
			position.x=p.x;
			position.y=p.y;
			position.z=p.z;
		}

		public void shiftPosition(double x, double y, double z) {
			for(int i=0; i < vertex.size(); i++) {
				vertex.get(i).x+=x;
				vertex.get(i).y+=y;
				vertex.get(i).z+=z;
			}
		}

		public void shiftPosition(Point3D p) {
			for(int i=0; i < vertex.size(); i++) {
				vertex.get(i).x+=p.x;
				vertex.get(i).y+=p.y;
				vertex.get(i).z+=p.z;
			}
		}

		public void move(double x, double y, double z) {
			position.x+=x;
			position.y+=y;
			position.z+=z;
		}

    	public void rotateX(double phi) {
			for(int i=0; i < vertex.size(); i++)
				vertex.get(i).rotateX(phi);
		}

		public void rotateY(double psi) {
			for(int i=0; i < vertex.size(); i++)
				vertex.get(i).rotateY(psi);
		}

		public void rotateZ(double theta) {
			for(int i=0; i < vertex.size(); i++)
				vertex.get(i).rotateZ(theta);
		}

		public void rotate(double phi, double psi, double theta) {
			for(int i=0; i < vertex.size(); i++)
				vertex.get(i).rotate(phi, psi, theta);
		}

		public void rotateEgo(double phi, double psi, double theta) {
			for(int i=0; i < vertex.size(); i++)
				vertex.get(i).rotateEgo(phi, psi, theta);
		}

		public void drawPoly() {
			Poly2D projection = new Poly2D();
			Point3D p3D = new Point3D();
			Point2D.Double p2D = new Point2D.Double();
			for(int i=0; i<vertex.size(); i++){
				p3D.setLocation(vertex.get(i).x+position.x,vertex.get(i).y+position.y,vertex.get(i).z+position.z);
				project(p3D,p2D);
				projection.addVertex(p2D.x,p2D.y);
			}

			//Color c = fog(itsColor,distanceCamera());
			Color c=itsColor;
			setColor(c);
			projection.drawFillPoly();
			//projection.drawShadePoly(c);
			if(outline) {
				//setColor(new Color(c.getRed()/2,c.getGreen()/2,c.getBlue()/2, c.getAlpha()));
				setColor(new Color(0,0,0));
				if(getCosAngleToView() < 0.2) projection.drawPoly();
				//projection.drawPoly();
			}
		}
    }

    public class Poly3DComparator implements Comparator {
        @Override
    	public int compare(Object poly1, Object poly2) {
	   	    double z1 = ((Poly3D)poly1).distanceCamera(); //distanceMaxDepth()
		    double z2 = ((Poly3D)poly2).distanceCamera();

		    if(z1 < z2)
		        return 1;
		    else if(z1 > z2)
		        return -1;
		    else
		        return 0;
	    }
    }

    public class PolyNormalComparator implements Comparator {
        @Override
    	public int compare(Object poly1, Object poly2) {
	   	    double a = Math.abs(((Poly3D)poly1).getCosAngleToView());
		    double b = Math.abs(((Poly3D)poly2).getCosAngleToView());

		    if(a > b)
		        return 1;
		    else if(a < b)
		        return -1;
		    else
		        return 0;
	    }
    }

	public Color fog(Color c, double distance) {
		int r=c.getRed(); int g=c.getGreen(); int b=c.getBlue(); int a=c.getAlpha();
		double alpha = (double)a;//-(Math.pow(distance,2)*0.0005);
		if(alpha > 255) alpha=255;
		if(alpha < 0) alpha=0;
		double shadeR = (double)r-(Math.pow(distance,2)*0.01); //(Math.pow(distance,2)*0.0005)
		if(shadeR > 255) shadeR=255;
		if(shadeR < 0) shadeR=0;
		double shadeG = (double)g-(Math.pow(distance,2)*0.01);
		if(shadeG > 255) shadeG=255;
		if(shadeG < 0) shadeG=0;
		double shadeB = (double)b-(Math.pow(distance,2)*0.01);
		if(shadeB > 255) shadeB=255;
		if(shadeB < 0) shadeB=0;
		Color shade = new Color((int)Math.round(shadeR),(int)Math.round(shadeG),(int)Math.round(shadeB),(int)Math.round(alpha));
		return shade;
	}

	public double fog(double distance) {
		double shade = (double)1-(Math.pow(distance,1.6f)*0.0002); //(Math.pow(distance,2)*0.0005)
		if(shade > 1) shade=1;
		if(shade < 0) shade=0;
		return shade;
	}

/////////////////////////////////////////////////////

    public class Object3D implements Cloneable {
    	private ArrayList<Poly3D> polygons;
    	private Color itsColor;
    	private double contrast;
    	private double brightness;
    	private ArrayList<Object3D> mesh;
    	private Point3D position;
    	private boolean light;

    	Object3D(Color c) {
    		polygons = new ArrayList<Poly3D>();
    		mesh = new ArrayList<Object3D>();
    		position = new Point3D();
    		itsColor=c;
    		contrast=0.9f;
    		brightness=1;
    		light=true;
    	}

    	Object3D() {
    		polygons = new ArrayList<Poly3D>();
    		mesh = new ArrayList<Object3D>();
    		position = new Point3D();
    		itsColor=new Color(240,240,240);
    		contrast=0.9f;
    		brightness=1;
    		light=true;
    	}

        @Override
    	public Object3D clone() throws CloneNotSupportedException {
			Object3D ObjectClone = (Object3D)super.clone();
			try{
				ArrayList<Object3D> newMesh = new ArrayList<Object3D>(); // Creating a new vector because it now contains a clone of references...
				for(Object3D m:mesh) newMesh.add(m.clone());
				ObjectClone.setMesh(newMesh);

				ArrayList<Poly3D> newPolygons = new ArrayList<Poly3D>(); // Creating a new vector because it now contains a clone of references...
				for(Poly3D poly:polygons) newPolygons.add(poly.clone());
				ObjectClone.setPolygons(newPolygons);
			}catch(CloneNotSupportedException e){}
			return ObjectClone;
    	}

    	public Point3D getCentroid() {
			double x=0;
			double y=0;
			double z=0;
			for(Poly3D poly : polygons) {
				x+=poly.getCentroid().x;
				y+=poly.getCentroid().y;
				z+=poly.getCentroid().z;
			}
			x/=polygons.size();
			y/=polygons.size();
			z/=polygons.size();

			return new Point3D(x,y,z);
		}

    	public ArrayList<Object3D> getMesh() {
    		return mesh;
    	}

    	public void setMesh(ArrayList<Object3D> m) {
    		mesh=m;
    	}

    	public ArrayList<Poly3D> getPolygons() {
    		return polygons;
    	}

    	public void setPolygons(ArrayList<Poly3D> p) {
    		polygons=p;
    	}

    	public void addPolygon(Poly3D p) {
    		polygons.add(p);
    	}

    	public Color getObjectColor() {
    		return itsColor;
    	}

    	public void setObjectColor(Color c) {
    		itsColor=c;
    	}

   	    public double getColorContrast() {
	    	return contrast;
	    }

	    public double getColorBrightness() {
	    	return brightness;
	    }

	    public void setColorContrast(double c) {
	    	if(c>1) c=1; if(c<0) c=0;
	    	contrast=c;
	    	for(Object3D m:mesh) m.setColorContrast(c);
	    }

	    public void setColorBrightness(double b) {
	    	if(b>1) b=1; if(b<0) b=0;
	    	brightness=b;
	    	for(Object3D m:mesh) m.setColorBrightness(b);
	    }

	    public void setLight(boolean b) {
	    	light=b;
	    	for(Object3D m:mesh) m.setLight(b);
	    }

	    public boolean isLight() {
	    	return light;
	    }

	    public void move(double x, double y, double z) {
	    	for(int i=0; i < polygons.size(); i++) {
				polygons.get(i).move(x,y,z);
			}
			for(int j=0; j < mesh.size(); j++) {
				mesh.get(j).move(x,y,z);
			}
		}

		public void setPosition(double x, double y, double z) {
			position.setLocation(x,y,z);

			for(int i=0; i < polygons.size(); i++) {
				polygons.get(i).setPosition(x,y,z);
			}
			for(int j=0; j < mesh.size(); j++) {
				mesh.get(j).setPosition(x,y,z);
			}
		}

		public void setPosition(Point3D pos) {
			position.setLocation(pos);

			for(int i=0; i < polygons.size(); i++) {
				polygons.get(i).setPosition(pos);
			}
			for(int j=0; j < mesh.size(); j++) {
				mesh.get(j).setPosition(pos);
			}
		}

		public Point3D getPosition() {
			return position;
		}

		public void shiftPosition(double x, double y, double z) {
			for(int i=0; i < polygons.size(); i++) {
				polygons.get(i).shiftPosition(x,y,z);
			}
			for(int j=0; j < mesh.size(); j++) {
				mesh.get(j).shiftPosition(x,y,z);
			}
		}

		public void shiftPosition(Point3D p) {
			for(int i=0; i < polygons.size(); i++) {
				polygons.get(i).shiftPosition(p.x,p.y,p.z);
			}
			for(int j=0; j < mesh.size(); j++) {
				mesh.get(j).shiftPosition(p.x,p.y,p.z);
			}
		}

		public void rotateX(double phi) {
			for(int i=0; i < polygons.size(); i++) {
				polygons.get(i).rotateX(phi);
			}
			for(int j=0; j < mesh.size(); j++) {
				mesh.get(j).rotateX(phi);
			}
		}

		public void rotateY(double psi) {
			for(int i=0; i < polygons.size(); i++) {
				polygons.get(i).rotateY(psi);
			}
			for(int j=0; j < mesh.size(); j++) {
				mesh.get(j).rotateY(psi);
			}
		}

		public void rotateZ(double theta) {
			for(int i=0; i < polygons.size(); i++) {
				polygons.get(i).rotateZ(theta);
			}
			for(int j=0; j < mesh.size(); j++) {
				mesh.get(j).rotateZ(theta);
			}
		}

		public void rotate(double phi, double psi, double theta) {
			for(int i=0; i < polygons.size(); i++) {
				polygons.get(i).rotate(phi, psi, theta);
			}
			for(int j=0; j < mesh.size(); j++) {
				mesh.get(j).rotate(phi, psi, theta);
			}
		}

		public void rotateEgo(double phi, double psi, double theta) {
			for(int i=0; i < polygons.size(); i++) {
				polygons.get(i).rotateEgo(phi, psi, theta);
			}
			for(int j=0; j < mesh.size(); j++) {
				mesh.get(j).rotateEgo(phi, psi, theta);
			}
		}

		public void addMesh(Object3D m) {
			mesh.add(m);
		}

		public void drawObject() {
			Point3D centroid=getCentroid();
			double distance = centroid.getDistanceToOrigo();
			//if(distance < 1 || distance > 700) return;
	    	try{surfaceModels.add(clone());} catch(CloneNotSupportedException e) {}
	    	for(int j=0; j < mesh.size(); j++) {
				mesh.get(j).drawObject();
			}
		}
    }

//	public void rotateX(double phi) {
//		if(phi > 2*Math.PI) phi -= 2*Math.PI;
//		for(int k=0; k < surfaceModels.size(); k++) {
//			for(int j=0; j < surfaceModels.get(k).getPolygons().size(); j++) {
//				for(int i=0; i < surfaceModels.get(k).getPolygons().get(j).getVertex().size(); i++) {
//					Point3D p;
//					p = surfaceModels.get(k).getPolygons().get(j).getVertex().get(i);
//					surfaceModels.get(k).getPolygons().get(j).getVertex().get(i).setLocation(p.x,p.z*sin(phi)+p.y*cos(phi),p.z*cos(phi)-p.y*sin(phi));
//				}
//			}
//		}
//	}
//
//	public void rotateY(double psi) {
//		if(psi > 2*Math.PI) psi -= 2*Math.PI;
//		for(int k=0; k < surfaceModels.size(); k++) {
//			for(int j=0; j < surfaceModels.get(k).getPolygons().size(); j++) {
//				for(int i=0; i < surfaceModels.get(k).getPolygons().get(j).getVertex().size(); i++) {
//					double x=surfaceModels.get(k).getPolygons().get(j).getVertex().get(i).x;
//					double y=surfaceModels.get(k).getPolygons().get(j).getVertex().get(i).y;
//					double z=surfaceModels.get(k).getPolygons().get(j).getVertex().get(i).z;
//					surfaceModels.get(k).getPolygons().get(j).getVertex().get(i).setLocation(x*cos(psi)+z*sin(psi),y,-x*sin(psi)+z*cos(psi));
//				}
//			}
//		}
//	}
//
//	public void rotateZ(double theta) {
//		if(theta > 2*Math.PI) theta -= 2*Math.PI;
//		for(int k=0; k < surfaceModels.size(); k++) {
//			for(int j=0; j < surfaceModels.get(k).getPolygons().size(); j++) {
//				for(int i=0; i < surfaceModels.get(k).getPolygons().get(j).getVertex().size(); i++) {
//					double x=surfaceModels.get(k).getPolygons().get(j).getVertex().get(i).x;
//					double y=surfaceModels.get(k).getPolygons().get(j).getVertex().get(i).y;
//					double z=surfaceModels.get(k).getPolygons().get(j).getVertex().get(i).z;
//					surfaceModels.get(k).getPolygons().get(j).getVertex().get(i).setLocation(x*cos(theta)-y*sin(theta),x*sin(theta)+y*cos(theta),z);
//				}
//			}
//		}
//	}

//	private void rotate() {
//		if(camRotPhi > 2*Math.PI) camRotPhi -= 2*Math.PI;
//		if(camRotPsi > 2*Math.PI) camRotPsi -= 2*Math.PI;
//		if(camRotTheta > 2*Math.PI) camRotTheta -= 2*Math.PI;
//
//		for(Poly3D currentPoly : surfacePolygons) {
//			for(Point3D p : currentPoly.getVertex()) {
//				p.setLocation(p.x,p.z*sin(camRotPhi)+p.y*cos(camRotPhi),p.z*cos(camRotPhi)-p.y*sin(camRotPhi));
//				p.setLocation(p.x*cos(camRotPsi)+p.z*sin(camRotPsi),p.y,-p.x*sin(camRotPsi)+p.z*cos(camRotPsi));
//				p.setLocation(p.x*cos(camRotTheta)-p.y*sin(camRotTheta),p.x*sin(camRotTheta)+p.y*cos(camRotTheta),p.z);
//			}
//		}
//
////		camRotPhi=0;
////		camRotPsi=0;
////		camRotTheta=0;
//	}

	public void setCamRotate(double phi, double psi, double theta) {
		camRotPhi=phi;
		camRotPsi=psi;
		camRotTheta=theta;
		if(camRotPhi > 2*Math.PI) camRotPhi -= 2*Math.PI;
		if(camRotPsi > 2*Math.PI) camRotPsi -= 2*Math.PI;
		if(camRotTheta > 2*Math.PI) camRotTheta -= 2*Math.PI;
	}
	
	private int readInt(DataInputStream data) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.clear();
        buffer.order(ByteOrder.BIG_ENDIAN).putInt(data.readInt()).flip();
        return buffer.order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
    
    private short readShort(DataInputStream data) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.clear();
        buffer.order(ByteOrder.BIG_ENDIAN).putInt(data.readShort()).flip();
        return buffer.order(ByteOrder.LITTLE_ENDIAN).getShort();
    }
    
    private float readFloat(DataInputStream data) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.clear();
        buffer.order(ByteOrder.BIG_ENDIAN).putFloat(data.readFloat()).flip();
        return buffer.order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }
    
    private String readString(DataInputStream data) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(80);
		buffer.clear();
		byte [] comment = new byte[80];
		data.read(comment, 0, 80);
        buffer.order(ByteOrder.BIG_ENDIAN).put(comment).flip();
        return buffer.order(ByteOrder.LITTLE_ENDIAN).array().toString();
    }
	
	public Object3D loadSTL(String filename, double scale, Color c) {		
		Object3D model = new Object3D(c);
		try {
			byte _1bitmask = 0x01; // 00000001
			byte _5bitmask = 0x1F; // 00011111
			byte _6bitmask = 0x3F; // 00111111
			DataInputStream data = new DataInputStream(new FileInputStream(filename));
			byte [] comment = new byte[80];
			
			try { 
				//System.out.println( readString(data) );
				readString(data);
				int numberOfTriangles = readInt(data);
				
				for(int i=0; i < numberOfTriangles; i++) {
					Poly3D triangle = new Poly3D(c);
					
					double normalX = readFloat(data);
					double normalZ = readFloat(data);
					double normalY = readFloat(data);
					
					triangle.setNormal(new MathVector(normalX,normalY,normalZ));
					
					for(int j=0; j < 3; j++){
						double x = readFloat(data)*scale;
						double z = readFloat(data)*scale;
						double y = readFloat(data)*scale;
						triangle.addVertex(x,y,z);
					}
					
					short color = data.readShort();
					/*short gotColor = (short)(color & _1bitmask);
					if(gotColor != 0) {
						//System.out.println("We got color!");
						//color = (short)(color >> 1);
						byte red = (byte)((color >> 11) & _5bitmask); // >> 10
						byte green = (byte)((color >> 5) & _6bitmask); //_5bitmask
						byte blue = (byte)(color & _5bitmask);
						//System.out.println("red: " + red);
						//System.out.println("green: " + green);
						//System.out.println("blue: " + blue);
						
						short R = (short)((255 * (float)(red/31.0))+0.5f);
						short G = (short)((255 * (float)(green/63.0))+0.5f); // 31
						short B = (short)((255 * (float)(blue/31.0))+0.5f);
						
						//System.out.println("red: " + R);
						//System.out.println("green: " + G);
						//System.out.println("blue: " + B);
						
						triangle.setPolyColor(new Color(R,G,B));
					}*/
					
					model.addPolygon(triangle);
				}
				
			} catch(IOException io) {}
		}
		catch(FileNotFoundException e) {}
		return model;
	}

    public Object3D createCube(double side, Color c) {
    	Object3D cube = new Object3D(c);

		Poly3D front;
		Poly3D back;
		Poly3D left;
		Poly3D right;
		Poly3D up;
		Poly3D down;

		back = new Poly3D(c);
		back.addVertex(-side,side,side);
		back.addVertex(side,side,side);
		back.addVertex(side,-side,side);
		back.addVertex(-side,-side,side);

		cube.addPolygon(back);

		front = new Poly3D(c);
		front.addVertex(-side,side,-side);
		front.addVertex(side,side,-side);
		front.addVertex(side,-side,-side);
		front.addVertex(-side,-side,-side);

		cube.addPolygon(front);

		left = new Poly3D(c);
		left.addVertex(-side,side,side);
		left.addVertex(-side,side,-side);
		left.addVertex(-side,-side,-side);
		left.addVertex(-side,-side,side);

		cube.addPolygon(left);

		right = new Poly3D(c);
		right.addVertex(side,side,side);
		right.addVertex(side,side,-side);
		right.addVertex(side,-side,-side);
		right.addVertex(side,-side,side);

		cube.addPolygon(right);

		up = new Poly3D(c);
		up.addVertex(-side,side,side);
		up.addVertex(-side,side,-side);
		up.addVertex(side,side,-side);
		up.addVertex(side,side,side);

		cube.addPolygon(up);

		down = new Poly3D(c);
		down.addVertex(-side,-side,side);
		down.addVertex(-side,-side,-side);
		down.addVertex(side,-side,-side);
		down.addVertex(side,-side,side);

		cube.addPolygon(down);
    	return cube;
    }

    public Object3D createChess(int num, double size) {
    	Object3D chess = new Object3D();
    	double sqrSize=size/(double)num;
    	double bound=size/2;

    	double z=-bound;
		for(int j=0; j < num; j++) {
			double x=-bound;
			for(int i=0; i < num; i++) {
				Color c;
				if(j%2==0){
					if(i%2==0)
						c = Color.white;
					else
						c = Color.black;
				}
				else {
					if(i%2==0)
						c = Color.black;
					else
						c = Color.white;
				}
				Object3D mesh = new Object3D(c);
				Poly3D square = new Poly3D();
				square.addVertex(-sqrSize/2,0,sqrSize/2);
				square.addVertex(-sqrSize/2,0,-sqrSize/2);
				square.addVertex(sqrSize/2,0,-sqrSize/2);
				square.addVertex(sqrSize/2,0,sqrSize/2);
				square.shiftPosition(x,0,z);
				mesh.addPolygon(square);
				x+=sqrSize;
				chess.addMesh(mesh);
			}
			x=0;
			z+=sqrSize;
		}

    	return chess;
    }

    public Object3D createChess(int num, double size, Color c1, Color c2) {
    	Object3D chess = new Object3D();
    	double sqrSize=size/(double)num;
    	double bound=size/2;

    	double z=-bound;
		for(int j=0; j < num; j++) {
			double x=-bound;
			for(int i=0; i < num; i++) {
				Color c;
				if(j%2==0){
					if(i%2==0)
						c = c1;
					else
						c = c2;
				}
				else {
					if(i%2==0)
						c = c2;
					else
						c = c1;
				}
				Object3D mesh = new Object3D(c);
				Poly3D square = new Poly3D();
				square.addVertex(-sqrSize/2,0,sqrSize/2);
				square.addVertex(-sqrSize/2,0,-sqrSize/2);
				square.addVertex(sqrSize/2,0,-sqrSize/2);
				square.addVertex(sqrSize/2,0,sqrSize/2);
				square.shiftPosition(x,0,z);
				mesh.addPolygon(square);
				x+=sqrSize;
				chess.addMesh(mesh);
			}
			x=0;
			z+=sqrSize;
		}

    	return chess;
    }

    public Object3D createRuby(int num, Color c, double scaleFactor) {
    	Object3D ruby = new Object3D(c);
		Poly3D top = new Poly3D(c);

		for(int i=0; i < num; i++) {
	    	Poly3D p1 = new Poly3D(c);
	    	p1.addVertex(0,-40*scaleFactor,0);
	    	p1.addVertex(scaleFactor*50*cos((double)i*Math.PI*2.0/num),30*scaleFactor,scaleFactor*50*sin((double)i*Math.PI*2.0/num));
	    	p1.addVertex(scaleFactor*50*cos((double)(i+1)*Math.PI*2.0/num),scaleFactor*30,scaleFactor*50*sin((double)(i+1)*Math.PI*2.0/num));
	    	ruby.addPolygon(p1);

	    	Poly3D p2 = new Poly3D(c);
	    	p2.addVertex(scaleFactor*50*cos((double)i*Math.PI*2.0/num),scaleFactor*30,scaleFactor*50*sin((double)i*Math.PI*2.0/num));
	    	p2.addVertex(scaleFactor*50*cos((double)(i+1)*Math.PI*2.0/num),scaleFactor*30,scaleFactor*50*sin((double)(i+1)*Math.PI*2.0/num));
	    	p2.addVertex(scaleFactor*30*cos((double)(i+1)*Math.PI*2.0/num),scaleFactor*40,scaleFactor*30*sin((double)(i+1)*Math.PI*2.0/num));
	    	p2.addVertex(scaleFactor*30*cos((double)i*Math.PI*2.0/num),scaleFactor*40,scaleFactor*30*sin((double)i*Math.PI*2.0/num));
	    	ruby.addPolygon(p2);

	    	top.addVertex(scaleFactor*30*cos((double)i*Math.PI*2.0/num),scaleFactor*40,scaleFactor*30*sin((double)i*Math.PI*2.0/num));
		}

		ruby.addPolygon(top);

    	return ruby;
    }

    public Object3D createSphere(int num, Color c, double radius) {
    	return createShape(num,num,c,radius,radius,radius);
    }

    /*public Object3D createCone(int num, Color c, double radius, double height) {
    	double h=height/(sin(Math.PI/6)+1);
    	Object3D cone = createShape(num,3,c,radius,h,radius);
    	cone.shiftPosition(0,height-h,0);
    	return cone;
    }*/

    public Object3D createCone(int num, Color c, double radius, double height) {
    	Object3D cone = new Object3D(c);
    	Poly3D bottom = new Poly3D(c);

    	for(double i=0; i < num; i++) {
    		Poly3D p = new Poly3D(c);
    		p.addVertex(0,height,0);
    		p.addVertex(0,height,0);
    		p.addVertex(radius*cos((i+1)*Math.PI*2.0/num),0,radius*sin((i+1)*Math.PI*2.0/num));
    		p.addVertex(radius*cos(i*Math.PI*2.0/num),0,radius*sin(i*Math.PI*2.0/num));
    		cone.addPolygon(p);
    		bottom.addVertex(radius*cos(i*Math.PI*2.0/num),0,radius*sin(i*Math.PI*2.0/num));
    	}

    	cone.addPolygon(bottom);
    	return cone;
    }

    public Object3D createLeaf(double scale) {
    	Object3D leaf = new Object3D(Color.green);
    	int num=(int)(50*scale);

    	for(double i=0; i < num; i++) {
    		Poly3D p1 = new Poly3D();
    		Poly3D p2 = new Poly3D();
    		p1.addVertex(-scale*sin(4*i*Math.PI/num)-10*scale*sin(i*Math.PI/num),5*scale*sin(i*Math.PI*2.0/num),i);
    		p1.addVertex(0,-5*scale*sin(i*Math.PI/num),i/2);
    		p1.addVertex(0,-5*scale*sin((i+1)*Math.PI/num),(i+1)/2);
    		p1.addVertex(-scale*sin(4*(i+1)*Math.PI/num)-10*scale*sin((i+1)*Math.PI/num),5*scale*sin((i+1)*Math.PI*2.0/num),(i+1));
    		leaf.addPolygon(p1);
    		p2.addVertex(0,-5*scale*sin(i*Math.PI/num),i/2);
    		p2.addVertex(scale*sin(4*i*Math.PI/num)+10*scale*sin(i*Math.PI/num),5*scale*sin(i*Math.PI*2.0/num),i);
    		p2.addVertex(scale*sin(4*(i+1)*Math.PI/num)+10*scale*sin((i+1)*Math.PI/num),5*scale*sin((i+1)*Math.PI*2.0/num),(i+1));
    		p2.addVertex(0,-5*scale*sin((i+1)*Math.PI/num),(i+1)/2);
    		leaf.addPolygon(p2);
    	}

    	return leaf;
    }

    public Object3D createFeather(double scale, Color c) {
    	Object3D feather = new Object3D(c);
    	Object3D shaft = new Object3D(c);
    	int num=(int)(400*scale);

    	for(double i=0; i < num; i++) {
    		Poly3D p1 = new Poly3D(c);
    		Poly3D p2 = new Poly3D(c);
    		p1.addVertex(-(Math.random()*5-1)*scale*sin(2*i*Math.PI/num)-(3*Math.random()+10)*scale*sin(i*Math.PI/num),2*scale*sin(i*Math.PI*2.0/num)+(Math.random()-0.5f),i/4);
    		p1.addVertex(0,-2*scale*sin(i*Math.PI/num)+(Math.random()-0.5f),i/5);
    		p1.addVertex(0,-2*scale*sin((i+1)*Math.PI/num)+(Math.random()-0.5f),(i+1)/5);
    		p1.addVertex(-(Math.random()*5-1)*scale*sin(2*(i+1)*Math.PI/num)-(3*Math.random()+10)*scale*sin((i+1)*Math.PI/num),2*scale*sin((i+1)*Math.PI*2.0/num)+(Math.random()-0.5f),(i+1)/4);
    		feather.addPolygon(p1);
    		p2.addVertex(0,-2*scale*sin(i*Math.PI/num)+(Math.random()-0.5f),i/5);
    		p2.addVertex((Math.random()*5-1)*scale*sin(2*i*Math.PI/num)+(3*Math.random()+10)*scale*sin(i*Math.PI/num),2*scale*sin(i*Math.PI*2.0/num)+(Math.random()-0.5f),i/4);
    		p2.addVertex((Math.random()*5-1)*scale*sin(2*(i+1)*Math.PI/num)+(3*Math.random()+10)*scale*sin((i+1)*Math.PI/num),2*scale*sin((i+1)*Math.PI*2.0/num)+(Math.random()-0.5f),(i+1)/4);
    		p2.addVertex(0,-2*scale*sin((i+1)*Math.PI/num)+(Math.random()-0.5f),(i+1)/5);
    		feather.addPolygon(p2);
    	}

    	shaft=createCone(8,c,scale/5,7);
    	shaft.rotate(Math.PI/2,0,0);
    	shaft.shiftPosition(0,0,1);

    	feather.addMesh(shaft);
    	return feather;
    }

    public Object3D createCone(int num, Color c, double radiusX, double radiusZ, double height) {
    	Object3D cone = new Object3D(c);
    	Poly3D bottom = new Poly3D();

    	for(double i=0; i < num; i++) {
    		Poly3D p = new Poly3D(c);
    		p.addVertex(0,height,0);
    		p.addVertex(0,height,0);
    		p.addVertex(radiusX*cos((i+1)*Math.PI*2.0/num),0,radiusZ*sin((i+1)*Math.PI*2.0/num));
    		p.addVertex(radiusX*cos(i*Math.PI*2.0/num),0,radiusZ*sin(i*Math.PI*2.0/num));
    		cone.addPolygon(p);
    		bottom.addVertex(radiusX*cos(i*Math.PI*2.0/num),0,radiusZ*sin(i*Math.PI*2.0/num));
    	}

    	cone.addPolygon(bottom);
    	return cone;
    }

    public Object3D createArrow(Color c, double length) {
		Object3D arrow = new Object3D();
		Object3D cone=createCone(30,c,10,20);
		cone.shiftPosition(0,length-20,0);
		Object3D cylinder=createCylinder(30,c,3,length-20);
		arrow.addMesh(cone);
		arrow.addMesh(cylinder);
    	return arrow;
    }


    public Object3D createArrow(Color c, double length, double scale) {
		Object3D arrow = new Object3D();
		Object3D cone=createCone((int)Math.round(30*Math.sqrt(scale)),c,5*scale,10*scale);
		cone.shiftPosition(0,length-10*scale,0);
		Object3D cylinder=createCylinder((int)Math.round(30*Math.sqrt(scale)),c,scale,length-10*scale);
		arrow.addMesh(cone);
		arrow.addMesh(cylinder);
    	return arrow;
    }

    public Object3D createArrow(Color c, Point3D p1, Point3D p2, double scale) {
		Object3D arrow = new Object3D();
		//p1.x=-p1.x;
		//p2.x=-p2.x;
		MathVector lineVector = p2.minus(p1);
		double length = lineVector.length();
		MathVector e_x = new MathVector(1,0,0), e_y = new MathVector(0,1,0), e_z = new MathVector(0,0,1);
		Object3D cone=createCone((int)Math.round(30*Math.sqrt(scale)),c,5*scale,10*scale);
		cone.shiftPosition(0,length-10*scale,0);
		Object3D cylinder=createCylinder((int)Math.round(30*Math.sqrt(scale)),c,scale,length-10*scale);
		arrow.addMesh(cone);
		arrow.addMesh(cylinder);
		arrow.rotateZ(-Math.PI/2);
		double phi=Math.acos(lineVector.dot(e_z)/length);
		double psi=Math.acos(lineVector.dot(e_y)/length);
		double theta=Math.acos(lineVector.dot(e_x)/length);
		//arrow.rotateX(phi-Math.PI/2);
		//arrow.rotateY(psi-Math.PI/2);
		//arrow.rotateZ(-Math.PI/2);
		arrow.rotate(phi-Math.PI/2,0,theta);
		arrow.shiftPosition(p1);
    	return arrow;
    }

    public Object3D createAxis(double length) {
    	Object3D arrowX, arrowY, arrowZ, axis;

		arrowX=createArrow(Color.red,length-3);
		arrowX.rotateZ(-Math.PI/2);
		arrowX.shiftPosition(3,0,0);

		arrowY=createArrow(Color.green,length-3);
		arrowY.shiftPosition(0,3,0);

		arrowZ=createArrow(Color.blue,length-3);
		arrowZ.rotateX(-Math.PI/2);
		arrowZ.shiftPosition(0,0,3);

		axis = new Object3D();
		axis.addMesh(arrowX);
		axis.addMesh(arrowY);
		axis.addMesh(arrowZ);

		return axis;
    }

    public Object3D createAxis(double length, double scale) {
    	Object3D arrowX, arrowY, arrowZ, axis;

		arrowX=createArrow(Color.red,length-scale,scale);
		arrowX.rotateZ(-Math.PI/2);
		arrowX.shiftPosition(scale,0,0);

		arrowY=createArrow(Color.green,length-scale,scale);
		arrowY.shiftPosition(0,scale,0);

		arrowZ=createArrow(Color.blue,length-scale,scale);
		arrowZ.rotateX(-Math.PI/2);
		arrowZ.shiftPosition(0,0,scale);

		axis = new Object3D();
		axis.addMesh(arrowX);
		axis.addMesh(arrowY);
		axis.addMesh(arrowZ);

		return axis;
    }

    public Object3D createShape(int num1, int num2, Color c, double radiusX, double radiusY, double radiusZ) {
    	Object3D shape = new Object3D(c);
    	double r1, r2, r3, r4, j;

		for(j=0; j < num2/2; j++) {
			for(double i=0; i < num1; i++) {
		    	Poly3D p = new Poly3D(c);
		    	r1=radiusX*sin((double)j*Math.PI*2/num2);
		    	r2=radiusX*sin((double)(j+1)*Math.PI*2/num2);
		    	r3=radiusZ*sin((double)j*Math.PI*2/num2);
		    	r4=radiusZ*sin((double)(j+1)*Math.PI*2/num2);
		    	p.addVertex(r1*cos((double)i*Math.PI*2.0/num1),radiusY*cos((double)j*Math.PI*2.0/num2),r3*sin((double)i*Math.PI*2.0/num1));
		    	p.addVertex(r1*cos((double)(i+1)*Math.PI*2.0/num1),radiusY*cos((double)j*Math.PI*2.0/num2),r3*sin((double)(i+1)*Math.PI*2.0/num1));
		    	p.addVertex(r2*cos((double)(i+1)*Math.PI*2.0/num1),radiusY*cos((double)(j+1)*Math.PI*2.0/num2),r4*sin((double)(i+1)*Math.PI*2.0/num1));
		    	p.addVertex(r2*cos((double)i*Math.PI*2.0/num1),radiusY*cos((double)(j+1)*Math.PI*2.0/num2),r4*sin((double)i*Math.PI*2.0/num1));
		    	shape.addPolygon(p);
			}
		}
		if(num2%2!=0){
			Poly3D p = new Poly3D(c);
			for(double i=0; i < num1; i++) {
		    	r1=radiusX*sin((double)j*Math.PI*2/num2);
		    	r3=radiusZ*sin((double)j*Math.PI*2/num2);
		    	p.addVertex(r1*cos((double)i*Math.PI*2.0/num1),radiusY*cos((double)j*Math.PI*2.0/num2),r3*sin((double)i*Math.PI*2.0/num1));
		    	p.addVertex(r1*cos((double)(i+1)*Math.PI*2.0/num1),radiusY*cos((double)j*Math.PI*2.0/num2),r3*sin((double)(i+1)*Math.PI*2.0/num1));
			}
			shape.addPolygon(p);
		}
    	return shape;
    }

    public Object3D createCylinder(int num, Color c, double radius, double height) {
    	Object3D cylinder = new Object3D(c);
    	Poly3D top = new Poly3D(), bottom = new Poly3D();
    	for(double i=0; i < num; i++) {
    		Poly3D p = new Poly3D(c);
    		p.addVertex(radius*cos(i*Math.PI*2.0/num),height,radius*sin(i*Math.PI*2.0/num));
    		p.addVertex(radius*cos((i+1)*Math.PI*2.0/num),height,radius*sin((i+1)*Math.PI*2.0/num));
    		p.addVertex(radius*cos((i+1)*Math.PI*2.0/num),0,radius*sin((i+1)*Math.PI*2.0/num));
    		p.addVertex(radius*cos(i*Math.PI*2.0/num),0,radius*sin(i*Math.PI*2.0/num));
    		cylinder.addPolygon(p);
    		top.addVertex(radius*cos(i*Math.PI*2.0/num),height,radius*sin(i*Math.PI*2.0/num));
    		bottom.addVertex(radius*cos(i*Math.PI*2.0/num),0,radius*sin(i*Math.PI*2.0/num));
    	}
    	cylinder.addPolygon(top);
    	cylinder.addPolygon(bottom);
    	return cylinder;
    }

    public Object3D createTorus(int num, Color c, double r1, double r2) {
    	Object3D torus = new Object3D(c);
    	int q=(int)Math.round(r1/r2);

		for(double j=0; j < num*q; j++) {
			for(double i=0; i < num; i++) {
				Poly3D poly = new Poly3D(c);
				Point3D p1 = new Point3D(0,r2*sin(i*Math.PI*2.0/num),r2*cos(i*Math.PI*2.0/num)+r1);
				Point3D p2 = new Point3D(0,r2*sin(i*Math.PI*2.0/num),r2*cos(i*Math.PI*2.0/num)+r1);
				Point3D p3 = new Point3D(0,r2*sin((i+1)*Math.PI*2.0/num),r2*cos((i+1)*Math.PI*2.0/num)+r1);
				Point3D p4 = new Point3D(0,r2*sin((i+1)*Math.PI*2.0/num),r2*cos((i+1)*Math.PI*2.0/num)+r1);
		    	p1.rotateY(j*Math.PI*2.0/(q*num));
		    	p2.rotateY((j+1)*Math.PI*2.0/(q*num));
		    	p3.rotateY((j+1)*Math.PI*2.0/(q*num));
		    	p4.rotateY(j*Math.PI*2.0/(q*num));
		    	poly.addVertex(p1);
		    	poly.addVertex(p2);
		    	poly.addVertex(p3);
		    	poly.addVertex(p4);
		    	torus.addPolygon(poly);
			}
		}
    	return torus;
    }

    public Object3D createTriForce(double base, double depth, Color c) {
    	Object3D triForce = new Object3D(c);

    	Poly3D front1 = new Poly3D(c);
    	Poly3D back1 = new Poly3D(c);
    	Poly3D left1 = new Poly3D(c);
    	Poly3D right1 = new Poly3D(c);
    	Poly3D bottom1 = new Poly3D(c);

    	Poly3D front2 = new Poly3D(c);
    	Poly3D back2 = new Poly3D(c);
    	Poly3D left2 = new Poly3D(c);
    	Poly3D right2 = new Poly3D(c);
    	Poly3D bottom2 = new Poly3D(c);

    	Poly3D front3 = new Poly3D(c);
    	Poly3D back3 = new Poly3D(c);
    	Poly3D left3 = new Poly3D(c);
    	Poly3D right3 = new Poly3D(c);
    	Poly3D bottom3 = new Poly3D(c);

    	double height=Math.sqrt(0.75*base*base);

    	front1.addVertex(-base/2,height/2,-depth/2);
    	front1.addVertex(base/2,height/2,-depth/2);
    	front1.addVertex(0,3*height/2,-depth/2);

    	triForce.addPolygon(front1);

    	back1.addVertex(-base/2,height/2,depth/2);
    	back1.addVertex(base/2,height/2,depth/2);
    	back1.addVertex(0,3*height/2,depth/2);

    	triForce.addPolygon(back1);

    	left1.addVertex(-base/2,height/2,-depth/2);
    	left1.addVertex(-base/2,height/2,depth/2);
    	left1.addVertex(0,3*height/2,depth/2);
    	left1.addVertex(0,3*height/2,-depth/2);

    	triForce.addPolygon(left1);

    	right1.addVertex(base/2,height/2,-depth/2);
    	right1.addVertex(base/2,height/2,depth/2);
    	right1.addVertex(0,3*height/2,depth/2);
    	right1.addVertex(0,3*height/2,-depth/2);

    	triForce.addPolygon(right1);

    	bottom1.addVertex(-base/2,height/2,-depth/2);
    	bottom1.addVertex(-base/2,height/2,depth/2);
    	bottom1.addVertex(base/2,height/2,depth/2);
    	bottom1.addVertex(base/2,height/2,-depth/2);

    	triForce.addPolygon(bottom1);

    	front2.addVertex(-base,-height/2,-depth/2);
    	front2.addVertex(0,-height/2,-depth/2);
    	front2.addVertex(-base/2,height/2,-depth/2);

    	triForce.addPolygon(front2);

    	back2.addVertex(-base,-height/2,depth/2);
    	back2.addVertex(0,-height/2,depth/2);
    	back2.addVertex(-base/2,height/2,depth/2);

    	triForce.addPolygon(back2);

    	left2.addVertex(-base,-height/2,-depth/2);
    	left2.addVertex(-base,-height/2,depth/2);
    	left2.addVertex(-base/2,height/2,depth/2);
    	left2.addVertex(-base/2,height/2,-depth/2);

    	triForce.addPolygon(left2);

    	right2.addVertex(0,-height/2,-depth/2);
    	right2.addVertex(0,-height/2,depth/2);
    	right2.addVertex(-base/2,height/2,depth/2);
    	right2.addVertex(-base/2,height/2,-depth/2);

    	triForce.addPolygon(right2);

    	bottom2.addVertex(-base,-height/2,-depth/2);
    	bottom2.addVertex(-base,-height/2,depth/2);
    	bottom2.addVertex(0,-height/2,depth/2);
    	bottom2.addVertex(0,-height/2,-depth/2);

    	triForce.addPolygon(bottom2);

    	front3.addVertex(0,-height/2,-depth/2);
    	front3.addVertex(base,-height/2,-depth/2);
    	front3.addVertex(base/2,height/2,-depth/2);

    	triForce.addPolygon(front3);

    	back3.addVertex(0,-height/2,depth/2);
    	back3.addVertex(base,-height/2,depth/2);
    	back3.addVertex(base/2,height/2,depth/2);

    	triForce.addPolygon(back3);

    	left3.addVertex(0,-height/2,-depth/2);
    	left3.addVertex(0,-height/2,depth/2);
    	left3.addVertex(base/2,height/2,depth/2);
    	left3.addVertex(base/2,height/2,-depth/2);

    	triForce.addPolygon(left3);

    	right3.addVertex(base,-height/2,-depth/2);
    	right3.addVertex(base,-height/2,depth/2);
    	right3.addVertex(base/2,height/2,depth/2);
    	right3.addVertex(base/2,height/2,-depth/2);

    	triForce.addPolygon(right3);

    	bottom3.addVertex(0,-height/2,-depth/2);
    	bottom3.addVertex(0,-height/2,depth/2);
    	bottom3.addVertex(base,-height/2,depth/2);
    	bottom3.addVertex(base,-height/2,-depth/2);

    	triForce.addPolygon(bottom3);

    	return triForce;
    }

//    public Object3D createRandomMatrix(double num, double size, Color c) {
//    	Object3D matrix = new Object3D(c);
//    	double sqrSize=size/(double)num;
//    	double bound=size/2;
//    	double amp1=(Math.random()*sqrSize/2)-sqrSize/2;
//    	double amp2=(Math.random()*sqrSize/2)-sqrSize/2;
//    	double amp3=(Math.random()*sqrSize/2)-sqrSize/2;
//    	double amp4=(Math.random()*sqrSize/2)-sqrSize/2;
//
//    	double z=-bound;
//		for(int j=0; j < num; j++) {
//			double x=-bound;
//			for(int i=0; i < num; i++) {
//				Poly3D square = new Poly3D();
//				square.addVertex(-sqrSize/2,amp1*Math.sin(2*i*Math.PI/num),sqrSize/2);
//				square.addVertex(-sqrSize/2,amp2*Math.sin(2*i*Math.PI/num),-sqrSize/2);
//				square.addVertex(sqrSize/2,amp3*Math.sin(2*i*Math.PI/num),-sqrSize/2);
//				square.addVertex(sqrSize/2,amp4*Math.sin(2*i*Math.PI/num),sqrSize/2);
//				amp1+=((Math.random()*sqrSize/2)-sqrSize/2)/100;
//				amp2+=((Math.random()*sqrSize/2)-sqrSize/2)/100;
//				amp3+=((Math.random()*sqrSize/2)-sqrSize/2)/100;
//				amp4+=((Math.random()*sqrSize/2)-sqrSize/2)/100;
//				square.shiftPosition(x,0,z);
//				matrix.addPolygon(square);
//				x+=sqrSize;
//			}
//			x=0;
//			z+=sqrSize;
//		}
//
//    	return matrix;
//    }

    public Poly3D clipPoly(Poly3D currentPoly) {
    	return null;
    }

    @Override
    public void init() {} // Could contain specific 2D configuration for a Surface3D. initScene()'s settings overides init()'s settings.

    @Override
    public void draw() {
    	drawScene();
    	drawModels();
    	drawSurface(getSurface());
    }

	public abstract void initScene();
    public abstract void drawScene();
    public abstract void drawSurface(Graphics2D g2);

    public void drawModels() {
    	for(Object3D model : surfaceModels) {
    		//Color c = model.getObjectColor();
    		double contrast=model.getColorContrast();
    		double brightness=model.getColorBrightness();
    		boolean light=model.isLight();
    		for(Poly3D poly : model.getPolygons()) {
    			poly.calcNormal();
    			double CosAngle=poly.getCosAngleToView();
    			if(CosAngle < 0 || poly.getMinDepth() < 1) continue; // no backfacing polygons
    			Color c = poly.getPolyColor();  			
    			if(light){
    				double absCosAngle=Math.abs(CosAngle);
    				double m = ((1-contrast)+absCosAngle*contrast)*brightness;
    				double n = 1;//fog(poly.distanceCamera());
    				double highlight=0.7*Math.pow(m,15)*n;
    				int r=(int)Math.round(c.getRed()*m*n+highlight*(255-c.getRed())), g=(int)Math.round(c.getGreen()*m*n+highlight*(255-c.getGreen())), b=(int)Math.round(c.getBlue()*m*n+highlight*(255-c.getBlue()));
	    			if(r>255) r=255; if(g>255) g=255; if(b>255) b=255;
	    			Color shade = new Color(r,g,b,c.getAlpha());
					poly.setPolyColor(shade);
    			}
    			else {
    				double absCosAngle=Math.abs(CosAngle);
    				Color shade = c;
    				//if(absCosAngle < 0.7) shade = new Color(c.getRed()/2,c.getGreen()/2,c.getBlue()/2,c.getAlpha());
    				if(absCosAngle >=0 && absCosAngle < 0.5) shade = new Color(c.getRed()/4,c.getGreen()/4,c.getBlue()/4,c.getAlpha());
    				if(absCosAngle >=0.5 && absCosAngle < 0.7) shade = new Color(c.getRed()/2,c.getGreen()/2,c.getBlue()/2,c.getAlpha());
    				if(absCosAngle >=0.7 && absCosAngle < 0.90) shade = new Color(3*c.getRed()/4,3*c.getGreen()/4,3*c.getBlue()/4,c.getAlpha());
    				poly.setPolyColor(shade);
    			}

	    		//poly.rotate(camRotPhi,camRotPsi,camRotTheta);
	    		surfacePolygons.add(poly); 
	    		//if(poly.getMaxDepth() > 1) surfacePolygons.add(poly);
	    	}

	    	//Collections.sort(model.getPolygons(),new PolyNormalComparator());
    	}

    	for(Poly3D poly : surfacePolygons) {
    		Poly3D newPoly=null;
    		newPoly = clipPoly(poly);						// Clip polygon if necessary,
			if(newPoly!=null) surfacePolygons.add(newPoly);	// and add new poly if there is one...
    	}

    	Collections.sort(surfacePolygons,new Poly3DComparator());
/*
    	for(int i=0; i < surfacePolygons.size(); i++) {
    		Poly3D thisPoly = surfacePolygons.get(i);
    		    		    		
    		if(i != surfacePolygon.size()-1) { // if there is a next polygon, check if this is covered...
    			Poly3D nextPoly = surfacePolygons.get(i+1);
    			
    			
    			if( thisPoly.getVertex().get(0).x < nextPoly.getVertex().x &&
    				thisPoly.getVertex().get(0).y < nextPoly.getVertex().y &&
    				thisPoly.getVertex().get(0).z < nextPoly.getVertex().z &&
    				) thisPoly.setVisible(false);
    		}
    		
    		if(thisPoly.getMaxDepth() < 1)
				thisPoly.setVisible(false);

    		if(thisPoly.isVisible()) {
    			thisPoly.drawPoly(); // "Clip"-position 1, actually "ignore"-position right now...
    		}
    	}*/
    	
    	for(Poly3D poly : surfacePolygons) { 		
    		if(poly.getMaxDepth() < 1)
				poly.setVisible(false);

    		if(poly.isVisible()) {
    			poly.drawPoly(); // "Clip"-position 1, actually "ignore"-position right now...
    		}
    	}

		numberOfModels=surfaceModels.size();
		numberOfPolys=surfacePolygons.size();
    	surfaceModels.clear();
    	surfacePolygons.clear();
    }
}

//		public void rotate(double phi, double psi, double theta) {
//			if(psi > 2*Math.PI) psi -= 2*Math.PI;
//			if(phi > 2*Math.PI) phi -= 2*Math.PI;
//			if(theta > 2*Math.PI) theta -= 2*Math.PI;
//
//			for(int i=0; i < vertex.size(); i++) {
//				double x=vertex.get(i).x;
//				double y=vertex.get(i).y;
//				double z=vertex.get(i).z;
//				vertex.get(i).setLocation( // [x,y,z] column vector times rotation matrix...
//					x*(cos(theta)*cos(psi))+
//					y*(-cos(phi)*sin(psi)+sin(phi)*sin(theta)*cos(psi))+
//					z*(sin(phi)*sin(psi)+cos(phi)*sin(theta)*cos(psi)),
//
//					x*(cos(theta)*sin(psi))+
//					y*(cos(phi)*cos(psi)+sin(phi)*sin(theta)*sin(psi))+
//					z*(-sin(phi)*cos(psi)+cos(phi)*sin(theta)*sin(psi)),
//
//					x*(-sin(theta))+
//					y*(sin(phi)*cos(theta))+
//					z*(cos(phi)*cos(theta))
//				);
//			}
//		}