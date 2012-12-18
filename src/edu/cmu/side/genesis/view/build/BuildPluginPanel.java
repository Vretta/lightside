package edu.cmu.side.genesis.view.build;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.BuildModelControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class BuildPluginPanel extends AbstractListPanel {

	public JPanel panel = new JPanel();
	public JPanel middle = new JPanel();
	public BuildPluginPanel(){
		setLayout(new BorderLayout());
		middle.setLayout(new BorderLayout());
		panel.setLayout(new RiverLayout());
		panel.add("left", new JLabel("Selected Learner:"));
		panel.add("br hfill", combo);
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					LearningPlugin r = (LearningPlugin)combo.getSelectedItem();
					BuildModelControl.setHighlightedLearningPlugin(r);
					middle.removeAll();
					LearningPlugin highlight = BuildModelControl.getHighlightedLearningPlugin();
					if(highlight != null){
						middle.add(BorderLayout.CENTER, highlight.getConfigurationUI());				
					}

				}
				GenesisWorkbench.update();
			}
		});
		add(BorderLayout.NORTH, panel);
		add(BorderLayout.CENTER, middle);
	}
	
	public void refreshPanel(){
		if(combo.getItemCount() != BuildModelControl.numLearningPlugins()){
			LearningPlugin highlight = BuildModelControl.getHighlightedLearningPlugin();
			GenesisWorkbench.reloadComboBoxContent(combo, BuildModelControl.getLearningPlugins(), highlight);
		}
	}
}