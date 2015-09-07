
public class Hotel implements Comparable<Hotel>{ 
	private int id;
	private double x;
	private double y;

	
	
	public Hotel(int _id, double _x, double _y) {
		this.x = _x;
		this.y = _y;
		this.id = _id;
	}

	public int getId() {
		return id;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

	@Override
	public int compareTo(Hotel other) {
		return this.getX() - other.getX() > 0 ? 1 : -1;
	}
	
	
}
