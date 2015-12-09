package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.volumeninterpreter;

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.AbstractShaderFunction;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.AbstractVolumeAccumulator;

/**
 * Abstract class defining a volume interpreter 
 * @author michael
 *
 */
public abstract class AbstractVolumeInterpreter extends AbstractShaderFunction{

	/**
	 * the current accumulator in use
	 */
	protected AbstractVolumeAccumulator accumulator;
	
	/**
	 * constructor using the shader function class interface 
	 * @param functionName the function name of the volume interpreter 
	 */
	protected AbstractVolumeInterpreter(String functionName) {
		super(functionName);
	}
	
	/**
	 * @param accumulator the accumulator to set
	 */
	public void setAccumulator(AbstractVolumeAccumulator accumulator) {
		this.accumulator = accumulator;
	}
	
	

}
