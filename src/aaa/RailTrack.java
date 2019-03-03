package aaa;

import java.util.ArrayList;
import java.util.LinkedList;

import org.joml.Matrix3f;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import gl.Shader;
import util.ModelUtils;
import util.Util;

public class RailTrack {

	private static final float RAIL_LEN = 13f;
	
	private float start_x;
	private float start_y;
	private float start_z;
	private float start_r;
	private float end_x;
	private float end_y;
	private float end_z;
	private float end_r;
	
	private ArrayList<Entity> tracks;
	
	public RailTrack(float x, float y, float r) {
		this.start_x = x;
		this.start_y = y;
		this.start_r = r;
		this.end_x = x;
		this.end_y = y;
		this.end_r = r;
		start_z = (float)Hydraglider.terrain_height.get(start_x, start_y);
		end_z = start_z;
		tracks = new ArrayList<Entity>();
	}
	
	public void add(double dr) {
		Entity e = new Entity();
		e.addModels(ModelUtils.get("rail"));
		
		end_r += dr;
		float dx = RAIL_LEN * (float)-Math.sin(end_r);
		float dy = RAIL_LEN * (float)Math.cos(end_r);
		 
		float pos_x = end_x + dx;
		float pos_y = end_y + dy;
		
		float new_end_x = pos_x + dx;
		float new_end_y = pos_y + dy;
		float new_end_z = (float)Hydraglider.terrain_height.get(new_end_x, new_end_y);
		
		float rise = (float)Math.atan((new_end_z - end_z) / RAIL_LEN * 0.5);
		
		e.translate(pos_x, pos_y, 0.5f * (end_z + new_end_z));
		e.rotate_y(-rise);
		e.rotate_z(end_r + (float)Util.tau * 0.25f); // need pre +dr rotation at start, and like this and end of rail, to make curve
		
		e.scale(0.001f);
		e.setShader(Shader.phong);
		tracks.add(e);
		
		end_x = new_end_x;
		end_y = new_end_y;
		end_z = new_end_z;
	}
	
	public Matrix3x2f getPosRot(float p) {
		int i = (int)p;
		float f;
		if (i < 0) {
			i = 0;
			f = 0;
		} else if (i >= tracks.size()) {
			i = tracks.size() - 1;
			f = 1;
		} else {
			f = (p-i)*2-1f; 
		}
		
		Vector3f pos = tracks.get(i).getTranslation();
		Vector3f rot = tracks.get(i).getRotation();
		Vector3f d = new Vector3f(RAIL_LEN * (float)Math.cos(rot.z), RAIL_LEN * (float)Math.sin(rot.z), (float)Math.tan(-rot.y)*RAIL_LEN);
		pos.add(d.mul(f));
		return new Matrix3x2f(pos.x, rot.x, pos.y, rot.y, pos.z, rot.z);
	}
	
	public void getRot(float p) {
		
	}
	
	public void render(Matrix4f transform) {
		for (Entity e : tracks) {
			e.render(transform);
		}
	}
	
	
}
