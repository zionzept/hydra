package gl;

import java.util.HashMap;

public class TexBase {
	
	private static HashMap<String, Texture> textures = new HashMap<>();
	
	public static Texture get(String name) {
		Texture tex = textures.get(name);
		if (tex == null) {
			tex = new Texture(name);
			textures.put(name, tex);
		}
		return tex;
	}
}
