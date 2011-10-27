package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import java.util.ArrayList;
import java.util.Map.Entry;

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
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vms.VmNewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.Align;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.parser.MemorySizeParser;
import org.ovirt.engine.ui.webadmin.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.webadmin.widget.renderer.MemorySizeRenderer;
import org.ovirt.engine.ui.webadmin.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.webadmin.widget.table.column.EntityModelTextColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.inject.Inject;

public class VmNewPopupView extends AbstractModelBoundPopupView<UnitVmModel> implements VmNewPopupPresenterWidget.ViewDef {
    interface Driver extends SimpleBeanEditorDriver<UnitVmModel, VmNewPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VmNewPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Style extends CssResource {
        String longCheckboxContent();
    }

    @UiField
    Style style;

    // ==General Tab==
    @UiField
    DialogTab generalTab;
    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    ListModelListBoxEditor<Object> dataCenterEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    ListModelListBoxEditor<Object> clusterEditor;

    @UiField
    @Path(value = "name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "template.selectedItem")
    ListModelListBoxEditor<Object> templateEditor;

    @UiField(provided = true)
    @Path(value = "memSize.entity")
    EntityModelTextBoxEditor memSizeEditor;

    @UiField
    @Path(value = "totalCPUCores.entity")
    EntityModelTextBoxEditor totalCPUCoresEditor;

    @UiField
    @Path(value = "numOfSockets.entity")
    EntityModelTextBoxEditor numOfSocketsEditor;

    @UiField(provided = true)
    @Path(value = "oSType.selectedItem")
    ListModelListBoxEditor<Object> oSTypeEditor;

    // ==Windows Prep Tab==
    @UiField
    DialogTab windowsSysPrepTab;

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
    ListModelListBoxEditor<Object> numOfMonitorsEditor;

    @UiField(provided = true)
    @Path(value = "isStateless.entity")
    EntityModelCheckBoxEditor isStatelessEditor;

    // ==Host Tab==
    @UiField
    DialogTab hostTab;

    @UiField(provided = true)
    @Path(value = "runVMOnSpecificHost.entity")
    EntityModelCheckBoxEditor runVMOnSpecificHostEditor;

    @UiField(provided = true)
    @Path(value = "dontMigrateVM.entity")
    EntityModelCheckBoxEditor dontMigrateVMEditor;

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
    DialogTab highAvailabilityTab;

    @UiField(provided = true)
    @Path(value = "isHighlyAvailable.entity")
    EntityModelCheckBoxEditor isHighlyAvailableEditor;

    // TODO: Priority is a ListModel which is rendered as RadioBox
    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel> priorityEditor;

    // ==Resource Allocation Tab==
    @UiField
    DialogTab resourceAllocationTab;

    @UiField(provided = true)
    @Path(value = "storageDomain.selectedItem")
    ListModelListBoxEditor<Object> storageDomainEditor;

    @UiField(provided = true)
    @Path(value = "provisioning.selectedItem")
    ListModelListBoxEditor<Object> provisioningEditor;

    @UiField(provided = true)
    @Path(value = "minAllocatedMemory.entity")
    EntityModelTextBoxEditor minAllocatedMemoryEditor;

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

    @UiField
    // EntityModelCheckBoxEditor cdImageIsChangable;
    // TODO: Should be handeled in a more generic way!
    // @Path(value = "cdImage.isChangable")
    @Ignore
    CheckBox cdImageIsChangable;

    @UiField
    FlowPanel linuxBootOptionsPanel;

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
    DialogTab customPropertiesTab;

    @UiField
    @Path(value = "customProperties.entity")
    EntityModelTextBoxEditor customPropertiesEditor;

    @Inject
    public VmNewPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();

        // Contains a special parser/renderer
        memSizeEditor = new EntityModelTextBoxEditor(new MemorySizeRenderer(), new MemorySizeParser());
        minAllocatedMemoryEditor = new EntityModelTextBoxEditor(new MemorySizeRenderer(), new MemorySizeParser());

        // TODO: How to align right without creating the widget manually?
        runVMOnSpecificHostEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        dontMigrateVMEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isHighlyAvailableEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isStatelessEditor = new EntityModelCheckBoxEditor(Align.RIGHT);


        priorityEditor = new EntityModelCellTable<ListModel>(false);
        priorityEditor.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                return ((EntityModel)model).getTitle();
            }
        }, "");

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        applyStyles();

        // Default is false
        windowsSysPrepTab.setVisible(false);

        localize(constants);
        Driver.driver.initialize(this);
    }

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

        templateEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VmTemplate) object).getname();
            }
        });

        oSTypeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

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
        specificHost = new RadioButton("runVmOnHostGroup");
        isAutoAssignEditor = new EntityModelRadioButtonEditor("runVmOnHostGroup");
        defaultHostEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDS) object).getvds_name();
            }
        });

        // Resource Allocation
        storageDomainEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_domains) object).getstorage_name();
            }
        });

        provisioningEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((EntityModel) object).getTitle();
            }
        });

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

    // TODO: Localize
    private void localize(ApplicationConstants constants) {
        // Tabs
        highAvailabilityTab.setLabel("High Availability");
        resourceAllocationTab.setLabel("Resource Allocation");
        bootOptionsTab.setLabel("Boot Options");
        customPropertiesTab.setLabel("Custom Properties");

        // General Tab
        generalTab.setLabel("General");
        dataCenterEditor.setLabel("Data Center");
        clusterEditor.setLabel("Host Cluster");
        nameEditor.setLabel("Name");
        descriptionEditor.setLabel("Description");
        templateEditor.setLabel("Based on Template");
        memSizeEditor.setLabel("Memory Size");
        totalCPUCoresEditor.setLabel("Total Cores");
        numOfSocketsEditor.setLabel("CPU Sockets");
        oSTypeEditor.setLabel("Operating System");
        isStatelessEditor.setLabel("Stateless");

        // Windows Sysprep Tab
        windowsSysPrepTab.setLabel("Windows Sysprep");
        domainEditor.setLabel("Domain");
        timeZoneEditor.setLabel("Time Zone");

        // Console Tab
        consoleTab.setLabel("Console");
        displayProtocolEditor.setLabel("Protocol");
        usbPolicyEditor.setLabel("USB Policy");
        numOfMonitorsEditor.setLabel("Monitors");

        // Host Tab
        hostTab.setLabel("Host");
        isAutoAssignEditor.setLabel("Any Host in Cluster");
        // specificHostEditor.setLabel("Specific");
        runVMOnSpecificHostEditor.setLabel("Run VM on the selected host (no migration allowed)");
        dontMigrateVMEditor.setLabel("Allow VM migration only upon Administrator specific request (system will not trigger automatic migration of this VM)");

        // High Availability Tab
        isHighlyAvailableEditor.setLabel("Highly Available");

        // Resource Allocation Tab
        storageDomainEditor.setLabel("Storage Domain");
        provisioningEditor.setLabel("Provisioning");
        minAllocatedMemoryEditor.setLabel("Physical Memory Guaranteed");

        // Boot Options
        firstBootDeviceEditor.setLabel("First Device");
        secondBootDeviceEditor.setLabel("Second Device");
        kernel_pathEditor.setLabel("kernel path");
        initrd_pathEditor.setLabel("initrd path");
        kernel_parametersEditor.setLabel("kernel parameters");
        customPropertiesEditor.setLabel("Custom Properties");
    }

    private void applyStyles() {
        runVMOnSpecificHostEditor.addContentWidgetStyleName(style.longCheckboxContent());
        dontMigrateVMEditor.addContentWidgetStyleName(style.longCheckboxContent());
    }

    @Override
    public void focus() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(UnitVmModel object) {
        priorityEditor.setRowData(new ArrayList<EntityModel>());
        priorityEditor.edit(object.getPriority());
        Driver.driver.edit(object);
        initTabAvailabilityListeners(object);
    }

    private void initTabAvailabilityListeners(final UnitVmModel vm) {
        // TODO should be handled by the core framework
        vm.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsWindowsOS".equals(propName)) {
                    if (vm.getIsWindowsOS()) {
                        windowsSysPrepTab.setVisible(true);
                    } else {
                        windowsSysPrepTab.setVisible(false);
                    }
                } else if ("IsGeneralTabValid".equals(propName)) {
                    if (vm.getIsGeneralTabValid()) {
                        generalTab.markAsValid();
                    } else {
                        generalTab.markAsInvalid(null);
                    }
                } else if ("IsDisplayTabValid".equals(propName)) {
                    if (vm.getIsDisplayTabValid()) {
                        consoleTab.markAsValid();
                    } else {
                        consoleTab.markAsInvalid(null);
                    }
                } else if ("IsHostAvailable".equals(propName)) {
                    if (vm.getIsHostAvailable()) {
                        hostTab.setVisible(true);
                    } else {
                        hostTab.setVisible(false);
                    }
                } else if ("IsHostAvailable".equals(propName)) {
                    if (vm.getIsHostTabValid()) {
                        hostTab.markAsValid();
                    } else {
                        hostTab.markAsInvalid(null);
                    }
                } else if ("IsAllocationTabValid".equals(propName)) {
                    if (vm.getIsAllocationTabValid()) {
                        resourceAllocationTab.markAsValid();
                    } else {
                        resourceAllocationTab.markAsInvalid(null);
                    }
                } else if ("IsHighlyAvailable".equals(propName)) {
                    if ((Boolean) vm.getIsHighlyAvailable().getEntity()) {
                        highAvailabilityTab.setVisible(true);
                    } else {
                        highAvailabilityTab.setVisible(false);
                    }
                } else if ("IsBootSequenceTabValid".equals(propName)) {
                    if ((Boolean) vm.getIsHighlyAvailable().getEntity()) {
                        bootOptionsTab.markAsValid();
                    } else {
                        bootOptionsTab.markAsInvalid(null);
                    }
                } else if ("IsCustomPropertiesAvailable".equals(propName)) {
                    customPropertiesTab.setVisible(vm.getIsCustomPropertiesAvailable());
                } else if ("IsCustomPropertiesTabValid".equals(propName)) {
                    if (vm.getIsCustomPropertiesTabValid()) {
                        customPropertiesTab.markAsValid();
                    } else {
                        customPropertiesTab.markAsInvalid(null);
                    }
                }
            }
        });

        //High Availability only avail in server mode
        highAvailabilityTab.setVisible(vm.getVmType().equals(VmType.Server));

        // TODO: Move to a more appropriate method
        vm.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsLinux_Unassign_UnknownOS".equals(propName)) {
                    linuxBootOptionsPanel.setVisible(vm.getIsLinux_Unassign_UnknownOS());
                }
            }
        });

        //only avail for desktop mode
        isStatelessEditor.setVisible(vm.getVmType().equals(VmType.Desktop));
        numOfMonitorsEditor.setVisible(vm.getVmType().equals(VmType.Desktop));

        // TODO: Should be handled in a more generic way!
        cdImageIsChangable.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                vm.getCdImage().setIsChangable(cdImageIsChangable.getValue());
            }
        });

        defaultHostEditor.setEnabled(false);
        specificHost.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                defaultHostEditor.setEnabled(specificHost.getValue());
            }
        });

        //TODO: This is a hack and should be handled cleanly via model property availability
        isAutoAssignEditor.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                defaultHostEditor.setEnabled(false);
            }
        }, ClickEvent.getType());
    }

    @Override
    public UnitVmModel flush() {
        priorityEditor.flush();
        return Driver.driver.flush();
    }
}
