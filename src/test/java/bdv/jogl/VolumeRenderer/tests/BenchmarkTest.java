package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import bdv.jogl.VolumeRenderer.utils.benchmark.Benchmark;

public class BenchmarkTest {

	@Test
	public void test() {
		TimeUnit tu=TimeUnit.MILLISECONDS;
		int time = 10;
		final int numberOfMeasurements = 100;
		Benchmark b = new Benchmark(){
			
			public void functionToMeasure(){
				Math.pow(2, 100);
			}

		};
		b.setMinTimeElapse(time,tu);
		b.setRepeatitions(numberOfMeasurements);
		b.setWarmupSteps(60);
		long begin = System.nanoTime();
		b.start();
		long end = System.nanoTime();
		double mean = b.getMean();
		double median = b.getMedian();
		double max = b.getMax();
		double min = b.getMin();
		double stderiv = b.getStandardDerivation();
		assertEquals(numberOfMeasurements, b.getSeconds().length);
		for(double d : b.getSeconds()){
			assertTrue(d >= 0.0);
		}
		assertNotEquals(0.0, min,0.001);
		assertTrue(max >= min);
		assertTrue(max >= mean && mean>=min);
		assertTrue(max >= median && median>=min);
		assertTrue(stderiv >= 0.0);
		assertTrue(end -begin >= TimeUnit.NANOSECONDS.convert(time, tu));
	}	

}
