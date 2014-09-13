/**
 * @(#)MathVector.java
 *
 *
 * @author Mikael Murstam
 * @version 1.00 2011/10/22
 */

public class MathVector {
	public double x;
	public double y;
	public double z;

	public MathVector() {
		x=0;
		y=0;
		z=0;
	}

	public MathVector(double x, double y, double z) {
		this.x=x;
		this.y=y;
		this.z=z;
	}

	public MathVector(MathVector v) {
		x=v.x;
		y=v.y;
		z=v.z;
	}

	public MathVector(double x, double y) {
		this.x=x;
		this.y=y;
		this.z=0;
	}

	public void decrease(MathVector b) {
		x-=b.x;
		y-=b.y;
		z-=b.z;
	}

	public void increase(MathVector b) {
		x+=b.x;
		y+=b.y;
		z+=b.z;
	}

	public MathVector minus(MathVector b) {
		MathVector v = new MathVector(x-b.x, y-b.y, z-b.z);
		return v;
	}

	public MathVector plus(MathVector b) {
		MathVector v = new MathVector(x+b.x, y+b.y, z+b.z);
		return v;
	}

	public void scale(double scalar) {
		x*=scalar;
		y*=scalar;
		z*=scalar;
	}

	public boolean equals(MathVector b) {
            if(x==b.x && y==b.y && z==b.z) {
                return true;
            }
            else {
                return false;
            }
	}

	public MathVector cross(MathVector b) {
		MathVector v = new MathVector();
		v.x=y*b.z - z*b.y;
		v.y=z*b.x - x*b.z;
		v.z=x*b.y - y*b.x;
		return v;
	}

	public double dot(MathVector b) {
		return (x*b.x+y*b.y+z*b.z);
	}

	public void normalize() {
		double l = length();
		x/=l;
		y/=l;
		z/=l;
	}

	public double length() {
		return Math.sqrt(x*x + y*y + z*z);
	}

	public static MathVector getNormal(MathVector a, MathVector b) {
		MathVector normal = new MathVector();
		normal=a.cross(b);
		normal.normalize();
		return normal;
	}

	public void setVector(double x, double y, double z) {
		this.x=x;
		this.y=y;
		this.z=z;
	}

	public void setX(double x) {
		this.x=x;
	}

	public void setY(double y) {
		this.y=y;
	}

	public void setZ(double z) {
		this.z=z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}
}