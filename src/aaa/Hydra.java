package aaa;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import affectors.Affector;
import affectors.RailMover;
import affectors.Rotator;
import affectors.TimedLife;
import affectors.TrainMover;
import affectors.VectorMover;
import attributes.Enabler;
import core.SKeyListener;
import core.SKeyboard;
import core.SMouse;
import core.SMouseMoveListener;
import core.Window;
import gl.Material;
import gl.Mesh;
import gl.Model;
import gl.Shader;
import gl.Texture;
import meff.BiomeGen;
import scripts.Script;
import terrain.QuadMapMessage;
import terrain.QuadMap;
import terrain.QuadMapBranch;
import terrain.QuadMapRoot;
import util.ModelUtils;
import util.Potato;

import static util.Util.*;

public class Hydra implements SKeyListener, SMouseMoveListener {
	public static void main(String[] args) {
		//GlobalEventManager manager = new GlobalEventManager();
		Window.create(new Hydra());
		//manager.close();
	}
	
	public static double g = 8.0;
	
	private static float fov = (float)(80d / 180d * Math.PI);
	
	private static final boolean DEVPANEL = true;
	private static Devpanel devpanel;
	
	private static final int RAY_MARCH_MAX = 2000;
	private static final float PLAYER_HEIGHT = 1.58f;
	
	public static View view;
	public static Shader terrain_shader;
	
	private static LinkedList<Entity> entities;
	private static LinkedList<Affector> affectors;
	private static LinkedList<Enabler> enablers;
	private static LinkedList<Script> scripts;
	private static LinkedList<Script> script_queue;
	
	
	private ReentrantLock mouse_lock;
	private double mouse_dx;
	private double mouse_dy;
	Vector3f mouse_pos;
	Vector3f mouse_vec;
	
	private static ReentrantLock entity_lock;
	private static ReentrantLock affector_lock;
	private static ReentrantLock enabler_lock;
	private static ReentrantLock script_lock;
	
	
	private Vector3f light_direction;
	private Vector4f light_color;
	private Vector4f ambient_color;
	private Vector4f distant_color;
	
	private static LinkedList<Runnable> tasks;
	private static ReentrantLock task_lock;
	
	public static float view_distance;
	
	public static BiomeGen terrain_height;
	
	private Vector3f spawn;
	
	public LinkedList<Integer> sector_task_list;
	public LinkedList<Potato> sector_potato_list;
	public LinkedList<Integer> sector_potato_secrets;
	
	private Entity water;
	private Entity ball;
	
	private static QuadMapBranch qm_terrain;
	private static PriorityQueue<QuadMapMessage> qm_messages;
	private static ReentrantLock qm_message_lock;
	
	private Railway rail;
	private Shader rail_shader;
	
	public static double turn_bias;
	public static double rise_bias;
	public static double swirl_bias;
	
	private Matrix4f near_projection;
	private Matrix4f far_projection;
	private Matrix4f view2projection;
	private Matrix4f world2projection;
	
