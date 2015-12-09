package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.volumeninterpreter;
import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.*;

/**
 * Supports Maximum Intensity Projection 
 * @author michael
 *
 */
public class MaximumIntensityProjectionInterpreter extends AbstractVolumeInterpreter {

	/**
	 * constructor
	 */
	public MaximumIntensityProjectionInterpreter(){
		super("maximumIntensityProjection");
	}
	
	@Override
	public String[] declaration() {

		return new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() +" 4",
				"",
				"//init of max intensity",
				"float maxIntensity = 0;",
				"",
				"//interpreter function",
				"vec4 "+getFunctionName()+"(vec4 c_in, vec4 c, float vm1, float v ){",
				"",
				"	//if current value is bigger than max then update",
				"	if(v > maxIntensity){",
				"		maxIntensity = v;",
				"",
				"		//color renormalization for the transfer function color to increase brightness",
				"		c.rbg /= "+suvRenderRectStepSize+";",
				"		return c;",
				"	}",
				"	return c_in;",
				"}"
		};
	};
}
