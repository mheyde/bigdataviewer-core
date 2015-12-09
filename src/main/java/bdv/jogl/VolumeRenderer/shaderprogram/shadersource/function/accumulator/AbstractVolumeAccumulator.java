package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator;

import com.jogamp.opengl.GL4;

import bdv.jogl.VolumeRenderer.shaderprogram.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.AbstractShaderFunction;

/**
 * Abstract shader class for volume accumulators
 * @author michael
 *
 */
public abstract class AbstractVolumeAccumulator extends AbstractShaderFunction  {
	
	private MultiVolumeRenderer parent;
	
	/**
	 * @return the parent
	 */
	public MultiVolumeRenderer getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(MultiVolumeRenderer parent) {
		this.parent = parent;
	}

	/**
	 * constructor
	 * @param name
	 */
	protected AbstractVolumeAccumulator(String name){
		super(name);
	}
	
	/**
	 * disables all needed gl bindings if some were needed by the accumulator
	 * @param gl2
	 */
	public void disposeGL(GL4 gl2) {}
	
	/**
	 * init all needed gl bindings if some were needed by the accumulator
	 * @param gl2
	 */
	public void init(GL4 gl) {}
	
	/**
	 * update all needed gl bindings if some were needed by the accumulator
	 * @param gl2
	 */
	public void updateData(GL4 gl){}
}
