package core;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.system.MemoryStack;

import aaa.Hydra;

public class Window {

	// The window handle
	public static boolean shutdown;
	public static long window;
	public static int w;
	public static int h;

	public static void create(Hydra hydra) {

		init_glfw(hydra);
		init_and_run_gl_thread(hydra); // spawns thread
		poll_window_event_loop(); // keeps thread until shutdown

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		shutdown = true;
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private static void init_glfw(Hydra hydra) {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		glfwWindowHint(GLFW_STENCIL_BITS, 16);
		glfwWindowHint(GLFW_SAMPLES, 16);
		
		// Create the window
		window = glfwCreateWindow(1920, 1080, "Hydra!", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated
		// or released.
		 glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
		 if (action > 0) {
			SKeyboard.keyPress(key);
		 } else {
			 SKeyboard.keyRelease(key);
		 }
		 //if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
		//	 glfwSetWindowShouldClose(window, true);
		 });
		 
		 glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
			 if (action == 1) {
				 SMouse.mouseDown(button);
			 } else {
				 SMouse.mouseUp(button);
			 }
		 });
		 
		 glfwSetCursorPosCallback(window, (window, x, y) -> {
			 SMouse.mouseMove(x, y);
		 });

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);
			w = pWidth.get();
			h = pHeight.get();

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			glfwSetWindowPos(window, vidmode.width() / 2 - w / 2, vidmode.height() / 2 - h / 2);
		} // the stack frame is popped automatically

	}

	private static void init_and_run_gl_thread(Hydra hydra) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// Make the OpenGL context current
				glfwMakeContextCurrent(window);
				// Enable v-sync
				glfwSwapInterval(0);
				//glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);
				// Make the window visible
				

				// This line is critical for LWJGL's interoperation with GLFW's
				// OpenGL context, or any context that is managed externally.
				// LWJGL detects the context that is current in the current thread,
				// creates the GLCapabilities instance and makes the OpenGL
				// bindings available for use.
				GL.createCapabilities();
				
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glDepthMask(true);
				GL11.glDepthFunc(GL11.GL_LEQUAL);
				
				GL11.glEnable(GL11.GL_CULL_FACE);
				
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL14.glBlendEquation(GL14.GL_FUNC_ADD);
				//glfwWindowHint(GLFW_SAMPLES, 4);
				//GL11.glEnable(GL_MULTISAMPLE);
				
				hydra.init();
				
				glfwShowWindow(window);
				while (!shutdown) {
					glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
					hydra.update();
					
					/*try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
					
					 glfwSwapBuffers(window); // swap the color buffers
					 //TODO there's a conflict with other programs like satisfactory causing swapbuffer to be very slow.
					 frames++;
					 
				}
				hydra.close();
			}
		}).start();
	}
	
	private static int frames;
	public static int fps() {
		int fps = frames;
		frames = 0;
		return fps;
	}

	private static void poll_window_event_loop() {
		// the window or has pressed the ESCAPE key.
		while (!glfwWindowShouldClose(window)) {
			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
