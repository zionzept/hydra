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
uniform Material material_t;

uniform vec3 light_direction;
uniform vec4 light_color;
uniform vec4 ambient_color;
uniform vec4 distant_color;
uniform vec3 view_pos;
uniform float view_distance;

uniform float xs;
uniform float ys;
uniform float zs;

in vec2 uv;
in float zz;
in vec3 frag_normal;
in vec3 frag_pos;
in vec3 model_pos;

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

/*
const vec2 grad3[12]= vec2[12](
    vec2(1,1), vec2(-1,1), vec2(1,-1), vec2(-1,-1),
    vec2(1,0), vec2(-1,0), vec2(1,0), vec2(1,0),
    vec2(0,1), vec2(0,-1), vec2(0,1), vec2(0,-1)
);
*/

/*const int perm[512] = int[512](151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 
    103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 
    203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 
    134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 
    40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196, 135, 130,
    116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118,
    126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213, 119, 248,
    152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9, 189, 22, 39, 253, 19, 98, 108, 110, 79, 113,
    224, 232, 178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241,
    81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50,
    45, 127, 4, 150, 154, 138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156,
    180, 151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 
    37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 
    177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 
    146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 
    25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 
    100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 
    206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 
    153, 101, 155, 167, 43, 172, 9, 189, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 
    246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107, 
    49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 154, 138, 236, 205, 
    93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180);
*/

/*
float get(float xin, float yin) {
    xin /= xs;
    yin /= ys;
    
    float n0, n1, n2; // noise contribution from three corners of simplex

    const float F2 = 0.5 * sqrt(3.0) - 0.5;
    float s = (xin + yin) * F2;
    int i = int(floor(xin + s));
    int j = int(floor(yin + s));

    const float G2 = (3.0 - sqrt(3.0)) / 6.0;
    float t = (i + j) * G2;
    float X0 = i - t;
    float Y0 = j - t;
    float x0 = xin - X0;
    float y0 = yin - Y0;

    // check if it's in upper or lower simplex of rombus
    int i1, j1;
    if (x0 > y0) {
        i1 = 1;
        j1 = 0;
    } else {
        i1 = 0;
        j1 = 1;
    }

    float x1 = x0 - i1 + G2;
    float y1 = y0 - j1 + G2;
    float x2 = x0 - 1.0 + 2.0 * G2;
    float y2 = y0 - 1.0 + 2.0 * G2;
    
    int ii = i & 255;
    int jj = j & 255;
    
    int gi0 = perm[ii + perm[jj]] % 12;
    int gi1 = perm[ii + i1 + perm[jj + j1]] % 12;
    int gi2 = perm[ii + 1 + perm[jj + 1]] % 12;

    //calculate contributions
    float t0 = 0.5 - x0*x0 - y0*y0;
    if (t0 < 0) {
        n0 = 0.0;
    } else {
        t0 *= t0;
        n0 = t0 * t0 * dot(grad3[gi0], vec2(x0, y0));
    }
    
    float t1 = 0.5 - x1*x1 - y1*y1;
    if (t1 < 0) {
        n1 = 0.0;
    } else {
        t1 *= t1;
        n1 = t1 * t1 * dot(grad3[gi1], vec2(x1, y1));
    }
    
    float t2 = 0.5 - x2*x2 - y2*y2;
    if (t2<0) {
        n2 = 0.0;
    } else {
        t2 *= t2;
        n2 = t2 * t2 * dot(grad3[gi2], vec2(x2, y2));
    }

    return zs * 70.0 * (n0 + n1 + n2);
}
*/

float random (in vec2 st) {
    return fract(sin(dot(st.xy,
                         vec2(12.9898,78.233)))
                * 43758.5453123 + frag_pos.z * 0.01);
}

float noise(vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);
    vec2 u = f*f*f*(10.+f*(-15.+f*6.));
    //u = f*f*(3.+f*-2.);
    return mix( mix( random( i + vec2(0.0,0.0) ),
                     random( i + vec2(1.0,0.0) ), u.x),
                mix( random( i + vec2(0.0,1.0) ),
                     random( i + vec2(1.0,1.0) ), u.x), u.y);
}

mat2 rotate2d(float angle){
    return mat2(cos(angle),-sin(angle),
                sin(angle),cos(angle));
}

float lines(in vec2 pos, float b){
    float scale = 10.0;
    pos *= scale;
    return smoothstep(0.0,
                    .5+b*.5,
                    abs((sin(pos.x*3.1415)+1.0))*.5);
}

void main() {
	vec3 n = normalize(frag_normal);
	vec3 V = normalize(view_pos.xyz - frag_pos);

    vec2 pos = frag_pos.xy * vec2(2., 0.5);
    pos = rotate2d(noise(pos)) * pos;
    float pattern = lines(pos, 0.5);

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
	

	fragColor =  mix(
        distant_color, 
        mix(vec4(0.395,0.138,0.134,1.0), vec4(0.710,0.365,0.255, 1.0), pattern) * surface_color, 
        //mix(vec4(0.37,0.1,0.1,1.0), vec4(0.5,0.25,0.15, 1.0), pattern) * surface_color, 
        clarity);
}
