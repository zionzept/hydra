package gl;

import java.util.ArrayList;

public class Mesh {
	
	public Material material;
	
	public ArrayList<Integer> v_indices;
	public ArrayList<Integer> t_indices;
	public ArrayList<Integer> n_indices;
	public ArrayList<Integer> indices;
	
	public int index_marker;
	
	public Mesh() {
		v_indices = new ArrayList<Integer>();
		t_indices = new ArrayList<Integer>();
		n_indices = new ArrayList<Integer>();
		indices = new ArrayList<Integer>();
	}
}
