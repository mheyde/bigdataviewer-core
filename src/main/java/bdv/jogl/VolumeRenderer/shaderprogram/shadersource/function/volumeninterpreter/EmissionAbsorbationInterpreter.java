package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function.volumeninterpreter;

/**
 * Emission absorbation Volume interpreter class
 * @author michael
 *
 */
public class EmissionAbsorbationInterpreter extends AbstractVolumeInterpreter {

	/**
	 * constructor
	 */
	public EmissionAbsorbationInterpreter(){
		super("transparentVolume");
	}
	
	@Override
	public String[] declaration() {

		return new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() +" 4",
				"",
				"//main ea interpreter function",
				"vec4 "+getFunctionName()+"(vec4 c_in, vec4 c, float vm1, float v ){",
				"",
				"	//front to back compositing",
				"	return c_in + (1.0 - c_in.a)*c;",
				"}"
		};
	};


}
