package bdv.jogl.VolumeRenderer.scene;


import java.util.HashMap;
import java.util.Map;

import bdv.jogl.VolumeRenderer.shaderprogram.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.shaderprogram.UnitCube;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManagerAdapter;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.*;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.Matrix4;

/**
 * Describes a scene for volume data with multiple scene objects
 * @author michael
 *
 */
public class VolumeDataScene extends AbstractScene{

	private Map<Integer,UnitCube> volumeBorders = new HashMap<Integer, UnitCube>();

	private VolumeDataManager dataManager;

	private boolean showVolumes = true;

	private MultiVolumeRenderer renderer;
	//private UnitCube boundingVolume =new UnitCube();

	
	@Override
	protected void disposeSpecial(GL4 gl2) {
	}

	/**
	 * Set all Volume Quads to visible
	 * @param flag the status of enabling
	 */
	public void enableVolumeBorders(boolean flag){
		showVolumes = flag;
		for(UnitCube c : volumeBorders.values()){
			c.setEnabled(flag);
		}
	}
	
	/**
	 * Adds a new Cube Shader with an appropriate color
	 * @param id the data index to connect to
	 */
	private void addNewCubeBorderShader(Integer id){
		UnitCube cubeShader = new UnitCube();
		volumeBorders.put(id,cubeShader);
		addSceneElement(cubeShader);
		cubeShader.setRenderWireframe(true);
		cubeShader.setColor(getColorOfVolume(id));
	}
	
	/**
	 * Updates the model-, view- and projection-matrices for a cube shader 
	 * @param id the data index to add a cube to
	 * @param data the data to use for transformation
	 */
	private void updateCubeBorderShader(Integer id, final VolumeDataBlock data){
		UnitCube cubeShader = volumeBorders.get(id);
		Matrix4 modelMatrix = calcScaledVolumeTransformation(data);
		cubeShader.setProjection(getCamera().getProjectionMatix());
		cubeShader.setView(getCamera().getViewMatrix());
		cubeShader.setModelTransformation(modelMatrix);
		cubeShader.setEnabled(showVolumes);
	}
	
	/**
	 * Sets and connects a new data manager
	 * @param manager the manager to set
	 */
	private void setDataManager(final VolumeDataManager manager){
		dataManager = manager;
		
		dataManager.addVolumeDataManagerListener(new VolumeDataManagerAdapter() {
			
			/**
			 * Handler to add new volume borders on adding new data
			 */
			@Override
			public void addedData(Integer id) {
				//add cubes
				addNewCubeBorderShader(id);
			}
			
			/**
			 * Handler to update volume borders on updating data 
			 */
			@Override 
			public void dataUpdated(Integer id) {
				updateCubeBorderShader(id, dataManager.getVolume(id));
				fireNeedUpdateAll();
			}
		});
	}
	
	@Override
	protected void resizeSpecial(GL4 gl2, int x, int y, int width, int height) {}

	/**
	 * @return the multi volume renderer in use
	 */
	public MultiVolumeRenderer getRenderer(){
		return renderer;
	}

	/**
	 * Constructor
	 * @param dataManager the data manager to use
	 * @param renderer the volume renderer to use 
	 */
	public VolumeDataScene( VolumeDataManager dataManager, MultiVolumeRenderer renderer){
		this.renderer = renderer;
		setDataManager(dataManager);
		addSceneElement(renderer);
	}

	/**
	 * does std gl camera initializations
	 * @param camera2 camera to init
	 * @param width the width of the view canvas
	 * @param height the height of the view canvas 
	 */
	public void initLocalCamera(Camera camera2, int width, int height){

		camera2.setAlpha(45);
		camera2.setWidth(width);
		camera2.setHeight(height);
		camera2.setZfar(5000);
		camera2.setZnear(1);
		camera2.setUpVector(new float[]{0,-1,0});
		camera2.init();
	}

	
	/**
	 * initializes the scene once
	 * @param gl2 the gl context to use
	 * @param width the width of the view canvas
	 * @param height the height of the view canvas 
	 */
	protected void initSpecial(GL4 gl2, int width, int height){
		initBoundingVolumeCube(gl2);
		
		initLocalCamera(camera, width,height);
	}

	/**
	 * Adds a cube to show the Hull volume
	 * @param gl2 the gl context to use 
	 */
	private void initBoundingVolumeCube(GL4 gl2) {
		/*	addSceneElement(boundingVolume);

		boundingVolume.init(gl2);
		boundingVolume.setRenderWireframe(true);
		boundingVolume.setColor(Color.yellow);*/
	}

	/**
	 * 
	 * @param gl2 the gl context to use
	 */
	protected void renderSpecial(GL4 gl2){

	}
}
