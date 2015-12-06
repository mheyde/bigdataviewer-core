package bdv.jogl.VolumeRenderer.shaderprogram;

import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.getNewIdentityMatrix;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.*;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import bdv.jogl.VolumeRenderer.GLErrorHandler;
import bdv.jogl.VolumeRenderer.scene.Texture;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.ISourceListener;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.AbstractVolumeAccumulator;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.MaximumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.transferfunction.TransferFunction1D;
import bdv.jogl.VolumeRenderer.transferfunction.TransferFunctionAdapter;
import bdv.jogl.VolumeRenderer.transferfunction.sampler.ITransferFunctionSampler;
import bdv.jogl.VolumeRenderer.utils.GeometryUtils;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManagerAdapter;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import static bdv.jogl.VolumeRenderer.utils.GLUtils.*;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.math.geom.AABBox;

import static bdv.jogl.VolumeRenderer.utils.WindowUtils.getNormalizedColor;

/**
 * Renderer for multiple volume data
 * @author michael
 *
 */
public class MultiVolumeRenderer extends AbstractShaderSceneElement{

	private float[] coordinates = GeometryUtils.getUnitCubeVerticesTriangles(); 

	private VolumeDataManager dataManager;

	private float isoSurfaceValue = 0;

	private boolean isIsoSurfaceValueUpdatable= true;

	private final Map<Integer,Texture> volumeTextureMap = new HashMap<Integer, Texture>();

	private TransferFunction1D tf;

	private boolean isColorUpdateable;

	private boolean isEyeUpdateable = true;

	private Color backgroundColor = Color.BLACK;

	private boolean isBackgroundColorUpdateable = true;

	private Matrix4 drawCubeTransformation = null;

	private MultiVolumeRendererShaderSource sources =new MultiVolumeRendererShaderSource ();

	private boolean isSliceUpdateable;

	private boolean showSlice = false;

	private boolean isShownUpdatable = true;

	private int samples;

	private boolean isSamplesUpdatable;

	private float length; 

	private boolean isUseGradientUpdateable =true;

	private boolean useGradient= false;

	private AABBox drawRect;

	private AbstractVolumeAccumulator accumulator;

	private boolean isClippingUpdatable = true;

	private boolean islengthUpdatable = true;

	private boolean isOpacity3DUpdateable = true;

	private boolean isHullVolumeUpdateable = true;

	private boolean isVertBufferUpdateable = true;

	private float opacity3D = 1.f;

	private boolean isLightColorUpdateable = true;

	private Color lightColorForIsoSurface = Color.GREEN;

	private float[] slice2Dplane;

	private ITransferFunctionSampler currentSampler;

	/**
	 * set the new hull volume in global space
	 * @param rect
	 */
	public void setDrawRect(AABBox rect){
		drawCubeTransformation = getTransformationRepresentAABBox(rect);
		drawRect  = rect;
		isClippingUpdatable = true;
		islengthUpdatable = true;
		isHullVolumeUpdateable=true;
	}



	/**
	 * @param useGradient the useGradient to set
	 */
	public void setUseGradient(boolean useGradient) {
		this.useGradient = useGradient;
		isUseGradientUpdateable = true;
	}
	@Override
	protected void updateShaderAttributesSubClass(GL4 gl2) {

		accumulator.updateData(gl2);
		updateLightColor(gl2);
		GLErrorHandler.assertGL(gl2);
		updateBackgroundColor(gl2);
		GLErrorHandler.assertGL(gl2);
		updateActiveVolumes(gl2);
		GLErrorHandler.assertGL(gl2);

		updateIsoValue(gl2);
		GLErrorHandler.assertGL(gl2);
		updateOpacity3D(gl2);

		boolean update = updateVolumeStackData(gl2);

		GLErrorHandler.assertGL(gl2);
		if(update){


			updateLocalTransformationInverse(gl2);
			GLErrorHandler.assertGL(gl2);
		}
		updateGlobalScale(gl2);
		GLErrorHandler.assertGL(gl2);
		GLErrorHandler.assertGL(gl2);
		updateClippingPlanes(gl2);
		updateMaxDiagonalLength(gl2);
		updateTransferFunction(gl2);
		GLErrorHandler.assertGL(gl2);
		updateEye(gl2);
		GLErrorHandler.assertGL(gl2);
		updateSliceShown(gl2);
		GLErrorHandler.assertGL(gl2);
		updateSlice(gl2);
		GLErrorHandler.assertGL(gl2);
		updateSamples(gl2);
		GLErrorHandler.assertGL(gl2);
		updateUseGradient(gl2);
		GLErrorHandler.assertGL(gl2);


	}

