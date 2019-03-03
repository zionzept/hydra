package hitbox;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BoundingSphere extends Hitbox {

	private Matrix4f transform;
	private float radius;
	
	public BoundingSphere(Vector3f translation, float radius) {
		this.transform = new Matrix4f();
		this.transform.m30(translation.x);
		this.transform.m31(translation.y);
		this.transform.m32(translation.z);
		this.radius = radius;
	}
	
	@Override
	public boolean intersects(Vector3f p, Vector3f d) {
		Matrix4f sphere_transform = new Matrix4f();
		entity.getTransform().mul(transform, sphere_transform);
		Vector3f c = new Vector3f(sphere_transform.m30(), sphere_transform.m31(), sphere_transform.m32());
		Vector3f v = new Vector3f();
		c.sub(p, v);
		
		float dc = d.dot(v);
		if (dc <= 0) {
			return false; //sphere behind p, might wanna do another test here for c to p, as point test, but not sure yet
		}
		Vector3f entity_scaling = entity.getScaling();
		float scaling = Math.max(entity_scaling.x, Math.max(entity_scaling.y, entity_scaling.z));
		
		Vector3f pc = new Vector3f();
		d.div(d.length(), pc);
		d.mul(v.dot(pc), pc);
		p.add(pc, pc);
		float dist = c.sub(pc).length();
		return dist < radius * scaling;
	}
}
