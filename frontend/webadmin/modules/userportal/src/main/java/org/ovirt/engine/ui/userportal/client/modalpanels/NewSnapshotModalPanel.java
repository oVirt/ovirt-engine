package org.ovirt.engine.ui.userportal.client.modalpanels;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import org.ovirt.engine.ui.userportal.client.components.Button;
import org.ovirt.engine.ui.userportal.client.components.TextItemEntityModelBinded;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.HeaderItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.layout.VLayout;


public class NewSnapshotModalPanel extends NonDraggableModalPanel {
	final NewSnapshotModalPanel newSnapshotModalPanel = this;
	UICommand cancelCommand;
	
	public NewSnapshotModalPanel(final GridController gridController, final VmSnapshotListModel vmSnapshotListModel) {
		super(320, 150, "Create Snapshot");
		
		final SnapshotModel snapshotModel = (SnapshotModel)vmSnapshotListModel.getWindow();
		
		// Creating and set a inner panel
		VLayout inPanel = new VLayout();
		inPanel.setHeight100();
		inPanel.setWidth100();
		
		// Creating and set an indent spacer
		SpacerItem indentSpacer = new SpacerItem();
		indentSpacer.setWidth(3);
		indentSpacer.setHeight(5);					
		
		// Creating description box and a message label 		
		TextItemEntityModelBinded descriptionTextItem = new TextItemEntityModelBinded("Description", snapshotModel.getDescription());			
		HeaderItem messageLabel = new HeaderItem();
		messageLabel.setDefaultValue(snapshotModel.getMessage());
		messageLabel.setTextBoxStyle("warningLabel");		
			
		// Set items inside a form
		DynamicForm f = new DynamicForm();
		f.setItems(descriptionTextItem, indentSpacer, messageLabel);		
		f.focus();
		f.setAutoFocus(true);
		f.setAutoHeight();
		
		// Adding form to inner panel
		inPanel.addMember(f);		
		// Adding inner panel to this view 
		addItem(inPanel);
		
		vmSnapshotListModel.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				String propertyName = ((PropertyChangedEventArgs)args).PropertyName;
				if (propertyName.equals("Window")) {
					vmSnapshotListModel.getPropertyChangedEvent().removeListener(this);
					destroy();
				}
			}
		});
		
		// Set footer buttons
		final UICommand saveCommand = new UICommand("OnNew", vmSnapshotListModel);
		cancelCommand = new UICommand("Cancel", vmSnapshotListModel);
		
		Button okButton = new Button("OK");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {	
				saveCommand.Execute();
				//SubTabRefreshTimer.initSubTabRefreshTimer(vmSnapshotListModel);
				gridController.gridChangePerformed();
			}
		});
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onClose();
			}
		});
		Button closeButton = new Button("Close");
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onClose();
			}
		});			
		
		// Can create a snapshot only if disks are available
		if (snapshotModel.getDisks().size() == 0)
		{
			setFooterButtons(Alignment.RIGHT, closeButton);
		}
		else
		{
			setFooterButtons(Alignment.RIGHT, okButton, cancelButton);
		}

        subscribeProgressChangedEvent(snapshotModel, descriptionTextItem, inPanel);
	}

	@Override
	public void onClose() {
		cancelCommand.Execute();
	}
}