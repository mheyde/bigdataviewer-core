package bdv.jogl.VolumeRenderer.gui.VDataAccumulationPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.AbstractVolumeAccumulator;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.AverageVolumeAccumulator;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.MaxCurvatureDifferenceAccumulator;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.MaxDifferenceAccumulator;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.MaximumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.MidmapMaxDifferenceAccumulator;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.MinimumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.SharpnessVolumeAccumulator;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.ViewDirectionAccumulator;

/**
 * Stores aggregators and calls listeners
 * @author michael
 *
 */
public class AccumulatorManager {
	
	private List<IVolumeAccumulatorListener> listeners = new ArrayList<IVolumeAccumulatorListener>();
	
	private Map<String, AbstractVolumeAccumulator> accumulators = new HashMap<String, AbstractVolumeAccumulator>();
	
	private List<String> aggregatorNames = new ArrayList<String>();
	
	private String activeAccumulatorName;
	
	/**
	 * Informs all listeners of accumulator changes 
	 */
	private void notifyChangedAll(){
		for(IVolumeAccumulatorListener listener: listeners){
			listener.aggregationChanged(accumulators.get(activeAccumulatorName));
		}
	}
	
	/**
	 * Stores the an accumulator
	 * @param a the accumulator instance to store 
	 */
	private void addAccumulator(AbstractVolumeAccumulator a){
		String visualName = beautifyaccumulatorFuncName(a.getFunctionName());
		accumulators.put(visualName, a);
		aggregatorNames.add(visualName);
	}
	
	/**
	 * Beautifies the function name of an accumulator (Upper case first letter, changes '_' to ' ')
	 * @param functionName the function name of the accumulator
	 * @return the beautyfied function name
	 */
	private String beautifyaccumulatorFuncName(String functionName) {
		String beautified =""+ Character.toUpperCase( (char)functionName.getBytes()[0]);
		
		for(int i =1; i < functionName.length(); i++){
			if(functionName.getBytes()[i] == (char)'_'){
				beautified += ' ';
				continue;
			}
			beautified +=  (char)functionName.getBytes()[i];
		}
		return beautified;
	}

	/**
	 * Constructor
	 */
	public AccumulatorManager(){
		MaximumVolumeAccumulator max =new MaximumVolumeAccumulator();
	
		addAccumulator(new MaximumVolumeAccumulator());
		addAccumulator(new MinimumVolumeAccumulator());
		addAccumulator(new AverageVolumeAccumulator());
		addAccumulator(new ViewDirectionAccumulator());
		addAccumulator(new SharpnessVolumeAccumulator());
		addAccumulator(new MaxDifferenceAccumulator());
		addAccumulator(new MidmapMaxDifferenceAccumulator());
		addAccumulator(new MaxCurvatureDifferenceAccumulator());

		setActiveAcumulator( beautifyaccumulatorFuncName(max.getFunctionName()));
	}
	
	/**
	 * Adds aggregation listener
	 * @param listener the listener instance to add
	 */
	public void addListener(IVolumeAccumulatorListener listener){
		this.listeners.add(listener);
	} 
	
	/**
	 * Returns the function name of the currently active accumulator
	 * @return the name of the current active accumulator
	 */
	public String getActiveAccumulator(){
		return activeAccumulatorName;
	}
	
	/**
	 * Set the current active accumulator and calls the changed listener 
	 * @param name the name to set
	 */
	public void setActiveAcumulator(String name){
		activeAccumulatorName = name;
		notifyChangedAll();
	}
	
	/**
	 * returns a accumulator identified by its funktion name or null if not present
	 * @param name the name of the queried accumulator
	 * @return the accumulator instance
	 */
	public AbstractVolumeAccumulator getAccumulator(String name){
		return accumulators.get(name);
	};
	
	/**
	 * returns the function-names of the stored accumulators 
	 * @return a list of all usable accumulator names.
	 */
	public List<String> getAccumulatorNames(){
		return aggregatorNames;
	}
}
