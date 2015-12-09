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
	
	/**
	 * voxel data
	 */
	public float[] data;
	
	/**
	 * memory offset in full volume for spartial textures
	 */
	public long[] memOffset={0,0,0};
	
	/**
	 * number of elements in each dimension
	 */
	public long[] memSize={0,0,0};
	
	/**
	 * dimension of the full volume voxel field
	 */
	public long[] dimensions = {0,0,0};
	
	/**
	 * maximum volume value
	 */
	public float maxValue;
	
	/**
	 * minimum volume value
	 */
	public float minValue;
	
	private Matrix4 localTransformation = getNewIdentityMatrix();
	
	/**
	 * the histogram of volume values
	 */
	public final TreeMap<Float, Integer> valueDistribution = new TreeMap<Float, Integer>();
	
	/**
	 * maximum occurance of a volume value
	 */
	public int maxOccurance = 0;
	
	private boolean needsUpdate = true;
	
	/**
	 * the volume data stack name 
	 */
	public String name;

	/**
	 * 	@return  the volume value distribution of the whole stack
	 */
	public TreeMap<Float, Integer> getValueDistribution() {
		return valueDistribution;
	}

	/**
	 * @return true if some data in the stack has changed
	 */
	public boolean needsUpdate(){
		return needsUpdate;
	}

	/**
	 * Set the update flag
	 * @param tag the update flag
	 */
	public void setNeedsUpdate(boolean tag){
		needsUpdate = tag;
	}

	/**
	 * @return the count of the most occuring volume value. It is not the value itself.  
	 */
	public int getMaxOccurance(){
		return maxOccurance;
	}

	/**
	 * @return the transformation from the local volume coordinate system to the global one.
	 */
	public Matrix4 getLocalTransformation() {
		return localTransformation;
	}

	/**
	 * Sets the transformation from the local volume coordinate system to the global one.
	 * @param localTransformation the transformation to global space
	 */
	public void setLocalTransformation(Matrix4 localTransformation) {
		this.localTransformation = localTransformation;
	}
}