package gl;

import java.util.HashMap;

import org.joml.Vector4f;

public class Material {
	
	public static final HashMap<String, Material> store = new HashMap<String, Material>();
	
	static {
		String s;
		s = "null";
		store.put(s, new Material("null") {
			@Override
			public void materialize(Shader shader) {}
		});
		s = "plain";
		store.put(s, new Material(s, new Vector4f(1.0f, 1.0f, 1.0f, 1f), 10f));
	}
	
	private String name;
	
	Vector4f ambient;
	Vector4f diffuse;
	Vector4f specular;
	Vector4f emission;
	float shininess;
	
	Texture ambient_tex;
	Texture diffuse_tex;
	Texture specular_tex;
	Texture emission_tex;
	
	private int bump_mode; // 0 for none, 1 for noise, 2 for tex
	private float bump_scale;
	private float bump_seed; // used in noise
	private Texture bump_tex;
	
	public Material(String name) {
		this.name = name;
	}
	
	public Material(String name, Vector4f color, float shininess) {
		this.name = name;
		ambient = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
		diffuse = color;
		specular = color;
		emission = new Vector4f(0f);
		this.shininess = shininess;
	}
	
	public Material(String name, Vector4f color, Vector4f emission, float shininess) {
		this.name = name;
		ambient = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
		diffuse = color;
		specular = color;
		this.emission = emission;
		this.shininess = shininess;
	}
	
	public void materialize(Shader shader) {
		shader.setUniform("material.ambient", ambient);
		shader.setUniform("material.diffuse", diffuse);
		shader.setUniform("material.specular", specular);
		shader.setUniform("material.emission", emission);		
		shader.setUniform("material.shininess", shininess);
		
		if (ambient_tex != null) {
			ambient_tex.bind(0);
			shader.setUniform("material.ambient_map", 0);
			shader.setUniform("material.ambient_mapped", true);
		} else {
			shader.setUniform("material.ambient_mapped", false);
		}
		if (diffuse_tex != null) {
			diffuse_tex.bind(1);
			shader.setUniform("material.diffuse_map", 1);
			shader.setUniform("material.diffuse_mapped", true);
		} else {
			shader.setUniform("material.diffuse_mapped", false);
		}
		if (specular_tex != null) {
			specular_tex.bind(2);
			shader.setUniform("material.specular_map", 2);
			shader.setUniform("material.specular_mapped", true);
		} else {
			shader.setUniform("material.specular_mapped", false);
		}
		if (emission_tex != null) {
			emission_tex.bind(3);
			shader.setUniform("material.emission_map", 3);
			shader.setUniform("material.emission_mapped", true);
		} else {
			shader.setUniform("material.emission_mapped", false);
		}
		switch(bump_mode) {
		case 1:
			shader.setUniform("material.bump_noise_scale", bump_scale);
			shader.setUniform("material.bump_noise_seed", bump_seed);
			shader.setUniform("material.bump_noise", true);
			shader.setUniform("material.bump_tex", false);
			break;
		case 2:
			// not implemented
			shader.setUniform("material.bump_noise", false);
			shader.setUniform("material.bump_tex", false);
			break;
		default:
			shader.setUniform("material.bump_noise", false);
			shader.setUniform("material.bump_tex", false);
			break;
		}
	}
	
	public void shininess(float s) {
		shininess = s;
	}
	
	public void ambient(float r, float g, float b) {
		ambient = new Vector4f(r, g, b, 1);
	}
	
	public void diffuse(float r, float g, float b) {
		diffuse = new Vector4f(r, g, b, 1);
	}
	
	public void specular(float r, float g, float b) {
		specular = new Vector4f(r, g, b, 1);
	}
	
	public Vector4f emission() {
		return emission;
	}
	
	public void emission(Vector4f emission) {
		this.emission = emission;
	}
	
	public void emission(float r, float g, float b) {
		emission = new Vector4f(r, g, b, 1);
	}
	
	public void ambient_tex(Texture tex) {
		ambient_tex = tex;
	}
	
	public void diffuse_tex(Texture tex) {
		diffuse_tex = tex;
	}
	
	public void specular_tex(Texture tex) {
		specular_tex = tex;
	}

	public void emission_tex(Texture tex) {
		emission_tex = tex;
	}
	
	public void bump_noise(float scale, float seed) {
		bump_mode = 1;
		bump_scale = scale;
		bump_seed = seed;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
