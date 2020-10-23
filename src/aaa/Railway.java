package aaa;

import static util.Util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.joml.Matrix3f;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import gl.Material;
import gl.Shader;
import util.ModelUtils;
import util.Util;

public class Railway {

	private static final double RAIL_LEN = 26;
	
	private double start_x;
	private double start_y;
	private double start_z;
	private double start_a;
	private double end_x;
	private double end_y;
	private double end_z;
	private double end_turn;
	private double end_rise;
	private double end_swirl;
	
	private ArrayList<Entity> tracks;
	private ArrayList<Float> turns;
	private ArrayList<Float> rises;
	private ArrayList<Float> swirls;
	
	private Shader shader;
	
	public Railway(double x, double y, double a, Shader shader) {
		this.start_x = x;
		this.start_y = y;
		this.start_a = a;
		this.end_x = x;
		this.end_y = y;
		this.end_turn = a;
		start_z = Hydra.terrain_height.get(start_x, start_y);
		end_z = start_z;
		tracks = new ArrayList<Entity>();
		turns = new ArrayList<Float>();
		rises = new ArrayList<Float>();
		swirls = new ArrayList<Float>();
		this.shader = shader;
	}
	
	public void add(double da) {
		double rad = RAIL_LEN / da;
		double cur_a = end_turn + da/2;
		double xy_r_f = 2*rad*Math.sin(da/2) / RAIL_LEN;
		
		double len = RAIL_LEN * Math.cos(end_rise) * xy_r_f;
		double new_end_x = end_x + len * Math.cos(cur_a);
		double new_end_y = end_y + len * Math.sin(cur_a);
		double new_end_z = Hydra.terrain_height.get(new_end_x, new_end_y);
		int r = 0;
		double hypo = Double.POSITIVE_INFINITY;
		while (Math.abs((RAIL_LEN * xy_r_f / hypo) - 1) > 1E-9 && r < 50) {
			hypo = Math.hypot(len, new_end_z - end_z);
			len *= RAIL_LEN * xy_r_f / hypo;
			new_end_x = end_x + len * Math.cos(cur_a);
			new_end_y = end_y + len * Math.sin(cur_a);
			new_end_z = Hydra.terrain_height.get(new_end_x, new_end_y);
			r++;
		}
		
		end_rise = Math.atan((new_end_z - end_z) / len);
		
		
		/*
		end_r += dr;
		double len = RAIL_LEN * Math.cos(end_rise);
		double new_end_x = end_x + len * Math.cos(end_r);
		double new_end_y = end_y + len * Math.sin(end_r);
		double new_end_z = Hydra.terrain_height.get(new_end_x, new_end_y);
		int r = 0;
		double hypo = Double.POSITIVE_INFINITY;
		while (Math.abs((hypo / RAIL_LEN) - 1) > 1E-9 || r < 50) {
			hypo = Math.hypot(len, new_end_z - end_z);
			len *= RAIL_LEN / hypo;
			new_end_x = end_x + len * Math.cos(end_r);
			new_end_y = end_y + len * Math.sin(end_r);
			new_end_z = Hydra.terrain_height.get(new_end_x, new_end_y);
			r++;
		}
		end_rise = Math.atan((new_end_z - end_z) / len);
		end_x = new_end_x;
		end_y = new_end_y;
		end_z = new_end_z;
		*/
		
		Entity e = new Entity();
		e.addMesh(ModelUtils.get("rail").getFirst(), Material.store.get("plain"));
		e.translate((float)end_x, (float)end_y, (float)end_z);
		e.rotate_y((float)-end_rise);
		e.rotate_z((float)end_turn);
		e.setShader(shader);
		tracks.add(e);
		
		end_x = new_end_x;
		end_y = new_end_y;
		end_z = new_end_z;
		end_turn += da;
		turns.add((float)end_turn);
	}
	
