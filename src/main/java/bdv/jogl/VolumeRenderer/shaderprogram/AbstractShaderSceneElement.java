package bdv.jogl.VolumeRenderer.shaderprogram;

import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.AbstractShaderSource.*;

import java.util.HashMap;
import java.util.Map;

import bdv.jogl.VolumeRenderer.scene.ISceneElements;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.AbstractShaderSource;
import bdv.jogl.VolumeRenderer.utils.MatrixUtils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;


/**
 * Defines standard behavior for shader elements.
 * @author michael
 *
 */
public abstract class AbstractShaderSceneElement implements ISceneElements{

	private Matrix4 projection = MatrixUtils.getNewIdentityMatrix();

	private Matrix4 view = MatrixUtils.getNewIdentityMatrix();

	private Matrix4 modelTransformations = MatrixUtils.getNewIdentityMatrix();

	private Map<String, Integer> shaderVariableMapping = new HashMap<String, Integer>();

	private ShaderProgram shaderProgram= new ShaderProgram();

	private VertexAttribute position;
	
	private boolean isEnabled = true;
	
	private boolean needsRebuild = false;
	
	private boolean viewNeedsUpdate = true;
	
	private boolean projectionNeedsUpdate = true;
	
	private boolean modelNeedsUpdate = true;

	private boolean needsInit = true;

	/**
	 * @param needsRebuild the needsRebuild to set
	 */
	public void setNeedsRebuild(boolean needsRebuild) {
		this.needsRebuild = needsRebuild;
	}

	/**
	 * @param projection the projection to set
	 */
	public void setProjection(final Matrix4 projection) {
		if(this.projection.equals(projection)){
			return;
		}
		this.projection = MatrixUtils.copyMatrix(projection);
		this.projectionNeedsUpdate = true;
	}

	/**
	 * @return the view
	 */
	public Matrix4 getView() {
		return MatrixUtils.copyMatrix(view);
	}

	/**
	 * @return the projection
	 */
	public Matrix4 getProjection() {
		return projection;
	}

	/**
	 * @param view the view to set
	 */
	public void setView(final Matrix4 view) {
		if(this.view.equals(view)){
			return;
		}
		this.view = MatrixUtils.copyMatrix(view);
		this.viewNeedsUpdate = true;
	}

	/**
	 * Retruns the Source object of the shader element
	 */
	protected abstract AbstractShaderSource getSource();
	
	/**
	 * releases all gl resources
	 * @param gl2
	 */
	public void disposeGL(GL4 gl2){
		
		disposeSubClass(gl2);
		
		position.delete(gl2);

		shaderProgram.destroy(gl2);
		
		for(ShaderCode code: getSource().getShaderCodes()){
			code.destroy(gl2);
		}
		
		modelNeedsUpdate = true;
		viewNeedsUpdate = true;
		projectionNeedsUpdate = true;
		needsRebuild = true;
	}
	
	/**
	 * Subclass hooks for disposal
	 * @param gl2
	 */
	protected void disposeSubClass(GL4 gl2){
		
	}

	/**
	 * @return the isEnabled
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * @param isEnabled the isEnabled to set
	 */
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * initializes the shader program
	 * @param gl
	 */
	public void init(GL4 gl){

		initProgram(gl);

		generateIdMapping(gl);

		generateVertexBuffer(gl);

		updateVertexBuffer(gl);
		
		needsInit = false;
	}

	/**
	 * update all shader variables 
	 */
	public void update(GL4 gl){
		if(needsRebuild){
			disposeGL(gl);
			init(gl);
			needsRebuild = false;
		}
		
		if(needsInit){
			init(gl);
		}
		
		updateShaderAttributes(gl);

		updateVertexBuffer(gl);
	}

	/**
	 * Sub class implemented vertex upload.
	 * @param gl2
	 */
	protected abstract void updateVertexBufferSubClass(GL4 gl2, VertexAttribute position);


	/**
	 * Activates vertex buffer and uploads data by calling updateVertexBufferSubClass.
	 * @param gl2
	 */
	private void updateVertexBuffer(GL4 gl2){

		shaderProgram.useProgram(gl2, true);

		updateVertexBufferSubClass(gl2,position);

		shaderProgram.useProgram(gl2, false);
	}

	/**
	 * Creates a name location mapping for uniform variables
	 * @param gl2 gl context
	 * @param uniforms List of uniform names to map 
	 */
	public void mapUniforms(GL4 gl2, final String[] uniforms){
		int location = -1;
		for(String uniform:uniforms){
			location = gl2.glGetUniformLocation(shaderProgram.program(), uniform);
			shaderVariableMapping.put(uniform, location);
		}
	}

