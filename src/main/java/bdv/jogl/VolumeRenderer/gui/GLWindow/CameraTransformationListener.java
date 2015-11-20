package bdv.jogl.VolumeRenderer.gui.GLWindow;

import java.util.EventListener;

/**
 * Listens interface for begin and end of a camera transformation like orbit or zoom
 * @author michael
 *
 */
public interface CameraTransformationListener extends EventListener {

	/**
	 * Function is triggered if the camera starts transformation
	 */
	public void transformationStart();
	
	/**
	 * Function is triggered if the camera stops transformation 
	 */
	public void transformationStop();
}
