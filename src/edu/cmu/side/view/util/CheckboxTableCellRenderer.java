package edu.cmu.side.view.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class CheckboxTableCellRenderer extends DefaultTableCellRenderer{

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
		Component rend = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
		rend.setBackground(Color.white);

		if(value instanceof RadioButtonListEntry){
			RadioButtonListEntry radioButton = (RadioButtonListEntry) value;
			radioButton.setEnabled(isEnabled());
			radioButton.setFont(getFont());
			radioButton.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			rend = radioButton;
		}
		if(value instanceof CheckBoxListEntry){
			CheckBoxListEntry checkButton = (CheckBoxListEntry) value;
			checkButton.setEnabled(isEnabled());
			checkButton.setFont(getFont());
			checkButton.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			rend = checkButton;
			
		}
		return rend;
	}
}