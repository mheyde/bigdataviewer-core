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
	
	private void generateBuffer(GL4 gl2){
		//vertex buffer
		int[] vertexBufferObject = new int[1];
		gl2.glGenBuffers(1,vertexBufferObject,0 );
		vbo =  vertexBufferObject[0];
	}
	
	public VertexBuffer(GL4 gl2){
		generateBuffer(gl2);
	}
	
	/**
	 * Allocates gpu memory for the buffer
	 * @param gl2
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
	 * @param gl2
	 */
	public void bind(GL4 gl2){
		gl2.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo);
	}
	
	/**
	 * Unbinds the buffer
	 * @param gl2
	 */
	public void unbind(GL4 gl2){
		gl2.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
	}
	
	public void delete(GL4 gl2){
		int[] buffers = {vbo};
		gl2.glDeleteBuffers(1, buffers,0);
	}

}
