package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.widgets.form.fields.TextItem;

public class UPTextItem extends TextItem {
	public UPTextItem() {
		super();
		setTextBoxStyle("engineTextItem");
		setAttribute("browserSpellCheck", false);
	}
	
	public UPTextItem(String title) {
		this();
		setTitle(title);
	}

	public UPTextItem(String name, String title) {
		this();
		setName(name);
		setTitle(title);
	}
}