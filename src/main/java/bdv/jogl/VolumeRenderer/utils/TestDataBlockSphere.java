package bdv.jogl.VolumeRenderer.utils;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;

import com.jogamp.opengl.math.Matrix4;

/**
 * Test dataset representing a sphere like volume block 
 * @author michael
 *
 */
public class TestDataBlockSphere extends VolumeDataBlock {

	private final int radius;
	
	/**
	 * Constructor
	 * @param radius Radius of the sphere
	 */
	public TestDataBlockSphere(int radius){
		this.radius = radius;
		
		initDataVolume();
	}
	
	/**
	 * create the volume values of the sphere
	 */
	private void initDataVolume() {
		//fill data values
		int i = 0;
		this.data= new float[(int)Math.pow(2*radius, 3)];
		for(int z = 0; z < 2*radius; z++ ){
			for (int y = 0; y < 2*radius; y++){
				for (int x = 0; x< 2*radius; x++){
					//sphere equation
					float value;
					float check = radius*radius;
					float checked = (x-radius)*(x-radius)+(y-radius)*(y-radius)+(z-radius)*(z-radius);
					if( checked < 1.01*check){
						value = checked;
					}else{
						value = 0;
					}
					
					int count;
					if(this.valueDistribution.containsKey(value)){
						count = this.valueDistribution.get(value)+1;
					}else{
						count = 0;
					}
					this.valueDistribution.put(value, count);
					if(this.maxOccurance < count){
						this.maxOccurance =count;
					}
					
					this.data[i++] = value;
				}
			}
		}
		
		//set dimensions
		for(int d = 0; d < this.dimensions.length;  d++){
			this.dimensions[d] = 2*radius;
			this.memOffset[d] = 0;
			this.memSize[d] = 2*radius;
		}
		
		this.minValue = this.valueDistribution.firstKey();
		this.maxValue = this.valueDistribution.lastKey();
	
		Matrix4 transform =  getNewIdentityMatrix();
		setLocalTransformation(transform);
	}	
}
