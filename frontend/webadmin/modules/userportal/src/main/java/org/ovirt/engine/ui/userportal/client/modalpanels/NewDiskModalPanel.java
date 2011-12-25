package org.ovirt.engine.ui.userportal.client.modalpanels;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.userportal.client.binders.FormConstructor;
import org.ovirt.engine.ui.userportal.client.components.CheckboxItemModelBinded;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import org.ovirt.engine.ui.userportal.client.components.Button;
import org.ovirt.engine.ui.userportal.client.components.SelectBoxListModelBinded;
import org.ovirt.engine.ui.userportal.client.components.TextItemEntityModelBinded;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameIdResolver;
import org.ovirt.engine.ui.userportal.client.timers.SubTabRefreshTimer;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.layout.VLayout;

public class NewDiskModalPanel extends NonDraggableModalPanel {
	
	final NewDiskModalPanel newDiskModalPanel = this;
	UICommand cancelCommand;
	
	public NewDiskModalPanel(final VmDiskListModel vmDiskListModel, final GridController gridController) {
		super(285, 300, vmDiskListModel.getWindow().getTitle());

		final DiskModel diskModel = (DiskModel)vmDiskListModel.getWindow();
	
		VLayout inPanel = new VLayout();
		inPanel.setHeight100();
		inPanel.setWidth100();
		
		TextItemEntityModelBinded sizeTextItem = new TextItemEntityModelBinded("Size (GB)", diskModel.getSize(), true);

		SelectBoxListModelBinded storageDomainSelectBox = new SelectBoxListModelBinded("Storage Domain", diskModel.getStorageDomain(), storage_domains.class);
		
		SelectBoxListModelBinded diskTypeSelectBox = new SelectBoxListModelBinded("Disk type", diskModel.getPreset(), DiskImageBase.class);

		SelectBoxListModelBinded interfaceSelectBox = new SelectBoxListModelBinded("Interface", diskModel.getInterface(), DiskInterface.class);

		SelectBoxListModelBinded formatSelectBox = new SelectBoxListModelBinded(false, "Format", diskModel.getVolumeType(), VolumeType.class);

		CheckboxItemModelBinded wipeAfterDeleteCheckBox = new CheckboxItemModelBinded("Wipe after delete", diskModel.getWipeAfterDelete());
		
		CheckboxItemModelBinded isBootableCheckBox = new CheckboxItemModelBinded("Is bootable", diskModel.getIsBootable());
		
		DynamicForm f = FormConstructor.constructForm(sizeTextItem, storageDomainSelectBox, diskTypeSelectBox, interfaceSelectBox, formatSelectBox, wipeAfterDeleteCheckBox, isBootableCheckBox);
		
		if (sizeTextItem.getDisabled()) {
			f.focus();
		}
		else {
			f.focusInItem(sizeTextItem);
			f.setAutoFocus(true);
		}

		inPanel.addMember(f);

		vmDiskListModel.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				String propertyName = ((PropertyChangedEventArgs)args).PropertyName;
				if (propertyName.equals("Window")) {
					vmDiskListModel.getPropertyChangedEvent().removeListener(this);
					destroy();
					gridController.gridChangePerformed();
				}
			}
		});
		
		final UICommand saveCommand = new UICommand("OnSave", vmDiskListModel);
		cancelCommand = new UICommand("Cancel", vmDiskListModel);

		
		Button okButton = new Button("OK");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// No need to init subtab refresh timer since we refresh the whole grid anyway
				//SubTabRefreshTimer.initSubTabRefreshTimer(vmDiskListModel);
				saveCommand.Execute();
			}
		});
		
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onClose();
			}
		});
		
		addItem(inPanel);
		
		setFooterButtons(Alignment.RIGHT, okButton, cancelButton);

		subscribeProgressChangedEvent(diskModel, sizeTextItem, inPanel);
	}

	@Override
	public void onClose() {
		cancelCommand.Execute();
	}
}