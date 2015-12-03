package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JPanel;

import bdv.jogl.VolumeRenderer.transferfunction.TransferFunction1D;
import bdv.jogl.VolumeRenderer.transferfunction.TransferFunctionAdapter;
import bdv.jogl.VolumeRenderer.utils.IVolumeDataManagerListener;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import static bdv.jogl.VolumeRenderer.transferfunction.TransferFunction1D.calculateDrawPoint;
import static bdv.jogl.VolumeRenderer.utils.WindowUtils.*;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.getColorOfVolume;

/**
 * Transfer function interaction panel similar to paraview
 * @author michael
 *
 */
public class TransferFunctionRenderPanel1D extends JPanel {

	/**
	 * default version
	 */
	private static final long serialVersionUID = 1L;
	
	private VolumeDataManager volumeDataManager = null;
	
	private final TransferFunctionContexMenu contextMenue;

	private final TransferFunctionPointInteractor pointInteractor;

	private TransferFunction1D transferFunction;
	
	private final int pointRadius = 10;

	private boolean logscale = true;

	/**
	 * Adds all control listeners
	 */
	private void addControls(){
		addMouseListener(contextMenue.getMouseListener());

		addMouseMotionListener(pointInteractor.getMouseMotionListener());

		addMouseListener(pointInteractor.getMouseListener());
	}
	
	/**
	 * constructor
	 */
	public TransferFunctionRenderPanel1D(final TransferFunction1D tf, final VolumeDataManager dataManager){

		initWindow();
		setTransferFunction(tf);
		pointInteractor = new TransferFunctionPointInteractor(this);
		setVolumeDataManager(dataManager);
		
		//resizeHandler = new TransferFunctionWindowResizeHandler(getSize(),transferFunction);
		contextMenue = new TransferFunctionContexMenu(this);
		addControls();
	}

	/**
	 * Initializes UI 
	 */
	private void initWindow() {
		setSize(640, 100);
		setPreferredSize(getSize());
		setMinimumSize(getSize());
		//setMaximumSize(getSize());
	}

	
	/**
	 * @return the transferFunction
	 */
	public TransferFunction1D getTransferFunction() {
		return transferFunction;
	}

	/**
	 * @param transferFunction the transferFunction to set
	 */
	public void setTransferFunction(TransferFunction1D transferFunction) {
		this.transferFunction = transferFunction;
		this.transferFunction.addTransferFunctionListener(new TransferFunctionAdapter() {
			
			@Override
			public void functionPointChanged(TransferFunction1D transferFunction) {
				repaint();
			}
		});
	}

	/**
	 * Returns true if the volume distribution is drawn logarithmic and false if not
	 * @return
	 */
	public boolean isLogscaleDistribution() {
		return logscale;
	}

	/**
	 * Defines whether the distributions of volume values in the panel should be drawn in log scale
	 * @param logscale
	 */
	public void setLogscaleDistribution(boolean logscale) {
		this.logscale = logscale;
		repaint();
	}

	/**
	 * Returns the data manager which is currently in use
	 * @return
	 */
	public VolumeDataManager getVolumeDataManager() {
		return volumeDataManager;
	}

	/**
	 * Set a new data manager to use
	 * @param volumeDataManager
	 */
	public void setVolumeDataManager(VolumeDataManager volumeDataManager) {
		this.volumeDataManager = volumeDataManager;
		volumeDataManager.addVolumeDataManagerListener(new IVolumeDataManagerListener() {
			
			// Repaint on data update all data actions
			@Override
			public void dataUpdated(Integer i) {
				repaint();
			}
			
			@Override
			public void addedData(Integer i) {
				repaint();	
			}
			
			@Override
			public void dataRemoved(Integer i) {
				repaint();
			}
			
			@Override
			public void dataEnabled(Integer i, Boolean flag) {
				repaint();	
			}
		});
	}

