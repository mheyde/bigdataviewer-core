package bdv.jogl.VolumeRenderer.utils;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.copyMatrix;

import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.geom.AABBox;

/**
 * Utils for standard geometry objects
 * @author michael
 *
 */
public class GeometryUtils {

	/**
	 * Returns the Vertices of a unit cube, renderable by GL_Triangles_Strip
	 * (http://doc.qt.io/qt-5/qtopengl-cube-example.html)
	 * @return the triangle strip coordinates of a unit cube in [0,1]
	 */
	public static float[] getUnitCubeVerticesTriangles(){
		float [] array = {
				0,0,0,
				1,0,0,
				0,1,0,
				1,1,0,

				1,0,0,
				1,0,1,
				1,1,0,
				1,1,1,

				1,0,1,
				0,0,1,
				1,1,1,
				0,1,1,

				0,0,1,
				0,0,0,
				0,1,1,
				0,1,0,

				0,0,1,
				1,0,1,
				0,0,0,
				1,0,0,


				0,1,0,
				1,1,0,
				0,1,1,
				1,1,1
		};


		return array;
	}

	/**
	 * Returns the Vertices of a unit cube, renderable by GL_Quads
	 * (http://doc.qt.io/qt-5/qtopengl-cube-example.html)
	 * @return the quads coordinates of a unit cube in [0,1]
	 */
	public static float[] getUnitCubeVerticesQuads(){
		float [] array = {
				0,0,0,
				1,0,0,
				1,1,0,
				0,1,0,

				0,0,0,
				0,1,0,
				0,1,1,
				0,0,1,

				0,0,0,
				1,0,0,
				1,0,1,
				0,0,1,

				1,0,0,
				1,0,1,
				1,1,1,
				1,1,0,

				1,0,1,
				0,0,1,
				0,1,1,
				1,1,1,

				0,1,0,
				1,1,0,
				1,1,1,
				0,1,1,
		};


		return array;
	}
	
	/**
	 * Returns the edges of a unit cube defined in the borders of [0,1]
	 * @return the edge begin end coordinate pairs
	 */
	public static float[] getUnitCubeEdges(){
		float array[] = {
			0,0,0,
			0,0,1,
			
			0,0,0,
			0,1,0,
			
			0,0,0,
			1,0,0,
			
			0,0,1,
			0,1,1,
			
			0,0,1,
			1,0,1,
			
			0,1,0,
			1,1,0,
			
			0,1,0,
			0,1,1,
			
			1,0,0,
			1,1,0,
			
			1,0,0,
			1,0,1,
			
			1,1,0,
			1,1,1,
			
			1,1,1,
			1,0,1,
			
			1,1,1,
			0,1,1,
		};
		return array;
	} 
	
	/**
	 * Returns the axis aligned bounding box of a certain box after transformation
	 * @param formerBoxDim width, height and depth for the former box
	 * @param boxTransformation the transformation to apply
	 * @return the axis aligned bounding box
	 */
	public static AABBox getAABBOfTransformedBox(final long[] formerBoxDim, Matrix4 boxTransformation){
		float[][] minMax = new float[][]{
				{Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE},
				{Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE}};
		float repesentantsToCheck[][] = {
				{0,0,0,1},
				{0,0,1,1},
				{0,1,0,1},
				{0,1,1,1},
				{1,0,0,1},
				{1,0,1,1},
				{1,1,0,1},
				{1,1,1,1},
		};


		Matrix4 transformation = copyMatrix(boxTransformation);
		transformation.scale(formerBoxDim[0],formerBoxDim[1],formerBoxDim[2]);
		for(float [] representant: repesentantsToCheck){
			//transform
			float[] globalVolumeCoordinate= new float[4];
			transformation.multVec(representant, globalVolumeCoordinate);


			//build box
			for(int i = 0; i < 3 ; i++){
				globalVolumeCoordinate[i] = globalVolumeCoordinate[i]/ globalVolumeCoordinate[3];
				minMax[1][i] = Math.max(minMax[1][i], globalVolumeCoordinate[i]);
				minMax[0][i] = Math.min(minMax[0][i], globalVolumeCoordinate[i]);
			} 
		}
		
		return new AABBox(minMax[0],minMax[1]);
	}
}
