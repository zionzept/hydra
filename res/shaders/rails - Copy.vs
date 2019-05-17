#version 460

#define mu 1.5707963267
#define pi 3.1415926538

layout(location = 0) in vec3 vertex;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 tex_coords;


uniform mat4 world;
uniform mat4 view;

uniform float turn_rad;
uniform float start_rise;
uniform float rise_rad;

out vec2 uv;
out float zz;
out vec3 frag_pos;
out vec3 frag_normal;
out vec3 model_pos;

void main() {
	uv = tex_coords;

	float swivel = vertex.x / 26.0;
	float b = vertex.x / rise_rad;
	float rise = start_rise + b;

	vec3 bent_v = vec3(
		sin(b) * (rise_rad - vertex.z),
		vertex.y,
		rise_rad * (1-cos(b)) + vertex.z * cos(b)
		);
	float a = bent_v.x / turn_rad;
	bent_v = vec3(
		turn_rad * sin(a) - bent_v.y * sin(a),
		turn_rad * (1-cos(a)) + bent_v.y * cos(a),
		bent_v.z
		);

	vec3 bent_n = vec3(
		normal.x * cos(a) - normal.y * sin(a),
		normal.y * cos(a) + normal.x * sin(a),
		normal.z
		);

	model_pos = bent_v;
	frag_pos = (world * vec4(model_pos, 1)).xyz;
	

	frag_normal = (world * vec4(bent_n, 0)).xyz;
	
	vec4 pos = view * vec4(frag_pos, 1);
	zz = pos.z;
	gl_Position = pos;
}




saved stuff

vec3 bent_v = vec3(
		sin(b) * (rise_rad - vertex.z * cos(swivel) + vertex.y * sin(swivel)),
		vertex.y * cos(swivel) + vertex.z * sin(swivel),
		rise_rad * (1-cos(b)) + vertex.z * cos(b) * cos(swivel) - vertex.y * cos(b) * sin(swivel)
		);
	float a = bent_v.x / turn_rad;
	bent_v = vec3(
		turn_rad * sin(a) - bent_v.y * sin(a),
		turn_rad * (1-cos(a)) + bent_v.y * cos(a),
		bent_v.z
		);