package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.widgets.form.fields.ComboBoxItem;

public class UPComboBoxItem extends ComboBoxItem {
	public UPComboBoxItem() {
		super();
		updateStyle();
	}
	
	public UPComboBoxItem(String name, String title) {
        super(name, title);
        updateStyle();
    }
	
	private void updateStyle() {
	    setTextBoxStyle("engineSelectItem");
        setPickerIconSrc("picker_button.gif");
        setAttribute("browserSpellCheck", false);
        setShowAllOptions(true);
	}
}