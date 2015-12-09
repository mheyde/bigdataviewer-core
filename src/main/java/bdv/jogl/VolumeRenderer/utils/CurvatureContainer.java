package bdv.jogl.VolumeRenderer.utils;

import java.util.TreeMap;

/**
 * Simple container class for storing the result of a curvature evaluation
 * @author michael
 *
 */
public class CurvatureContainer {
	
	/**
	 * curvature values of each voxel
	 */
	public float[] valueMesh3d; 
	
	/**
	 * histogram of curvature values
	 */
	public final TreeMap<Float,Integer> distribution = new TreeMap<Float, Integer>(); 
	
	/**
	 * minimal curvature value
	 */
	public float minValue = Float.MAX_VALUE;
	
	/**
	 * maximal curvature values
	 */
	public float maxValue = Float.MIN_VALUE;
	
	/**
	 * dimension of the value field
	 */
	public int dimension[]; 
}