	private Hydra() {
		SKeyboard.addKeyListener(this);
		SMouse.addMouseMoveListener(this);
		mouse_lock = new ReentrantLock();
		
		entities = new LinkedList<Entity>();
		entity_lock = new ReentrantLock();
		
		affectors = new LinkedList<Affector>();
		affector_lock = new ReentrantLock();
		
		enablers = new LinkedList<Enabler>();
		enabler_lock = new ReentrantLock();
		
		scripts = new LinkedList<Script>();
		script_queue = new LinkedList<Script>();
		script_lock = new ReentrantLock();
		
		light_direction = new Vector3f(0.0f, 0.0f, 1f).normalize();
		light_color = new Vector4f(1.0f, .0f, .0f, 1.0f);
		ambient_color = new Vector4f();
		
		tasks = new LinkedList<Runnable>();
		task_lock = new ReentrantLock();
		
		view_distance = 10000000f;
		
		sector_task_list = new LinkedList<Integer>();
		sector_potato_list = new LinkedList<Potato>();
		sector_potato_secrets = new LinkedList<Integer>();
		
		qm_messages = new PriorityQueue<QuadMapMessage>();
		qm_message_lock = new ReentrantLock();
	}
	
	
	public void init() {
		setAmbience(0.0f, 0.01f, 0.02f);
		//ModelUtils.init();
		TEST.pre();
		//Model grid = 
		
		Shader.phong = new Shader("phong_shader", "phong", "phong");
		Shader.shaders.add(Shader.phong);
		Shader.wood = new Shader("wood_shader", "phong", "wood");
		Shader.shaders.add(Shader.wood);
		terrain_shader = new Shader("terrain_shader", "terrain", "terrain");
		Shader.shaders.add(terrain_shader);
		rail_shader = new Shader("rail_shader", "rails", "phong");
		Shader.shaders.add(rail_shader);
	
		// Get the window size passed to glfwCreateWindow
		view = new View();
		//view = new View((float)(80d / 180d * Math.PI), (float)Window.w / Window.h, 0.01f, 1000000f);
		view.pivot(-Math.PI*0.5);
		
		float ar = (float)Window.w / Window.h;
		near_projection = new Matrix4f().perspective(fov, ar, 1f/64f, 1024f*4f);
	//	near_projection = new Matrix4f().perspective(fov, ar, 1f, 1024f*4f);
		far_projection = new Matrix4f().perspective(fov, ar, 1024f, 1024f*1024f*1024f);
		view2projection = near_projection;
		world2projection = new Matrix4f();
		Runnable setUniforms = new Runnable() {
			@Override
			public void run() {
				Shader.phong.setUniform("view", world2projection);
				Shader.phong.setUniform("view_pos", view.pos());
				Shader.phong.setUniform("light_direction", light_direction);
				Shader.phong.setUniform("light_color", light_color);
				Shader.phong.setUniform("ambient_color", ambient_color);
				Shader.phong.setUniform("distant_color", distant_color);
				Shader.phong.setUniform("view_distance", view_distance);
			}
		};
		Shader.phong.setUniforms(setUniforms);
		
		setUniforms = new Runnable() {
			@Override
			public void run() {
				Shader.wood.setUniform("view", world2projection);
				Shader.wood.setUniform("view_pos", view.pos());
				Shader.wood.setUniform("light_direction", light_direction);
				Shader.wood.setUniform("light_color", light_color);
				Shader.wood.setUniform("ambient_color", ambient_color);
				Shader.wood.setUniform("distant_color", distant_color);
				Shader.wood.setUniform("view_distance", view_distance);
			}
		};
		Shader.wood.setUniforms(setUniforms);
		
		setUniforms = new Runnable() {
			@Override
			public void run() {
				terrain_shader.setUniform("view", world2projection);
				terrain_shader.setUniform("view_pos", view.pos());
				terrain_shader.setUniform("light_direction", light_direction);
				terrain_shader.setUniform("light_color", light_color);
				terrain_shader.setUniform("ambient_color", ambient_color);
				terrain_shader.setUniform("distant_color", distant_color);
				terrain_shader.setUniform("view_distance", view_distance);
			}
		};
		terrain_shader.setUniforms(setUniforms);
		
		QuadMap.shader = terrain_shader;
		QuadMap.grass_tex = new Texture("grass.png");
		
		
		setUniforms = new Runnable() {
			@Override
			public void run() {
				rail_shader.setUniform("view", world2projection);
				rail_shader.setUniform("view_pos", view.pos());
				rail_shader.setUniform("light_direction", light_direction);
				rail_shader.setUniform("light_color", light_color);
				rail_shader.setUniform("ambient_color", ambient_color);
				rail_shader.setUniform("distant_color", distant_color);
				rail_shader.setUniform("view_distance", view_distance);
			}
		};
		rail_shader.setUniforms(setUniforms);
		/*
		terrain_height = new F2Da();
		Random random = new Random(2999);
		for (int i = 1; i <= 80; i++) {
			float sz = 0.5f + 0.5f * random.nextFloat();
			float size = (float)Math.pow(3 + sz * i, 1.2);
			float vibrance = 0.02f + random.nextFloat()*0.5f/(float)Math.sqrt(size);
			float direction = (float)Math.PI * random.nextFloat();
			float sharpness = 1 + random.nextFloat() * (float)Math.sqrt(size);
			terrain_height.addSin(size, 1/size*(float)Math.cos(direction)*vibrance, 1/size*(float)Math.sin(direction)*vibrance, sharpness);
		}
		*/
		
		/*
		Noise2D noise = new Noise2D();
		Random random = new Random(987123);
		for (int i = 1; i <= 50; i++) {
			double size = 20 + Math.pow(1 + random.nextDouble() * i * 20, 1.8);
			double amp = 0.2 + 0.2*random.nextDouble();
			noise.addComponent(size, amp, random.nextDouble());
		}
		terrain_height = noise;
		*/
		
		terrain_height = new BiomeGen();
		
		
		
		int size = 20; // 24
		qm_terrain = new QuadMapRoot(0, -0.5*QuadMap.POLYS*(1<<size), -0.5*QuadMap.POLYS*(1<<size), size);
		
	
		
		spawn = new Vector3f(0, 0, (float)terrain_height.get(0, 0) + 2f);

		view.move(spawn.x, spawn.y, spawn.z);
		
		Entity e;
		Affector a;
		
		for (int i = 0; i < 1; i++) {
			e = new Entity();
			e.setShader(Shader.wood);
			e.addMesh(ModelUtils.get("cube").getFirst(), Material.store.get("plain"));
			e.translate(10, 10, (float)terrain_height.get(0,  10)+2 + 2*i);
			e.scale(1,1,0.2f);
			entities.add(e);
		}
		
		e = new Entity();
		e.addMesh(ModelUtils.ball(80, 80).getFirst(), Material.store.get("plain"));
		e.setShader(Shader.wood);
		e.translate(0, 10, (float)terrain_height.get(0, 10) + 20);
		e.scale(0.1f);
		entities.add(e);
		ball = e;		
	//	EntityFactory.createStoneTower(0, 0);
		EntityFactory.createSpawner(10, 200, 0);
		
		/*
		for (int x = 0; x < 800; x+= 20) {
			for (int y= 0; y < 400; y+= 20) {
				e = new Entity();
				e.addMeshes(ModelUtils.get("building_a", (int)(Math.random()*14)+1), null);
				e.scale(1f);
				e.translate(x, y, (float)terrain_height.get(x, y));
				e.setShader(Shader.wood);
				entities.add(e);
				System.out.println(x + " " + y);
				if (y%3 == 0) y+=20;
			}
			if (x%3 == 0) x+=20;
		}
		*/
		
		e = new Entity();
		Mesh m = ModelUtils.get("khalifa").getFirst();
		System.out.println(m.toString());
		e.addMesh(ModelUtils.get("khalifa").getFirst(), Material.store.get("plain"));
		e.translate(400, 1100, (float)terrain_height.get(400, 1100));
		e.setShader(Shader.wood);
		entities.add(e);
		
		//System.out.println("railing");
		//rail = new Railway(10, 10, 0, rail_shader);
		/*for (int i = 0; i < 50; i++) {			
			rail.add(0);
		}*/
		//rail.add(0.01, 0.01);
		
		// air rails
	//	for (int dturn = -1; dturn <= 1; dturn++) {
	//		for (int drise = -1; drise <= 1; drise++) {
	//			for (int srise = -1; srise <= 1; srise++) {
	//				for (int sswirl = -1; sswirl <= 1; sswirl++) {
	//					rail.addtp(drise*40, sswirl*40, srise*40 + 150, 0, srise*mu/2, sswirl*mu/2, dturn*mu/4, drise*mu/4);					
	//				}
	//			}
	//		}
	//	}
		
		
		
		/*
		rail.add(0.1, tau/4);
		rail.add(0.1, -tau/4);
		rail.add(-0.1, -tau/4);
		rail.add(-0.1, tau/4);
		for (int i = 0; i < 2; i++) {
			rail.add(tau/2, mu*(i+1));
			rail.add(-tau/2, -mu*(i+1));
		}
		for (int i = 0; i < 10; i++) {
			rail.add(tau/4, 0.1);
		}
		rail.add(0.1, tau/4);
		*/
		//rail.add(0.01, 0.01);
		/*
		for (int j = 0; j < ; j++) {
			for (int i = 0; i < 4; i++) {
				rail.add(Math.random()*2);
			}
			for (int i = 0; i < 4; i++) {
				rail.add(Math.random()*2);
			}
		}
		*/
		/*
		rail.add(0.1, tau/4);
		rail.add(tau/4, 0);
		for (int j = 0; j < 5; j++) {
			for (int i = 0; i < 4; i++) {
				rail.add(0.2, tau/8);
			}
			for (int i = 0; i < 4; i++) {
				rail.add(-0.2, tau/8);
			}
		}
		*/
		/*
		for (int i = 0; i < 20; i++) {
			rail.add(Math.random()*2-1);
		}
		for (int i = 0; i < 60; i++) {
			rail.add(2);
			rail.add(-2);
		}
		for (int j = 0; j < 30; j++) {
			for (int i = 0; i < 2; i++) {
				rail.add(0.2, tau/4);
			}
			for (int i = 0; i < 2; i++) {
				rail.add(-0.2, tau/4);
			}
		}
		for (int i = 0; i < 100; i++) {
			rail.add(Math.random()*2-1);
		}
		for (int i = 0; i < 300; i++) {
			rail.add(0);
		}
		*/
		
		/*
		e = new Entity();
		e.addModels(ModelUtils.get("locomotive"));
		e.setShader(Shader.phong);
		a = new RailMover(rail, 1, 0.1f, 0, 0);
		a.applyTo(e);
		affectors.add(a);
		entities.add(e);
		*/
		
		
		// TRAIN
		/*
		e = new Entity();
		e.addModels(ModelUtils.get("locomotive"));
		e.setShader(Shader.phong);
		Train t = new Train(e, 5000);
		a = new TrainMover(t, rail, 0, 0.1f); // Todo: why is t here when it's on next line
		a.applyTo(t);
		affectors.add(a);
		entities.add(t);
		
		for (int i = 0; i < 2; i++) {
			e = new Entity();
			e.addModels(ModelUtils.get("locomotive"));
			e.setShader(Shader.phong);
			t.add(e, 5000, 0.5f);
		}
		
		*/
		
		/*
		e = new Entity();
		e.addModels(ModelUtils.grid_xy(100, 100));
		e.scale(1);
		e.setShader(Shader.phong); // water
		entities.add(e);
		water = e;
		*/
		
		for (int i = 0; i < 10; i++) {
			spawnSectorTasker();			
		}
		
		if (DEVPANEL) {
			devpanel = new Devpanel();
		}
		
		lt = System.nanoTime();
		
		
	}
	
