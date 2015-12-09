package bdv.jogl.VolumeRenderer.scene;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bdv.jogl.VolumeRenderer.GLErrorHandler;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;

import static bdv.jogl.VolumeRenderer.utils.GLUtils.contextSupportsExtension;

/**
 * Class representing gl textures. Sadly jogl does not support 1D and 3D Textures. 
 * @author michael
 *
 */
public class Texture {

	private static Set<Integer> usedTextureUnits = new HashSet<Integer>();
	
	private int textureUnit;
	
	private int textureObject;
	
	private final int textureType;
	
	private final int variableLocation;

	private final int internalFormat;

	private final int pixelFormat;
	
	private final int pixelDataType;
	
	private final Map<Integer, Integer> parameteriMap = new HashMap<Integer, Integer>();
	
	private final Map<Integer, int[]> parameterivMap = new HashMap<Integer, int[]>();
	
	private final Map<Integer, float[]> parameterfvMap = new HashMap<Integer, float[]>();
	
	private final Map<Integer, Boolean> updatableTextureParameters = new HashMap<Integer, Boolean>();

	private boolean sparseMemoryAllocated = false;

	private boolean shouldGenerateMidmaps = false;
	

	/**
	 * Constructor
	 * @param textureType type of the texture (GL_TEXTURE_<N>D)
	 * @param variableLocation Location of the texture variable in the shader program.
	 * @param internalFormat the texture internal format
	 * @param pixelFormat the pixel format
	 * @param pixelDataType the data format
	 */
	public Texture(int textureType, int variableLocation, int internalFormat, int pixelFormat, int pixelDataType) {

		this.textureType = textureType;
		this.variableLocation = variableLocation;
		this.internalFormat = internalFormat;
		this.pixelFormat = pixelFormat;
		this.pixelDataType = pixelDataType;
	}
	
	/**
	 * Generates the texture binding for the glsl shaders
	 * @param gl2 the gl context to use
	 */
	public void genTexture(GL4 gl2){
		int testUnit = GL2.GL_TEXTURE0; 
		
		//find next free unit
		for(;;){
			if(!usedTextureUnits.contains( testUnit)){
				usedTextureUnits.add(testUnit);
				break;
			}
			testUnit++;
		}
		textureUnit = testUnit;
		
		//activate texture
		gl2.glActiveTexture(textureUnit);

		//generate texture object
		int[] textures = new int[1];
		gl2.glGenTextures(textures.length, textures,0);
		textureObject = textures[0];

		rebindTexture(gl2);

	}
	
	/**
	 * Binds the texture object to the current shader program context
	 * @param gl2 the gl context to use
	 */
	private void rebindTexture(GL4 gl2){
		gl2.glBindTexture(textureType, textureObject);

		int logicalTextureUnit = textureUnit-GL2.GL_TEXTURE0;

		//activate texture unit
		gl2.glUniform1i(variableLocation,logicalTextureUnit);
	}
	
	/**
	 * Test method to check whether sparse textures are supported
	 * @param context the gl context to use
	 * @return true if supported
	 */
	public static boolean isSparseTextureSupported(GL context){
		return contextSupportsExtension(context, "GL_ARB_sparse_texture");
	}
	
	/**
	 * Returns the virtual page sizes needed for sparse textures 
	 * @param gl the gl context to use
	 * @return the virtual pages size in x,y,z
	 */
	public int[] getVirtPageSizes(GL4 gl){
		int pagesizes[] = new int[3];
		gl.glGetInternalformativ(textureType, internalFormat,GL4.GL_VIRTUAL_PAGE_SIZE_X_ARB, 1,pagesizes, 0);
		gl.glGetInternalformativ(textureType, internalFormat,GL4.GL_VIRTUAL_PAGE_SIZE_Y_ARB, 1,pagesizes, 1);
		gl.glGetInternalformativ(textureType, internalFormat,GL4.GL_VIRTUAL_PAGE_SIZE_Z_ARB, 1,pagesizes, 2);
		return pagesizes;
	}
	
