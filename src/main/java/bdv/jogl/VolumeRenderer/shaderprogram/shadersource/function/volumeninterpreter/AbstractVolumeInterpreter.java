package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.volumeninterpreter;

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.AbstractShaderFunction;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.AbstractVolumeAccumulator;

public abstract class AbstractVolumeInterpreter extends AbstractShaderFunction{

	protected AbstractVolumeAccumulator accumulator;
	protected AbstractVolumeInterpreter(String functionName) {
		super(functionName);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param accumulator the accumulator to set
	 */
	public void setAccumulator(AbstractVolumeAccumulator accumulator) {
		this.accumulator = accumulator;
	}
	
	

}
