package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.transferfunctioninterpreter;

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.AbstractShaderFunction;

/**
 * defines syntax for GPU tf value desampler
 * @author michael
 *
 */
public abstract class AbstractTransferFunctionInterpreter extends AbstractShaderFunction {
	
	protected AbstractTransferFunctionInterpreter(){
		super("desampler");
	}
	
	
}