	/**
	 * Uploads sparse texture data
	 * @param gl2 the gl context to use
	 * @param mipmapLevel the mipmap level to update
	 * @param data the data buffer 
	 * @param virtualDimensions the virtual dimensions of the texture
	 * @param offsets the texture offset xyz
	 * @param sizes the element size to set xyz
	 * @throws UnsupportedOperationException if sparse textures are not supported.
	 */
	public void updateSparse(GL4 gl2,int mipmapLevel, Buffer data, int[] virtualDimensions, int[] offsets, int[] sizes) {
		if(!isSparseTextureSupported(gl2)){
			throw new UnsupportedOperationException("sparse textures are not supported on your system!");
		}
		if(!sparseMemoryAllocated){
			delete(gl2);
			genTexture(gl2);		
			rebindTexture(gl2);
			setTexParameteri(GL4.GL_TEXTURE_SPARSE_ARB, GL2.GL_TRUE);
			GLErrorHandler.assertGL(gl2);
			setTexParameteri(GL4.GL_VIRTUAL_PAGE_SIZE_INDEX_ARB, 0);
			GLErrorHandler.assertGL(gl2);
			gl2.glActiveTexture(textureUnit);
			rebindTexture(gl2);		
		}
		
		
		GLErrorHandler.assertGL(gl2);
		//activate context
		gl2.glActiveTexture(textureUnit);
		GLErrorHandler.assertGL(gl2);
		rebindTexture(gl2);
		updateTextureParameters(gl2);
		GLErrorHandler.assertGL(gl2);
		if(shouldGenerateMidmaps){
			gl2.glGenerateMipmap(textureType);
		}
		GLErrorHandler.assertGL(gl2);

		int pagesizes[] = new int[6];
		gl2.glGetInternalformativ(textureType, internalFormat, GL4.GL_NUM_VIRTUAL_PAGE_SIZES_ARB, 3,pagesizes,3);
		gl2.glGetInternalformativ(textureType, internalFormat,GL4.GL_VIRTUAL_PAGE_SIZE_X_ARB, 1,pagesizes, 0);
		gl2.glGetInternalformativ(textureType, internalFormat,GL4.GL_VIRTUAL_PAGE_SIZE_Y_ARB, 1,pagesizes, 1);
		gl2.glGetInternalformativ(textureType, internalFormat,GL4.GL_VIRTUAL_PAGE_SIZE_Z_ARB, 1,pagesizes, 2);
		GLErrorHandler.assertGL(gl2);
		switch (virtualDimensions.length) {
			case 1:
				gl2.glTexStorage1D(this.textureType, 1, this.internalFormat, virtualDimensions[0]);
				//release all commitments
				gl2.glTexPageCommitmentARB(this.textureType, 1, 0, 0, 0, virtualDimensions[0], 1, 1,false);
				//add commitments
				gl2.glTexPageCommitmentARB(this.textureType, 1, offsets[0], 0, 0, sizes[0], 1, 1,true);
				gl2.glTexSubImage1D(this.textureType, 0, offsets[0], sizes[0], this.pixelFormat, this.pixelDataType, data);
				break;
			case 2:
				gl2.glTexStorage2D(this.textureType, 1, this.internalFormat, virtualDimensions[0],virtualDimensions[2]);
				//release all commitments
				gl2.glTexPageCommitmentARB(this.textureType, 1, 0, 0, 0, virtualDimensions[0], virtualDimensions[1], 1,false);
				//add commitments
				gl2.glTexPageCommitmentARB(this.textureType, 1, offsets[0], offsets[1], 0, sizes[0], sizes[1], 1,true);
				gl2.glTexSubImage2D(this.textureType, 0, offsets[0],offsets[1], sizes[0],sizes[1], this.pixelFormat, this.pixelDataType, data);
				break;
			case 3:
				GLErrorHandler.assertGL(gl2);
				if(!sparseMemoryAllocated){
					gl2.glTexStorage3D(this.textureType, 1, this.internalFormat, virtualDimensions[0],virtualDimensions[1],virtualDimensions[2]);
					sparseMemoryAllocated = true;
				}
				GLErrorHandler.assertGL(gl2);
				//release all commitments
				gl2.glTexPageCommitmentARB(this.textureType, 0, 0, 0, 0, virtualDimensions[0], virtualDimensions[1], virtualDimensions[2],false);
				//add commitments
				GLErrorHandler.assertGL(gl2);

				int [] tmpSize= new int[3];
				for(int i =0; i < virtualDimensions.length; i++){
					tmpSize[i] = pagesizes[i]*(int)Math.ceil((float)sizes[i]/(float)pagesizes[i]);
				}
				
				int [] tmpOffset= new int[3];
				for(int i =0; i < virtualDimensions.length; i++){
					tmpOffset[i] = pagesizes[i]*(int)Math.floor((float)offsets[i]/(float)pagesizes[i]);
				}
				gl2.glTexPageCommitmentARB(this.textureType, 0, tmpOffset[0], tmpOffset[1], tmpOffset[2], tmpSize[0], tmpSize[1], tmpSize[2],true);
				GLErrorHandler.assertGL(gl2);
				IntBuffer clearBuffer = Buffers.newDirectIntBuffer(tmpSize[0]*tmpSize[1]*tmpSize[2]);
				for(int t= 0; t< clearBuffer.capacity(); t++){
					clearBuffer.put(t, -1);
				}
				
				clearBuffer.rewind();
				gl2.glTexSubImage3D(this.textureType, 0, tmpOffset[0], tmpOffset[1], tmpOffset[2], tmpSize[0],tmpSize[1],tmpSize[2], this.pixelFormat, this.pixelDataType, clearBuffer);
				gl2.glTexSubImage3D(this.textureType, 0, offsets[0],offsets[1],offsets[2], sizes[0],sizes[1],sizes[2], this.pixelFormat, this.pixelDataType, data);
				GLErrorHandler.assertGL(gl2);
				break;
			default:
			break;
		}
	}
	
