package scripts;

import org.lwjgl.glfw.GLFW;

import core.SMouse;
import hitbox.Hitbox;

public class ClickCondition implements Condition {

	public ClickCondition(Hitbox hitbox) {
		this.hitbox = hitbox;
	}
	
	Hitbox hitbox;
	boolean enabled;
	
	@Override
	public boolean evaluate() {
		if (enabled) {
			if (!SMouse.isPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT) && !hitbox.intersects(SMouse.pos(), SMouse.vec())) {
				enabled = false;
				return false;
			}
		} else {
			if (SMouse.isPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT) && hitbox.intersects(SMouse.pos(), SMouse.vec())) {
				enabled = true;
				return true;
			}
		}
		return false;
	}

}
