package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.Test;

import bdv.jogl.VolumeRenderer.FrameBufferRedirector;
import bdv.jogl.VolumeRenderer.GLErrorHandler;
import bdv.jogl.VolumeRenderer.scene.AbstractScene;
import bdv.jogl.VolumeRenderer.scene.Camera;
import bdv.jogl.VolumeRenderer.scene.SimpleScene;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;

/**
 * Test of frame buffer wrapper
 * @author michael
 *
 */
public class FrameBufferRedirectorTest {

	//camera defaults
	private final float[] eye = {0,0,4};

	private final float[] center = {0,0,0};

	private final float[] up = {0,1,0};

	private final float znear = 0.1f;

	private final float zfar = 10f;

	private float[][][] result;
	
	private AbstractScene testScene = new SimpleScene();  

	private JFrame testwindow = new JFrame();

	private GLCanvas glCanvas = new GLCanvas();

	private FrameBufferRedirector redirector = new FrameBufferRedirector(); 

	private final int testSize = 10;

	private BlockingQueue<Boolean> syncQueue = new ArrayBlockingQueue<Boolean>(1);

	@Before
	public void setup(){
		testwindow.getContentPane().add(glCanvas);


		GLEventListener listener = new GLEventListener() {

			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y, int width,
					int height) { 
				Camera camera = testScene.getCamera();
				camera.setWidth(width);
				camera.setHeight(height);
				camera.init();
			}

			@Override
			public void init(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL4 gl2 = gl.getGL4();
				int width = drawable.getSurfaceWidth();
				int height = drawable .getSurfaceHeight();

				//camera init
				Camera camera = testScene.getCamera();
				camera.setWidth(width);
				camera.setHeight(height);
				camera.setZnear(znear);
				camera.setZfar(zfar);
				camera.setEyePoint(eye);
				camera.setLookAtPoint(center);
				camera.setLookAtPoint(up);
				camera.init();

				testScene.setBackgroundColor(Color.GREEN);

				//redirector
				redirector.setHeight(height);
				redirector.setWidth(width);;
				redirector.init(gl2);

				GLErrorHandler.assertGL(gl2);

			}

			@Override
			public void dispose(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL4 gl2 = gl.getGL4();

				result = redirector.getFrameBufferContent(gl2);

				GLErrorHandler.assertGL(gl2);
				try {
					syncQueue.put(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void display(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL4 gl2 = gl.getGL4();
				
				gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

				//cube.render(gl2);
				redirector.render(gl2);

				GLErrorHandler.assertGL(gl2);
				try {
					syncQueue.put(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} ;

		redirector.setScene(testScene);

		testwindow.setSize(testSize, testSize);
		glCanvas.addGLEventListener(listener);
	}

	@Test
	public void readTest() throws InterruptedException {
		testwindow.setSize(100, 100);
		testwindow.setVisible(true);
	
		syncQueue.poll(20, TimeUnit.SECONDS);

		glCanvas.destroy();
		testwindow.dispose();

		syncQueue.poll(20, TimeUnit.SECONDS);

		for(int y =0; y < redirector.getHeight(); y++){
			for(int x =0; x < redirector.getWidth(); x++){

				Color currentColor = new Color(result[x][y][0],result[x][y][1],result[x][y][2],result[x][y][3]);
				assertEquals(currentColor, Color.GREEN);
			}
		}
	}

}
