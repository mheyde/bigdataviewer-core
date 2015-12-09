package bdv.jogl.VolumeRenderer.shaderprogram.shadersource.function;

/**
 * defines function name for function and std call syntax
 * @author michael
 *
 */
public abstract class AbstractShaderFunction implements IFunction {

	private final String functionName;

	/**
	 * constructor
	 * @param functionName the function name to use in the definition
	 */
	protected AbstractShaderFunction(String functionName){
		this.functionName = functionName; 
	}

	/**
	 * @return the name of the shader function
	 */
	public String getFunctionName() {
		return functionName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		if(obj.getClass()!=this.getClass()){
			return false;
		}
		AbstractShaderFunction func = (AbstractShaderFunction) obj;
		String[] dec = this.declaration();
		String[] fdec= func.declaration();
		if(dec.length != fdec.length){
			return false;
		}
		for(int i =0; i < dec.length; i++){
			if(!dec[i].equals(fdec[i])){
				return false;
			}
		}
		return true;
	};
	
	@Override
	public String call(String[] parameters) {
		String call = functionName + "(";
		int i =0;
		for(String parameter:parameters){
			call += parameter;
			if(i < parameters.length-1){
				call+=",";
			}
			i++;
		}
		call+=")";
		return call;
	}
}
