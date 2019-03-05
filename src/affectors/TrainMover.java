package affectors;

import java.util.Iterator;

import org.joml.Matrix3x2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import aaa.Entity;
import aaa.Hydra;
import aaa.Railway;
import aaa.Train;
import core.SKeyboard;

public class TrainMover extends Affector {
	private Railway rail;
	private float speed;
	private float pos;
	
	private Train train;
	
	public TrainMover(Train train, Railway rail, float pos, float speed) {
		super();
		this.train = train;
		this.rail = rail;
		this.pos = pos;
		this.speed = speed / 26;
	}
	
	public void modifySpeed(double ds) {
		speed += ds / 26;
	}
	
	@Override
	protected void pre_update(double dt) {
		
	}
	
	@Override
	protected void update_internal(double dt, Entity x) {
		this.pos += speed * dt;
		
		Iterator<Entity> e_itr = train.entity_itr();
		Iterator<Float> p_itr = train.dpos_itr();
		Iterator<Float> w_itr = train.weight_itr();
		
		float pos = this.pos;
		
		while (e_itr.hasNext()) {
			Entity e = e_itr.next();
			pos -= p_itr.next();
			float w = w_itr.next();
			Matrix3x2f stuff = rail.getPosRot(pos);
			e.setTranslation(stuff.m00, stuff.m10, stuff.m20);
			e.setRotation(stuff.m01, stuff.m11, stuff.m21);
			float f = (float) (w * Hydra.g * Math.sin(stuff.m11));
			if (SKeyboard.isPressed(GLFW.GLFW_KEY_P)) {
				f+=10000;
			}
			speed += dt * f / train.mass;
		}
		
		
		
	}
}
