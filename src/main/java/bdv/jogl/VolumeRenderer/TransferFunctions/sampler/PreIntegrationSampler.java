package bdv.jogl.VolumeRenderer.TransferFunctions.sampler;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.TreeMap;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;

import bdv.jogl.VolumeRenderer.Scene.Texture;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.transferfunctioninterpreter.PreIntegrationInterpreter;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.suvColorTexture;
import static bdv.jogl.VolumeRenderer.utils.WindowUtils.getNormalizedColor;

public class PreIntegrationSampler implements ITransferFunctionSampler {

	private final PreIntegrationInterpreter desampler = new PreIntegrationInterpreter();
	private Texture colorTexture;

	@Override
	public IFunction getShaderCode() {
		return desampler;
	}

	/**
	 * Returns the integral of a product linear absorbing and color funktion
	 * @param c1 color at begin 
	 * @param c2 color at end 
	 * @param a1 absorbation at begin
	 * @param a2 absorbation at end
	 * @param stepSize slap size
	 * @return
	 */
	private float calcColorChannelIntegral(float c1, float c2, float a1, float a2, float stepSize){
		return stepSize/3.f *(c1*a1+(c1*a2+c2*a1)/2.f +c2*a2);
	}
	
	/**
	 * Returns the integral of a linear absorbation function in its ports
	 * @param a1 absorbation at begin
	 * @param a2 absorbation at end
	 * @param stepSize slap size
	 * @return
	 */
	private float calcAbsorbationIntegral(float a1, float a2, float stepSize){
		return stepSize/2.f * (a1+a2);
	}
	
	@Override
	public void init(GL4 gl, int colorTextureId) {
		colorTexture = new Texture(GL2.GL_TEXTURE_2D,colorTextureId,GL2.GL_RGBA,GL2.GL_RGBA,GL2.GL_FLOAT);
		colorTexture.genTexture(gl);
		colorTexture.setTexParameteri(gl,GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
		colorTexture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
	}
	
	@Override
	public void updateData(GL4 gl, TransferFunction1D transferFunction,
			float sampleStep) {
		FloatBuffer buffer = sample(transferFunction, sampleStep);
		int dim[] = new int[]{(int)Math.round(Math.sqrt(buffer.capacity()/4)),(int)Math.round(Math.sqrt(buffer.capacity()/4))};
		colorTexture.update(gl, 0, buffer,dim );
		
	}
	
	@Override
	public FloatBuffer sample( TransferFunction1D transferFunction, float stepSize) {
		//http://www.uni-koblenz.de/~cg/Studienarbeiten/SA_MariusErdt.pdf
		
		int sampleStep=1;
		TreeMap<Integer, Color> colorMap = transferFunction.getTexturColor();


		
		//make samples
		Integer intervalBegin = colorMap.firstKey();
		float[] integral = {0,0,0,0};
		ArrayList <float[]> integalsOfS = new ArrayList<float[]>();
		ArrayList <float[]> colors = new ArrayList<float[]>();

		//iterate candidates
		for(Integer intervalEnd: colorMap.keySet()){
			if(intervalEnd == intervalBegin){
				continue;
			}

			float[] intervalBeginColor = getNormalizedColor(colorMap.get(intervalBegin));
			float[] intervalEndColor = getNormalizedColor(colorMap.get(intervalEnd));
			float[] intervalColorGradient = {0,0,0,0};
			
			//forward difference
			for(int dim = 0; dim < intervalColorGradient.length; dim++){
				intervalColorGradient[dim] = (intervalEndColor[dim]-intervalBeginColor[dim])/((float)(intervalEnd-intervalBegin));
			}
			
			//get upper border of the integral
			float[] sampleColor = intervalBeginColor.clone();
			float[] formerSampleColor = intervalBeginColor.clone();
		
			//sample linear
			for(int step = intervalBegin; step < intervalEnd; step++ ){
				colors.add(sampleColor);
				//increment color
				for(int i =0; i< sampleColor.length; i++){
					sampleColor[i]+= intervalColorGradient[i];
				}
				
				
				//absorbation integral
				integral[3] += calcAbsorbationIntegral(formerSampleColor[3], sampleColor[3], 1);
				
				//color integral
				for(int i = 0; i< 3; i++){
					integral[i] +=calcColorChannelIntegral(formerSampleColor[i], sampleColor[i], 
							formerSampleColor[3], sampleColor[3], 1);
				}
				integalsOfS.add(integral.clone());
				formerSampleColor = sampleColor.clone();
			}
			intervalBegin = intervalEnd;
		}
		
		//get Buffer last key is the highest number 
		FloatBuffer buffer = Buffers.newDirectFloatBuffer((int)Math.pow(integalsOfS.size(), 2)*4);
		//classification
		for(int sb = 0; sb < integalsOfS.size(); sb++){
			for(int sf = 0; sf < integalsOfS.size(); sf++){
				float rgba[] = {0,0,0,0};
				if(sf!=sb){
					float integralFront[] = integalsOfS.get(sf);
					float integralBack[] = integalsOfS.get(sb);
					for(int i =0; i< rgba.length; i++){
						rgba[i] = stepSize/(float)(sb - sf) * (integralBack[i] - integralFront[i]);
					}
				}else{
					//rgba = colors.get(sf).clone();
					for(int i =0; i< rgba.length; i++){
						rgba[i] *=stepSize;
					}
					
				}
				rgba[3] = (float) (1.f - Math.exp(-rgba[3]));
				buffer.put(rgba.clone());
			}
		}
		
		buffer.rewind();
		return buffer;
		
	}
	/*"	if(vbegin - vend < minValue){",
	"		if(vbegin - minValueHalf < 0.0){",
	"			vend+= minValue*10.0;",	
	"		}else{",	
	"			if(vend +minValueHalf > 1.0){",
	"				vbegin-=minValue*10.0;",
	"			}else{",			
	"				vend+=minValueHalf*10.0;",
	"				vbegin-=minValueHalf*10.0;",
	"			}",
	"		}",	
	"	}",
	"	vec4 iFront = texture("+suvColorTexture+",vbegin*texnorm + texoffset);",
	"	vec4 iBack = texture("+suvColorTexture+",vend*texnorm + texoffset);",
	"	vec4 color = vec4(0.0);",				
	"	vec4 iDiff = distance/(vend - vbegin) * (iBack - iFront);",
	"",
	"	//alpha desample",
	"	color.a = 1.0 - exp(-iDiff.a);",
	"",
	"	//rgb desample",
	"	color.rgb = iDiff.rgb;",
	"	return color;",
	"}",*/

	@Override
	public void dispose(GL4 gl) {
		colorTexture.delete(gl);
		
	}
}
