package core;

import java.util.HashSet;
import java.util.LinkedList;

public class SKeyboard {
	private static HashSet<Integer> pressedKeys = new HashSet<Integer>();
	private static LinkedList<SHotkey> hotkeys = new LinkedList<SHotkey>();
	private static LinkedList<SKeyListener> keyListeners = new LinkedList<SKeyListener>();
	
	private SKeyboard() {}
	
	public static boolean isPressed(int key) {
		return pressedKeys.contains(key);
	}
	
	public static int pressedKeyCount() {
		return pressedKeys.size();
	}
	
	public static HashSet<Integer> pressedKeys() {
		return pressedKeys; //TODO: roubustness  issue :D
	}
	
	public static void addHotkey(SHotkey hotkey) {
		hotkey.setKeyboardActivated();
		hotkeys.add(hotkey);
	}

	public static void addKeyListener(SKeyListener keyListener) {
		keyListeners.add(keyListener);
	}
	
	protected static void keyPress(int key) {
		pressedKeys.add(key);
		for (SKeyListener keyListener : keyListeners) {
			keyListener.keyPress(key);
		}
		for (SHotkey hotkey : hotkeys) {
			hotkey.check(key);
		}
		//if (ZUI.instance != null) {
		//	ZUI.instance.keyPress(e);
		//}
		
	}
	
	protected static void keyRelease(int key) {
		pressedKeys.remove(key);
		for (SKeyListener keyListener : keyListeners) {
			keyListener.keyRelease(key);
		}
		//if (ZUI.instance != null) {
		//	ZUI.instance.keyRelease(e);
		//}
	}
}