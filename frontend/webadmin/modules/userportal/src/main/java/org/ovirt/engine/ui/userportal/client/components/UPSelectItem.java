package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.widgets.form.fields.SelectItem;

public class UPSelectItem extends SelectItem {
	public UPSelectItem() {
		super();
		setTextBoxStyle("engineSelectItem");
		setPickerIconSrc("picker_button.gif");
	}

	public UPSelectItem(String name, String title) {
		super(name, title);
		setTextBoxStyle("engineSelectItem");
		setPickerIconSrc("picker_button.gif");
	}
}