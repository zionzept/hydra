package aaa;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import affectors.Affector;
import affectors.GroundMover;
import affectors.Rotator;
import affectors.TimedLife;
import affectors.Tracker;
import affectors.VectorMover;
import attributes.Attribute;
import attributes.ClickEnabler;
import attributes.Enabler;
import attributes.Glow;
import attributes.MouseEnabler;
import core.SMouse;
import gl.Material;
import gl.Shader;
import hitbox.BoundingSphere;
import hitbox.Hitbox;
import scripts.ClickCondition;
import scripts.Condition;
import scripts.Script;
import scripts.ScriptO;
import util.ModelUtils;

public class EntityFactory {
	
	public static void createStoneTower(double x, double y) {
		Entity e = new Entity();
		e.addModels(ModelUtils.get("stone_tower"));
		e.translate((float)x, (float)y, (float)Hydra.terrain_height.get(x, y));
		e.scale(2,2,10);
		e.setShader(Shader.phong);
		
		Hitbox hitbox = new BoundingSphere(new Vector3f(0, 0, 4), 5);
		hitbox.setEntity(e);
		
		Attribute glow = new Glow(new Vector3f(1f, 0f, 0f));
		Enabler enabler = new MouseEnabler(glow, e, hitbox);
		Hydra.addEnabler(enabler);
		
		ScriptO script = new ScriptO(new Runnable() {
			@Override
			public void run() {
				createStone(SMouse.pos());
			}
		});
		script.addCondition(new ClickCondition(hitbox));
		Hydra.addScript(script);
	
		
		Hydra.addEntity(e);
	}
	
	public static void createStone(Vector3f pos) {
		Entity e = new Entity();
		e.addModels(ModelUtils.get("sphere"));
		e.setTranslation(pos.x, pos.y, pos.z);
		e.scale(0.2f);
		e.setShader(Shader.phong);
		
		TimedLife tl = new TimedLife(30);
		tl.applyTo(e);
		Hydra.addAffector(tl);
		
		Tracker t = new Tracker(SMouse.pos());
		t.applyTo(e);
		Hydra.addAffector(t);
		
		ScriptO script = new ScriptO(new Runnable() {
			@Override
			public void run() {
				t.kill();
				VectorMover vm = new VectorMover(new Vector3f(SMouse.vec().x, SMouse.vec().y, SMouse.vec().z), 100);
				vm.applyTo(e);
				Hydra.addAffector(vm);
				ScriptO script = new ScriptO(new Runnable() {
					@Override
					public void run() {
						spawnGlowCactus(e.x(), e.y());
						e.kill();
					}
				});
				script.addCondition(new Condition() {
					@Override
					public boolean evaluate() {
						return e.z() < Hydra.terrain_height.get(e.x(), e.y());
					}
				});
				Hydra.queueScript(script);
			}
		});
		script.addCondition(new Condition() {
			@Override
			public boolean evaluate() {
				return !SMouse.isPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT);
			}
		});
		Hydra.queueScript(script);
		
		Hydra.addEntity(e);
	}
	
	public static void spawnGlowCactus(double x, double y) {
		boolean troll = false;
		if (Math.random() < 0.2) {
			troll = true;
		}
		Entity e = new Entity();
		e.addModels(ModelUtils.get("tree"));
		if (!troll) {
			e.setMaterialOverride(new Material(new Vector4f(0f,(float)Math.random(),0f, 1f), 
					new Vector4f((float)Math.random(),0f,(float)Math.random(), 1f), 10));
		} else {
			e.setMaterialOverride(new Material(new Vector4f(0f,(float)Math.random(),0f, 1f), 
					new Vector4f((float)Math.random(),1f,(float)Math.random(), 1f), 10));
		}
		e.setTranslation((float)x, (float)y, (float)Hydra.terrain_height.get(x, y) - 1f);
		e.scale((float)Math.random() + 2);
		e.rotate_z((float)Math.random()*2*(float)Math.PI);
		e.setShader(Shader.phong);
		
		if (troll) {
			ScriptO script = new ScriptO(new Runnable() {
				@Override
				public void run() {
					Affector gm = new GroundMover(Hydra.view.pos(), 100);
					gm.applyTo(e);
					Hydra.addAffector(gm);
				}
			});
			script.addCondition(new Condition() {
				@Override
				public boolean evaluate() {
					return Hydra.view.pos().distance(new Vector3f(e.x(), e.y(), e.z())) < 30;
				}
			});
			Hydra.queueScript(script);
		}
		
		Hydra.addEntity(e);
	}
	
	// type temporary solution
	public static void createSpawner(double x, double y, int type) {
		Entity spawner = new Entity();
		spawner.addModels(ModelUtils.get("spawner"));
		spawner.translate((float)x, (float)y, (float)Hydra.terrain_height.get(x, y));
		//e.scale(2,2,10);
		spawner.setShader(Shader.phong);
		
		Entity e = new Entity();;
		Runnable run = null;
		switch (type) {
		case 0:
			e.addModels(ModelUtils.get("sphere"));
			e.scale(0.2f);
			run = new Runnable() {
				@Override
				public void run() {
					createStone(SMouse.pos());
				}
			};
			break;
		}
		e.setTranslation(0, 0, 1.4f);
		e.setShader(Shader.phong);
		spawner.addChild(e);
		Rotator rotator = new Rotator(new Vector3f(0, 0, 0.3f));
		rotator.applyTo(e);
		Hydra.addAffector(rotator);
		
		Hitbox hitbox = new BoundingSphere(new Vector3f(0, 0, 0), 2);
		hitbox.setEntity(spawner);
		
		Attribute glow = new Glow(new Vector3f(0f, 1f, 0f));
		Enabler enabler = new MouseEnabler(glow, spawner, hitbox);
		Hydra.addEnabler(enabler);
		
		Script script = new Script(run);
		script.addCondition(new ClickCondition(hitbox));
		Hydra.addScript(script);
		
		Hydra.addEntity(spawner);
	}
}
