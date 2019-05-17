package aaa;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class View {
	
	private Vector3f pos;
	private float rx, ry, rz;
	
	private double dir;
	private double piv;
	
	private Matrix4f view2projection;
	
	public View() {
		pos = new Vector3f();
	}
	
	public Vector3f pos() {
		return pos;
	}
	
	public Matrix4f aworld2Projection() {
		Matrix4f world2projection = new Matrix4f();
		view2projection.mul(world2view(), world2projection);
		return world2projection;
	}
	
	public Matrix4f aview2Projection() {
		return view2projection;
	}
	
	public Matrix4f world2view() {
		Matrix4f t0 = new Matrix4f();
		Matrix4f t1;
		double rz = sin(dir*2)*piv;
		double rx = cos(dir*2)*piv;
		rx = piv;
		t0 = new Matrix4f();
		
		// rotation_x
				t1 = new Matrix4f();
				t1.m11((float) cos(rx));
				t1.m12((float) sin(rx));
				t1.m21((float) -sin(rx));
				t1.m22((float) cos(rx));
				t0.mulAffine(t1);
	/*	t1 = new Matrix4f();
		t1.m00((float) cos(dir));
		t1.m02((float) -sin(dir));
		t1.m20((float) sin(dir));
		t1.m22((float) cos(dir));
		t0.mulAffine(t1);*/
				// rotation_y
				// rotation_z
				t1 = new Matrix4f();
				t1.m00((float) cos(dir));
				t1.m01((float) sin(dir));
				t1.m10((float) -sin(dir));
				t1.m11((float) cos(dir));
				t0.mulAffine(t1);

		
		
		// translate
		t1 = new Matrix4f();
		t1.m30(-pos.x);
		t1.m31(-pos.y);
		t1.m32(-pos.z);
		t0.mulAffine(t1);
		return t0;
	}
	
	public void move(double dx, double dy, double dz) {
		pos.x += dx * Math.cos(dir) + dy * Math.sin(dir); 
		pos.y += dy * Math.cos(dir) - dx * Math.sin(dir); 
		pos.z += dz;
	}
	
	/*public void rotate(double drx, double dry, double drz) {
		rx -= drx * Math.cos(ry);
		ry -= dry;
		rz -= drz * Math.sin(ry);
	}*/
	
	public void rotate(double a) {
		dir += a;
	}
	
	public void pivot(double a) {
		piv = Math.max(Math.min(piv+a, 0), -Math.PI);
	}
	
	public void z(float z) {
		pos.z = z;
	}
}