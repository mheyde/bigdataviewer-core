package bdv.jogl.VolumeRenderer.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import bdv.jogl.VolumeRenderer.GLErrorHandler;
import bdv.jogl.VolumeRenderer.shaderprogram.UnitCube;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;

/**
 * class to test the Unit cube implementation
 * @author michael
 *
 */
public class UnitCubeTest {

	private JFrame testWindow = new JFrame();

	private GLCanvas glCanvas = new GLCanvas();

	private BlockingQueue<Boolean> syncQueue = new ArrayBlockingQueue<Boolean>(1);

	@Before
	public void setup(){
		syncQueue.clear();

		testWindow.getContentPane().add(glCanvas);
		testWindow.setSize(100, 100);
	}

	@After
	public void tearDown() throws InterruptedException{
		syncQueue.clear();

		glCanvas.destroy();
		testWindow.dispose();

		Boolean test = syncQueue.poll(2,TimeUnit.MINUTES);
		assertEquals("There was an error while shutting down the test window", test,true);

	}

	@Test
	public void buildShaderProgramWithoutErrorTest() throws InterruptedException{
		final UnitCube cube = new UnitCube();

		GLEventListener glListener = new GLEventListener() {

			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y, int width,
					int height) {
				// TODO Auto-generated method stub

			}

			@Override
			public void init(GLAutoDrawable drawable) {
				GL4 gl2 = drawable.getGL().getGL4();

				cube.init(gl2);

				try {
					GLErrorHandler.assertGL(gl2);	
				} catch (Exception e) {
					try {
						e.printStackTrace();
						syncQueue.put(false);

					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			}

			@Override
			public void dispose(GLAutoDrawable drawable) {
				//everything ok
				try {
					syncQueue.put(true);

				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			@Override
			public void display(GLAutoDrawable drawable) {
				GL4 gl2 = drawable.getGL().getGL4();
		//		cube.update(gl2);
				cube.render(gl2);

				try {
					GLErrorHandler.assertGL(gl2);	
				} catch (Exception e) {
					try {
						e.printStackTrace();
						syncQueue.put(false);

					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

				//everything ok
				try {
					syncQueue.put(true);

				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		};


		glCanvas.addGLEventListener(glListener);

		testWindow.setVisible(true);

		Boolean test = syncQueue.poll(5,TimeUnit.SECONDS);
		assertEquals("There was an error while running the unit cube shader", test,true);

	}

	@Test
	public void buildMultipleShaderProgramsWithoutErrorTest() throws InterruptedException{
		final List<UnitCube> cubes = new LinkedList<UnitCube>();

		final int numberOfShaderPrograms = 3;
		for(int i = 0; i< numberOfShaderPrograms; i++ ){
			cubes.add(new UnitCube());
		}
		GLEventListener glListener = new GLEventListener() {



			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y, int width,
					int height) {
				// TODO Auto-generated method stub

			}

			@Override
			public void init(GLAutoDrawable drawable) {
				GL4 gl2 = drawable.getGL().getGL4();
				for(UnitCube cube:cubes){
					cube.init(gl2);

					try {
						GLErrorHandler.assertGL(gl2);	
					} catch (Exception e) {
						try {
							e.printStackTrace();
							syncQueue.put(false);

						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}

			@Override
			public void dispose(GLAutoDrawable drawable) {
				//everything ok
				try {
					syncQueue.put(true);

				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			@Override
			public void display(GLAutoDrawable drawable) {
				GL4 gl2 = drawable.getGL().getGL4();
				for(UnitCube cube:cubes){
				//	cube.update(gl2);
					cube.render(gl2);

					try {
						GLErrorHandler.assertGL(gl2);	
					} catch (Exception e) {
						try {
							e.printStackTrace();
							syncQueue.put(false);

						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				//everything ok
				try {
					syncQueue.put(true);

				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		};


		glCanvas.addGLEventListener(glListener);

		testWindow.setVisible(true);

		Boolean test = syncQueue.poll(5, TimeUnit.SECONDS);
		assertEquals("There was an error while running the unit cube shader", test,true);

	}


}
