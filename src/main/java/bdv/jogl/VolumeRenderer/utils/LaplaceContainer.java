package bdv.jogl.VolumeRenderer.utils;

/**
 * container for laplace evaluation
 * @author michael
 *
 */
public class LaplaceContainer{
	
	/**
	 * laplace operator values for each voxel
	 */
	public float[] valueMesh3d;
	
	/**
	 * minimal laplace operator value 
	 */
	public float minValue = Float.MAX_VALUE;
	
	/**
	 * maximal laplace operator value 
	 */
	public float maxValue = Float.MIN_VALUE;
	
	/**
	 * dimension of the value field
	 */
	public int dimension[]; 
}
