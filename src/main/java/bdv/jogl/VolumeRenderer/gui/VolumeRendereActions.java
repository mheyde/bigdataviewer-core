package bdv.jogl.VolumeRenderer.gui;

import java.awt.event.ActionEvent;

import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;
import bdv.jogl.VolumeRenderer.scene.InteractionAnimator;
import bdv.util.AbstractNamedAction;

/**
 * Defines the BigDataViewer menu action for opening the 3D volume extension 
 * @author michael
 *
 */
public class VolumeRendereActions {

	public static class OpenVolumeRendererAction extends AbstractNamedAction{

		/**
		 * default version
		 */
		private static final long serialVersionUID = 1L;

		private final GLWindow window3D;
		
		private final SceneControlsWindow controls;
		
		private final InteractionAnimator animator;
		
		/**
		 * Constructor
		 * @param name
		 * @param win3d
		 * @param controls
		 * @param animator
		 */
		public OpenVolumeRendererAction(final String name, final GLWindow win3d, SceneControlsWindow controls, InteractionAnimator animator ) {
			super(name);
			window3D = win3d;
			this.controls = controls;
			this.animator = animator;
		}
		
		/**
		 * Opens the render and configuration window
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			animator.startInitAnimation();
			controls.setVisible(true);
			window3D.setVisible(true);
		}
	}	
}
