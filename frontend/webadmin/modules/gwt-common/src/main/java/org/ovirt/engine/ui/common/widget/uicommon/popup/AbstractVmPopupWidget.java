package org.ovirt.engine.ui.common.widget.uicommon.popup;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelSliderWithTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.parser.MemorySizeParser;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.MemorySizeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.storage.DisksAllocationView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;

public abstract class AbstractVmPopupWidget extends AbstractModelBoundPopupWidget<UnitVmModel> {

    interface Driver extends SimpleBeanEditorDriver<UnitVmModel, AbstractVmPopupWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<DialogTabPanel, AbstractVmPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    protected interface Style extends CssResource {
        String longCheckboxContent();

        String provisioningEditorContent();

        String provisioningRadioContent();

        String cdAttachedLabelWidth();

        String assignedVmsLabel();
    }

    @UiField
    protected Style style;

    // ==General Tab==
    @UiField
    protected DialogTab generalTab;
    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    ListModelListBoxEditor<Object> dataCenterEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    ListModelListBoxEditor<Object> clusterEditor;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    ListModelListBoxEditor<Object> quotaEditor;

    @UiField
    @Path(value = "name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Ignore
    KeyValueWidget customPropertiesSheetEditor;

    @UiField(provided = true)
    @Path(value = "template.selectedItem")
    protected ListModelListBoxEditor<Object> templateEditor;

    @UiField(provided = true)
    @Path(value = "memSize.entity")
    EntityModelTextBoxEditor memSizeEditor;

    @UiField(provided = true)
    @Path(value = "totalCPUCores.entity")
    EntityModelSliderWithTextBoxEditor totalCPUCoresEditor;

    @UiField(provided = true)
    @Path(value = "numOfSockets.entity")
    EntityModelSliderWithTextBoxEditor numOfSocketsEditor;

    @UiField(provided = true)
    @Path(value = "oSType.selectedItem")
    ListModelListBoxEditor<Object> oSTypeEditor;

    @UiField
    @Ignore
    Label generalWarningMessage;

    // == Pools ==
    @UiField
    protected DialogTab poolTab;

    @UiField(provided = true)
    @Path(value = "poolType.selectedItem")
    ListModelListBoxEditor<Object> poolTypeEditor;

    @UiField
    @Path(value = "assignedVms.entity")
    protected EntityModelTextBoxEditor assignedVmsEditor;

    @UiField
    @Ignore
    protected Label numOfVmsLabel;

    @UiField
    @Path(value = "numOfDesktops.entity")
    protected EntityModelTextBoxEditor numOfDesktopsEditor;

    @UiField
    @Path(value = "prestartedVms.entity")
    protected EntityModelTextBoxEditor prestartedVmsEditor;

    @UiField
    @Ignore
    protected Label prestartedVmsHintLabel;

    // ==Windows Prep Tab==
    @UiField
    protected DialogTab windowsSysPrepTab;

    @UiField(provided = true)
    @Path(value = "domain.selectedItem")
    ListModelListBoxEditor<Object> domainEditor;

    @UiField(provided = true)
    @Path(value = "timeZone.selectedItem")
    ListModelListBoxEditor<Object> timeZoneEditor;

    // ==Console Tab==
    @UiField
    DialogTab consoleTab;

    @UiField(provided = true)
    @Path(value = "displayProtocol.selectedItem")
    ListModelListBoxEditor<Object> displayProtocolEditor;

    @UiField(provided = true)
    @Path(value = "usbPolicy.selectedItem")
    ListModelListBoxEditor<Object> usbPolicyEditor;

    @UiField(provided = true)
    @Path(value = "numOfMonitors.selectedItem")
    protected ListModelListBoxEditor<Object> numOfMonitorsEditor;

    @UiField(provided = true)
    @Path(value = "isStateless.entity")
    protected EntityModelCheckBoxEditor isStatelessEditor;

    @UiField(provided = true)
    @Path(value = "allowConsoleReconnect.entity")
    protected EntityModelCheckBoxEditor allowConsoleReconnectEditor;

    // ==Host Tab==
    @UiField
    protected DialogTab hostTab;

    @UiField(provided = true)
    @Path(value = "runVMOnSpecificHost.entity")
    EntityModelCheckBoxEditor runVMOnSpecificHostEditor;

    @UiField(provided = true)
    @Path(value = "dontMigrateVM.entity")
    protected EntityModelCheckBoxEditor dontMigrateVMEditor;

    @UiField(provided = true)
    @Ignore
    RadioButton specificHost;

    @UiField(provided = true)
    @Path(value = "defaultHost.selectedItem")
    ListModelListBoxEditor<Object> defaultHostEditor;

    @UiField(provided = true)
    @Path(value = "isAutoAssign.entity")
    EntityModelRadioButtonEditor isAutoAssignEditor;

    // ==High Availability Tab==
    @UiField
    protected DialogTab highAvailabilityTab;

    @UiField(provided = true)
    @Path(value = "isHighlyAvailable.entity")
    EntityModelCheckBoxEditor isHighlyAvailableEditor;

    // TODO: Priority is a ListModel which is rendered as RadioBox
    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel> priorityEditor;

    // ==Resource Allocation Tab==
    @UiField
    protected DialogTab resourceAllocationTab;

    @UiField
    protected FlowPanel storageAllocationPanel;

    @UiField
    protected HorizontalPanel provisionSelectionPanel;

    @UiField
    protected FlowPanel disksAllocationPanel;

    @UiField
    @Ignore
    ListModelListBoxEditor<Object> provisioningEditor;

    @UiField(provided = true)
    @Path(value = "minAllocatedMemory.entity")
    EntityModelTextBoxEditor minAllocatedMemoryEditor;

    @UiField(provided = true)
    @Path(value = "provisioningThin_IsSelected.entity")
    EntityModelRadioButtonEditor provisioningThinEditor;

    @UiField(provided = true)
    @Path(value = "provisioningClone_IsSelected.entity")
    EntityModelRadioButtonEditor provisioningCloneEditor;

    @UiField
    @Ignore
    Label disksAllocationLabel;

    @UiField(provided = true)
    @Ignore
    DisksAllocationView disksAllocationView;

    // ==Boot Options Tab==
    @UiField
    DialogTab bootOptionsTab;

    @UiField(provided = true)
    @Path(value = "firstBootDevice.selectedItem")
    ListModelListBoxEditor<Object> firstBootDeviceEditor;

    @UiField(provided = true)
    @Path(value = "secondBootDevice.selectedItem")
    ListModelListBoxEditor<Object> secondBootDeviceEditor;

    @UiField(provided = true)
    @Path(value = "cdImage.selectedItem")
    ListModelListBoxEditor<Object> cdImageEditor;

    @UiField(provided = true)
    @Path(value = "cdAttached.entity")
    EntityModelCheckBoxEditor cdAttachedEditor;

    @UiField
    protected FlowPanel linuxBootOptionsPanel;

    @UiField
    @Path(value = "kernel_path.entity")
    EntityModelTextBoxEditor kernel_pathEditor;

    @UiField
    @Path(value = "initrd_path.entity")
    EntityModelTextBoxEditor initrd_pathEditor;

    @UiField
    @Path(value = "kernel_parameters.entity")
    EntityModelTextBoxEditor kernel_parametersEditor;

    // @UiField
    // @Path(value = "isHighlyAvailable.entity")
    // EntityModelCheckBoxEditor ;

    // ==Custom Properties Tab==
    @UiField
    protected DialogTab customPropertiesTab;

    CommonApplicationConstants constants;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AbstractVmPopupWidget(CommonApplicationConstants constants) {
        this.constants = constants;

        initListBoxEditors();

        // Contains a special parser/renderer
        memSizeEditor = new EntityModelTextBoxEditor(
                new MemorySizeRenderer(constants), new MemorySizeParser());
        minAllocatedMemoryEditor = new EntityModelTextBoxEditor(
                new MemorySizeRenderer(constants), new MemorySizeParser());
        totalCPUCoresEditor = new EntityModelSliderWithTextBoxEditor(1, 16);
        numOfSocketsEditor = new EntityModelSliderWithTextBoxEditor(1, 16);

        // TODO: How to align right without creating the widget manually?
        runVMOnSpecificHostEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        dontMigrateVMEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isHighlyAvailableEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isStatelessEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        cdAttachedEditor = new EntityModelCheckBoxEditor(Align.LEFT);
        allowConsoleReconnectEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        priorityEditor = new EntityModelCellTable<ListModel>(
                (Resources) GWT.create(ButtonCellTableResources.class));
        priorityEditor.addEntityModelColumn(new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                return model.getTitle();
            }
        }, ""); //$NON-NLS-1$

