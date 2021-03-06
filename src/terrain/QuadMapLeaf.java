package terrain;

import java.util.Iterator;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import aaa.Entity;
import aaa.Hydra;
import core.SKeyboard;
import gl.Material;
import gl.Mesh;
import gl.Model;
import gl.Shader;
import gl.TerrainSection;
import util.Potato;

public class QuadMapLeaf extends QuadMap {

	private Entity terrain;
	private Potato potato;
	
	private QuadMapBranch split;
	private QuadMapMessage message;
	
	private int id;
	
	private boolean in_bounds;
	
	
	public QuadMapLeaf(QuadMapBranch parent, int position, double x0, double y0, int res) {
		super(parent, position, x0, y0, res);
		this.position = position;
		terrain = Entity.NULL;
		message = new QuadMapMessage(this, x0, y0, res);
		Hydra.addQMMessage(message);
		id = genID();
	}
	
	private static int id_counter = 0;
	private static synchronized int genID() {
		return id_counter++;
	}
	
	public int getID() {
		return id;
	}
	
	public void interrupt() {
		Hydra.removeQMMessage(message);
		if (split != null) {
			split.interrupt();
			split.delete();
			split = null;
		}
	}

	public void delete() {
		if (terrain == Entity.NULL) {
			
		} else {
			for (Mesh m : terrain.getModels()) {
				m.free();
				QuadMap.modify_check_num(-1);
			}
		}
	}
	
	@Override
	public int validate(double x, double y, double dt) {
		if (potato != null) {
			Entity baked_potato = new Entity();
			QuadMap.modify_check_num(1);
			TerrainSection bp = potato.bake();
			baked_potato.addMesh(bp, Material.store.get("null"));
			baked_potato.setShader(shader);
			terrain = baked_potato;
			potato = null;
			parent.ready_counter++;
			ready = true;
		}
		if (ready) {
			if (split == null) {
				if (res > MAX_QUALITY && in_bounds(x, y)) {
					split = new QuadMapBranch(parent, position, x0, y0, res);
				}
			} else {
				if (out_of_bounds(x, y)) {
					interrupt();
					in_bounds = false;
				} else {
					split.validate(x, y, dt);
					if (split.ready_counter == 4) {
						parent.quad_map[position] = split;
						split = null;
						delete();
					}
					in_bounds = true;
				}
			}
		}
		return 1;
	}
	
	public void potato(Potato potato) {
		this.potato = potato;
	}
	
	@Override
	public void render(Matrix4f transform, boolean far) {
	//	System.out.println("Render terrain leaf: far = " + far);
		if (far) {
			terrain.render(transform);
		}
		if (!far && (res < FAR_LIMIT || in_bounds)) {
			if (res < 2 && SKeyboard.isPressed(GLFW.GLFW_KEY_X)) {
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			}
			if (res >= FAR_LIMIT == far) {
				terrain.render(transform);
			}
			if (res < 2) {
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			}
		}
	}
	
	@Override
	public void nodeString(StringBuilder sb, int depth) {
		for (int i = 0; i < depth*2; i++) {
			sb.append(' ');
		}
		sb.append("map_leaf: ").append(res).append('\n');
	}
	
	@Override
	public String toString() {
		return "quad_leaf_" + id;
	}
}