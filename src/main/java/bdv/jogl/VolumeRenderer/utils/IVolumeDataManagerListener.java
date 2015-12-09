package bdv.jogl.VolumeRenderer.utils;

/**
 * listen on events of the Volume data manager
 * @author michael
 *
 */
public interface IVolumeDataManagerListener {

	/**
	 * generic callback if some data changed within the data manager
	 * @param i the index of the data that was added
	 */
	public void addedData(Integer i);

	/**
	 * Data values changed in a certain dataset
	 * @param i the index of the data that was updated
	 */
	public void dataUpdated(Integer i);

	/**
	 * Dataset was removed
	 * @param i the index of the data that was removed
	 */
	public void dataRemoved(Integer i);

	/**
	 * A certain dataset changed its enabled/visable flag
	 * @param i the index of the data that changed
	 * @param flag the new enable flag
	 */
	public void dataEnabled(Integer i, Boolean flag);

}
