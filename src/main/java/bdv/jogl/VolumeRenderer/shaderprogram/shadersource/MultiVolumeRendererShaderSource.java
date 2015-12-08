package bdv.jogl.VolumeRenderer.shaderprogram.shadersource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.*;
//import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.GetMaxStepsFunction;
//import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.GetStepsToVolumeFunction;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.IFunction;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.VolumeGradientEvaluationFunction;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.AbstractVolumeAccumulator;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator.AverageVolumeAccumulator;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.volumeninterpreter.AbstractVolumeInterpreter;
import bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.volumeninterpreter.EmissionAbsorbationInterpreter;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderCode;

/**
 * Class to handles the multi-volume shader codes
 * @author michael
 *
 */
public class MultiVolumeRendererShaderSource extends AbstractShaderSource{

	private int maxNumberOfVolumes = 2;

	//private final GetMaxStepsFunction stepsFunction = new GetMaxStepsFunction(); 

	//private final GetStepsToVolumeFunction stepsToVolume = new GetStepsToVolumeFunction();

	private IFunction transferFunctionCode;

	private AbstractVolumeAccumulator accumulator = new AverageVolumeAccumulator();

	private AbstractVolumeInterpreter interpreter; 

	private VolumeGradientEvaluationFunction gradient = new VolumeGradientEvaluationFunction();

	private static final String svRayStartCoordinate = "textureCoordinate";

	//Vertex shader uniforms
	public static final String suvDrawCubeTransformation ="inDrawCubeTransformation";

	public static final String suvTextureTransformationInverse ="inTextureTransformationInverse";

	//Fragment shader uniforms 
	public static final String suvIsoValue = "inIsoValue";

	public static final String suvActiveVolumes = "inActiveVolumes";

	public static final String suvVolumeTexture = "inVolumeTexture";

	public static final String suvColorTexture = "inColorTexture";

	public static final String suvEyePosition = "inEyePosition";

	public static final String suvMinVolumeValue = "inMinVolumeValue";

	public static final String suvMaxVolumeValue = "inMaxVolumeValue";

	public static final String scvMaxNumberOfVolumes = "maxNumberOfVolumes";

	public static final String sgvNormIsoValue = "normIsoValue";

	public static final String scvMinDelta = "minDelta";

	public static final String sgvRayPositions = "ray_poss";

	public static final String sgvRayDirections = "ray_dirs";

	public static final String sgvVolumeNormalizeFactor = "volumeNormalizeFactor";

	public static final String suvBackgroundColor = "inBackgroundColorFragmentShader";

	public static final String suvLightIntensiy = "iniIn";

	public static final String suvNormalSlice = "inZeroSliceNormal";

	public static final String suvShowSlice = "inShowSlice";

	public static final String suvVoxelCount = "inVoxelCount";

	public static final String suvSamples = "inSamples";

	public static final String sgvTexTOffsets = "vtextOffsets";

	public static final String sgvTexTScales = "vtextScales";

	public static final String suvUseGradient = "inUseGradient";

	public static final String suvRenderRectClippingPlanes = "inRectClippingPlanes";

	public static final String suvRenderRectStepSize = "inRectStepSize";

	public static final String suvTransferFuntionSize = "inTransferFunctionSize";

	public static final String suvOpacity3D = "inOpacity3D"; 

	public static final String suvVoxelOffsets = "inVoxelOffsets";

	/**
	 * constructor
	 */
	public MultiVolumeRendererShaderSource(){
		setVolumeInterpreter(  new EmissionAbsorbationInterpreter());
		setShaderLanguageVersion(330);
	}

	/**
	 * @return the maxNumberOfVolumes
	 */
	public int getMaxNumberOfVolumes() {
		return maxNumberOfVolumes;
	}


	/**
	 * @param maxNumberOfVolumes the maxNumberOfVolumes to set
	 */
	public void setMaxNumberOfVolumes(int maxNumberOfVolumes) {
		if(this.maxNumberOfVolumes == maxNumberOfVolumes){
			return;
		}
		this.maxNumberOfVolumes = maxNumberOfVolumes;
		notifySourceCodeChanged();
	}


