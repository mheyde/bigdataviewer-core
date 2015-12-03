package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.Test;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.glsl.ShaderCode;

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.UnitCubeShaderSource;

public class UnitCubeShaderSourceTest {

	private final BlockingQueue<Boolean> sync = new ArrayBlockingQueue<Boolean>(1);
	
	private JFrame testWindow;
	
	private GLCanvas glcanvas;
	
	private GLEventListener shaderBuilder = new GLEventListener() {
		
		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void init(GLAutoDrawable drawable) {
			GL gl = drawable.getGL();
			GL gl2 = gl.getGL2();
			UnitCubeShaderSource shaderSource = new UnitCubeShaderSource();
			
			boolean result = true;
			for(ShaderCode code : shaderSource.getShaderCodes()){

				result = (result && code.compile(gl2.getGL2ES2(), System.err));
			}
			try {
				sync.put(result);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		@Override
		public void dispose(GLAutoDrawable drawable) {
			try {
				sync.put(true);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		@Override
		public void display(GLAutoDrawable drawable) {
			// TODO Auto-generated method stub
			
		}
	};
	@Before
	public void setup(){
		glcanvas = new GLCanvas();
		
		testWindow = new JFrame("test window mvr");
		testWindow.getContentPane().add(glcanvas);
		testWindow.setSize(100, 100);
		
	}
	
	@Test
	public void compileTest() throws InterruptedException {
		testWindow.setVisible(true);
		glcanvas.addGLEventListener(shaderBuilder);
		Boolean syncVal=false;
		
		syncVal = sync.poll(10, TimeUnit.SECONDS);
		assertTrue(syncVal);
		
		testWindow.dispose();
		glcanvas.destroy();
		syncVal = sync.poll(10, TimeUnit.SECONDS);
		assertTrue(syncVal);
	}
}
