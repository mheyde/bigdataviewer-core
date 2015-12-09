package bdv.jogl.VolumeRenderer;

import java.util.List;

import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;

import com.jogamp.opengl.math.geom.AABBox;

/**
 * Trigger to be handled in the VR extension
 * @author michael
 *
 */
public interface IBigDataViewerDataSelectorListener {

	/**
	 * triggered if the queried data is available
	 * @param hullVolume the global region which was queried
	 * @param partialVolumesInHullVolume the list of volume data stacks in the hull volume
	 * @param time the queried time stamp
	 */
	public void selectedDataAvailable(AABBox hullVolume, List<VolumeDataBlock> partialVolumesInHullVolume, int time );
	
	/**
	 * triggered if a query of data is started
	 * @param hullVolume the global region to query in
	 */
	public void dataRegionSelected(AABBox hullVolume);
}
