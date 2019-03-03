package affectors;

import org.joml.Matrix3x2f;
import org.joml.Vector3f;

import aaa.Entity;
import aaa.Hydraglider;
import aaa.RailTrack;

public class RailMover extends Affector {
	private RailTrack rail;
	private float speed;
	private float pos;
	private float rot_bias;
	private float z_bias;
	
	public RailMover(RailTrack rail, float pos, float speed, float rot_bias, float z_bias) {
		super();
		this.rail = rail;
		this.pos = pos;
		this.speed = speed / 26;
		this.rot_bias = rot_bias;
		this.z_bias = z_bias;
	}
	
	public void modifySpeed(double ds) {
		speed += ds / 26;
	}
	
	@Override
	protected void pre_update(double dt) {
		
	}
	
	@Override
	protected void update_internal(double dt, Entity e) {
		this.pos += speed * dt;
		
		Matrix3x2f stuff = rail.getPosRot(this.pos);
		
		e.setTranslation(stuff.m00, stuff.m10, stuff.m20 + z_bias);
		e.setRotation(stuff.m11, stuff.m01, stuff.m21 + rot_bias); // rotation bias mess up rotation on x and y
	}
}
