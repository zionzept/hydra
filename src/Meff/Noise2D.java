package Meff;

import org.joml.Vector2d;

public class Noise2D implements F2D {

	public Noise2D() {
		size = new double[0];
		dir = new double[0];
		amp = new double[0];
	}
	
	private Vector2d random(Vector2d st) {
		st = new Vector2d(st.dot(new Vector2d(127.1, 311.7)), st.dot(new Vector2d(269.5,183.3)));
		return fract(sin(st).mul(43758.5453123));
	}
	
	private Vector2d fract(Vector2d st) {
		return new Vector2d(st.x - (double)Math.floor(st.x), st.y - (double)Math.floor(st.y));
	}
	
	private Vector2d sin(Vector2d st) {
		return new Vector2d(Math.sin(st.x), Math.sin(st.y));
	}
	
	
	private double constant;
	
	public void setConstant(double c) {
		constant = c;
	}
	
	private double[] size;
	private double[] amp;
	private double[] dir;
	
	public void addComponent(double size, double amplitude, double direction) {
		int i;
		double[] new_size = new double[this.size.length + 1];
		for (i = 0; i < this.size.length; i++) {
			new_size[i] = this.size[i];
		}
		new_size[i] = size;
		this.size = new_size;
		double[] new_amp = new double[this.amp.length + 1];
		for (i = 0; i < this.amp.length; i++) {
			new_amp[i] = this.amp[i];
		}
		new_amp[i] = amplitude;
		amp = new_amp;
		double[] new_dir = new double[this.dir.length + 1];
		for (i = 0; i < this.dir.length; i++) {
			new_dir[i] = this.dir[i];
		}
		new_dir[i] = direction;
		dir = new_dir;
	}
	
	@Override
	public double get(double x, double y) {
		double z = 0;
		for (int k = 0; k < size.length; k++) {
			Vector2d st = new Vector2d(((1-dir[k])*x - dir[k]*y) / size[k], ((1-dir[k])*y + dir[k]*x) / size[k]);
			Vector2d i = new Vector2d((double)Math.floor(st.x), (double)Math.floor(st.y));
			Vector2d f = fract(st);
			Vector2d u = new Vector2d();
			
			// cubic softstep
			/*
			f.mul(-2, u);
			u.add(3, 3);
			f.mul(u, u);
			f.mul(u, u);
			*/
			
			// quintic softstep
			f.mul(-6, u);
			u.add(15, 15);
			f.mul(u, u);
			u.mul(-1);
			u.add(10, 10);
			f.mul(u, u);
			f.mul(u, u);
			f.mul(u, u);
			
			
			
			double a = random(i).dot(f);
			i.x++;
			f.x--;
			double b = random(i).dot(f);
			i.y++;
			f.y--;
			double d = random(i).dot(f);
			i.x--;
			f.x++;
			double c = random(i).dot(f);
			
			
			//double a = random(i);
			
			
			
			
			double q = (1d-u.x)*a + u.x*b;
			double p = (1d-u.x)*c + u.x*d;
			z += ((1d-u.y)*q + u.y*p) * amp[k];
		}
		return z + constant;
	}

}
