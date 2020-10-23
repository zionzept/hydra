package gl;

import java.util.ArrayList;

public class MeshPotato {
	
	public ArrayList<Integer> v_indices;
	public ArrayList<Integer> t_indices;
	public ArrayList<Integer> n_indices;
	public ArrayList<Integer> indices;
	
	public int index_marker;
	
	public MeshPotato() {
		v_indices = new ArrayList<Integer>();
		t_indices = new ArrayList<Integer>();
		n_indices = new ArrayList<Integer>();
		indices = new ArrayList<Integer>();
	}
	
	public Mesh bake(ArrayList<Float> v_list, ArrayList<Float> t_list, ArrayList<Float> n_list) {
		for (int i = 0; i < v_indices.size(); i++) {
			int index = v_indices.get(i);
			if (index < 0) {
				v_indices.set(i, (v_list.size() + 3*index) / 3 + 1);
			}
		}
		for (int i = 0; i < t_indices.size(); i++) {
			int index = t_indices.get(i);
			if (index < 0) {
				t_indices.set(i, (t_list.size() + 2*index) / 2 + 1);
			}
		}
		for (int i = 0; i < n_indices.size(); i++) {
			int index = n_indices.get(i);
			if (index < 0) {
				n_indices.set(i, (n_list.size() / 3 + index) + 1);
			}
		}
		
		
		if (t_list.size() == 0) {
			t_list.add(0f);
			t_list.add(0f);
		}
		
		float[] vertices = new float[index_marker*3];
		float[] tex_coords = new float[index_marker*2];
		float[] normals = new float[index_marker*3];
		
		/*for (int i = 0; i < indices.size(); i++) {
			vertices[i*3] = v_list.get(v_indices.get(indices.get(i))*3);
			vertices[i*3+1] = v_list.get(v_indices.get(indices.get(i))*3+1);
			vertices[i*3+2] = v_list.get(v_indices.get(indices.get(i))*3+2);
			tex_coords[i*2] = t_list.get(t_indices.get(indices.get(i))*2);
			tex_coords[i*2+1] = t_list.get(t_indices.get(indices.get(i))*2+1);
			normals[i*3] = n_list.get(n_indices.get(indices.get(i))*3);
			normals[i*3+1] = n_list.get(n_indices.get(indices.get(i))*3+1);
			normals[i*3+2] = n_list.get(n_indices.get(indices.get(i))*3+2);
		}*/
		for (int i = 0; i < index_marker; i++) {
			vertices[i*3] = v_list.get(v_indices.get(i)*3);
			vertices[i*3+1] = v_list.get(v_indices.get(i)*3+1);
			vertices[i*3+2] = v_list.get(v_indices.get(i)*3+2);
			tex_coords[i*2] = t_list.get(t_indices.get(i)*2);
			tex_coords[i*2+1] = t_list.get(t_indices.get(i)*2+1);
			int n_id = n_indices.get(i)*3;
			normals[i*3] = n_list.get(n_id);
			normals[i*3+1] = n_list.get(n_id+1);
			normals[i*3+2] = n_list.get(n_id+2);
		}
		int[] id = indices.stream().mapToInt(Integer::intValue).toArray();
		return new Mesh(vertices, normals, tex_coords, id);
	}
}
