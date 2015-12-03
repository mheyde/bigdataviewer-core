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

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.ISourceListener;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.AbstractVolumeAccumulator;
import bdv.jogl.VolumeRenderer.transferfunction.TransferFunction1D;
import bdv.jogl.VolumeRenderer.transferfunction.sampler.PreIntegrationSampler;
import bdv.jogl.VolumeRenderer.transferfunction.sampler.RegularSampler;

public class MultiVolumeRendererShaderSourceTest {

	private final BlockingQueue<Boolean> sync = new ArrayBlockingQueue<Boolean>(1);
	
	private JFrame testWindow;
	
	private GLCanvas glcanvas;
	
	int counter;
	
	private GLEventListener shaderBuilder = new GLEventListener() {
		
		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
		}
		
		@Override
		public void init(GLAutoDrawable drawable) {
			GL gl = drawable.getGL();
			GL gl2 = gl.getGL2();
			MultiVolumeRendererShaderSource shaderSource = new MultiVolumeRendererShaderSource();
			shaderSource.setTransferFunctionCode(new TransferFunction1D().getTransferFunctionShaderCode());
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
		counter = 0;
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
	
	@Test
	public void listenerTest(){
		MultiVolumeRendererShaderSource source = new MultiVolumeRendererShaderSource();
		source.addSourceListener(new ISourceListener() {
			
			@Override
			public void sourceCodeChanged() {
				counter++;
			}
		});
		int outCounter = 0;
		source.setMaxNumberOfVolumes(0);
		assertEquals(counter, ++outCounter);
		source.setMaxNumberOfVolumes(2);
		assertEquals(counter, ++outCounter);
		
		//no change on same number
		source.setMaxNumberOfVolumes(2);
		assertEquals(counter, outCounter);
		
		source.setShaderLanguageVersion(0);
		assertEquals(counter, ++outCounter);
		source.setShaderLanguageVersion(3);
		assertEquals(counter, ++outCounter);
		
		//no change
		source.setShaderLanguageVersion(3);
		assertEquals(counter, outCounter);
		
		source.setTransferFunctionCode(new RegularSampler().getShaderCode());
		assertEquals(counter, ++outCounter);
		source.setTransferFunctionCode(new PreIntegrationSampler().getShaderCode());
		assertEquals(counter, ++outCounter);
		
		//no change
		source.setTransferFunctionCode(new PreIntegrationSampler().getShaderCode());
		assertEquals(counter, outCounter);
		
		AbstractVolumeAccumulator a1 = new AbstractVolumeAccumulator("fo") {
			
			@Override
			public String[] declaration() {
				return new String[]{"fooo"};
			}
		};
		
		AbstractVolumeAccumulator a2 = new AbstractVolumeAccumulator("barrr") {
			
			@Override
			public String[] declaration() {
				return new String[]{"bar"};
			}
		};
		source.setAccumulator(a1 );
		assertEquals(counter, ++outCounter);
		source.setAccumulator(a2 );
		assertEquals(counter, ++outCounter);
		
		source.setAccumulator(a2 );
		assertEquals(counter, outCounter);
	}
	
	
	
}
