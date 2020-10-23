#version 460

struct Material {
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec4 emission;
	float shininess;

	bool ambient_mapped;
	sampler2D ambient_map;
	bool diffuse_mapped;
	sampler2D diffuse_map;
	bool specular_mapped;
	sampler2D specular_map;
	bool emission_mapped;
	sampler2D emission_map;

	bool bump_noise;
	bool bump_tex;
	float bump_noise_scale;
	float bump_noise_seed;
	sampler2D bump_map;
};

uniform Material material;

uniform vec3 light_direction;
uniform vec4 light_color;
uniform vec4 ambient_color;
uniform vec4 distant_color;
uniform vec3 view_pos;
uniform float view_distance;


in vec2 uv;
in float zz;
in vec3 model_pos;
in vec3 frag_normal;
in vec3 frag_pos;

out vec4 fragColor;


float random3D (in vec3 s) {
    return fract(sin(dot(s.xyz,
                         vec3(12.9898,78.233, 41.512)))
                 * 43758.5453123);
}

float noise3D (in vec3 s) {
    vec3 i = floor(s);
    vec3 f = fract(s);

    // Four corners in 2D of a tile
    float a0 = random3D(i);
    float b0 = random3D(i + vec3(1.0, 0.0, 0.0));
    float c0 = random3D(i + vec3(0.0, 1.0, 0.0));
    float d0 = random3D(i + vec3(1.0, 1.0, 0.0));
    float a1 = random3D(i + vec3(0.0, 0.0, 1.0));
    float b1 = random3D(i + vec3(1.0, 0.0, 1.0));
    float c1 = random3D(i + vec3(0.0, 1.0, 1.0));
    float d1 = random3D(i + vec3(1.0, 1.0, 1.0));

    vec3 u = f*f*(3.0-2.0*f);

    // Mix 4 coorners percentages
    return mix(
    	mix(
			mix(a0, b0, u.x),
    		mix(c0, d0, u.x),
    		u.y
    	),
    	mix(
    		mix(a1, b1, u.x),
    		mix(c1, d1, u.x),
    		u.y
    	),
    	u.z
    );
}

void main() {
	vec3 n = normalize(frag_normal);
	vec3 V = normalize(view_pos.xyz - frag_pos);

	if (material.bump_noise) {
		vec3 bump_s = model_pos * material.bump_noise_scale;
		float bump_x = noise3D(bump_s+87.2315);
		float bump_y = noise3D(bump_s+1257.237);
		float bump_z = noise3D(bump_s-711.113);
		vec3 dir = vec3(bump_x, bump_y, bump_z);
		dir = normalize(dir);
		n = normalize(n + dir*noise3D(bump_s)*0.5);
	}
	else if (material.bump_tex) {

	}
	vec4 ambient = material.ambient * material.diffuse;
	if (material.ambient_mapped) {
		ambient *= texture2D(material.ambient_map, uv);
	}
	ambient *= ambient_color;
	
	vec4 diffuse = material.diffuse;
	if (material.diffuse_mapped) {
		diffuse *= texture2D(material.diffuse_map, uv);
	}
	diffuse *= light_color;
	diffuse *= max(dot(n, light_direction), 0.0);

	vec4 specular = material.specular;
	if (material.specular_mapped) {
		specular *= texture2D(material.specular_map, uv);
	}
	specular *= light_color;
	specular *= pow(max(dot(V, reflect(-light_direction, n)), 0f), 256);

	vec4 emission = material.emission;
	if (material.emission_mapped) {
		emission *= texture2D(material.emission_map, uv);
	}

	vec4 surface_color = ambient + diffuse + specular + emission;
	float clarity = view_distance / (pow(zz, 1.2) + view_distance);
	

	fragColor =  mix(distant_color, surface_color, clarity);
}
