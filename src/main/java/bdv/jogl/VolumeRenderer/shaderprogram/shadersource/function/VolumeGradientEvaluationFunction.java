package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function;
/**
 * Shader function class for gpu based gradient evaluation
 * @author michael
 *
 */
public class VolumeGradientEvaluationFunction extends AbstractShaderFunction {

	/**
	 * Constructor
	 */
	public VolumeGradientEvaluationFunction() {
		super("gradient");
	}

	@Override
	public String[] declaration() {
		return new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 5",
				"",
				"//calculate the gradient value at a globale position",
				"vec4 "+getFunctionName()+"(vec3 globalPosition ){",
				"	const float offset = 0.1;",
				"",
				"	//get center value",
				"	float center = getNormalizedAndAggregatedVolumeValue(globalPosition);",
				"",
				"	//get the values of the + offset positions",
				"	vec3 plus = vec3(	getNormalizedAndAggregatedVolumeValue(globalPosition+vec3(offset,0.0,0.0)),",
				"						getNormalizedAndAggregatedVolumeValue(globalPosition+vec3(0.0,offset,0.0)),",
				"						getNormalizedAndAggregatedVolumeValue(globalPosition+vec3(0.0,0.0,offset)));",
				"",
				"	//get the values of the - offset positions",
				"	vec3 minus = vec3(	getNormalizedAndAggregatedVolumeValue(globalPosition+vec3(-offset,0.0,0.0)),",
				"						getNormalizedAndAggregatedVolumeValue(globalPosition+vec3(0.0,-offset,0.0)),",
				"						getNormalizedAndAggregatedVolumeValue(globalPosition+vec3(0.0,0.0,-offset)));",
				"	vec4 gradient = vec4(0.0);",
				"	vec3 factor = vec3(0.5);",
				"",
				"	//run through dimension and evaluate an error measurement in the w component",
				"	for(int d =0; d < 3; d++){",
				"",
				"		//invalid values",
				"		if(plus[d] < 0.0){",
				"			plus[d] = center;",
				"			factor[d] = 1.0;",
				"			gradient.w+=0.4;",
				"		}",
				"		if(minus[d] < 0.0){",
				"			minus[d] = center;",
				"			factor[d] = 1.0;",
				"			gradient.w+=0.4;",
				"		}",
				"	}",
				"",
				"",
				"	//evaluate gradient by component central differences",
				"   gradient.xyz = factor* (plus- minus)/offset;",
				"	return gradient;",
				"}",
		};
	}

}
