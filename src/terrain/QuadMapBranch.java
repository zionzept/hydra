package terrain;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class QuadMapBranch extends QuadMap {
	protected QuadMap[] quad_map;
	protected int ready_counter;
	private QuadMapLeaf merge;
	
	public QuadMapBranch(QuadMapBranch parent, int position, double x0, double y0, int res) {
		super(parent, position, x0, y0, res);
		quad_map = new QuadMap[4];
		quad_map[0] = new QuadMapLeaf(this, 0, x0 + side * 0.5, y0 + side * 0.5, res - 1);
		quad_map[1] = new QuadMapLeaf(this, 1, x0, y0 + side * 0.5, res - 1);
		quad_map[2] = new QuadMapLeaf(this, 2, x0, y0, res - 1);
		quad_map[3] = new QuadMapLeaf(this, 3, x0 + side * 0.5, y0, res - 1);
	}
	
	public void delete() {
		quad_map[0].delete();
		quad_map[1].delete();
		quad_map[2].delete();
		quad_map[3].delete();
	}
	
	public void interrupt() {
		quad_map[0].interrupt();
		quad_map[1].interrupt();
		quad_map[2].interrupt();
		quad_map[3].interrupt();
	}
	
	public boolean confirm(QuadMap child) {
		return     quad_map[0] == child
				|| quad_map[1] == child
				|| quad_map[2] == child
				|| quad_map[3] == child;
	}
	
	@Override
	public int validate(double x, double y, double dt) {	
		if (merge == null) {
			if (out_of_bounds(x, y)) {
				merge = new QuadMapLeaf(parent, position, x0, y0, res);
			}
		} 
		if (merge != null) {
			merge.validate(x, y, dt);
			if (out_of_bounds(x, y)) {
				if (merge.ready) {
					parent.quad_map[position] = merge;
					merge = null;
					interrupt();
					delete();
				}
			} else if (in_bounds(x, y)) {
				merge.interrupt();
				merge.delete();
				merge = null;
			}
		}
		int i = 0;
		i += quad_map[0].validate(x, y, dt);
		i += quad_map[1].validate(x, y, dt);
		i += quad_map[2].validate(x, y, dt);
		i += quad_map[3].validate(x, y, dt);
		return i;
	}
	
	@Override
	public void render(Matrix4f transform, boolean far) {
		quad_map[0].render(transform, far);
		quad_map[1].render(transform, far);
		quad_map[2].render(transform, far);
		quad_map[3].render(transform, far);
	}
	
	@Override
	public void nodeString(StringBuilder sb, int depth) {
		for (int i = 0; i < depth*2; i++) {
			sb.append(' ');
		}
		sb.append("map_node: ").append(res).append('\n');
		quad_map[0].nodeString(sb, depth + 1);
		quad_map[1].nodeString(sb, depth + 1);
		quad_map[2].nodeString(sb, depth + 1);
		quad_map[3].nodeString(sb, depth + 1);
	}
	
	
}
