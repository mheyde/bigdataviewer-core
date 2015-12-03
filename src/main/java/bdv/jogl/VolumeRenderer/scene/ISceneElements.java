package bdv.jogl.VolumeRenderer.scene;


import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.Matrix4;

/**
 * delivers standard methods for gl scene elements
 * @author michael
 *
 */
public interface ISceneElements {

	/**
	 * releases all gl resources
	 * @param gl2
	 */
	public void disposeGL(GL4 gl2);
	
	/**
	 * Updates the mandatory data and renders the scene element
	 * @param gl2
	 */
	public void render(GL4 gl2);
	
	/**
	 * initializes the scene element context
	 * @param gl
	 */
	public void init(GL4 gl);

	/**
	 * set the model transformation matrix of the current element
	 * @param modelTransformations the modelTransformations to set
	 */
	public void setModelTransformation(Matrix4 modelTransformations);
	
	/**
	 * set the projection transformation matrix of the current element
	 * @param projection the projection to set
	 */
	public void setProjection(Matrix4 projection);

	/**
	 * set the view transformation matrix of the current element
	 * @param view the view to set
	 */
	public void setView(Matrix4 view);
	
}
