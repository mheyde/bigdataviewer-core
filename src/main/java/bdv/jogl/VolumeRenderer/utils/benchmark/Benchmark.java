package bdv.jogl.VolumeRenderer.utils.benchmark;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * An abstract benchmarking class
 * @author michael
 *
 */
public abstract class Benchmark {

	private int repeatitions = 1;
	private TimeUnit minTimeUnit = TimeUnit.SECONDS;
	private int minTime = 0;
	private int numberOfWarmupSteps = 0;
	private BenchStatistic result = new BenchStatistic();
	private double values[] = new double[repeatitions];

	/**
	 * class defining the state of a benchmark
	 * @author michael
	 *
	 */
	private interface BenchmarkState{

		/**
		 * make one action in the state
		 */
		public void doStep();
	};

	/**
	 * start state which just runs the test function without measurements
	 * @author michael
	 *
	 */
	private class WarmupState implements BenchmarkState{

		@Override
		public void doStep() {
			System.out.print("Warmup ");
			for(int i  = 0; i < numberOfWarmupSteps; i++){
				functionToMeasure();
			}
			System.out.println("done");
		}	
	}

	/**
	 * state which just runs the test function and does measurements
	 * @author michael
	 *
	 */
	private class MeasureState implements BenchmarkState{

		@Override
		public void doStep() {
			System.out.print("Measurement ");
			long current;
			long nanosToElapse = TimeUnit.NANOSECONDS.convert(minTime, minTimeUnit);
			for(int measurement =0; measurement < repeatitions; measurement++){
				int iter = -1;
				long begin = System.nanoTime();
				do{

					current = System.nanoTime();
					functionToMeasure();
					iter++;
				}while(nanosToElapse > current-begin || iter <1 );
				values[measurement] = ((double)(current-begin))/((double)iter);
			}
			System.out.println("done");
		}	
	}

	/**
	 * evaluate statistics on the data
	 * @param inValues the run times of the measurements
	 * @return the statistic
	 */
	public static BenchStatistic evaluateResults(double[] inValues){
		double[] fpsField = inValues.clone();
		BenchStatistic result = new BenchStatistic();
		//sort for median
		Arrays.sort(fpsField);

		result.max = Double.MIN_VALUE;
		result.min = Double.MAX_VALUE;
		result.mean = 0;
		result.median =0;
		//median 
		if(fpsField.length%2 ==0){
			//even
			result.median= 0.5* (fpsField[fpsField.length/2-1]+fpsField[fpsField.length/2]);

		}else{
			//uneven
			result.median = fpsField[(int)Math.floor(fpsField.length/2)];
		}

		HashMap<Double,Long>histogram  = new HashMap<Double, Long>();
		for(int i = 0; i<fpsField.length;i++ ){
			//min
			result.min = Math.min(result.min, fpsField[i]);

			//max
			result.max = Math.max(result.max, fpsField[i]);

			//mean acc
			result.mean += fpsField[i];

			//hist
			if(!histogram.containsKey(fpsField[i])){
				histogram.put(fpsField[i], 0l);
			}
			histogram.put(fpsField[i],1l + histogram.get(fpsField[i]));

			fpsField[i] = -1;
		}
		result.mean/=((double)fpsField.length);

		//variance
		result.stdder = 0;
		for(Double fps : histogram.keySet()){
			Long occurence = histogram.get(fps);
			double pi = (((double)occurence)/((double)fpsField.length));
			result.stdder +=Math.pow(fps - result.stdder,2.0)*pi;
		}
		result.stdder = Math.sqrt(result.stdder); 
		return result;
	}

	/**
	 * last state of the benchmark evaluating the results
	 * @author michael
	 *
	 */
	private class EvaluationState implements BenchmarkState{
		@Override
		public void doStep() {
			System.out.print("evaluation ");
			result = evaluateResults(values);
			System.out.println("done");
		}

	} 

	/**
	 * starts the benchmark in warmup state
	 */
	public void start() {
		BenchmarkState states[] ={new WarmupState(),new MeasureState(),new EvaluationState()}; 
		for(BenchmarkState state: states){
			state.doStep();
		}


	}
	
	/**
	 * the function which run time should be measured
	 */
	public abstract void functionToMeasure();

	/**
	 * set the amount of repeatitions for the measurement
	 * @param repeatitions number of repeatitions
	 */
	public void setRepeatitions(int repeatitions) {
		this.repeatitions = repeatitions;
	}

	/**
	 * set minimal amount of time to pass under measurement until the measurement is valid
	 * @param number number of time stamps to pass
	 * @param unit the unit of the time stamps
	 */
	public void setMinTimeElapse(int number, TimeUnit unit) {
		this.minTimeUnit = unit;
		this.minTime = number;
	}

	/**
	 * set the amount of steps in the warmup phase
	 * @param i the steps
	 */
	public void setWarmupSteps(int i) {
		numberOfWarmupSteps = i;
		values = new double[repeatitions];
	}

	/**
	 * @return the mean of the current measurement times
	 */
	public double getMean() {
		return result.mean;
	}

	/**
	 * @return the median of the current measurement times
	 */
	public double getMedian() {
		return result.median;
	}

	/**
	 * @return the maximum of the current measurement times
	 */
	public double getMax() {
		return result.max;
	}

	/**
	 * @return the minimum of the current measurement times
	 */
	public double getMin() {
		return result.min;
	}

	/**
	 * @return the standard derivation of the current measurement times
	 */
	public double getStandardDerivation() {
		return result.stdder;
	}

	/**
	 * @return run times of the current measurement
	 */
	public double[] getSeconds() {
		return values;
	}

	public String toString(){
		return "" + result.min+ " " +result.max+" "+result.mean + " "+result.median +" " + result.stdder;
	}

}
