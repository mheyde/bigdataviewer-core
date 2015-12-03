package bdv.jogl.VolumeRenderer.shaderprogram.shadersource;

import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.*;

import java.util.HashSet;
import java.util.Set;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderCode;

public class UnitCubeShaderSource extends AbstractShaderSource {

	private static final String svFragmentColor = "fragmentColor";
	
	public static final String suvColor = "inColor";
	
	@Override
	public Set<ShaderCode> getShaderCodes() {
		Set<ShaderCode> codes = new HashSet<ShaderCode>();
		codes.add(new ShaderCode(GL2.GL_VERTEX_SHADER, 1, new String[][]{vertexShaderCode()}));
		codes.add(new ShaderCode(GL2.GL_FRAGMENT_SHADER, 1, new String[][]{fragmentShaderCode()}));
		return codes;
	}

	private String[] vertexShaderCode(){
		String[] code={
				"#version "+getShaderLanguageVersion(),
				"",
				"uniform mat4x4 "+suvProjectionMatrix+";",
				"uniform mat4x4 "+suvViewMatrix+";",
				"uniform mat4x4 "+suvModelMatrix+";",
				"uniform vec4 "+suvColor+";",
				"",
				"in vec3 "+satPosition+";",
				"out vec4 "+svFragmentColor+";",
				"",
				"void main()",
				"{",
				"	vec4 position4d = vec4("+satPosition+".xyz,1.f);",
				"",
				"	gl_Position ="+suvProjectionMatrix+" * "+suvViewMatrix+" * "+suvModelMatrix+" * position4d;",
				"	"+svFragmentColor+" = "+suvColor+";",
				"}"
		};
		appendNewLines(code);
		return code;
	}
	
	private String[] fragmentShaderCode(){
		String[] code={
				"#version "+getShaderLanguageVersion(),
				"in vec4 "+svFragmentColor+";",
				"out vec4 color;",
				"",
				"void main(void)",
				"{",	
				"	color = "+svFragmentColor+";",
				"}"
		};
		appendNewLines(code);
		return code;
	}
}
