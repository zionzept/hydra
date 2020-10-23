package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import gl.Material;
import gl.MeshPotato;
import gl.TexBase;

public class ModelLoader {
	
	/**
	 * 
	 * @param subpath	Relative path starting from res folder. Do not provide file extension.
	 * @return
	 */
	public static LinkedList<FatPotato> load(String subpath, String file_name, boolean flip) {
		System.out.println("obj: " + subpath + "/" + file_name);
		if (subpath.length() > 0) {
			file_name = subpath + "/" + file_name;
		}
		File file = new File("res/models/" + file_name + ".obj");
		
		ArrayList<Float> v_list = new ArrayList<Float>();
		ArrayList<Float> t_list = new ArrayList<Float>();
		ArrayList<Float> n_list = new ArrayList<Float>();
		
		HashMap<String, MeshPotato> meshes = new HashMap<>(); // material name, mesh
		HashMap<String, Material> mesh_mtl = new HashMap<>(); // mesh, material
		MeshPotato mesh = new MeshPotato();
		meshes.put("default", mesh);
		try (BufferedReader sc = new BufferedReader(new FileReader(file))) {
			String line;
			while (true) {
				line = sc.readLine();
				if (line == null) {
					break;
				}
				String[] s = line.split("( )+");
				if (s.length == 0) { //fail safe
					continue;
				}
				if (s[0].equals("#")) { //comments
					continue;
				}
				if (s[0].equals("mtllib")) {
					meshes = new HashMap<>();
					mesh_mtl = loadMtl(subpath, s[1]);
					for (Map.Entry<String, Material> entry : mesh_mtl.entrySet()) {
						mesh = new MeshPotato();
						meshes.put(entry.getKey(), mesh);
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
						mesh = new MeshPotato();
						meshes.put(s[1], mesh);
						//mesh_mtl.put(mesh, Material.plain); ???
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
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		LinkedList<FatPotato> models = new LinkedList<>();
		for (Map.Entry<String, MeshPotato> entry : meshes.entrySet()) {
			models.add(new FatPotato(entry.getValue().bake(v_list, t_list, n_list), mesh_mtl.get(entry.getKey())));
		}
		return models;
	}
	
	private static HashMap<String, Material> loadMtl(String subpath, String file_name) {
		System.out.println("  mtl: " + subpath + "/" + file_name);
		if (subpath.length() > 0) {
			file_name = subpath + "/" + file_name;
		}
		HashMap<String, Material> materials = new HashMap<>();
		Material current_mtl = null;
		File file = new File("res/models/" + file_name);
		try (Scanner sc = new Scanner(file)) {
			String line;
			while (true) {
				try {
					line = sc.nextLine();
				} catch (NoSuchElementException e) {
					break;
				}
				
				if (line == null) {
					break;
				}
				String[] s = line.split("( )+");
				String[] path;
				String u;
				switch (s[0].toLowerCase()) {
				case "#":
					sc.nextLine();
					break;
				case "newmtl":
					current_mtl = new Material(s[1]);
					materials.put(s[1], current_mtl);
					break;
				case "ns":
					current_mtl.shininess(Float.parseFloat(s[1]));
					break;
				case "ka":
					current_mtl.ambient(Float.parseFloat(s[1]), Float.parseFloat(s[2]), Float.parseFloat(s[3]));
					break;
				case "kd":
					current_mtl.diffuse(Float.parseFloat(s[1]), Float.parseFloat(s[2]), Float.parseFloat(s[3]));
					break;
				case "ks":
					current_mtl.specular(Float.parseFloat(s[1]), Float.parseFloat(s[2]), Float.parseFloat(s[3]));
					break;
				case "ke":
					current_mtl.emission(Float.parseFloat(s[1]), Float.parseFloat(s[2]), Float.parseFloat(s[3]));
					break;
				case "map_ka":
					u = s[1];
					for (int i = 2; i < s.length; i++) {
						u += " " + s[i];
					}
					path = u.trim().split("\\\\\\\\");
					current_mtl.ambient_tex(TexBase.get(path[path.length - 1]));
					break;
				case "map_kd":
					u = s[1];
					for (int i = 2; i < s.length; i++) {
						u += " " + s[i];
					}
					path = u.trim().split("\\\\\\\\");
					current_mtl.diffuse_tex(TexBase.get(path[path.length - 1]));
					break;
				case "map_ks":
					u = s[1];
					for (int i = 2; i < s.length; i++) {
						u += " " + s[i];
					}
					path = u.trim().split("\\\\\\\\");
					current_mtl.specular_tex(TexBase.get(path[path.length - 1]));
					break;
				case "map_ke":
					u = s[1];
					for (int i = 2; i < s.length; i++) {
						u += " " + s[i];
					}
					path = u.trim().split("\\\\\\\\");
					current_mtl.emission_tex(TexBase.get(path[path.length - 1]));
					break;
				case "map_bump":
					if (s[1].equals("-bm")) {
						float scale = Float.parseFloat(s[2]);
						String seed_string = s[3];
						int seed = seed_string.hashCode();
						current_mtl.bump_noise(scale, seed);
					} else {
						System.err.println("no support for bump map by texture");
					}
					break;
					
					
				case "ni":
				case "d":
				case "tr":
				case "tf":
				case "illum":
					try {
						sc.nextLine();						
					} catch (NoSuchElementException e) {}
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