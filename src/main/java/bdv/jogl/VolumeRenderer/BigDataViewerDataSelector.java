package bdv.jogl.VolumeRenderer;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;


import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.geom.AABBox;

import bdv.BigDataViewer;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.*;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.viewer.Source;
import bdv.viewer.state.SourceState;


public class BigDataViewerDataSelector {

	private final BigDataViewer bdv;

	private float HullVolumeDimensions[] = new float[]{100,100,100};

	private AABBox currentGlobalSelection = null;

	private final Collection<IBigDataViewerDataSelectorListener> listeners = new ArrayList<IBigDataViewerDataSelectorListener>();

	public BigDataViewerDataSelector(final BigDataViewer bdv){
		this.bdv = bdv;	
		initListener();
	}

	private void fireAllRegionSelected(final AABBox region){
		for(IBigDataViewerDataSelectorListener l :listeners){
			l.dataRegionSelected(region);
		}
	}

	private void fireDataAvailable(IBigDataViewerDataSelectorListener l, AABBox hullVolume, List<VolumeDataBlock> partialVolumes,int time){
		l.selectedDataAvailable(hullVolume, partialVolumes, time);
	}

	private void fireAllDataAvailable(AABBox hullVolume, List<VolumeDataBlock> partialVolumes,int time){
		for(IBigDataViewerDataSelectorListener l :listeners){
			fireDataAvailable(l, hullVolume, partialVolumes, time);
		}
	}

	/**
	 * prints the selected volume box in the bigDataView
	 */
	private void printBox(){
		if(currentGlobalSelection==null){
			return;
		}
		/*		if(!renderer.getDrawRect().equals(currentGlobalSelection)){
			currentGlobalSelection = null;
			return;
		}
		 */	
		//todo
	}

	/**
	 * init all needed listeners
	 */
	private synchronized void initListener() {

		this.bdv.getViewerFrame().getViewerPanel().getDisplay().addMouseListener(new MouseAdapter() {

			@Override
			public synchronized void mouseClicked(MouseEvent e) {

				//select on double click
				if(e.getClickCount() == 2){
					selectVolumePart(e.getPoint());
				}
			}
		});
	}

	/**
	 * Adds listener
	 * @param l
	 */
	public void addBigDataViewerDataSelectorListener(IBigDataViewerDataSelectorListener l){
		listeners.add(l);
	}
	
	/**
	 * event to select a appropriate part of the volumes
	 * @param p point on the panel
	 */
	public void selectVolumePart(final Point p){
		//test 
		//	p.x = 252;
		//	p.y = 284;

		
		

		final AABBox volumeRectangle = getVolumeRegion(bdv, p, new float[]{HullVolumeDimensions[0]/2f,HullVolumeDimensions[1]/2f,HullVolumeDimensions[2]/2f});
		//System.out.println("area: "+ volumeRectangle);
		
		fireAllRegionSelected(volumeRectangle);

		currentGlobalSelection = volumeRectangle;

		// thread to init data gathering
		Thread collectorThread = new Thread(){
			@Override
			public void run() {
				List<SourceState<?>> sources = bdv.getViewer().getState().getSources();
				ArrayList<VolumeDataBlock> partialVolumes = new ArrayList<VolumeDataBlock>();
				int time =bdv.getViewer().getState().getCurrentTimepoint();
				for(int i =0; i < sources.size(); i++){
					int midmapLevel =0; //bdv.getViewer().getState().getSources().get(i).getSpimSource().getNumMipmapLevels()-1;

					AABBox b = getInnerVolume(bdv, volumeRectangle, midmapLevel, time,i);

					//		System.out.println(p);
					//		System.out.println(b);

					VolumeDataBlock data = getDataBlock(bdv, b, i, midmapLevel);
					partialVolumes.add(data);

					//	System.out.println(data);
					//break;
				}

				fireAllDataAvailable(volumeRectangle, partialVolumes, time);};
		};
		collectorThread.start();
	}

	/**
	 * get the AABB of the queried region
	 * @param bdv data source
	 * @param queryPoint center of the region
	 * @param dimensions size of the region (xyz) 
	 * @return AABB in global space
	 */
	private static AABBox getVolumeRegion(final BigDataViewer bdv, final Point queryPoint, float dimensions[] ){	
		AffineTransform3D transform3D = new AffineTransform3D();
		bdv.getViewer().getState().getViewerTransform(transform3D);
		Matrix4 transformJogl = convertToJoglTransform(transform3D);
		transformJogl.invert();


		//get coord in global space
		float transformer[] = new float[]{queryPoint.x,queryPoint.y,0,1};
		float transformed[] = new float[4];
		transformJogl.multVec(transformer, transformed);

		//wclip
		for(int i = 0; i < 3; i++ ){
			transformed[i]/=transformed[3];
		}

		//build box
		AABBox box = new AABBox(transformed[0]-dimensions[0],transformed[1]-dimensions[1],transformed[2]-dimensions[2],
				transformed[0]+dimensions[0],transformed[1]+dimensions[1],transformed[2]+dimensions[2]);
		return box;
	}

	/**
	 * calculates the inner Volume borders of a certain region 
	 * @param bdv viewer
	 * @param outerVolume outer region
	 * @param midmapLevel current midmap to query
	 * @param time current time to query
	 * @param sorceId it of the source to query
	 * @return AABB of the volumes in volume coordinate system
	 */
	private static AABBox getInnerVolume(final BigDataViewer bdv, final AABBox outerVolume, int midmapLevel, int time, int sourceId){
		//iterate sources
		List<SourceState<?>> sources = bdv.getViewer().getState().getSources();

		Source<?> source = sources.get(sourceId).getSpimSource();

		//get volume trans  
		AffineTransform3D sourceTrans3D = new AffineTransform3D();
		source.getSourceTransform(time, midmapLevel, sourceTrans3D);
		Matrix4 sourceTransJogl = convertToJoglTransform(sourceTrans3D);
		sourceTransJogl.invert();

		//test points for borders since they may overlap
		float[][]minMax={{Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE},
				{Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE}};
		float z = outerVolume.getMinZ();
		for(int i= 0; i < 2; i++ ){
			float y = outerVolume.getMinY();
			for(int j =0; j < 2; j++){
				float x =  outerVolume.getMinX();
				for(int k =0; k < 2; k++){
					float transformer[]={x,y,z,1};
					float transformed[] = new float[4];

					sourceTransJogl.multVec(transformer,transformed);

					//update min max
					for(int m = 0; m < 3; m++){
						minMax[0][m] = Math.min(minMax[0][m], transformed[m]/transformed[3]);
						minMax[1][m] = Math.max(minMax[1][m], transformed[m]/transformed[3]);
					}

					x = outerVolume.getMaxX();
				}
				y = outerVolume.getMaxY();
			}
			z = outerVolume.getMaxZ();
		}

		//get a little offset for surpressing render artefacts of the sparse textures
		float offset = 10f;
		for (int i =0; i < 3; i++){
			minMax[0][i]-=offset;
			minMax[1][i]+=offset;
		}
		return new AABBox(minMax[0], minMax[1]);
	}

	/**
	 * @return the hullVolumeDimensions
	 */
	public float[] getHullVolumeDimensions() {
		return HullVolumeDimensions;
	}

	/**
	 * @param hullVolumeDimensions the hullVolumeDimensions to set
	 */
	public void setHullVolumeDimensions(float[] hullVolumeDimensions) {
		HullVolumeDimensions = hullVolumeDimensions.clone();
	}
}