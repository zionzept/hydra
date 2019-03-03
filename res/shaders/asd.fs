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
};

uniform Material material;

uniform vec3 light_direction;
uniform vec4 light_color;
uniform vec4 ambient_color;
uniform vec4 distance_color;
uniform vec3 view_pos;
uniform float view_distance;


in vec2 uv;
in float zz;
in vec3 frag_normal;
in vec3 frag_pos;

out vec4 fragColor;

void main() {
	vec3 n = normalize(frag_normal);
	vec3 V = normalize(view_pos.xyz - frag_pos);

	vec4 ambient = material.ambient;
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
	

	fragColor =  mix(distance_color, surface_color, clarity);
}