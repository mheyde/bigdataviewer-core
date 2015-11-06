package bdv.jogl.VolumeRenderer.TransferFunctions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.jogamp.opengl.math.VectorUtil;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IFunction;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.ITransferFunctionSampler;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.PreIntegrationSampler;

/**
 * store data for 1D Transfer functions
 * @author michael
 *
 */
public class TransferFunction1D {

	private List<TransferFunctionListener> transferFunctionListeners = new ArrayList<TransferFunctionListener>();

	private ITransferFunctionSampler sampler; 
	
	private final int maxFunctionPointSsmples = 2048;

	//order points first by x then by y
	private final Comparator<Point2D.Float> pointOrderXOperator = new Comparator<Point2D.Float>() {

		@Override
		public int compare(Point2D.Float a, Point2D.Float b) {
			//same x
			if(a.x == b.x){
				return (int)Math.signum(a.y-b.y);
			}
			return (int)Math.signum(a.x-b.x);
		}
	};

	private Point2D.Float maxOrdinates;

	private final Point2D.Float minOrdinates = new Point2D.Float(0,0);

	private final TreeMap<Point2D.Float,Color> colors = new TreeMap<Point2D.Float, Color>(pointOrderXOperator);

	private float maxVolumeValue;

	private boolean isPointValid(Point2D.Float point){
		//min
		if(point.x < minOrdinates.x || point.y < minOrdinates.y ){
			return false;
		}
		
		//max
		if(point.x > maxOrdinates.x || point.y > maxOrdinates.y ){
			return false;
		}
		return true;
	}
	
	private void fireSamplerChangedEventAll(){
		for(TransferFunctionListener listener: transferFunctionListeners){
			fireSamplerChangedEvent(listener);
		}
	}

	private void fireSamplerChangedEvent(final TransferFunctionListener l){
		l.samplerChanged(this);
	}

	/**
	 * Calls all event methods on listener using the current data state
	 * @param listener
	 */
	private void fireColorChangedEvent(final TransferFunctionListener listener){

		listener.colorChanged(this);
	}

	private void fireColorChangedEventAll(){

		for(TransferFunctionListener listener:transferFunctionListeners){
			fireColorChangedEvent(listener);
		}
	}


	public void resetColors(){
		colors.clear();
		colors.put(new Point2D.Float(minOrdinates.x, minOrdinates.y), Color.BLUE);
		colors.put(new Point2D.Float(maxOrdinates.x/2f,maxOrdinates.y/2f), Color.WHITE);
		colors.put(new Point2D.Float(maxOrdinates.x,maxOrdinates.y),Color.RED);

		fireColorChangedEventAll();
	}


	private void init( Point2D.Float maxOrdinates){
		this.maxOrdinates = maxOrdinates;

		resetColors();
		setSampler( new PreIntegrationSampler());
	}

	public TransferFunction1D(float maxVolume,  float maxTau) {
		Double maxV = Math.ceil(maxVolume);
		Double maxT = Math.ceil(maxTau);
		init(new Point2D.Float(maxV.intValue(),maxT.intValue()));
	}

	public TransferFunction1D(){
		init(new Point2D.Float(256,1.f));
	}

	/**
	 * removes a color point if it is contained and not the first or last point.
	 * @param pos
	 */
	public void removeColor(Point2D.Float pos){
		if(!colors.containsKey(pos)){
			return;
		}
		if(pos.equals(colors.firstKey()) || pos.equals(colors.lastKey())){
			return;
		}
		colors.remove(pos);
	}

	/**
	 * @return the colors
	 */
	public TreeMap<Point2D.Float, Color> getColors() {
		return new TreeMap<Point2D.Float, Color>(colors);
	}

	/**
	 * Sets the color of a certain point in the panel.
	 * @param point Position on the panel area.
	 * @param color Color to be set.
	 */
	public void setColor(final Point2D.Float point, final Color color){
		if(!isPointValid(point)){
			throw new IndexOutOfBoundsException("Point: "+point+" was not in tf space");
		}
		
		colors.put(point, color);

		fireColorChangedEventAll();
	}


	/**
	 * Adds a listener to the transfer function panel
	 * @param listener
	 */
	public void addTransferFunctionListener(final TransferFunctionListener listener){
		transferFunctionListeners.add(listener);

		fireColorChangedEvent(listener);
	}


	/**
	 * @return the maxOrdinates
	 */
	public Point2D.Float getMaxOrdinates() {
		return maxOrdinates;
	}

