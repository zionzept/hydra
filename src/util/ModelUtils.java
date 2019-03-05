package util;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Vector3f;

import Meff.BiomeGen;
import gl.Material;
import gl.Model;
import gl.ModelA;

public class ModelUtils {
	
	private static HashMap<String, LinkedList<Model>> models;
	private static HashMap<Model, Material> materials;
	
	
	public static void init() {
		models = new HashMap<>();
		materials = new HashMap<>();
		
		initModelsFromFiles();
		initModelsFromCode();
		
	}
	
	public static LinkedList<Model> get(String name) {
		return models.get(name);
	}
	
	public static LinkedList<Model> get(String name, int variant) {
		String suffix = Integer.toString(variant);
		while (suffix.length() < 3) {
			suffix = '0' + suffix;
		}
		return get(name + suffix);
	}
	
	private static void initModelsFromFiles() {
		models.put("sphere", ModelLoader.load("", "sphere", false));
		models.put("tree", ModelLoader.load("", "tree", false));
		
		for (int i = 1; i <= 14; i++) {
			String suffix = Integer.toString(i);
			while (suffix.length() < 2) {
				suffix = '0' + suffix;
			}
			models.put("building_a" + '0' + suffix, ModelLoader.load("buildings", "Building_A" + suffix, true));
		}
		models.put("khalifa", ModelLoader.load("buildings", "khalifa", false));
		
		models.put("rail", ModelLoader.load("trains", "Rails_OBJ", false));
		models.put("locomotive", ModelLoader.load("trains", "locomotive", false));
		
		models.put("stone_tower", ModelLoader.load("", "stone_tower", false));
		models.put("spawner", ModelLoader.load("", "spawner", false));
		
	}
	
	private static void put(String name, Model model) {
		LinkedList<Model> list = new LinkedList<>();
		list.add(model);
		models.put(name, list);
	}
	
	private static void initModelsFromCode() {
		put("cube", cube);
	}
	
	public static void free() {
		for (LinkedList<Model> ml : models.values()) {
			for (Model m : ml) {
				m.free();
			}
		}
	}
	
	
	// param models
	public static Potato createTerrainSection(double x0, double y0, int s, int r, BiomeGen z) {
		s++;
		float[] vertices = new float[s*s*3];
		float[] normals = new float[s*s*3];
		float[] tex_coords = new float[s*s*2];
		
		float[] ti = new float[s*s];
		float[] tf = new float[s*s];
		float[] ai = new float[s*s];
		float[] af = new float[s*s];
		
		float[] vert_z = new float[(s+2)*(s+2)];
		
		for (int i = 0; i < s; i++) {
			for (int j = 0; j < s; j++) {
				int pos = i*s + j;
				vertices[3*pos] = (float)(x0 + (j<<r)); // could translate instead of adding 0 coords here?
				vertices[3*pos + 1] = (float)(y0 + (i<<r));
				double[] terrain_data = z.getData(x0 + (j<<r), y0 + (i<<r)); // but 0 coords are needed here
				vertices[3*pos + 2] = (float)terrain_data[0]; 
				vert_z[(i+1)*(s+2) + j+1] = vertices[3*pos + 2];
				
				tex_coords[2*pos] = ((float)x0 + (float)j*(1<<r)) * 0.25f;
				tex_coords[2*pos + 1] = ((float)y0 + (float)i*(1<<r)) * 0.25f;
				
				// obtains biome data, these get calculated in the z.get() call above.
				ti[pos] = (float)terrain_data[1];
				ai[pos] = (float)terrain_data[2];
				tf[pos] = (float)terrain_data[3];
				af[pos] = (float)terrain_data[4];
				//System.out.println("(" + ti[pos] + " " + tf[pos] + ") : (" + ai[pos] + " " + af[pos] + ")");
 			}
		}
		for (int j = 0; j < s; j++) {
			vert_z[j+1] = (float) z.get(x0 + (j<<r), y0 + (-1<<r));
			vert_z[(s+1)*(s+2)+j+1] = (float) z.get(x0 + (j<<r), y0 + (s<<r));
		}
		for (int i = 0; i < s; i++) {
			vert_z[(i+1)*(s+2)] = (float) z.get(x0 + (-1<<r), y0 + (i<<r));
			vert_z[(i+1)*(s+2)+s+1] = (float) z.get(x0 + (s<<r), y0 + (i<<r));
		}
		
		for (int i = 0; i < s; i++) {
			for (int j = 0; j < s; j++) {
				int pos = (i+1)*(s+2)+j+1;
				float ax = 0;
				float ay = 0;
				
				double dz = vert_z[pos-1] - vert_z[pos];
				ax += Math.PI/2 - Math.atan(dz/(1<<r));
				dz = vert_z[pos+1] - vert_z[pos];
				ax += Math.PI/2 + Math.atan(dz/(1<<r));
				ax /= 2;
				
				dz = vert_z[pos-s-2] - vert_z[pos];
				ay += Math.PI/2 - Math.atan(dz/(1<<r));
				dz = vert_z[pos+s+2] - vert_z[pos];
				ay += Math.PI/2 + Math.atan(dz/(1<<r));
				ay /= 2;
				
				Vector3f n = new Vector3f((float) Math.tan(Math.PI/2-ax), (float) Math.tan(Math.PI/2-ay), 1f);
				n.normalize();
				
				pos = 3*(i*s + j);
				normals[pos] = n.x();
				normals[pos + 1] = n.y();
				normals[pos + 2] = n.z();
			}
		}
		
		int[] indices = new int[(s-1)*(s-1)*6];
		for (int i = 0; i <s - 1; i++) {
			for (int j = 0; j < s - 1; j++) {
				int pos = 6 * (i*(s-1) + j);
				indices[pos] = i*s + j;
				indices[pos+1] = i*s + j + 1;
				indices[pos+2] = i*s + j + s + 1;
				indices[pos+3] = i*s + j;
				indices[pos+4] = i*s + j + s + 1;
				indices[pos+5] = i*s + j + s;
			}
		}
		
		return new Potato(vertices, normals, tex_coords, ti, ai, tf, af, indices);
	}
	
