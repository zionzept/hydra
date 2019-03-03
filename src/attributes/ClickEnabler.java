package attributes;

import org.lwjgl.glfw.GLFW;

import aaa.Entity;
import core.SMouse;
import hitbox.Hitbox;

public class ClickEnabler extends Enabler {
	
	public ClickEnabler(Attribute attribute, Entity entity, Hitbox hitbox) {
		super(attribute, entity, hitbox);
	}
	
	public void update(double dt) {
		if (enabled) {
			if (!SMouse.isPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT) || !hitbox.intersects(SMouse.pos(), SMouse.vec())) {
				enabled = false;
				attribute.disable(entity);
			}
		} else {
			if (SMouse.isPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT) && hitbox.intersects(SMouse.pos(), SMouse.vec())) {
				enabled = true;
				attribute.enable(entity);
			}
		}
	}
}