	/**
	 * Creates a name location mapping for attribute variables
	 * @param gl2 gl context
	 * @param uniforms List of uniform names to map 
	 */
	public void mapAvertexAttributs(GL4 gl2, final String[] attributes){
		int location = -1;
		for(String attribute:attributes){
			location = gl2.glGetAttribLocation(shaderProgram.program(), attribute);
			shaderVariableMapping.put(attribute, location);
		}
	}


	/**
	 * Returns the location of a certain variable 
	 * @param variableName Name of the shader variable
	 * @return
	 */
	public int getLocation(final String variableName){
		return shaderVariableMapping.get(variableName);
	}

	public int getArrayEntryLocation(GL4 gl, final String variableName, int index){
		return gl.glGetUniformLocation(shaderProgram.program(),variableName+"["+index+"]");
	}

	/**
	 * Sub class uniform upload
	 * @param gl2
	 */
	protected abstract void  updateShaderAttributesSubClass(GL4 gl2);


	/**
	 * uploads uniform shader variables to the graphic device 
	 * @param gl2
	 */
	private void updateShaderAttributes(GL4 gl2){

		shaderProgram.useProgram(gl2, true);

		//memcopy
		if(projectionNeedsUpdate){
			gl2.glUniformMatrix4fv(shaderVariableMapping.get(suvProjectionMatrix), 1, false, projection.getMatrix(),0);
			projectionNeedsUpdate = false;
		}
		
		if(viewNeedsUpdate){
			gl2.glUniformMatrix4fv(shaderVariableMapping.get(suvViewMatrix), 1, false, view.getMatrix(),0);
			viewNeedsUpdate = false;
		}
		
		if(modelNeedsUpdate){
			gl2.glUniformMatrix4fv(shaderVariableMapping.get(suvModelMatrix), 1, false, modelTransformations.getMatrix(),0);
			modelNeedsUpdate = false;
		}
		updateShaderAttributesSubClass(gl2);

		shaderProgram.useProgram(gl2, false);
	}


	/**
	 * Sub class id mapping for special ids.
	 * @param gl2
	 */
	protected abstract void generateIdMappingSubClass(GL4 gl2);


	/**
	 * Mapps all Shader variables to the shaderVariableMapping, also does for sub classes
	 * @param gl2
	 */
	private void generateIdMapping(GL4 gl2){
		shaderProgram.useProgram(gl2, true);

		//get ids
		mapUniforms(gl2, new String[]{
				suvProjectionMatrix,
				suvViewMatrix,
				suvModelMatrix});

		mapAvertexAttributs(gl2, new String[]{satPosition});

		generateIdMappingSubClass(gl2);

		shaderProgram.useProgram(gl2, false);
	}

	/**
	 * Creates a shader program
	 * @param gl2
	 */
	private void initProgram(GL4 gl2){
		//create program id 
		shaderProgram.init(gl2);
		
		//compile and attache shaders from files
		for(ShaderCode code:getSource().getShaderCodes()){
			code.compile(gl2,System.err);
			if(!code.isValid()){
				throw new IllegalArgumentException("No valid shader code in "+ code);
			}
			shaderProgram.add(code);
		}
		
		//link program
		shaderProgram.link(gl2, System.err);

		shaderProgram.useProgram(gl2, false);
	}

	/**
	 * Returns the size of the Vertex buffer needed by the sub class
	 * @return
	 */
	protected abstract int getVertexBufferSize();


	/**
	 * Creates a vertex buffer for the current program
	 * @param gl2
	 */
	private void generateVertexBuffer(GL4 gl2){

		position = new VertexAttribute(
				gl2, 
				getLocation(satPosition), 
				GL4.GL_FLOAT, 3, Buffers.SIZEOF_FLOAT);
		
		position.allocateAttributes(gl2, getVertexBufferSize());
	}


	/**
	 * render call, updates data if needed an delivers the program context for sub classes
	 */
	public void render(GL4 gl2){
		if(!isEnabled){
			return;
		}
		update(gl2);

		shaderProgram.useProgram(gl2, true);

		position.bind(gl2);
		
		renderSubClass(gl2);
		
		position.unbind(gl2);

		shaderProgram.useProgram(gl2, false);
	}


	/**
	 * Function containing the actual render call. program and Vertex buffer are bound in there.
	 * @param gl2
	 */
	protected abstract void renderSubClass(GL4 gl2);

	/**
	 * @return the modelTransformations
	 */
	public Matrix4 getModelTransformation() {
		return MatrixUtils.copyMatrix(modelTransformations);
	}


	/**
	 * @param modelTransformations the modelTransformations to set
	 */
	public void setModelTransformation(final Matrix4 modelTransformations) {
		if(this.modelTransformations.equals(modelTransformations)){
			return;
		}
		this.modelTransformations = MatrixUtils.copyMatrix(modelTransformations);
		this.modelNeedsUpdate = true;
	}
}
