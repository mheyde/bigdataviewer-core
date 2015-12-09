package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator;

import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates the max difference of the midmaps
 * @author michael
 *
 */
public class MidmapMaxDifferenceAccumulator extends AbstractVolumeAccumulator {

	/**
	 * constructor
	 */
	public MidmapMaxDifferenceAccumulator(){
		super("difference_of_midmap_area");
	}
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 57",
				"float["+scvMaxNumberOfVolumes+"] getMidmapValues(vec3 position){",
				"	float ret["+scvMaxNumberOfVolumes+"];",
				"	for(int i = 0; i < "+scvMaxNumberOfVolumes+";i++){",
				"		vec3 textureCoord = getCorrectedTexturePositions(position,i);",
				"		float midmapValue = textureLod( "+suvVolumeTexture+"[i],textureCoord,1.0).r;",
				"		ret[i] = midmapValue;",
				"	}",
				"	return ret;",
				"}",
				"",
				"//main accumulator function",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]){",
				"	float maxValue = 0.0;",
				"	float minValue = "+Float.MAX_VALUE+";",
				"	float midMap["+scvMaxNumberOfVolumes+"] = getMidmapValues("+sgvRayPositions+");",	
				"	int numberOfChanges = 0;",
				"",
				"	//iterate densities",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"",
				"		//scip on invalid",
				"		if(densities[n] < 0.0){",
				"			continue;",	
				"		}",	
				"		maxValue = max(maxValue,midMap[n]);",
				"		minValue = min(minValue,midMap[n]);",	
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
