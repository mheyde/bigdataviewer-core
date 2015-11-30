package bdv.jogl.VolumeRenderer.utils;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;




import bdv.BigDataViewer;
import bdv.jogl.VolumeRenderer.Scene.Texture;
import bdv.viewer.state.SourceState;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.math.geom.AABBox;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;


/**
 * Delivers standard volume data interaction methods
 * @author michael
 *
 */
public class VolumeDataUtils {

	/**
	 * global stack id color map 
	 */
	private static Color volumeColor[] = new Color[]{

		Color.RED,
		Color.CYAN,
		Color.DARK_GRAY,
		Color.BLUE,
		Color.GREEN,
		Color.BLACK,
		Color.YELLOW,
		Color.CYAN,
	};


	/**
	 * Gets the volume data from a RandomAccessibleInterval
	 * @param source data to copy from
	 * @return An array of volume data with the dimensions x,y,z 
	 */
	public static VolumeDataBlock getDataBlock(final BigDataViewer bdv,final AABBox minBoundingBox, int sourceId, int midmap){
		SourceState<?> source = bdv.getViewer().getState().getSources().get(sourceId);
		int currentTimePoint = bdv.getViewer().getState().getCurrentTimepoint();
		VolumeDataBlock data = new VolumeDataBlock();
		TreeMap<Float, Integer> distr = data.getValueDistribution();
		RandomAccessibleInterval<?> dataField = source.getSpimSource().getSource(currentTimePoint, midmap);
		IterableInterval<?> tmp = Views.flatIterable(dataField);
		tmp.dimensions( data.dimensions);

		long minMax[][] = new long[][]{{(long)Math.max(Math.floor(minBoundingBox.getMinX()),0),(long)Math.max(Math.floor(minBoundingBox.getMinY()),0),(long)Math.max(Math.floor(minBoundingBox.getMinZ()),0)},
				{Math.min((long)Math.ceil(minBoundingBox.getMaxX()), data.dimensions[0])-1,Math.min((long)Math.ceil(minBoundingBox.getMaxY()), data.dimensions[1])-1,Math.min((long)Math.ceil(minBoundingBox.getMaxZ()), data.dimensions[2])-1}};
		float[] block = new float[Math.max(0,(int)((minMax[1][0]+1-minMax[0][0]) * (minMax[1][1]+1-minMax[0][1]) * (minMax[1][2]+1-minMax[0][2])))];

		tmp = Views.flatIterable(Views.interval(dataField, minMax[0], minMax[1]));

		String name = source.getSpimSource().getName();

		int maxOcc =0;
		// copy values 
		int i = 0;
		float minValue = Float.MAX_VALUE;
		float maxValue = Float.MIN_VALUE; 

		@SuppressWarnings("unchecked")
		Iterator<UnsignedShortType> values = (Iterator<UnsignedShortType>) tmp.iterator();
		for(long z = minMax[0][2]; z <=  minMax[1][2]; z++){
			for(long y = minMax[0][1]; y <= minMax[1][1]; y++){
				for(long x = minMax[0][0]; x <= minMax[1][0]; x++ ){
					short value = (short)values.next().get();

					block[i++] = value ;

					//update local distribution
					if(!distr.containsKey((float)value)){
						distr.put((float)value,0);
					}
					Integer curOcc= distr.get((float) value);
					curOcc++;
					distr.put((float)value,curOcc);
					maxOcc = Math.max(curOcc, maxOcc);
					minValue = Math.min(minValue, value);
					maxValue = Math.max(maxValue, value);
				}
			}
		}

		tmp.min(data.memOffset);
		for(int d = 0;d < 3 ;d++){
			data.memSize[d] = Math.max(minMax[1][d]+1-minMax[0][d],0);
		}
		data.maxValue = maxValue;
		data.minValue = minValue;
		data.data = block;
		data.maxOccurance = maxOcc;
		data.name = name;
		AffineTransform3D sourceTransform3D = new AffineTransform3D();
		source.getSpimSource().getSourceTransform(currentTimePoint, midmap, sourceTransform3D);
		Matrix4 sourceTransformation = convertToJoglTransform(sourceTransform3D);
		data.setLocalTransformation(sourceTransformation);
		data.setNeedsUpdate(true);
		return data;
	}


