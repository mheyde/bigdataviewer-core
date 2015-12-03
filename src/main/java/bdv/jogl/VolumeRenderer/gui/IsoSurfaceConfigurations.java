package bdv.jogl.VolumeRenderer.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;
import bdv.jogl.VolumeRenderer.shaderprogram.MultiVolumeRenderer;

/**
 * Configuration panel for the iso surface interpreter
 * @author michael
 */
public class IsoSurfaceConfigurations extends JPanel {

	/**
	 * Default id
	 */
	private static final long serialVersionUID = 1L;
	
	private final JSpinner isoValueSpinner = new JSpinner();

	private final JButton isoLightButton = new JButton();  

	private final GLWindow drawWindow;
	
	private final MultiVolumeRenderer renderer;
	
	/**
	 * Constructor
	 * @param drawWindow
	 * @param renderer
	 */
	public IsoSurfaceConfigurations(final GLWindow drawWindow, final MultiVolumeRenderer renderer){
		this.drawWindow = drawWindow;
		this.renderer = renderer;
		initUI();
		initListeners();
	}

	/**
	 * Update the renderer iso value with the iso spinner value
	 */
	private void updateIsoSurface(){
		renderer.setIsoSurface(((Number)isoValueSpinner.getValue()).floatValue());
	}
	
	/**
	 * Initializes iso value change and light color value change handlers
	 */
	private void initListeners() {
		updateIsoSurface();
		isoValueSpinner.addChangeListener(new ChangeListener() {
			
			/**
			 * Handles iso value changes
			 */
			@Override
			public void stateChanged(ChangeEvent e) {
				updateIsoSurface();
				drawWindow.getGlCanvas().repaint();
			}
		});
		

		updateLightColor();
		final IsoSurfaceConfigurations me = this;
		isoLightButton.addActionListener(new ActionListener() {
			
			/**
			 * Handles light color changes
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				Color selectedColor = JColorChooser.showDialog(me, "Select light color", isoLightButton.getBackground());
				if(selectedColor == null){
					return;
				}
				isoLightButton.setBackground(selectedColor);
				updateLightColor();
				drawWindow.getGlCanvas().repaint();
			}
		});
	}

	/**
	 * Updates the render light color for iso surfaces with the color of the light button
	 */
	private void updateLightColor() {
		renderer.setLightColorForIsoSurface(isoLightButton.getBackground());
	}

	/**
	 * Initializes the UI
	 */
	private void initUI() {
		setLayout(new GridLayout(2, 2));
		setBorder(BorderFactory.createTitledBorder("Iso configuration"));
		isoValueSpinner.setModel(new SpinnerNumberModel(0.0,0.0, 10000, 1.0f));
		isoValueSpinner.setPreferredSize(isoValueSpinner.getMinimumSize());
		isoValueSpinner.setMaximumSize(isoValueSpinner.getPreferredSize());
		
		add(new JLabel("Iso value:")); 
		add(isoValueSpinner);
		add(new JLabel("Light color: "));
		add(isoLightButton);
		isoLightButton.setBackground(Color.GREEN);
		setPreferredSize(getMinimumSize());
		setMaximumSize(getMinimumSize());
	}
}
