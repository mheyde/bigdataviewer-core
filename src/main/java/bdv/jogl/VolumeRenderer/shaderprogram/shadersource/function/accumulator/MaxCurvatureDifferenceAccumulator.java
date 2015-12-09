package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.accumulator;

import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;
import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.sgvRayPositions;
import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.sgvVolumeNormalizeFactor;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.calculateCurvatureOfVolume;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.createVolumeTexture;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bdv.jogl.VolumeRenderer.scene.Texture;
import bdv.jogl.VolumeRenderer.shaderprogram.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.utils.CurvatureContainer;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManagerAdapter;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

/**
 * Calculates the maximum difference of the curvature values
 * @author michael
 *
 */
public class MaxCurvatureDifferenceAccumulator extends AbstractVolumeAccumulator {

	private boolean needsReset = true;

	private final Map<Integer, Texture> curvatureTexture = new HashMap<Integer, Texture>();

	private final Map<Integer, CurvatureContainer> evaluatedCurvatures = new HashMap<Integer, CurvatureContainer>();

	private final static String suvCurvatureTexture = "inCurvatureTexture"; 

	private final static String suvCurvatureMax = "inCurvatureMax";

	private final static String suvCurvatureMin = "inCurvatureMin";

	private final Object mutex = new Object();

	private final Set<Integer> needsUpdate = new HashSet<Integer>(); 

	/**
	 * Constructor
	 */
	public MaxCurvatureDifferenceAccumulator() {
		super("difference_total_curvature");
	}
	
	@Override
	public void init(GL4 gl) {
		//create glsl variable mapping for texture and min may
		getParent().mapUniforms(gl,new String[]{suvCurvatureTexture});
		getParent().mapUniforms(gl,new String[]{suvCurvatureMax});
		getParent().mapUniforms(gl,new String[]{suvCurvatureMin});
		super.init(gl);
	}

	@Override
	public void disposeGL(GL4 gl2) {
		for(Texture t: curvatureTexture.values()){
			t.delete(gl2);
		}
		curvatureTexture.clear();
		evaluatedCurvatures.clear();
		super.disposeGL(gl2);
	}

	@Override
	public void updateData(GL4 gl) {
		VolumeDataManager dataManager = getParent().getDataManager();
		float globalMin = Float.MAX_VALUE;
		float globalMax = Float.MIN_VALUE;

		//create and update laplace textures 
		for(Integer key:dataManager.getVolumeKeys()){
			VolumeDataBlock data = dataManager.getVolume(key);
			Texture t;

			//create texture objects
			if(!curvatureTexture.containsKey(key)||needsReset){
				t = createVolumeTexture(gl, getParent().getArrayEntryLocation(gl, suvCurvatureTexture,key)) ;
				curvatureTexture.put(key, t);
			}
			t = curvatureTexture.get(key);

			synchronized (mutex) {


				//update laplacians
				if(this.needsUpdate.contains(key)||!evaluatedCurvatures.containsKey(key)||needsReset){
					CurvatureContainer container = calculateCurvatureOfVolume(data); 
					evaluatedCurvatures.put(key,container);


					//fill textures
					FloatBuffer buffer = Buffers.newDirectFloatBuffer(container.valueMesh3d);
					if(buffer.capacity() > 0){
						t.update(gl, 0, buffer, container.dimension);
					}
				}
				this.needsUpdate.remove(key);
			}
			CurvatureContainer tmp = evaluatedCurvatures.get(key);

			//get global min max value of laplace
			globalMax = Math.max(globalMax, tmp.maxValue);
			globalMin = Math.min(globalMin, tmp.minValue);
		}
		//update globals
		gl.glUniform1f(getParent().getLocation(suvCurvatureMin), globalMin);
		gl.glUniform1f(getParent().getLocation(suvCurvatureMax), globalMax);

		needsReset = false;
		super.updateData(gl);
	}



	@Override
	public void setParent(MultiVolumeRenderer parent) {
		needsReset = true;
		if(!parent.equals(super.getParent())){
			parent.getDataManager().addVolumeDataManagerListener(new VolumeDataManagerAdapter() {
				
				/**
				 * needs to update curvature texture
				 */
				@Override
				public void dataUpdated(Integer i) {
					synchronized (mutex) {
						needsUpdate.add(i);
					}

				}
			});
		}
		super.setParent(parent);
	}

	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 6666",
				"",
				"//pre calculated data",
				"uniform sampler3D "+suvCurvatureTexture+"["+scvMaxNumberOfVolumes+"];",
				"uniform float "+suvCurvatureMax+";",
				"uniform float "+suvCurvatureMin+";",
				"float curveNormalizeFactor = 1.0/("+suvCurvatureMax+"-"+suvCurvatureMin+");",
				"bool factorChanged = false;",
				"",
				"//main accumulator method",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float minValue = "+Float.MAX_VALUE+";",
				"	float maxValue = "+Float.MIN_VALUE+";",
				"	int numberOfChanges = 0;",
				"",
				"	//change globale value normalization factor of the multi volume renderer",
				"	//on first call. needed for proper tf texture fitting",		
				"   if(!factorChanged){",
				"		"+sgvVolumeNormalizeFactor+" = curveNormalizeFactor;",
				"		factorChanged = true;",
				"	}",
				"",
				"	//iterate densities",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"",
				"		//scip on invalid",
				"		if(densities[n] < 0.0 ){",
				"			continue;",
				"		}",	
				"",
				"		//read min max curv values",
				"		vec3 texCN = getCorrectedTexturePositions("+sgvRayPositions+", n);",
				"		float cn = texture("+suvCurvatureTexture+"[n],texCN).r;",
				"		maxValue = max(maxValue,cn);",
				"		minValue = min(minValue,cn);",	
				"		numberOfChanges++;",
				"	}",
				"",
				"	//no valid values zero returns",
				"	if(numberOfChanges ==0){",
				"		return 0.0;",
				"	}",
				"	return maxValue -minValue;",	
				"}"
		};
		addCodeArrayToList(dec, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}
}
