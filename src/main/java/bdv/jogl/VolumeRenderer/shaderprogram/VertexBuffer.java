package bdv.jogl.VolumeRenderer.shaderprogram;

import java.nio.Buffer;

import com.jogamp.opengl.GL4;

/**
 * Class encapsulating gl vbo routines
 * @author michael
 *
 */
public class VertexBuffer {

	private int vbo; 
	
	/**
	 * Generates the opengl buffer object
	 * @param gl2 the gl context to use
	 */
	private void generateBuffer(GL4 gl2){
		//vertex buffer
		int[] vertexBufferObject = new int[1];
		gl2.glGenBuffers(1,vertexBufferObject,0 );
		vbo =  vertexBufferObject[0];
	}
	
	/**
	 * Constructor
	 * @param gl2 the gl context to use
	 */
	public VertexBuffer(GL4 gl2){
		generateBuffer(gl2);
	}
	
	/**
	 * Allocates gpu memory for the buffer
	 * @param gl2 the gl context to use
	 * @param sizeInBytes Memory size
	 */
	public void allocateMemory(GL4 gl2, int sizeInBytes){
		
		bind(gl2);
		
		gl2.glBufferData(GL4.GL_ARRAY_BUFFER, 
				sizeInBytes,
				null, 
				GL4.GL_STATIC_DRAW);
		
		unbind(gl2);
		//System.out.println("allocated "+ sizeInBytes);
	}
	
	/**
	 * Copies data for the buffer object to the gpu after the storage was allocated
	 * @param gl2 the gl context to use
	 * @param data the data buffer to upload
	 * @param elementSize the mem size of one element
	 * @param offset the offset on the vbo storage
	 */
	public void memcopyData(GL4 gl2, final Buffer data, int elementSize, int offset){
		bind(gl2);
		gl2.glBufferSubData(
				GL4.GL_ARRAY_BUFFER,
				offset, 
				data.capacity() * elementSize, 
				data);
		
		unbind(gl2);
		//System.out.println("memcopy "+ data.capacity() * elementSize );
	}
	
	/**
	 * Binds the buffer to the current context
	 * @param gl2 the gl context to use
	 */
	public void bind(GL4 gl2){
		gl2.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo);
	}
	
	/**
	 * Un-binds the buffer
	 * @param gl2 the gl context to use
	 */
	public void unbind(GL4 gl2){
		gl2.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Deletes the opengl buffer object 
	 * @param gl2 the gl context to use
	 */
	public void delete(GL4 gl2){
		int[] buffers = {vbo};
		gl2.glDeleteBuffers(1, buffers,0);
	}
}
