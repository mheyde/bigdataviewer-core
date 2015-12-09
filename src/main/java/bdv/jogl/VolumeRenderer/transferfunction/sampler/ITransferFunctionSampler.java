package bdv.jogl.VolumeRenderer.transferfunction.sampler;

import java.nio.FloatBuffer;

import com.jogamp.opengl.GL4;

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.IFunction;
import bdv.jogl.VolumeRenderer.transferfunction.TransferFunction1D;

/**
 * Interface for a Sampler of Transfer functions that creates the texture and defines the desampling
 * @author michael
 *
 */
public interface ITransferFunctionSampler {
	
	/**
	 * Hook for initializing the textures
	 * @param gl the gl context to use
	 * @param colorTextureId transfer function texture id in the shader code
	 */
	public void init(GL4 gl, int colorTextureId);
	
	/**
	 * Hook for disposing the textures
	 * @param gl the gl context to use
	 */
	public void dispose(GL4 gl);

	/**
	 * Hook for updating the texture data
	 * @param gl the gl context to use
	 * @param transferFunction the transfer function to get the data from
	 * @param sampleStep the used sample step size
	 */
	public void updateData(GL4 gl,TransferFunction1D transferFunction, float sampleStep);
	
	/**
	 * Returns the transfer function classification shader code class
	 * @return the shader code class
	 */
	public IFunction getShaderCode();
	
	/**
	 * samples the texture
	 * @param transferFunction the transfer function to sample 
	 * @param sampleStep the step size to sample 
	 * @return the sampled data in texture format
	 */
	public FloatBuffer sample(TransferFunction1D transferFunction, float sampleStep);
}
