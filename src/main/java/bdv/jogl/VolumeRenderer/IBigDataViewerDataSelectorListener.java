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

	public void selectedDataAvailable(AABBox hullVolume, List<VolumeDataBlock> partialVolumesInHullVolume, int time );
	
	public void dataRegionSelected(AABBox hullVolume);
}