	public void add(double ddir, double dryz) {
		
		double swirl_n = 1 - Math.sqrt(1 - Math.pow(((end_rise + mu) % pi - mu) / mu, 2));
		swirl_n = Math.sin(end_rise)*Math.sin(ddir);
		double turn_n = Math.cos(end_rise)*Math.cos(ddir);
		
		
		double dswirl = ddir * swirl_n;
		dswirl = 0;
		//ddir *= 1-swirl_n;
		
		double dturn = ddir * Math.cos(end_swirl) * Math.cos(end_rise) - dryz * Math.sin(end_swirl);
		double drise = ddir * Math.sin(end_swirl) * Math.cos(end_rise) + dryz * Math.cos(end_swirl);
		dswirl = ddir * Math.sin(end_rise);
		
		
		
		
		
		double turn_rad = RAIL_LEN / ddir;
		double rise_rad = RAIL_LEN / dryz;
		
		double rise_hypo = 2*rise_rad*Math.sin(dryz/2);
		double rise_v = rise_hypo * Math.cos(dryz/2);
		double rise_f = RAIL_LEN / rise_v;
		
		double fake_l = RAIL_LEN * rise_f;
		double nerf = Math.sqrt(RAIL_LEN / fake_l);
		
		double turn_f = 2*turn_rad*Math.sin(ddir/2) / RAIL_LEN;
		//double rise_f = 2*rise_rad*Math.sin(drise/2) / RAIL_LEN; //yes? assuming how this will work together with turn_f
		//double rad_f = Math.hypot(2*turn_rad*Math.sin(dturn/2), 2*rise_rad*Math.sin(drise/2)) / RAIL_LEN;
		System.out.println("drise: " + dryz);
		System.out.println("rise_rad: " + rise_rad);
		System.out.println("end_rise: " + end_rise + " swirl_f:" + swirl_n);
		System.out.println("rh:" + rise_hypo + " rv:" + rise_v + " rf:" + rise_f + " fl:" + fake_l + " nerf:" + nerf);
		System.out.println(turn_rad + " : " + rise_rad);
		//turn_rad *= rad_f * rad_f;
		//turn_rad /= turn_part;
		//rise_rad *= rad_f * rad_f;
		System.out.println(turn_rad + " : " + rise_rad);
		System.out.println();
		
		
		double cur_turn = end_turn + ddir/2;
		
		double cur_rise = end_rise + dryz/2;
		
		
		
		
		
		double new_end_x = end_x + Math.cos(cur_turn) * RAIL_LEN * Math.cos(end_rise) * nerf;
		double new_end_y = end_y + Math.sin(cur_turn) * RAIL_LEN * Math.cos(end_rise) * nerf;
		double new_end_z = end_z + Math.sin(cur_rise) * RAIL_LEN * nerf;
	
		
		/*
		Entity e = new Entity();
		e.addModels(ModelUtils.get("rail"));
		
		end_rise += drise;
		double dx = RAIL_LEN * (float)(Math.cos(end_a)*Math.cos(end_rise));
		double dy = RAIL_LEN * (float)(Math.sin(end_a)*Math.cos(end_rise));
		
		double new_end_x = end_x + dx;
		double new_end_y = end_y + dy;
		double new_end_z = end_z + (float)Math.sin(end_rise) * RAIL_LEN;
		*/
		
		Entity e = new Entity();
		e.addMesh(ModelUtils.get("rail").getFirst(), Material.store.get("plain"));
		e.translate((float)end_x, (float)end_y, (float)end_z);
		e.rotate_y((float)-end_rise);
		e.rotate_z((float)end_turn);
		e.rotate_x((float)end_swirl);
		e.setShader(shader);
		tracks.add(e);
		
		
		end_x = new_end_x;
		end_y = new_end_y;
		end_z = new_end_z;
		end_turn += ddir;
		end_rise += dryz;
		end_swirl += dswirl;
		turns.add((float)ddir*(float)turn_n);
		rises.add((float)dryz);
		swirls.add((float)dswirl*(float)swirl_n);
	}
	
	public void addtp(double x, double y, double z, double turn, double rise, double swirl, double dturn, double drise) {
		end_x = x;
		end_y = y;
		end_z = z;
		end_turn = turn;
		end_rise = rise;
		end_swirl = swirl;
		add(dturn, drise);
	}
	
	public Matrix3x2f getPosRot(float p) {
		int i = (int)p;
		float f;
		if (p < 0) {
			i = 0;
			f = 0;
		} else if (i >= tracks.size()) {
			i = tracks.size() - 1;
			f = 1;
		} else {
			f = p-i;
		}
		
		
		
		
		float turn_rad = 1;
		float aaa = f * (float)RAIL_LEN / turn_rad;
		Matrix4f transform = tracks.get(i).getTransform();
		Vector4f pos = new Vector4f(turn_rad * (float)Math.sin(aaa), turn_rad * (1-(float)Math.cos(aaa)), 0.0f, 1.0f);
		pos.mul(transform);
		
		float rise_s = tracks.get(i).getRotation().y;
		
		float rise_rad = 1;
		float b = f * (float)RAIL_LEN / rise_rad;
		Vector4f bent_v = new Vector4f(
				(float)Math.sin(b) * rise_rad,
				0.0f,
				(float)(1-Math.cos(b)) * rise_rad,
				1.0f
				);
		float a = bent_v.x / turn_rad;
		bent_v.set(
				turn_rad * (float)Math.sin(a),
				turn_rad * (float)(1-Math.cos(a)),
				bent_v.z,
				1.0f
				);
		bent_v.mul(transform);
		
		Vector3f rot = tracks.get(i).getRotation();
		rot.set(rot.x + (float)Math.sin(rise_s)*f*turns.get(i), rot.y - f*rises.get(i), rot.z + (float)Math.cos(rise_s)*f*turns.get(i));
		
		
		
		//pos.add(d.mul(f));
		return new Matrix3x2f(bent_v.x, rot.x, bent_v.y, rot.y, bent_v.z, rot.z);
	}
	
	public void render(Matrix4f transform) {
		Iterator<Entity> e_itr = tracks.iterator();
		Iterator<Float> turn_itr = turns.iterator();
		Iterator<Float> rise_itr = rises.iterator();
		Iterator<Float> swirl_itr = swirls.iterator();
		while (e_itr.hasNext()) {
			shader.setUniform("L", (float)RAIL_LEN);
			shader.setUniform("dturn", turn_itr.next() + (float)Hydra.turn_bias);
			shader.setUniform("drise", rise_itr.next() + (float)Hydra.rise_bias);
			shader.setUniform("dswirl", swirl_itr.next() + (float)Hydra.swirl_bias);
			e_itr.next().render(transform);			
		}
	}
	
	
}
