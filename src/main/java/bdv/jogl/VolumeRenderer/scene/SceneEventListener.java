package bdv.jogl.VolumeRenderer.scene;

import java.util.EventListener;

/**
 * Listener for scene events
 * @author michael
 *
 */
public interface SceneEventListener extends EventListener {

	/**
	 * Triggered if the scene needs to be updated.
	 */
	public void needsUpdate();
}
