package aaa;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFrame;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import Meff.BiomeGen;
import affectors.Affector;
import affectors.RailMover;
import affectors.TimedLife;
import affectors.VectorMover;
import attributes.Enabler;
import core.SKeyListener;
import core.SKeyboard;
import core.SMouse;
import core.SMouseMoveListener;
import core.Window;
import gl.Material;
import gl.Shader;
import gl.Texture;
import scripts.Script;
import terrain.QuadMapMessage;
import terrain.QuadMap;
import terrain.QuadMapBranch;
import terrain.QuadMapRoot;
import util.ModelUtils;
import util.Potato;

import static util.Util.tau;

public class Hydra implements SKeyListener, SMouseMoveListener {
	public static void main(String[] args) {
		//GlobalEventManager manager = new GlobalEventManager();
		Window.create(new Hydra());
		//manager.close();
	}
	
	private static final boolean DEVPANEL = true;
	private static Devpanel devpanel;
	
	private static final int RAY_MARCH_MAX = 2000;
	private static final float PLAYER_HEIGHT = 1.58f;
	
	public static View view;
	private Shader shader;
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
	
	private LinkedList<Runnable> tasks;
	private ReentrantLock task_lock;
	
	public static float view_distance;
	
	public static BiomeGen terrain_height;
	
	private Vector3f spawn;
	
	public LinkedList<Integer> sector_task_list;
	public LinkedList<Potato> sector_potato_list;
	public LinkedList<Integer> sector_potato_secrets;
	
	private Entity water;
	
	private static QuadMapBranch qm_terrain;
	private static PriorityQueue<QuadMapMessage> qm_messages;
	private static ReentrantLock qm_message_lock;
	
	private Railway rail;
	
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
		light_color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
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
		setAmbience(0.0f, 0.2f, 0.3f);
		ModelUtils.init();
		TEST.pre();
		//Model grid = 
		
		Shader.phong = new Shader("default", "asd", "asd");
		shader = Shader.phong;
		terrain_shader = new Shader("terrain", "terrain", "terrain");
	
		// Get the window size passed to glfwCreateWindow
		float view_offset = 1000f;
		view = new View((float)(80d / 180d * Math.PI), (float)Window.w / Window.h, view_offset * 0.4f, view_offset * 40000000f);
		//view = new View((float)(80d / 180d * Math.PI), (float)Window.w / Window.h, 0.01f, 1000000f);
		view.pivot(-Math.PI*0.5);
		
		Runnable setUniforms = new Runnable() {
			@Override
			public void run() {
				shader.setUniform("view", view.world2Projection());
				shader.setUniform("view_pos", view.pos());
				shader.setUniform("sampler", 0);
				shader.setUniform("light_direction", light_direction);
				shader.setUniform("light_color", light_color);
				shader.setUniform("ambient_color", ambient_color);
				shader.setUniform("distant_color", distant_color);
				shader.setUniform("view_distance", view_distance);
			}
		};
		shader.setUniforms(setUniforms);
		
