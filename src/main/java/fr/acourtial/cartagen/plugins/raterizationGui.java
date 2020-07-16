package fr.acourtial.cartagen.plugins;
//annulé
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import fr.ign.cogit.geoxygene.appli.I18N;
import fr.ign.cogit.geoxygene.appli.plugin.matching.netmatcher.gui.EditParamDatasetPanel;

public class raterizationGui extends JDialog implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JButton launchButton = null;
	private JButton cancelButton = null;
	
	
	JPanel buttonPanel = null;
	//EditParamActionPanel actionPanel = null;
	EditParamDatasetPanel datasetPanel = null;
	EditParamDatasetPanel datasetPanel2 = null;
	//EditParamDirectionPanel directionPanel = null;
	//EditParamDistancePanel distancePanel = null;
	//EditParamTopoPanel topoTreatmentPanel = null;
	//EditParamProjectionPanel projectionPanel = null;
	//EditParamVariantePanel variante = null;
	  
	public raterizationGui() {
		setModal(true);
	    setTitle("MON Title");
	    initButtonPanel();
	    datasetPanel = new EditParamDatasetPanel(null, null);
	    datasetPanel2 = new EditParamDatasetPanel(null,null);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	  private void initButtonPanel() {
		    
		    buttonPanel = new JPanel(); 
		    
		    launchButton = new JButton("BouOTN A");
		    cancelButton = new JButton("BOUTON B");
		    
		    launchButton.addActionListener(this);
		    cancelButton.addActionListener(this);
		    
		    buttonPanel.setLayout(new FlowLayout (FlowLayout.CENTER)); 
		    buttonPanel.add(cancelButton);
		    buttonPanel.add(launchButton);
		    
		  }
}
