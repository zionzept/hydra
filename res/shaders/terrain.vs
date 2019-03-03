#version 460

layout(location = 0) in vec3 vertex;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 tex_coords;

layout(location = 3) in float vs_ti;
layout(location = 4) in float vs_ai;
layout(location = 5) in float vs_tf;
layout(location = 6) in float vs_af;



uniform mat4 world;
uniform mat4 view;

out vec2 uv;
out float zz;
out vec3 frag_pos;
out vec3 frag_normal;

out flat float ti;
out flat float ai;
out flat float tf;
out flat float af;

void main() {
	uv = tex_coords;

	frag_normal = (world * vec4(normal, 0)).xyz;
	frag_pos = (world * vec4(vertex, 1)).xyz;

	ti = vs_ti;
	ai = vs_ai;
	tf = vs_tf;
	af = vs_af;


	vec4 pos = view * vec4(frag_pos, 1);
	zz = pos.z;
	gl_Position = pos;
}