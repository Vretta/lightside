package edu.cmu.side.simple.newui;

import java.awt.Color;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.genesis.GenesisWorkbench;

public abstract class AbstractListPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = -1090634417229954402L;

	protected FastListModel listModel;
	protected JList list;
	protected JScrollPane listScroll;

	protected JTextArea description;
	protected JScrollPane describeScroll;
	
	protected JComboBox combo;
	
	protected JButton add;
	protected JButton delete;
	protected JButton clear;
	
	private void init(){
		this.setLayout(new RiverLayout());
		listModel = new FastListModel();
		list = new JList();
		list.setModel(listModel);	
		combo = new JComboBox();

		listScroll = new JScrollPane(list);
		description = new JTextArea();
		description.setEditable(false);
		describeScroll = new JScrollPane(description);
		add = new JButton("Add");
		delete = new JButton("Delete");
		clear = new JButton("Clear");
	}
	
	public AbstractListPanel(){
		init();
	}

	/** What needs to be updated in this panel when something changes in the backend model? */
	public void refreshPanel(){
		if(listModel.getSize()>0 && list.getSelectedIndex()==-1){
			list.setSelectedIndex(0);
			fireActionEvent();
		}
	}
	
	/**
	 * Listener pattern stuff from here to the end of the file.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		fireActionEvent();
		refreshPanel();
	}

	private List<ActionListener> actionListenerList = new ArrayList<ActionListener>();

	public void addActionListener(ActionListener al){
		this.actionListenerList.add(al);
	}
	public void removeActionListener(ActionListener al){
		this.actionListenerList.remove(al);
	}

	private int actionEventID = 1;
	public void fireActionEvent(){
		GenesisWorkbench.update();
	}
	
}
