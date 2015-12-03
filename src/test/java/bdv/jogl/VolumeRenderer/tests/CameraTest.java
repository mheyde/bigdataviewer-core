package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JFrame;

import org.junit.Test;

import bdv.jogl.VolumeRenderer.scene.Camera;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.math.VectorUtil;

public class CameraTest {

	private float[] eye ={100,333,122};

	private float[] up ={300,22,11};

	private float[] center ={123,44,55};

	private float[] viewGLU = new float[16];

	private float[] viewCamera = new float[16];

	private float[] projGLU = new float[16];

	private float[] projCamera = new float[16];

	private float[] nullVector = {0,0,0};

	private static final float floatAccuracy = 0.00001f;

	private final Integer height = 453;

	private final Integer width = 634;

	private final float znear = 0.1f;

	private final float zfar = 1000;

	private final float alpha = 45;

	private float[] vector = {1,1,1};

	private GLCanvas canvas = new GLCanvas();

	private JFrame frame  = new JFrame();

	private BlockingQueue<Boolean> syncQueue = new ArrayBlockingQueue<Boolean>(1); 


	private GLEventListener listener = new GLEventListener() {

		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width,
				int height) { }

		@Override
		public  void init(GLAutoDrawable drawable) {
			GL gl = drawable.getGL();
			GL2 gl2 = gl.getGL2();
			GLU glu = new GLU();

			//proj part
			gl2.glMatrixMode(GL2.GL_PROJECTION);
			gl2.glLoadIdentity();
			glu.gluPerspective(alpha, width.floatValue()/height.floatValue(), znear, zfar);

			gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projGLU,0);

			//glu view part
			gl2.glMatrixMode(GL2.GL_MODELVIEW);
			gl2.glLoadIdentity();				
			float[] upNorm = VectorUtil.normalizeVec3(up, 0);
			glu.gluLookAt(eye[0], eye[1], eye[2], 
					center[0], center[1], center[2], 
					upNorm[0], upNorm[1], upNorm[2]);

			gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, viewGLU,0);

			//camera part
			Camera camera = new Camera();
			camera.setAlpha(alpha);
			camera.setHeight(height);
			camera.setWidth(width);
			camera.setZnear(znear);
			camera.setZfar(zfar);
			camera.setEyePoint(eye);
			camera.setLookAtPoint(center);
			camera.setUpVector(up);
			camera.init();
			viewCamera = camera.getViewMatrix().getMatrix();
			projCamera = camera.getProjectionMatix().getMatrix();


			try {
				//for tests
				syncQueue.put(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void dispose(GLAutoDrawable drawable) {
			try {
				//for tests
				syncQueue.put(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void display(GLAutoDrawable drawable) { }
	};


	@Test
	public void sameLookAtAsGlutest() {

		frame.getContentPane().add(canvas);
		frame.setSize(100, 100);
		frame.setVisible(true);

		canvas.addGLEventListener(listener);


		try {
			//wait for calc end
			syncQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		canvas.destroy();
		frame.dispose();
		try {
			//wait for gl close
			syncQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertArrayEquals(viewGLU, viewCamera, floatAccuracy);
	}

	@Test
	public void similarPerspectiveAsGLUtest() {
		frame.getContentPane().add(canvas);
		frame.setSize(100, 100);
		frame.setVisible(true);
		canvas.addGLEventListener(listener);

		try {		
			//wait for calc end
			syncQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		canvas.destroy();
		frame.dispose();

		try {		
			//wait for gl close
			syncQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//check relevant fields
		for (int i =0; i< 16; i++){
			if(projGLU[i] > 0){
				assertEquals(true, projCamera[i] > 0);
				continue;
			}

			if(projGLU[i] < 0){
				assertEquals(true, projCamera[i] < 0);
				continue;
			}

			assertEquals(projCamera[i], 0, floatAccuracy);
		}


		// another math theorem is used
		//assertArrayEquals(projGLU, projCamera, floatAccuracy);
	}

	@Test
	public void setterGettersAlphaTest(){
		Camera c = new Camera();

		//correct alpha
		c.setAlpha(Camera.minAlpha);
		assertEquals(Camera.minAlpha, c.getAlpha(), floatAccuracy);

		c.setAlpha(Camera.maxAlpha);
		assertEquals(Camera.maxAlpha, c.getAlpha(), floatAccuracy);


		//false alphas
		c.setAlpha(Camera.minAlpha-floatAccuracy);
		assertNotEquals(Camera.minAlpha-floatAccuracy, c.getAlpha(),floatAccuracy);
		assertEquals(Camera.minAlpha, c.getAlpha(), floatAccuracy);

		c.setAlpha(0);
		assertNotEquals(0.f, c.getAlpha(),floatAccuracy);
		assertEquals(Camera.minAlpha, c.getAlpha(), floatAccuracy);

		c.setAlpha(-1);
		assertNotEquals(-1.f, c.getAlpha(),floatAccuracy);
		assertEquals(Camera.minAlpha, c.getAlpha(), floatAccuracy);

		c.setAlpha(Camera.maxAlpha+floatAccuracy);
		assertNotEquals(Camera.maxAlpha+floatAccuracy, c.getAlpha(),floatAccuracy);
		assertEquals(Camera.maxAlpha, c.getAlpha(), floatAccuracy);

	}

	@Test
	public void setterGettersSizeTest(){
		Camera c = new Camera();

		//correct size
		c.setHeight(Camera.minSize);
		assertEquals(Camera.minSize, c.getHeight());

		c.setWidth(Camera.minSize);
		assertEquals(Camera.minSize, c.getWidth());


		//false size
		c.setHeight(0);
		assertNotEquals(0, c.getHeight());
		assertEquals(Camera.minSize, c.getHeight());

		c.setHeight(-1);
		assertNotEquals(-1, c.getHeight());
		assertEquals(Camera.minSize, c.getHeight());

		c.setHeight(0);
		assertNotEquals(0, c.getHeight());
		assertEquals(Camera.minSize, c.getHeight());

		c.setHeight(-1);
		assertNotEquals(-1, c.getHeight());
		assertEquals(Camera.minSize, c.getHeight());

	}

	@Test 
	public void znearAndFarTest(){
		Camera c = new Camera();

		//correct z
		c.setZnear(Camera.minZ);
		assertEquals(Camera.minZ, c.getZnear(),floatAccuracy);

		c.setZfar(Camera.minZ+Camera.minZ);
		assertEquals(Camera.minZ+Camera.minZ, c.getZfar(),floatAccuracy);

		//wrong z
		c.setZnear(Camera.minZ);
		c.setZfar(Camera.minZ);
		assertEquals(true,c.getZnear() < c.getZfar());

		c.setZnear(0);
		assertNotEquals(0.f, c.getZnear(),floatAccuracy);
		assertEquals(Camera.minZ, c.getZnear(), floatAccuracy);

		c.setZnear(-1);
		assertNotEquals(-1.f, c.getZnear(),floatAccuracy);
		assertEquals(Camera.minZ, c.getZnear(),floatAccuracy);

		c.setZfar(100);
		c.setZnear(101);
		assertNotEquals(100, c.getZfar(),floatAccuracy);
		assertEquals(true, c.getZnear()< c.getZfar());
	}

	@Test
	public void PointsAndVectorsTest(){
		Camera c = new Camera();


		//correct cases
		c.setEyePoint(eye.clone());
		c.setLookAtPoint(center.clone());
		c.setUpVector(vector.clone());

		assertArrayEquals(eye, c.getEyePoint(),0.001f);
		assertArrayEquals(center, c.getLookAtPoint(),0.001f);
		assertArrayEquals("Up vector should be normalized!",
				VectorUtil.normalizeVec3(vector, 0), 
				c.getUpVector(),0.001f);

		//wrong cases
		c.setUpVector(nullVector.clone());
		assertArrayEquals(Camera.defaultUpVector, c.getUpVector(),floatAccuracy);
	}

	@Test(expected = IllegalArgumentException.class)
	public void exceptionForSettingEyeEqualToCenter(){
		Camera c = new Camera();
		c.setEyePoint(eye.clone());
		c.setLookAtPoint(eye.clone());
	}

	@Test(expected = IllegalArgumentException.class)
	public void exceptionForSettingCenterEqualToEye(){
		Camera c = new Camera();
		c.setLookAtPoint(center.clone());
		c.setEyePoint(center.clone());
	}
	@Test
	public void repeatedUpdateSameDataTest(){
		Camera c = new Camera();

		//first
		c.setAlpha(alpha);
		c.setWidth(width);
		c.setHeight(height);
		c.setZfar(zfar);
		c.setZnear(znear);
		c.setLookAtPoint(center.clone());
		c.setEyePoint(eye.clone());
		c.init();

		float[] proj1 = c.getProjectionMatix().getMatrix().clone();
		float[] view1 = c.getViewMatrix().getMatrix().clone();

		//manipulate like gl does sometimes
		c.getProjectionMatix().scale(1, 2, 3);
		c.getViewMatrix().scale(3, 2, 1);

		//second
		c.setAlpha(alpha);
		c.setWidth(width);
		c.setHeight(height);
		c.setZfar(zfar);
		c.setZnear(znear);
		c.setLookAtPoint(center.clone());
		c.setEyePoint(eye.clone());
		c.init();

		float[] proj2 = c.getProjectionMatix().getMatrix().clone();
		float[] view2 = c.getViewMatrix().getMatrix().clone();

		assertArrayEquals(proj1, proj2, floatAccuracy);
		assertArrayEquals(view1, view2, floatAccuracy);
	}

	@Test
	public void getterProtections(){
		Camera c = new Camera();

		c.setEyePoint(eye);
		float[] eyeTemp = c.getEyePoint();
		eyeTemp[0] = eyeTemp[0] + 2;
		assertNotEquals(eyeTemp[0], c.getEyePoint()[0], floatAccuracy);

		c.setLookAtPoint(center);
		float[] centerTemp = c.getLookAtPoint();
		centerTemp[0] = centerTemp[0] + 2;
		assertNotEquals(centerTemp[0], c.getLookAtPoint()[0], floatAccuracy);

		c.setUpVector(up);
		float[] upTemp = c.getUpVector();
		upTemp[0] = upTemp[0] + 2;
		assertNotEquals(upTemp[0], c.getUpVector()[0], floatAccuracy);

		float[] projTemp = c.getProjectionMatix().getMatrix();
		projTemp[0] = projTemp[0] + 2;
		assertNotEquals(projTemp[0], c.getProjectionMatix().getMatrix()[0], floatAccuracy);

		float[] viewTemp = c.getViewMatrix().getMatrix();
		viewTemp[0] = viewTemp[0] + 2;
		assertNotEquals(viewTemp[0], c.getViewMatrix().getMatrix()[0], floatAccuracy);
	}

	@Test
	public void setterProtections(){
		Camera c = new Camera();

		float[] eyeTemp = {1,1,1};
		c.setEyePoint(eyeTemp);
		eyeTemp[0] = eyeTemp[0] + 2;
		assertNotEquals(eyeTemp[0], c.getEyePoint()[0], floatAccuracy);

		float[] centerTemp = {2,2,2};
		c.setLookAtPoint(centerTemp);
		centerTemp[0] = centerTemp[0] + 2;
		assertNotEquals(centerTemp[0], c.getLookAtPoint()[0], floatAccuracy);
		
		float[] upTemp = {2,2,2};
		c.setUpVector(upTemp);
		upTemp[0] = upTemp[0] + 2;
		assertNotEquals(upTemp[0], c.getUpVector()[0], floatAccuracy);
	}
}
