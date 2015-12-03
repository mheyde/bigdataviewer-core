package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.transferfunctioninterpreter;

import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.*;
/**
 * Desamples a regular sampled transfer function
 * @author michael
 *
 */
public class RegularTransferFunctionInterpreter extends AbstractTransferFunctionInterpreter {
	@Override
	public String[] declaration() {
		String dec[] ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() +" 11",
				"",
				"uniform sampler1D "+suvColorTexture+";",
				"const float maxDistance = 0.75;",
				"float texoffset = 1.0/(2.0*"+suvMaxVolumeValue+");",
				"float texnorm = ("+suvMaxVolumeValue+"-1.0)/"+suvMaxVolumeValue+";",
				"",
				"vec4 "+getFunctionName()+"(float vbegin, float vend, float distance){",
				"	vec4 color = texture("+suvColorTexture+",vend*texnorm+texoffset);",
				"	float tau = color.a;",
				"	float alpha = 1.0 - exp(-tau*distance);",
				"	color.rgb*= tau*distance;",
				"	color.a = alpha;",
				"	return color;",
				"}",
				""
		};
		return dec;
	}
}
