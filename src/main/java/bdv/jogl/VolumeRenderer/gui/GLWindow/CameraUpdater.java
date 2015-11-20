package bdv.jogl.VolumeRenderer.gui.GLWindow;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;

import static bdv.jogl.VolumeRenderer.utils.WindowUtils.transformWindowNormalSpace;
import bdv.jogl.VolumeRenderer.Camera;

/**
 * Class for providing mouse camere interactions in gl scenes
 * @author michael
 *
 */
public class CameraUpdater {
	
	private final Camera camera;

	private Point previousOrbitPoint = null;
	
	private Point previousTracPoint = null;
	
	private final static int orbitButton = MouseEvent.BUTTON1;
	
	private final static int tracButton = MouseEvent.BUTTON3;
	
	private final Collection<CameraTransformationListener> cameraTransformationListeners = new ArrayList<CameraTransformationListener>();
	
	private long wheelThreadWaiteTime =0;
	
	private long wheelThreadWaiteTimePeriod = 200;
	
	private Thread mouseWheelThread = null;
	
	private final MouseListener mouseListener = new MouseAdapter() {
	
		/**
		 * Starts mouse drag events orbit and trac
		 */
		@Override
		public synchronized void mousePressed(MouseEvent e) {
			Point point = transformWindowNormalSpace(e.getPoint(),e.getComponent().getSize());
			
			//start orbit
			if(e.getButton() == orbitButton){
				previousOrbitPoint = point;	
				fireAllTransformationStart();
			}
			
			//start trac
			if(e.getButton() == tracButton){
				previousTracPoint = point;
				fireAllTransformationStart();
			}
		};
		
		/**
		 * Stops mouse drag events orbit and trac
		 */
		@Override
		public synchronized void mouseReleased(MouseEvent e) {
			//stop orbit
			if(e.getButton() == orbitButton){
				previousOrbitPoint = null;
				fireAllTransformationStop();
			}
			
			//stop trac
			if(e.getButton() == tracButton){
				previousTracPoint = null;
				fireAllTransformationStop();
			}			
		};
	};
	
	private final MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
	
		/**
		 * Updates drag events orbit and trac
		 */
		@Override
		public synchronized void  mouseDragged(MouseEvent e) {
			Point currentPoint = transformWindowNormalSpace(e.getPoint(),e.getComponent().getSize());
			
			//update orbit
			if(previousOrbitPoint != null){
				//calculate angles for spinning view 360 degrees by dragging form center to border 
				float angleScaleX = 90f / (float)(8* e.getComponent().getWidth());
				float angleScaleY = 90f / (float)(8* e.getComponent().getHeight());
				float alpha = -( currentPoint.y - previousOrbitPoint.y )*angleScaleY;
				float beta = ( currentPoint.x - previousOrbitPoint.x )*angleScaleX;
				camera.orbit(alpha, beta);
				previousOrbitPoint = currentPoint;
				return;
			}

			//update trac
			if(previousTracPoint != null){
				float diffx = currentPoint.x - previousTracPoint.x;
				float diffy = currentPoint.y - previousTracPoint.y;
				camera.trac(diffx, diffy);
				previousTracPoint = currentPoint;
				return;
			}
		
		};
	};
	
	private final MouseWheelListener mouseWheelListener = new MouseWheelListener() {
		
		/**
		 * Camera zoom handler
		 */
		@Override
		public synchronized void mouseWheelMoved(MouseWheelEvent e) {
			//reset wait time
			wheelThreadWaiteTime = wheelThreadWaiteTimePeriod;
			
			//create a wheel thread to support down sampling while zoom 
			//needed since there is no wheel stop and begin event 
			if(mouseWheelThread ==null || !mouseWheelThread.isAlive()){
				mouseWheelThread = new Thread(){
					public void run() {
						fireAllTransformationStart();
						
						//wait till time runs out for upsampling
						while(wheelThreadWaiteTime > 0){
							long waitTime = wheelThreadWaiteTime;
							wheelThreadWaiteTime = 0;
							try {
								sleep(waitTime);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						fireAllTransformationStop();
					};
				};
				mouseWheelThread.start();
			}
			
			//do zoom in main thread
			float alpha = camera.getAlpha(); 
			alpha += (float)e.getWheelRotation();
			alpha = Math.min(Camera.maxAlpha,Math.max(Camera.minAlpha,alpha));
			camera.setAlpha(alpha);
			camera.updatePerspectiveMatrix();
		}
	};
	
	/**
	 * triggers all listeners for transformation start events
	 */
	private void fireAllTransformationStart(){
		for(CameraTransformationListener l : cameraTransformationListeners){
			l.transformationStart();
		}
	}
	
	/**
	 * triggers all listeners for transformation stop events
	 */
	private void fireAllTransformationStop(){
		for(CameraTransformationListener l : cameraTransformationListeners){
			l.transformationStop();
		}
	}
	
	
	/**
	 * Constructor for setting the camera
	 * @param camera
	 */
	public CameraUpdater(Camera camera) {
		this.camera = camera;
	}

	/**
	 * return the motion listener to add to the scene widget
	 * @return
	 */
	public MouseMotionListener getMouseMotionListener() {
		return mouseMotionListener;
	}

	/**
	 * return the mouse listener to add to the scene widget
	 * @return
	 */
	public MouseListener getMouseListener() {
		return mouseListener;
	}

	/**
	 * @return the mouseWheelListener
	 */
	public MouseWheelListener getMouseWheelListener() {
		return mouseWheelListener;
	} 

	/**
	 * adds a motionListenr
	 * @param l
	 */
	public void addCameraMotionListener(CameraTransformationListener l){
		cameraTransformationListeners.add(l);
	}
}
