package affectors;

import org.joml.Vector3f;

import aaa.Entity;
import aaa.Hydra;

public class GroundMover extends Affector {
	
	private Vector3f target;
	private float speed;
	
	public GroundMover(Vector3f target, float speed) {
		super();
		this.target = target;
		this.speed = speed;
	}
	
	@Override
	protected void pre_update(double dt) {
	}
	
	@Override
	protected void update_internal(double dt, Entity e) {
		Vector3f pos = new Vector3f(e.x(), e.y(), e.z());
		Vector3f ground_target = new Vector3f(target.x, target.y, (float)Hydra.terrain_height.get(target.x, target.y));
		Vector3f direction = new Vector3f();
		ground_target.sub(pos, direction);
		if (direction.length() < dt*speed) {
			e.setTranslation(ground_target.x, ground_target.y, ground_target.z);
			return;
		}
		direction.normalize();
		e.translate((float)(direction.x*dt*speed), (float)(direction.y*dt*speed), 0);
		e.translate(0,  0,  (float)(Hydra.terrain_height.get(e.x(), e.y()) - Hydra.terrain_height.get(pos.x, pos.y)));
	}

}
