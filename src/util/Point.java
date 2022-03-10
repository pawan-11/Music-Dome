package util;

public class Point {
	
	private float x = 0, y = 0;
	public static int count = 0;
	
	public Point(float x, float y) { //could add boolean final, to indicate whether this point should ever be modified	
		count++;
		copy(x, y);
	}
	
	public Point() {
		this(0,0);
	}
	
	public int deg_ang(Point p) { //angle from source <p> as origin to this
		return Math.floorMod((int)Math.toDegrees(rad_ang(p)), 360);
	}
	
	public double rad_ang(Point p) {
		return Math.atan2(y-p.getY(), p.getX()-x);
	}
	
	public float distance(Point p) {
		return (float)(Math.sqrt(Math.pow(p.getY()-y, 2)+Math.pow(p.getX()-x, 2)));
	}
	
	public Point add(Point p) {
		return add(p.getX(), p.getY());
	}
	
	public Point add(double x, double y) {
		this.x+=x;
		this.y+=y;
		return this;
	}
	
	public Point subtract(Point p) {
		return this.add(p.multiply(-1));
	}
	
	public Point multiply(double r) {
		return multiply(r, r);
	}
	
	public Point multiply(double x_r, double y_r) {
		x*= x_r;
		y*= y_r;
		return this;
	}
	
	public Point flip() {
		float tmp = x;
		this.setX(y);
		this.setY(tmp);
		return this;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public Point copy() {
		return new Point(x, y);
	}
	
	public Point copy(Point p) {
		return copy(p.getX(), p.getY());
	}
	
	public Point copy(float x, float y) {
		this.setX(x);
		this.setY(y);
		return this;
	}
	
	public String toString() {
		return "Point: "+"("+x+", "+y+")";
	}
	
	public boolean equals(Point p) {
		return equals(p.getX(), p.getY());
	}

	public boolean equals(float x, float y) {
		return x == this.x && this.x == this.y;
	}
}
