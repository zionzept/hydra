package affectors;

import aaa.Entity;

public class TimedLife extends Affector {
	
	private double t;
	
	public TimedLife(double t) {
		this.t = t;
	}

	@Override
	protected void pre_update(double dt) {
		t -= dt;
	}

	@Override
	protected void update_internal(double dt, Entity e) {
		if (t < 0) {
			e.kill();
		}
	}
	
	
}
