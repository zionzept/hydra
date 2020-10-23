package aaa;

import java.util.Iterator;
import java.util.LinkedList;

import org.joml.Matrix4f;

import gl.Material;
import gl.Mesh;

public class EntityNode extends Entity {
	
	private LinkedList<Entity> children;
	
	public EntityNode() {
		super();
		children = new LinkedList<>();
	}
	
	public void addChild(Entity child) {
		children.add(child);
	}
	
	public LinkedList<Entity> getChildren() {
		return children;
	}
	
	public void render(Matrix4f transform) {
		Matrix4f new_transform = new Matrix4f();
		transform.mulAffine(getTransform(), new_transform);
		shader.bind();
		shader.setUniform("world", new_transform);
		
		Iterator<? extends Mesh> mesh_itr = meshes.iterator();
		Iterator<Material> mtl_itr = materials.iterator();
		while (mesh_itr.hasNext()) {
			mtl_itr.next().materialize(shader);
			mesh_itr.next().render();
		}
		
		for (Entity child : children) {
			child.render(new_transform);
		}
	}
}
