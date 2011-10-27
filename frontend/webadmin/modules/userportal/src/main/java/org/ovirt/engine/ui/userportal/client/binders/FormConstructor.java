package org.ovirt.engine.ui.userportal.client.binders;

import java.util.ArrayList;
import java.util.List;

import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;

public class FormConstructor {

	public static DynamicForm constructForm(ModelBindedComponent... items) {
		DynamicForm form = new DynamicForm();

		List<FormItem> formItems = new ArrayList<FormItem>();
		for (ModelBindedComponent c : items) {
			if (c.getModel().getIsAvailable()) {
				formItems.add((FormItem)c);
			}
		}

		form.setItems(formItems.toArray(new FormItem[0]));
		return form;
	}

}