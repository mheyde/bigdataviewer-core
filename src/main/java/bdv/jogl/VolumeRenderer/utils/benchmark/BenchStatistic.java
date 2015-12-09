package bdv.jogl.VolumeRenderer.utils.benchmark;

/**
 * Class storing the statistics of a benchmark result
 * @author michael
 *
 */
public class BenchStatistic {

	/**
	 * minimal time
	 */
		public double min = 0.0;
		
		/**
		 * maximal time
		 */
		public double max = 0.0;
		
		/**
		 * mean time
		 */
		public double mean = 0.0;
		
		/**
		 * median time
		 */
		public double median = 0.0;
		
		/**
		 * standard derivation of the time values
		 */
		public double stdder = 0.0;

}
