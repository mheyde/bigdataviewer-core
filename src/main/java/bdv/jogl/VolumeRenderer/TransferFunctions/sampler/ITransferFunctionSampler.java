package bdv.jogl.VolumeRenderer.TransferFunctions.sampler;

import java.nio.FloatBuffer;

import com.jogamp.opengl.GL4;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IFunction;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;

/**
 * Interface for a Sampler of Transfer functions that creates the texture and defines the desampling
 * @author michael
 *
 */
public interface ITransferFunctionSampler {
	
	/**
	 * Hook for initializing the textures
	 * @param gl
	 * @param colorTextureId
	 */
	public void init(GL4 gl, int colorTextureId);
	
	/**
	 * Hook for disposing the textures
	 * @param gl
	 */
	public void dispose(GL4 gl);

	/**
	 * Hook for updating the texture data
	 * @param gl
	 * @param transferFunction
	 * @param sampleStep
	 */
	public void updateData(GL4 gl,TransferFunction1D transferFunction, float sampleStep);
	
	/**
	 * Retruns the desampling shader code
	 * @return
	 */
	public IFunction getShaderCode();
	
	/**
	 * samples the texture
	 * @param transferFunction
	 * @param sampleStep
	 * @return
	 */
	public FloatBuffer sample(TransferFunction1D transferFunction, float sampleStep);
}
