package bdv.jogl.VolumeRenderer.utils;


import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.getDataBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jogamp.opengl.math.geom.AABBox;

import bdv.BigDataViewer;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;


/**
 * class to store the volume data and unified access
 * @author michael
 *
 */
public class VolumeDataManager {

	private final Map<Integer, VolumeDataBlock> volumes = new HashMap<Integer, VolumeDataBlock>();
	
	private final Map<Integer, Integer> timestamps = new HashMap<Integer, Integer>();
	
	private final Map<Integer, Boolean> enabled =new HashMap<Integer, Boolean>();
	
	private float globalMaxVolume = (float) Math.pow(2, 16)-1;
	
	private int globalMaxOccurance = 0;
	
	private int currentTime = -1;
	
	private final BigDataViewer bdv;
	
	private List<IVolumeDataManagerListener> listeners = new ArrayList<IVolumeDataManagerListener>();

	private float minGlobalValue;
	
	/**
	 * Constructor
	 * @param bdv The data interface to fetch volume data from
	 */
	public VolumeDataManager(final BigDataViewer bdv){
		this.bdv = bdv;
	}

	/**
	 * Tells all listeners that a certain volume stack was removed from the current storage
	 * @param i
	 */
	private void fireAllRemovedData(Integer i ){
		for(IVolumeDataManagerListener l:listeners){
			l.dataRemoved(i);
		}
	}
	
	/**
	 * Tells all listeners that a certain volume stack was added to the current data set
	 * @param i
	 */
	private void fireAllAddedData(Integer i){
		for(IVolumeDataManagerListener l:listeners){
			l.addedData(i); 
		}
	}
	
	/**
	 * update statistic values over all volume stacks
	 */
	private void updateGlobals(){

		globalMaxOccurance = 0;
		minGlobalValue = Float.MAX_VALUE;
		
		for(VolumeDataBlock data: volumes.values()){
			
			globalMaxOccurance = Math.max(globalMaxOccurance, data.getMaxOccurance());
			
			if(data.getValueDistribution().isEmpty()){
				continue;
			}
			//Float cmax =data.getValueDistribution().lastKey();
			Float cmin = data.getValueDistribution().firstKey();
			//globalMaxVolume = Math.max(globalMaxVolume,cmax.floatValue());
			minGlobalValue = Math.max(0.0f,Math.min(minGlobalValue, cmin.floatValue()));
		}
	}
	
	/**
	 * @return the currentTime
	 */
	public int getCurrentTime() {
		return currentTime;
	}

	/**
	 * Returns the maximal volume value of the currently stored volume values.
	 * @return
	 */
	public float getGlobalMaxVolumeValue(){
		return globalMaxVolume;
	}

	/**
	 * Returns the set of volume indices which are currently in use,
	 * @return
	 */
	public Set<Integer> getVolumeKeys() {
		return volumes.keySet();
	}

	/**
	 * Returns if a certain volume stack is currently visible/ enabled
	 * @param i
	 * @return
	 */
	public boolean isEnabled(int i){
		return enabled.get(i);
	}
	
	/**
	 * Get volume data stack from its id
	 * @param i
	 * @return
	 */
	public VolumeDataBlock getVolume(Integer i) {
		return volumes.get(i);
	}

	/**
	 * add a list of volume data stack. Triggers the appropriate listeners after every stack was added for synchronization purposes. 
	 * @param time
	 * @param data
	 */
	public void volumeUpdateTransaction(int time, List<VolumeDataBlock> data){
		List<Boolean> wasPresent = new ArrayList<Boolean>();
		if(time != this.currentTime){
			currentTime = time;
		}	
		
		for(int key =0; key < data.size();key++){
			VolumeDataBlock volume = data.get(key);
			wasPresent.add( volumes.containsKey(key));
		
			volumes.put(key, volume);
			timestamps.put(key, time);
			enabled.put(key, true);
			updateGlobals();
		}
		
		//end of transaction inform all listeners
		for(int key =0; key < data.size();key++){
			if(!wasPresent.get(key)){
				fireAllAddedData(key);
			}
			fireAllUpdatedData(key);
		}
	}
 
