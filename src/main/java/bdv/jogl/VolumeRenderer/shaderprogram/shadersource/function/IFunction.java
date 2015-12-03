package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function;

/**
 * Defines object based interfaces for shader functions
 * @author michael
 *
 */
public interface IFunction {

	/**
	 * Return the function declaration, the function source code.
	 * @return
	 */
	public String[] declaration();
	
	/**
	 * Return the function call semantic 
	 * @param parameters Parameter Strings for the shader.
	 * @return
	 */
	public String call(final String[] parameters);
}
