package bdv.jogl.VolumeRenderer.shaderprogram.shadersource;

import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.*;

import java.util.HashSet;
import java.util.Set;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderCode;

/**
 * Stores shader code string for cubes 
 * @author michael
 *
 */
public class UnitCubeShaderSource extends AbstractShaderSource {

	/**
	 * the color uniform variable name in shader code
	 */
	public static final String suvColor = "inColor";
	
	@Override
	public Set<ShaderCode> getShaderCodes() {
		Set<ShaderCode> codes = new HashSet<ShaderCode>();
		codes.add(new ShaderCode(GL2.GL_VERTEX_SHADER, 1, new String[][]{vertexShaderCode()}));
		codes.add(new ShaderCode(GL2.GL_FRAGMENT_SHADER, 1, new String[][]{fragmentShaderCode()}));
		return codes;
	}

	/**
	 * Creates fragment shader code string
	 * @return the lines of the vertex shader code
	 */
	private String[] vertexShaderCode(){
		String[] code={
				"#version "+getShaderLanguageVersion(),
				"",
				"uniform mat4x4 "+suvProjectionMatrix+";",
				"uniform mat4x4 "+suvViewMatrix+";",
				"uniform mat4x4 "+suvModelMatrix+";",
				"",
				"in vec3 "+satPosition+";",
				"",
				"void main()",
				"{",
				"	vec4 position4d = vec4("+satPosition+".xyz,1.f);",
				"",
				"	//model view transformation",
				"	gl_Position ="+suvProjectionMatrix+" * "+suvViewMatrix+" * "+suvModelMatrix+" * position4d;",
				"}"
		};
		appendNewLines(code);
		return code;
	}

	/**
	 * Creates vertex shader code string
	 * @return the lines of the fragment shader code
	 */
	private String[] fragmentShaderCode(){
		String[] code={
				"#version "+getShaderLanguageVersion(),
				"uniform vec4 "+suvColor+";",
				"out vec4 color;",
				"",
				"//pipe through fragment shader",
				"void main(void)",
				"{",	
				"	color = "+suvColor+";",
				"}"
		};
		appendNewLines(code);
		return code;
	}
}
