package util;

import java.util.LinkedList;

import gl.TerrainSection;

public class Potato {
	
	private float[] vertices;
	private float[] normals;
	private float[] tex_coords;
	private float[] ti;
	private float[] ai;
	private float[] tf;
	private float[] af;
	private int[] indices;

	public Potato(float[] vertices, float[] normals, float[] tex_coords, float[] ti, float[] ai, float[] tf, float[] af, int[] indices) {
		this.vertices = vertices;
		this.normals = normals;
		this.tex_coords = tex_coords;
		this.ti = ti;
		this.ai = ai;
		this.tf = tf;
		this.af = af;
		this.indices = indices;
	}
	
	public LinkedList<TerrainSection> bake() {
		LinkedList<TerrainSection> list = new LinkedList<>();
		list.add(new TerrainSection(vertices, normals, tex_coords, ti, ai, tf, af, indices));
		return list;
	}
}
