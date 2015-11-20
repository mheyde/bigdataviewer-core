package bdv.jogl.VolumeRenderer.gui;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bdv.jogl.VolumeRenderer.Scene.VolumeDataScene;
import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.volumeninterpreter.IsoSurfaceVolumeInterpreter;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.volumeninterpreter.MaximumIntensityProjectionInterpreter;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.volumeninterpreter.TransparentVolumeinterpreter;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.PreIntegrationSampler;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.RegularSampler;
import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;
import bdv.jogl.VolumeRenderer.gui.TFDataPanel.TransferFunctionDataPanel;
import bdv.jogl.VolumeRenderer.gui.TFDrawPanel.TransferFunctionDrawPanel;
import bdv.jogl.VolumeRenderer.gui.TFDrawPanel.VolumeLegend;
import bdv.jogl.VolumeRenderer.gui.VDataAccumulationPanel.AccumulatorManager;
import bdv.jogl.VolumeRenderer.gui.VDataAccumulationPanel.VolumeDataAccumulatorPanel;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManagerAdapter;
import static bdv.jogl.VolumeRenderer.utils.WindowUtils.aligneLeft;

/**
 * Window for providing the controls
 * @author michael
 *
 */
public class SceneControlsWindow extends JFrame {

	/**
	 * default version
	 */
	private static final long serialVersionUID = 1L;

	private Point2D.Float addedTransfeFunctionPoint = null; 
	
	private TransferFunctionDrawPanel tfpanel = null;

	private final JPanel mainPanel  = new JPanel();

	private TransferFunctionDataPanel tfDataPanel = null;

	private TransferFunction1D transferFunction;

	private VolumeDataAccumulatorPanel aggregationPanel;

	private JCheckBox usePreIntegration = new JCheckBox("Use pre-integration",false);

	private JCheckBox advancedCheck = new JCheckBox("Advanced configurations",false);

	private JButton backgroundColorButton = new JButton("");

	private JPanel backgroundPanel = new JPanel();

	private final VolumeDataManager dataManager;

	private final JCheckBox rectBorderCheck = new JCheckBox("Show volume Borders",false);

	private final MultiVolumeRenderer renderer;

	private final VolumeDataScene scene;

	private final GLWindow drawWindow;

	//interpreter panels

	private final JPanel isoPanel = new JPanel();
	
	private final IsoSurfaceConfigurations isoMenu;

	private final JPanel volumeInterpreterPanel = new JPanel(); 

	private final ButtonGroup volumenInterpreterGroup = new ButtonGroup();

	private final JRadioButton emissionsAbsorbationRadioButton = new JRadioButton("Emission absorbation");

	private final JRadioButton isoRadioButton = new JRadioButton("Isosurface");

	private final JRadioButton maximumIntensityProjectionRadioButton = new JRadioButton("Maximum intensity projection");

	private final JCheckBox showSlice = new JCheckBox("Show slice in 3D View", true);

	private final JPanel samplePanel = new JPanel();

	private final JSpinner sampleSpinner = new JSpinner(new SpinnerNumberModel(256, 1, 10000, 1));

	//private final JCheckBox useGradient = new JCheckBox("Use gradients as values",false);

	private final JPanel transferFunktionEditorPanel = new JPanel();
	
	private final JPanel sceneConfigurationPanel = new JPanel();
	
	private final DetailViewConfiguration detailViewConfig = new DetailViewConfiguration();
	
	private final VolumeLegend legend;
	
	private final JPanel shaderElementPanel = new JPanel();
	
	private final JPanel interactionToolPanel = new JPanel(); 
	
	private final JCheckBox downSampleCheckBox = new JCheckBox("Active motion downsampling",true);
	
	private final JSpinner downSampleSpinner = new JSpinner(new SpinnerNumberModel(64,16,1000, 1));
	
	private final JButton benchButton = new JButton("start Benchmark");
	
	/**
	 * Constructor
	 * @param tf
	 * @param agm
	 * @param dataManager
	 * @param mvr
	 * @param win
	 * @param scene
	 */
	public SceneControlsWindow(
			final TransferFunction1D tf,
			final AccumulatorManager agm, 
			final VolumeDataManager dataManager, 
			final MultiVolumeRenderer mvr, 
			final GLWindow win,
			final VolumeDataScene scene){
		this.scene = scene;
		this.drawWindow = win;
		this.renderer = mvr;
		transferFunction = tf;
		this.dataManager = dataManager;
		this.legend = new VolumeLegend(dataManager);
		this.isoMenu = new IsoSurfaceConfigurations(drawWindow, renderer);
		createTFWindow(tf,agm,dataManager);
	}

	/**
	 * @return the reset button instance for defining listeners on it
	 */
	public JButton getResetButton(){
		return detailViewConfig.getResetButton();
	}
	
