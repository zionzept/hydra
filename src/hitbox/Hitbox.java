package hitbox;

import org.joml.Vector3f;

import aaa.Entity;

public abstract class Hitbox {
	
	protected Entity entity;
	
	public Hitbox() {
		
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public boolean isDead() {
		return entity.isDead();
	}
	
	
	
	public abstract boolean intersects(Vector3f p, Vector3f d);
}
