package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function;

/**
 * calculates the amount of steps of a certain size the ray
 * needs till it reaches a volume.
 * @author michael
 *
 */
public class GetStepsToVolumeFunction extends AbstractShaderFunction {

	public GetStepsToVolumeFunction(){
		super("getStepsToVolume");
	}

	@Override
	public String[] declaration() {
		String dec[] = {
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 3",
				"int "+getFunctionName()+"(float stepsSize, vec3 position, vec3 direction){",
				"	//infinite steps ;)",
				"	int steps = "+Integer.MAX_VALUE+";",
				"",	
				"	vec3 targetPoint =vec3(1.0) - max(sign(direction),vec3(0.0));",
				"	vec3 differenceVector = targetPoint - position;",
				"	vec3 stepsInDirections = min(differenceVector / (direction * stepsSize),vec3(0.0));",
				"	for(int i =0; i< 3; i++){",
				"		if(stepsInDirections[i] < steps){",
				"			steps = int(floor(stepsInDirections[i]));",
				"		}",
				"	}",
				"	return steps;",
				"}"
		};
		return dec;
	}



}
