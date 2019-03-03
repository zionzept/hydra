package gl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

public class TerrainSection extends Model {
	
	private int draw_count;
	private int v_id;
	private int n_id;
	private int t_id;
	private int i_id;
	private int ti_id;
	private int ai_id;
	private int tf_id;
	private int af_id;
	
	public TerrainSection(float[] vertices, float[] normals, float[] tex_coords, float[] ti, float[] ai, float[] tf, float[] af, int[] indices) {
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
		
		ti_id = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, ti_id);
		glBufferData(GL_ARRAY_BUFFER, createBuffer(ti), GL_STATIC_DRAW);
		
		ai_id = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, ai_id);
		glBufferData(GL_ARRAY_BUFFER, createBuffer(ai), GL_STATIC_DRAW);
		
		tf_id = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, tf_id);
		glBufferData(GL_ARRAY_BUFFER, createBuffer(tf), GL_STATIC_DRAW);
		
		af_id = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, af_id);
		glBufferData(GL_ARRAY_BUFFER, createBuffer(af), GL_STATIC_DRAW);
		
		i_id = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, i_id);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, createBuffer(indices), GL_STATIC_DRAW);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void free() {
		glDeleteBuffers(v_id);
		glDeleteBuffers(t_id);
		glDeleteBuffers(n_id);
		glDeleteBuffers(ti_id);
		glDeleteBuffers(ai_id);
		glDeleteBuffers(tf_id);
		glDeleteBuffers(af_id);
		glDeleteBuffers(i_id);
	}
	
	@Override
	public void render(Matrix4f transform, Shader shader) {
		shader.setUniform("world", transform);
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);
		glEnableVertexAttribArray(4);
		glEnableVertexAttribArray(5);
		glEnableVertexAttribArray(6);
		
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
		
		glBindBuffer(GL_ARRAY_BUFFER, ti_id);
		glVertexAttribPointer(3, 1, GL_FLOAT, false, 0, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, ai_id);
		glVertexAttribPointer(4, 1, GL_FLOAT, false, 0, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, tf_id);
		glVertexAttribPointer(5, 1, GL_FLOAT, false, 0, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, af_id);
		glVertexAttribPointer(6, 1, GL_FLOAT, false, 0, 0);
		
		
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
		glDisableVertexAttribArray(3);
		glDisableVertexAttribArray(4);
		glDisableVertexAttribArray(5);
		glDisableVertexAttribArray(6);
	}

	@Override
	public void setMaterial(Material material) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Material material() {
		// TODO Auto-generated method stub
		return null;
	}
}