	public static LinkedList<Model> grid_xy(int x, int y) {
		x++;
		y++;
		double dx = 1;
		double dy = 1;
		double x0 = -x / 2;
		double y0 = -y / 2;
		
		float[] vertices = new float[x*y*3];
		float[] normals = new float[x*y*3];
		float[] tex_coords = new float[x*y*2];
		
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				int pos = i*x + j;
				vertices[3*pos] = (float)(x0 + j * dx);
				vertices[3*pos + 1] = (float)(y0 + i * dy);
				//vertices[3*pos + 2]  = 0f;
				
				//normals[3*pos] = 0f;
				//normals[3*pos + 1] = 0f;
				normals[3*pos + 2] = 1f;
				
				tex_coords[2*pos] = (float)j;
				tex_coords[2*pos + 1] = (float)i;
 			}
		}
		
		int[] indices = new int[(x-1)*(y-1)*6];
		for (int i = 0; i < y - 1; i++) {
			for (int j = 0; j < x - 1; j++) {
				int pos = 6 * (i*(x-1) + j);
				indices[pos] = i*x + j;
				indices[pos+1] = i*x + j + 1;
				indices[pos+2] = i*x + j + x + 1;
				indices[pos+3] = i*x + j;
				indices[pos+4] = i*x + j + x + 1;
				indices[pos+5] = i*x + j + x;
			}
		}
		LinkedList<Model> list = new LinkedList<Model>();
		list.add(new ModelA(vertices, normals, tex_coords, indices));
		return list; 
	}
	
	public static LinkedList<Model> ball(int x, int y) {
		
		float[] vertices = new float[x*y*3];
		float[] normals = new float[x*y*3];
		float[] tex_coords = new float[x*y*2];
		
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				double theta = 2*PI*j/x;
				double phi = PI*i/(y-1);
				int pos = i*x + j;
				vertices[3*pos] = (float)(sin(phi)*cos(theta));
				vertices[3*pos+1] = (float)(sin(phi)*sin(theta));
				vertices[3*pos+2] = (float)(cos(phi));
				
				normals[3*pos] = vertices[3*pos];
				normals[3*pos+1] = vertices[3*pos+1];
				normals[3*pos+2] = vertices[3*pos+2];
				
				tex_coords[2*pos] = (float)j/x;
				tex_coords[2*pos+1] = (float)i/y;
			}
		}
		
		int[] indices = new int[(x)*(y-1)*6];
		for (int i = 0; i < y-1; i++) {
			for (int j = 0; j < x; j++) {
				int pos = 6 * (i*x + j);
				indices[pos] = i*x + j;
				indices[pos+1] = (i+1)*x + (j+1)%x;
				indices[pos+2] = i*x + (j+1)%x;
				
				indices[pos+3] = i*x + j;
				indices[pos+4] = (i+1)*x + j;
				indices[pos+5] = (i+1)*x + (j+1)%x;
			}
		}
		LinkedList<Model> list = new LinkedList<>();
		list.add(new ModelA(vertices, normals, tex_coords, indices));
		return list;
	}
	
	public static final ModelA cube = new ModelA(
			new float[] {
					-.5f, -.5f, -.5f, //b
					-.5f, .5f, -.5f,
					.5f, .5f, -.5f,
					.5f, -.5f, -.5f,
					
					-.5f, -.5f, .5f, //t
					.5f, -.5f, .5f,
					.5f, .5f, .5f,
					-.5f, .5f, .5f,
					
					-.5f, -.5f, -.5f, //f
					.5f, -.5f, -.5f,
					.5f, -.5f, .5f,
					-.5f, -.5f, .5f,
					
					-.5f, .5f, -.5f, //b
					-.5f, .5f, .5f,
					.5f, .5f, .5f,
					.5f, .5f, -.5f,
					
					-.5f, -.5f, -.5f, //l
					-.5f, -.5f, .5f,
					-.5f, .5f, .5f,
					-.5f, .5f, -.5f,
					
					.5f, -.5f, -.5f, //r
					.5f, .5f, -.5f,
					.5f, .5f, .5f,
					.5f, -.5f, .5f
			},
			new float[] {
					0f, 0f, -1f,
					0f, 0f, -1f,
					0f, 0f, -1f,
					0f, 0f, -1f,
					
					0f, 0f, 1f,
					0f, 0f, 1f,
					0f, 0f, 1f,
					0f, 0f, 1f,
					
					0f, -1f, 0f,
					0f, -1f, 0f,
					0f, -1f, 0f,
					0f, -1f, 0f,
					
					0f, 1f, 0f,
					0f, 1f, 0f,
					0f, 1f, 0f,
					0f, 1f, 0f,
					
					-1f, 0f, 0f,
					-1f, 0f, 0f,
					-1f, 0f, 0f,
					-1f, 0f, 0f,
					
					1f, 0f, 0f,
					1f, 0f, 0f,
					1f, 0f, 0f,
					1f, 0f, 0f,
			},
			new float[] {
					0, 0,
					0.25f, 0,
					0.25f, 0.25f,
					0, 0.25f,
					
					0.25f, 0f,
					0.5f, 0f,
					0.5f, 0.25f,
					0.25f, 0.25f,
					
					0.5f, 0f,
					0.75f, 0f,
					0.75f, 0.25f,
					0.5f, 0.25f,
					
					0.75f, 0f,
					1f, 0f,
					1f, 0.25f,
					0.75f, 0.25f,
					
					0, 0.25f,
					0.25f, 0.25f,
					0.25f, 0.5f,
					0, 0.5f,
					
					0.25f, 0.25f,
					0.5f, 0.25f,
					0.5f, 0.5f,
					0.25f, 0.5f
			},
			new int[] {
					0, 1, 2,
					0, 2, 3,
					4, 5, 6,
					4, 6, 7,
					8, 9, 10,
					8, 10, 11,
					12, 13, 14,
					12, 14, 15,
					16, 17, 18,
					16, 18, 19,
					20, 21, 22,
					20, 22, 23
			}
			);
			
	
	public static LinkedList<Model> square_xy() {
		ModelA model = new ModelA(
				new float[] {
						-.5f, -.5f, 0f,
						.5f, -.5f, 0f,
						.5f, .5f, 0f,
						-.5f, .5f, 0f,
				},
				new float[] {
						0, 0, 1,
						0, 0, 1,
						0, 0, 1,
						0, 0, 1
				},
				new float[] {
						0, 0,
						1, 0,
						1, 1,
						0, 1,
				},
				new int[] {
						0, 1, 2,
						0, 2, 3,
				}
		);
		LinkedList<Model> list = new LinkedList<>();
		list.add(model);
		return list;
	}
}
