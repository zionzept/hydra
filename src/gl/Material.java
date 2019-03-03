package gl;

import org.joml.Vector4f;

public class Material {
	
	public static Material plain = new Material(new Vector4f(0.5f, 0.5f, 0.5f, 1f), 10f);
	public static final Material NULL = new Material() {
		@Override
		public void materialize(Shader shader) {}
	};
	
	Vector4f ambient;
	Vector4f diffuse;
	Vector4f specular;
	Vector4f emission;
	float shininess;
	
	Texture ambient_tex;
	Texture diffuse_tex;
	Texture specular_tex;
	Texture emission_tex;
	
	public Material() {
		
	}
	
	public Material(Vector4f color, float shininess) {
		ambient = color;
		diffuse = color;
		specular = color;
		emission = new Vector4f(0f);
		this.shininess = shininess;
	}
	
	public Material(Vector4f color, Vector4f emission, float shininess) {
		ambient = color;
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
}
