#version 460
//[a][t]
uniform vec4[3][3] biome_colors = vec4[3][3](
		vec4[3](vec4(0.0f, 0.01f, 0.05f, 1.0f),
			vec4(0.0f, 0.07f, 0.1f, 1.0f),
			vec4(0.0f, 0.3f, 0.2f, 1.0f)),
		vec4[3](vec4(0.85f, 0.85f, 0.9f, 1.0f),
			vec4(0.2f, 0.4f, 0.0f, 1.0f),
			vec4(0.9f, 0.4f, 0.0f, 1.0f)),
		vec4[3](vec4(0.95f, 0.95f, 1.0f, 1.0f),
			vec4(0.5f, 0.4f, 0.3f, 1.0f),
			vec4(0.3f, 0.1f, 0.0f, 1.0f)));



uniform vec3 light_direction;
uniform vec4 light_color;
uniform vec4 ambient_color;
uniform vec4 distant_color;
uniform vec3 view_pos;
uniform float view_distance;
uniform float alpha;

uniform sampler2D grass_tex;

in vec2 uv;
in float zz;
in vec3 frag_normal;
in vec3 frag_pos;

in flat float ti;
in flat float ai;
in flat float tf;
in flat float af;

out vec4 fragColor;

vec4 surface(int a, int t) {
	if (a == 1 && t == 1) {
		return texture2D(grass_tex, uv);
	}
	return biome_colors[a][t];
}

void main() {

	vec3 n = normalize(frag_normal);
	vec3 V = normalize(view_pos.xyz - frag_pos);
	vec4 ambient = ambient_color;
	
	float diff = max(dot(n, light_direction), 0.0);
	vec4 diffuse = diff * light_color;

	float spec = pow(max(dot(V, reflect(-light_direction, n)), 0f), 256);
	vec4 specular = spec * light_color;

	float clarity = view_distance / (pow(zz, 1.2) + view_distance);
	

	vec4 surface_color_0;
	surface_color_0 = surface(int(ai), int(ti));

	float af_l = af;
	if (ai == 0 && frag_pos.z < 0) {
		af_l = 0;
	}
	if (tf > 0) {
		surface_color_0 = mix(surface_color_0, surface(int(ai), int(ti)+1), tf);
	}
	if (af_l > 0) {
		vec4 surface_color_1 = surface(int(ai)+1, int(ti));
		if (tf > 0) {
			surface_color_1 = mix(surface_color_1, surface(int(ai)+1, int(ti)+1), tf);
		}
		surface_color_0 = mix(surface_color_0, surface_color_1, af_l);
	}
	fragColor =  mix(distant_color, (ambient + diffuse + specular) * surface_color_0, clarity);

	
	//fragColor = vec4(1, 1, 1, alpha) * fragColor; // not needed without use of alpha
}