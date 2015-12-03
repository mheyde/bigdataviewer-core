package bdv.jogl.VolumeRenderer.shaderprogram;

import java.util.EventListener;

import com.jogamp.opengl.math.geom.AABBox;
/**
 * callbacks for the multi volume renderer
 * @author michael
 *
 */
public interface IMultiVolumeRendererListener extends EventListener {

	public void drawRectChanged(AABBox drawRect);
}
