package org.ovirt.engine.ui.userportal.client.modalpanels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.EntityModel;
import org.ovirt.engine.ui.uicommon.models.ListModel;
import org.ovirt.engine.ui.uicommon.models.vms.BootSequenceModel;
import org.ovirt.engine.ui.uicommon.models.vms.RunOnceModel;
import org.ovirt.engine.ui.userportal.client.components.CheckboxItemModelBinded;
import org.ovirt.engine.ui.userportal.client.components.ComboBoxListModelBinded;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import org.ovirt.engine.ui.userportal.client.components.Button;
import org.ovirt.engine.ui.userportal.client.components.SelectBoxListModelBinded;
import org.ovirt.engine.ui.userportal.client.components.TextItemEntityModelBinded;
import org.ovirt.engine.ui.userportal.client.views.extended.maingrid.MainGrid;
import org.ovirt.engine.ui.userportal.client.views.extended.maingrid.UserPortalItemsGrid;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DragStopEvent;
import com.smartgwt.client.widgets.events.DragStopHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.HeaderItem;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;

public class RunOnceModalPanel extends NonDraggableModalPanel {
    final RunOnceModalPanel newRunOnceModalPanel = this;

    List<TextItemEntityModelBindedWrapper> textItems = new ArrayList<TextItemEntityModelBindedWrapper>();

