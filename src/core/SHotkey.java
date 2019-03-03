package core;

import java.util.LinkedList;

public abstract class SHotkey {
	public static final SHotkey NULL = new SHotkey(0) {
		@Override
		public void check(int i) {
		}
		@Override
		public void addRequirements() {
		}
		@Override
		public void actuation() {
		}
		@Override
		public void setEnabled(boolean v) {
			System.out.println("fail");
		}
	};
	
	
	private LinkedList<Integer> requiredKeys;	  // These are linked lists rather than hash \\
	private LinkedList<Integer> requiredButtons; // sets because of their expected small size \\
	private int activation;
	
	public static final int KEYBOARD = 0;
	public static final int MOUSE = 1;
	public static final int WHEEL = 2;
	private int activationType;
	
	private boolean disallowAdditionalKeys;
	private boolean disallowAdditionalButtons;
	private boolean enabled;

	public SHotkey(int activation) {
		this.enabled = true;
		this.activation = activation;
		this.requiredKeys = new LinkedList<Integer>();
		this.requiredButtons = new LinkedList<Integer>();
		addRequirements();
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean getEnabled() {
		return enabled;
	}
	
	protected void setKeyboardActivated() {
		activationType = KEYBOARD;
		for (int i : requiredKeys) {
			if (i == activation) {
				requiredKeys.remove(i);
				break;
			}
		}
		// removes double check, also corrects
		// some unexpected behaviour when additional keys are disallowed.
	}
	
	protected void setMouseActivated() {
		activationType = MOUSE;
		for (int i : requiredButtons) {
			if (i == activation) {
				requiredButtons.remove(i);
				break;
			}
		} // removes double check, also corrects
		// some unexpected behaviour when additional buttons are disallowed.
	}
	
	protected void setWheelActivated() {
		activationType = WHEEL;
	}
	
	public int getActivationType() {
		return activationType;
	}
	
	/**
	 * Disallows unrequired keys for hotkey actuation.
	 */
	public void addRequirementDisallowAdditionalKeys() {
		disallowAdditionalKeys = true;
	}
	
	/**
	 * Disallows unrequired buttons for hotkey actuation.
	 */
	public void addRequirementDisallowAdditionalButtons() {
		disallowAdditionalButtons = true;
	}

	
	/**
	 * Adds a requirement for a specified key on the keyboard to be pressed during hotkey actuation.
	 * @param the specified key			See SKeyEvent for keys.
	 */
	public void addRequirementPressedKey(int key) {
		if (!requiredKeys.contains(key)) {
			requiredKeys.add(key);
		}
	}
	
	/**
	 * Add requirement for a specified button on the mouse to be pressed during hotkey actuation.
	 * @param the specified button		See SMouseEvent for buttons.
	 */
	public void addRequirementPressedButton(int button) {
		if (!requiredButtons.contains(button)) {
			requiredButtons.add(button);
		}
	}
	
	protected void check(int activation) {
		if (enabled && this.activation == activation) {
			if (disallowAdditionalKeys && requiredKeys.size() + (activationType == KEYBOARD ? 1 : 0) != SKeyboard.pressedKeyCount() ||
					disallowAdditionalButtons && requiredButtons.size() + (activationType == MOUSE ? 1 : 0) != SMouse.pressedButtonCount()) {
				return;
			}
			for (int i : requiredKeys) {
				if (!SKeyboard.isPressed(i)) {
					return;
				}
			}
			for (int i : requiredButtons) {
				if (!SMouse.isPressed(i)) {
					return;
				}
			}
			actuation();
		}
	}
	
	public abstract void addRequirements();
	public abstract void actuation();
}