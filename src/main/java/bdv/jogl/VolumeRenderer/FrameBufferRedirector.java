package bdv.jogl.VolumeRenderer;

import java.nio.FloatBuffer;

import bdv.jogl.VolumeRenderer.scene.AbstractScene;
import bdv.jogl.VolumeRenderer.scene.SimpleScene;

import com.jogamp.opengl.FBObject;
import com.jogamp.opengl.FBObject.Colorbuffer;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;

/**
 * Provides methods to redirect the frame buffer to a cpu readable format mainly for test purposes 
 * @author michael
 *
 */
public class FrameBufferRedirector {
	
	private FBObject internalFrameBuffer;
	
	private AbstractScene scene =  new SimpleScene();

	private int width = 480;
	
	private int height = 640;
	
	private Colorbuffer buffer;
	
	/**
	 * initializes the gl bindings
	 * @param gl2 the gl context to use
	 */
	private void initFrameBufferObject(GL4 gl2){	
		buffer = FBObject.createColorTextureAttachment(gl2, true, width, height);
		buffer.initialize(gl2);
		internalFrameBuffer = new FBObject();
		internalFrameBuffer.init(gl2, width, height, 0);
		internalFrameBuffer.attachColorbuffer(gl2, 0, buffer);
		internalFrameBuffer.bind(gl2);
	
	}
	/**
	 * deletes the gl bindings 
	 * @param gl2 the gl context to use
	 */
	private void disposeFrameBufferObject(GL4 gl2){
		internalFrameBuffer.destroy(gl2);
	}
	
	/**
	 * @return the scene to render
	 */
	public AbstractScene getScene() {
		return scene;
	}


	/**
	 * @param scene the scene to set
	 */
	public void setScene(AbstractScene scene) {
		this.scene = scene;
	}
	
	
	/**
	 * init wrapper
	 * @param gl2 the gl context to use
	 */
	public void init(GL4 gl2){
		initFrameBufferObject(gl2);
		scene.init(gl2, width, height);
	}
	
	/**
	 * render wrapper
	 * @param gl2 the gl context to use
	 */
	public void render(GL4 gl2){
		internalFrameBuffer.bind(gl2);
		scene.render(gl2);
		internalFrameBuffer.unbind(gl2);
	}
	
	/**
	 * dispose wrapper
	 * @param gl2 the gl context to use
	 */
	public void disposeGL(GL4 gl2){
		scene.dispose(gl2);
		disposeFrameBufferObject(gl2);
		buffer.free(gl2);
	}
	
	/**
	 * returns the current content of the frame buffer as a matrix. x,y rgba
	 * @param gl2 the gl context to use
	 * @return the 2D rgba image of the rendered scene. first is x , then y, then rgba dimension
	 */
	public float[][][] getFrameBufferContent(GL4 gl2){
		internalFrameBuffer.bind(gl2);
		float[][][] matrix = new float[width][height][4];
		FloatBuffer buffer = FloatBuffer.allocate(height*width*4);
		gl2.glReadPixels(0, 0, width, height, GL2.GL_RGBA, GL2.GL_FLOAT, buffer);
		buffer.rewind();
		
		//to array
		for(int y =0; y< height; y++){
			for(int x = 0; x < width; x++){
				for(int c =0; c < 4; c++){
					matrix[x][y][c] = buffer.get((y*width + x)*4+c);
				}
			}
		}
		
		internalFrameBuffer.unbind(gl2);
		return matrix;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
}
