package bdv.jogl.VolumeRenderer.transferfunction;


/**
 * empty implementation of the listener to not waste code
 * @author michael
 *
 */
public abstract class TransferFunctionAdapter implements
		TransferFunctionListener {

	@Override
	public void functionPointChanged(TransferFunction1D transferFunction) {

	}

	@Override
	public void classifierChanged(TransferFunction1D transferFunction1D) {

	}
}
