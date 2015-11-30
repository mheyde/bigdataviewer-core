package bdv.jogl.VolumeRenderer.utils;

import java.util.TreeMap;

import com.jogamp.opengl.math.Matrix4;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;

/**
 * Basic storage data class for storing one stack of 2D volume data images
 * @author michael
 *
 */
public class VolumeDataBlock{
	public float[] data;
	public long[] memOffset={0,0,0};
	public long[] memSize={0,0,0};
	public long[] dimensions = {0,0,0};
	public float maxValue;
	public float minValue;
	private Matrix4 localTransformation = getNewIdentityMatrix();
	public final TreeMap<Float, Integer> valueDistribution = new TreeMap<Float, Integer>();
	public int maxOccurance = 0;
	private boolean needsUpdate = true;
	public String name;
	
	/**
	 * Returns the volume value distribution of the whole stack
	 * @return
	 */
	public TreeMap<Float, Integer> getValueDistribution() {
		return valueDistribution;
	}

	/**
	 * Retuns true if some data in the stack has changed
	 * @return
	 */
	public boolean needsUpdate(){
		return needsUpdate;
	}
	
	/**
	 * Set the update flag
	 * @param tag
	 */
	public void setNeedsUpdate(boolean tag){
		needsUpdate = tag;
	}
	
	/**
	 * returns the count of the most occuring volume value. It is not the value itself.  
	 * @return
	 */
	public int getMaxOccurance(){
		return maxOccurance;
	}

	/**
	 * Return the transformation from the local volume coordinate system to the global one.
	 * @return
	 */
	public Matrix4 getLocalTransformation() {
		return localTransformation;
	}

	/**
	 * Sets the transformation from the local volume coordinate system to the global one.
	 */
	public void setLocalTransformation(Matrix4 localTransformation) {
		this.localTransformation = localTransformation;
	}
}