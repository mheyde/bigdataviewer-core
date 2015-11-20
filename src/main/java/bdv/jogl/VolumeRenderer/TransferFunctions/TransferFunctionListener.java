package bdv.jogl.VolumeRenderer.TransferFunctions;

import java.util.EventListener;

/**
 * listen if a certain transfer function was altered.
 * @author michael
 *
 */
public interface TransferFunctionListener extends EventListener {

	/**
	 * Triggered by function point changes. 
	 */
	public void functionPointChanged(final TransferFunction1D transferFunction );
	

	/**
	 * called if the sampler (pre integration or normal sampler) was altered. 
	 * The shader then needs to recompile.
	 */
	public void samplerChanged(final TransferFunction1D transferFunction1D);
}
