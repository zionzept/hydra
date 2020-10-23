#version 460

layout(location = 0) in vec3 vertex;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 tex_coords;


uniform mat4 world;
uniform mat4 view;

out vec2 uv;
out float zz;
out vec3 model_pos;
out vec3 frag_pos;
out vec3 frag_normal;

void main() {
	uv = tex_coords;

    model_pos = vertex;

	frag_normal = (world * vec4(normal, 0)).xyz;
	frag_pos = (world * vec4(vertex, 1)).xyz;

	vec4 pos = view * vec4(frag_pos, 1);
	zz = pos.z;
	gl_Position = pos;
}