        disksAllocationView = new DisksAllocationView(constants);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        applyStyles();

        // Default is false
        windowsSysPrepTab.setVisible(false);
        poolTab.setVisible(false);

        localize(constants);
        Driver.driver.initialize(this);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void initListBoxEditors() {
        // General tab
        dataCenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_pool) object).getname();
            }
        });

        clusterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDSGroup) object).getname();
            }
        });

        quotaEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Quota) object).getQuotaName();
            }
        });

        templateEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VmTemplate) object).getname();
            }
        });

        oSTypeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        // Pools
        poolTypeEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((EntityModel) object).getTitle();
            }
        });

        // Windows Sysprep
        domainEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return object.toString();
            }
        });

        timeZoneEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Entry<String, String>) object).getValue();
            }
        });

        // Console tab
        displayProtocolEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((EntityModel) object).getTitle();
            }
        });

        usbPolicyEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        numOfMonitorsEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return object.toString();
            }
        });

        // Host Tab
        specificHost = new RadioButton("runVmOnHostGroup"); //$NON-NLS-1$
        isAutoAssignEditor = new EntityModelRadioButtonEditor("runVmOnHostGroup"); //$NON-NLS-1$
        defaultHostEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDS) object).getvds_name();
            }
        });

        // Resource Allocation
        provisioningThinEditor = new EntityModelRadioButtonEditor("provisioningGroup"); //$NON-NLS-1$
        provisioningCloneEditor = new EntityModelRadioButtonEditor("provisioningGroup"); //$NON-NLS-1$

        // Boot Options Tab
        firstBootDeviceEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((EntityModel) object).getTitle();
            }
        });

        secondBootDeviceEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((EntityModel) object).getTitle();
            }
        });

        cdImageEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return (String) object;
            }
        });
    }

    protected void localize(CommonApplicationConstants constants) {
        // Tabs
        highAvailabilityTab.setLabel(constants.highAvailVmPopup());
        resourceAllocationTab.setLabel(constants.resourceAllocVmPopup());
        bootOptionsTab.setLabel(constants.bootOptionsVmPopup());
        customPropertiesTab.setLabel(constants.customPropsVmPopup());

        // General Tab
        generalTab.setLabel(constants.GeneralVmPopup());
        dataCenterEditor.setLabel(constants.dcVmPopup());
        clusterEditor.setLabel(constants.hostClusterVmPopup());
        quotaEditor.setLabel(constants.quotaVmPopup());
        nameEditor.setLabel(constants.nameVmPopup());
        descriptionEditor.setLabel(constants.descriptionVmPopup());
        templateEditor.setLabel(constants.basedOnTemplateVmPopup());
        memSizeEditor.setLabel(constants.memSizeVmPopup());
        totalCPUCoresEditor.setLabel(constants.totalCoresVmPopup());
        numOfSocketsEditor.setLabel(constants.cpuSocketsVmPopup());
        oSTypeEditor.setLabel(constants.osVmPopup());
        isStatelessEditor.setLabel(constants.statelessVmPopup());

        // Pools Tab
        poolTab.setLabel(constants.poolVmPopup());
        poolTypeEditor.setLabel(constants.poolTypeVmPopup());
        numOfVmsLabel.setText(constants.numOfVmsPoolPopup());
        assignedVmsEditor.setLabel(constants.totalPoolPopup());
        numOfDesktopsEditor.setLabel("+"); //$NON-NLS-1$
        prestartedVmsEditor.setLabel(constants.prestartedPoolPopup());

        // Windows Sysprep Tab
        windowsSysPrepTab.setLabel(constants.windowsSysprepVmPopup());
        domainEditor.setLabel(constants.domainVmPopup());
        timeZoneEditor.setLabel(constants.tzVmPopup());

        // Console Tab
        consoleTab.setLabel(constants.consoleVmPopup());
        displayProtocolEditor.setLabel(constants.protocolVmPopup());
        usbPolicyEditor.setLabel(constants.usbPolicyVmPopup());
        numOfMonitorsEditor.setLabel(constants.monitorsVmPopup());
        allowConsoleReconnectEditor.setLabel(constants.allowConsoleReconnect());

        // Host Tab
        hostTab.setLabel(constants.hostVmPopup());
        isAutoAssignEditor.setLabel(constants.anyHostInClusterVmPopup());
        // specificHostEditor.setLabel("Specific");
        runVMOnSpecificHostEditor.setLabel(constants.runOnSelectedHostVmPopup());
        dontMigrateVMEditor.setLabel(constants.allowMigrationOnlyAdminVmPopup());

        // High Availability Tab
        isHighlyAvailableEditor.setLabel(constants.highlyAvailableVmPopup());

        // Resource Allocation Tab
        provisioningEditor.setLabel(constants.templateProvisVmPopup());
        provisioningThinEditor.setLabel(constants.thinVmPopup());
        provisioningCloneEditor.setLabel(constants.cloneVmPopup());
        minAllocatedMemoryEditor.setLabel(constants.physMemGuarVmPopup());

        // Boot Options
        firstBootDeviceEditor.setLabel(constants.firstDeviceVmPopup());
        secondBootDeviceEditor.setLabel(constants.secondDeviceVmPopup());
        kernel_pathEditor.setLabel(constants.kernelPathVmPopup());
        initrd_pathEditor.setLabel(constants.initrdPathVmPopup());
        kernel_parametersEditor.setLabel(constants.kernelParamsVmPopup());
    }

    private void applyStyles() {
        runVMOnSpecificHostEditor.addContentWidgetStyleName(style.longCheckboxContent());
        dontMigrateVMEditor.addContentWidgetStyleName(style.longCheckboxContent());
        allowConsoleReconnectEditor.addContentWidgetStyleName(style.longCheckboxContent());
        provisioningEditor.addContentWidgetStyleName(style.provisioningEditorContent());
        provisioningThinEditor.addContentWidgetStyleName(style.provisioningRadioContent());
        provisioningCloneEditor.addContentWidgetStyleName(style.provisioningRadioContent());
        cdAttachedEditor.addContentWidgetStyleName(style.cdAttachedLabelWidth());
    }

    @Override
    public void edit(UnitVmModel object) {
        priorityEditor.setRowData(new ArrayList<EntityModel>());
        priorityEditor.edit(object.getPriority());
        Driver.driver.edit(object);
        initSliders(object);
        initTabAvailabilityListeners(object);
        initListeners(object);
        initCustomPropertySheet(object);

        numOfVmsLabel.setVisible(false);
    }

    private void initCustomPropertySheet(final UnitVmModel object) {
        object.getCustomPropertySheet().getKeyValueLines().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                customPropertiesSheetEditor.edit(object.getCustomPropertySheet());
            }
        });
    }

    private void initListeners(final UnitVmModel object) {
        // TODO should be handled by the core framework
        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsHostAvailable".equals(propName)) { //$NON-NLS-1$
                    hostTab.setVisible(object.getIsHostAvailable());
                } else if ("IsHostTabValid".equals(propName)) { //$NON-NLS-1$
                    if (object.getIsHostTabValid()) {
                        hostTab.markAsValid();
                    } else {
                        hostTab.markAsInvalid(null);
                    }
                }
            }
        });

        // only for non local storage available
        hostTab.setVisible(object.getIsHostAvailable());

        object.getStorageDomain().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                addDiskAllocation(object);
            }
        });

        object.getProvisioning().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isProvisioningChangable = object.getProvisioning().getIsChangable();
                provisioningThinEditor.setEnabled(isProvisioningChangable);
                provisioningCloneEditor.setEnabled(isProvisioningChangable);

                boolean isProvisioningAvailable = object.getProvisioning().getIsAvailable();
                provisionSelectionPanel.setVisible(isProvisioningAvailable);

                boolean isDisksAvailable = object.getIsDisksAvailable();
                disksAllocationPanel.setVisible(isDisksAvailable);

                storageAllocationPanel.setVisible(isProvisioningAvailable || isDisksAvailable);
            }
        });

        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

                PropertyChangedEventArgs e = (PropertyChangedEventArgs) args;

                if (e.PropertyName == "PrestartedVmsHint") { //$NON-NLS-1$
                    prestartedVmsHintLabel.setText(object.getPrestartedVmsHint());
                }
            }
        });
    }

    private void addDiskAllocation(UnitVmModel model) {
        ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) model.getStorageDomain().getItems();
        if (!model.getIsDisksAvailable()) {
            return;
        }
        disksAllocationView.edit(model.getDisksAllocationModel());
        model.getDisksAllocationModel().getStorageDomain().setItems(model.getStorageDomain().getItems());
        model.getDisksAllocationModel().setDisks(model.getDisks());
    }

    private void initSliders(final UnitVmModel object) {
        object.getTotalCPUCores().getPropertyChangedEvent()
                .addListener(new IEventListener() {
                    @Override
                    public void eventRaised(Event ev, Object sender,
                            EventArgs args) {
                        PropertyChangedEventArgs rangeArgs = (PropertyChangedEventArgs) args;
                        if (rangeArgs.PropertyName.equalsIgnoreCase("Min")) { //$NON-NLS-1$
                            totalCPUCoresEditor.setMin(((Double) object
                                    .getTotalCPUCores().getMin()).intValue());
                        } else if (rangeArgs.PropertyName
                                .equalsIgnoreCase("Max")) { //$NON-NLS-1$
                            totalCPUCoresEditor.setMax(((Double) object
                                    .getTotalCPUCores().getMax()).intValue());
                        } else if (rangeArgs.PropertyName
                                .equalsIgnoreCase("Interval")) { //$NON-NLS-1$
                            totalCPUCoresEditor.setStepSize(((Double) object
                                    .getTotalCPUCores().getInterval())
                                    .intValue());
                        }
                    }
                });
    }

    private void initTabAvailabilityListeners(final UnitVmModel vm) {
        // TODO should be handled by the core framework
        vm.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsWindowsOS".equals(propName)) { //$NON-NLS-1$
                    if (vm.getIsWindowsOS()) {
                        windowsSysPrepTab.setVisible(true);
                    } else {
                        windowsSysPrepTab.setVisible(false);
                    }
                } else if ("IsGeneralTabValid".equals(propName)) { //$NON-NLS-1$
                    if (vm.getIsGeneralTabValid()) {
                        generalTab.markAsValid();
                    } else {
                        generalTab.markAsInvalid(null);
                    }
                } else if ("IsDisplayTabValid".equals(propName)) { //$NON-NLS-1$
                    if (vm.getIsDisplayTabValid()) {
                        consoleTab.markAsValid();
                    } else {
                        consoleTab.markAsInvalid(null);
                    }
                } else if ("IsAllocationTabValid".equals(propName)) { //$NON-NLS-1$
                    if (vm.getIsAllocationTabValid()) {
                        resourceAllocationTab.markAsValid();
                    } else {
                        resourceAllocationTab.markAsInvalid(null);
                    }
                } else if ("IsHighlyAvailable".equals(propName)) { //$NON-NLS-1$
                    highAvailabilityTab.setVisible((Boolean) vm.getIsHighlyAvailable().getEntity());
                } else if ("IsBootSequenceTabValid".equals(propName)) { //$NON-NLS-1$
                    if ((Boolean) vm.getIsHighlyAvailable().getEntity()) {
                        bootOptionsTab.markAsValid();
                    } else {
                        bootOptionsTab.markAsInvalid(null);
                    }
                } else if ("IsCustomPropertiesAvailable".equals(propName)) { //$NON-NLS-1$
                    customPropertiesTab.setVisible(vm.getIsCustomPropertiesAvailable());
                } else if ("IsCustomPropertiesTabValid".equals(propName)) { //$NON-NLS-1$
                    if (vm.getIsCustomPropertiesTabValid()) {
                        customPropertiesTab.markAsValid();
                    } else {
                        customPropertiesTab.markAsInvalid(null);
                    }
                }
                else if ("IsDisksAvailable".equals(propName)) { //$NON-NLS-1$
                    boolean isDisksAvailable = vm.getIsDisksAvailable();
                    disksAllocationPanel.setVisible(isDisksAvailable);

                    boolean isProvisioningAvailable = vm.getProvisioning().getIsAvailable();
                    storageAllocationPanel.setVisible(isProvisioningAvailable || isDisksAvailable);

                    if (vm.getDisks() != null) {
                        for (DiskModel diskModel : vm.getDisks()) {
                            if (diskModel.getDiskImage().getimageStatus() == ImageStatus.ILLEGAL) {
                                generalWarningMessage.setText(constants.illegalDisksInVm());
                                return;
                            }
                        }
                    }
                }
            }
        });

        // High Availability only avail in server mode
        highAvailabilityTab.setVisible(vm.getVmType().equals(VmType.Server));

        // TODO: Move to a more appropriate method
        vm.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsLinux_Unassign_UnknownOS".equals(propName)) { //$NON-NLS-1$
                    linuxBootOptionsPanel.setVisible(vm.getIsLinux_Unassign_UnknownOS());
                }
            }
        });

        // only avail for desktop mode
        isStatelessEditor.setVisible(vm.getVmType().equals(VmType.Desktop));
        numOfMonitorsEditor.setVisible(vm.getVmType().equals(VmType.Desktop));
        allowConsoleReconnectEditor.setVisible(vm.getVmType().equals(VmType.Desktop));

        defaultHostEditor.setEnabled(false);
        specificHost.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                defaultHostEditor.setEnabled(specificHost.getValue());
                ValueChangeEvent.fire(isAutoAssignEditor.asRadioButton(), false);
            }
        });

        // TODO: This is a hack and should be handled cleanly via model property availability
        isAutoAssignEditor.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                defaultHostEditor.setEnabled(false);
            }
        }, ClickEvent.getType());

        vm.getIsAutoAssign().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!isAutoAssignEditor.asRadioButton().getValue())
                    specificHost.setValue(true, true);
            }
        });

    }

    @Override
    public UnitVmModel flush() {
        priorityEditor.flush();
        return Driver.driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    public interface ButtonCellTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/common/css/ButtonCellTable.css" })
        TableStyle cellTableStyle();
    }

}
