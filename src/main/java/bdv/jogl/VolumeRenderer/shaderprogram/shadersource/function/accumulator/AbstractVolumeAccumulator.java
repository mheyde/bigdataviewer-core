package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator;

import com.jogamp.opengl.GL4;

import bdv.jogl.VolumeRenderer.shaderprogram.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.AbstractShaderFunction;

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

	protected AbstractVolumeAccumulator(String name){
		super(name);
	}
	
	public void disposeGL(GL4 gl2) {}
	
	public void init(GL4 gl) {}
	
	public void updateData(GL4 gl){}
	
	
	
}
