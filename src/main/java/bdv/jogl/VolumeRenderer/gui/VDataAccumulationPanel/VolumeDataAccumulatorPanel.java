package bdv.jogl.VolumeRenderer.gui.VDataAccumulationPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * configuration for volume renderer data aggregation
 * @author michael
 *
 */
public class VolumeDataAccumulatorPanel extends JPanel {
	
	/**
	 * default version
	 */
	private static final long serialVersionUID = 1L;

	private ButtonGroup aggregationGroup = new ButtonGroup();
	
	private List<JRadioButton> aggregationButtons = new LinkedList<JRadioButton>();
	
	private final AccumulatorManager dataManager;
	
	private ActionListener radioListener = new ActionListener() {
		
		/**
		 * Sets an accumulator to activ, when its radiobutton is selected
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			dataManager.setActiveAcumulator(e.getActionCommand());
		}
	}; 
	
	/**
	 * Initializes the UI
	 */
	private void initPanel(){
		setBorder(BorderFactory.createTitledBorder("Volume data accumulators"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		//create buttons
		String selected = dataManager.getActiveAccumulator();
		for(String name: dataManager.getAccumulatorNames()){
			
			JRadioButton avg= new JRadioButton(name);
			avg.setActionCommand(name);
			if(name.equals(selected)){
				avg.setSelected(true);
			}
			avg.addActionListener(radioListener);
			aggregationButtons.add(avg);
		}
		
		//add buttons
		for(JRadioButton button: aggregationButtons){
			aggregationGroup.add(button);
			this.add(button);
		}
	}
	
	/**
	 * Constructor
	 * @param manager
	 */
	public VolumeDataAccumulatorPanel(final AccumulatorManager manager){
		this.dataManager = manager;
		initPanel();
	}
}