	/**
	 * Add aligned Component to the layout
	 * @param c
	 */
	private void addComponetenToMainPanel(JComponent c){
		c.setAlignmentX(LEFT_ALIGNMENT);
		c.setAlignmentY(TOP_ALIGNMENT);
		mainPanel.add(c);
	}

	/**
	 * Initializes the UI
	 * @param tf
	 * @param agm
	 * @param dataManager
	 */
	private void createTFWindow(final TransferFunction1D tf,final AccumulatorManager agm,final VolumeDataManager dataManager){
		tfpanel = new TransferFunctionDrawPanel(tf,dataManager);
		tfDataPanel = new TransferFunctionDataPanel(tf);
		aggregationPanel = new VolumeDataAccumulatorPanel(agm);


		setTitle("Transfer function configurations");
		setSize(640, 100);
		initAdvancedBox();
		initBackgroundPanel();
		initUsePreIntegration();
		initTransferFunctionEditorPanel();
		initListeners();
		initBorderCheck();
		initShowSlice();
		initSampleSpinner();
		//initUseGradient();
		initVolumeInterpreterPanel();
		initSceneControlsPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		sceneConfigurationPanel.setAlignmentY(TOP_ALIGNMENT);
		detailViewConfig.setAlignmentY(TOP_ALIGNMENT);
		legend.setAlignmentY(TOP_ALIGNMENT);
		interactionToolPanel.setLayout(new BoxLayout(interactionToolPanel, BoxLayout.X_AXIS));
		interactionToolPanel.add(aligneLeft(sceneConfigurationPanel));
		interactionToolPanel.add(aligneLeft(detailViewConfig));
		interactionToolPanel.add(aligneLeft(legend));
		
		aggregationPanel.setAlignmentY(TOP_ALIGNMENT);
		volumeInterpreterPanel.setAlignmentY(TOP_ALIGNMENT);
		shaderElementPanel.setLayout(new BoxLayout(shaderElementPanel, BoxLayout.X_AXIS));
		shaderElementPanel.add(aligneLeft(aggregationPanel));
		shaderElementPanel.add(aligneLeft(volumeInterpreterPanel));

		addComponetenToMainPanel(aligneLeft(transferFunktionEditorPanel));
		addComponetenToMainPanel(aligneLeft(interactionToolPanel));
		addComponetenToMainPanel(aligneLeft(shaderElementPanel));
		
		//TODO bench stuff
		//addComponetenToMainPanel(aligneLeft(benchButton));
		benchButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				drawWindow.startBenchmark();
			}
		});
		//end bench stuff

		tfDataPanel.setVisible(advancedCheck.isSelected());

		getContentPane().add(mainPanel);
		pack();
	}

	/**
	 * Initializes scene controls menu 
	 */
	private void initSceneControlsPanel() {
		sceneConfigurationPanel.setBorder(BorderFactory.createTitledBorder("Scene configurations"));
		sceneConfigurationPanel.setLayout(new BoxLayout(sceneConfigurationPanel, BoxLayout.Y_AXIS));
		
		JPanel downSamplePanel = new JPanel();
		downSamplePanel.setLayout(new BoxLayout(downSamplePanel, BoxLayout.X_AXIS));
		downSamplePanel.add(downSampleCheckBox);
		downSamplePanel.add(downSampleSpinner);
		downSampleSpinner.setPreferredSize(downSampleSpinner.getMinimumSize());
		downSampleSpinner.setMaximumSize(downSampleSpinner.getMinimumSize());
		
		
		sceneConfigurationPanel.add(aligneLeft(samplePanel));
		sceneConfigurationPanel.add(aligneLeft(downSamplePanel));
		sceneConfigurationPanel.add(aligneLeft(rectBorderCheck));
		sceneConfigurationPanel.add(aligneLeft(showSlice));
		sceneConfigurationPanel.add(aligneLeft(backgroundPanel));
	}

	/**
	 * Initializes the transfer function editor panel
	 */
	private void initTransferFunctionEditorPanel() {
		transferFunktionEditorPanel.setBorder(BorderFactory.createTitledBorder("Transferfunction Editor"));
		transferFunktionEditorPanel.setLayout(new BoxLayout(transferFunktionEditorPanel, BoxLayout.Y_AXIS));
		
		transferFunktionEditorPanel.add(aligneLeft(tfpanel));
		transferFunktionEditorPanel.add(aligneLeft(usePreIntegration));
		//transferFunktionEditorPanel.add(aligneLeft(useGradient));
		transferFunktionEditorPanel.add(aligneLeft(advancedCheck));
		transferFunktionEditorPanel.add(aligneLeft(tfDataPanel));
		
	}

	/**
	 * Initializes the volumen interpreter menu
	 */
	private void initVolumeInterpreterPanel() {
		volumeInterpreterPanel.setBorder(BorderFactory.createTitledBorder("Volume interpreters"));
		volumeInterpreterPanel.setLayout(new BoxLayout(volumeInterpreterPanel, BoxLayout.Y_AXIS));

		emissionsAbsorbationRadioButton.setSelected(true);
		
		volumenInterpreterGroup.add(emissionsAbsorbationRadioButton);
		volumenInterpreterGroup.add(isoRadioButton);
		volumenInterpreterGroup.add(maximumIntensityProjectionRadioButton);
		
		maximumIntensityProjectionRadioButton.setAlignmentY(BOTTOM_ALIGNMENT);
		
		volumeInterpreterPanel.add(aligneLeft(emissionsAbsorbationRadioButton));
		volumeInterpreterPanel.add(aligneLeft(isoPanel));
		volumeInterpreterPanel.add(aligneLeft(maximumIntensityProjectionRadioButton));
		volumeInterpreterPanel.setAlignmentY(TOP_ALIGNMENT);
	}

