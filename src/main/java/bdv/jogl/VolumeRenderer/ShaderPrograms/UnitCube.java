package bdv.jogl.VolumeRenderer.ShaderPrograms;

import java.awt.Color;
import java.nio.FloatBuffer;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.UnitCubeShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.WindowUtils.getNormalizedColor;
import static bdv.jogl.VolumeRenderer.utils.GeometryUtils.*;
import bdv.jogl.VolumeRenderer.GLErrorHandler;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.UnitCubeShaderSource;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

/**
 * class to render a cube in gl
 * @author michael
 *
 */
public class UnitCube extends AbstractShaderSceneElement{

	private boolean renderWireframe = false;
	
	private boolean colorNeedsUpdate = true;

	private Color color = new Color(1f, 1f, 1f, 1f);
	
	private UnitCubeShaderSource source = new UnitCubeShaderSource();

	private boolean isVertBufferUpdateable = true;

	@Override
	protected UnitCubeShaderSource getSource() {
		return source;
	};
	
	protected void updateShaderAttributesSubClass(GL4 gl2){
		if(!colorNeedsUpdate){
			return;
		}
		float[] rgba = getNormalizedColor(color);
		gl2.glUniform4fv(getLocation(suvColor),1,rgba,0);
		colorNeedsUpdate = false;
	}

	
	protected void generateIdMappingSubClass(GL4 gl){
		mapUniforms(gl, new String[]{suvColor});
	}


	/**
	 * @return the renderWireframe
	 */
	public boolean isRenderWireframe() {
		return renderWireframe;
	}

	
	/**
	 * @param renderWireframe the renderWireframe to set
	 */
	public void setRenderWireframe(boolean renderWireframe) {
		this.renderWireframe = renderWireframe;
	}
	
	
	protected void renderSubClass(GL4 gl2){
		GLErrorHandler.assertGL(gl2);
		float coordinates[];
		int drawDirective;
		if(isRenderWireframe()){
			gl2.glDisable(GL4.GL_DEPTH_TEST);
			drawDirective= GL4.GL_LINES;
			coordinates = getUnitCubeEdges(); 
		}else{
			drawDirective = GL4.GL_TRIANGLE_STRIP;
			coordinates = getUnitCubeVerticesTriangles();
		}
		GLErrorHandler.assertGL(gl2);
		gl2.glDrawArrays(drawDirective, 0,coordinates.length/3);
		GLErrorHandler.assertGL(gl2);
	}

	
	protected void updateVertexBufferSubClass(GL4 gl2, VertexAttribute position){
		if(!isVertBufferUpdateable){
			return;
		}
		FloatBuffer bufferData;
		
		if(renderWireframe){
			bufferData = Buffers.newDirectFloatBuffer(getUnitCubeEdges());
		}else{
			bufferData = Buffers.newDirectFloatBuffer(getUnitCubeVerticesTriangles());
		}
		bufferData.rewind();

		position.setAttributeValues(gl2, bufferData);
		isVertBufferUpdateable  = false;
		
		
	}


	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	
	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
		this.colorNeedsUpdate = true;
	}

	@Override
	protected void disposeSubClass(GL4 gl2) {
		colorNeedsUpdate = true;
	};
	
	protected int getVertexBufferSize(){
		float[] coordinates;
		if(renderWireframe){
			coordinates = getUnitCubeEdges();
		}else{
			coordinates = getUnitCubeVerticesQuads();
		}
		return coordinates.length * Buffers.SIZEOF_FLOAT;
	}
}