	/**
	 * Uploads the iso surface lighte source color to the gpu
	 * @param gl2
	 */
	private void updateLightColor(GL4 gl2) {
		if(!isLightColorUpdateable){
			return;
		}
		float color[] = getNormalizedColor(lightColorForIsoSurface);

		gl2.glUniform3fv(getLocation(suvLightIntensiy),1, color,0);

		isLightColorUpdateable = false; 

	}

	/**
	 * Updates the opacity of the 3D volume for animations to the gpu
	 * @param gl2
	 */
	private void updateOpacity3D(GL4 gl2) {
		if(!isOpacity3DUpdateable){
			return;
		}
		gl2.glUniform1f(getLocation(suvOpacity3D), opacity3D);
		isOpacity3DUpdateable=false;
	}

	/**
	 * Uploads a flag whether the gradient values of the data should be used for rendering   
	 * @param gl2
	 */
	private void updateUseGradient(GL4 gl2) {
		if(!isUseGradientUpdateable){
			return;
		}
		gl2.glUniform1i(getLocation(suvUseGradient), boolToInt(useGradient));
	}

	/**
	 * Uploads the sample count for rendering
	 * @param gl2
	 */
	private void updateSamples(GL4 gl2) {
		if(!isSamplesUpdatable){
			return;
		}

		gl2.glUniform1i(getLocation(suvSamples), samples);
		isSamplesUpdatable = false;
	}

	/**
	 * Convertes a boolean to an int value for the shader
	 * @param bool
	 * @return
	 */
	private int boolToInt(boolean bool){
		if(bool){
			return 1;
		}else{
			return 0;
		}
	}

	/**
	 * Uploads a flag whether the big data viewer slice should be rendered in the 3D view 
	 * @param gl2
	 */
	private void updateSliceShown(GL4 gl2) {
		if(!isShownUpdatable){
			return;
		}

		gl2.glUniform1i(getLocation(suvShowSlice), boolToInt( this.showSlice));
	}

	/**
	 * Calculates and uploads the bdv slice plane
	 * @param gl2
	 */
	private void updateSlice(GL4 gl2) {
		if(!isSliceUpdateable){
			return;
		}

		slice2Dplane=calcSlicePlane();

		gl2.glUniform4fv(getLocation(suvNormalSlice), 1, slice2Dplane, 0);
		isSliceUpdateable=false;
	}

	/**
	 * calculates the bdv plane in global space
	 * @return
	 */
	private float[] calcSlicePlane() {
		float normVector[] = {0,0,1,0};

		//viewer to global
		Matrix4 bdvTransSafe = getNewIdentityMatrix();
		bdvTransSafe.multMatrix(getModelTransformation());
		bdvTransSafe.invert();

		//plane to global space
		float plane[] = transformPlane(bdvTransSafe, normVector);		

		//take the mirrored plane
		plane[3] *= -1.f;
		return plane;
	}

	/**
	 * returns the source
	 * @return
	 */
	@Override
	public MultiVolumeRendererShaderSource getSource(){
		return sources;
	}

	/**
	 * reset all update flags. Insert new updates here 
	 * @param flag
	 */
	private void setAllUpdate(boolean flag){

		isColorUpdateable = flag;
		isEyeUpdateable = flag;
		isIsoSurfaceValueUpdatable = flag;
		isBackgroundColorUpdateable = flag;
		isSliceUpdateable = flag;
		isSamplesUpdatable = flag;
		isClippingUpdatable = flag;
		islengthUpdatable = flag;
		isOpacity3DUpdateable = flag;
		isHullVolumeUpdateable = flag;
		isLightColorUpdateable = flag;
		isVertBufferUpdateable = flag;
		for(VolumeDataBlock data: dataManager.getVolumes()){
			data.setNeedsUpdate(true);
		}
		sources.setTransferFunctionCode(tf.getTransferFunctionShaderCode());
	} 

