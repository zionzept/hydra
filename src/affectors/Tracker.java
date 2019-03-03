package affectors;

import org.joml.Vector3f;

import aaa.Entity;

public class Tracker extends Affector {

	private Vector3f pos;
	
	public Tracker(Vector3f pos) {
		this.pos = pos;
	}
	
	@Override
	protected void pre_update(double dt) {
		
	}

	@Override
	protected void update_internal(double dt, Entity e) {
		e.setTranslation(pos.x, pos.y, pos.z);
	}

}
