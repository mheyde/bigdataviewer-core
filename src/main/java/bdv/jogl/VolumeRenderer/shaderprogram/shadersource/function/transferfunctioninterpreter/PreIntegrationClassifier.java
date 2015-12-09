package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.transferfunctioninterpreter;

import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.*;

/**
 * Pre-integration gpu classifier class
 * @author michael
 *
 */
public class PreIntegrationClassifier extends AbstractTransferFunctionClassifier {
	
	@Override
	public String[] declaration() {
		String dec[] ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 10",
				"",
				"//transfer function color texture",
				"uniform sampler2D "+suvColorTexture+";",
				"",
				"//volume value normalization terms",
				"float texoffset = 1.0/(2.0*"+suvMaxVolumeValue+");",
				"float texnorm = ("+suvMaxVolumeValue+"-1.0)/"+suvMaxVolumeValue+";",
				"",
				"//main classifier method",
				"vec4 "+getFunctionName()+"(float vbegin, float vend, float distance){",
				"",
				"	//only texture access need because of pre-integration",
				"	return texture("+suvColorTexture+",vec2(vbegin*texnorm + texoffset,vend*texnorm + texoffset)).rgba;",
				"}"
		};
		return dec;
	}

}
