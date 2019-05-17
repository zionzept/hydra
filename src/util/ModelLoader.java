package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import gl.Material;
import gl.Mesh;
import gl.Model;
import gl.ModelA;
import gl.TexBase;

public class ModelLoader {
	
	/**
	 * 
	 * @param subpath	Relative path starting from res folder. Do not provide file extension.
	 * @return
	 */
	public static LinkedList<Model> load(String subpath, String file_name, boolean flip) {
		if (subpath.length() > 0) {
			file_name = subpath + "/" + file_name;
		}
		File file = new File("res/models/" + file_name + ".obj");
		
		ArrayList<Float> v_list = new ArrayList<Float>();
		ArrayList<Float> t_list = new ArrayList<Float>();
		ArrayList<Float> n_list = new ArrayList<Float>();
		
		HashMap<String, Mesh> meshes = new HashMap<>(); // material name, mesh
		HashMap<Mesh, Material> mesh_mtl = new HashMap<>(); // mesh, material
		Mesh mesh = new Mesh();
		meshes.put("default", mesh);
		mesh.material = Material.plain;
		mesh_mtl.put(mesh, Material.plain);
		
		try (Scanner sc = new Scanner(file)) {
			while (sc.hasNext()) {
				String line = sc.nextLine();
				String[] s = line.split("( )+");
				if (s.length == 0) { //fail safe
					continue;
				}
				if (s[0].equals("#")) { //comments
					continue;
				}
				if (s[0].equals("mtllib")) { //TODO: add mtl loader here
					meshes = new HashMap<>();
					mesh_mtl = new HashMap<>();
					HashMap<String, Material> materials = loadMtl(subpath, s[1]);
					for (Map.Entry<String, Material> entry : materials.entrySet()) {
						mesh = new Mesh();
						mesh.material = entry.getValue();
						meshes.put(entry.getKey(), mesh);
						mesh_mtl.put(mesh, entry.getValue());
					}
					continue;
				}
				if (s[0].equals("o")) { //TODO: add multiple shape support here
					continue;
				}
				if (s[0].equals("v")) {
					if (flip) {
						v_list.add(Float.parseFloat(s[1]));
						v_list.add(Float.parseFloat(s[3]));
						v_list.add(Float.parseFloat(s[2]));
					} else {
						v_list.add(Float.parseFloat(s[1]));
						v_list.add(Float.parseFloat(s[2]));
						v_list.add(Float.parseFloat(s[3]));
					}
				}
				if (s[0].equals("vt")) {
					t_list.add(Float.parseFloat(s[1]));
					t_list.add(Float.parseFloat(s[2]));
				}
				if (s[0].equals("vn")) {
					if (flip) {
						n_list.add(Float.parseFloat(s[1]));
						n_list.add(Float.parseFloat(s[3]));
						n_list.add(Float.parseFloat(s[2]));
					} else {
						n_list.add(Float.parseFloat(s[1]));
						n_list.add(Float.parseFloat(s[2]));
						n_list.add(Float.parseFloat(s[3]));
					}
				}
				if (s[0].equals("usemtl")) {
					mesh = meshes.get(s[1]);
					if (mesh == null) {
						mesh = new Mesh();
						mesh.material = Material.plain;
						meshes.put(s[1], mesh);
						mesh_mtl.put(mesh, Material.plain);
					}
				}
				if (s[0].equals("s")) { // shading smoothness
					continue;
				}
				if (s[0].equals("f")) {
					for (int i = 1; i < s.length; i++) {
						String[] element = s[i].split("/");
						
						mesh.v_indices.add(Integer.parseInt(element[0])-1);
						if (element[1].length() > 0) {
							mesh.t_indices.add(Integer.parseInt(element[1])-1);
						} else {
							mesh.t_indices.add(0);
						}
						mesh.n_indices.add(Integer.parseInt(element[2])-1);
						if (i > 2) {
							mesh.indices.add(mesh.index_marker);
							if (flip) {
								mesh.indices.add(mesh.index_marker + i - 1);
								mesh.indices.add(mesh.index_marker + i - 2);
							} else {
								mesh.indices.add(mesh.index_marker + i - 2);
								mesh.indices.add(mesh.index_marker + i - 1);
							}
						}
					}
					mesh.index_marker += s.length - 1;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		LinkedList<Model> models = new LinkedList<>();
		for (Mesh m : meshes.values()) {
			for (int i = 0; i < m.v_indices.size(); i++) {
				int index = m.v_indices.get(i);
				if (index < 0) {
					m.v_indices.set(i, (v_list.size() + 3*index) / 3 + 1);
				}
			}
			for (int i = 0; i < m.t_indices.size(); i++) {
				int index = m.t_indices.get(i);
				if (index < 0) {
					m.t_indices.set(i, (t_list.size() + 2*index) / 2 + 1);
				}
			}
			for (int i = 0; i < m.n_indices.size(); i++) {
				int index = m.n_indices.get(i);
				if (index < 0) {
					m.n_indices.set(i, (n_list.size() / 3 + index) + 1);
				}
			}
			
			
			if (t_list.size() == 0) {
				t_list.add(0f);
				t_list.add(0f);
			}
			
			float[] vertices = new float[m.index_marker*3];
			float[] tex_coords = new float[m.index_marker*2];
			float[] normals = new float[m.index_marker*3];
			
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
			for (int i = 0; i < m.index_marker; i++) {
				vertices[i*3] = v_list.get(m.v_indices.get(i)*3);
				vertices[i*3+1] = v_list.get(m.v_indices.get(i)*3+1);
				vertices[i*3+2] = v_list.get(m.v_indices.get(i)*3+2);
				tex_coords[i*2] = t_list.get(m.t_indices.get(i)*2);
				tex_coords[i*2+1] = t_list.get(m.t_indices.get(i)*2+1);
				int n_id = m.n_indices.get(i)*3;
				normals[i*3] = n_list.get(n_id);
				normals[i*3+1] = n_list.get(n_id+1);
				normals[i*3+2] = n_list.get(n_id+2);
			}
			int[] id = m.indices.stream().mapToInt(Integer::intValue).toArray();
			Model model = new ModelA(vertices, normals, tex_coords, id);
			model.setMaterial(m.material);
			models.add(model);
		}
		return models;
	}
	
	private static HashMap<String, Material> loadMtl(String subpath, String file_name) {
		if (subpath.length() > 0) {
			file_name = subpath + "/" + file_name;
		}
		HashMap<String, Material> materials = new HashMap<>();
		Material current_mtl = null;
		File file = new File("res/models/" + file_name);
		try (Scanner sc = new Scanner(file)) {
			while (sc.hasNext()) {
				String s = sc.next();
				String[] path = null;
				switch (s.toLowerCase()) {
				case "#":
					sc.nextLine();
					break;
				case "newmtl":
					String name = sc.next();
					current_mtl = new Material(name);
					materials.put(name, current_mtl);
					break;
				case "ns":
					current_mtl.shininess(sc.nextFloat());
					break;
				case "ka":
					current_mtl.ambient(sc.nextFloat(), sc.nextFloat(), sc.nextFloat());
					break;
				case "kd":
					current_mtl.diffuse(sc.nextFloat(), sc.nextFloat(), sc.nextFloat());
					break;
				case "ks":
					current_mtl.specular(sc.nextFloat(), sc.nextFloat(), sc.nextFloat());
					break;
				case "ke":
					current_mtl.emission(sc.nextFloat(), sc.nextFloat(), sc.nextFloat());
					break;
				case "map_ka":
					path = sc.nextLine().trim().split("\\\\");
					current_mtl.ambient_tex(TexBase.get(path[path.length - 1]));
					break;
				case "map_kd":
					path = sc.nextLine().trim().split("\\\\");
					current_mtl.diffuse_tex(TexBase.get(path[path.length - 1]));
					break;
				case "map_ks":
					path = sc.nextLine().trim().split("\\\\");
					current_mtl.specular_tex(TexBase.get(path[path.length - 1]));
					break;
				case "map_ke":
					path = sc.nextLine().trim().split("\\\\");
					current_mtl.emission_tex(TexBase.get(path[path.length - 1]));
					break;
				case "map_bump":
					if (sc.next().equals("-bm")) {
						float scale = sc.nextFloat();
						String seed_string = sc.nextLine();
						int seed = seed_string.hashCode();
						current_mtl.bump_noise(scale, seed);
					} else {
						sc.nextLine();
						System.out.println("no support for bump map by texture");
					}
					break;
					
					
				case "ni":
				case "d":
				case "tr":
				case "tf":
				case "illum":
					sc.nextLine();
					break;
				default:
					System.out.println("unhandled case in mtl loader on: " + s);
					break;
				}
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return materials;
	}
}