	/**
	 * Sets and connects a new volume data manager
	 * @param manager
	 */
	private void setVolumeDataManager(VolumeDataManager manager){
		dataManager = manager;
		this.dataManager.addVolumeDataManagerListener(new VolumeDataManagerAdapter() {

			@Override
			public void addedData(Integer i) {
				sources.setMaxNumberOfVolumes(dataManager.getVolumeKeys().size());
			}

			@Override
			public void dataRemoved(Integer i) {
				sources.setMaxNumberOfVolumes(dataManager.getVolumeKeys().size());
			}
		});
	}

	/**
	 * Constructor
	 * @param tf
	 * @param manager
	 */
	public MultiVolumeRenderer(TransferFunction1D tf, VolumeDataManager manager){
		setVolumeDataManager(manager);
		setTransferFunction(tf);
		setAccumulator(new MaximumVolumeAccumulator());
		setDrawRect(new AABBox(0, 0, 0, 1, 1, 1));
		sources.addSourceListener(new ISourceListener() {

			@Override
			public void sourceCodeChanged() {
				setNeedsRebuild(true);
				setAllUpdate(true);

			}
		});
	}

	/**
	 * Calculates the eye position in global space 
	 * @return
	 */
	private float[] calculateEyePosition(){
		float eyePositionsObjectSpace[] = new float[3];

		Matrix4 globalTransformation = getNewIdentityMatrix();
		globalTransformation.multMatrix(getView());
		globalTransformation.multMatrix(drawCubeTransformation);
		float eye[] = getEyeInCurrentSpace(globalTransformation);
		float eyeTransformer[] ={eye[0] ,eye[1],eye[2],1}; 
		float eyeTransformed[] = new float[4];

		drawCubeTransformation.multVec(eyeTransformer, eyeTransformed);
		for(int j = 0; j < eye.length; j++){
			eyePositionsObjectSpace[j] = eyeTransformed[j]/eyeTransformed[3];
		}

		return eyePositionsObjectSpace;
	}

	/**
	 * Calculates and uploads the eye position in global space 
	 * @param gl2
	 */
	private void updateEye(GL4 gl2){
		if(!isEyeUpdateable ){
			return;
		}

		float [] eyePositions = calculateEyePosition();

		//eye position
		gl2.glUniform3fv(getLocation(suvEyePosition), 
				1,eyePositions,0);
		GLErrorHandler.assertGL(gl2);

		isEyeUpdateable = false;
	}

	/**
	 * Updates the background color of the renderer
	 * @param gl2
	 */
	private void updateBackgroundColor(GL4 gl2) {
		if(!this.isBackgroundColorUpdateable){
			return;
		}

		float[] c=getNormalizedColor(backgroundColor);

		gl2.glUniform3fv(getLocation(suvBackgroundColor),1, c, 0);

		isBackgroundColorUpdateable = false;
	}

	/**
	 * calculates the individual length of the bounding volume in each texture space since it may scale by transformation 
	 * @param gl2
	 */
	private void updateMaxDiagonalLength(GL4 gl2) {
		if(!islengthUpdatable){
			return;
		}

		length = Float.MIN_VALUE;
		//length = (float)Math.sqrt(3d);
		//cube transform in texture space to get the maximum extend
		final float diagVec[]={drawRect.getWidth(),drawRect.getHeight(),drawRect.getDepth(),0};
		float runLength = VectorUtil.normVec3(diagVec);

		gl2.glUniform1f(getLocation(suvRenderRectStepSize), runLength/(float)samples);
		GLErrorHandler.assertGL(gl2);
		//create a logical stepsize for the the classifications
		float sizeDim = 0;
		for(int i = 0; i < 3; i ++){
			sizeDim = Math.max(sizeDim, diagVec[i]);
		} 
		for(int i = 0; i< 3; i++){
			diagVec[i]/=sizeDim;
		}

		length = runLength;//VectorUtil.normVec3(diagVec);

		gl2.glUniform1f(getLocation(suvTransferFuntionSize), length);
		GLErrorHandler.assertGL(gl2);

		islengthUpdatable = false;
		isColorUpdateable = true;
	}

