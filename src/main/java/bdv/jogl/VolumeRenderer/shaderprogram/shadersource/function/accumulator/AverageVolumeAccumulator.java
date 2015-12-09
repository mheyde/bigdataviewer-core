package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator;

import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;

import java.util.ArrayList;
import java.util.List;

/**
 * Weighted accumulator using the average
 * @author michael
 *
 */
public class AverageVolumeAccumulator extends AbstractVolumeAccumulator {

	/**
	 * constructor
	 */
	public AverageVolumeAccumulator(){
		super("average");
	}
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 6",
				"",
				"//accumulator function for the average",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = 0.0;",
				"	int count =0;",
				"",
				"	//iterate densities",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"",
				"		//scip on invalid",
				"		if(densities[n]< 0.0){",
				"			continue;",
				"		}",
				"",
				"		//add to avg",
				"		density += densities[n];",
				"		count++;",
				"	}",
				"",
				"	//no valid values zero returns",
				"	if(count == 0 ){",
				"		return 0.0;",
				"	}",	
				"",
				"	//calc and return avg",
				"	density/=float(count);",
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