	private long lt;
	private double dt() {
		long ct = System.nanoTime();
		long dt = ct-lt;
		lt = ct;
		return dt / 1E9;
	}
	
	private boolean onGround() {
		Vector3f view_pos = view.pos();
		float z = (float)terrain_height.get(view_pos.x(), view_pos.y()) + PLAYER_HEIGHT + 0.01f;
		return view_pos.z() < z;
	}
	
	float mz = 30f;
	
	double move_speed = 10;
	private double report_time = 1;
	private double t = 0;
	private double z_speed = 0;
	final double gravity = 2;
	
	public void update() {
		// time
		
		double dt = dt();
		report_time -= dt;
		t += dt;
		
		
		// tasks
		
		task_lock.lock();
		while (!tasks.isEmpty()) {
			Runnable task = tasks.removeFirst();
			task.run();
		}
		task_lock.unlock();
		
		
		// movement
		
		
		boolean ground = onGround();
		Vector3f old_xy = view.pos();
		if (SKeyboard.isPressed(GLFW_KEY_W))
			view.move(0, move_speed*dt, 0);
		if (SKeyboard.isPressed(GLFW_KEY_S))
			view.move(0, -move_speed*dt, 0);
		if (SKeyboard.isPressed(GLFW_KEY_A))
			view.move(-move_speed*dt, 0, 0);
		if (SKeyboard.isPressed(GLFW_KEY_D))
			view.move(move_speed*dt, 0, 0);
		Vector3f new_xy = view.pos();
		if (ground) {
			float dz = (float)(terrain_height.get(new_xy.x(), new_xy.y()) - terrain_height.get(old_xy.x(), old_xy.y()));
	//		view.move(0, 0, dz);
		}
		if (SKeyboard.isPressed(GLFW_KEY_Q))
			view.move(0, 0, move_speed*dt);
		if (SKeyboard.isPressed(GLFW_KEY_Z))
			view.move(0, 0, -move_speed*dt);
		if (SKeyboard.isPressed(GLFW_KEY_R))
			mz *= 0.5;
		if (SKeyboard.isPressed(GLFW_KEY_T))
			mz /= 0.5;
		mouse_lock.lock();
		view.rotate(mouse_dx * 0.001);
		view.pivot(mouse_dy * 0.001);
		mouse_dx = 0;
		mouse_dy = 0;
		mouse_lock.unlock();
		//z_speed -= gravity*dt;
		view.move(0, 0, z_speed);
		Vector3f view_pos = view.pos();
		float z = (float)terrain_height.get(view_pos.x(), view_pos.y()) + PLAYER_HEIGHT;
		if (view_pos.z() < z) {
	//		view.z(z);
			z_speed = 0;
		}
		
	
		
		StringBuilder sb = new StringBuilder();
		
		// create world2projection
		view2projection = near_projection;
		view2projection.mul(view.world2view(), world2projection);

		// create projection2mouse?
		double mx = SMouse.getX() / (Window.w-1) * 2 - 1;
		double my = SMouse.getY() / (Window.h-1) * -2 + 1;
		Matrix4f projection2mouse = new Matrix4f();
		projection2mouse.m30((float)mx);
		projection2mouse.m31((float)my);
		projection2mouse.m32(mz); // mz probably should be a set small value and use mouse_vec to get desired point at certain distance
		
		Matrix4f world2mouse = new Matrix4f();
		projection2mouse.mul(world2projection, world2mouse);
		Matrix4f mouse2world = new Matrix4f();
		world2mouse.invert(mouse2world);
		
		// correct for w element
		float scale = 1 / mouse2world.m33();
		mouse2world.m00(mouse2world.m00()*scale);
		mouse2world.m01(mouse2world.m01()*scale);
		mouse2world.m02(mouse2world.m02()*scale);
		mouse2world.m03(mouse2world.m03()*scale);
		mouse2world.m10(mouse2world.m10()*scale);
		mouse2world.m11(mouse2world.m11()*scale);
		mouse2world.m12(mouse2world.m12()*scale);
		mouse2world.m13(mouse2world.m13()*scale);
		mouse2world.m20(mouse2world.m20()*scale);
		mouse2world.m21(mouse2world.m21()*scale);
		mouse2world.m22(mouse2world.m22()*scale);
		mouse2world.m23(mouse2world.m23()*scale);
		mouse2world.m30(mouse2world.m30()*scale);
		mouse2world.m31(mouse2world.m31()*scale);
		mouse2world.m32(mouse2world.m32()*scale);
		mouse2world.m33(mouse2world.m33()*scale);
	
	//	mouse2world.m30(mouse2world.m30() * mz);
	//	mouse2world.m31(mouse2world.m31() * mz);
	//	mouse2world.m32((float)-mz);
	//	mouse2world.m33(1);
		sb.append("mx: " + mx + '\n');
		sb.append("my: " + my + '\n');
		sb.append("mz: " + mz + "\n\n");
		sb.append("mouse_world matrix:\n");
		sb.append(mouse2world).append('\n');
		
		mouse_pos = new Vector3f(mouse2world.m30(), mouse2world.m31(), mouse2world.m32());
		//mouse_pos.div(32.0f);
		mouse_vec = new Vector3f(mouse_pos).sub(view.pos());
		float mouse_dist = mouse_vec.length();
		mouse_vec.normalize();
		
		sb.append("mouse_pos: " + mouse_pos.toString() + '\n');
		sb.append("mouse_vec: " + mouse_vec.toString() + '\n');
		sb.append("mouse_len: " + mouse_dist);
	//	System.out.println(mouse_pos + "\t" + view.pos());
		
		//System.out.println(sb);
		
		
		
		
		
		
		/*
		// update mouse vectors
		view2projection = near_projection;
		view2projection.mul(view.world2view(), world2projection);
		
//		double mx = SMouse.getX() / (Window.w-1) * 2 - 1;
//		double my = SMouse.getY() / (Window.h-1) * -2 + 1;
		Matrix4f temp = new Matrix4f();
//		Matrix4f mouse2projection = new Matrix4f();
		mouse2projection.m30((float)mx);
		mouse2projection.m31((float)my);
		mouse2projection.m32(mz); // not needed?
		Matrix4f inverse = new Matrix4f();
		
		world2projection.invert(temp).mul(mouse2projection, inverse);
		inverse.m30(inverse.m30() * mz);
		inverse.m31(inverse.m31() * mz);
		inverse.m32((float)-mz);
		inverse.m33(1f);
		temp = view.world2view();
		temp.invert(temp).mul(inverse, inverse);
		mouse_pos = new Vector3f(inverse.m30(), inverse.m31(), inverse.m32());
		mouse_vec = new Vector3f(inverse.m30(), inverse.m31(), inverse.m32());
		mouse_vec.sub(view.pos());
		mouse_vec.normalize();
		SMouse.pos(mouse_pos);
		SMouse.vec(mouse_vec);
		*/

		
		
		// light
		float light_factor = 1f+0.0f*(float)Math.cos(t*0.1);
		//light_factor = 1;
		//light_color.set(light_factor*1.0f, light_factor*0.8f, light_factor*0.2f, 1f);
		//light_color.set(light_factor*(0.5f + 0.5f*(float)Math.cos(t*0.1)), light_factor*(0.5f + 0.5f*(float)Math.sin(t*0.1f)), 0.0f, 1f);
		//setAmbience(0.1f*light_factor, 0.2f*light_factor, 0.3f*light_factor);
		light_direction.set((float)Math.sin(t*0.1), (float)Math.cos(t*0.1),1).normalize();
		light_color.set(light_factor, light_factor, light_factor, 1f);
		if (view.pos().z < 0) {
			setAmbience(0.01f * light_factor, 0.08f * light_factor, 0.12f * light_factor);
		} else {
			setAmbience(0.3f * light_factor, 0.4f * light_factor, 0.5f * light_factor);		
//			setAmbience(0.00f * light_factor, 0.01f * light_factor, 0.02f * light_factor);	
		}
		
		//wut
		
		int qq = qm_terrain.validate(new_xy.x(), new_xy.y(), dt);
		
		//System.out.println(qm_terrain.toString());
		
		// mouse click
		
		if (SMouse.isPressed(GLFW_MOUSE_BUTTON_1)) {
			Vector3f cursor_pos = new Vector3f(mouse_pos);
			Vector3f step = new Vector3f(mouse_vec).mul(1f);
			int step_count = 0;
			while (cursor_pos.z > terrain_height.get(cursor_pos.x, cursor_pos.y) && step_count < RAY_MARCH_MAX) {
				cursor_pos.add(step);
				step_count++;
			}
			if (step_count < RAY_MARCH_MAX) {
				step.mul(-0.5f);
				for (int i = 0; i < 20; i++) {
					cursor_pos.add(step);
					while (cursor_pos.z < terrain_height.get(cursor_pos.x, cursor_pos.y)) {
						step.mul(0.5f);
						cursor_pos.add(step);
						i++;
						if (i >= 20) {
							break;
						}
					}
					step.mul(-0.5f);
					cursor_pos.add(step);
					while (cursor_pos.z > terrain_height.get(cursor_pos.x, cursor_pos.y)) {
						step.mul(0.5f);
						cursor_pos.add(step);
						i++;
						if (i >= 20) {
							break;
						}
					}
					step.mul(-0.5f);
				}
			} else {
				System.out.println("max");				
			}
			
			//entity.setTranslation(cursor_pos.x, cursor_pos.y, cursor_pos.z);		old cursor tracking	
		}
		
		
		
		
		if (SKeyboard.isPressed(GLFW_KEY_B)) {
			Entity e = new Entity();
			e.addMesh(ModelUtils.get("cube").getFirst(), null);
			e.scale(1f);
			e.setTranslation(mouse_pos.x(), mouse_pos.y(), mouse_pos.z());
			e.setShader(Shader.wood);
			entity_lock.lock();
			entities.add(e);
			entity_lock.unlock();
			
			VectorMover bt = new VectorMover(mouse_vec, 10f);
			bt.applyTo(e);
			TimedLife td = new TimedLife(5);
			td.applyTo(e);
			affector_lock.lock();
			affectors.add(bt);
			affectors.add(td);
			affector_lock.unlock();
		}
		
		
		
		
		
		
		// objects logic
		
		ball.scale(1.0001f);
		
		script_lock.lock();
		for (Iterator<Script> itr = scripts.iterator(); itr.hasNext();) {
			Script script = itr.next();
			if (script.evaluate()) {
				itr.remove();
			}
		}
		while (!script_queue.isEmpty()) {
			scripts.add(script_queue.removeFirst());
		}
		script_lock.unlock();
		
		affector_lock.lock();
		for (Iterator<Affector> itr = affectors.iterator(); itr.hasNext();) {
			Affector affector = itr.next();
			if (affector.isDead()) {
				itr.remove();
			} else {
				affector.update(dt);
			}
		}
		affector_lock.unlock();
		
		enabler_lock.lock();
		for (Iterator<Enabler> itr = enablers.iterator(); itr.hasNext();) {
			Enabler enabler = itr.next();
			if (enabler.isDead()) {
				itr.remove();
			} else {
				enabler.update(dt);
			}
		}
		enabler_lock.unlock();
		
		
		// rendering
		
		// terrain
		Matrix4f transform = new Matrix4f();
		view2projection = far_projection;
		view2projection.mul(view.world2view(), world2projection);
		qm_terrain.render(transform, true);
		glClear(GL_DEPTH_BUFFER_BIT);
		view2projection = near_projection;
		view2projection.mul(view.world2view(), world2projection);
		qm_terrain.render(transform, false);
		
		entity_lock.lock();
		for (Iterator<Entity> itr = entities.iterator(); itr.hasNext();) {
			Entity e = itr.next();
			if (e.isDead()) {
				itr.remove();
			} else {
				e.render(transform);
			}
		}
		entity_lock.unlock();
		
		
		//rail.render(transform);
		
		
		
		if (DEVPANEL) {
			devpanel.update();
			devpanel.update_monitor(sb.toString());
		}
		/*
		
		if (report_time < 0) {
			report_time += 1d;
			StringBuilder sb = new StringBuilder();
			sb.append("fps: ").append(Window.fps());
			sb.append("\n");
			System.out.println(sb.toString());
			
			LinkedList<Mesh> list = ModelUtils.get("khalifa");
			System.out.println(list.size() + " : " + list.getFirst().toString());
			
			
			
		}
		
		*/
		
		
	}
	
