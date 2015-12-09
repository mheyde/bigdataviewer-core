package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator;

import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulator using the max difference of the volume values at a certain point
 * @author michael
 *
 */
public class MaxDifferenceAccumulator extends AbstractVolumeAccumulator {
	
	/**
	 * constructor
	 */
	public MaxDifferenceAccumulator() {
		super("difference");
	}
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 7",
				"",
				"//main accumulator function",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float maxValue = 0.0;",
				"	float minValue = "+Float.MAX_VALUE+";",
				"	int numberOfChanges = 0;",
				"",
				"	//iterate densities",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"",
				"		//scip on invalid",
				"		if(densities[n]< 0.0 ){",
				"			continue;",
				"		}",	
				"		maxValue = max(maxValue,densities[n]);",
				"		minValue = min(minValue,densities[n]);",
				"		numberOfChanges++;",
				"	}",
				"",
				"	//no valid values zero returns",
				"	if(numberOfChanges ==0){",
				"		return 0.0;",
				"	}",
				"	return maxValue - minValue;",	
				"}"
		};
		addCodeArrayToList(dec, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}

}
