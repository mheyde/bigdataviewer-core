package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.awt.geom.Point2D.Float;

import org.junit.Test;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;

public class TransferFunction1DTest {

	@Test
	public void borderCaseTest() {
		TransferFunction1D tf= new TransferFunction1D();
		assertNotEquals(true,tf.sampleColors().isEmpty());
		
		//all zero
		for(Float point: tf.getColors().keySet()){
			tf.moveColor(point, new Float(point.x, 0));
		}
		assertNotEquals(true,tf.sampleColors().isEmpty());
		
		//all one
		for(Float point: tf.getColors().keySet()){
			tf.moveColor(point, new Float(point.x, 1));
		}
		assertNotEquals(true,tf.sampleColors().isEmpty());
	}

}
