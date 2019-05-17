package terrain;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;


public class QuadMapRoot extends QuadMapBranch {

	
	
	public QuadMapRoot(int position, double x0, double y0, int res) {
		super(null, position, x0, y0, res);
	}

	@Override
	public int validate(double x, double y, double dt) {
		int i = 0;
		i += quad_map[0].validate(x, y, dt);
		i += quad_map[1].validate(x, y, dt);
		i += quad_map[2].validate(x, y, dt);
		i += quad_map[3].validate(x, y, dt);
		return i;
	}
	
	@Override
	public void render(Matrix4f transform, boolean far) {
		grass_tex.bind(0);
		shader.setUniform("grass_tex", 0);
		shader.setUniform("alpha", 1.0f);
		
		quad_map[0].render(transform, far);
		quad_map[1].render(transform, far);
		quad_map[2].render(transform, far);
		quad_map[3].render(transform, far);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("map_root: ").append(res).append('\n');
		quad_map[0].nodeString(sb, 1);
		quad_map[1].nodeString(sb, 1);
		quad_map[2].nodeString(sb, 1);
		quad_map[3].nodeString(sb, 1);
		return sb.toString();
	}
	
	
}
