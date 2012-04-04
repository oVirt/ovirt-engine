package org.ovirt.engine.ui.userportal.client.modalpanels;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.EntityModel;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommon.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommon.models.vms.UnitVmModel;
import org.ovirt.engine.ui.userportal.client.binders.FormConstructor;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameIdResolver;
import org.ovirt.engine.ui.userportal.client.components.CheckboxItemModelBinded;
import org.ovirt.engine.ui.userportal.client.components.ComboBoxListModelBinded;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.GridRefreshManager;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import org.ovirt.engine.ui.userportal.client.components.SelectBoxListModelBinded;
import org.ovirt.engine.ui.userportal.client.components.SliderItemRangeModelBinded;
import org.ovirt.engine.ui.userportal.client.components.TextItemEntityModelBinded;
import org.ovirt.engine.ui.userportal.client.parsers.SizeParser;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.HeaderItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class NewVmModalPanel extends NonDraggableModalPanel {

	private UnitVmModel userVmModel;

	private static final int windowWidth = 460;
	private static final int windowHeight = 500;
	
	private static String noDatacenterAvailableMessage = "There are no active Data-Centers you can create your VM on. Please contact your administrator.";
	private static String vncConsoleSelectedMessage = "VNC is the selected Display Protocol and is not supported from the Power User Portal.";
	
	TabButton selectedButton;

	VLayout buttonsSide = new VLayout();
	final VLayout tabPaneContainer = new VLayout();

	UICommand cancelCommand;
	
	TabButton generalTabButton;
	TabButton windowsSysPrepTabButton;
	TabButton consoleTabButton;
	TabButton hostTabButton;
	TabButton highAvailabilityButton;
	TabButton resourceAllocationTabButton;
	TabButton bootOptionsTabButton;
	
	WarningLabel noDatacenterAvailableLabel;
	FormItem focusedItem;
	DynamicForm focusedForm;
	
	boolean isServer;

	boolean isTemplate;
	
	public NewVmModalPanel(final GridController gridController, UnitVmModel model, final Model parentModel) {
		super(windowWidth, windowHeight, model.getTitle(), 0);
		
		userVmModel = model;

		isTemplate = parentModel instanceof TemplateListModel;
		isServer = userVmModel.getVmType().equals(VmType.Server);
		
		generalTabButton = new TabButton("General", new GeneralTabPane());
		windowsSysPrepTabButton = new TabButton("Windows Sysprep", new WindowsSysPrepTabPane());
		consoleTabButton = new TabButton("Console", new ConsoleTabPane());
		hostTabButton = new TabButton("Host", new HostTabPane());
		highAvailabilityButton = new TabButton("High Availability", new HighAvailabilityTabPane());
		resourceAllocationTabButton = new TabButton("Resource Allocation", new ResourceAllocationTabPane());
		bootOptionsTabButton = new TabButton("Boot Options", new BootOptionsTabPane());				

		if (isTemplate) {
			hostTabButton.hide();
			resourceAllocationTabButton.hide();
		}
		
		final UICommand saveCommand = (parentModel instanceof UserPortalListModel) ? ((UserPortalListModel)parentModel).getSaveCommand() : new UICommand("OnSave", parentModel); 
		cancelCommand = new UICommand("Cancel", parentModel);
		
		parentModel.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				String propertyName = ((PropertyChangedEventArgs)args).PropertyName;
				GWT.log("DERBA: " + propertyName);
				if (propertyName.equals("Window") || propertyName.equals("VmModel")) {
					parentModel.getPropertyChangedEvent().removeListener(this);
					destroy();
				}
			}
		});
		
		org.ovirt.engine.ui.userportal.client.components.Button cancelButton = new org.ovirt.engine.ui.userportal.client.components.Button ("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onClose();
			}
		});
		
		org.ovirt.engine.ui.userportal.client.components.Button okButton = new org.ovirt.engine.ui.userportal.client.components.Button("OK");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveCommand.Execute();
				gridController.gridChangePerformed();
			}
		});
		
		setFooterButtons(Alignment.RIGHT, okButton, cancelButton);
		
		// By default those tabs are hidden until a Windows os or a non blank template is selected respectively 
		if (!userVmModel.getIsWindowsOS())
			windowsSysPrepTabButton.hide();
		if (!isTemplate && !isServer)
			highAvailabilityButton.hide();
		hostTabButton.setDisabled(!userVmModel.getIsHostAvailable());

		HLayout inPanel = new HLayout();
		inPanel.setHeight100();
		inPanel.setWidth100();
		buttonsSide.setStyleName("newVmDialogTabButtons");
		buttonsSide.setWidth(150);

		tabPaneContainer.setAutoWidth();
		tabPaneContainer.setHeight100();
		tabPaneContainer.setPadding(4);
		userVmModel.getPropertyChangedEvent().addListener(new UserVmModelPropertyChangedListener());

		inPanel.addMember(buttonsSide);
		inPanel.addMember(tabPaneContainer);		
		
		addItem(inPanel);
	}

	@Override
	public void draw() {
		super.draw();
		// Initialize the tab container to show the general tab first
		selectedButton = generalTabButton;
		selectedButton.select();
		
		GridRefreshManager.getInstance().suspendRefresh();
		subscribeProgressChangedEvent(userVmModel, focusedItem, tabPaneContainer);
	}
	
