package aaa;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import gl.Material;
import gl.Model;
import gl.Shader;
import gl.Texture;

public class Entity {
	
	public static final Entity NULL = new Entity() {
		@Override
		public void addModels(LinkedList<? extends Model> models) {}
		
		@Override
		public void setMaterialOverride(Material m) {}
		
		@Override
		public boolean isDead() {
			return true;
		}
		
		@Override
		public void render(Matrix4f transform) {}
		
		@Override
		public Matrix4f getTransform() {
			return new Matrix4f();
		}
		
		@Override
		public Vector3f getScaling() {
			return new Vector3f();
		}
	};
	
	private LinkedList<Entity> children;
	private Model[] model;
	private Material[] material_override;
	
	private Shader shader;

	private float scaling_x;
	private float scaling_y;
	private float scaling_z;
	private float rotation_x;
	private float rotation_y;
	private float rotation_z;
	private float translation_x;
	private float translation_y;
	private float translation_z;
	
	private boolean dead;

	public Entity() {
		children = new LinkedList<>();
		model = new Model[0];
		material_override = new Material[0];
		scaling_x = 1;
		scaling_y = 1;
		scaling_z = 1;
	}
	
	public float x() {
		return translation_x;
	}
	
	public float y() {
		return translation_y;
	}
	
	public float z() {
		return translation_z;
	}
	
	public void kill() {
		dead = true;
	}
	
	public Model[] getModels() {
		return model;
	}
	
	public boolean isDead() {
		return dead;
	}
	
	public void addModels(LinkedList<? extends Model> models) {
		Model[] new_model = new Model[model.length + models.size()];
		int i;
		for (i = 0; i < model.length; i++) {
			new_model[i] = model[i];
		}
		for (Model m : models) {
			new_model[i++] = m;
		}
		model = new_model;
		Material[] new_material = new Material[model.length];
		for (i = 0; i < material_override.length; i++) {
			new_material[i] = material_override[i];
		}
		material_override = new_material;
	}
	
	public void setMaterialOverride(Material material) {
		material_override[material_override.length-1] = material;
	}
	
	public void setMaterialOverrideR(Material material) {
		material_override[material_override.length-1] = material;
		for (Entity e : children) {
			e.setMaterialOverrideR(material);
		}
	}
	
	public void addChild(Entity child) {
		children.add(child);
	}
	
	public LinkedList<Entity> getChildren() {
		return children;
	}
	
	public void setShader(Shader shader) {
		this.shader = shader;
	}
	
	public void render(Matrix4f transform) {
		Matrix4f new_transform = new Matrix4f();
		transform.mulAffine(getTransform(), new_transform);
		shader.bind();
		for (int i = 0; i < model.length; i++) {
			if (material_override[i] != null) {
				material_override[i].materialize(shader);
			} else {
				model[i].material().materialize(shader);
			}
			model[i].render(new_transform, shader);
		}
		for (Entity child : children) {
			child.render(new_transform);
		}
	}

	public Matrix4f getTransform() {
		Matrix4f t0 = new Matrix4f();
		Matrix4f t1;
		// translate
		t0 = new Matrix4f();
		t0.m30(translation_x);
		t0.m31(translation_y);
		t0.m32(translation_z);
		// rotation_z
		t1 = new Matrix4f();
		t1.m00((float) cos(rotation_z));
		t1.m01((float) sin(rotation_z));
		t1.m10((float) -sin(rotation_z));
		t1.m11((float) cos(rotation_z));
		t0.mulAffine(t1);
		// rotation_y
		t1 = new Matrix4f();
		t1.m00((float) cos(rotation_y));
		t1.m02((float) -sin(rotation_y));
		t1.m20((float) sin(rotation_y));
		t1.m22((float) cos(rotation_y));
		t0.mulAffine(t1);
		// rotation_x
		t1 = new Matrix4f();
		t1.m11((float) cos(rotation_x));
		t1.m12((float) sin(rotation_x));
		t1.m21((float) -sin(rotation_x));
		t1.m22((float) cos(rotation_x));
		t0.mulAffine(t1);
		// scale
		t1 = new Matrix4f();
		t1.m00(scaling_x);
		t1.m11(scaling_y);
		t1.m22(scaling_z);
		t0.mulAffine(t1);
		return t0;
	}
	
	public void setTransform(Entity e) {
		scaling_x = e.scaling_x;
		scaling_y = e.scaling_y;
		scaling_z = e.scaling_z;
		rotation_x = e.rotation_x;
		rotation_y = e.rotation_y;
		rotation_z = e.rotation_z;
		translation_x = e.translation_x;
		translation_y = e.translation_y;
		translation_z = e.translation_z;
	}

	public void scale(float x, float y, float z) {
		scaling_x *= x;
		scaling_y *= y;
		scaling_z *= z;
	}

	public void scale(float s) {
		scale(s, s, s);
	}
	
	public Vector3f getScaling() {
		return new Vector3f(scaling_x, scaling_y, scaling_z);
	}
	
	public void rotate(Vector3f rot) {
		rotation_x += rot.x;
		rotation_y += rot.y;
		rotation_z += rot.z;
	}
	
	public void rotate(float x, float y, float z) {
		rotation_x += x;
		rotation_y += y;
		rotation_z += z;
	}
	
	public void setRotation(float x, float y, float z) {
		rotation_x = x;
		rotation_y = y;
		rotation_z = z;
	}
	
	public void setRotation(Vector3f rot) {
		rotation_x = rot.x;
		rotation_y = rot.y;
		rotation_z = rot.z;
	}

	public void rotate_x(float r) {
		rotation_x += r;
	}

	public void rotate_y(float r) {
		rotation_y += r;
	}

	public void rotate_z(float r) {
		rotation_z += r;
	}
	
	public Vector3f getRotation() {
		return new Vector3f(rotation_x, rotation_y, rotation_z);
	}
	
	public void translate(Vector3f vec) {
		translation_x += vec.x;
		translation_y += vec.y;
		translation_z += vec.z;
	}

	public void translate(float x, float y, float z) {
		translation_x += x;
		translation_y += y;
		translation_z += z;
	}
	
	public void setTranslation(float x, float y, float z) {
		translation_x = x;
		translation_y = y;
		translation_z = z;
	}
	
	public void setTranslation(Vector3f vec) {
		translation_x = vec.x;
		translation_y = vec.y;
		translation_z = vec.z;
	}
	
	public Vector3f getTranslation() {
		return new Vector3f(translation_x, translation_y, translation_z);
	}

}
