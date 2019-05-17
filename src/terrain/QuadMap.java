package terrain;

import org.joml.Matrix4f;

import gl.Shader;
import gl.Texture;

public abstract class QuadMap {
	
	public static final int POLYS = 64;
	public static final int MAX_QUALITY = 0;
	public static final int FAR_LIMIT = 6;
	
	public static Shader shader;
	public static Texture grass_tex;

	protected final double x0;
	protected final double y0;
	protected final int res;
	protected final double side;
	

	protected QuadMapBranch parent;
	protected int position;
	

	protected boolean ready;
	
	public QuadMap(QuadMapBranch parent, int position, double x0, double y0, int res) {
		this.parent = parent;
		this.position = position;
		this.x0 = x0;
		this.y0 = y0;
		this.res = res;
		this.side = POLYS * (1 << res);
	}
	
	protected boolean in_bounds(double x, double y) {
		return x >= x0 - side * 0.6 && x < x0 + side * 1.8 && y >= y0 - side * 0.6 && y < y0 + side * 1.8;
	}
	
	protected boolean out_of_bounds(double x, double y) {
		return x < x0 - side * 0.66 || x >= x0 + side * 1.98 || y < y0 - side * 0.66 || y >= y0 + side * 1.98;
	}
	
	public abstract void delete();
	public abstract void interrupt();
	public abstract int validate(double x, double y, double dt);
	public abstract void render(Matrix4f transform, boolean far);
	
	public abstract void nodeString(StringBuilder sb, int depth);
	
	private static int check_num = 0;
	public static synchronized void modify_check_num(int i) {
		check_num += i;
	}
	
	public static synchronized int get_check_num() {
		return check_num;
	}
}