	public void close() {
		if (DEVPANEL) {
			devpanel.dispose();
		}
		ModelUtils.free();
	}
	
	public void setAmbience(float r, float g, float b) {
		ambient_color = new Vector4f(r, g, b, 1f);
		distant_color = new Vector4f(r*1.04f, g*1.04f, b*1.04f, 1f).min(new Vector4f(1f, 1f, 1f, 1f));
		glClearColor(r, g, b, 1f);
	}

	@Override
	public void keyPress(int key) {
		//System.out.println(key);
		switch (key) {
		case 334: //+
			move_speed *= 2f;
			break;
		case 333: //-
			move_speed *= 0.5f;
			break;
		case 32: //space
			if (onGround()) {
				z_speed += 20f;
			}
			break;
		case GLFW_KEY_R:
			if (SKeyboard.isPressed(341)) {
				Runnable task = new Runnable() {
					@Override
					public void run() {
						System.out.println("recompile");
						for (Shader shader : Shader.shaders) {
							shader.recompile();
						}						
					}
				};
				addTask(task);
			}
			break;
		}
	}

	@Override
	public void keyRelease(int key) {
		//System.out.println(" " + key);
	}

	
	@Override
	public void mouseMove(double x, double y) {
		if (SMouse.isPressed(1)) {
			mouse_lock.lock();
			mouse_dx += SMouse.getDX();
			mouse_dy += SMouse.getDY();
			mouse_lock.unlock();
		}
	}
	
