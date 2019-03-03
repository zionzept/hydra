package attributes;

import aaa.Entity;
import core.SMouse;
import hitbox.Hitbox;

public class MouseEnabler extends Enabler {
	
	public MouseEnabler(Attribute attribute, Entity entity, Hitbox hitbox) {
		super(attribute, entity, hitbox);
	}
	
	public void update(double dt) {
		if (enabled) {
			if (!hitbox.intersects(SMouse.pos(), SMouse.vec())) {
				enabled = false;
				attribute.disable(entity);
			}
		} else {
			if (hitbox.intersects(SMouse.pos(), SMouse.vec())) {
				enabled = true;
				attribute.enable(entity);
			}
		}
	}
}
