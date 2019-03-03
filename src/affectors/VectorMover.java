package affectors;

import org.joml.Vector3f;

import aaa.Entity;

public class VectorMover extends Affector {
	
	private Vector3f direction;
	private float speed;
	
	public VectorMover(Vector3f direction, float speed) {
		super();
		this.direction = direction;
		this.speed = speed;
		this.translation = new Vector3f();
	}

	private Vector3f translation;
	
	@Override
	protected void pre_update(double dt) {
		direction.mul((float)dt * speed, translation);
	}
	
	@Override
	protected void update_internal(double dt, Entity e) {
		e.translate(translation.x, translation.y, translation.z);
	}

}