/*	public void updateUseGradient(){
		renderer.setUseGradient(this.useGradient.isSelected());
		drawWindow.getGlCanvas().repaint();
	}
	
	private void initUseGradient() {
		updateUseGradient();
		useGradient.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				updateUseGradient();
			}
		});

	}*/

	/**
	 * Initializes the samples spin box
	 */
	private void initSampleSpinner() {
		sampleSpinner.setPreferredSize(sampleSpinner.getMinimumSize());
		sampleSpinner.setMaximumSize(sampleSpinner.getMinimumSize());
		samplePanel.setLayout(new BoxLayout(samplePanel, BoxLayout.X_AXIS));
		samplePanel.add(new JLabel("Render samples: "));
		samplePanel.add(sampleSpinner);

		updateSamples();
		sampleSpinner.addChangeListener(new ChangeListener() {

			/**
			 * Updates renderer sample count on spinner update
			 */
			@Override
			public void stateChanged(ChangeEvent e) {
				updateSamples();	
			}
		});
	}

	/**
	 * Set render sample count with the spinner value. Causing repaint of the scene
	 */
	private void updateSamples() {
		renderer.setSamples(((Number) sampleSpinner.getValue()).intValue());
		drawWindow.getGlCanvas().repaint();
	}

	/**
	 * Initializes the checkbox defining the visability of the 2D slice
	 */
	private void initShowSlice() {
		updateSlice();
		showSlice.addItemListener(new ItemListener() {

			/**
			 * Handler for visability
			 */
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateSlice();
			}
		});

	}

	/**
	 * Updates the visability of the 2D slice. Causing repaint of the scene.
	 */
	private void updateSlice() {
		renderer.setSliceShown(showSlice.isSelected());
		drawWindow.getGlCanvas().repaint();
	}

	/**
	 * Updates the visability of the partial volume borders. Causing repaint of the scene.
	 */
	private void updateBorderStatus(){
		scene.enableVolumeBorders(rectBorderCheck.isSelected());
		drawWindow.getGlCanvas().repaint();
	}

	/**
	 * Initializes the border visability checkbox 
	 */
	private void initBorderCheck() {
		updateBorderStatus();
		rectBorderCheck.addItemListener(new ItemListener() {

			/**
			 * Handler for border visability
			 */
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateBorderStatus();
			}
		});
	}

	/**
	 * Updates the background color of the render view with the button color. Causing repaint if the view.
	 * @param c
	 */
	private void updateBackgroundColors(Color c){
		backgroundColorButton.setBackground(c);
		renderer.setBackgroundColor(c);
		drawWindow.getScene().setBackgroundColor(c);
		drawWindow.getGlCanvas().repaint();
	}

	/**
	 * Initializes menu item controling the background color
	 */
	private void initBackgroundPanel() {

		backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.X_AXIS));
		backgroundPanel.add(new JLabel("Background color: ") );
		backgroundPanel.add(backgroundColorButton);
		updateBackgroundColors( drawWindow.getScene().getBackgroundColor());

		backgroundColorButton.addActionListener(new ActionListener() {

			/**
			 * Handler for backgroung color change
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(new JFrame(), "color dialog", backgroundColorButton.getBackground());

				if(color == null && !drawWindow.getScene().getBackgroundColor().equals(color)){
					return;
				}

				updateBackgroundColors(color);
			}
		});
	}

	/**
	 * Updates the interpreter of the renderer with the currently selected one.
	 */
	private void changeVolumeInterpreter(){
		if(isoRadioButton.isSelected()){
			renderer.getSource().setVolumeInterpreter(new IsoSurfaceVolumeInterpreter());
		}

		if(emissionsAbsorbationRadioButton.isSelected()){
			renderer.getSource().setVolumeInterpreter(new TransparentVolumeinterpreter());
		}

		if(maximumIntensityProjectionRadioButton.isSelected()){
			renderer.getSource().setVolumeInterpreter(new MaximumIntensityProjectionInterpreter());
		}

	}

	/**
	 * Initializes listeners for volume data changes and interpreter changes
	 */
	private void initListeners() {

		dataManager.addVolumeDataManagerListener(new VolumeDataManagerAdapter() {

			/**
			 * Handler to void low data on data update
			 */
			@Override
			public void dataUpdated(Integer i) {
			
				transferFunction.setMaxOrdinates(new Point2D.Float(dataManager.getGlobalMaxVolumeValue(), 1.0f));
				float lowestDataPoint = dataManager.getGlobalLowestVolumeValue();
			
				if(addedTransfeFunctionPoint != null){
					if(transferFunction.getColors().containsKey(addedTransfeFunctionPoint)){
						transferFunction.removeColor(addedTransfeFunctionPoint);
					}
				}
				addedTransfeFunctionPoint = new Point2D.Float((transferFunction.getMaxOrdinates().x/dataManager.getGlobalMaxVolumeValue())*lowestDataPoint, 0.001f);
				transferFunction.setColor(addedTransfeFunctionPoint, transferFunction.getColors().firstEntry().getValue());
			}
			
			/**
			 * Handler repainting on visibility change
			 */
			@Override
			public void dataEnabled(Integer i, Boolean flag) {
				drawWindow.getGlCanvas().repaint();
			}
		});	

		changeVolumeInterpreter();
		isoRadioButton.addItemListener(new ItemListener() {

			/**
			 * Handler for interpreter changes to iso
			 */
			@Override
			public void itemStateChanged(ItemEvent e) {
				changeVolumeInterpreter();
				drawWindow.getGlCanvas().repaint();
			}
		});

		emissionsAbsorbationRadioButton.addItemListener(new ItemListener() {

			/**
			 * Handler for interpreter changes to em
			 */
			@Override
			public void itemStateChanged(ItemEvent e) {
				changeVolumeInterpreter();
				drawWindow.getGlCanvas().repaint();
			}
		});

		maximumIntensityProjectionRadioButton.addItemListener(new ItemListener() {
			
			/**
			 * Handler for interpreter changes to mip
			 */
			@Override
			public void itemStateChanged(ItemEvent e) {
				changeVolumeInterpreter();
				drawWindow.getGlCanvas().repaint();
			}
		});

		isoRadioButton.setAlignmentY(BOTTOM_ALIGNMENT);
		isoPanel.setLayout(new BoxLayout(isoPanel, BoxLayout.X_AXIS));
		isoPanel.add(isoRadioButton);
		isoPanel.add(isoMenu);
		
	}

	/**
	 * Changes sampler for transfer function depending on current configuration
	 */
	private void changeTransferfuntionSampler(){
		if(usePreIntegration.isSelected()){
			transferFunction.setSampler(new PreIntegrationSampler());
		}else{
			transferFunction.setSampler(new RegularSampler());
		}
	}

	/**
	 * Initializes the preintegration checkbox
	 */
	private void initUsePreIntegration() {
		changeTransferfuntionSampler();
		usePreIntegration.addItemListener(new ItemListener() {

			/**
			 * Handler to change sampler
			 */
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				changeTransferfuntionSampler();
			}
		});
	}

	/**
	 * Initializes detail transfer function data table switch
	 */
	private void initAdvancedBox() {
		advancedCheck.addItemListener(new ItemListener() {

			/**
			 * Handler to show data table if needed
			 */
			@Override
			public void itemStateChanged(ItemEvent e) {
				tfDataPanel.setVisible(advancedCheck.isSelected());
				pack();
			}
		});
	}

	/**
	 * Cleanup of the window instance
	 */
	public void destroyTFWindow() {
		dispose();
		tfpanel = null;
	}

	/**
	 * @return the detailViewConfig
	 */
	public DetailViewConfiguration getDetailViewConfig() {
		return detailViewConfig;
	}

	/**
	 * @return the showSlice
	 */
	public JCheckBox getShowSlice() {
		return showSlice;
	}

	/**
	 * Returns the sample spinner instance
	 * @return
	 */
	public JSpinner getSamplesSpinner() {
		return sampleSpinner;
	}

	/**
	 * @return the downSampleCheckBox
	 */
	public JCheckBox getDownSampleCheckBox() {
		return downSampleCheckBox;
	}

	/**
	 * @return the downSampleSpinner
	 */
	public JSpinner getDownSampleSpinner() {
		return downSampleSpinner;
	}
}
