package bdv.jogl.VolumeRenderer.utils;

/**
 * listen on events of the Volume data manager
 * @author michael
 *
 */
public interface IVolumeDataManagerListener {

	/**
	 * generic callback if some data changed within the data manager
	 */
	public void addedData(Integer i);

	/**
	 * Data values changed in a certain dataset
	 * @param i
	 */
	public void dataUpdated(Integer i);

	/**
	 * Dataset was removed
	 * @param i
	 */
	public void dataRemoved(Integer i);

	/*
	 * A certain dataset changed its enabled/visable flag
	 */
	public void dataEnabled(Integer i, Boolean flag);

}
