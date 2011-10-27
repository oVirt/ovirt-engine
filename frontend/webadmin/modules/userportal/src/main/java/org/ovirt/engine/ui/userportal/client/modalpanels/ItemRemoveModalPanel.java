package org.ovirt.engine.ui.userportal.client.modalpanels;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.ListModel;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameResolver;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import org.ovirt.engine.ui.userportal.client.components.Button;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public class ItemRemoveModalPanel extends NonDraggableModalPanel {

	private ItemRemoveModalPanel instance = this;
	
	private static final int panelWidth = 370;
	private static final int panelHeight = 350;
	private ListModel model;
	
	public ItemRemoveModalPanel(String title, String itemsString, final ListModel model, ObjectNameResolver nameResolver, final GridController gridController) {
		super (panelWidth, panelHeight, title);
			
		this.model = model;
		
		VLayout inPanel = new VLayout();
		inPanel.setHeight100();
		inPanel.setWidth100();
		
		Label message = new Label("Are you sure you want to remove the following " + itemsString + "?");
		message.setStyleName("removePanelLabel");
		message.setAutoHeight();
		inPanel.addMember(message);
		
		for (Object item : model.getSelectedItems()) {
			Label l = new Label("- " + nameResolver.getItemName(item));
			l.setStyleName("removePanelItems");
			l.setAutoHeight();
			l.setValign(VerticalAlignment.TOP);
			inPanel.addMember(l);
			
		}
	
		model.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				String propertyName = ((PropertyChangedEventArgs)args).PropertyName;
				GWT.log("DERBA: " + propertyName);
				if (propertyName.equals("Window") || propertyName.equals("ConfirmationModel")) {
					destroy();
					model.getPropertyChangedEvent().removeListener(this);
					if (gridController != null)
						gridController.gridChangePerformed();
				}
			}
		});
		
		Button okButton = new Button("OK");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.ExecuteCommand(new UICommand("OnRemove", model));
			}
		});
		
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onClose();
			}
		});

		setFooterButtons(Alignment.RIGHT, okButton, cancelButton);
		
		addItem(inPanel);
	
	}

	@Override
	public void onClose() {
		model.ExecuteCommand(new UICommand("Cancel", model));
	}
	
}