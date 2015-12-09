package bdv.jogl.VolumeRenderer.utils;

/**
 * simple container class for storing evaluated hessian matrices
 * @author michael
 *
 */
public class HessianContainer {
	
	/**
	 * hessian matrices for each voxel
	 */
	public float[][] valueMesh3d;
	
	/**
	 * dimension of the data field
	 */
	public int dimension[]; 
}
