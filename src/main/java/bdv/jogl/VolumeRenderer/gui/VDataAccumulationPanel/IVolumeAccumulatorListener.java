package bdv.jogl.VolumeRenderer.gui.VDataAccumulationPanel;

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.AbstractVolumeAccumulator;

/**
 * Listen on aggregation events
 * @author michael
 *
 */
public interface IVolumeAccumulatorListener {

	/**
	 * Is called if the accumulation changes
	 * @param acc
	 */
	public void aggregationChanged(AbstractVolumeAccumulator acc);
}
