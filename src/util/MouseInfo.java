package util;


public class MouseInfo extends Observable { //mousestrategies observe mouse

	private Point pos;
	public static MouseInfo mouse_info = new MouseInfo();
	private boolean pressed = false;
	
	private MouseInfo() {
		pos = new Point(0,0);
	}
	
	public void mouseMoved(double x, double y) {
		pos.copy((float)x, (float)y);
	}
	
	public void mousePressed() {
		if (!pressed) {
			changed("clicked");
			pressed = true;
		}
	}
	
	public void mouseReleased() {
		pressed = false;
	}

	public double getX() {
		return pos.getX();
	}
	
	public double getY() {
		return pos.getY();
	}
	
	public Point get_pos() {
		return pos;
	}
	
	public boolean isPressed() {
		return pressed;
	}

	@Override
	public void create_lists() {
		//mouse strategies observe this
		super.create_list("strat", Integer.MAX_VALUE);
	}
}