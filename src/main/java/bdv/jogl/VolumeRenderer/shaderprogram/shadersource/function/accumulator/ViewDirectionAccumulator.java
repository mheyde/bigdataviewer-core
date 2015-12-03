package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator;

import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

public class ViewDirectionAccumulator extends AbstractVolumeAccumulator {

	
	
	public ViewDirectionAccumulator() {
		super("view_direction_weight");
	}

	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 33",
				"float["+scvMaxNumberOfVolumes+"] calcWeights(vec3 rayDirections){",
				"	float weights["+scvMaxNumberOfVolumes+"];",
				"	for(int v=0; v < "+scvMaxNumberOfVolumes+"; v++){",
				"		vec4 rayDirInVolumeSpace = vec4(rayDirections,0.0);",
				"		rayDirInVolumeSpace = "+suvTextureTransformationInverse+"[v] * rayDirInVolumeSpace;",
				"		weights[v] = abs(dot(rayDirInVolumeSpace.xyz,vec3(0,0,1)));",
				"	}",
				"	return weights;",
				"}",
				"",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = 0.0;",		
				"	float sum = 0.0;",
				"	const int N = "+scvMaxNumberOfVolumes+";",
				"	float weights[N] = calcWeights("+sgvRayDirections+");",
				"	for(int n = 0; n< N; n++){",
				"		if(densities[n] < 0.0){",
				"			continue;",	
				"		}",	
				"		sum+=weights[n];",
				"	}",	
				"	for(int n = 0; n < N; n++){",
				"		if(densities[n] < 0.0){",
				"			continue;",	
				"		}",	
				"		float weight = (weights[n]/sum);",
				"		density += weight * densities[n];",
				"	}",
				"	return density;",	
				"}"
		};
	
		addCodeArrayToList(dec, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}
}