	/**
	 * Adds one data block for a timestamp and an id if non was present.
	 * @param i
	 * @param time
	 * @param data
	 */
	public void setVolume(Integer i, int time , VolumeDataBlock data){
		if(time != this.currentTime){
			currentTime = time;
		}
		boolean sameTime = (timestamps.containsKey(i))?(timestamps.get(i) == time):false;
		boolean contained = volumes.containsKey(i);
		if(sameTime){
			if(contained){
				return;
			}
		}
		
		volumes.put(i, data);
		timestamps.put(i, time);
		enabled.put(i, true);
		updateGlobals();
		
		if(!contained){
			fireAllAddedData(i);
		}	
		fireAllUpdatedData(i);
	}
	
	/**
	 * Tells all listeners that a certain data stack updated its data
	 * @param i
	 */
	private void fireAllUpdatedData(Integer i) {
		for(IVolumeDataManagerListener listener : listeners){
			listener.dataUpdated(i);
		}
	}
	
	/**
	 * Return the count of the most occuring volume data value over all data stacks. Not the value itself
	 * @return
	 */
	public float getGlobalMaxOccurance() {
		return globalMaxOccurance;
	}

	/**
	 * Returns the volume data stacks 
	 * @return
	 */
	public Collection<VolumeDataBlock> getVolumes() {
		return volumes.values();
	}
	
	/**
	 * Removes a certain volume data stack from the storage
	 * @param i
	 */
	public void removeVolumeByIndex(int i) {
		VolumeDataBlock removedData = volumes.remove(i);
		if(removedData != null){
			fireAllRemovedData(i);
		}
	}
	
	/**
	 * Sets the visible / enable flag for a certain volume data stack.
	 * @param i
	 * @param flag
	 */
	public void enableVolume(int i , boolean flag){
		if(!enabled.containsKey(i)){
			return;
		}
		enabled.put(i, flag);
		fireAllDataEnabled(i,flag);
	}
	
	/**
	 * Tells all listeners that a certain volume data stack has changed its enable/visible status 
	 * @param i
	 * @param flag
	 */
	private void fireAllDataEnabled(int i, boolean flag) {
		for(IVolumeDataManagerListener l:listeners){
			l.dataEnabled(i, flag);
		}
		
	}

	/**
	 * Add a listener and calls its data add and update method for each data stack
	 * @param l
	 */
	public void addVolumeDataManagerListener(IVolumeDataManagerListener l ){
		listeners.add(l);
		for(int i: volumes.keySet()){
			l.addedData(i);
			l.dataUpdated(i);		
		}
	}

	/**
	 * Get the lowest resolution of the full data set at the current timestamp from the data interface, if needed.
	 */
	public void updateData(){	
		ViewerState state = bdv.getViewer().getState();
		int currentTimepoint = state.getCurrentTimepoint();
		if(getCurrentTime() == currentTimepoint){
			return;
		}
		
		resetVolumeData();
	}
	
	/**
	 * Get the lowest resolution of the full data set at the current timestamp from the data interface.
	 */
	public void resetVolumeData() {
		//mainly for new time points and data not realy for transform
		ViewerState state = bdv.getViewer().getState();
		List<SourceState<?>> sources = state.getSources();
		int currentTimepoint = state.getCurrentTimepoint();
		List<VolumeDataBlock> data = new ArrayList<VolumeDataBlock>();
/*		System.out.println();
		System.out.println();
		System.out.println("Timepoints: "+state.getNumTimePoints());
	*/	
		int i =-1;
		for(SourceState<?> source : sources){
		/*	System.out.println(source.getSpimSource().getNumMipmapLevels());
			for(int k = 0;  k < source.getSpimSource().getNumMipmapLevels();k++){
				long dim[] = new long[3];
				source.getSpimSource().getSource(currentTimepoint, k).dimensions(dim);
				for(int d= 0; d < 3; d++){
					System.out.print(dim[d] +" ");
				}
				System.out.println();
			}*/
			
			i++;

			//block transform
			int midMapLevel = source.getSpimSource().getNumMipmapLevels()-1;
		    data.add( getDataBlock(bdv,new AABBox(new float[]{0,0,0,0},new float[]{Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}),i,midMapLevel));
			
		
		}
		volumeUpdateTransaction(currentTimepoint, data);
	}

	/**
	 * Returns the lowest volume value from all current stacks
	 * @return
	 */
	public float getGlobalLowestVolumeValue() {
		
		return minGlobalValue;
	}
}
