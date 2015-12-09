package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator;

import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

/**
 * selection accumulator using the minimal volume value 
 * @author michael
 *
 */
public class MinimumVolumeAccumulator extends AbstractVolumeAccumulator {

	/**
	 * constructor
	 */
	public MinimumVolumeAccumulator(){
		super("minimum");
	}

	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 9",
				"",
				"//main accumulator function",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = "+Float.MAX_VALUE+";",		
				"	int numberOfChanges=  0;",
				"",
				"	//iterate densities",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"",
				"		//scip on invalid",
				"		if(densities[n] < 0){",
				"			continue;",	
				"		}",	
				"		density = min(density, densities[n]);",
				"		numberOfChanges++;",
				"	}",
				"",
				"	//no valid values zero returns",
				"	if(numberOfChanges ==0){",
				"		return 0.0;",
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
