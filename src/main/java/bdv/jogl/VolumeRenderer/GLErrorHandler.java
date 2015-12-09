package bdv.jogl.VolumeRenderer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.glu.GLU;

/**
 * handles latest gl and glu errors
 * @author michael
 *
 */
public class GLErrorHandler {
	
	/**
	 * evaluates the current gl context and throws an exception if there was an error.
	 * @param gl the gl context to use
	 */
	public static void assertGL(GL gl){
		int latestError = gl.glGetError();
		
		//everything is ok
		if(latestError == GL.GL_NO_ERROR){
			return;
		}
		
		//prepare exception
		GLU glu = new GLU();
		String errorMessage = glu.gluGetString(latestError);
		
		if(errorMessage==null){
			switch (latestError) {
			case GL.GL_INVALID_ENUM:
				errorMessage = "GL_INVALID_ENUM";
				break;

			case GL.GL_INVALID_VALUE:
				errorMessage = "GL_INVALID_VALUE";
				break;

			case GL.GL_INVALID_OPERATION:
				errorMessage = "GL_INVALID_OPERATION";
				break;

			case GL.GL_INVALID_FRAMEBUFFER_OPERATION:
				errorMessage = "GL_INVALID_FRAMEBUFFER_OPERATION";
				break;
				
			case GL.GL_OUT_OF_MEMORY:
				errorMessage = "GL_OUT_OF_MEMORY";
				break;
				
			default:
				errorMessage = "unknown";
				break;
			}
		}
		
		throw new RuntimeException("A GL or GLU error occured: \""+ errorMessage + 
				"\" Code: "+latestError);
	}

}
