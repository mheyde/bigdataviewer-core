package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

public class Axis extends JPanel {

	/**
	 * default id
	 */
	private static final long serialVersionUID = 1L;
	
	public static enum AxisType {
		XAXIS,
		YAXIS
	};
	
	private int vMargin = 5;
	
	private final AxisType type;
	
	private final String axisName;
	
	private float min = 0;
	
	private float max = 1;
	
	private boolean leftAxis = false;
	
	private String minAxisString = ""+min;
	
	private String maxAxisString = ""+max;
	
	public Axis(String name, AxisType type){
		this.axisName = name;
		this.type = type;
		updateToolTip();
		updateSize();

	}
	
	private void updateToolTip(){
		setToolTipText(axisName+ " values form: "+min+ " to "+ max);
	}
	
	private void updateSize(){
		FontMetrics metrics = getFontMetrics(getFont());
		Dimension minSize = new Dimension();
		if(type == AxisType.XAXIS){
			minSize = getMinimumSize();
			minSize.setSize(minSize.width, metrics.getHeight()+vMargin);
		}else{
			int widthMax = metrics.stringWidth(maxAxisString);
			int widthMin = metrics.stringWidth(minAxisString);
			minSize = new Dimension(Math.max(widthMax, widthMin), getMinimumSize().height);
		}
		setMinimumSize(minSize);
	
		Dimension currentSize = new Dimension(Math.max(getWidth(), minSize.width), Math.max(getHeight(), minSize.height));
		setSize(currentSize);
		setPreferredSize(currentSize);
		repaint();
	}
	
	private void drawMinMax(Graphics2D g){
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int maxx=0,maxy=0;
		int minx=0,miny=0;

		minx = 0;
		miny = getHeight()-1;
		
		if(type == AxisType.XAXIS){
			maxx = getWidth()-1 - metrics.stringWidth(maxAxisString);
			maxy = metrics.getHeight();
		}else{
			maxx = 0;
			maxy = 0 + metrics.getHeight();
		}
		
		//draw min in lower area
		g.drawChars(minAxisString.toCharArray(), 0,  minAxisString.length(), minx, miny);
		g.drawChars(maxAxisString.toCharArray(), 0,  maxAxisString.length(), maxx, maxy);
	}
	
	public void setMax(float max) {
		this.max = max;
		maxAxisString = ""+max;
		updateToolTip();
		updateSize();
	}
	
	public void setMin(float min) {
		this.min = min;
		minAxisString = ""+min;
		updateToolTip();
		updateSize();
	}
	
	public boolean isLeftAxis() {
		return leftAxis;
	}

	public void setLeftAxis(boolean leftAxis) {
		this.leftAxis = leftAxis;
	}

	private void drawName(Graphics2D g2) {
		FontMetrics fontMetrics = getFontMetrics(getFont());
		int strWidth = fontMetrics.stringWidth(axisName);
		
		if(type == AxisType.XAXIS){
			g2.drawChars(axisName.toCharArray(), 0, axisName.length(), getWidth()/2 - strWidth/2, getHeight()-1);
		}else{
			
			AffineTransform rot = new AffineTransform();
			//foot of text is at axis
			if(isLeftAxis()){
				rot.setToRotation(Math.toRadians(270));
				g2.setTransform(rot);
				g2.drawChars(axisName.toCharArray(), 0,axisName.length(),-getLocation().y -getHeight()/2- strWidth/2 ,getLocation().x +getWidth()- vMargin);
				
			}else{
				rot.setToRotation(Math.toRadians(90));
				g2.setTransform(rot);
				g2.drawChars(axisName.toCharArray(), 0,axisName.length(), getHeight()/2 - strWidth/2, -vMargin);
				
			}
			//reset painter
			rot.setToIdentity();
			g2.setTransform(rot);
		}
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setFont(getFont());
		
		drawMinMax(g2);
		
		drawName(g2);
	}


}
