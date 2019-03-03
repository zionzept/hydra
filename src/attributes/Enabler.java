package attributes;

import aaa.Entity;
import hitbox.Hitbox;

public abstract class Enabler {
	
	protected Attribute attribute;
	protected Entity entity;
	protected Hitbox hitbox;
	protected boolean enabled;
	
	public Enabler(Attribute attribute, Entity entity, Hitbox hitbox) {
		this.attribute = attribute;
		this.entity = entity;
		this.hitbox = hitbox;
	}
	
	public abstract void update(double dt);
	public boolean isDead() {
		return entity.isDead();
	}
	
	
}
