package bdv.jogl.VolumeRenderer.shaderprogram;

import java.nio.Buffer;


import com.jogamp.opengl.GL4;

/**
 * Class to handle vertex Attributes
 * @author michael
 *
 */
public class VertexAttribute {

	private final VertexBuffer associatedVertexBuffer;
	
	private final int attributeLocation;
	
	private int vao;
	
	private final boolean normaized = false;
	
	private final int glDataType; 
	
	private final int elementCount;
	
	private final int dataTypeSize;
	
	private void generateAttributeArray(GL4 gl2){
	
		//gen vertex array
		int[] vertexArrays  = new int[1];
		gl2.glGenVertexArrays(1, vertexArrays, 0);
		vao = vertexArrays[0];
	}
	
	private void generatePointer(GL4 gl2){

		associatedVertexBuffer.bind(gl2);
		
		bind(gl2);
		
		gl2.glEnableVertexAttribArray(attributeLocation);

		gl2.glVertexAttribPointer(
				attributeLocation, 
				elementCount, 
				glDataType, 
				normaized, 
				0,//Buffers.SIZEOF_FLOAT*3, 
				0);
		
		unbind(gl2);
		
		associatedVertexBuffer.unbind(gl2);
	}	
	public VertexAttribute(GL4 gl2,
			int location, 
			int glDataType, 
			int elementsPerVertex,
			int dataTypeSize){
		
		this.associatedVertexBuffer = new VertexBuffer(gl2);
		
		this.attributeLocation = location;	
		
		this.elementCount = elementsPerVertex;
		
		this.glDataType = glDataType;
		
		this.dataTypeSize = dataTypeSize;
		
		generateAttributeArray(gl2);

		generatePointer(gl2);	
	}
	
	
	public void allocateAttributes(GL4 gl2,int numberOfElements){
		
		bind(gl2);
		
		this.associatedVertexBuffer.allocateMemory(gl2, numberOfElements *elementCount*dataTypeSize);
		
		unbind(gl2);
	}
	
	public void setAttributeValues(GL4 gl2, final Buffer data){
		
		bind(gl2);
		
		this.associatedVertexBuffer.bind(gl2);
		
		this.associatedVertexBuffer.memcopyData(gl2, data, elementCount*dataTypeSize, 0);
		
		this.associatedVertexBuffer.unbind(gl2);
		
		unbind(gl2);
	}
	
	public VertexBuffer getVBO(){
		return associatedVertexBuffer;
	}
	
	public void bind(GL4 gl2){
		//bind
		gl2.glBindVertexArray(vao);
		
	}
	
	public void unbind(GL4 gl2){
		//unbind
		gl2.glBindVertexArray(0);
	}

	public void delete(GL4 gl2){
		associatedVertexBuffer.delete(gl2);
		int[] arrays = {vao};
		gl2.glDeleteVertexArrays(1, arrays, 0);
	}
}
