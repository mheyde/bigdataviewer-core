package bdv.jogl.VolumeRenderer.utils;

import bdv.jogl.VolumeRenderer.scene.Texture;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;

/**
 * utils for handling gl contextes
 * @author michael
 *
 */
public class GLUtils {

	/**
	 * Checks if a specific extension is supported in the current gl context
	 * @param context The current gl context.
	 * @param extensionName The extension name. 
	 * @return True if the extension is supported, else false.
	 */
	public static boolean contextSupportsExtension(final GL context, final String extensionName){
		String extensions = context.glGetString(GL.GL_EXTENSIONS);
		return extensions == null ? false : extensions.indexOf(extensionName) != -1;
	}
	
	/**
	 * calculates the virtual dimension needed for the spares texture upload
	 * @param gl
	 * @param sparseTexture
	 * @param currentDimensions full data dimension to map virtually
	 * @return
	 */
	public static long[] calculateSparseVirtualTextures(GL4 gl, final Texture sparseTexture, long currentDimensions[]){
		long dim[] = new long[3];
		int pagesizes[]  = sparseTexture.getVirtPageSizes(gl);
		for(int k =0;k < 3; k++){
			dim[k] = pagesizes[k]*(int)Math.ceil((float)currentDimensions[k]/(float)pagesizes[k]);

		}
		return dim;
	}
}
