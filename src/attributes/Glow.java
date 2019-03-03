package attributes;

import org.joml.Vector3f;
import org.joml.Vector4f;

import aaa.Entity;
import gl.Material;
import gl.Model;

public class Glow implements Attribute {

	private Vector4f color;
	private Vector4f original;
	
	public Glow(Vector3f color) {
		this.color = new Vector4f(color.x, color.y, color.z, 1);
	}
	
	@Override
	public void enable(Entity e) {
		for (Model m : e.getModels()) {
			Material material = m.material();
			original = material.emission();
			material.emission(color);
		}
		for (Entity child : e.getChildren()) {
			enable(child);
		}
	}

	@Override
	public void disable(Entity e) {
		for (Model m : e.getModels()) {
			Material material = m.material();
			material.emission(original);
		}
		for (Entity child : e.getChildren()) {
			disable(child);
		}
	}


}