	/**
	 * @param maxOrdinates the maxOrdinates to set
	 */
	public void setMaxOrdinates(Point2D.Float maxOrdinates) {
		maxVolumeValue = maxOrdinates.x;
		maxOrdinates.x  = (maxOrdinates.x > maxFunctionPointSsmples)?maxFunctionPointSsmples:maxOrdinates.x;
			
		Point2D.Float oldMax = new Point2D.Float(this.maxOrdinates.x,this.maxOrdinates.y);

		this.maxOrdinates = maxOrdinates;

		rescale(oldMax);

		fireColorChangedEventAll();
	}

	/**
	 * rescales the transfer function from 0 to max ordinate in each dim 
	 */
	private void rescale(Point2D.Float oldMax){
		float [] scaleFactors = {(float)maxOrdinates.x/(float)oldMax.x, 
				(float)maxOrdinates.y/(float)oldMax.y };

		//scale color points
		TreeMap<Point2D.Float, Color> newColorMap = new TreeMap<Point2D.Float, Color>(pointOrderXOperator);
		for(Point2D.Float position:colors.keySet()){
			float newX = scaleFactors[0]*(float)position.getX();
			float newY = scaleFactors[1]*(float)position.getY();
			Color color = colors.get(position);
			newColorMap.put(new Point2D.Float(newX,newY),color);
		}
		colors.clear();
		colors.putAll(newColorMap);

	
	}

	private Color getColorComponent(Point2D.Float index){
		float [] result = {0,0,0};


		//get RGB
		Point2D.Float nextIndex = colors.ceilingKey(index);
		if(nextIndex == null){
			return colors.lastEntry().getValue();
		}

		if(nextIndex == index){
			return colors.get(index);
		}
		Point2D.Float previousIndex = colors.lowerKey(index);
		float colorDiff = nextIndex.x-previousIndex.x;
		float colorOffset = index.x - previousIndex.x;

		float[] colorPrev ={0,0,0};
		float[] colorNext ={0,0,0};

		colors.get(previousIndex).getColorComponents(colorPrev);
		colors.get(nextIndex).getColorComponents(colorNext);

		float []tmpColor= {0,0,0};
		VectorUtil.subVec3(tmpColor, colorNext, colorPrev);
		VectorUtil.scaleVec3(tmpColor,tmpColor,colorOffset/colorDiff);
		VectorUtil.addVec3(result, colorPrev,tmpColor );

		return new Color(result[0],result[1],result[2],0);
	}


	private float getNormalizedAlphaValue(float unNormalizedValue){
		float normFactor = 1f/ (float)getMaxOrdinates().y;

		float calculatedValue =  unNormalizedValue*normFactor;

		return Math.min(1, Math.max(calculatedValue, 0));

	}

	private float getAlpha (Point2D.Float index){
		//get alpha
		Point2D.Float nextIndex = colors.ceilingKey(index);
		float nextAlpha = getNormalizedAlphaValue(nextIndex.y);

		if(nextIndex.x == index.x){
			return nextAlpha;
		}
		Point2D.Float previousIndex = colors.lowerKey(index);
		float prevAlpha = getNormalizedAlphaValue(previousIndex.y);

		float colorDiff = nextIndex.x-previousIndex.x;
		float colorOffset = index.x - previousIndex.x;

		float m = (nextAlpha - prevAlpha)/colorDiff;
		return m* colorOffset + prevAlpha;
	} 

