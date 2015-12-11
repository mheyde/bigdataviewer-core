package bdv.jogl.VolumeRenderer.scene;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.math.geom.AABBox;

import bdv.jogl.VolumeRenderer.gui.SceneControlsWindow;
import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;
import bdv.jogl.VolumeRenderer.shaderprogram.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.utils.VolumeRendereSampleController;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.calcEyeAndCenterByGivenHull;;

/**
 * Class controlling the animations in the scene
 * @author michael
 *
 */
public class InteractionAnimator {
	
	private final MultiVolumeRenderer renderer;
	
	private final GLWindow renderWindow;
	
	private int currentAnimationPercentage =0;
	
	private final int percentageIncrement = 5;
	
	private Thread initAnimationThread = null;
	
	private Thread motionToTargetThread = null;
	
	private boolean stopTriggered = false;
	
	private final SceneControlsWindow controls;

	private final VolumeRendereSampleController sampleController;
	
	public final Object animationMutex = new Object();
	/**
	 * Animation constructor
	 * @param renderer the used volume renderer
	 * @param renderWindow the window to render in
	 * @param controls the used menu window
	 * @param contoller the down sampling controller class
	 */
	public InteractionAnimator(
			final MultiVolumeRenderer renderer, 
			final GLWindow renderWindow, 
			final SceneControlsWindow controls,
			final VolumeRendereSampleController contoller){
		this.renderer = renderer;
		this.renderWindow = renderWindow;
		this.controls = controls;
		this.sampleController = contoller;
	}
	
	/**
	 * stop init animation thread
	 */
	public void interruptInitAnimation(){
		if(initAnimationThread == null){
			return;
		}
		initAnimationThread.interrupt();
	};
	


	/**
	 * blending of 3D on 3D
	 */
	private void doInitAnimationStep(){
		
		renderer.setOpacity3D((float)(currentAnimationPercentage)/ 100f);
		renderWindow.getGlCanvas().repaint();
		

	}
	
	/**
	 * Starts the big volume in fading 
	 */
	public void startInitAnimation(){
		if(initAnimationThread!= null){
			if(initAnimationThread.isAlive()){
				return;
			}
			initAnimationThread = null;
		}
		prepareAnimations();
		initAnimationThread = new Thread(){
			
				/**
				 * Runs over Animation steps and animates blending of views, is interruptable  
				 */
				public void run(){
					sampleController.downSample();
					for(currentAnimationPercentage =0; currentAnimationPercentage< 100; currentAnimationPercentage+=percentageIncrement){
						if(stopTriggered){
							break;
						}
						if(isInterrupted()){
							break;
						}
						doInitAnimationStep();
						
						try {
							sleep(100);
						} catch (InterruptedException e) {
							break;
						}
					}
					sampleController.upSample();
					//make 100% animation mode
					currentAnimationPercentage = 100;
					doInitAnimationStep();
						
				}
		};
		
		initAnimationThread.start();
	}
	
	/**
	 * Restore default preconditions of an animation, slice is shown and no stop signal was triggered
	 */
	private void prepareAnimations(){
		stopTriggered = false;
		controls.getShowSlice().setSelected(true);
	} 
	
	/**
	 * Motion to data and update partial data
	 * @param hullVolume the hull volume to move to
	 */
	public void startMoveToSelectionAnimation( final AABBox hullVolume){
		if(null != this.motionToTargetThread && motionToTargetThread.isAlive()){
			return;
		}
		
		prepareAnimations();
		
		motionToTargetThread = null;
		final List<float[][]> motionPositions = calcEyeAndCenterPath(hullVolume, 100 /percentageIncrement);
		motionToTargetThread = new Thread(){
			
			/**
			 * Runs over animation steps and animates camera motion to target partial volume. is interruptable
			 */
			public void run(){
			synchronized (animationMutex) {
				
			
				boolean updatedData= false;
				Camera c = renderWindow.getScene().getCamera();
				AABBox currentHullVolume = renderer.getDrawRect();
				sampleController.downSample();
				int n = 0;
				for(currentAnimationPercentage = 0; currentAnimationPercentage < 100; currentAnimationPercentage+=percentageIncrement){
					if(stopTriggered){
						break;
					}

					try {
						float eyeCenter[][] = motionPositions.get(n);
						//enter hull volume -> invalid fragments
						if(!updatedData&&currentHullVolume.contains( eyeCenter[0][0],eyeCenter[0][1],eyeCenter[0][2])){
							renderer.setDrawRect(hullVolume);
							updatedData=true;
						}
						c.setEyePoint(eyeCenter[0]);
						c.setLookAtPoint(eyeCenter[1]);
						c.updateViewMatrix();
						n++;
						sleep(100);
						
					} catch (InterruptedException e) {
						break;
					}
				}
				sampleController.upSample();
				currentAnimationPercentage = 100;
				
				//finally set new hull volume 
				renderer.setDrawRect(hullVolume);				
			}
			}
		};
		motionToTargetThread.start();
	}
	
	/**
	 * cancel move to animation and shows result
	 */
	public void interruptMoveToSelectionAnimation(){
		if(motionToTargetThread == null){
			return;
		}
		motionToTargetThread.interrupt();
	}
	
	/**
	 * stops all running animation threads 
	 */
	public void stopAllAnimations() {
		stopTriggered = true;
		interruptInitAnimation();
		interruptMoveToSelectionAnimation();
		
	}
	
	/**
	 * calculates the positions of eye and center on a n steps path to view hullVolume
	 * @param hullVolume the hull volume to move to
	 * @param n the number of used animation steps
	 * @return a lost of eye and center coordinates as path for the view 
	 */
	private List<float[][]> calcEyeAndCenterPath(AABBox hullVolume,int n){
		List<float[][]> positions = new ArrayList<float[][]>();
		float eyeCenterStart[][] = new float[][]{renderWindow.getScene().getCamera().getEyePoint(),renderWindow.getScene().getCamera().getLookAtPoint()};
		float eyeCenterFinal[][] = calcEyeAndCenterByGivenHull(hullVolume,renderer.getSlice2Dplane());
		
		//TODO non linear
		float stepMovesEyeCenter [][]= new float[2][3];
		for(int p = 0;p < stepMovesEyeCenter.length; p++){
			for(int d = 0; d < stepMovesEyeCenter[p].length; d++){
				stepMovesEyeCenter[p][d] = (eyeCenterFinal[p][d] - eyeCenterStart[p][d]) / (float)n;
			}
		}
		
		//linear steps
		float currentPos[][] = eyeCenterStart.clone();
		for(int i=0; i < n; i++){
			for(int p = 0;p < stepMovesEyeCenter.length; p++){
				for(int d = 0; d < stepMovesEyeCenter[p].length; d++){
					currentPos[p][d] += stepMovesEyeCenter[p][d];
					
				}
			}
			float savePosition[][] = new float[][]{currentPos[0].clone(),currentPos[1].clone()};
			positions.add(savePosition);
		}
		//TODO end
		
		return positions;
	}
}
