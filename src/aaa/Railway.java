package aaa;

import static util.Util.*;
import java.util.ArrayList;
import java.util.LinkedList;

import org.joml.Matrix3f;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import gl.Shader;
import util.ModelUtils;
import util.Util;

public class Railway {

	private static final double RAIL_LEN = 26;
	
	private double start_x;
	private double start_y;
	private double start_z;
	private double start_r;
	private double end_x;
	private double end_y;
	private double end_z;
	private double end_r;
	private double end_rise;
	
	private ArrayList<Entity> tracks;
	
	public Railway(double x, double y, double r) {
		this.start_x = x;
		this.start_y = y;
		this.start_r = r;
		this.end_x = x;
		this.end_y = y;
		this.end_r = r;
		start_z = Hydra.terrain_height.get(start_x, start_y);
		end_z = start_z;
		tracks = new ArrayList<Entity>();
	}
	
	public void add(double dr) {
		Entity e = new Entity();
		e.addModels(ModelUtils.get("rail"));
		double new_end_x = 0;
		double new_end_y = 0;
		double new_end_z = 0;
		end_r += dr;
	
		double len = RAIL_LEN * Math.cos(end_rise);
		
		new_end_x = end_x + len * Math.cos(end_r);
		new_end_y = end_y + len * Math.sin(end_r);
		new_end_z = Hydra.terrain_height.get(new_end_x, new_end_y);
		
		
		int r = 0;
		double hypo = Double.POSITIVE_INFINITY;
		while (Math.abs((hypo / RAIL_LEN) - 1) > 1E-9 || r < 50) {
			hypo = Math.hypot(len, new_end_z - end_z);
			len *= RAIL_LEN / hypo;
			new_end_x = end_x + len * Math.cos(end_r);
			new_end_y = end_y + len * Math.sin(end_r);
			new_end_z = Hydra.terrain_height.get(new_end_x, new_end_y);
			r++;
			System.out.println(len / RAIL_LEN + "\t" + RAIL_LEN / hypo);
		}
		
		end_rise = Math.atan((new_end_z - end_z) / len);
		
		//end_rise = Math.atan((new_end_z - end_z) / len);
		
		e.translate((float)end_x, (float)end_y, (float)end_z);
		e.rotate_y((float)-end_rise);
		e.rotate_z((float)end_r); // need pre +dr rotation at start, and like this and end of rail, to make curve
		
		e.setShader(Shader.phong);
		tracks.add(e);
		
		end_x = new_end_x;
		end_y = new_end_y;
		end_z = new_end_z;
	}
	
	public void add(double dr, double drise) {
		Entity e = new Entity();
		e.addModels(ModelUtils.get("rail"));
		
		end_r += dr;
		end_rise += drise;
		double dx = RAIL_LEN * (float)(Math.cos(end_r)*Math.cos(end_rise));
		double dy = RAIL_LEN * (float)(Math.sin(end_r)*Math.cos(end_rise));
		
		double new_end_x = end_x + dx;
		double new_end_y = end_y + dy;
		double new_end_z = end_z + (float)Math.sin(end_rise) * RAIL_LEN;
		
		e.translate((float)end_x, (float)end_y, (float)end_z);
		e.rotate_y((float)-end_rise); // it's probably rotated the wrong way since - here
		e.rotate_z((float)end_r); // need pre +dr rotation at start, and like this and end of rail, to make curve
		
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
			f = p-i;
		}
		
		Vector3f pos = tracks.get(i).getTranslation();
		Vector3f rot = tracks.get(i).getRotation();
		Vector3f d = new Vector3f((float)(RAIL_LEN * Math.cos(rot.z)*Math.cos(rot.y)), (float)(RAIL_LEN * Math.sin(rot.z)*Math.cos(rot.y)), (float)(RAIL_LEN * Math.sin(-rot.y)));
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
