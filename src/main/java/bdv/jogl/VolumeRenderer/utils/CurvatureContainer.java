package bdv.jogl.VolumeRenderer.utils;

import java.util.TreeMap;

/**
 * Simple container class for storing the result of a curvature evaluation
 * @author michael
 *
 */
public class CurvatureContainer {
	
	public float[] valueMesh3d; 
	public final TreeMap<Float,Integer> distribution = new TreeMap<Float, Integer>(); 
	public float minValue = Float.MAX_VALUE;
	public float maxValue = Float.MIN_VALUE;
	public int dimension[]; 
}