		setUniforms = new Runnable() {
			@Override
			public void run() {
				terrain_shader.setUniform("view", view.world2Projection());
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
		
		BiomeGen biome = new BiomeGen();
		
		terrain_height = biome;
		
		
		
		int size = 24;
		qm_terrain = new QuadMapRoot(0, -0.5*QuadMap.POLYS*(1<<size), -0.5*QuadMap.POLYS*(1<<size), size);
		
		/*terrain_height.addSin(2, 0.17, 0.01);
		terrain_height.addSin(2, 0.07, 0.007);
		terrain_height.addSin(3, 0.11, 0.021);
		terrain_height.addSin(1, 0.0, 0.21);
		terrain_height.addSin(0.219, 0.31, 0.11);
		terrain_height.addSin(0.1122234, 0.337, 0.92);
		terrain_height.addSin(0.053543, 0.89, 0.77);*/
		
		spawn = new Vector3f(0, 0, (float)terrain_height.get(0, 0) + 2f);

		view.move(spawn.x, spawn.y, spawn.z);
		
		Entity e;
		
		//Entities.createStoneTower(0, 0);
		EntityFactory.createSpawner(10, 200, 0);
		
		for (int x = 0; x < 800; x+= 20) {
			for (int y= 600; y < 1000; y+= 20) {
				e = new Entity();
				e.addModels(ModelUtils.get("building_a", (int)(Math.random()*14)+1));
				e.scale(1f);
				e.translate(x, y, (float)terrain_height.get(x, y));
				e.setShader(shader);
				entities.add(e);
				if (y%3 == 0) y+=20;
			}
			if (x%3 == 0) x+=20;
		}
		
		e = new Entity();
		e.addModels(ModelUtils.get("khalifa"));
		e.translate(400, 1100, (float)terrain_height.get(400, 1100));
		e.setShader(shader);
		entities.add(e);
		
		rail = new Railway(0, 0, -(float)tau*0.25f);
		rail.add(0);
		for (int i = 0; i < 20; i++) {
			rail.add(Math.random()*2-1);
		}
		rail.add(0, 0.1);
		rail.add(0, -0.1);
		for (int j = 0; j < 5; j++) {
			for (int i = 0; i < 10; i++) {
				rail.add(0.01, tau/20);
			}
			for (int i = 0; i < 10; i++) {
				rail.add(-0.01, tau/20);
			}
		}
		for (int i = 0; i < 20; i++) {
			rail.add(Math.random()*2-1);
		}
		
		
		ModelUtils.get("train").getFirst().setMaterial(Material.plain);
		e = new Entity();
		e.addModels(ModelUtils.get("train"));
		e.translate(0,  0.06f,  (float)terrain_height.get(0, 0.063f) + 0.215f);
		e.rotate_z((float)Math.PI);
		e.rotate_z((float)-Math.PI*0.5f);
		e.scale(0.0255f);
		e.setShader(shader);
		RailMover a = new RailMover(rail, 0, 26, (float)tau*0.25f, 0.215f);
		a.applyTo(e);
		affectors.add(a);
		entities.add(e);
		
		e = new Entity();
		e.addModels(ModelUtils.grid_xy(100, 100));
		e.scale(1);
		e.setShader(shader); // water
		entities.add(e);
		water = e;
		
		
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
	
	float mz = 2f;
	
	double move_speed = 100;
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
			view.move(0, 0, dz);
		}
		if (SKeyboard.isPressed(GLFW_KEY_Q))
			view.move(0, 0, move_speed*dt);
		if (SKeyboard.isPressed(GLFW_KEY_Z))
			view.move(0, 0, -move_speed*dt);
		if (SKeyboard.isPressed(GLFW_KEY_R))
			mz *= 0.99;
		if (SKeyboard.isPressed(GLFW_KEY_T))
			mz /= 0.99;
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
			view.z(z);
			z_speed = 0;
		}
		
		// update mouse vectors
		double mx = SMouse.getX() / (Window.w-1) * 2 - 1;
		double my = SMouse.getY() / (Window.h-1) * -2 + 1;
		Matrix4f temp = new Matrix4f();
		Matrix4f mouse2projection = new Matrix4f();
		mouse2projection.m30((float)mx);
		mouse2projection.m31((float)my);
		mouse2projection.m32(mz); // not needed?
		Matrix4f inverse = new Matrix4f();
		view.view2Projection().invert(temp).mul(mouse2projection, inverse);
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
		
		// light
		float light_factor = 0.52f+0.48f*(float)Math.cos(t*0.1);
		light_factor = 1;
		//light_color.set(light_factor*1.0f, light_factor*0.8f, light_factor*0.2f, 1f);
		//light_color.set(light_factor*(0.5f + 0.5f*(float)Math.cos(t*0.1)), light_factor*(0.5f + 0.5f*(float)Math.sin(t*0.1f)), 0.0f, 1f);
		//setAmbience(0.1f*light_factor, 0.2f*light_factor, 0.3f*light_factor);
		light_direction.set((float)Math.sin(t*0.1), (float)Math.cos(t*0.1),1).normalize();
		light_color.set(1.0f, 1.0f, 1.0f, 1f);
		if (view.pos().z < 0) {
			setAmbience(0.01f, 0.08f, 0.12f);
		} else {
			setAmbience(0.3f, 0.5f, 0.7f);			
		}
		
		
		//wut
		int qq = qm_terrain.validate(new_xy.x(), new_xy.y(), dt);
		//System.out.println(qm_terrain.toString());
		
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
			e.addModels(ModelUtils.get("cube"));
			e.scale(1f);
			e.setTranslation(mouse_pos.x(), mouse_pos.y(), mouse_pos.z());
			e.setShader(shader);
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
		
		Matrix4f transform = new Matrix4f();
		qm_terrain.render(transform, true);

		water.setTranslation(view.pos().x, view.pos().y, 0);
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
		
		rail.render(transform);
		
		if (report_time < 0) {
			report_time += 1d;
			StringBuilder sb = new StringBuilder();
			sb.append("position: " + view.pos());
			sb.append('\n');
			sb.append("entities: ").append(entities.size());
			sb.append("    ");
			sb.append("affectors: ").append(affectors.size());
			sb.append('\n');
			sb.append("terrain stats: " + qq + " : " + qm_messages.size() + " : " + QuadMap.get_check_num());
			sb.append('\n');
			double[] bd = terrain_height.getData(view.pos().x, view.pos().y);
			String s = "biome stats: (" + bd[1] + ", " + bd[3] + "), (" + bd[2] + ", " + bd[4] + ")";
			sb.append(s);
			sb.append("\n");
			System.out.println(sb.toString());
			
			if (DEVPANEL) {
				devpanel.update();
			}
		}
		
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
}
