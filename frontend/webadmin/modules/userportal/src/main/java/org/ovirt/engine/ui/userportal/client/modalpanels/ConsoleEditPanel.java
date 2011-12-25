package org.ovirt.engine.ui.userportal.client.modalpanels;

import java.util.LinkedHashMap;

import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommon.models.vms.IRdp;
import org.ovirt.engine.ui.uicommon.models.vms.ISpice;
import org.ovirt.engine.ui.uicommon.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommon.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.userportal.client.components.Button;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import org.ovirt.engine.ui.userportal.client.protocols.ConsoleUtils;
import org.ovirt.engine.ui.userportal.client.protocols.Protocol;
import org.ovirt.engine.ui.userportal.client.protocols.ProtocolOptionContainer;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.HeaderItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;

public class ConsoleEditPanel extends NonDraggableModalPanel {
	private UserPortalItemModel currentItem;
	private ProtocolOptionContainer protocolContainer;
	
	public ConsoleEditPanel(UserPortalItemModel item, final ProtocolOptionContainer protocolContainer) {
		super(300, 300, "Console Options");
		
		this.protocolContainer = protocolContainer;
		
		this.currentItem = item;
		
		HeaderItem consoleTypeLabel = new HeaderItem();
		consoleTypeLabel.setDefaultValue("Select Console for '" + currentItem.getName() + "':");
		consoleTypeLabel.setTextBoxStyle("generalLabel");
		
		final RadioGroupItem consoleType = new RadioGroupItem();
		LinkedHashMap<String,String> consoleTypeOptions = new LinkedHashMap<String, String>();
					
		if (currentItem.getDefaultConsole() instanceof SpiceConsoleModel && ConsoleUtils.isSpiceAvailable())
			consoleTypeOptions.put(Protocol.SPICE.name(), Protocol.SPICE.displayName);

		if (currentItem.getHasAdditionalConsole() && ConsoleUtils.isRDPAvailable())
			consoleTypeOptions.put(Protocol.RDP.name(), Protocol.RDP.displayName);
		
		
		// Create Spice/RDP options
		final DynamicForm spiceOptionsForm = getSpiceOptionsForm();
		final DynamicForm rdpOptionsForm = getRdpOptionsForm();
					
		consoleType.setValueMap(consoleTypeOptions);
		consoleType.setDefaultValue(protocolContainer.getProtocol().name());
		consoleType.setShowTitle(false);
		consoleType.setWrap(false);
		consoleType.addChangedHandler(new ChangedHandler() {
			@Override
			public void onChanged(ChangedEvent event) {
			    // Set visible options according to the selected protocol
			    Protocol selection = Protocol.valueOf((String)consoleType.getValue());
			    spiceOptionsForm.setVisible(selection.equals(Protocol.SPICE));
			    rdpOptionsForm.setVisible(selection.equals(Protocol.RDP));
			}
		});			
		
		DynamicForm f = new DynamicForm();
		f.setItems(consoleTypeLabel, consoleType, new SpacerItem(), new SpacerItem());
		
		addItem(f);
		addItem(spiceOptionsForm);
		addItem(rdpOptionsForm);
		
		Button okButton = new Button("OK");
		Button cancelButton = new Button("Cancel");
	
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				destroy();
			}
		});
		
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Protocol selection = Protocol.valueOf((String)consoleType.getValue());
				if (!protocolContainer.getProtocol().equals(selection)) {
					GWT.log("Changing the selected protocol to: " + selection);
					protocolContainer.setProtocol(selection);
				}
				destroy();
			}
		});
		
		setFooterButtons(Alignment.RIGHT, okButton, cancelButton);
	}
	
	DynamicForm getSpiceOptionsForm()
	{
	    // Create form	
	    DynamicForm spiceOptionsForm = new DynamicForm();
	    spiceOptionsForm.setVisible(protocolContainer.getProtocol().equals(Protocol.SPICE));
	    spiceOptionsForm.setNumCols(1);
	    
	    if (!(currentItem.getDefaultConsole() instanceof SpiceConsoleModel))
	        return spiceOptionsForm;
		
	    // Create header
	    HeaderItem spiceOptionsHeader = new HeaderItem();
	    spiceOptionsHeader.setDefaultValue("SPICE Options");
	    spiceOptionsHeader.setTextBoxStyle("generalLabel");
	    
	    // Get SPICE model
	    final ISpice spice = ((SpiceConsoleModel)currentItem.getDefaultConsole()).getspice();
		
	    // Create check boxes
	    CheckboxItem sendCtrlAltDelete = new CheckboxItem("sendCtrlAltDelete", "Pass Ctrl-Alt-Del to virtual machine");		    
	    sendCtrlAltDelete.setDefaultValue(spice.getSendCtrlAltDelete());
	    sendCtrlAltDelete.setShowTitle(false);
	    sendCtrlAltDelete.addChangedHandler(new ChangedHandler() {
		@Override
		public void onChanged(ChangedEvent event) {
		    
		    spice.setSendCtrlAltDelete((Boolean)event.getValue());
		    spice.setNoTaskMgrExecution((Boolean)event.getValue());
		}
	    });			
	    CheckboxItem usbAutoShare = new CheckboxItem("usbAutoShare", "Enable USB Auto-Share");
	    usbAutoShare.setDefaultValue(spice.getUsbAutoShare());
	    usbAutoShare.setShowTitle(false);	
	    usbAutoShare.addChangedHandler(new ChangedHandler() {
		@Override
		public void onChanged(ChangedEvent event) {
		    spice.setUsbAutoShare((Boolean)event.getValue());				    
		}
	    });
	    CheckboxItem fullscreen = new CheckboxItem("fullscreen", "Open in Full Screen");
	    fullscreen.setDefaultValue(spice.getFullScreen());
	    fullscreen.setShowTitle(false);
	    fullscreen.addChangedHandler(new ChangedHandler() {
		@Override
		public void onChanged(ChangedEvent event) {
		    spice.setFullScreen((Boolean)event.getValue());
		}
	    });
	
	    // Add header and check boxes
	    spiceOptionsForm.setItems(spiceOptionsHeader, sendCtrlAltDelete, usbAutoShare, fullscreen);
		
	    return spiceOptionsForm;
	}
	
	DynamicForm getRdpOptionsForm()
	{
	    // Create form	
	    DynamicForm rdpOptionsForm = new DynamicForm();
	    rdpOptionsForm.setVisible(protocolContainer.getProtocol().equals(Protocol.RDP));
	    rdpOptionsForm.setNumCols(1);
		
	    // Create header
	    HeaderItem rdpOptionsHeader = new HeaderItem();
	    rdpOptionsHeader.setDefaultValue("RDP Options");
	    rdpOptionsHeader.setTextBoxStyle("generalLabel");
	    
	    if (currentItem.getHasAdditionalConsole())
	    {			
    		    // Get RDP model
    		    final IRdp rdp = ((RdpConsoleModel)currentItem.getAdditionalConsole()).getrdp();
    		    
    		    // Create check boxes
    		    CheckboxItem useLocalDrives = new CheckboxItem("useLocalDrives", "Use Local Drives");
    		    useLocalDrives.setDefaultValue(rdp.getUseLocalDrives());
    		    useLocalDrives.setShowTitle(false);
    		    useLocalDrives.addChangedHandler(new ChangedHandler() {
    			@Override
    			public void onChanged(ChangedEvent event) {			    
    			    rdp.setUseLocalDrives((Boolean)event.getValue());				    
    			}
    		    });
    		
    		    // Add header and check boxes
    		    rdpOptionsForm.setItems(rdpOptionsHeader, useLocalDrives);
	    }
	    
	    return rdpOptionsForm;
	}
}
