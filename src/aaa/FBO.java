package aaa;

import static org.lwjgl.opengl.GL30.*;

import core.Window;

public class FBO {

	int framebuffer;
	
	public FBO() {
		framebuffer = glGenFramebuffers();
		
		int texture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texture);
		
		//glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, Window.w, Window.h, GL_RGB, GL_UNSIGNED_BYTE, null);
		
	}
	
	public void bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
	}
	
	public void unbind() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
}
