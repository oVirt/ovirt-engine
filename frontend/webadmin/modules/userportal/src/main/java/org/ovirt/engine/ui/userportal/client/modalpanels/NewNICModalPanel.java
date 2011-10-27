package org.ovirt.engine.ui.userportal.client.modalpanels;


import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.SearchableListModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import org.ovirt.engine.ui.userportal.client.components.Button;
import org.ovirt.engine.ui.userportal.client.components.SelectBoxListModelBinded;
import org.ovirt.engine.ui.userportal.client.components.TextItemEntityModelBinded;
import org.ovirt.engine.ui.userportal.client.timers.SubTabRefreshTimer;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public class NewNICModalPanel extends NonDraggableModalPanel {
	final NewNICModalPanel newNICModalPanel = this;
	UICommand cancelCommand;
	
	public NewNICModalPanel(final SearchableListModel parentListModel, final VmInterfaceModel vmInterfaceModel) {
		super(320, 300, vmInterfaceModel.getTitle());
		
		VLayout inPanel = new VLayout();
		inPanel.setHeight100();
		inPanel.setWidth100();
		
		DynamicForm f = new DynamicForm();

		TextItemEntityModelBinded nameTextItem = new TextItemEntityModelBinded("Name", vmInterfaceModel.getName());
		
		SelectBoxListModelBinded networkSelectBox = new SelectBoxListModelBinded("Network", vmInterfaceModel.getNetwork(), network.class);
		
		SelectBoxListModelBinded nicTypeSelectBox = new SelectBoxListModelBinded(false, "Type", vmInterfaceModel.getNicType(), VmInterfaceType.class);
		
		if (parentListModel.getEntity() instanceof VM) {
    		CheckboxItem specifyMacCheckBox = new CheckboxItem();
    		specifyMacCheckBox.setTitle("Specify custom MAC address");
    		specifyMacCheckBox.setValue(vmInterfaceModel.getMAC().getIsChangable());
    		specifyMacCheckBox.setColSpan(2);
    		specifyMacCheckBox.setShowTitle(false);
    		
    		final TextItemEntityModelBinded macTextItem = new TextItemEntityModelBinded("", vmInterfaceModel.getMAC());
    		macTextItem.setDisabled(!vmInterfaceModel.getMAC().getIsChangable());
    		specifyMacCheckBox.addChangedHandler(new ChangedHandler() {
    			@Override
    			public void onChanged(ChangedEvent event) {
    				Boolean specifyMac = (Boolean)((CheckboxItem)event.getSource()).getValue();				
    				vmInterfaceModel.getMAC().setIsChangable(specifyMac);
    				macTextItem.setDisabled(!specifyMac);
    			}
    		});
    		macTextItem.setShowTitle(false);
		
		    f.setItems(nameTextItem, networkSelectBox, nicTypeSelectBox, specifyMacCheckBox, new SpacerItem(), macTextItem);
		}
		else {
		    f.setItems(nameTextItem, networkSelectBox, nicTypeSelectBox);
		}
		    
		
		f.focus();
		f.setAutoFocus(true);
		inPanel.addMember(f);
		
		parentListModel.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				String propertyName = ((PropertyChangedEventArgs)args).PropertyName;
				if (propertyName.equals("Window")) {
					parentListModel.getPropertyChangedEvent().removeListener(this);
					destroy();
				}
			}
		});
		
		final UICommand saveCommand = new UICommand("OnSave", parentListModel);
		cancelCommand = new UICommand("Cancel", parentListModel);
		
		Button okButton = new Button("OK");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				SubTabRefreshTimer.initSubTabRefreshTimer(parentListModel);
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
	}

	@Override
	public void onClose() {
		cancelCommand.Execute();
	}
}