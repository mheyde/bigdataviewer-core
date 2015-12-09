package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function;

/**
 * Defines object based interfaces for shader functions
 * @author michael
 *
 */
public interface IFunction {

	/**
	 * Return the function declaration, the function source code.
	 * @return the declaration of the function as string array
	 */
	public String[] declaration();
	
	/**
	 * Return the function call semantic 
	 * @param parameters Parameter Strings for the shader.
	 * @return the call syntax of the function
	 */
	public String call(final String[] parameters);
}
