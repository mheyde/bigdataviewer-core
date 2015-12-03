package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.volumeninterpreter;
import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.VolumeGradientEvaluationFunction;


public class IsoSurfaceInterpreter extends AbstractVolumeInterpreter {

	private VolumeGradientEvaluationFunction gradEval = new VolumeGradientEvaluationFunction();
	
	private final int refinementSteps = 4;
	
	public IsoSurfaceInterpreter() {
		super("isoSurfaceInterpreter");

	}

	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		//addCodeArrayToList(gradEval.declaration(),code);
		addCodeArrayToList( new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 101",
				"float isoPainted =0;",
				"uniform vec3 "+suvLightIntensiy+";",
				"uniform int refSteps = "+refinementSteps+";",
				"",
				"//bisection form http://onlinelibrary.wiley.com/doi/10.1111/j.1467-8659.2005.00855.x/abstract",
				"vec3 bisection(float fNear, float fFar, vec3 xNear, vec3 xFar, float isoValue){",
				"	vec3 xNew = (xFar - xNear) * (isoValue - fNear)/(fFar - fNear) + xNear;",
				"	return xNew;",	
				"}",
				"",
				"vec4[2] refineIntersection(float fNear, float fFar, vec3 xNear, vec3 xFar, float isoValue){",
				"	vec4[2] refined;",
				"	bool nearInlowerIso = (fNear < isoValue);",
				"	for(int i =0; i < refSteps; i++){",
				"		vec3 xNew = bisection(fNear,fFar,xNear,xFar,isoValue);",
				"		float fNew = getValue(xNew);",
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
				"	refined[0].xyz = xNear;",
				"	refined[0].w = fNear;",
				"	refined[1].xyz = xFar;",
				"	refined[1].w = fFar;",
				"	return refined;",
				"}",
				"",
				"const vec3 inconstants = vec3(0.1,0.4,0.5);",
				"const vec3 ambientColor = vec3(0.7);",
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
				"vec4 "+getFunctionName()+"(vec4 c_in, vec4 c, float vm1, float v){",
				"	int n = 0;",
				"	if(vm1 -"+scvMinDelta+" <= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" <= v +"+scvMinDelta+"  ||",
				"		    vm1 + "+scvMinDelta+" >= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" >= v -"+scvMinDelta+"){",
				"		vec4 color = vec4(0.0,0.0,0.0,1.0);",
				"		vec4 refinedVal;",
				"",
				"		vec4 xNear = vec4(vec3("+sgvRayPositions+" - "+sgvRayDirections+" * "+suvRenderRectStepSize+").xyz,1.0);",
				"		vec4 xFar = vec4("+sgvRayPositions+".xyz,1.0);",
				"",
				"		vec4 refined[2]= refineIntersection(vm1,v,xNear.xyz,xFar.xyz,"+sgvNormIsoValue+");",
				"",
				"		if(refined[0].a < 0.0){",
				"			color.a=-1.0;",	
				"		}",
				"		if(distance("+sgvNormIsoValue+",refined[1].a) < distance("+sgvNormIsoValue+",refined[0].a)){",
				"			refinedVal = refined[1];",
				"		}else{",
				"			refinedVal = refined[0];",
				"		}",
				"		vec4 gradient = "+gradEval.call(new String[]{"refinedVal.xyz"})+";",
				"		color.rgb = blinnPhongShading(	inconstants,",
				"										ambientColor/*c.rgb*/,",
				"										-normalize(gradient.xyz),",
				"										-1.0 * "+sgvRayDirections+",",
				"										normalize( "+suvEyePosition+" - "+sgvRayPositions+"),",
				"										"+suvLightIntensiy+");",
				"		color.a = gradient.w;",			
				"		c.rgb = color.rgb;",
				"		c.a = 1.0;",
				"	}",	
				"	return c_in + (1.0 - c_in.a)*c;",
				"}"
		},code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}

}
