package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.transferfunctioninterpreter;

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.AbstractShaderFunction;

/**
 * defines syntax for GPU transfer function value classification 
 * @author michael
 *
 */
public abstract class AbstractTransferFunctionClassifier extends AbstractShaderFunction {
	
	protected AbstractTransferFunctionClassifier(){
		super("transferFunctionClassifier");
	}
	
	
}
