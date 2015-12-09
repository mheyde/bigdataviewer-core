package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.transferfunctionclassifier;

import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.*;
/**
 * Klassifies  a regular sampled transfer function
 * @author michael
 *
 */
public class RegularTransferFunctionClassifier extends AbstractTransferFunctionClassifier {
	@Override
	public String[] declaration() {
		String dec[] ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() +" 11",
				"",
				"//transfer function color texture",
				"uniform sampler1D "+suvColorTexture+";",
				"",
				"//volume value normalization terms",
				"float texoffset = 1.0/(2.0*"+suvMaxVolumeValue+");",
				"float texnorm = ("+suvMaxVolumeValue+"-1.0)/"+suvMaxVolumeValue+";",
				"",
				"//main classifier method",
				"vec4 "+getFunctionName()+"(float vbegin, float vend, float distance){",
				"",
				"	//get tau and color",
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
