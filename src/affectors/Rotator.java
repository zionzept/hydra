package affectors;

import org.joml.Vector3f;

import aaa.Entity;

public class Rotator extends Affector {
	
	private Vector3f rotation_vector;
	
	public Rotator(Vector3f rotation) {
		super();
		this.rotation_vector = rotation;
		this.rotation = new Vector3f();
	}
	
	private Vector3f rotation;
	
	@Override
	protected void pre_update(double dt) {
		rotation_vector.mul((float)dt, rotation);
	}
	
	@Override
	protected void update_internal(double dt, Entity e) {
		e.rotate(rotation.x, rotation.y, rotation.z);
	}
}