	/**
	 * prints the voxel data as a paraview file
	 * @param data volume data array to write
	 * @param fileName file to write
	 */
	public static void writeParaviewFile(final VolumeDataBlock vData, final String fileName){
		PrintWriter paraWriter = null;
		List<List<Long>> cellDefines = new LinkedList<List<Long>>();
		List<Float> values = new ArrayList<Float>();
		try {
			paraWriter = new PrintWriter(fileName+".vtu","UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//vti header
		paraWriter.println("# vtk DataFile Version 3.1");
		paraWriter.println("A converted sacked view");
		paraWriter.println("ASCII");
		paraWriter.println("DATASET UNSTRUCTURED_GRID");


		//write point data
		final int offsetNextNode = 1;
		final long offsetNextLine = vData.dimensions[0];
		final long offsetNextSlice = vData.dimensions[0]*vData.dimensions[1];
		int i =0;
		paraWriter.println("POINTS "+ vData.dimensions[0]*vData.dimensions[1]*vData.dimensions[2]+ " FLOAT" );
		for(int z = 0; z< vData.dimensions[2];z++){
			for(int y = 0; y < vData.dimensions[1]; y++){
				for (int x = 0; x < vData.dimensions[0]; x++){

					//not last element in dimension
					if(x < vData.dimensions[0]-1&&y < vData.dimensions[1]-1&&z < vData.dimensions[2]-1){
						Long currenVoxelID = (long)values.size();


						//add cell ids
						List<Long> cell = new ArrayList<Long>(8);
						cell.add(currenVoxelID);
						cell.add(currenVoxelID+offsetNextNode);
						cell.add(currenVoxelID+offsetNextLine);
						cell.add(currenVoxelID+offsetNextLine+offsetNextNode);
						cell.add(offsetNextSlice+currenVoxelID);
						cell.add(offsetNextSlice+currenVoxelID+offsetNextNode);
						cell.add(offsetNextSlice+currenVoxelID+offsetNextLine);
						cell.add(offsetNextSlice+currenVoxelID+offsetNextLine+offsetNextNode);

						cellDefines.add(cell);
					}

					String line = "" +x + " "+y+" "+z;
					values.add(vData.data[i++]);
					paraWriter.println(line);

				}
			}
		}
		paraWriter.println();
		//end points

		//write cells vti
		paraWriter.println("CELLS "+cellDefines.size()+" "+cellDefines.size() * 9);
		for(List<Long> cellDefine : cellDefines ){
			paraWriter.print(cellDefine.size());
			for(Long number: cellDefine){
				paraWriter.print(" "+number);
			}
			paraWriter.println();
		}
		paraWriter.println();

		//cell Type
		paraWriter.println("CELL_TYPES "+ cellDefines.size());
		for(i = 0; i < cellDefines.size(); i++ ){
			paraWriter.println(11);
		}
		paraWriter.println();
		//end cells

		//point data
		paraWriter.println("POINT_DATA "+ values.size() );
		paraWriter.println("SCALARS intensity FLOAT 1" );
		paraWriter.println("LOOKUP_TABLE default");
		for(Float s : values){
			paraWriter.println(s.toString());
		}
		//end point data

		paraWriter.close();
	}

	/**
	 * Returns the color for a given volume stack id
	 * @param i
	 * @return
	 */
	public static Color getColorOfVolume(int i){
		return volumeColor[i%volumeColor.length];
	}

	/**
	 * Returns the transformation matrix for a volume stack to the global coordinate system
	 * @param block
	 * @return
	 */
	public static Matrix4 fromVolumeToGlobalSpace(final VolumeDataBlock block){
		return copyMatrix( block.getLocalTransformation());
	}

	/**
	 * Returns the transformation matrix for a volume stack to the global coordinate system scaled in each dimension with the voxel count.
	 * needed to expand the [0,1] defined texture coordinate system and get the real dimension in global space
	 * @param block
	 * @return
	 */
	public static Matrix4 calcScaledVolumeTransformation(final VolumeDataBlock block){
		Matrix4 trans = getNewIdentityMatrix();

		trans.multMatrix(block.getLocalTransformation());
		trans.scale(block.dimensions[0], block.dimensions[1], block.dimensions[2]);

		return trans;
	}

	/**
	 * Return the transformation from global to local volume coordinates of a certain stack.
	 * @param block
	 * @return
	 */
	public static Matrix4 fromCubeToVolumeSpace(final VolumeDataBlock block){

		Matrix4 tmp = copyMatrix(block.getLocalTransformation());
		//tmp.scale(block.dimensions[0], block.dimensions[1], block.dimensions[2]);
		tmp.invert();
		//long[] dim = block.dimensions;

		//	trans.scale((float)(dim[0]-1)/((float)dim[0]), (float)(dim[1]-1)/((float)dim[1]), (float)(dim[2]-1)/((float)dim[2]));
		//	trans.translate(1.f/(2.f*(float)dim[0]), 1.f/(2.f*(float)dim[1]), 1.f/(2.f*(float)dim[2]));
		return tmp;
		//return trans;
	}


	/**
	 * convolves the data in x and y dimension since these dimension have the highest resolution
	 * @param data
	 * @return
	 */
	public static LaplaceContainer calulateLablacianSimple(final VolumeDataBlock data){
		//assumeed kernel:
		//-1 -1 -1
		//-1  8 -1
		//-1 -1 -1
		/*float log2dKernel[][] = new float[][]{{-1,-1,-1},
											  {-1,8,-1},
											  {-1,-1,-1}};*/
		int xyz[] = new int[]{(int)data.memSize[0],(int)data.memSize[1],(int)data.memSize[2]};
		float convolved[] = new float[(int)(xyz[2]*xyz[1]*xyz[0])];

		LaplaceContainer ret = new LaplaceContainer();
		ret.dimension = xyz.clone();

		//folding
		for(int z = 0; z < xyz[2]; z++){
			int zOffset = z*xyz[0]*xyz[1];
			for(int y = 0; y < xyz[1]; y++){
				float kernelValue = 0f;
				int yOffset = y*xyz[0];
				int vertOffset = xyz[0];


				for(int x=0; x < xyz[0]; x++){
					//simple kernel evaluation
					kernelValue =0;
					for(int y1 = -1; y1 < 2; y1++){						
						for(int x1 = -1; x1 < 2; x1++){
							float dataValue;
							//repeat at border
							if((x == 0 && x1 ==-1) || (x == xyz[0]-1 && x1 ==1)||
									(y == 0 && y1 ==-1)|| (y == xyz[1]-1 && y1 ==1)){
								dataValue = data.data[zOffset + yOffset +x ];
							}else{
								dataValue = data.data[zOffset + yOffset  + vertOffset*y1 +x +x1];
							}
							float koeffizient;

							if(y1 == 0 && x1 == 0){
								koeffizient = -8;
							}else{
								koeffizient = 1;
							}
							kernelValue+= koeffizient * dataValue;
						}
					}
					ret.maxValue = Math.max(ret.maxValue, kernelValue);
					ret.minValue = Math.min(ret.minValue, kernelValue);
					convolved[zOffset+yOffset+x] = kernelValue;
				}
			}
		}
		ret.valueMesh3d = convolved;
		return ret;
	}

	/**
	 * Creates a default volume texture (interpolation, clamping , etc,)
	 * @param gl The gl context.
	 * @param programLocation The location of the uniform sampler.
	 * @return The created texture object
	 */
	public static Texture createVolumeTexture(final GL4 gl, int programLocation){
		Texture volumeTexture = new Texture(GL4.GL_TEXTURE_3D,programLocation,GL4.GL_R32F,GL4.GL_RED,GL4.GL_FLOAT);
		volumeTexture.genTexture(gl);
		volumeTexture.setTexParameteri(GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		volumeTexture.setTexParameteri(GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
		volumeTexture.setTexParameterfv(GL4.GL_TEXTURE_BORDER_COLOR, new float[]{-1,-1,-1,-1});
		volumeTexture.setTexParameteri(GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_BORDER);
		volumeTexture.setTexParameteri(GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_BORDER);
		volumeTexture.setTexParameteri(GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_BORDER);
		return volumeTexture;
	}


	/**
	 * Calculates eye and center positions of a camera defined by a hull volume and a view direction
	 * @param hull the hull volume
	 * @param viewDirection at least 3D vector where the first 3 entries are xyz of the camera direction
	 * @return float[2][3] defining eye and center positions
	 */
	public static float[][] calcEyeAndCenterByGivenHull(final AABBox hull, final float viewDirection[]){
		float[] center = hull.getCenter();
		float distanceFromCenter =  2f * (Math.max(Math.max(hull.getWidth(),hull.getHeight()),hull.getDepth()));
		float normal[] = new float[3];
		float step [] = new float[3];
		float[] eye = {0,0,0};


		VectorUtil.normalizeVec3(normal, viewDirection.clone());
		VectorUtil.scaleVec3(step, normal.clone(), distanceFromCenter);
		VectorUtil.subVec3(eye, center.clone(), step);

		return new float[][]{eye.clone(),center.clone()}; 
	}

	/**
	 * Calculates the total curvature values for each point of a volume stack.
	 * @param data
	 * @return
	 */
	public static CurvatureContainer calculateCurvatureOfVolume(final VolumeDataBlock data){
		CurvatureContainer cc = new CurvatureContainer();
		GradientContainer cg= calculateGradientOfVolume(data);
		HessianContainer ch = calculateHessianOfGradients(cg, data);

		int xyz[] = new int[]{(int)data.memSize[0],(int)data.memSize[1],(int)data.memSize[2]};
		float convolved[] = new float[(int)(xyz[2]*xyz[1]*xyz[0])];

		cc.dimension = xyz.clone();
		//folding
		for(int z = 0; z < xyz[2]; z++){
			int zOffset = z*xyz[0]*xyz[1];
			for(int y = 0; y < xyz[1]; y++){
				int yOffset = y*xyz[0];



				for(int x=0; x < xyz[0]; x++){
					float totalCurvature = 0;
					float g[] = cg.valueMesh3d[zOffset+yOffset+x].clone();
					float h[] = ch.valueMesh3d[zOffset+yOffset+x].clone();
					float l = VectorUtil.normVec3(g);

					if(l>0.00001){
						//get the normal to column matrix
						Matrix4 n = getNewNullMatrix();
						for(int d = 0; d < 3; d++){
							n.getMatrix()[d*4]=-1f*g[d]/l;
						}
						//create nt
						Matrix4 nt = copyMatrix(n);
						nt.transpose();

						Matrix4 H = createMatrix4X4(new float[]{
								h[0],h[1],h[2],0,
								h[3],h[4],h[5],0,
								h[6],h[7],h[8],0,
								0,0,0,0
						});

						//mat3x3 p = getIdentity() - n * transpose(n);
						Matrix4 P = subMatrix(getNewIdentityMatrix(), matMul(n, nt));

						//mat3x3 g = -p*h*p/length(gradient);
						Matrix4 G =scale(matMul(scale(P, -1), matMul(H,P)),1f/l);
						Matrix4 Gt = copyMatrix(G);
						Gt.transpose();

						totalCurvature = (float)Math.sqrt( trace(matMul( G,Gt)));
					}else{
						totalCurvature = 0;
					}
					//distribution
					int count = 0;
					if(cc.distribution.containsKey(totalCurvature)){
						cc.distribution.get(totalCurvature);
					}
					count ++;
					cc.distribution.put(totalCurvature, count);

					cc.maxValue = Math.max(cc.maxValue, totalCurvature);
					cc.minValue = Math.min(cc.minValue, totalCurvature);
					convolved[zOffset+yOffset+x] = totalCurvature;
				}
			}
		}
		cc.valueMesh3d = convolved;
		/*"	mat3x3 n = mat3x3(-1.0* gradient/length(gradient),vec3(0.0),vec3(0.0));",
		"	mat3x3 p = getIdentity() - n * transpose(n);",
		"	mat3x3 g = -p*h*p/length(gradient);",
		"	float t = trace(g);",
		"	float f = frobeniusNorm(g);",
		"	float s = sqrt(2.0* f*f - t*t);",
		"	float k[2];",
		"	k[0] = (t+s)/2.0;",
		"	k[1] = (t-s)/2.0;",*/
		return cc;
	}

	/**
	 * Calculates the hessian matrice for each point of a volume stack.
	 * @param cg
	 * @param data
	 * @return
	 */
	private static HessianContainer calculateHessianOfGradients(
			GradientContainer cg, VolumeDataBlock data) {
		//get voxel distances per dim
		float voxelDist[] = calculateVoxelDistance(data);		
		HessianContainer hc = new HessianContainer();

		int xyz[] = new int[]{(int)data.memSize[0],(int)data.memSize[1],(int)data.memSize[2]};
		hc.valueMesh3d = new float[(int)(xyz[2]*xyz[1]*xyz[0])][9];
		hc.dimension = xyz.clone();
		for(int z = 0; z < xyz[2]; z++){
			int zOffset = z*xyz[0]*xyz[1];
			for(int y = 0; y < xyz[1]; y++){
				int yOffset = y*xyz[0];
				for(int x = 0; x < xyz[0]; x++ ){
					int cPos[] = {x,y,z};
					int index = zOffset+yOffset+x;
					int offsets[] = {1,xyz[0],xyz[0]*xyz[1]};
					//central diff optimizable through symetry
					for(int i = 0; i < 3; i++ ){
						for(int j = i; j< 3; j++){

							float[] gpdj = (cPos[j] < xyz[j]-1)?cg.valueMesh3d[index + offsets[j]]:cg.valueMesh3d[index];
							float[] gmdj = (cPos[j] > 0)?cg.valueMesh3d[index - offsets[j]]:cg.valueMesh3d[index];
							float[] gpdi = (cPos[i] < xyz[i]-1)?cg.valueMesh3d[index + offsets[i]]:cg.valueMesh3d[index];
							float[] gmdi = (cPos[i] > 0)?cg.valueMesh3d[index - offsets[i]]:cg.valueMesh3d[index];

							float partialDerivative=
									(gpdj[i] - gmdj[i])/4f*voxelDist[j]+
									(gpdi[j] - gmdi[j])/4f*voxelDist[i];

							hc.valueMesh3d[index][i*3+j] = partialDerivative;
							//symetry
							hc.valueMesh3d[index][j*3+i] = partialDerivative;

						}
					}
				}
			}
		}

		return hc;
	}

	/**
	 * Calculates for each data point a gradient vector
	 * @param data
	 * @return
	 */
	public static GradientContainer calculateGradientOfVolume(
			VolumeDataBlock data) {
		//get voxel distances per dim
		float voxelDist[] = calculateVoxelDistance(data);

		GradientContainer gc = new GradientContainer();

		int xyz[] = new int[]{(int)data.memSize[0],(int)data.memSize[1],(int)data.memSize[2]};
		gc.valueMesh3d = new float[(int)(xyz[2]*xyz[1]*xyz[0])][3];

		gc.dimension = xyz.clone();
		//folding
		for(int z = 0; z < xyz[2]; z++){
			int zOffset = z*xyz[0]*xyz[1];
			for(int y = 0; y < xyz[1]; y++){
				int yOffset = y*xyz[0];
				for(int x = 0; x < xyz[0]; x++ ){
					int cPos[] = {x,y,z};
					int index = zOffset+yOffset+x;
					int offsets[] = {1,xyz[0],xyz[0]*xyz[1]};
					//central diff
					for(int d = 0; d < 3; d++ ){

						float higherPosValue = (cPos[d] < xyz[d]-1)?data.data[index + offsets[d]]:data.data[index];
						float lowerPosValue = (cPos[d] > 0)?data.data[index - offsets[d]]:data.data[index];
						gc.valueMesh3d[index][d] = (higherPosValue - lowerPosValue)/(2.f*voxelDist[d]);
					}

				}
			}
		}
		return gc;
	}

	/**
	 * Interpolates linear between 2 points 
	 * @param v1 startvalue
	 * @param v2 endvalue
	 * @param dist distance between start and end
	 * @param minStep distance from start to interpolate
	 * @return 
	 */
	private static float interpolate(float v1, float v2, float dist, float minStep) {
		float m = (v2 -v1) /dist;
		return v1+ m*minStep;
	}

	/**
	 * calculates the Voxel space dimension of a volume stack.
	 * @param data
	 * @return
	 */
	private static float[] getScaledVolumeDimensions(VolumeDataBlock data){
		float transferPoints[][] = {{0,0,0,1},{1,0,0,1},{0,1,0,1},{0,0,1,1}};
		float transferedPoints[][] = new float[4][4];
		Matrix4 trans = calcScaledVolumeTransformation(data);

		//get the transfered border points
		for(int i = 0; i < transferPoints.length; i++){
			trans.multVec(transferPoints[i], transferedPoints[i]);

			//wclip
			for(int d=0; d < 3; d++){
				transferedPoints[i][d] /= transferedPoints[i][3];
			}
		}

		return new float[]{
				VectorUtil.distVec3(transferedPoints[0], transferedPoints[1]),
				VectorUtil.distVec3(transferedPoints[0], transferedPoints[2]),
				VectorUtil.distVec3(transferedPoints[0], transferedPoints[3])
		};
	}

	/**
	 * Calculates the distance between voxels for each dimension.
	 * @param data
	 * @return
	 */
	private static float[] calculateVoxelDistance(VolumeDataBlock data) {
		// TODO Auto-generated method stub
		// TODO correct dim
		float distances[] = new float[]{1,1,1};
		float realDimensions[] = getScaledVolumeDimensions(data);

		for(int d = 0; d < 3; d++){
			distances[d] = realDimensions[d]/(float) data.dimensions[d];
		}

		return distances;
	}
}