	private void spawnSectorTasker() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Window.shutdown) {
					while (!qm_messages.isEmpty() && !Window.shutdown) {
						qm_message_lock.lock();
						if (qm_messages.isEmpty()) {
							qm_message_lock.unlock();
							break;
						}
						QuadMapMessage message = qm_messages.poll();
						qm_message_lock.unlock();
						Potato potato = createSector(message.x0, message.y0, message.res);
						message.target.potato(potato);
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
		}).start();
	}
	
	public static void addQMMessage(QuadMapMessage message) {
		qm_message_lock.lock();
		qm_messages.add(message);
		qm_message_lock.unlock();
	}
	
	public static void removeQMMessage(QuadMapMessage message) {
		qm_message_lock.lock();
		qm_messages.remove(message);
		qm_message_lock.unlock();
	}
	
	public static int QMMessageSize() {
		return qm_messages.size();
	}
	
	private Potato createSector(double x, double y, int r) {
		Potato terrain_potato = ModelUtils.createTerrainSection(x, y, QuadMap.POLYS, r, terrain_height);
		return terrain_potato;
	}
	
	public static void addEntity(Entity e) {
		entity_lock.lock();
		entities.add(e);
		entity_lock.unlock();
	}
	
	public static void addAffector(Affector a) {
		affector_lock.lock();
		affectors.add(a);
		affector_lock.unlock();
	}
	
	public static void addEnabler(Enabler e) {
		enabler_lock.lock();
		enablers.add(e);
		enabler_lock.unlock();
	}
	
	public static void addScript(Script s) {
		script_lock.lock();
		scripts.add(s);
		script_lock.unlock();
	}
	
	public static void queueScript(Script s) {
		script_lock.lock();
		script_queue.add(s);
		script_lock.unlock();
	}
	
	public static void removeScript(Script s) {
		script_lock.lock();
		scripts.remove(s);
		script_lock.unlock();
	}
	
	public static void addTask(Runnable t) {
		task_lock.lock();
		tasks.add(t);
		task_lock.unlock();
	}
}