	@Override
	public Set<ShaderCode> getShaderCodes() {
		Set<ShaderCode> codes = new HashSet<ShaderCode>();
		codes.add(new ShaderCode(GL2.GL_VERTEX_SHADER, 1, new String[][]{vertexShaderCode()}));
		codes.add(new ShaderCode(GL2.GL_FRAGMENT_SHADER, 1, new String[][]{fragmentShaderCode()}));
		return codes;
	}

	/**
	 * @param transferFunctionCode the transferFunctionCode to set
	 */
	public void setTransferFunctionCode(IFunction transferFunctionCode) {
		if(transferFunctionCode.equals(this.transferFunctionCode)){
			return;
		}
		this.transferFunctionCode = transferFunctionCode;
		notifySourceCodeChanged();
	}

	/**
	 * creates vertex shader code string
	 * @return
	 */
	private String[] vertexShaderCode() {
		String[] shaderCode ={
				"#version "+getShaderLanguageVersion(),
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber(),
				"",
				"const int "+scvMaxNumberOfVolumes+" = "+maxNumberOfVolumes+";",	
				"const float "+scvMinDelta+" = 0.00001;",
				"",
				"uniform mat4x4 "+suvViewMatrix+";",
				"uniform mat4x4 "+suvProjectionMatrix+";",
				"uniform mat4x4 "+suvModelMatrix+";",
				"uniform mat4x4 "+suvDrawCubeTransformation+";",
				"uniform mat4x4 "+suvTextureTransformationInverse+"["+scvMaxNumberOfVolumes+"];",
				"",
				"in vec3 "+satPosition+";",
				"out vec3 "+svRayStartCoordinate+";",
				"",
				"void main(){",
				"",
				"	vec4 position4d = vec4("+satPosition+".xyz,1.f);",
				"",
				"	//global hull volume coordiante",
				"	vec4 positionInGlobalSpace = "+suvDrawCubeTransformation+" * position4d;",
				"",
				"	//geometry coordinate",
				"	gl_Position ="+suvProjectionMatrix+" * "+suvViewMatrix+"  * positionInGlobalSpace;",
				"",
				"	//set ray start coordinate for later rasterization",
				"	"+svRayStartCoordinate+"= positionInGlobalSpace.xyz /max(positionInGlobalSpace.w,"+scvMinDelta+");",
				"}",

		};
		appendNewLines(shaderCode);
		return shaderCode;
	}

