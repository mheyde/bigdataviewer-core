package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.volumeninterpreter;
import static bdv.jogl.VolumeRenderer.shaderprogram.shadersource.MultiVolumeRendererShaderSource.*;
/**
 * Supports Maximum Intensity Projection 
 * @author michael
 *
 */
public class MaximumIntensityProjectionInterpreter extends AbstractVolumeInterpreter {
	public MaximumIntensityProjectionInterpreter(){
		super("maximumIntensityProjection");
	}
	@Override
	public String[] declaration() {

		return new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() +" 4",
				"float maxIntensity = 0;",
				"vec4 "+getFunctionName()+"(vec4 c_in, vec4 c, float vm1, float v ){",
				"	if(v > maxIntensity){",
				"		maxIntensity = v;",
				"		c.rbg /= "+suvRenderRectStepSize+";",
				"		return c;",
				"	}",
				"	return c_in;",
				"}"
		};
	};

}
