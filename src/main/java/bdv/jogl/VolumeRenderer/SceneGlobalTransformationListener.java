package bdv.jogl.VolumeRenderer;



import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;
import bdv.jogl.VolumeRenderer.shaderprogram.MultiVolumeRenderer;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;
/**
 * Listener for global transformation changes
 * @author michael
 *
 */
public class SceneGlobalTransformationListener implements TransformListener<AffineTransform3D> {

	private final MultiVolumeRenderer renderer;
	private final GLWindow window;
	
	/**
	 * add scene to transfer data
	 * @param scene
	 */
	public SceneGlobalTransformationListener(final MultiVolumeRenderer renderer, final GLWindow window){
		this.renderer = renderer;
		this.window = window;
	}
	
	@Override
	public void transformChanged(AffineTransform3D transform) {
		renderer.setModelTransformation(convertToJoglTransform(transform));
		window.getGlCanvas().repaint();
	}
}
