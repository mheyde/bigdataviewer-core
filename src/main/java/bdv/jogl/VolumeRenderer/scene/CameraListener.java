package bdv.jogl.VolumeRenderer.scene;

import java.util.EventListener;

import com.jogamp.opengl.math.Matrix4;

/**
 * Concerns Camera update scenarios
 * @author michael
 *
 */
public interface CameraListener extends EventListener {

	/**
	 * Triggers if the projection matrix changed
	 * @param matrix The new projection matrix
	 */
	public void projectionMatrixUpdate(final Matrix4 matrix);
	
	/**
	 * Triggers if the view matrix changed
	 * @param matrix The new view matrix
	 */
	public void viewMatrixUpdate(final Matrix4 matrix);
}