	/**
	 * Uploads the active flags for the volume stacks
	 * @param gl2
	 */
	private void updateActiveVolumes(GL4 gl2) {
		IntBuffer activeBuffers = Buffers.newDirectIntBuffer(sources.getMaxNumberOfVolumes());
		activeBuffers.rewind();
		for(int i = 0; i<sources.getMaxNumberOfVolumes();i++){
			int active; 
			if(dataManager.isEnabled(i)){
				active=1;
			}else{
				active=0;
			}
			activeBuffers.put(i, active);

		}
		activeBuffers.rewind();
		gl2.glUniform1iv(getLocation(suvActiveVolumes),
				activeBuffers.capacity(),activeBuffers);
	}

	/**
	 * Uploads the local inverses of the volume stacks 
	 * @param gl2
	 */
	private void updateLocalTransformationInverse(GL4 gl2) {
		int numberOfVolumes = dataManager.getVolumeKeys().size();
		float localInverses[] = new float[16*numberOfVolumes];
		for(Integer index: dataManager.getVolumeKeys()){
			VolumeDataBlock data = dataManager.getVolume(index);


			Matrix4 localInverse = fromCubeToVolumeSpace( data);
			int k=0;
			for(float f: localInverse.getMatrix()){
				localInverses[16*index+k] =f;
				k++;
			}
		}
		gl2.glUniformMatrix4fv(getLocation(suvTextureTransformationInverse),
				numberOfVolumes,false,localInverses,0);

		GLErrorHandler.assertGL(gl2);
		//updateNormalAxises(gl2,localInverses);GLErrorHandler.assertGL(gl2);
	}


	/**
	 * transforms the xyz Axis of the bounding volume in the local texture coordinate systems
	 * @param gl2 context to upload the data
	 * @param localInverses the transformation matrices from draw rectangle space to texture space
	 */
	private void updateNormalAxises(GL4 gl2, Map<Integer, Matrix4> localInverses) {
		float drawRectAxises[][] = {{1,0,0,0},{0,1,0,0},{0,0,1,0}};

		//transform and upload TODO
		for(Integer volume: localInverses.keySet()){
			float axisesInTextSpace[][] = new float[3][4]; 
			Matrix4  transformation = localInverses.get(volume);
			transformation.invert();
			transformation.transpose();

			for(int axis=0; axis < drawRectAxises.length; axis++){
				transformation.multVec(drawRectAxises[axis],axisesInTextSpace[axis]);
			}
		}
	}

	/**
	 * Calculates the clipping planes for the ray based of drawing rects
	 * @param gl
	 * @param localInverses
	 */
	private void updateClippingPlanes(GL4 gl) {
		if(!isClippingUpdatable){
			return;
		}

		float planesInDrawRectSpace[]= new float[]{
				1,0,0,drawRect.getMaxX(),
				-1,0,0,-drawRect.getMinX(),
				0,1,0,drawRect.getMaxY(),
				0,-1,0,-drawRect.getMinY(),
				0,0,1,drawRect.getMaxZ(),
				0,0,-1,-drawRect.getMinZ()
		};

		if(getLocation(suvRenderRectClippingPlanes) != -1){
			gl.glUniform4fv(getLocation(suvRenderRectClippingPlanes), 6, planesInDrawRectSpace, 0);
		}

		isClippingUpdatable = false;
	}

	/**
	 * Uploads the unit cube to hull volume transformation
	 * @param gl2
	 */
	private void updateGlobalScale(GL4 gl2) {

		if(!isHullVolumeUpdateable){
			return;
		}
		gl2.glUniformMatrix4fv(getLocation(suvDrawCubeTransformation),1,false,drawCubeTransformation.getMatrix(),0);
		isHullVolumeUpdateable = false;
	}

	/**
	 * uplaods the iso value for the surfaces
	 * @param gl2
	 */
	private void updateIsoValue(GL4 gl2){
		if(!isIsoSurfaceValueUpdatable){
			return;
		}
		gl2.glUniform1f(getLocation(suvIsoValue), isoSurfaceValue);

		isIsoSurfaceValueUpdatable = false;
	}

