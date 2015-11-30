package bdv.jogl.VolumeRenderer.utils;

import java.util.List;

/**
 * 
 * @author michael
 *
 */
public class ShaderSourceUtil {

	/**
	 * appends system depending new lines to each line of the source code
	 * @param shaderCode
	 */
	public static void appendNewLines(String[] shaderCode){
		for(int i = 0; i < shaderCode.length; i++){
			   
			shaderCode[i] = new String(shaderCode[i] + System.lineSeparator());
		}
	}
	
	/**
	 * Adds the given array code to the given List
	 * @param code
	 * @param list
	 */
	public static void addCodeArrayToList(final String[] code, List<String> list){
		for(String line: code){
			list.add(line);
		}
	} 
}