	private Color getColorForXOrdinateInObjectTransferSpace(Point2D.Float index){

		float [] result = {0,0,0,0};

		getColorComponent(index).getColorComponents(result);

		result[3] = getAlpha(index); 
		Color resultColor = null ;
		try {
			resultColor = new Color(result[0],result[1],result[2],result[3]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultColor;
	}

	/**
	 * @return the with alpha values
	 */
	public final TreeMap<Integer, Color> getTexturColor() {		
		TreeMap<Integer, Color> returnColors = new TreeMap<Integer, Color>();


		Color currentColor = null;
		//get colors from gradient
		for(Point2D.Float index : colors.keySet()){

			if(returnColors.containsKey((int)index.x)){
				continue;
			}

			currentColor = getColorForXOrdinateInObjectTransferSpace(index);
			if(currentColor == null){
				continue;
			}

			//TODO
			returnColors.put((int)index.x, currentColor);
		}

		//get colors from line
		for(Point2D.Float index : colors.keySet()){

			if(returnColors.containsKey((int)index.x)){
				continue;
			}

			currentColor = getColorForXOrdinateInObjectTransferSpace(index);
			if(currentColor == null){
				continue;
			}

			//TODO
			returnColors.put((int)index.x, currentColor);
		}

		return returnColors;
	}

	/**
	 * updates a color point
	 * @param oldPoint
	 * @param newPoint
	 */
	public void moveColor(Point2D.Float oldPoint, Point2D.Float newPoint) {
		
		
		Color color = colors.get(oldPoint);
		if(color == null){
			return;
		}
		
		if(!isPointValid(newPoint)){
			throw new IndexOutOfBoundsException("Point: "+newPoint+" was not in tf space");
		}
		//last and first point must not be altered in x
		if(oldPoint.equals(colors.firstKey()) || oldPoint.equals(colors.lastKey())){
			newPoint.x = oldPoint.x;
		}
		
		colors.remove(oldPoint);
		colors.put(newPoint, color);
		fireColorChangedEventAll();
	}



	public IFunction getTransferFunctionShaderCode(){
		return sampler.getShaderCode();
	}

	/**
	 * @return the sampler
	 */
	public ITransferFunctionSampler getSampler() {
		return sampler;
	}

	/**
	 * @param sampler the sampler to set
	 */
	public void setSampler(ITransferFunctionSampler sampler) {
		this.sampler = sampler;
		fireSamplerChangedEventAll();
	}

	/**
	 * Calculates the draw point of a transfer function  coordinate in window space
	 * @param transferFunctionPoint
	 * @param transferFunction
	 * @param drawAreaSize
	 * @return
	 */
	public static Point calculateDrawPoint(Point2D.Float transferFunctionPoint, 
			TransferFunction1D transferFunction,
			Dimension drawAreaSize){
		float xyScale[] = {(float)(drawAreaSize.getWidth()-1)/ transferFunction.getMaxOrdinates().x,
				(float)(drawAreaSize.getHeight()-1)/transferFunction.getMaxOrdinates().y};
		Point drawPoint = new Point((int)Math.round(transferFunctionPoint.getX() * xyScale[0]),
				(int)Math.round(transferFunctionPoint.getY() * xyScale[1]));
		return drawPoint;
	}

	/**
	 * Calculates the transfer function point from a given window space point 
	 * @param windowSpacePoint
	 * @param transferFunction
	 * @param drawAreaSize
	 * @return
	 */
	public static Point2D.Float calculateTransferFunctionPoint(Point windowSpacePoint, 
			TransferFunction1D transferFunction,
			Dimension drawAreaSize){
		float xyScale[] = {transferFunction.getMaxOrdinates().x/(float)(drawAreaSize.getWidth()-1),
				transferFunction.getMaxOrdinates().y/(float)(drawAreaSize.getHeight()-1)};
		Point2D.Float functionPoint = new Point2D.Float((float)windowSpacePoint.getX() * xyScale[0],
				(float)windowSpacePoint.getY() * xyScale[1]);
		return functionPoint;
	}
	
	
	public Point2D.Float getNearestValidPoint(final Point2D.Float p, float maxDist){
		Point2D.Float query = null;
		TreeMap<Point2D.Float, Color> colors = getColors();
		Point2D.Float upperPoint = colors.higherKey(p);
		Point2D.Float lowerPoint = colors.lowerKey(p);
		
		float dists[] = {Float.MAX_VALUE,Float.MAX_VALUE};
		//valid points ?
		if(lowerPoint != null ){
			dists[0]= Math.min(dists[0],(float)p.distance(lowerPoint));
		}
		if(upperPoint != null ){
			dists[1]= Math.min(dists[1],(float)p.distance(upperPoint));
		}
		
		//min point ? 
		if(dists[0] < dists[1]){
			if(dists[0] <= maxDist){
				query = lowerPoint;
			}
		}else{
			if(dists[1] <= maxDist){
				query = upperPoint;
			}
		}
		
		return query;
	}
	
	public float getDataVolumeValue(float tfVolumeValue){
		return (maxVolumeValue/maxOrdinates.x)*tfVolumeValue;
	}
	
	public float getTransferFunctionVolumeValue(float dataVolumeValue){
		return (maxOrdinates.x/maxVolumeValue)*dataVolumeValue;
	}
}
