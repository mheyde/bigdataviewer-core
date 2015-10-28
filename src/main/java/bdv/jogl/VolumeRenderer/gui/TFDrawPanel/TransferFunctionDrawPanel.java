package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.gui.TFDrawPanel.Axis.AxisType;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;

/**
 * Frames the render panel and adds scales and options
 * @author michael
 *
 */
public class TransferFunctionDrawPanel extends JPanel {

	/**
	 *	default serial id 
	 */
	private static final long serialVersionUID = 1L;
	
	private final TransferFunctionRenderPanel1D renderPanel;

	private final GridBagLayout raaalayout = new GridBagLayout();
	
	private final BoxLayout mainLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
	
	private final Axis yTauAxis = new Axis("Tau",AxisType.YAXIS);
	
	private final Axis yDistributionAxis = new Axis("Count", AxisType.YAXIS);
	
	private final Axis xAxis = new Axis("Volume values",AxisType.XAXIS);
	
	private final VolumeDataManager dataManager;
	
	private final JPanel renderAndAxisArea= new JPanel();
	
	private final JScrollPane scrollArea = new JScrollPane(renderAndAxisArea);
	


	private final JPanel zoomXPanel = new JPanel();
	
	private final JPanel zoomYPanel = new JPanel();
	
	private final JSpinner zoomSpinnerx  = new JSpinner(new SpinnerNumberModel(1,1,1000,1));
	
	private final JSpinner zoomSpinnery  = new JSpinner(new SpinnerNumberModel(1,1,1000,1));
	
	private JCheckBox logarithmicOccuranceCheck = new JCheckBox("Logarithmic distribution");
	
	/**
	 * Constructor to create the render image
	 * @param tf
	 * @param dataManager
	 */
	public TransferFunctionDrawPanel(final TransferFunction1D tf, final VolumeDataManager dataManager){
		this.dataManager = dataManager;
		logarithmicOccuranceCheck.setSelected(true);
		renderPanel = new TransferFunctionRenderPanel1D(tf, dataManager);
		renderPanel.setLogscaleDistribution(logarithmicOccuranceCheck.isSelected());
		initUI();
		initListener();
	}
	
	private void addComponent(JComponent c){
		c.setAlignmentX(LEFT_ALIGNMENT);
		add(c);
	}
	
	
	private void initUI(){
		setLayout(mainLayout);
		
		renderAndAxisArea.setLayout(raaalayout);
		
		yTauAxis.setLeftAxis(true);
		//render area + axis
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx =0;
		c.gridy = 0;
		renderAndAxisArea.add(yTauAxis,c);
		
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx =2;
		c.gridy = 0;
		renderAndAxisArea.add(yDistributionAxis,c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx =1;
		c.gridy = 1;
		renderAndAxisArea.add(xAxis,c);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx =1;
		c.gridy = 0;
		renderAndAxisArea.add(renderPanel,c);
		//renderAndAxisArea.setPreferredSize(new Dimension(2000,180));
		
		
		scrollArea.setPreferredSize(new Dimension(700,200));
		
		zoomXPanel.setLayout(new BoxLayout(zoomXPanel, BoxLayout.X_AXIS));
		zoomXPanel.add(new JLabel("Zoom x Axis: ") );
		zoomSpinnerx.setPreferredSize(zoomSpinnerx.getMinimumSize());
		zoomSpinnerx.setMaximumSize(zoomSpinnerx.getMinimumSize());
		zoomXPanel.add(zoomSpinnerx);
		
		zoomYPanel.setLayout(new BoxLayout(zoomYPanel, BoxLayout.X_AXIS));
		zoomYPanel.add(new JLabel("Zoom y Axis: ") );
		zoomSpinnery.setPreferredSize(zoomSpinnery.getMinimumSize());
		zoomSpinnery.setMaximumSize(zoomSpinnery.getMinimumSize());
		zoomYPanel.add(zoomSpinnery);
		
		addComponent(scrollArea);
		addComponent(zoomXPanel);
		addComponent(zoomYPanel);
		
		//controls
		addComponent(logarithmicOccuranceCheck);
	}

	@Override
	public void paint(Graphics g) {
		//update xAxis TODO
		xAxis.setMax(dataManager.getGlobalMaxVolumeValue());
		yDistributionAxis.setMax(dataManager.getGlobalMaxOccurance());
		super.paint(g);
	};
	
	private void updateXScale(){
		renderPanel.setSize(renderPanel.getMinimumSize().width * ((Number)zoomSpinnerx.getValue()).intValue(), renderPanel.getHeight());
		renderPanel.setPreferredSize(renderPanel.getSize());
		scrollArea.updateUI();
	}

	private void updateYScale(){
		renderPanel.setSize(renderPanel.getWidth(), renderPanel.getMinimumSize().height * ((Number) zoomSpinnery.getValue()).intValue());
		renderPanel.setPreferredSize(renderPanel.getSize());
		scrollArea.updateUI();
	}
	
	private void initListener(){
		logarithmicOccuranceCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				renderPanel.setLogscaleDistribution(logarithmicOccuranceCheck.isSelected());			
				
			}
		});
		
		updateXScale();
		zoomSpinnerx.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				updateXScale();
			}
		});
		
		updateYScale();
		zoomSpinnery.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				updateYScale();
			}
		});
	}
}
