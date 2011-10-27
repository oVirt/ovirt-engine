package org.ovirt.engine.ui.userportal.client.components;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.EntityModel;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.userportal.client.binders.ModelBindedComponent;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;

public class CheckboxItemModelBinded extends CheckboxItem implements ModelBindedComponent {
	private EntityModel entityModel;

	public CheckboxItemModelBinded(String title, EntityModel model) {
		this.entityModel = model;
		setTitle(title);
		setWrapTitle(false);
		setValue((Boolean)model.getEntity());
		setShowTitle(false);
		setDisabled(!model.getIsChangable());
		setColSpan(2);
		if (!model.getIsAvailable())
			setVisible(false);
		
		addChangedHandler(new ChangedHandler() {
			@Override
			public void onChanged(ChangedEvent event) {
				GWT.log("Check box " + getTitle() + " value change to " + event.getValue());
				entityModel.setEntity((Boolean)event.getValue());
			}
		});

		entityModel.getEntityChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				setValue((Boolean)entityModel.getEntity());
			}
		});

		entityModel.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				String property = ((PropertyChangedEventArgs)args).PropertyName;

				if (property.equals("IsChangable")) {
					setDisabled(!entityModel.getIsChangable());
				}
				else if (property.equals("IsAvailable")) {
					if (!entityModel.getIsAvailable()) {
						hide();
					}
					else {
						show();
					}
				}
			}
		});
	}

	@Override
	public Model getModel() {
		return entityModel;
	}

}