package terrain;

import java.util.Iterator;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import aaa.Entity;
import aaa.Hydraglider;
import core.SKeyboard;
import gl.Material;
import gl.Model;
import util.Potato;

public class QuadMapLeaf extends QuadMap {

	private Entity terrain;
	private Potato potato;
	
	private QuadMapBranch split;
	private QuadMapMessage message;
	
	private int id;
	
	
	
	
	public QuadMapLeaf(QuadMapBranch parent, int position, double x0, double y0, int res) {
		super(parent, position, x0, y0, res);
		this.position = position;
		terrain = Entity.NULL;
		message = new QuadMapMessage(this, x0, y0, res);
		Hydraglider.addQMMessage(message);
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
		Hydraglider.removeQMMessage(message);
		if (split != null) {
			split.interrupt();
			split.delete();
			split = null;
		}
	}

	public void delete() {
		if (terrain == Entity.NULL) {
			
		} else {
			for (Model m : terrain.getModels()) {
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
			baked_potato.addModels(potato.bake());
			baked_potato.setMaterialOverride(Material.NULL);
			baked_potato.setShader(Hydraglider.terrain_shader);
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
				} else {
					split.validate(x, y, dt);
					if (split.ready_counter == 4) {
						parent.quad_map[position] = split;
						split = null;
						delete();
					}
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
		if (res < 2 && SKeyboard.isPressed(GLFW.GLFW_KEY_X)) {

			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		}
		if (res >= FAR_LIMIT == far) {
			float alpha = 1;
			shader.setUniform("alpha", alpha);
			terrain.render(transform);
		}
		if (res < 2) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
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