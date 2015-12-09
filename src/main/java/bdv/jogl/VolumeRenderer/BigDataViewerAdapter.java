package bdv.jogl.VolumeRenderer;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;

import com.jogamp.opengl.math.Matrix4;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;
import bdv.BigDataViewer;
import bdv.jogl.VolumeRenderer.utils.TestDataBlockSphere;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.viewer.state.SourceState;


/**
 * Bridging class between bdv and the data manager
 * @author michael
 *
 */
public class BigDataViewerAdapter {

	/**
	 * get the mipmap level to use ()
	 * @param source the source to get the mipmap index from
	 * @return the mipmap index
	 */
	private static int getMipmapLevel(final SourceState<?> source){
		int levels= source.getSpimSource().getNumMipmapLevels();
		return levels -1;
	}

	/**
	 * connects the bdv to the data manger
	 * @param bdv the data source to use
	 * @param manager the data manager to connect
	 */
	public static synchronized void connect(final BigDataViewer bdv,final VolumeDataManager manager){
		//updateData(bdv,manager);
		//updatedTestData(manager);
		bdv.getViewer().addRenderTransformListener(new TransformListener<AffineTransform3D>() {

			@Override
			public synchronized void transformChanged(AffineTransform3D transform) {

					updateData(manager);
				
			}
		});
	}
	
	/**
	 * adds test sphere data stack to a data manager 
	 * @param manager the data manager to add data
	 */
	private static void updatedTestData(final VolumeDataManager manager){
		
		for(int n = 0; n < 4 ; n++){
			int radius = 50;
			
			//make transformation
			Matrix4 trans= getNewIdentityMatrix();
			trans.rotate(30*n,0,0,1);
			trans.translate(-radius, -radius, -radius);
	
			TestDataBlockSphere s = new TestDataBlockSphere(radius);
			s.setLocalTransformation(trans);
			
			manager.setVolume(n,0, s);
			
		}
	};
	
	/**
	 * updates the manager data
	 * @param manager the manager to update
	 */
	private static void updateData(final VolumeDataManager manager){

		manager.updateData();

	}
}
