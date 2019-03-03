package core;

import java.util.HashSet;
import java.util.LinkedList;

import org.joml.Vector3f;

public class SMouse {
	private static HashSet<Integer> pressedButtons = new HashSet<>();
	private static LinkedList<SHotkey> hotkeys = new LinkedList<>();
	private static LinkedList<SHotkey> wheelHotkeys = new LinkedList<>();
	private static LinkedList<SMouseListener> mouseListeners = new LinkedList<>();
	private static LinkedList<SMouseMoveListener> mouseMoveListeners = new LinkedList<>();
	private static LinkedList<SMouseTrackListener> mouseTrackListeners = new LinkedList<>();
	private static LinkedList<SMouseWheelListener> mouseWheelListeners = new LinkedList<>();
	
	private static double x;
	private static double y;
	private static double px;
	private static double py;
	
	private static Vector3f pos = new Vector3f();
	private static Vector3f vec = new Vector3f();

	private SMouse() {}
	
	public static void pos(Vector3f pos) {
		SMouse.pos.set(pos);
	}
	
	public static Vector3f pos() {
		return pos;
	}
	
	public static void vec(Vector3f vec) {
		SMouse.vec.set(vec);
	}
	
	public static Vector3f vec() {
		return vec;
	}
	
	public static double getX() {
		return x;
	}
	
	public static double getY() {
		return y;
	}
	
	public static double getDX() {
		return x - px;
	}
	
	public static double getDY() {
		return y - py;
	}
	
	public static boolean isPressed(int button) {
		return pressedButtons.contains(button);
	}
	
	public static int pressedButtonCount() {
		return pressedButtons.size();
	}
	
	public static HashSet<Integer> pressedButtons() {
		return pressedButtons; //TODO: security issue :D
	}
	
	public static void addHotkey(SHotkey hotkey) {
		hotkey.setMouseActivated();
		hotkeys.add(hotkey);
	}
	
	public static void addWheelHotkey(SHotkey hotkey) {
		hotkey.setWheelActivated();
		wheelHotkeys.add(hotkey);
	}
	
	public static void addMouseListener(SMouseListener mouseListener) {
		mouseListeners.add(mouseListener);
	}
	
	public static void addMouseMoveListener(SMouseMoveListener mouseMotionListener) {
		mouseMoveListeners.add(mouseMotionListener);
	}
	
	public static void addMouseTrackListener(SMouseTrackListener mouseTrackListener) {
		mouseTrackListeners.add(mouseTrackListener);
	}
	
	public static void addMouseWheelListener(SMouseWheelListener mouseWheelListener) {
		mouseWheelListeners.add(mouseWheelListener);
	}
	
	protected static void mouseDown(int button) {
		pressedButtons.add(button);
		for (SMouseListener mouseListener : mouseListeners) {
			mouseListener.mouseDown(x, y, button);
		}
		for (SHotkey hotkey : hotkeys) {
			hotkey.check(button);
		}
	//	if (ZUI.instance != null) {
	//		ZUI.instance.mouseDown(e);
	//	}
	}

	protected static void mouseUp(int button) {
		pressedButtons.remove(button);
		for (SMouseListener mouseListener : mouseListeners) {
			mouseListener.mouseUp(x, y, button);
		}
	//	if (ZUI.instance != null) {
	//		ZUI.instance.mouseUp(e);
	//	}
	}

	protected static void mouseMove(double x, double y) {
		px = SMouse.x;
		py = SMouse.y;
		SMouse.x = x;
		SMouse.y = y;
		for (SMouseMoveListener mouseMoveListener : mouseMoveListeners) {
			mouseMoveListener.mouseMove(x, y);
		}
	//	if (ZUI.instance != null) {
	//		ZUI.instance.mouseMove(e);
	//	}
	}
	
	protected static void mouseEnter(double x, double y) {
		px = SMouse.x;
		py = SMouse.y;
		SMouse.x = x;
		SMouse.y = y;
		for (SMouseTrackListener mouseTrackListener : mouseTrackListeners) {
			mouseTrackListener.mouseEnter(x, y);
		}
	//	if (ZUI.instance != null) {
	//		ZUI.instance.mouseEnter(e);
	//	}
	}
	
	protected static void mouseExit(double x, double y) {
		for (SMouseTrackListener mouseTrackListener : mouseTrackListeners) {
			mouseTrackListener.mouseExit(x, y);
		}
	//	if (ZUI.instance != null) {
	//		ZUI.instance.mouseExit(e);
	//	}
	}

	protected static void mouseScroll(double x, double y, int direction) {
		for (SMouseWheelListener mouseWheelListener : mouseWheelListeners) {
			mouseWheelListener.mouseScroll(x, y, direction);
		}
		for (SHotkey hotkey : wheelHotkeys) {
			hotkey.check(direction);
		}
	//	if (ZUI.instance != null) {
	//		ZUI.instance.mouseScroll(e);
	//	}
	}
}