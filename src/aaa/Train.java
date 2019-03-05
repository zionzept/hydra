package aaa;

import java.util.Iterator;
import java.util.LinkedList;

import org.joml.Matrix4f;

public class Train extends Entity {
	
	private LinkedList<Entity> train;
	private LinkedList<Float> dpos;
	private LinkedList<Float> masses;
	public float mass;
	
	public Train(Entity head, float mass) {
		super();
		train = new LinkedList<Entity>();
		train.add(head);
		this.mass = mass;
		masses = new LinkedList<Float>();
		masses.add(mass);
		this.dpos = new LinkedList<Float>();
		this.dpos.add(0f);
	}
	
	public void add(Entity e, float mass, float dpos) {
		train.add(e);
		this.mass += mass;
		masses.add(mass);
		this.dpos.add(dpos);
	}
	
	public Iterator<Entity> entity_itr() {
		return train.iterator();
	}
	
	public Iterator<Float> dpos_itr() {
		return dpos.iterator();
	}
	
	public Iterator<Float> weight_itr() {
		return masses.iterator();
	}
	
	@Override
	public void render(Matrix4f transform) {
		for (Entity e : train) {
			e.render(transform);
		}
	}
}
