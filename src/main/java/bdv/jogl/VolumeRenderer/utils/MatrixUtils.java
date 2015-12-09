package bdv.jogl.VolumeRenderer.utils;

import static bdv.jogl.VolumeRenderer.utils.GeometryUtils.getAABBOfTransformedBox;

import java.util.Collection;

import net.imglib2.realtransform.AffineTransform3D;

import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.math.geom.AABBox;

/**
 * Class to provide matrix operations for affine and matrix4
 * @author michael
 *
 */
public class MatrixUtils {

	/**
	 * Returns a new matrix instance initialized with the identity
	 * @return the identity matrix
	 */
	public static Matrix4 getNewIdentityMatrix(){
		Matrix4 matrix = new Matrix4();
		matrix.loadIdentity();
		return matrix;
	}
	
	/**
	 * converts a AffineTransform3D matrix to a matrix 4 in opengl format
	 * @param viewerTransform the matrix to convert
	 * @return the converted matrix
	 */
	public static Matrix4 convertToJoglTransform(final AffineTransform3D viewerTransform) {
		Matrix4 matrix = getNewIdentityMatrix();

		double [] viewerArray = new double[4*3];
		viewerTransform.toArray(viewerArray);

		for(int i = 0; i < viewerArray.length; i++){
			Double data = viewerArray[i];
			matrix.getMatrix()[i] = data.floatValue();
		}
		//matrix.invert();
		matrix.transpose();

		return matrix;
	}
	
	/**
	 * Create a new instance as copy of a given matrix
	 * @param matrixToCopy the matrix to copy
	 * @return the copied matrix
	 */
	public static Matrix4 copyMatrix(final Matrix4 matrixToCopy){
		Matrix4 matrix = getNewIdentityMatrix();
		matrix.multMatrix(matrixToCopy);
		return matrix;
	}
	
	/**
	 * Get the normalized eye position in the model view space.
	 * @param modelViewMatrix model view matrix defining the current space in gl format (transposed)
	 * @return the eye position in the given model view space
	 */
	public static float[] getEyeInCurrentSpace(final Matrix4 modelViewMatrix){
		float eyePositionInCurrentSpace[] = new float[3];
		Matrix4 modelViewMatrixInverse = copyMatrix(modelViewMatrix);
		modelViewMatrixInverse.invert();
	
		//translation part of model view
		for(int i= 0; i < eyePositionInCurrentSpace.length; i++){
			eyePositionInCurrentSpace[i] = modelViewMatrixInverse.getMatrix()[12 + i];
		}
		return eyePositionInCurrentSpace;

	}
	
	/**
	 * Calculates the transformation from 0 -1 Space to global space AABBox 
	 * @param box The box to calculates the transformation from
	 * @return The transformation
	 */
	public static Matrix4 getTransformationRepresentAABBox(final AABBox box){
		Matrix4 transformation = getNewIdentityMatrix();
		transformation.translate(box.getMinX(),box.getMinY(),box.getMinZ());
		transformation.scale(box.getWidth(),box.getHeight(),box.getDepth());
		return transformation;
	}
	
	/**
	 * 
	 * @return a Matrix filled with zeros
	 */
	public static Matrix4 getNewNullMatrix(){
		Matrix4 nullMatrix = getNewIdentityMatrix();
		for(int i =0; i < 4; i++){
			nullMatrix.getMatrix()[i*4+i]=0;
		}
		return nullMatrix;
	}
	
	/**
	 * adds two matrices and returns a new matrix object containing the result
	 * @param a matrix a
	 * @param b matrix b
	 * @return matrix a + b
	 */
	public static Matrix4 addMatrix(Matrix4 a, Matrix4 b){
		Matrix4 ret = new Matrix4();
		for(int i =0; i < 16; i++){
			ret.getMatrix()[i] = a.getMatrix()[i] + b.getMatrix()[i];
		}
		return ret;
	}
	
	/**
	 * subs two matrices and returns a new matrix object containing the result
	 * @param a matrix a 
	 * @param b matrix b
	 * @return matrix a - b
	 */
	public static Matrix4 subMatrix(Matrix4 a, Matrix4 b){
		Matrix4 binv = new Matrix4();
		for(int i = 0; i < 16; i++ ){
			binv.getMatrix()[i] = -b.getMatrix()[i];
		}
		
		return addMatrix(a, binv);
	}
	
	/**
	 * Creates a matrix instance from given data
	 * @param data 16 entry field symbolizing a 4 x 4 matrix as vector of rows
	 * @return the matrix of the data
	 */
	public static Matrix4 createMatrix4X4(float data[]){
		Matrix4 m = new Matrix4();
		for(int i =0; i < 16; i++){
			m.getMatrix()[i] = data[i];
		}
		return m;
	}
	
	/**
	 * scales the given matrix by n and returns a new matrix of its content
	 * @param m the matrix to scale
	 * @param n the scaling factor
	 * @return the new scaled matrix instance
	 */
	public static Matrix4 scale(Matrix4 m,  float n){
		Matrix4 k = copyMatrix(m);
		for(int i=0; i < 16; i++){
			k.getMatrix()[i]*=n;
		}
		return k;
	}
	
	/**
	 * Does matmul without altering a and b
	 * @param a the matrix a
	 * @param b the matrix b
	 * @return the new matrix instance a * b
	 */
	public static Matrix4 matMul(Matrix4 a, Matrix4 b){
		Matrix4 c = copyMatrix(a);
		c.multMatrix(b);
		return c;
	}
	
	/**
	 * Calculates the trace of a given Matrix 
	 * @param m the matrix to use
	 * @return the trace of m
	 */
	public static float trace(Matrix4 m){
		float t = 0;
		for(int i =0; i < 4; i++){
			t += m.getMatrix()[i*4+i];
		}
		return t;
	}
	
	/**
	 * Calculates a closely fitting bounding box of transformations 
	 * @param transformations The list of transformations of 0-1 space coordinates
	 * @return The bounding box
	 */
	public static AABBox calculateCloseFittingBox(final Collection<Matrix4> transformations){

		float lowhighPoint[][] = {
				{Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE},
				{Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE}
		};

		//iterate transformation for get bounding box
		for(Matrix4 transformation: transformations){
			long [] foo= new long[]{1,1,1};

			AABBox box = getAABBOfTransformedBox(foo, transformation);

			for(int d =0; d < 3; d++){
				lowhighPoint[0][d] = Math.min(lowhighPoint[0][d], box.getLow()[d]);
				lowhighPoint[1][d] = Math.max(lowhighPoint[1][d], box.getHigh()[d]); 
			}
		}

		AABBox boundingBox = new AABBox(lowhighPoint[0],lowhighPoint[1]);
		return boundingBox;
	}
	
	/**
	 * transforms a given plane as hessian normal form using a matrix transformation
	 * @param transformation the transformation to apply
	 * @param plane the plane in hessian normal form
	 * @return the plane in the transformed space
	 */
    public static float[] transformPlane(Matrix4 transformation, final float[] plane){
    	Matrix4 mat = copyMatrix(transformation); 
		mat.invert();
		mat.transpose();

		float[]transformedNormal ={0,0,0,0};
		mat.multVec(plane, transformedNormal);

		float n =VectorUtil.normVec3(transformedNormal);

		float newPlane[] = new float[4];
		//prepare return
		for(int i =0; i < 4; i++){
			newPlane[i]= transformedNormal[i]/n;
		}
		return newPlane;
    }
}
