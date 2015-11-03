package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

public class MaxDifferenceAccumulator extends AbstractVolumeAccumulator {

	public MaxDifferenceAccumulator() {
		super("difference");
	}
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 7",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float maxValue = 0.0;",
				"	float minValue = "+Float.MAX_VALUE+";",
				"	int numberOfChanges = 0;",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		if(densities[n]< 0.0 ){",
				"			continue;",
				"		}",	
				"		maxValue = max(maxValue,densities[n]);",
				"		minValue = min(minValue,densities[n]);",
				"		numberOfChanges++;",
				"	}",
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
