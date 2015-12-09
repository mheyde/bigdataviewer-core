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
	 * @param i the data index which was removed
	 */
	private void fireAllRemovedData(Integer i ){
		for(IVolumeDataManagerListener l:listeners){
			l.dataRemoved(i);
		}
	}
	
	/**
	 * Tells all listeners that a certain volume stack was added to the current data set
	 * @param i the data index which was added
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
	 * @return the maximal volume value of the currently stored volume values.
	 */
	public float getGlobalMaxVolumeValue(){
		return globalMaxVolume;
	}

	/**
	 * @return  the set of volume indices which are currently in use,
	 */
	public Set<Integer> getVolumeKeys() {
		return volumes.keySet();
	}

	/**
	 * Returns if a certain volume stack is currently visible/ enabled
	 * @param i data index to check
	 * @return true if the i-th volume was enabled 
	 */
	public boolean isEnabled(int i){
		return enabled.get(i);
	}
	
	/**
	 * Get volume data stack from its id
	 * @param i the data index to get data from
	 * @return the data stack
	 */
	public VolumeDataBlock getVolume(Integer i) {
		return volumes.get(i);
	}

	/**
	 * add a list of volume data stack. Triggers the appropriate listeners after every stack was added for synchronization purposes. 
	 * @param time the time stamp to add data to
	 * @param data the data blocks to add
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
	 * Adds one data block for a time stamp and an id if non was present.
	 * @param i the data index to set the data stack
	 * @param time the time stamp
	 * @param data the data to add
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
	 * @param i the data index which was updated
	 */
	private void fireAllUpdatedData(Integer i) {
		for(IVolumeDataManagerListener listener : listeners){
			listener.dataUpdated(i);
		}
	}
	
	/**
	 * @return the count of the most occuring volume data value over all data stacks. Not the value itself
	 */
	public float getGlobalMaxOccurance() {
		return globalMaxOccurance;
	}

	/**
	 * @return the volume data stacks 
	 */
	public Collection<VolumeDataBlock> getVolumes() {
		return volumes.values();
	}
	
	/**
	 * Removes a certain volume data stack from the storage
	 * @param i the data index to remove
	 */
	public void removeVolumeByIndex(int i) {
		VolumeDataBlock removedData = volumes.remove(i);
		if(removedData != null){
			fireAllRemovedData(i);
		}
	}
	
	/**
	 * Sets the visible / enable flag for a certain volume data stack.
	 * @param i the index to change
	 * @param flag the enable status
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
	 * @param i the data index which was manipulated
	 * @param flag the enable status to set
	 */
	private void fireAllDataEnabled(int i, boolean flag) {
		for(IVolumeDataManagerListener l:listeners){
			l.dataEnabled(i, flag);
		}
		
	}

	/**
	 * Add a listener and calls its data add and update method for each data stack
	 * @param l the listener to add
	 */
	public void addVolumeDataManagerListener(IVolumeDataManagerListener l ){
		listeners.add(l);
		for(int i: volumes.keySet()){
			l.addedData(i);
			l.dataUpdated(i);		
		}
	}

	/**
	 * Get the lowest resolution of the full data set at the current time stamp from the data interface, if needed.
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
	 * Get the lowest resolution of the full data set at the current time stamp from the data interface.
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
	 * @return the lowest volume value from all current stacks
	 */
	public float getGlobalLowestVolumeValue() {
		
		return minGlobalValue;
	}
}