	/**
	 * Paints the color gradients
	 * @param g
	 */
	private void paintSkala(Graphics g){
		//paint gradient image
		//error check
		TreeMap<Point2D.Float, Color> colors = transferFunction.getColors();
		if(colors.size() < 2){
			return;
		}

		//get painter
		Graphics2D painter = (Graphics2D) g;


		Point2D.Float latestPoint = colors.firstKey();
		for(Point2D.Float currentPoint: colors.keySet()){
			//skip first iteration
			if(currentPoint.equals( latestPoint)){
				continue;
			}
			Point beginGradient = transformWindowNormalSpace(calculateDrawPoint(latestPoint,transferFunction,getSize()), getSize());
			Point endGradient = transformWindowNormalSpace(calculateDrawPoint(currentPoint,transferFunction,getSize()), getSize());
			
			beginGradient.setLocation(beginGradient.x, 0);
			endGradient.setLocation(endGradient.x, 0);
			//gradient
			GradientPaint gradient = new GradientPaint(
					beginGradient, colors.get(latestPoint),
					endGradient, colors.get(currentPoint));


			//draw gradient
			painter.setPaint(gradient);
			painter.fillRect(beginGradient.x, 0, 
					endGradient.x, getHeight());
			latestPoint = currentPoint;
		}
	}
	
	/**
	 * Draw a transfer function point 
	 * @param painter
	 * @param point
	 */
	private void drawPointIcon(Graphics2D painter, final Point point){

		painter.setStroke(new BasicStroke(3));
		painter.drawOval(point.x-pointRadius, point.y-pointRadius, 
				pointRadius*2, pointRadius*2);
	}

	/**
	 * Draw line segments representing the transfer function
	 * @param g
	 */
	private void paintLines(Graphics g){
		TreeMap<Point2D.Float,Color> functionPoints = transferFunction.getColors();
		if(functionPoints.size() < 2){
			return;
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		//draw line and points
		Point2D.Float latestRenderedPoint = functionPoints.firstKey();
		for( Point2D.Float currentPoint: functionPoints.keySet()){
		
			//skip first point to get a valid line
			if(!currentPoint.equals( latestRenderedPoint) ){
				Point a = transformWindowNormalSpace(calculateDrawPoint(latestRenderedPoint,transferFunction,getSize()), getSize());
				Point b = transformWindowNormalSpace(calculateDrawPoint(currentPoint,transferFunction,getSize()), getSize());
				
				//print line
				g2d.setStroke(new BasicStroke(5));
				g2d.drawLine(a.x, 
						a.y,
						b.x, 
						b.y);
			}
			latestRenderedPoint = currentPoint;
		}
	}

	/**
	 * Draws the control points of the transfer function 
	 * @param g
	 */
	private void paintPoints(Graphics g){
		//print points	
		Graphics2D g2d = (Graphics2D) g;	
		
		for( Point2D.Float currentPoint: transferFunction.getColors().keySet()){
			
			//highlight currently selected (dragged) point
			if(currentPoint.equals(pointInteractor.getSelectedPoint())){
				g2d.setColor(Color.gray);
			}
			
			Point drawPoint = transformWindowNormalSpace(calculateDrawPoint(currentPoint,transferFunction,getSize()), getSize());
			drawPointIcon(g2d, drawPoint);
			g2d.setColor(Color.black);
		}	
	}

	/**
	 * Paint the value distribution on the panel
	 * @param g
	 */
	private void paintDistributions(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;	
		Set<Integer> volumeKeys = volumeDataManager.getVolumeKeys();
		
		float maxYValue =volumeDataManager.getGlobalMaxOccurance();
		float maxXValue = volumeDataManager.getGlobalMaxVolumeValue();
		if(logscale){
			maxYValue = (float) Math.log10(maxYValue);
		}
		float xyScale [] = new float[]{
				(float)getWidth()/maxXValue,
				(float)getHeight()/maxYValue 
		};
		
		//iterate volumes to draw 
		for(Integer i : volumeKeys){
			Color volumeColor = getColorOfVolume(i);
			VolumeDataBlock data = volumeDataManager.getVolume(i);
			TreeMap<Float,Integer> distribution = data.getValueDistribution();
					
			//sample
			for(Float volume: distribution.keySet()){
				Integer occurance = distribution.get(volume);
				float coord[]={volume,occurance};
				if(logscale ){
					coord[1] = (float) Math.log10(coord[1]); 
				}
					for(int j =0; j< coord.length;j++){
						coord[j]*=xyScale[j];
					}
				 
				Point drawPoint = new Point((int)coord[0],(int)coord[1]);
				drawPoint = transformWindowNormalSpace(drawPoint, getSize());
				g2d.setColor(volumeColor);
				g2d.drawRect(drawPoint.x, drawPoint.y, 1,1);
			}
		}
	} 
	
	/**
	 * Draw panel as overlapping layers: lowest Colors then Volume distribution then Function line then Controle points 
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		paintSkala(g);
		
		paintDistributions(g);
		
		paintLines(g);
		
		paintPoints(g);
	}
}
