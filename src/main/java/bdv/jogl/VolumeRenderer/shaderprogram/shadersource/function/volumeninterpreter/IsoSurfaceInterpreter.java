package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.volumeninterpreter;
import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.VolumeGradientEvaluationFunction;

/**
 * Volume interpreter class for iso surfaces
 * @author michael
 *
 */
public class IsoSurfaceInterpreter extends AbstractVolumeInterpreter {

	private VolumeGradientEvaluationFunction gradEval = new VolumeGradientEvaluationFunction();
	
	/**
	 * constructor
	 */
	public IsoSurfaceInterpreter() {
		super("isoSurfaceInterpreter");

	}

	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		addCodeArrayToList( new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 101",
				"",
				"//color of the light source connected to the camera position",
				"uniform vec3 "+suvLightIntensiy+";",
				"",
				"//bisection form http://onlinelibrary.wiley.com/doi/10.1111/j.1467-8659.2005.00855.x/abstract",
				"vec3 bisection(float fNear, float fFar, vec3 xNear, vec3 xFar, float isoValue){",
				"	vec3 xNew = (xFar - xNear) * (isoValue - fNear)/(fFar - fNear) + xNear;",
				"	return xNew;",	
				"}",
				"",
				"//refinement of the intersection",
				"vec4[2] refineIntersection(float fNear, float fFar, vec3 xNear, vec3 xFar, float isoValue){",
				"	vec4[2] refined;",
				"",
				"	//get the ascending or descending of the volume values ",
				"  //for later correct assignment of the corrected values",
				"	bool nearInlowerIso = (fNear < isoValue);",
				"",
				"	//run 4 iteration as mentioned in the paper to get a good intersection. ",
				"	//Surpress unrolling to fit to gpu instruction memory",
				"#pragma optionNV(unroll none)",
				"	for(int i =0; i < 4; i++){",
				"",
				"		//refine via bisection",
				"		vec3 xNew = bisection(fNear,fFar,xNear,xFar,isoValue);",
				"		float fNew = getValue(xNew);",
				"",
				"		//assignement of corrected values",
				"		if(fNew > isoValue ){",
				"			if(nearInlowerIso){",
				"				xFar = xNew;",
				"				fFar = fNew;",
				"			}else{",
				"				xNear = xNew;",
				"				fNear = fNew;",
				"			}",
				"		}else{",
				"			if(nearInlowerIso){",
				"				xNear = xNew;",
				"				fNear = fNew;",
				"			}else{",
				"				xFar = xNew;",
				"				fFar = fNew;",
				"			}",
				"		}",	
				"	}",
				"",
				"	//prepare returning of corrected ray positions and volume values",
				"	refined[0].xyz = xNear;",
				"	refined[0].w = fNear;",
				"	refined[1].xyz = xFar;",
				"	refined[1].w = fFar;",
				"	return refined;",
				"}",
				"",
				"//variables for blinn phong model",
				"const vec3 inconstants = vec3(0.1,0.4,0.5);",
				"const vec3 ambientColor = vec3(0.7);",
				"",
				"//blinn phong color evaluation",
				"vec3 blinnPhongShading(vec3 constants, ",
				"						vec3 iAmbient,",
				"						vec3 normal, ",
				"						vec3 lookVector, ",
				"						vec3 lightDirs,",
				"						vec3 iIn){",
				"	vec3 halfVec = (lightDirs+lookVector)/length(lightDirs+lookVector);",
				"	float ln = dot(lightDirs, normal);",
				"	float v =max (sign(ln),0.0);",
				"	vec3 iOut = constants.x * iAmbient;//ambient",
				"	iOut += iIn*(constants.y*max(ln,0.0));//diffuse",
				"	iOut += v*iIn*(constants.z*max(pow( dot(halfVec,normal),10.0),0.0));//specular",
				"	",
				"	return iOut;",
				"}",
				"",		
				"//main interpreter function",
				"vec4 "+getFunctionName()+"(vec4 c_in, vec4 c, float vm1, float v){",
				"	int n = 0;",
				"",
				"	//test for possible intersections",
				"	if(vm1 -"+scvMinDelta+" <= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" <= v +"+scvMinDelta+"  ||",
				"		    vm1 + "+scvMinDelta+" >= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" >= v -"+scvMinDelta+"){",
				"		vec4 color = vec4(0.0,0.0,0.0,1.0);",
				"		vec4 refinedVal;",
				"",
				"		vec4 xNear = vec4(vec3("+sgvRayPositions+" - "+sgvRayDirections+" * "+suvRenderRectStepSize+").xyz,1.0);",
				"		vec4 xFar = vec4("+sgvRayPositions+".xyz,1.0);",
				"",
				"		//refine intersection",
				"		vec4 refined[2]= refineIntersection(vm1,v,xNear.xyz,xFar.xyz,"+sgvNormIsoValue+");",
				"",
				"		//get the nearest refind value and position",
				"		if(distance("+sgvNormIsoValue+",refined[1].a) < distance("+sgvNormIsoValue+",refined[0].a)){",
				"			refinedVal = refined[1];",
				"		}else{",
				"			refinedVal = refined[0];",
				"		}",
				"",
				"		//evaluate the volume gradient for use as normal direction",
				"		vec4 gradient = "+gradEval.call(new String[]{"refinedVal.xyz"})+";",
				"",
				"		//evaluate the blinn phong color",
				"		color.rgb = blinnPhongShading(	inconstants,",
				"										ambientColor,",
				"										-normalize(gradient.xyz),",
				"										-1.0 * "+sgvRayDirections+",",
				"										normalize( "+suvEyePosition+" - "+sgvRayPositions+"),",
				"										"+suvLightIntensiy+");",			
				"		c.rgb = color.rgb;",
				"		c.a = 1.0;",
				"	}",
				"",
				"	//front to back compositing",
				"	return c_in + (1.0 - c_in.a)*c;",
				"}"
		},code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}

}