	private String[] fragmentShaderCode(){
		List<String> code = new ArrayList<String>();
		String[] head = {
				"#version "+getShaderLanguageVersion(),
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() +" 0",

				"const int "+scvMaxNumberOfVolumes+" = "+maxNumberOfVolumes+";",
				"const int maxInt = "+Integer.MAX_VALUE+";",
				"const float gamma = 20.0;",
				"const float "+scvMinDelta+" = 0.00001;",
				"",
				"uniform ivec3 "+suvVoxelCount+"["+scvMaxNumberOfVolumes+"];",
				"uniform int "+suvActiveVolumes+"["+scvMaxNumberOfVolumes+"];",
				"uniform float "+suvMaxVolumeValue+";",
				"uniform float "+suvMinVolumeValue+";",
				"uniform vec3 "+suvEyePosition+";",
				"uniform sampler3D "+suvVolumeTexture+"["+scvMaxNumberOfVolumes+"];",
				"uniform ivec3 "+suvVoxelOffsets+"["+scvMaxNumberOfVolumes+"];",
				"uniform float "+suvIsoValue+";",
				"uniform vec3 "+suvBackgroundColor+";",
				"uniform vec4 "+suvNormalSlice+";",
				"uniform int "+suvShowSlice+";",
				"uniform int "+suvSamples+";",
				"uniform int "+suvUseGradient+"=1;",
				"uniform vec4 "+suvRenderRectClippingPlanes+"[6];",
				"uniform float "+suvRenderRectStepSize+";",
				"uniform mat4x4 "+suvTextureTransformationInverse+"["+scvMaxNumberOfVolumes+"];",
				"uniform float "+suvOpacity3D+";",
				"float "+sgvNormIsoValue+";",
				"vec3 "+sgvRayDirections+";",	
				"vec3 "+sgvRayPositions+";",
				"float "+sgvVolumeNormalizeFactor+";",
				"float "+suvTransferFuntionSize+";",
				"",
				"in vec3 "+svRayStartCoordinate+";",
				"out vec4 fragmentColor;",
				"vec3 vtextOffsets["+scvMaxNumberOfVolumes+"]; ",
				"vec3 vtextScales["+scvMaxNumberOfVolumes+"]; ",
				"",
				"//retruns the corrected texture coordinate for surpressing texture border missselection",
				"vec3 correctTexturePositions(vec3 positionOnRay, vec3 offset, vec3 scale){",
				"	return positionOnRay * scale + offset;",
				"}",
				"",
				"//transforms a global space coordinate to a local volume coordinate",
				"vec3 getCoordinateInVolumeSpace(vec3 positionOnRay, int volumeNumber){",
				"	vec4 transformedPosition ="+suvTextureTransformationInverse+"[volumeNumber]*vec4(positionOnRay.xyz,1.0); ",
				"	transformedPosition /= max(transformedPosition.w,"+scvMinDelta+");",
				"	return transformedPosition.xyz;",
				"}",
				"",
				"//calculates the corrected texture coordinate from a partial volume for a given globale coordinate",
				"vec3 getCorrectedTexturePositions(vec3 positionOnRay, int volumeNumber){",
				"	vec3 transformedPosition =getCoordinateInVolumeSpace(positionOnRay,volumeNumber)-"+suvVoxelOffsets+"[volumeNumber]; ",
				"	return correctTexturePositions(transformedPosition,vtextOffsets[volumeNumber],vtextScales[volumeNumber]);",
				"}",
				"",
				"//calculates the distance between a point and a plane in hessian normal form",
				"float getPlaneDistance(vec4 plane, vec3 position){",
				"	float distance = -plane.a;",
				"	distance += dot(plane.xyz,position);",
				"	return distance;",
				"}",
				"",
				"//returns the maximal possible steps in the hull volume",
				"int getStepsTillClipp(){",
				"	int steps = "+suvSamples+";",
				"	//render cube clipping plane",
				"#pragma optionNV(unroll none)",
				"	for(int p = 0; p < 3; p++){",
				"		float dist = 0;",
				"		for(int i = 0; i < 2;i++){",
				"		vec4 plane = "+suvRenderRectClippingPlanes+"[p*2 + i];",
				"			if(sign("+sgvRayDirections+"[p]) == sign(plane[p])){",
				"				dist = max(dist, abs(getPlaneDistance(plane, "+sgvRayPositions+")));",
				"			}",
				"		}",
				"		if(!(dist > 0.0)){",
				"			continue;",
				"		}",
				"		int stepsInRect = int(floor(abs(dist)/ max("+scvMinDelta+",(abs("+sgvRayDirections+"[p]) * "+suvRenderRectStepSize+"))));",
				"		steps = min(steps, stepsInRect);",
				"	}",
				"	return steps;",
				"}",
				"",

		};

		String[] dependingFunctions ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() + " 1",
				"",
				"//collects the volume values of a given position for each partial volume",
				"float["+scvMaxNumberOfVolumes+"] getVolumeValues(vec3 positions ){",
				"	float volumeValues["+scvMaxNumberOfVolumes+"];",
				"	for(int i = 0; i < "+scvMaxNumberOfVolumes+"; i++){",
				"		vec3 normtexturePos = getCorrectedTexturePositions(positions,i);",
				"",
				"		//check for activity and containment of the volume",
				"		if("+suvActiveVolumes+"[i]==1",
				"			&& all(greaterThanEqual(normtexturePos,vtextOffsets[i]))",
				"			&& all(lessThanEqual(normtexturePos, vec3(1.0)-vtextOffsets[i] ))){",
				"			float value = texture("+suvVolumeTexture+"[i], normtexturePos ).r;",	
				"			volumeValues[i] = value;",
				"		}else{",
				"			volumeValues[i]=-1.0;",	
				"		}",
				"	}",
				"	return volumeValues;",
				"}",
				"",
				"//collects the accumulated volume value and normalizes them to [0,1] for accessing the texture",
				"float getNormalizedAndAggregatedVolumeValue(vec3 positionOnRay){",
				"	float densities["+scvMaxNumberOfVolumes+"] = getVolumeValues(positionOnRay);",
				"	return "+accumulator.call(new String[]{"densities"})+" * "+sgvVolumeNormalizeFactor+";",		
				"}",
				"",
				"//pre declaration for using this function in the iso surface interpreter",
				"float getValue(vec3 positionOnRay);",

		};

