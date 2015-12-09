package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.transferfunctionclassifier;

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.AbstractShaderFunction;

/**
 * defines syntax for GPU transfer function value classification 
 * @author michael
 *
 */
public abstract class AbstractTransferFunctionClassifier extends AbstractShaderFunction {
	
	/**
	 * constructor
	 */
	protected AbstractTransferFunctionClassifier(){
		super("transferFunctionClassifier");
	}	
}