    public RunOnceModalPanel(final MainGrid mainGrid) {
	super(350, 560, "Run Virtual Machine(s)");

	final RunOnceModel runOnceModel = mainGrid.uplm.getRunOnceModel();	

	// Creating and set a inner panel
	VLayout inPanel = new VLayout();
	inPanel.setHeight100();
	inPanel.setWidth100();

	// Creating and set an indent spacer
	SpacerItem indentSpacer = new SpacerItem();
	indentSpacer.setWidth(3);
	indentSpacer.setHeight(5);

	/** Create 'Boot Options' section **/

	// Create 'Boot Options' label
	HeaderItem bootOptionsLabel = new HeaderItem();
	bootOptionsLabel.setDefaultValue("<nobr>Boot Options:</nobr>");
	bootOptionsLabel.setTextBoxStyle("runOncePrimaryLabel");

	// Create Floppy images selectbox
	final SelectBoxListModelBinded floppyImageBox = getImageSelectBox("Attach Floppy", runOnceModel.getFloppyImage(), !runOnceModel.getFloppyImage().getIsChangable());

	// Create 'Attach Floppy' checkbox
	CheckboxItemModelBinded attachFloppyCheckBox = getAttachCheckBox("Attach Floppy", runOnceModel.getFloppyImage(), runOnceModel.getAttachFloppy(), floppyImageBox);

	// Create CD images selectbox
	final SelectBoxListModelBinded cdImageBox = getImageSelectBox("Attach CD", runOnceModel.getIsoImage(), !runOnceModel.getIsoImage().getIsChangable());

	// Create 'Attach CD' checkbox
	CheckboxItem attachCdCheckBox = getAttachCheckBox("Attach CD", runOnceModel.getIsoImage(), runOnceModel.getAttachIso(),	cdImageBox);

	// Create 'Boot Sequence' label
	HeaderItem bootSequenceLabel = new HeaderItem();
	bootSequenceLabel.setDefaultValue("<nobr>Boot Sequence:</nobr>");
	bootSequenceLabel.setTextBoxStyle("runOnceSecondaryLabel");

	// Create 'Boot Sequence' grid
	final ListGrid bootSequenceGrid = getBootSequenceGrid(runOnceModel);

	// Insert 'bootSequenceGrid to panel
	VLayout bootSequenceGridLayout = new VLayout();
	bootSequenceGridLayout.addMember(bootSequenceGrid);
	bootSequenceGridLayout.setAutoHeight();

	// Create 'Stateless' and 'Start in Pause Mode' checkboxes
	CheckboxItemModelBinded statelessCheckBox = new CheckboxItemModelBinded("Run Stateless", runOnceModel.getRunAsStateless());
	CheckboxItemModelBinded startInPauseModeCheckBox = new CheckboxItemModelBinded("Start in Pause Mode", runOnceModel.getRunAndPause());

	// Create 'Linux Boot Options' label and inputs
	HeaderItem linuxBootOptionsLabel = new HeaderItem();
	linuxBootOptionsLabel.setDefaultValue("<nobr>Linux Boot Options:</nobr>");
	linuxBootOptionsLabel.setTextBoxStyle("runOnceSecondaryLabel");
	TextItemEntityModelBindedWrapper kernelPathInput = new TextItemEntityModelBindedWrapper("kernel path", runOnceModel.getKernel_path());
	TextItemEntityModelBindedWrapper initrdPathCDInput = new TextItemEntityModelBindedWrapper("initrd path", runOnceModel.getInitrd_path());
	TextItemEntityModelBindedWrapper kernelParametersInput = new TextItemEntityModelBindedWrapper("kernel params", runOnceModel.getKernel_parameters());

	// Create 'Windows Sysprep' components
    HeaderItem windowsSysprepLabel = new HeaderItem();
    windowsSysprepLabel.setDefaultValue("<nobr>Windows Sysprep:</nobr>");
    windowsSysprepLabel.setTextBoxStyle("runOnceSecondaryLabel");    
    ComboBoxListModelBinded sysPrepDomainNameInput = new ComboBoxListModelBinded("Domain", runOnceModel.getSysPrepDomainName(), String.class);
    CheckboxItemModelBinded useAlternateCredentialsCheckBox = new CheckboxItemModelBinded("Alternate Credentials", runOnceModel.getUseAlternateCredentials());
    TextItemEntityModelBindedWrapper sysPrepUserNameInput = new TextItemEntityModelBindedWrapper("User Name", runOnceModel.getSysPrepUserName());    
    
    // Create 'Sysprep password box'
    final PasswordItem sysPrepPasswordInput = new PasswordItem("password", "Password");
    sysPrepPasswordInput.setTitleAlign(Alignment.LEFT);
    sysPrepPasswordInput.setTextBoxStyle("engineTextItem");
    
    // Update 'SysPrepPassword' 'changeable' state
    runOnceModel.getSysPrepPassword().getPropertyChangedEvent().addListener(new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            PropertyChangedEventArgs evArgs = (PropertyChangedEventArgs) args;
            if (evArgs.PropertyName.equals("IsChangable")) {
                boolean isPasswordChangable = runOnceModel.getSysPrepPassword().getIsChangable();
                sysPrepPasswordInput.setDisabled(!isPasswordChangable);
                if (!isPasswordChangable) {
                    sysPrepPasswordInput.setValue((String)null);
                }
                else {
                    sysPrepPasswordInput.setValue("");
                }                    
            }
        }
    });
    runOnceModel.getSysPrepPassword().getPropertyChangedEvent().raise(this, new PropertyChangedEventArgs("IsChangable"));
    
	// Create 'display protocol' label
	HeaderItem displayProtocolsLabel = new HeaderItem();
	displayProtocolsLabel.setDefaultValue("<nobr>Display Protocol:</nobr>");
	displayProtocolsLabel.setTextBoxStyle("runOncePrimaryLabel");
	displayProtocolsLabel.setHeight(15);

	// Create 'display protocol' radiobuttons
	RadioGroupItem displayProtocolsRadio = new RadioGroupItem();
	LinkedHashMap<String, String> displayProtocols = new LinkedHashMap<String, String>();
	for (EntityModel displayProtocol : (ArrayList<EntityModel>) runOnceModel.getDisplayProtocol().getItems()) {
		displayProtocols.put(displayProtocol.getEntity().toString(), displayProtocol.getTitle());
	}
	displayProtocolsRadio.setValueMap(displayProtocols);
	displayProtocolsRadio.setShowTitle(false);
	displayProtocolsRadio.setCellHeight(10);
	displayProtocolsRadio.setDefaultValue(((EntityModel) runOnceModel.getDisplayProtocol().getSelectedItem()).getEntity().toString());
	displayProtocolsRadio.addChangedHandler(new ChangedHandler() {
	    @Override
	    public void onChanged(ChangedEvent event) {
			ListModel displayProtocolModel = (ListModel) runOnceModel.getDisplayProtocol();
			Iterable items = runOnceModel.getDisplayProtocol().getItems();
			for (Object o : items) {
			    if (event.getValue().toString().equals(((EntityModel) o).getEntity().toString())) {
					displayProtocolModel.setSelectedItem(o);
					break;
			    }
			}
	    }
	});

	/** Add components to panel **/

	// Set items inside a form
	final DynamicForm f = new DynamicForm();
	final DynamicForm f2 = new DynamicForm();
	final DynamicForm f3 = new DynamicForm();
	final DynamicForm f4 = new DynamicForm();
	final DynamicForm f5 = new DynamicForm();

	f.setNumCols(3);
	f2.setNumCols(3);
	f3.setNumCols(3);
	f4.setNumCols(3);
	f5.setNumCols(3);

	f.setItems(bootOptionsLabel, attachFloppyCheckBox, floppyImageBox,
		indentSpacer, attachCdCheckBox, cdImageBox, indentSpacer,
		indentSpacer, bootSequenceLabel);

	f2.setItems(statelessCheckBox, startInPauseModeCheckBox, indentSpacer,
		indentSpacer);

	f3.setItems(linuxBootOptionsLabel, kernelPathInput, initrdPathCDInput,
		kernelParametersInput, indentSpacer, indentSpacer);
	
	f4.setItems(windowsSysprepLabel, sysPrepDomainNameInput, useAlternateCredentialsCheckBox,
	        sysPrepUserNameInput, sysPrepPasswordInput, indentSpacer, indentSpacer);

	f5.setItems(indentSpacer, displayProtocolsLabel, displayProtocolsRadio);

	// Adding components to inner panel
	inPanel.addMember(f);
	inPanel.addMember(bootSequenceGridLayout);
	inPanel.addMember(f2);
	if (runOnceModel.getIsLinux_Unassign_UnknownOS())
	    inPanel.addMember(f3);
	else if (runOnceModel.getIsWindowsOS())
	    inPanel.addMember(f4);
	inPanel.addMember(f5);

	// Adding inner panel to this view
	addItem(inPanel);

	// Add footer buttons
	addFooterButtons(runOnceModel, mainGrid, sysPrepPasswordInput);
	
	// Update 'Sysprep' section visibility according to 'IsSysprepEnabled' value
	runOnceModel.getIsSysprepEnabled().getEntityChangedEvent().addListener(new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            f4.setVisible((Boolean)runOnceModel.getIsSysprepEnabled().getEntity());
        }
    });
	runOnceModel.getIsSysprepEnabled().getEntityChangedEvent().raise(this, new EventArgs());
	
    }

    private void addFooterButtons(final RunOnceModel runOnceModel, final MainGrid mainGrid, final PasswordItem sysPrepPasswordInput) {
	Button okButton = new Button("OK");
	okButton.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
	            if ((Boolean)runOnceModel.getIsSysprepEnabled().getEntity()) {
	                runOnceModel.getSysPrepPassword().setEntity((String) sysPrepPasswordInput.getValue());
	            }
	            mainGrid.uplm.ExecuteCommand(new UICommand("OnRunOnce", null));
			    newRunOnceModalPanel.destroy();
			    mainGrid.gridActionPerformed();
	    }
	});
	Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
		    @Override
		    public void onClick(ClickEvent event) {
		    	newRunOnceModalPanel.destroy();
		    }
		});
	
		setFooterButtons(Alignment.RIGHT, okButton, cancelButton);
    }

    private SelectBoxListModelBinded getImageSelectBox(String title, ListModel listModelToBind, Boolean disabled) {
		SelectBoxListModelBinded imageSelectBox = new SelectBoxListModelBinded(title, listModelToBind, String.class);
		imageSelectBox.setDisabled(disabled);
		imageSelectBox.setShowTitle(false);
	
		return imageSelectBox;
    }

    private CheckboxItemModelBinded getAttachCheckBox(String title, final ListModel imagesList, final EntityModel isAttached, final SelectBoxListModelBinded imageSelectBox) {
    	CheckboxItemModelBinded attachCheckBox = new CheckboxItemModelBinded(title, isAttached);
		attachCheckBox.setValue((Boolean) isAttached.getEntity());
		attachCheckBox.setShowTitle(false);
		attachCheckBox.setWidth(5);
		attachCheckBox.setColSpan(1);
		attachCheckBox.addChangedHandler(new ChangedHandler() {
		    @Override
		    public void onChanged(ChangedEvent event) {
				Boolean attach = (Boolean) ((CheckboxItem) event.getSource()).getValue();
				if (!attach) {
				    imagesList.setSelectedItem(null);
				} else if (imagesList.getSelectedItem() == null
					&& imagesList.getItems() != null) {
				    Iterator imagesIterator = imagesList.getItems().iterator();
				    Object firstItem = imagesIterator.hasNext() ? imagesIterator
					    .next() : null;
				    imagesList.setSelectedItem(firstItem);
				}
				isAttached.setEntity(attach);
				imageSelectBox.setDisabled(!attach);
		    }
		});
	
		return attachCheckBox;
    }

    private ListGrid getBootSequenceGrid(final RunOnceModel runOnceModel) {
		final ListGrid bootSequenceGrid = new ListGrid();
		bootSequenceGrid.setHeight(72);
		bootSequenceGrid.setWidth100();
		bootSequenceGrid.setShowAllRecords(true);
		bootSequenceGrid.setCanReorderRecords(true);
		bootSequenceGrid.setShowHeader(false);
		bootSequenceGrid.setShowEdges(true);
		bootSequenceGrid.setEdgeSize(2);
		bootSequenceGrid.setLeaveScrollbarGap(false);
	
		// Set 'Boot Sequence' grid data fields
		final RecordList bootDriveRecords = new RecordList();
		final ObservableCollection<EntityModel> bootDrives = (ObservableCollection<EntityModel>) runOnceModel.getBootSequence().getItems();
		if (bootDrives != null) {
		    for (EntityModel bootDrive : bootDrives) {
				final ListGridRecord r = new ListGridRecord();
		
				// Disable record if needed
				boolean isChangable = bootDrive.getIsChangable();
				r.setCanDrag(isChangable);
				if (!isChangable)
				    r.setCustomStyle("cellDisabledDark");
		
				// Set and add record
				r.setAttribute("bootDrive", bootDrive.getTitle());
				bootDriveRecords.add(r);
		
				// Listen to records' 'IsChangable' property
				bootDrive.getPropertyChangedEvent().addListener(
					new IEventListener() {
					    @Override
					    public void eventRaised(Event ev, Object sender, EventArgs args) {
							PropertyChangedEventArgs evArgs = (PropertyChangedEventArgs) args;
							if (evArgs.PropertyName.equals("IsChangable")) {
							    boolean isChangable = ((EntityModel) sender).getIsChangable();
							    r.setCanDrag(isChangable);
							    if (!isChangable) r.setCustomStyle("cellDisabledDark");
							    else r.setCustomStyle(null);
							    bootSequenceGrid.redraw();
							}
					    }
					});
		    }
		}
	
		// Create 'boot drive' grid-field
		ListGridField bootDriveField = new ListGridField("bootDrive", "");
		bootSequenceGrid.setFields(bootDriveField);
		bootSequenceGrid.setData(bootDriveRecords);
	
		// Update items order every drag-drop
		bootSequenceGrid.addDragStopHandler(new DragStopHandler() {
		    public void onDragStop(DragStopEvent event) {
			ObservableCollection<EntityModel> newBootDrivesOrder = new ObservableCollection<EntityModel>();
			for (Record bootDriveRecord : bootSequenceGrid.getRecords()) {
			    for (EntityModel bootDrive : bootDrives) {
				if (bootDriveRecord.getAttribute("bootDrive").equals(
					bootDrive.getTitle())) {
				    newBootDrivesOrder.add(bootDrive);
				}
			    }
			}
	
			BootSequenceModel bootSequenceModel = new BootSequenceModel();
			bootSequenceModel.setItems(newBootDrivesOrder);
			runOnceModel.setBootSequence(bootSequenceModel);
		    }
		});
	
		return bootSequenceGrid;
    }

    // Wraps TextItemEntityModelBinded in order to add all local components to
    // be added to a validation list
    class TextItemEntityModelBindedWrapper extends TextItemEntityModelBinded {
		public TextItemEntityModelBindedWrapper(String title, EntityModel model) {
		    super(title, model);
		    textItems.add(this);
		}
    }

}