		String body[] = {	
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() + " 1111",
				"",
				"//returns the aggregated volume or volume gradient value",
				"float getValue(vec3 positionOnRay){",
				"	if("+suvUseGradient+"== 0){",
				"		return getNormalizedAndAggregatedVolumeValue(positionOnRay );",
				"	}else{",
				"		return 7.0*length("+gradient.call(new String[]{"positionOnRay"})+".xyz);",
				"	}",
				"}",
				"",
				"int renderedSlice =0;",
				"float latestdDistanceToSlice;",
				"bool sliceNeedsGammaCorrection = (1.0/max("+scvMinDelta+",("+suvMaxVolumeValue+")*"+suvMinVolumeValue+") < 0.05);",
				"",
				"//evaluates and renders the current bdv slice",
				"void evaluate2DSlice(float density, float nextDensity){",
				"	if("+suvShowSlice+" == 1 && renderedSlice != 1){",
				"		vec4 sliceColor= vec4(0.0);  ",
				"		float currentSliceDistance = getPlaneDistance("+suvNormalSlice+","+sgvRayPositions+" );",
				"		if(sign(currentSliceDistance) != sign(latestdDistanceToSlice) || sign(currentSliceDistance) == 0 ){",			
				"			float midDensity;",
				"			float dist = abs(latestdDistanceToSlice);",
				"			//if(dist < 0.5){",
				"				midDensity = density;",
				"			//}else{",
				" 				//midDensity = density+(nextDensity-density)/dist;",
				"			//}",
				"			sliceColor =vec4(midDensity,midDensity,midDensity,1.0);",
				"			sliceColor.a  = 1.0;",
				"			if(sliceNeedsGammaCorrection){",
				"				sliceColor.rgb *= gamma;",
				"			}",
				"",
				"			fragmentColor = fragmentColor + (1.0 - fragmentColor.a)*sliceColor;",
				"			fragmentColor.a = 1.0;",
				"			"+sgvRayPositions+" += "+sgvRayDirections+" * dist;",	
				"			renderedSlice = 1;",
				"		}",
				"		latestdDistanceToSlice = currentSliceDistance;",
				"	}",	
				"}",
				"",
				"//main fragment shader",
				"void main(void)",
				"{",	
				"",	
				"	fragmentColor = vec4("+suvBackgroundColor+".xyz,0.0);",
				"	"+sgvVolumeNormalizeFactor+" = 1.0/ ("+suvMaxVolumeValue+");",
				"	"+sgvNormIsoValue+"="+suvIsoValue+"*"+sgvVolumeNormalizeFactor+";",
				"	int steps = "+Short.MAX_VALUE+";",
				"	int startStep = "+Short.MAX_VALUE+";",	
				"",
				"	//init position, direction ,etc",
				"	"+sgvRayDirections+" = normalize("+svRayStartCoordinate+" - "+suvEyePosition+" );",
				"	"+sgvRayPositions+" = "+svRayStartCoordinate+";",
				"",
				"	//calculate volume texture normalization data and surpress unrolling tor fit on gpu memory",	
				"#pragma optionNV(unroll none)",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		vec3 tmp = "+suvVoxelCount+"[n];",
				"	    vtextOffsets[n] = vec3(1.0/(2.0*max(tmp,vec3(1.0))));",
				"	    vtextScales[n] = (vec3("+suvVoxelCount+"[n]-ivec3(1)))/max((vec3("+suvVoxelCount+"[n])*vec3("+suvVoxelCount+"[n]-ivec3(1))),vec3(1.0));",
				"",   
				"	}",
				"",
				"	//update steps",
				"	steps = min(steps,getStepsTillClipp());",
				"   steps = min(steps, "+suvSamples+");",
				"	startStep = max(startStep,steps-1);",	
				"	//end init",
				"",
				"	//ray casting",
				"  	float density =getValue("+sgvRayPositions+"); ",
				"",
				"	latestdDistanceToSlice = getPlaneDistance("+suvNormalSlice+","+sgvRayPositions+" );",
				"  	for(int i = 0; i < steps; i++){",
				"",
				"      	// note:", 
				"      	// - ray_dir * "+suvRenderRectStepSize+" can be precomputed",
				"      	// - we assume the volume has a cube-like shape",
				"",

				"      	// break out if ray reached the end of the cube.",
				"		float nextDensity = getValue("+sgvRayPositions+");",
				"",
				"      	vec4 color = "+transferFunctionCode.call(new String[]{"density","nextDensity",suvRenderRectStepSize})+";",
				"      	fragmentColor =  "+interpreter.call(new String[]{"fragmentColor","color","density","nextDensity"})+";",
				"",		
				"",
				"		//render slice",
				"		evaluate2DSlice(density,nextDensity);",	
				"",
				"		//early ray termination",
				"		if(fragmentColor.a +"+scvMinDelta+" >= 1.0){",
				"			fragmentColor.a = 1.0;",
				"			break;",
				"		}",	
				"",
				"		//blending for animation",
				"		if("+suvOpacity3D+" < 0.99){",
				"			fragmentColor *= "+suvOpacity3D+";",
				"		}",
				"",
				"		//increment ray",
				"		"+sgvRayPositions+" += "+sgvRayDirections+" * "+suvRenderRectStepSize+";",
				"",
				"		//save density value",
				"		density = nextDensity;",
				"   }",
				"}"
		};
		//append shader strings
		//global declarations
		addCodeArrayToList(head, code);
		//addCodeArrayToList(stepsToVolume.declaration(), code);
		//addCodeArrayToList(stepsFunction.declaration(), code);

		//add acumulator code
		addCodeArrayToList(accumulator.declaration(), code);

		//transfer function interpreter
		addCodeArrayToList(transferFunctionCode.declaration(), code);

		//function using former declared functions
		addCodeArrayToList(dependingFunctions, code);

		//gradient evaluator
		addCodeArrayToList(gradient.declaration(), code);

		//visualization mode
		addCodeArrayToList(interpreter.declaration(), code);

		//main method
		addCodeArrayToList(body, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}

	/**
	 * set new accumulator code and trigger rebuild
	 * @param a1
	 */
	public void setAccumulator(AbstractVolumeAccumulator a1) {
		if(a1.equals(this.accumulator)){
			return;
		}
		this.accumulator = a1;
		this.interpreter.setAccumulator(accumulator);
		notifySourceCodeChanged();
	}

	/**
	 * set new volume interpreter code and trigger rebuild
	 * @param a1
	 */
	public void setVolumeInterpreter(AbstractVolumeInterpreter volumeInterpreter) {
		this.interpreter = volumeInterpreter;
		this.interpreter.setAccumulator(accumulator);
		notifySourceCodeChanged();

	}
}