	/**
	 * Upload the volume stack data
	 * @param gl2
	 * @return
	 */
	private boolean updateVolumeStackData(GL4 gl2){

		float min = Float.MAX_VALUE;
		boolean somethingUpdated = false;

		for(Integer i : dataManager.getVolumeKeys()){
			VolumeDataBlock data = dataManager.getVolume(i);

			min = Math.min(min, data.minValue);


			if(!data.needsUpdate()){
				continue;
			}

			somethingUpdated= true;
			isSliceUpdateable = true;

			//get Buffer
			FloatBuffer buffer = Buffers.newDirectFloatBuffer(data.data);
			buffer.rewind();

			//uploade data
			int dim[];

			dim = new int[]{(int)data.memSize[0], 
					(int)data.memSize[1], 
					(int)data.memSize[2]};
			if(buffer.capacity()>0){
				volumeTextureMap.get(i).update(gl2, 0, buffer, dim);
			}
			if(getArrayEntryLocation(gl2,suvVoxelOffsets,i)!=-1){
				int off[] = new int[]{
						(int)data.memOffset[0],
						(int)data.memOffset[1],
						(int)data.memOffset[2]
				};
				gl2.glUniform3iv(getArrayEntryLocation(gl2,suvVoxelOffsets,i), 1,off,0 );
			}
			if(getArrayEntryLocation(gl2,suvVoxelCount,i)!=-1){
				gl2.glUniform3iv(getArrayEntryLocation(gl2,suvVoxelCount,i), 1,dim,0 );
			}
			data.setNeedsUpdate(false);
		}

		//update values
		if(somethingUpdated){
			//min max
			gl2.glUniform1f(getLocation(suvMinVolumeValue), min);
			gl2.glUniform1f(getLocation(suvMaxVolumeValue), dataManager.getGlobalMaxVolumeValue());
		}
		return somethingUpdated;
	}

