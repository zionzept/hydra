package meff.meff2D;

import java.util.LinkedList;

public class CompositeF2D extends LinkedList<F2D> implements F2D {
	private static final long serialVersionUID = 1L;
	private double c;
	
	public CompositeF2D(double constant) {
		super();
		c = constant;
	}

	@Override
	public double get(double x, double y) {
		double val = c;
		for (F2D f2d : this) {
			val += f2d.get(x, y);
		}
		return val;
	}
}
