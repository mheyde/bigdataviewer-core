package bdv.util.benchmark;

import java.sql.Time;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public abstract class Benchmark {

	private int repeatitions = 1;
	private TimeUnit minTimeUnit = TimeUnit.SECONDS;
	private int minTime = 0;
	private int numberOfWarmupSteps = 0;
	private BenchStatistic result = new BenchStatistic();
	private double values[] = new double[repeatitions];

	private interface BenchmarkState{
		public void doStep();
	};

	private class WarmupState implements BenchmarkState{

		@Override
		public void doStep() {
			for(int i  = 0; i < numberOfWarmupSteps; i++){
				functionToMeasure();
			}
		}	
	}
	private class MeasureState implements BenchmarkState{

		@Override
		public void doStep() {
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
		}	
	}


	
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
	
	private class EvaluationState implements BenchmarkState{
		@Override
		public void doStep() {
			result = evaluateResults(values);
			
		}

	} 

	public void start() {
		BenchmarkState states[] ={new WarmupState(),new MeasureState(),new EvaluationState()}; 
		for(BenchmarkState state: states){
			state.doStep();
		}
		

	}
	public abstract void functionToMeasure();

	public void setRepeatitions(int repeatitions) {
		this.repeatitions = repeatitions;
	}

	public void setMinTimeElapse(int number, TimeUnit unit) {
		this.minTimeUnit = unit;
		this.minTime = number;
	}

	public void setWarmupSteps(int i) {
		numberOfWarmupSteps = i;
		values = new double[repeatitions];
	}

	public double getMean() {
		return result.mean;
	}

	public double getMedian() {
		return result.median;
	}

	public double getMax() {
		return result.max;
	}

	public double getMin() {
		return result.min;
	}

	public double getStandardDerivation() {
		return result.stdder;
	}

	public double[] getSeconds() {
		return values;
	}

}
