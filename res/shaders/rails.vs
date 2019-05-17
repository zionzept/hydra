#version 460

#define mu 1.5707963267
#define pi 3.1415926538

layout(location = 0) in vec3 vertex;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 tex_coords;


uniform mat4 world;
uniform mat4 view;

uniform float L;
uniform float dturn;
uniform float drise;
uniform float dswirl;

out vec2 uv;
out float zz;
out vec3 frag_pos;
out vec3 frag_normal;
out vec3 model_pos;

void main() {
	uv = tex_coords;

	float t = vertex.x / L;

	float rise = t * drise;
	float swirl = t * +dswirl;
	//swirl = t*pi;
	float tr = L/dturn;
	float rr = L/drise;

	vec3 ft = vec3(
		sin(rise) * (rr - vertex.z * cos(swirl) - vertex.y * sin(swirl)),
		vertex.y * cos(swirl) - vertex.z * sin(swirl),
		(rr * (1-cos(rise)) + vertex.z * cos(rise) * cos(swirl) + vertex.y * cos(rise) * sin(swirl))
		);
	float turn_fix = drise/sin(drise);
	float turn = ft.x / tr * turn_fix;
	ft = vec3(
		tr * sin(turn) / turn_fix - ft.y * sin(turn),
		tr * (1-cos(turn)) / turn_fix + ft.y * cos(turn),
		ft.z
		);

	//vec3 ft = vec3(
	//	tr*sin(turn)     - vertex.y*sin(turn)*cos(swirl) - vertex.z*cos(turn)*sin(rise)*cos(swirl) + vertex.z*sin(turn)*sin(swirl),
	//	tr-tr*cos(turn)  + vertex.y*cos(turn)*cos(swirl) - vertex.z*sin(turn)*sin(rise)*cos(swirl) - vertex.z*cos(turn)*sin(swirl),
	//	rr-rr*cos(rise)  + vertex.z*cos(rise)*cos(swirl) + vertex.y*cos(rise)*sin(swirl) 
	//	);


	vec3 bent_n = vec3(
		normal.x * cos(turn) - normal.y * sin(turn),
		normal.y * cos(turn) + normal.x * sin(turn),
		normal.z
		);

	model_pos = ft;
	frag_pos = (world * vec4(model_pos, 1)).xyz;
	

	frag_normal = (world * vec4(bent_n, 0)).xyz;
	
	vec4 pos = view * vec4(frag_pos, 1);
	zz = pos.z;
	gl_Position = pos;
}