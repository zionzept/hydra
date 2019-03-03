package affectors;

import java.util.Iterator;
import java.util.LinkedList;

import aaa.Entity;

public abstract class Affector {
	
	protected LinkedList<Entity> entities;
	
	public Affector() {
		entities = new LinkedList<>();
	}
	
	public boolean isDead() {
		return entities.size() == 0;
	}
	
	public void kill() {
		entities = new LinkedList<>();
	}
	
	public void applyTo(Entity e) {
		entities.add(e);
	}
	
	public void update(double dt) {
		pre_update(dt);
		for (Iterator<Entity> itr = entities.iterator(); itr.hasNext();) {
			Entity e = itr.next();
			if (e.isDead()) {
				itr.remove();
			} else {
				update_internal(dt, e);
			}
		}
	}
	
	protected abstract void pre_update(double dt);
	protected abstract void update_internal(double dt, Entity e);
}