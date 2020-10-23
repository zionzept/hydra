package gl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

public class ModelA extends Model {
	private int draw_count;
	private int v_id;
	private int n_id;
	private int t_id;
	private int i_id;
	
	private Material material;
	
	public ModelA(float[] vertices, float[] normals, float[] tex_coords, int[] indices) {
		draw_count = indices.length;
		
		v_id = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, v_id);
		glBufferData(GL_ARRAY_BUFFER, createBuffer(vertices), GL_STATIC_DRAW);
		
		n_id = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, n_id);
		glBufferData(GL_ARRAY_BUFFER, createBuffer(normals), GL_STATIC_DRAW);
		
		t_id = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, t_id);
		glBufferData(GL_ARRAY_BUFFER, createBuffer(tex_coords), GL_STATIC_DRAW);
		
		i_id = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, i_id);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, createBuffer(indices), GL_STATIC_DRAW);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		material = Material.store.get("plain");
	}
	
	public void setMaterial(Material material) {
		this.material = material;
	}
	
	public Material material() {
		return material;
	}
	
	@Override
	public void free() {
		glDeleteBuffers(v_id);
		glDeleteBuffers(t_id);
		glDeleteBuffers(n_id);
		glDeleteBuffers(i_id);
	}
	
	@Override
	public void render(Matrix4f transform, Shader shader) {
		shader.setUniform("world", transform);
		
		System.out.println("Render model_a: dc = " + draw_count);
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glBindBuffer(GL_ARRAY_BUFFER, v_id);
		glVertexPointer(3, GL_FLOAT, 0, 0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		
		glEnableClientState(GL_NORMAL_ARRAY);
		glBindBuffer(GL_ARRAY_BUFFER, n_id);
		glNormalPointer(GL_FLOAT, 0, 0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glBindBuffer(GL_ARRAY_BUFFER, t_id);
		glTexCoordPointer(2, GL_FLOAT, 0, 0);
		glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, i_id);
		
		
		//glDisable(GL_CULL_FACE);
		glDrawElements(GL_TRIANGLES, draw_count, GL_UNSIGNED_INT, 0);
		//glEnable(GL_CULL_FACE);
		//glDrawElements(GL_LINES, draw_count, GL_UNSIGNED_INT, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
	}
	

}