	@Override
	protected void generateIdMappingSubClass(GL4 gl2) {


		mapUniforms(gl2, new String[]{		
				suvVoxelCount,
				suvDrawCubeTransformation,
				suvTextureTransformationInverse,
				suvActiveVolumes,
				suvEyePosition,
				suvMinVolumeValue,
				suvMaxVolumeValue,
				suvVolumeTexture,
				suvColorTexture,
				suvIsoValue, 
				suvBackgroundColor,
				suvNormalSlice,
				suvShowSlice,
				suvSamples,
				suvUseGradient,
				suvRenderRectClippingPlanes,
				suvRenderRectStepSize,
				suvTransferFuntionSize,
				suvOpacity3D,
				suvLightIntensiy,
				suvVoxelOffsets
		});

		accumulator.init(gl2);

		int location;
		for(int i =0; i< sources.getMaxNumberOfVolumes(); i++){
			location = getArrayEntryLocation(gl2, suvVolumeTexture, i);
			Texture t = createVolumeTexture(gl2, location);
			t.setTexParameteri(GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
			t.setShouldGenerateMidmaps(true);
			volumeTextureMap.put(i, t);
			GLErrorHandler.assertGL(gl2);
		}
		this.currentSampler = tf.getSampler();
		currentSampler.init(gl2, getLocation(suvColorTexture));

	}

	@Override
	protected void updateVertexBufferSubClass(GL4 gl2, VertexAttribute position) {
		if(!isVertBufferUpdateable){
			return;
		}
		FloatBuffer bufferData = Buffers.newDirectFloatBuffer(coordinates);
		bufferData.rewind();

		position.setAttributeValues(gl2, bufferData);
		isVertBufferUpdateable = false;
	}

	@Override
	public void setModelTransformation(Matrix4 modelTransformations) {
		if(modelTransformations.equals(getModelTransformation())){
			return;
		}
		super.setModelTransformation(modelTransformations);

		isSliceUpdateable = true;
	}

	@Override
	public void setView(Matrix4 view) {
		if(view.equals(getView())){
			return;
		}
		super.setView(view);

		isEyeUpdateable = true;
	}

	/**
	 * Update the transfer function configuration
	 * @param gl2
	 */
	private void updateTransferFunction(GL4 gl2){
		if(!isColorUpdateable){
			return;
		}

		//get Buffer last key is the highest number 
		//FloatBuffer buffer = tf.getTexture(length/(float)samples); 

		tf.getSampler().updateData(gl2,tf, length/(float)samples);

		//upload data

		//gl2.glBindTexture(GL2.GL_TEXTURE_1D, 0);
		isColorUpdateable = false;
	}


	@Override
	protected int getVertexBufferSize() {

		return coordinates.length * Buffers.SIZEOF_FLOAT;
	}

	@Override
	protected void renderSubClass(GL4 gl2) {
		gl2.glDrawArrays(GL4.GL_TRIANGLE_STRIP, 0,coordinates.length/3);
	}

	/**
	 * Updates color data
	 * @param newData
	 */
	public void setTransferFunction(final TransferFunction1D tf){
		this.tf = tf;

		this.tf.addTransferFunctionListener(new TransferFunctionAdapter() {

			@Override
			public void functionPointChanged(TransferFunction1D transferFunction) {
				isColorUpdateable =true;

			}

			@Override
			public void samplerChanged(TransferFunction1D transferFunction1D) {
				setNeedsRebuild(true);
				setAllUpdate(true);
			}
		});
		sources.setTransferFunctionCode(this.tf.getTransferFunctionShaderCode());
	}

	/**
	 * @return the drawCubeTransformation
	 */
	public Matrix4 getDrawCubeTransformation() {
		return copyMatrix( drawCubeTransformation);
	}	

	/**
	 * @param backgroundColor the backgroundColor to set
	 */
	public void setBackgroundColor(Color backgroundColor) {
		if(this.backgroundColor.equals(backgroundColor)){
			return;
		}
		this.backgroundColor = backgroundColor;
		isBackgroundColorUpdateable=true;
	}


	@Override
	protected void disposeSubClass(GL4 gl2) {


		currentSampler.dispose(gl2);
		for(Texture texture: volumeTextureMap.values() ){
			texture.delete(gl2);
		}

		accumulator.disposeGL(gl2);
		setAllUpdate(true);
	}

	/**
	 * setter for iso value. triggers update on next rendering
	 * @param floatValue
	 */
	public void setIsoSurface(float floatValue) {
		this.isoSurfaceValue = floatValue;

		isIsoSurfaceValueUpdatable = true;
	}

	/**
	 * setter for showing slice. triggers update on next rendering
	 * @param selected
	 */
	public void setSliceShown(boolean selected) {
		this.showSlice = selected;

		isShownUpdatable  = true; 

	}

	/**
	 * setter for sample count. triggers update on next rendering
	 * @param intValue
	 */
	public void setSamples(int intValue) {
		this.samples = intValue;

		isSamplesUpdatable = true;
		isColorUpdateable = true;
		islengthUpdatable = true;

	}

	/**
	 * @param accumulatur the accumulator to set
	 */
	public void setAccumulator(AbstractVolumeAccumulator accumulator) {
		this.accumulator = accumulator;
		accumulator.setParent(this);
		sources.setAccumulator(accumulator);
	}

	/**
	 * return The associated data manager 
	 */
	public VolumeDataManager getDataManager() {
		return dataManager;
	}

	/**
	 * 
	 * @return the current bounding volume to draw
	 */
	public AABBox getDrawRect() {

		return drawRect;
	}

	/**
	 * @return the opacity3D
	 */
	public float getOpacity3D() {
		return opacity3D;
	}

	/**
	 * @param opacity3d the opacity3D to set
	 */
	public void setOpacity3D(float opacity3d) {
		opacity3D = opacity3d;
		isOpacity3DUpdateable = true;
	}

	/**
	 * @return the lightColorForIsoSurface
	 */
	public Color getLightColorForIsoSurface() {
		return lightColorForIsoSurface;
	}

	/**
	 * @param lightColorForIsoSurface the lightColorForIsoSurface to set
	 */
	public void setLightColorForIsoSurface(Color lightColorForIsoSurface) {
		this.lightColorForIsoSurface = lightColorForIsoSurface;
		isLightColorUpdateable = true;
	}

	/**
	 * @return the slice2Dplane
	 */
	public float[] getSlice2Dplane() {
		if(slice2Dplane == null){
			slice2Dplane = calcSlicePlane();
		}
		return slice2Dplane;
	}
}
