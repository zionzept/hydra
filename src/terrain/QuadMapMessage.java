package terrain;

public class QuadMapMessage implements Comparable<QuadMapMessage> {
	
	public QuadMapLeaf target;
	public double x0;
	public double y0;
	public int res;
	
	public QuadMapMessage(QuadMapLeaf target, double x0, double y0, int res) {
		this.target = target;
		this.x0 = x0;
		this.y0 = y0;
		this.res = res;
	}

	@Override
	public int compareTo(QuadMapMessage m) {
		return m.res - res;
	}
	
	@Override
	public String toString() {
		return res + " " + x0 + " " + y0;
	}
}