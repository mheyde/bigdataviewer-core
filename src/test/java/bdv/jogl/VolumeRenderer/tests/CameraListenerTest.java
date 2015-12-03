package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import bdv.jogl.VolumeRenderer.scene.Camera;
import bdv.jogl.VolumeRenderer.scene.CameraListener;

import com.jogamp.opengl.math.Matrix4;

public class CameraListenerTest {
	
	private final float floatAccuracy = 0.0001f;
	
	private int calledProject;
	
	private int calledView;
	
	private Matrix4 proj;
	
	private Matrix4 view;
	
	private CameraListener both = new CameraListener() {
		
		@Override
		public void viewMatrixUpdate(Matrix4 matrix) {
			calledView++;
			view = matrix;
		}
		
		@Override
		public void projectionMatrixUpdate(Matrix4 matrix) {
			calledProject++;
			proj = matrix;
		}
	};
	
	@Before
	public void setup(){
		calledProject = 0;
		calledView = 0;
	}
	
	@Test
	public void Test() {
		Camera c = new Camera();
		
		assertFalse(c.addCameraListener(null));
		assertTrue(c.addCameraListener(both));
		
		c.updatePerspectiveMatrix();
		assertArrayEquals(c.getProjectionMatix().getMatrix(), proj.getMatrix(), floatAccuracy);
		
		assertEquals(1, calledProject);
		assertEquals(0, calledView);
		
		c.updateViewMatrix();
		assertArrayEquals(c.getViewMatrix().getMatrix(), view.getMatrix(), floatAccuracy);
		
		assertEquals(1, calledProject);
		assertEquals(1, calledView);
		
		c.init();
		
		assertEquals(2, calledProject);
		assertEquals(2, calledView);
		
		c.clearCameraListeners();
		c.init();
		
		assertEquals(2, calledProject);
		assertEquals(2, calledView);
	}

}
