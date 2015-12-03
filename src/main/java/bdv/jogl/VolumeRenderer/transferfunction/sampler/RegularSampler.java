package bdv.jogl.VolumeRenderer.transferfunction.sampler;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.TreeMap;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;

import bdv.jogl.VolumeRenderer.scene.Texture;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.IFunction;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.transferfunctioninterpreter.RegularTransferFunctionInterpreter;
import bdv.jogl.VolumeRenderer.transferfunction.TransferFunction1D;
import static bdv.jogl.VolumeRenderer.utils.WindowUtils.getNormalizedColor;
/**
 * Samples transfer function texture regularly 
 * @author michael
 *
 */
public class RegularSampler implements ITransferFunctionSampler {
	
	private final RegularTransferFunctionInterpreter desampler = new RegularTransferFunctionInterpreter();
	
	private Texture colorTexture;  
	
	/**
	 * Initialize the 1D transfer function texture
	 */
	@Override
	public void init(GL4 gl, int colorTextureId) {
		colorTexture = new Texture(GL2.GL_TEXTURE_1D,colorTextureId,GL2.GL_RGBA,GL2.GL_RGBA,GL2.GL_FLOAT);
		colorTexture.genTexture(gl);
		colorTexture.setTexParameteri(GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
	}
	
	/**
	 * Calculates the transfer function texture and uploads it to the gpu
	 */
	@Override
	public void updateData(GL4 gl, TransferFunction1D transferFunction,
			float sampleStep) {
		FloatBuffer buffer = sample( transferFunction, sampleStep);
		colorTexture.update(gl, 0, buffer, new int[]{buffer.capacity()/4});
	}
	
	/**
	 * Samples tf data and returns 1d texture
	 * @param transferFunction
	 * @param sampleStep
	 * @return
	 */
	public FloatBuffer sample(TransferFunction1D transferFunction, float sampleStep){
		TreeMap<Integer, Color> colorMap = transferFunction.sampleColors();
		//get Buffer last key is the highest number 
		FloatBuffer buffer = Buffers.newDirectFloatBuffer(((colorMap.lastKey()-colorMap.firstKey())+1)*4);

		//make samples
		Integer latestMapIndex = colorMap.firstKey();
		//iterate candidates
		for(Integer currentMapIndex: colorMap.keySet()){
			if(currentMapIndex == colorMap.firstKey()){
				continue;
			}

			float[] startColor = getNormalizedColor(colorMap.get(latestMapIndex));
			float[] finalColor = getNormalizedColor(colorMap.get(currentMapIndex));
			float[] currentColor = startColor.clone();
			float[] colorGradient = {0,0,0,0};
			
			//forward difference
			for(int dim = 0; dim < colorGradient.length; dim++){
				colorGradient[dim] = (finalColor[dim]-startColor[dim])/((float)(currentMapIndex-latestMapIndex));
			}

			//sample linear
			for(Integer step = latestMapIndex; step < currentMapIndex; step++ ){
				buffer.put(currentColor.clone());
				
				//add to buffer and increment
				for(int dim = 0; dim < colorGradient.length; dim++){	
					currentColor[dim] += colorGradient[dim];
					currentColor[dim] = Math.min(1.f, Math.max(0.f, currentColor[dim]));
				}
			}		
			//add latest color
			if(currentMapIndex == colorMap.lastKey()){
				buffer.put(finalColor.clone());
			}
			latestMapIndex = currentMapIndex;
		}

		buffer.rewind();
		return buffer;

	};
	
	/**
	 * Retruns the de sampling shader code
	 * @return
	 */
	public IFunction getShaderCode(){
		return desampler;
	}

	/**
	 * Deletes the 1D texture
	 */
	@Override
	public void dispose(GL4 gl) {
		colorTexture.delete(gl);
	}
}