// Supertypes for all components
	
	class TabPaneSkeletal extends VLayout {
		public TabPaneSkeletal() {
			setHeight100();
			setWidth100();
		}
	}

	class TabButton extends Button {
		private Canvas pane;

		public TabButton(String title, Canvas pane) {
			super(title);
			setBaseStyle("tabButton");
			setWidth100();
			setHeight(27);			
			this.pane = pane;
			pane.setVisible(false);
			buttonsSide.addMember(this);
			tabPaneContainer.addMember(pane);

			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (selectedButton != TabButton.this) {
						selectedButton.deselect();
						selectedButton = TabButton.this;
						selectedButton.select();
					}
				}
			});
		}
		
		@Override
	    public void select() {
			super.select();
			pane.show();			
		}
		@Override
    	public void deselect() {
			super.deselect();
			pane.hide();			
		}
		
		public void setIsValid(boolean isValid) {
			 setStyleName(isValid ? "newVmTabButtonValid" : "newVmTabButtonInvalid");
		}
	}

	
	
// Tab panes
	
	class GeneralTabPane extends TabPaneSkeletal {
		public GeneralTabPane() {

			final SelectBoxListModelBinded dataCenterBox = new SelectBoxListModelBinded("Data Center", userVmModel.getDataCenter(), storage_pool.class);	
			
			final SelectBoxListModelBinded hostClusterBox = new SelectBoxListModelBinded("Host Cluster", userVmModel.getCluster(), VDSGroup.class);		
						
			final TextItemEntityModelBinded nameInput = new TextItemEntityModelBinded("Name", userVmModel.getName());

			final TextItemEntityModelBinded descriptionInput = new TextItemEntityModelBinded("Description", userVmModel.getDescription());
			
			final SelectBoxListModelBinded basedOnTemplateBox = new SelectBoxListModelBinded("Based on Template", userVmModel.getTemplate(), VmTemplate.class);
			
			final TextItemEntityModelBinded memoryInput = new TextItemEntityModelBinded("Memory Size", userVmModel.getMemSize(), true, new SizeParser());		

			final SliderItemRangeModelBinded totalCoresSlider = new SliderItemRangeModelBinded("Total Cores", 140, userVmModel.getTotalCPUCores(), "coresSlider");

			final SliderItemRangeModelBinded cpuSocketsSlider = new SliderItemRangeModelBinded("CPU Sockets", 140, userVmModel.getNumOfSockets(), "socketsSlider");

			//final TextItemEntityModelBinded totalCoresInput = new TextItemEntityModelBinded("Total Cores", userVmModel.getTotalCPUCores(), true);

			//final TextItemEntityModelBinded cpuSocketsInput = new TextItemEntityModelBinded("CPU Sockets", userVmModel.getNumOfSockets(), true);
			
			final SelectBoxListModelBinded operatingSystemBox = new SelectBoxListModelBinded(false, "Operating System", userVmModel.getOSType(), VmOsType.class);
			
			CheckboxItemModelBinded statelessCheckBox = new CheckboxItemModelBinded("Stateless", userVmModel.getIsStateless());
			
			DynamicForm f = new DynamicForm();
			if (isTemplate)
				f.setItems(dataCenterBox, hostClusterBox, nameInput, descriptionInput, memoryInput, totalCoresSlider, cpuSocketsSlider, operatingSystemBox, statelessCheckBox);
			else
				f.setItems(dataCenterBox, hostClusterBox, nameInput, descriptionInput, basedOnTemplateBox, memoryInput, totalCoresSlider, cpuSocketsSlider, operatingSystemBox, statelessCheckBox);

			focusedItem = nameInput;
			focusedForm = f;
			
			f.focusInItem(nameInput);
			//Not sure why this is needed as according to spec focusInItem should be enough, but does not work without setAutoFocus=true
			f.setAutoFocus(true);
		
			addMember(f);
			
            // Add 'no data-center is available' error message
            noDatacenterAvailableLabel = new WarningLabel(noDatacenterAvailableMessage);
                            
            // Add warning panel
            WarningPanel warningPanel = new WarningPanel();
            warningPanel.addMember(noDatacenterAvailableLabel);
            warningPanel.addMember(createVncConsoleSelectedMessageLabel());            
            addMember(warningPanel);
		}

	}

	class WindowsSysPrepTabPane extends TabPaneSkeletal {
		public WindowsSysPrepTabPane() {
			DynamicForm f = new DynamicForm();	

			ComboBoxListModelBinded domainInput = new ComboBoxListModelBinded("Domain", userVmModel.getDomain(), String.class);			
			SelectBoxListModelBinded timeZoneBox = new SelectBoxListModelBinded("Time Zone", userVmModel.getTimeZone(), Entry.class);

			domainInput.setWidth(200);
			timeZoneBox.setWidth(200);
			
			f.setItems(domainInput, timeZoneBox);
			addChild(f);
		}
	}
	
	
	class ConsoleTabPane extends TabPaneSkeletal{
		public ConsoleTabPane() {

			SelectBoxListModelBinded protocolBox = new SelectBoxListModelBinded(false, "Protocol", userVmModel.getDisplayProtocol(), new ObjectNameIdResolver() {
				public String getItemId(Object o) {
					return ((DisplayType)(((EntityModel)o).getEntity())).name();
				}
				public String getItemName(Object o) {
					return ((EntityModel)o).getTitle();
				}
			});			
			
			final SelectBoxListModelBinded usbPolicyBox = new SelectBoxListModelBinded(false, "USB Policy", userVmModel.getUsbPolicy(), UsbPolicy.class);		
			
			final SelectBoxListModelBinded monitorsBox = new SelectBoxListModelBinded(false, "Monitors", userVmModel.getNumOfMonitors(), Integer.class);		

			final CheckboxItemModelBinded allowConsoleReconnectCheckBox = new CheckboxItemModelBinded(
                "Allow a user to connect to the console of " +
                "<br/><span class=\"checkBoxWrapIndent\">" +
                "this VM when a different user has also " +
                "</span>" +
                "<br/><span class=\"checkBoxWrapIndent\">" +
                "connected since the last boot. Note: With " +
                "</span>" +
                "<br/><span class=\"checkBoxWrapIndent\">" +
                "this option selected, the VM will not need " +
                "</span>" +
                "<br/><span class=\"checkBoxWrapIndent\">" +
                "to be rebooted before allowing a new user " +
                "</span>" +
                "<br/><span class=\"checkBoxWrapIndent\">" +
                "to connect. This may result in exposure of " +
                "</span>" +
                "<br/><span class=\"checkBoxWrapIndent\">" +
                "the previous user's session under certain " +
                "</span>" +
                "<br/><span class=\"checkBoxWrapIndent\">" +
                "conditions." +
                "</span>",
	             userVmModel.getAllowConsoleReconnect()
	        );

			DynamicForm f;
			if (!isServer) {
			    f = FormConstructor.constructForm(protocolBox, usbPolicyBox, monitorsBox, allowConsoleReconnectCheckBox);
			}
			else {
 			    f = FormConstructor.constructForm(protocolBox, usbPolicyBox);
			}
			    
			addChild(f);
			
			// Add warning panel
			WarningPanel warningPanel = new WarningPanel();
			warningPanel.addMember(createVncConsoleSelectedMessageLabel());            
			addMember(warningPanel);
		}
	}
	
	
	
	
	class HostTabPane extends TabPaneSkeletal{
		public HostTabPane() {
			DynamicForm f = new DynamicForm();

			HeaderItem runOnLabel = new HeaderItem();
			runOnLabel.setDefaultValue("Run On:");
			runOnLabel.setTextBoxStyle("generalLabel");

			final RadioGroupItem specificHostRadio = new RadioGroupItem();
			LinkedHashMap<String,String> specificHostOptions = new LinkedHashMap<String, String>();
			specificHostOptions.put("true", "Any Host in Cluster");
			specificHostOptions.put("false", "Specific");
			specificHostRadio.setValueMap(specificHostOptions);
			specificHostRadio.setShowTitle(false);
			specificHostRadio.setWrap(false);
			specificHostRadio.setDefaultValue(((Boolean)userVmModel.getIsAutoAssign().getEntity()) == true ? "true" : "false");
			specificHostRadio.addChangedHandler(new ChangedHandler() {
				@Override
				public void onChanged(ChangedEvent event) {
					userVmModel.getIsAutoAssign().setEntity(event.getValue().equals("true") ? true : false);
				}
			});

			// TODO: Create 'RadiobuttonItemModelBinded'
			userVmModel.getIsAutoAssign().getPropertyChangedEvent().addListener(new IEventListener() {
				@Override
				public void eventRaised(Event ev, Object sender, EventArgs args) {
					specificHostRadio.setValue(((Boolean)userVmModel.getIsAutoAssign().getEntity()) == true ? "true" : "false");
				}
			});
			
			SelectBoxListModelBinded defaultHostBox = new SelectBoxListModelBinded("Default Host", userVmModel.getDefaultHost(), VDS.class);
			defaultHostBox.setShowTitle(false);
			defaultHostBox.setRowSpan(2);
			
			// Due to a bug in SmartGWT (Issue 206) the label of a checkboxitem does not wrap thus we have to wrap it ourselves and indent correctly in both checkboxes
			CheckboxItemModelBinded runOnlyOnSelectedHost = new CheckboxItemModelBinded("Run VM on the selected host <br/><span class=\"checkBoxWrapIndent\">(No migration allowed)</span>", userVmModel.getRunVMOnSpecificHost());
			CheckboxItemModelBinded dontMigrateVM = new CheckboxItemModelBinded("Allow VM migration only upon Administrator <br/><span class=\"checkBoxWrapIndent\">specific request (System will not trigger</span><br/><span class=\"checkBoxWrapIndent\">automatic migration of this VM)</span>", userVmModel.getDontMigrateVM());
			
			HeaderItem h = new HeaderItem();
			h.setDefaultValue("Run/Migration Options:");
			h.setTextBoxStyle("generalLabel");

			f.setItems(runOnLabel, specificHostRadio, defaultHostBox, new SpacerItem(), h, runOnlyOnSelectedHost, dontMigrateVM);
			addMember(f);
		}
	}

	class HighAvailabilityTabPane extends TabPaneSkeletal{
		public HighAvailabilityTabPane() {
			
			CheckboxItemModelBinded highlyAvailableCheckBox = new CheckboxItemModelBinded("Highly Available", userVmModel.getIsHighlyAvailable());
			
			SelectBoxListModelBinded priorityBox = new SelectBoxListModelBinded("Priority for Run/Migrate Queue", userVmModel.getPriority(), new ObjectNameIdResolver() {
				@Override
				public String getItemName(Object o) {
					return ((EntityModel)o).getTitle();
				}
				@Override
				public String getItemId(Object o) {
					return ((EntityModel)o).getEntity().toString();
				}
			});

			priorityBox.setWidth(100);
			
			DynamicForm f = FormConstructor.constructForm(highlyAvailableCheckBox, priorityBox);
			addChild(f);
		}
	}
	
	class ResourceAllocationTabPane extends TabPaneSkeletal{
		Label disksLabel;
		VLayout disksListLayout;
		Label storageAllacationLabelRemark;
		public ResourceAllocationTabPane() {

			DynamicForm f = new DynamicForm();
			DynamicForm f2 = new DynamicForm();
			f.setNumCols(3);
			f2.setNumCols(3);

			SpacerItem indentSpacer = new SpacerItem();
			indentSpacer.setWidth(3);
			
			Label storageAllocationLabel = new Label("<nobr>Storage Allocation:</nobr>");
			storageAllocationLabel.setAutoFit(true);
			storageAllocationLabel.setStyleName("generalLabel");

			storageAllacationLabelRemark = new Label("<nobr>(Available only when a template is selected)</nobr>");
			storageAllacationLabelRemark.setAutoFit(true);
			storageAllacationLabelRemark.setStyleName("generalLabelRemark");

			VLayout storageAllacationLabelLayout = new VLayout();
			storageAllacationLabelLayout.setAutoHeight();
			storageAllacationLabelLayout.setAutoWidth();
			storageAllacationLabelLayout.addMember(storageAllocationLabel);
			storageAllacationLabelLayout.addMember(storageAllacationLabelRemark);

			SelectBoxListModelBinded storageDomainBox = new SelectBoxListModelBinded("Storage Domain", userVmModel.getStorageDomain(), storage_domains.class);		
			
			SelectBoxListModelBinded provisioningBox = new SelectBoxListModelBinded(false, "Provisioning", userVmModel.getProvisioning(), new ObjectNameIdResolver() {
				public String getItemName(Object o) {
					return ((EntityModel)o).getTitle();
				}
				public String getItemId(Object o) {
					return (Boolean)((EntityModel)o).getEntity() ? "true" : "false";
				}
			});

			HeaderItem memoryAllocationLabel = new HeaderItem();
			memoryAllocationLabel.setDefaultValue("Memory Allocation:");
			memoryAllocationLabel.setTextBoxStyle("generalLabel");

			TextItemEntityModelBinded minAllocatedMemoryInput = new TextItemEntityModelBinded("Physical Memory Guaranteed", userVmModel.getMinAllocatedMemory(), true, new SizeParser());
			minAllocatedMemoryInput.setWidth(100);			
			
			disksLabel = new Label("<nobr>Disks:</nobr>");
			disksLabel.setAutoFit(true);
			disksLabel.setStyleName("generalLabel");
			
			disksListLayout = new VLayout();
			disksListLayout.setAutoHeight();
			
			disksLabel.hide();
			disksListLayout.hide();


			f.setItems(indentSpacer, storageDomainBox, indentSpacer, provisioningBox, indentSpacer);
			f2.setItems(memoryAllocationLabel, indentSpacer, minAllocatedMemoryInput);
			addMember(storageAllacationLabelLayout);
			addMember(f);
			addMember(disksLabel);
			addMember(disksListLayout);
			addMember(f2);
		}

		public void setDisksLabelVisibility() {
			if (userVmModel.getIsDisksAvailable()) {
				disksLabel.show();
				disksListLayout.show();
			}
			else {
				disksLabel.hide();
				disksListLayout.hide();
			}
		}

		public void setStorageLabelRemarkVisibility() {
			if (userVmModel.getIsBlankTemplate()) {
				storageAllacationLabelRemark.show();
			}
			else {
				storageAllacationLabelRemark.hide();
			}
		}
	
		public void updateDisksList() {
			disksListLayout.removeMembers(disksListLayout.getMembers());
			List<DiskModel> diskModelList = userVmModel.getDisks();
			if (diskModelList == null)
			{
				return;
			}
			for (DiskModel dm : userVmModel.getDisks()) {
				HLayout diskLayout = new HLayout();
				
				SelectBoxListModelBinded diskAllocationBox = new SelectBoxListModelBinded(false, "Disk " + dm.getName() + " - " + dm.getSize().getEntity().toString() + "GB", dm.getVolumeType(), VolumeType.class);
				
				diskLayout.addMember(FormConstructor.constructForm(diskAllocationBox));
				diskLayout.setAutoHeight();
				diskLayout.setAutoWidth();
				disksListLayout.addMember(diskLayout);
			}
		}
		
	}

	class BootOptionsTabPane extends TabPaneSkeletal{
		
		public TextItemEntityModelBinded kernelPathInput, initrdPathCDInput, kernelParametersInput;
		public HeaderItem linuxBootOptionsLabel;
		
		public BootOptionsTabPane() {

			DynamicForm f = new DynamicForm();
			f.setNumCols(3);
			
			SpacerItem indentSpacer = new SpacerItem();
			indentSpacer.setWidth(3);
			
			final SelectBoxListModelBinded firstDeviceBox = new SelectBoxListModelBinded(false, "First Device", userVmModel.getFirstBootDevice(), new ObjectNameIdResolver() {
				public String getItemId(Object o) {
					if (((EntityModel)o).getEntity() == null)
						return null;
					return (((BootSequence)((EntityModel)o).getEntity())).name();
				}
				public String getItemName(Object o) {
					if (((EntityModel)o).getEntity() == null)
						return "[None]";
					return ((EntityModel)o).getTitle();
				}
			});					
					
			final SelectBoxListModelBinded secondDeviceBox = new SelectBoxListModelBinded(false, "Second Device", userVmModel.getSecondBootDevice(), new ObjectNameIdResolver() {
				public String getItemId(Object o) {
					if (o == null)
						return null;
					if (((EntityModel)o).getEntity() == null)
						return "[None]";
					return (((BootSequence)((EntityModel)o).getEntity())).name();
				}
				public String getItemName(Object o) {
					return ((EntityModel)o).getTitle();
				}
			});		
			
			final CheckboxItem attachCdCheckBox = new CheckboxItem(); 
			attachCdCheckBox.setValue(userVmModel.getCdImage().getIsChangable());
			attachCdCheckBox.setShowTitle(false);
			attachCdCheckBox.setTitle("Attach CD");
			attachCdCheckBox.setWidth(5);
			
			// bind 'attachCdCheckBox' to the entity model
			userVmModel.getCdImage().getPropertyChangedEvent().addListener(new IEventListener() {
	            @Override
	            public void eventRaised(Event ev, Object sender, EventArgs args) {
	                String property = ((PropertyChangedEventArgs)args).PropertyName;

	                if (property.equals("IsChangable")) {
	                    attachCdCheckBox.setValue(userVmModel.getCdImage().getIsChangable());
	                }
	            }
	        });

			final SelectBoxListModelBinded cdImageBox = new SelectBoxListModelBinded("Attach CD", userVmModel.getCdImage(), String.class);
			cdImageBox.setDisabled(!userVmModel.getCdImage().getIsChangable());
			cdImageBox.setShowTitle(false);
			
			attachCdCheckBox.addChangedHandler(new ChangedHandler() {
				@Override
				public void onChanged(ChangedEvent event) {
					Boolean attachCd = (Boolean)((CheckboxItem)event.getSource()).getValue();
					if (!attachCd) {
						userVmModel.getCdImage().setSelectedItem(null);
					}
					userVmModel.getCdImage().setIsChangable(attachCd);					
					cdImageBox.setDisabled(!attachCd);
				}
			});
			
			kernelPathInput = new TextItemEntityModelBinded("kernel path", userVmModel.getKernel_path());
			
			initrdPathCDInput = new TextItemEntityModelBinded("initrd path", userVmModel.getInitrd_path());

			kernelParametersInput = new TextItemEntityModelBinded("kernel parameters", userVmModel.getKernel_parameters());

			HeaderItem bootSequenceLabel = new HeaderItem();
			bootSequenceLabel.setDefaultValue("<nobr>Boot Sequence:</nobr>");
			bootSequenceLabel.setTextBoxStyle("generalLabel");

			linuxBootOptionsLabel = new HeaderItem();
			linuxBootOptionsLabel.setDefaultValue("<nobr>Linux Boot Options:</nobr>");
			linuxBootOptionsLabel.setTextBoxStyle("generalLabel");			
			
			f.setItems(bootSequenceLabel, indentSpacer, firstDeviceBox, indentSpacer, secondDeviceBox, indentSpacer, attachCdCheckBox, cdImageBox, indentSpacer, linuxBootOptionsLabel, indentSpacer, kernelPathInput, indentSpacer, initrdPathCDInput, indentSpacer, kernelParametersInput);
			addMember(f);
		}
		
		public void setLinuxBootOptionsVisibility(boolean visible) {
			if (visible) {
				linuxBootOptionsLabel.show();
			}
			else {
				linuxBootOptionsLabel.hide();
			}
		}
	}

	class CustomPropertiesTabPane extends TabPaneSkeletal{
		public CustomPropertiesTabPane() {
	
			DynamicForm f = new DynamicForm();

			TextItemEntityModelBinded customPropertiesInput = new TextItemEntityModelBinded("Custom Properties", userVmModel.getCustomProperties());
						
			f.setItems(customPropertiesInput);
			addChild(f);
		}
	}
	
	class WarningPanel extends VLayout {
        public WarningPanel() {                        
            setHeight100();
            setWidth(260);
            setAlign(VerticalAlignment.BOTTOM); 
        }       
    }
    
    class WarningLabel extends Label {
        public WarningLabel(String contents) {
            super(contents);
                        
            setStyleName("warningLabel");
            setVisible(false);
            setAutoHeight();                        
            setPadding(5);            
            setWrap(true); 
        }       
    }
    
    // Create a warning label indicating VNC display protocol is selected
    // (add a listener to DisplayPotocol list model for determining visibility)
    private WarningLabel createVncConsoleSelectedMessageLabel() {
        final WarningLabel vncConsoleSelectedMessageLabel = new WarningLabel(vncConsoleSelectedMessage);                        
        userVmModel.getDisplayProtocol().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel selectedDisplayProtocol = (EntityModel)userVmModel.getDisplayProtocol().getSelectedItem();
                boolean isVncProtocolSelected = selectedDisplayProtocol != null && selectedDisplayProtocol.getEntity().equals(DisplayType.vnc);
                vncConsoleSelectedMessageLabel.setVisible(isVncProtocolSelected);
            }            
        });
        
        return vncConsoleSelectedMessageLabel;
    }
	
	class UserVmModelPropertyChangedListener implements IEventListener {
		@Override
		public void eventRaised(Event ev, Object sender, EventArgs args) {

			String changedPropertyName = ((PropertyChangedEventArgs)args).PropertyName;
			
			if (changedPropertyName.equals("IsWindowsOS")) {

				if (!userVmModel.getIsWindowsOS()) {
					windowsSysPrepTabButton.hide();
				}
				else {
					windowsSysPrepTabButton.show();
				}
			}
			
			else if (changedPropertyName.equals("IsBlankTemplate")) {
				((ResourceAllocationTabPane)resourceAllocationTabButton.pane).setStorageLabelRemarkVisibility();
			}

			else if (changedPropertyName.equals("IsLinux_Unassign_UnknownOS")) {
				((BootOptionsTabPane)bootOptionsTabButton.pane).setLinuxBootOptionsVisibility(userVmModel.getIsLinux_Unassign_UnknownOS());
			}

			else if (changedPropertyName.equals("IsDisksAvailable")) {
				((ResourceAllocationTabPane)resourceAllocationTabButton.pane).setDisksLabelVisibility();
			}
			
			else if (changedPropertyName.equals("Disks")) {
				((ResourceAllocationTabPane)resourceAllocationTabButton.pane).updateDisksList();
			}

			else if (changedPropertyName.equals("IsHostAvailable")){
				hostTabButton.setDisabled(!userVmModel.getIsHostAvailable());
			}
			
            else if (changedPropertyName.equals("IsDatacenterAvailable")){
               noDatacenterAvailableLabel.setVisible(!userVmModel.getIsDatacenterAvailable());
            }
			
			else if (changedPropertyName.equals("IsGeneralTabValid")) {
				generalTabButton.setIsValid(userVmModel.getIsGeneralTabValid());
			}
			else if (changedPropertyName.equals("IsDisplayTabValid")) {
				consoleTabButton.setIsValid(userVmModel.getIsDisplayTabValid());
			}
			else if (changedPropertyName.equals("IsFirstRunTabValid")) {
				windowsSysPrepTabButton.setIsValid(userVmModel.getIsFirstRunTabValid());
			}
			else if (changedPropertyName.equals("IsAllocationTabValid")) {
				resourceAllocationTabButton.setIsValid(userVmModel.getIsAllocationTabValid());
			}
			else if (changedPropertyName.equals("IsHostTabValid")) {
				hostTabButton.setIsValid(userVmModel.getIsHostTabValid());
			}
			else if (changedPropertyName.equals("IsBootSequenceTabValid")) {
				bootOptionsTabButton.setIsValid(userVmModel.getIsBootSequenceTabValid());
			}
		
		}
	}

	@Override
	public void onClose() {
		cancelCommand.Execute();
	}
}