	/**
	 * Updates the data for the texture
	 * @param gl2 the gl context to use
	 * @param mipmapLevel the mipmap level to set
	 * @param data the data buffer to set
	 * @param dimensions the dimensions of the data buffer xyz
	 */
	public void update(GL4 gl2,int mipmapLevel, Buffer data, int[] dimensions){
		//activate context
		gl2.glActiveTexture(textureUnit);
		GLErrorHandler.assertGL(gl2);

		rebindTexture(gl2);
		GLErrorHandler.assertGL(gl2);
		if(sparseMemoryAllocated){
			delete(gl2);
			genTexture(gl2);
			setTexParameteri(GL4.GL_TEXTURE_SPARSE_ARB, GL2.GL_FALSE);
	
			gl2.glActiveTexture(textureUnit);
			rebindTexture(gl2);		
		}
		updateTextureParameters(gl2);
		GLErrorHandler.assertGL(gl2);

		switch (dimensions.length) {
		case 1:

			gl2.glTexImage1D(textureType, 
					mipmapLevel, 
					internalFormat, 
					dimensions[0], 
					0,
					pixelFormat, 
					pixelDataType, 
					data);
			break;
	
		case 2:
			gl2.glTexImage2D(textureType, 
					mipmapLevel, 
					internalFormat, 
					dimensions[0],dimensions[1], 
					0,
					pixelFormat, 
					pixelDataType, 
					data);
			break;

		case 3:
			gl2.glTexImage3D(textureType, 
					mipmapLevel, 
					internalFormat, 
					dimensions[0],dimensions[1],dimensions[2], 
					0,
					pixelFormat, 
					pixelDataType, 
					data);
			break;
		default:
			break;
		}
		if(shouldGenerateMidmaps){
			gl2.glGenerateMipmap(textureType);
		}
		GLErrorHandler.assertGL(gl2);
	}
	
	/**
	 * Stores texture parameters with glTexParameteri
	 * @param parameter the parameter id 
	 * @param value the value
	 */
	public void setTexParameteri(int parameter, int value){
		parameteriMap.put(parameter, value);
		updatableTextureParameters.put(parameter, true);
	}

	/**
	 * Stores texture parameters with glTexParameteriv
	 * @param parameter the parameter id 
	 * @param values the value field
	 */
	public void setTexParameteriv(int parameter, int[] values){
		parameterivMap.put(parameter, values.clone());
		updatableTextureParameters.put(parameter, true);
	}
	
	/**
	 * Stores texture parameters with glTexParameterfv
	 * @param parameter the parameter id
	 * @param values the value field to set
	 */
	public void setTexParameterfv(int parameter, float[] values){
		parameterfvMap.put(parameter, values.clone());
		updatableTextureParameters.put(parameter, true);
	}
	
	/**
	 * central update for parameters
	 * @param gl the gl context to use
	 */
	public void updateTextureParameters(GL4 gl){
		if(updatableTextureParameters.isEmpty()){
			return;
		}
		
		for(Integer parameter :updatableTextureParameters.keySet()){
			
			//int parameters
			if(parameteriMap.containsKey(parameter)){
				gl.glTexParameteri(textureType, parameter, parameteriMap.get(parameter));
			}
			
			//intv parameters
			if(parameterivMap.containsKey(parameter)){
				gl.glTexParameteriv(textureType, parameter, parameterivMap.get(parameter),0);
			}
			
			//intv parameters
			if(parameterfvMap.containsKey(parameter)){
				gl.glTexParameterfv(textureType, parameter, parameterfvMap.get(parameter),0);
			}
		}
		updatableTextureParameters.clear();
	}
	
	/**
	 * Clears the current texture context of the object
	 * @param gl2 the gl context to use
	 */
	public void delete(GL4 gl2){
		
		
		int textBuffer[] = {textureObject};
		gl2.glDeleteTextures(1, textBuffer, 0);
		
		usedTextureUnits.remove(textureUnit);
		
		setParametersNeedsUpdate(parameteriMap.keySet());
		setParametersNeedsUpdate(parameterivMap.keySet());
		setParametersNeedsUpdate(parameterfvMap.keySet());
		sparseMemoryAllocated = false;
	}
	
	/**
	 * Enables the update flag for all stored texture parameters
	 * @param parameters the parameter ids to set updateable
	 */
	private void setParametersNeedsUpdate(Set<Integer> parameters){
		for(Integer parameter: parameters){
			updatableTextureParameters.put(parameter, true);
		}
	}

	/**
	 * @return the shouldGenerateMidmaps
	 */
	public boolean isShouldGenerateMidmaps() {
		return shouldGenerateMidmaps;
	}

	/**
	 * @param shouldGenerateMidmaps the shouldGenerateMidmaps to set
	 */
	public void setShouldGenerateMidmaps(boolean shouldGenerateMidmaps) {
		this.shouldGenerateMidmaps = shouldGenerateMidmaps;
	}
}
