package org.ovirt.engine.ui.common.widget.uicommon.popup;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;
import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.simpleField;
import static org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel.ListModelWithClusterDefault.CLUSTER_VALUE_EVENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Row;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ConsoleDisconnectAction;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmResumeBehavior;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.TabbedView;
import org.ovirt.engine.ui.common.widget.AffinityGroupSelectionWithListWidget;
import org.ovirt.engine.ui.common.widget.AffinityLabelSelectionWithListWidget;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.EntityModelDetachableWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.HasDetachable;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.OvirtTabListItem;
import org.ovirt.engine.ui.common.widget.editor.CpuPinningPolicyListBox;
import org.ovirt.engine.ui.common.widget.editor.GroupedListModelListBox;
import org.ovirt.engine.ui.common.widget.editor.GroupedListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.IconEditorWidget;
import org.ovirt.engine.ui.common.widget.editor.IntegerListModelTypeAheadListBox;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelMultipleSelectListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadChangeableListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.SuggestionMatcher;
import org.ovirt.engine.ui.common.widget.editor.VncKeyMapRenderer;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelDetachableWidget;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelDetachableWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.MemorySizeEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.common.widget.profile.ProfilesInstanceTypeEditor;
import org.ovirt.engine.ui.common.widget.renderer.BiosTypeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.BooleanRenderer;
import org.ovirt.engine.ui.common.widget.renderer.ClusterDefaultRenderer;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipWidth;
import org.ovirt.engine.ui.common.widget.uicommon.instanceimages.InstanceImagesEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmPopupVmInitWidget;
import org.ovirt.engine.ui.common.widget.uicommon.storage.DisksAllocationView;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.VirtioMultiQueueType;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateWithVersion;
import org.ovirt.engine.ui.uicommonweb.models.vms.CpuPinningListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.CpuPinningListModel.CpuPinningListModelItem;
import org.ovirt.engine.ui.uicommonweb.models.vms.DataCenterWithCluster;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.TimeZoneModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel.ListModelWithClusterDefault;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.client.IntegerRenderer;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractVmPopupWidget extends AbstractModeSwitchingPopupWidget<UnitVmModel>
    implements TabbedView {

    interface Driver extends UiCommonEditorDriver<UnitVmModel, AbstractVmPopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<DialogTabPanel, AbstractVmPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    // ==General Tab==
    @UiField
    protected DialogTab generalTab;

    @UiField(provided = true)
    @Path(value = "dataCenterWithClustersList.selectedItem")
    @WithElementId("dataCenterWithCluster")
    public GroupedListModelListBoxEditor<DataCenterWithCluster> dataCenterWithClusterEditor;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    @WithElementId("quota")
    public ListModelTypeAheadListBoxEditor<Quota> quotaEditor;

    @UiField
    @Ignore
    public Label nameLabel;

    @UiField(provided = true)
    @Path(value = "name.entity")
    @WithElementId("name")
    public StringEntityModelTextBoxEditor nameEditor;

    @UiField(provided = true)
    @Path(value = "templateVersionName.entity")
    @WithElementId("templateVersionName")
    public StringEntityModelTextBoxEditor templateVersionNameEditor;

    @UiField(provided = true)
    @Ignore
    public InfoIcon poolNameIcon;

    @UiField
    @Path(value = "vmId.entity")
    @WithElementId("vmId")
    public StringEntityModelTextBoxEditor vmIdEditor;

    @UiField(provided = true)
    @Path(value = "description.entity")
    @WithElementId("description")
    public StringEntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "comment.entity")
    @WithElementId("comment")
    public StringEntityModelTextBoxEditor commentEditor;

    @UiField(provided = true)
    @Path(value = "baseTemplate.selectedItem")
    @WithElementId("baseTemplate")
    public ListModelTypeAheadListBoxEditor<VmTemplate> baseTemplateEditor;

    @UiField(provided = true)
    @Path(value = "templateWithVersion.selectedItem")
    @WithElementId("templateWithVersion")
    public ListModelTypeAheadListBoxEditor<TemplateWithVersion> templateWithVersionEditor;

    @UiField(provided = true)
    @Path(value = "OSType.selectedItem")
    @WithElementId("osType")
    public ListModelListBoxEditor<Integer> oSTypeEditor;

    @UiField(provided = true)
    @Path(value = "vmType.selectedItem")
    @WithElementId("vmType")
    public ListModelListBoxEditor<VmType> vmTypeEditor;

    @Path(value = "instanceTypes.selectedItem")
    @WithElementId("instanceType")
    public ListModelTypeAheadListBoxEditor<InstanceType> instanceTypesEditor;

    @UiField(provided = true)
    public EntityModelDetachableWidgetWithLabel detachableInstanceTypesEditor;

    @UiField(provided = true)
    @Path(value = "isDeleteProtected.entity")
    @WithElementId("isDeleteProtected")
    public EntityModelCheckBoxEditor isDeleteProtectedEditor;

    @UiField(provided = true)
    @Path(value = "isSealed.entity")
    @WithElementId("isSealed")
    public EntityModelCheckBoxEditor isSealedEditor;

    @UiField
    public Panel logicalNetworksEditorRow;

    @UiField
    @Ignore
    @WithElementId("instanceImages")
    public InstanceImagesEditor instanceImagesEditor;

    @UiField
    @Ignore
    @WithElementId("vnicsEditor")
    public ProfilesInstanceTypeEditor profilesInstanceTypeEditor;

    @UiField
    @Ignore
    Alert generalWarningMessage;

    // == System ==
    @UiField
    protected DialogTab systemTab;

    @UiField(provided = true)
    public EntityModelDetachableWidgetWithLabel detachableMemSizeEditor;

    @Path(value = "memSize.entity")
    @WithElementId("memSize")
    public MemorySizeEntityModelTextBoxEditor memSizeEditor;

    @UiField(provided = true)
    public EntityModelDetachableWidgetWithInfo detachableMaxMemorySizeEditor;

    @Path(value = "maxMemorySize.entity")
    @WithElementId("maxMemorySize")
    @UiField(provided = true)
    public MemorySizeEntityModelTextBoxEditor maxMemorySizeEditor;

    @UiField(provided = true)
    @Path(value = "totalCPUCores.entity")
    @WithElementId("totalCPUCores")
    public StringEntityModelTextBoxOnlyEditor totalvCPUsEditor;

    @UiField(provided = true)
    @Ignore
    public EntityModelDetachableWidgetWithInfo totalvCPUsEditorWithInfoIcon;

    @UiField
    @Ignore
    AdvancedParametersExpander vcpusAdvancedParameterExpander;

    @UiField
    @Ignore
    Panel vcpusAdvancedParameterExpanderContent;

    @Path(value = "numOfSockets.selectedItem")
    @WithElementId("numOfSockets")
    public ListModelListBoxEditor<Integer> numOfSocketsEditor;

    @Path(value = "coresPerSocket.selectedItem")
    @WithElementId("coresPerSocket")
    public ListModelListBoxEditor<Integer> corePerSocketEditor;

    @Path(value = "threadsPerCore.selectedItem")
    @WithElementId("threadsPerCore")
    @UiField(provided = true)
    public ListModelListBoxOnlyEditor<Integer> threadsPerCoreEditor;

    @UiField(provided = true)
    public EntityModelDetachableWidgetWithLabel numOfSocketsEditorWithDetachable;

    @UiField(provided = true)
    public EntityModelDetachableWidgetWithLabel corePerSocketEditorWithDetachable;

    @UiField(provided = true)
    public EntityModelDetachableWidgetWithInfo threadsPerCoreEditorWithInfoIcon;

    private BiosTypeRenderer biosTypeRenderer;

    @UiField(provided = true)
    @Path(value = "biosType.selectedItem")
    @WithElementId("biosType")
    public ListModelListBoxEditor<BiosType> biosTypeEditor;

    @UiField(provided = true)
    @Path(value = "emulatedMachine.selectedItem")
    @WithElementId("emulatedMachine")
    public ListModelTypeAheadChangeableListBoxEditor emulatedMachine;

    @UiField(provided = true)
    @Path(value = "customCpu.selectedItem")
    @WithElementId("customCpu")
    public ListModelTypeAheadChangeableListBoxEditor customCpu;

    @UiField
    @Ignore
    public EnableableFormLabel ssoMethodLabel;

    @UiField(provided = true)
    @Path(value = "ssoMethodNone.entity")
    @WithElementId("ssoMethodNone")
    public EntityModelRadioButtonEditor ssoMethodNone;

    @UiField(provided = true)
    @Path(value = "ssoMethodGuestAgent.entity")
    @WithElementId("ssoMethodGuestAgent")
    public EntityModelRadioButtonEditor ssoMethodGuestAgent;

    @UiField(provided = true)
    @Path(value = "isSoundcardEnabled.entity")
    @WithElementId("isSoundcardEnabled")
    public EntityModelCheckBoxEditor isSoundcardEnabledEditor;

    @UiField(provided = true)
    @Path("copyPermissions.entity")
    @WithElementId("copyTemplatePermissions")
    public EntityModelCheckBoxEditor copyTemplatePermissionsEditor;

    private ClusterDefaultRenderer<SerialNumberPolicy> serialNumberPolicyRenderer;

    @UiField(provided = true)
    @Path(value = "serialNumberPolicy.selectedItem")
    @WithElementId
    public ListModelListBoxEditor<SerialNumberPolicy> serialNumberPolicyEditor;

    @UiField
    @Path("customSerialNumber.entity")
    public StringEntityModelTextBoxEditor customSerialNumberEditor;

    // == Pools ==
    @UiField
    protected DialogTab poolTab;

    @UiField(provided = true)
    @Path(value = "poolType.selectedItem")
    @WithElementId("poolType")
    public ListModelListBoxEditor<EntityModel<VmPoolType>> poolTypeEditor;

    @UiField
    @Path(value = "poolStateful.entity")
    @WithElementId("poolStateful")
    public EntityModelCheckBoxEditor poolStatefulEditor;

    @UiField(provided = true)
    @Ignore
    public InfoIcon newPoolPrestartedVmsIcon;

    @UiField(provided = true)
    @Ignore
    public InfoIcon editPoolPrestartedVmsIcon;

    @UiField(provided = true)
    @Ignore
    public InfoIcon newPoolMaxAssignedVmsPerUserIcon;

    @UiField(provided = true)
    @Ignore
    public InfoIcon editPoolMaxAssignedVmsPerUserIcon;

    @UiField(provided = true)
    @Path(value = "prestartedVms.entity")
    @WithElementId("prestartedVms")
    public IntegerEntityModelTextBoxOnlyEditor prestartedVmsEditor;

    @UiField(provided = true)
    @Path("maxAssignedVmsPerUser.entity")
    @WithElementId("maxAssignedVmsPerUser")
    public IntegerEntityModelTextBoxOnlyEditor maxAssignedVmsPerUserEditor;

    @UiField
    @Ignore
    public Row newPoolEditVmsRow;

    @UiField
    @Ignore
    public Row newPoolEditMaxAssignedVmsPerUserRow;

    @UiField
    @Ignore
    public Label prestartedLabel;

    @UiField(provided = true)
    @Path("numOfDesktops.entity")
    @WithElementId("numOfVms")
    public EntityModelTextBoxEditor<Integer> numOfVmsEditor;

    @UiField
    @Ignore
    public Row editPoolEditVmsRow;

    @UiField
    @Ignore
    public Row editPoolIncreaseNumOfVmsRow;

    @UiField
    @Ignore
    public Row editPoolEditMaxAssignedVmsPerUserRow;

    @UiField
    @Ignore
    public Label editPrestartedVmsLabel;

    @UiField(provided = true)
    @Path("prestartedVms.entity")
    @WithElementId("editPrestartedVms")
    public IntegerEntityModelTextBoxOnlyEditor editPrestartedVmsEditor;

    @UiField(provided = true)
    @Path("numOfDesktops.entity")
    @WithElementId("increaseNumOfVms")
    public EntityModelTextBoxEditor<Integer> increaseNumOfVmsEditor;

    @UiField(provided = true)
    @Path("maxAssignedVmsPerUser.entity")
    @WithElementId("editMaxAssignedVmsPerUser")
    public IntegerEntityModelTextBoxOnlyEditor editMaxAssignedVmsPerUserEditor;

    @UiField
    @Ignore
    // system tab -> general time zone
    public Label generalLabel;

    @Path(value = "timeZone.selectedItem")
    @WithElementId("timeZone")
    public ListModelListBoxOnlyEditor<TimeZoneModel> timeZoneEditor;

    @UiField(provided = true)
    @Ignore
    public EntityModelWidgetWithInfo timeZoneEditorWithInfo;

    // ==Initial run Tab==
    @UiField
    protected DialogTab initialRunTab;

    @UiField(provided = true)
    @Path(value = "vmInitEnabled.entity")
    @WithElementId("vmInitEnabled")
    public EntityModelCheckBoxEditor vmInitEnabledEditor;

    @UiField
    @Ignore
    public VmPopupVmInitWidget vmInitEditor;

    // ==Console Tab==
    @UiField
    protected DialogTab consoleTab;

    @UiField(provided = true)
    @Path(value = "isHeadlessModeEnabled.entity")
    @WithElementId("isHeadlessModeEnabled")
    public EntityModelCheckBoxEditor isHeadlessModeEnabledEditor;

    @WithElementId
    @Ignore
    @UiField(provided = true)
    public InfoIcon isHeadlessModeEnabledInfoIcon;

    @UiField(provided = true)
    @Path(value = "displayType.selectedItem")
    @WithElementId("displayType")
    public ListModelListBoxEditor<DisplayType> displayTypeEditor;

    @UiField(provided = true)
    @Path(value = "graphicsType.selectedItem")
    @WithElementId("graphicsType")
    public ListModelListBoxEditor<UnitVmModel.GraphicsTypes> graphicsTypeEditor;

    @UiField(provided = true)
    @Path(value = "vncKeyboardLayout.selectedItem")
    @WithElementId("vncKeyboardLayout")
    public ListModelListBoxEditor<String> vncKeyboardLayoutEditor;

    @UiField(provided = true)
    @Path(value = "isUsbEnabled.entity")
    @WithElementId("usbPolicy")
    public EntityModelCheckBoxEditor isUsbEnabledEditor;

    @UiField(provided = true)
    @Path(value = "consoleDisconnectAction.selectedItem")
    @WithElementId("consoleDisconnectAction")
    public ListModelListBoxEditor<ConsoleDisconnectAction> consoleDisconnectActionEditor;

    @UiField
    @Path("consoleDisconnectActionDelay.entity")
    public IntegerEntityModelTextBoxEditor consoleDisconnectActionDelayEditor;

    @UiField
    @Ignore
    public EnableableFormLabel monitorsLabel;

    @UiField(provided = true)
    @Path(value = "numOfMonitors.selectedItem")
    @WithElementId("numOfMonitors")
    public ListModelListBoxEditor<Integer> numOfMonitorsEditor;

    @UiField
    @Ignore
    public Row monitors;

    @UiField(provided = true)
    @Path(value = "isStateless.entity")
    @WithElementId("isStateless")
    public EntityModelCheckBoxEditor isStatelessEditor;

    @UiField(provided = true)
    @Path(value = "isRunAndPause.entity")
    @WithElementId("isRunAndPause")
    public EntityModelCheckBoxEditor isRunAndPauseEditor;

    @UiField(provided = true)
    @Path(value = "isSmartcardEnabled.entity")
    @WithElementId("isSmartcardEnabled")
    public EntityModelCheckBoxEditor isSmartcardEnabledEditor;

    @UiField(provided = true)
    @Path(value = "allowConsoleReconnect.entity")
    @WithElementId("allowConsoleReconnect")
    public EntityModelCheckBoxEditor allowConsoleReconnectEditor;

    @UiField
    @Ignore
    public Label serialConsoleOptionsVmPopupLabel;

    @UiField(provided = true)
    @Path(value = "isConsoleDeviceEnabled.entity")
    @WithElementId("isConsoleDeviceEnabled")
    public EntityModelCheckBoxEditor isConsoleDeviceEnabledEditor;

    @UiField
    @Path(value = "spiceProxy.entity")
    @WithElementId
    public StringEntityModelTextBoxEditor spiceProxyEditor;

    @UiField(provided = true)
    @Ignore
    public EntityModelWidgetWithInfo spiceProxyEnabledCheckboxWithInfoIcon;

    @Path(value = "spiceProxyEnabled.entity")
    @WithElementId
    @UiField(provided = true)
    public EntityModelCheckBoxOnlyEditor spiceProxyOverrideEnabledEditor;

    @UiField(provided = true)
    @Path("spiceFileTransferEnabled.entity")
    @WithElementId("spiceFileTransferEnabled")
    public EntityModelCheckBoxEditor spiceFileTransferEnabledEditor;

    @UiField(provided = true)
    @Path("spiceCopyPasteEnabled.entity")
    @WithElementId("spiceCopyPasteEnabled")
    public EntityModelCheckBoxEditor spiceCopyPasteEnabledEditor;

    // == Rng Tab ==
    @UiField
    protected DialogTab rngDeviceTab;

    @Path(value = "isRngEnabled.entity")
    @WithElementId("isRngEnabled")
    @UiField(provided = true)
    public EntityModelCheckBoxEditor isRngEnabledEditor;

    @WithElementId
    @Ignore
    @UiField(provided = true)
    public InfoIcon isRngEnabledInfoIcon;

    @UiField
    protected FlowPanel rngPanel;

    @UiField(provided = true)
    @Path(value = "rngPeriod.entity")
    @WithElementId("rngPeriodEditor")
    public IntegerEntityModelTextBoxEditor rngPeriodEditor;

    @UiField(provided = true)
    @Path(value = "rngBytes.entity")
    @WithElementId("rngBytesEditor")
    public IntegerEntityModelTextBoxEditor rngBytesEditor;

    /**
     * @see UnitVmModel#rngSourceUrandom
     */
    @UiField(provided = true)
    @Path(value = "rngSourceUrandom.entity")
    @WithElementId("rngSourceUrandom")
    public EntityModelRadioButtonEditor rngSourceUrandom;

    @UiField
    public InfoIcon rngSourceUrandomInfoIcon;

    @UiField(provided = true)
    @Path(value = "rngSourceHwrng.entity")
    @WithElementId("rngSourceHwrng")
    public EntityModelRadioButtonEditor rngSourceHwrng;

    // ==Host Tab==
    @UiField
    protected DialogTab hostTab;

    @UiField(provided = true)
    @Path(value = "hostCpu.entity")
    @WithElementId("hostCpu")
    public EntityModelCheckBoxEditor hostCpuEditor;

    @UiField(provided = true)
    @Path(value = "tscFrequency.entity")
    @WithElementId("tscFrequency")
    public EntityModelCheckBoxEditor tscFrequencyEditor;

    @UiField(provided = true)
    public InfoIcon hostCpuInfoIcon;

    @UiField(provided = true)
    public InfoIcon tscFrequencyInfoIcon;

    @UiField
    @Ignore
    public FlowPanel numaPanel;

    @UiField
    @Ignore
    public FlowPanel startRunningOnPanel;

    @UiField(provided = true)
    @Ignore
    public InfoIcon numaInfoIcon;

    @UiField
    @Path(value = "numaNodeCount.entity")
    @WithElementId("numaNodeCount")
    public IntegerEntityModelTextBoxEditor numaNodeCount;

    @UiField
    UiCommandButton numaSupportButton;

    @Path(value = "migrationMode.selectedItem")
    @WithElementId("migrationMode")
    @UiField(provided = true)
    public ListModelListBoxEditor<MigrationSupport> migrationModeEditor;

    @UiField(provided = true)
    public EntityModelDetachableWidget migrationModeEditorWithDetachable;

    @UiField(provided = true)
    public EntityModelDetachableWidget overrideMigrationDowntimeEditorWithDetachable;

    @UiField(provided = true)
    @Path(value = "migrationPolicies.selectedItem")
    @WithElementId("migrationPolicy")
    public ListModelListBoxOnlyEditor<MigrationPolicy> migrationPolicyEditor;

    private ClusterDefaultRenderer<MigrationPolicy> migrationPolicyRenderer;

    @UiField(provided = true)
    public EntityModelDetachableWidget overrideMigrationPolicyEditorWithDetachable;

    @UiField(provided = true)
    public InfoIcon migrationPolicyInfoIcon;

    @UiField(provided = true)
    public InfoIcon migrationDowntimeInfoIcon;

    @UiField(provided = true)
    public InfoIcon migrationSelectInfoIcon;

    @UiField
    @Ignore
    public EnableableFormLabel migrationDowntimeLabel;

    @UiField(provided = true)
    @Path(value = "migrationDowntime.selectedItem")
    @WithElementId("migrationDowntime")
    public ListModelTypeAheadListBoxEditor<Integer> migrationDowntimeEditor;

    @UiField(provided = true)
    @Path(value = "autoConverge.selectedItem")
    @WithElementId("autoConverge")
    public ListModelListBoxEditor<Boolean> autoConvergeEditor;

    private ClusterDefaultRenderer<Boolean> autoConvergeRenderer;

    @UiField(provided = true)
    @Path(value = "migrateCompressed.selectedItem")
    @WithElementId("migrateCompressed")
    public ListModelListBoxEditor<Boolean> migrateCompressedEditor;

    private ClusterDefaultRenderer<Boolean> migrateCompressedRenderer;

    @UiField(provided = true)
    @Path(value = "migrateEncrypted.selectedItem")
    @WithElementId("migrateEncrypted")
    public ListModelListBoxEditor<Boolean> migrateEncryptedEditor;

    private ClusterDefaultRenderer<Boolean> migrateEncryptedRenderer;

    @UiField(provided = true)
    @Ignore
    @WithElementId("specificHost")
    public EntityModelRadioButtonEditor specificHost;

    @UiField(provided = true)
    @Path(value = "defaultHost.selectedItems")
    @WithElementId("defaultHost")
    public ListModelMultipleSelectListBoxEditor<VDS> defaultHostEditor;

    @UiField(provided = true)
    @Path(value = "isAutoAssign.entity")
    @WithElementId("isAutoAssign")
    public EntityModelRadioButtonEditor isAutoAssignEditor;

    @UiField(provided = true)
    @Path(value = "cpuProfiles.selectedItem")
    @WithElementId("cpuProfiles")
    public ListModelListBoxEditor<CpuProfile> cpuProfilesEditor;

    @UiField(provided = true)
    InfoIcon multiQueuesInfo;

    @UiField
    @Ignore
    Label multiQueuesLabel;

    @Ignore
    public CpuPinningPolicyListBox cpuPinningPolicyListBox;

    @UiField(provided = true)
    @Path(value = "cpuPinningPolicy.selectedItem")
    @WithElementId("cpuPinningPolicy")
    public ListModelTypeAheadListBoxEditor<CpuPinningListModelItem> cpuPinningPolicyEditor;

    @UiField(provided = true)
    InfoIcon cpuPinningInfo;

    @UiField(provided = true)
    @Path(value = "cpuPinning.entity")
    @WithElementId("cpuPinning")
    public StringEntityModelTextBoxOnlyEditor cpuPinning;

    @UiField(provided = true)
    @Path(value = "multiQueues.entity")
    @WithElementId("multiQueues")
    public EntityModelCheckBoxEditor multiQueues;

    @UiField(provided = true)
    @Path(value = "cpuSharesAmountSelection.selectedItem")
    @WithElementId("cpuSharesAmountSelection")
    public ListModelListBoxOnlyEditor<UnitVmModel.CpuSharesAmount> cpuSharesAmountSelectionEditor;

    @UiField(provided = true)
    @Path(value = "virtioScsiMultiQueueTypeSelection.selectedItem")
    @WithElementId("virtioScsiMultiQueueTypeSelection")
    public ListModelListBoxOnlyEditor<VirtioMultiQueueType> virtioScsiMultiQueueSelectionEditor;

    @UiField(provided = true)
    @Path(value = "cpuSharesAmount.entity")
    @WithElementId("cpuSharesAmount")
    public IntegerEntityModelTextBoxOnlyEditor cpuSharesAmountEditor;

    @UiField
    @Path(value = "customCompatibilityVersion.selectedItem")
    @WithElementId("customCompatibilityVersion")
    public ListModelListBoxEditor<Version> customCompatibilityVersionEditor;

    // ==High Availability Tab==
    @UiField
    protected DialogTab highAvailabilityTab;

    @UiField(provided = true)
    @Path(value = "isHighlyAvailable.entity")
    @WithElementId("isHighlyAvailable")
    public EntityModelCheckBoxEditor isHighlyAvailableEditor;

    @UiField(provided = true)
    @Path("lease.selectedItem")
    @WithElementId("lease")
    public ListModelListBoxOnlyEditor<StorageDomain> leaseEditor;

    @UiField(provided = true)
    public InfoIcon leaseInfoIcon;

    @UiField(provided = true)
    @Path("resumeBehavior.selectedItem")
    @WithElementId("resumeBehavior")
    public ListModelListBoxOnlyEditor<VmResumeBehavior> resumeBehavior;

    @UiField(provided = true)
    public InfoIcon resumeBehaviorInfoIcon;

    @UiField(provided = true)
    @Ignore
    public EntityModelDetachableWidget isHighlyAvailableEditorWithDetachable;

    @Path("priority.selectedItem")
    @WithElementId("priority")
    public ListModelListBoxEditor<EntityModel<Integer>> priorityEditor;

    @UiField(provided = true)
    @Ignore
    public EntityModelDetachableWidgetWithLabel detachablePriorityEditor;

    @UiField(provided = true)
    @Path(value = "watchdogModel.selectedItem")
    @WithElementId("watchdogModel")
    public ListModelListBoxEditor<VmWatchdogType> watchdogModelEditor;

    @UiField(provided = true)
    @Path(value = "watchdogAction.selectedItem")
    @WithElementId("watchdogAction")
    public ListModelListBoxEditor<VmWatchdogAction> watchdogActionEditor;

    // ==Resource Allocation Tab==
    @UiField
    protected DialogTab resourceAllocationTab;

    @UiField
    protected Row cpuPinningRow;

    @UiField
    protected Row cpuSharesEditorRow;

    @UiField(provided = true)
    protected InfoIcon diskFormatTypeMatrixInfo;

    @UiField
    protected FlowPanel storageAllocationPanel;

    @UiField
    protected FlowPanel provisionSelectionPanel;

    @UiField
    protected FlowPanel disksAllocationPanel;

    @UiField
    protected FlowPanel disksPanel;

    @UiField
    public FlowPanel cpuAllocationPanel;

    @Path(value = "minAllocatedMemory.entity")
    @WithElementId("minAllocatedMemory")
    @UiField(provided=true)
    public MemorySizeEntityModelTextBoxEditor minAllocatedMemoryEditor;

    @UiField(provided = true)
    public EntityModelDetachableWidgetWithInfo detachableMinAllocatedMemoryEditor;

    @UiField(provided = true)
    @Path(value = "ioThreadsEnabled.entity")
    EntityModelCheckBoxEditor isIoThreadsEnabled;

    @UiField
    protected FlowPanel ioThreadsPanel;

    @UiField(provided = true)
    InfoIcon ioThreadsInfo;

    @UiField(provided = true)
    @Path(value = "numOfIoThreads.entity")
    @WithElementId("numOfIoThreadsEditor")
    public StringEntityModelTextBoxOnlyEditor numOfIoThreadsEditor;

    @UiField(provided = true)
    @Path(value = "numOfVirtioScsiMultiQueues.entity")
    @WithElementId("numOfVirtioScsiMultiQueuesEditor")
    public IntegerEntityModelTextBoxOnlyEditor numOfVirtioScsiMultiQueuesEditor;

    @UiField(provided = true)
    public EntityModelDetachableWidget isIoThreadsEnabledDetachable;

    @UiField(provided = true)
    @Path(value = "memoryBalloonEnabled.entity")
    EntityModelCheckBoxEditor isMemoryBalloonEnabled;

    @UiField(provided = true)
    public EntityModelDetachableWidget isMemoryBalloonEnabledDetachable;

    @UiField(provided = true)
    @Path(value = "tpmEnabled.entity")
    @WithElementId("tpmEnabled")
    public EntityModelCheckBoxEditor tpmEnabledEditor;

    @UiField(provided = true)
    @Path(value = "provisioningThin_IsSelected.entity")
    @WithElementId("provisioningThin")
    public EntityModelRadioButtonEditor provisioningThinEditor;

    @UiField(provided = true)
    @Path(value = "provisioningClone_IsSelected.entity")
    @WithElementId("provisioningClone")
    public EntityModelRadioButtonEditor provisioningCloneEditor;

    @UiField(provided = true)
    @Path(value = "isVirtioScsiEnabled.entity")
    @WithElementId("isVirtioScsiEnabled")
    public EntityModelCheckBoxEditor isVirtioScsiEnabled;

    @UiField(provided = true)
    @Ignore
    public InfoIcon isVirtioScsiEnabledInfoIcon;

    @UiField(provided = true)
    @Ignore
    public InfoIcon isVirtioScsiMultiQueuesInfoIcon;

    @UiField(provided = true)
    @Ignore
    @WithElementId("disksAllocation")
    public DisksAllocationView disksAllocationView;

    // ==Boot Options Tab==
    @UiField
    protected DialogTab bootOptionsTab;

    @UiField(provided = true)
    @Path(value = "firstBootDevice.selectedItem")
    @WithElementId("firstBootDevice")
    public ListModelListBoxEditor<EntityModel<BootSequence>> firstBootDeviceEditor;

    @UiField(provided = true)
    @Path(value = "secondBootDevice.selectedItem")
    @WithElementId("secondBootDevice")
    public ListModelListBoxEditor<EntityModel<BootSequence>> secondBootDeviceEditor;

    @UiField(provided = true)
    @Path(value = "cdImage.selectedItem")
    @WithElementId("cdImage")
    public ListModelListBoxEditor<RepoImage> cdImageEditor;

    @UiField(provided = true)
    @Path(value = "cdAttached.entity")
    @WithElementId("cdAttached")
    public EntityModelCheckBoxEditor cdAttachedEditor;

    @UiField
    public Row attachCdRow;

    @UiField(provided = true)
    @Path("bootMenuEnabled.entity")
    @WithElementId("bootMenuEnabled")
    public EntityModelCheckBoxEditor bootMenuEnabledEditor;

    @UiField
    protected FlowPanel linuxBootOptionsPanel;

    @UiField(provided = true)
    @Path(value = "kernel_path.entity")
    @WithElementId("kernelPath")
    public StringEntityModelTextBoxEditor kernel_pathEditor;

    @UiField(provided = true)
    @Path(value = "initrd_path.entity")
    @WithElementId("initrdPath")
    public StringEntityModelTextBoxEditor initrd_pathEditor;

    @UiField(provided = true)
    @Path(value = "kernel_parameters.entity")
    @WithElementId("kernelParameters")
    public StringEntityModelTextBoxEditor kernel_parametersEditor;

    // ==Custom Properties Tab==
    @UiField
    protected DialogTab customPropertiesTab;

    @UiField
    @Ignore
    protected KeyValueWidget<KeyValueModel> customPropertiesSheetEditor;

    @UiField
    @Ignore
    protected AdvancedParametersExpander expander;

    @UiField
    @Ignore
    Panel expanderContent;

    @UiField
    @Ignore
    public ButtonBase refreshButton;

    @UiField
    @Ignore
    protected DialogTabPanel mainTabPanel;

    @UiField
    @Ignore
    protected DialogTab iconTab;

    @UiField
    @Path("icon.entity")
    protected IconEditorWidget iconEditorWidget;

    @UiField
    @Ignore
    protected DialogTab foremanTab;

    @UiField(provided = true)
    @Path(value = "providers.selectedItem")
    @WithElementId("providers")
    public ListModelListBoxEditor<Provider<OpenstackNetworkProviderProperties>> providersEditor;

    // ==Affinity Tab==
    @UiField
    protected DialogTab affinityTab;

    @UiField
    @Path(value = "affinityGroupList.selectedItem")
    @WithElementId("affinityGroupList")
    public AffinityGroupSelectionWithListWidget affinityGroupSelectionWidget;

    @UiField
    @Path(value = "labelList.selectedItem")
    @WithElementId("labelList")
    public AffinityLabelSelectionWithListWidget affinityLabelSelectionWidget;

    private UnitVmModel unitVmModel;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    private final Map<TabName, OvirtTabListItem> tabMap = new HashMap<>();

    public AbstractVmPopupWidget() {

        initListBoxEditors();
        // Contains a special parser/renderer
        memSizeEditor = new MemorySizeEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());

        minAllocatedMemoryEditor = new MemorySizeEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        minAllocatedMemoryEditor.hideLabel();

        // TODO: How to align right without creating the widget manually?
        hostCpuEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        tscFrequencyEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        isHighlyAvailableEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());

        watchdogModelEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<VmWatchdogType>() {
            @Override
            public String render(VmWatchdogType object) {
                return object == null ? constants.noWatchdogLabel() : renderNullSafe(object);
            }

            @Override
            protected String renderNullSafe(VmWatchdogType object) {
                return EnumTranslator.getInstance().translate(object);
            }
        }, new ModeSwitchingVisibilityRenderer());

        watchdogActionEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<VmWatchdogAction>() {
            @Override
            protected String renderNullSafe(VmWatchdogAction object) {
                return EnumTranslator.getInstance().translate(object);
            }
        }, new ModeSwitchingVisibilityRenderer());

        vmInitEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isStatelessEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        isRunAndPauseEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        isDeleteProtectedEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        isSealedEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        isHeadlessModeEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        isSmartcardEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        isConsoleDeviceEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer(), true);
        isRngEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        rngPeriodEditor = new IntegerEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        rngBytesEditor = new IntegerEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        rngSourceUrandom = new EntityModelRadioButtonEditor("rndBackendModel"); //$NON-NLS-1$
        rngSourceHwrng = new EntityModelRadioButtonEditor("rndBackendModel"); //$NON-NLS-1$
        tpmEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());

        cdAttachedEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        bootMenuEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        allowConsoleReconnectEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        isUsbEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        isSoundcardEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        ssoMethodNone = new EntityModelRadioButtonEditor("ssoMethod", new ModeSwitchingVisibilityRenderer()); //$NON-NLS-1$
        ssoMethodGuestAgent = new EntityModelRadioButtonEditor("ssoMethod", new ModeSwitchingVisibilityRenderer());//$NON-NLS-1$
        copyTemplatePermissionsEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        isMemoryBalloonEnabled = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        isIoThreadsEnabled = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        numOfIoThreadsEditor = StringEntityModelTextBoxOnlyEditor.newTrimmingEditor(new ModeSwitchingVisibilityRenderer());
        ioThreadsInfo = new InfoIcon(multiLineItalicSafeHtml(constants.ioThreadsExplanation()));
        ioThreadsInfo.setTooltipMaxWidth(TooltipWidth.W420);
        isVirtioScsiEnabled = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        cpuPinningInfo = new InfoIcon(multiLineItalicSafeHtml(constants.cpuPinningLabelExplanation()));
        cpuPinningInfo.setTooltipMaxWidth(TooltipWidth.W420);
        multiQueuesInfo = new InfoIcon(templates.italicText(constants.multiQueuesLabelExplanation()));
        isHeadlessModeEnabledInfoIcon =
                new InfoIcon(SafeHtmlUtils.fromTrustedString(constants.headlessModeExplanation()));
        isVirtioScsiEnabledInfoIcon =
                new InfoIcon(templates.italicText(constants.isVirtioScsiEnabledInfo()));
        isVirtioScsiMultiQueuesInfoIcon =
                new InfoIcon(templates.italicText(constants.isVirtioScsiMultiQueuesInfoIcon()));
        final Integer defaultMaximumMigrationDowntime = (Integer) AsyncDataProvider.getInstance().
                getConfigValuePreConverted(ConfigValues.DefaultMaximumMigrationDowntime);

        migrationDowntimeInfoIcon = new InfoIcon(templates.italicText(
                messages.migrationDowntimeInfo(defaultMaximumMigrationDowntime)));

        migrationPolicyInfoIcon = new InfoIcon(templates.italicText(messages.migrationPolicyInfo()));

        migrationSelectInfoIcon = new InfoIcon(multiLineItalicSafeHtml(messages.migrationSelectInfo()));

        hostCpuInfoIcon = new InfoIcon(templates.italicText(messages.hostCpuInfo()));
        tscFrequencyInfoIcon = new InfoIcon(templates.italicText(messages.tscFrequencyInfo()));

        diskFormatTypeMatrixInfo = new InfoIcon(multiLineItalicSafeHtml(constants.diskFormatTypeMatrixInfo()));

        priorityEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<EntityModel<Integer>>() {
            @Override
            protected String renderNullSafe(EntityModel<Integer> model) {
                return model.getTitle();
            }
        }, new ModeSwitchingVisibilityRenderer());
        disksAllocationView = new DisksAllocationView();
        spiceFileTransferEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        spiceCopyPasteEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());
        multiQueues = new EntityModelCheckBoxEditor(Align.RIGHT, new ModeSwitchingVisibilityRenderer());

        initPoolSpecificWidgets();
        initTextBoxEditors();
        initSpiceProxy();
        initTotalVcpus();
        initDetachableFields();
        leaseEditor = new ListModelListBoxOnlyEditor<>(new AbstractRenderer<StorageDomain>() {
            @Override
            public String render(StorageDomain domain) {
                return domain != null ? domain.getName() : constants.emptyLeaseStorageDomain();
            }
        });

        resumeBehavior = new ListModelListBoxOnlyEditor<>(new EnumRenderer<VmResumeBehavior>() {
            @Override
            public String render(VmResumeBehavior object) {
                if (object == null) {
                    return super.render(VmResumeBehavior.AUTO_RESUME);
                }

                return super.render(object);
            }
        }, new ModeSwitchingVisibilityRenderer());

        leaseInfoIcon = new InfoIcon(multiLineItalicSafeHtml(messages.leaseInfoIcon()));
        resumeBehaviorInfoIcon = new InfoIcon(multiLineItalicSafeHtml(messages.resumeBehaviorInfoIcon()));

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        expander.initWithContent(expanderContent.getElement());
        vcpusAdvancedParameterExpander.initWithContent(vcpusAdvancedParameterExpanderContent.getElement());

        generateIds();

        driver.initialize(this);

        initialize();
        localizeSafeHtmlFields();
        populateTabMap();
    }

    protected void populateTabMap() {
        getTabNameMapping().put(TabName.GENERAL_TAB, generalTab.getTabListItem());
        getTabNameMapping().put(TabName.BOOT_OPTIONS_TAB, this.bootOptionsTab.getTabListItem());
        getTabNameMapping().put(TabName.CONSOLE_TAB, this.consoleTab.getTabListItem());
        getTabNameMapping().put(TabName.CUSTOM_PROPERTIES_TAB, this.customPropertiesTab.getTabListItem());
        getTabNameMapping().put(TabName.HIGH_AVAILABILITY_TAB, this.highAvailabilityTab.getTabListItem());
        getTabNameMapping().put(TabName.HOST_TAB, this.hostTab.getTabListItem());
        getTabNameMapping().put(TabName.INITIAL_RUN_TAB, this.initialRunTab.getTabListItem());
        getTabNameMapping().put(TabName.POOL_TAB, this.poolTab.getTabListItem());
        getTabNameMapping().put(TabName.RESOURCE_ALLOCATION_TAB, this.resourceAllocationTab.getTabListItem());
        getTabNameMapping().put(TabName.TAB_RNG, this.rngDeviceTab.getTabListItem());
        getTabNameMapping().put(TabName.SYSTEM_TAB, this.systemTab.getTabListItem());
        getTabNameMapping().put(TabName.ICON_TAB, this.iconTab.getTabListItem());
        getTabNameMapping().put(TabName.FOREMAN_TAB, foremanTab.getTabListItem());
        getTabNameMapping().put(TabName.AFFINITY_TAB, this.affinityTab.getTabListItem());
    }

    private void initDetachableFields() {
        detachableInstanceTypesEditor = new EntityModelDetachableWidgetWithLabel(instanceTypesEditor);

        detachableMemSizeEditor = new EntityModelDetachableWidgetWithLabel(memSizeEditor);
        final EnableableFormLabel maxMemoryLabel = new EnableableFormLabel(constants.maxMemorySizePopup());
        maxMemorySizeEditor = new MemorySizeEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        detachableMaxMemorySizeEditor = new EntityModelDetachableWidgetWithInfo(maxMemoryLabel, maxMemorySizeEditor);
        isHighlyAvailableEditorWithDetachable = new EntityModelDetachableWidget(isHighlyAvailableEditor, Align.IGNORE);

        detachablePriorityEditor = new EntityModelDetachableWidgetWithLabel(priorityEditor);
        isMemoryBalloonEnabledDetachable = new EntityModelDetachableWidget(isMemoryBalloonEnabled);
        isIoThreadsEnabledDetachable = new EntityModelDetachableWidget(isIoThreadsEnabled);

        final EnableableFormLabel physMemGuarLabel = new EnableableFormLabel(constants.physMemGuarVmPopup());
        minAllocatedMemoryEditor = new MemorySizeEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        detachableMinAllocatedMemoryEditor = new EntityModelDetachableWidgetWithInfo(physMemGuarLabel, minAllocatedMemoryEditor);

        overrideMigrationDowntimeEditorWithDetachable = new EntityModelDetachableWidget(migrationDowntimeEditor, Align.IGNORE);
        overrideMigrationDowntimeEditorWithDetachable.setupContentWrapper(Align.RIGHT);

        overrideMigrationPolicyEditorWithDetachable = new EntityModelDetachableWidget(migrationPolicyEditor, Align.IGNORE);
        overrideMigrationPolicyEditorWithDetachable.setupContentWrapper(Align.RIGHT);

        migrationModeEditorWithDetachable = new EntityModelDetachableWidget(migrationModeEditor, Align.IGNORE);
        migrationModeEditorWithDetachable.setupContentWrapper(Align.RIGHT);
    }

    protected void initialize() {

    }

    protected void initSpiceProxy() {
        EnableableFormLabel label = new EnableableFormLabel();
        label.setText(constants.defineSpiceProxyEnable());
        spiceProxyOverrideEnabledEditor = new EntityModelCheckBoxOnlyEditor();
        spiceProxyEnabledCheckboxWithInfoIcon = new EntityModelWidgetWithInfo(label, spiceProxyOverrideEnabledEditor,
                Align.LEFT);
    }

    private void initTotalVcpus() {
        EnableableFormLabel label = new EnableableFormLabel();
        label.setText(constants.numOfVCPUs());
        label.addStyleName("numCPUs_pfly_fix"); //$NON-NLS-1$
        totalvCPUsEditor = StringEntityModelTextBoxOnlyEditor.newTrimmingEditor(new ModeSwitchingVisibilityRenderer());
        totalvCPUsEditorWithInfoIcon = new EntityModelDetachableWidgetWithInfo(label, totalvCPUsEditor);
        totalvCPUsEditorWithInfoIcon.setExplanation(templates.italicText(messages.hotPlugUnplugCpuWarning()));
    }

    private void initThreadsPerCore() {
        EnableableFormLabel label = new EnableableFormLabel();
        label.setText(constants.threadsPerCore());

        threadsPerCoreEditor = new ListModelListBoxOnlyEditor<>(IntegerRenderer.instance(),
                new ModeSwitchingVisibilityRenderer());
        threadsPerCoreEditorWithInfoIcon = new EntityModelDetachableWidgetWithInfo(label, threadsPerCoreEditor);
        threadsPerCoreEditorWithInfoIcon.setExplanation(multiLineItalicSafeHtml(messages.threadsPerCoreInfo()));
    }

    private SafeHtml multiLineItalicSafeHtml(String text) {
        return SafeHtmlUtils.fromTrustedString(templates.italicText(text)
                .asString()
                .replaceAll("(\r\n|\n)", "<br />")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void setSpiceProxyOverrideExplanation(String explanation) {
        spiceProxyEnabledCheckboxWithInfoIcon.setExplanation(templates.italicText(explanation));
    }

    private void initTextBoxEditors() {
        templateVersionNameEditor = new StringEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        vmIdEditor = new StringEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        descriptionEditor = new StringEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        commentEditor = new StringEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        numOfVmsEditor = new IntegerEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        cpuPinning = new StringEntityModelTextBoxOnlyEditor(new ModeSwitchingVisibilityRenderer());
        cpuSharesAmountEditor = new IntegerEntityModelTextBoxOnlyEditor(new ModeSwitchingVisibilityRenderer());
        numOfVirtioScsiMultiQueuesEditor = new IntegerEntityModelTextBoxOnlyEditor(new ModeSwitchingVisibilityRenderer());

        kernel_pathEditor = new StringEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        initrd_pathEditor = new StringEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        kernel_parametersEditor = new StringEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        nameEditor = new StringEntityModelTextBoxEditor(new ModeSwitchingVisibilityRenderer());
        nameEditor.hideLabel();
        prestartedVmsEditor = new IntegerEntityModelTextBoxOnlyEditor(new ModeSwitchingVisibilityRenderer());
        editPrestartedVmsEditor = new IntegerEntityModelTextBoxOnlyEditor(new ModeSwitchingVisibilityRenderer());
        maxAssignedVmsPerUserEditor = new IntegerEntityModelTextBoxOnlyEditor(new ModeSwitchingVisibilityRenderer());
        editMaxAssignedVmsPerUserEditor = new IntegerEntityModelTextBoxOnlyEditor(new ModeSwitchingVisibilityRenderer());
    }

    protected void initPoolSpecificWidgets() {
        createNumOfDesktopEditors();

        newPoolPrestartedVmsIcon =
                new InfoIcon(templates.italicText(messages.prestartedHelp()));

        editPoolPrestartedVmsIcon =
                new InfoIcon(templates.italicText(messages.prestartedHelp()));

        poolNameIcon =
                new InfoIcon(templates.italicText(messages.poolNameHelp()));

        newPoolMaxAssignedVmsPerUserIcon =
                new InfoIcon(templates.italicText(messages.maxAssignedVmsPerUserHelp()));

        editPoolMaxAssignedVmsPerUserIcon =
                new InfoIcon(templates.italicText(messages.maxAssignedVmsPerUserHelp()));

        numaInfoIcon = new InfoIcon(SafeHtmlUtils.fromTrustedString("")); //$NON-NLS-1$

        isRngEnabledInfoIcon = new InfoIcon(SafeHtmlUtils.fromTrustedString(constants.rngDevExplanation()));

    }

    /**
     * There are two editors which edits the same entity - in the correct subclass make sure that the correct one's
     * value is used to edit the model
     * <p>
     * The default implementation just creates the simple editors
     */
    protected void createNumOfDesktopEditors() {
        increaseNumOfVmsEditor = new IntegerEntityModelTextBoxEditor();
        numOfVmsEditor = new IntegerEntityModelTextBoxEditor();
    }

    protected abstract void generateIds();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void initListBoxEditors() {
        // General tab
        dataCenterWithClusterEditor = new GroupedListModelListBoxEditor<>(
                new GroupedListModelListBox<DataCenterWithCluster>(new NameRenderer<>()) {

            @Override
            public String getModelLabel(DataCenterWithCluster model) {
                return model.getCluster().getName();
            }

            @Override
            public String getGroupLabel(DataCenterWithCluster model) {
                return messages.hostDataCenter(model.getDataCenter().getName());
            }

            public Comparator<DataCenterWithCluster> getComparator() {
                return Comparator
                        .comparing((DataCenterWithCluster d) -> d.getDataCenter().getName(),
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(d -> d.getCluster().getName(),
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            }
        });

        quotaEditor = new ListModelTypeAheadListBoxEditor<>(
                new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<Quota>() {

                    @Override
                    public String getReplacementStringNullSafe(Quota data) {
                        return data.getQuotaName();
                    }

                    @Override
                    public String getDisplayStringNullSafe(Quota data) {
                        return typeAheadNameDescriptionTemplateNullSafe(
                                data.getQuotaName(),
                                data.getDescription()
                        );
                    }

                },
                new ModeSwitchingVisibilityRenderer());

        baseTemplateEditor = new ListModelTypeAheadListBoxEditor<>(
                new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<VmTemplate>() {

                    @Override
                    public String getReplacementStringNullSafe(VmTemplate data) {
                        return data.getName();
                    }

                    @Override
                    public String getDisplayStringNullSafe(VmTemplate data) {
                        return typeAheadNameDescriptionTemplateNullSafe(
                                data.getName(),
                                data.getDescription()
                        );
                    }
                },
                new ModeSwitchingVisibilityRenderer());

        templateWithVersionEditor = new ListModelTypeAheadListBoxEditor<>(
                new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<TemplateWithVersion>(){
                    @Override
                    public String getReplacementStringNullSafe(TemplateWithVersion templateWithVersion) {
                        return getFirstColumn(templateWithVersion)
                                + " | " //$NON-NLS-1$
                                + getSecondColumn(templateWithVersion);
                    }

                    @Override
                    public String getDisplayStringNullSafe(TemplateWithVersion templateWithVersion) {
                        return typeAheadNameDescriptionTemplateNullSafe(
                                getFirstColumn(templateWithVersion),
                                getSecondColumn(templateWithVersion));
                    }

                    private String getFirstColumn(TemplateWithVersion templateWithVersion) {
                        return templateWithVersion.getBaseTemplate().getName();
                    }

                    private String getSecondColumn(TemplateWithVersion templateWithVersion) {
                        final VmTemplate versionTemplate = templateWithVersion.getTemplateVersion();
                        final String versionName = versionTemplate.getTemplateVersionName() == null
                                ? "" //$NON-NLS-1$
                                : versionTemplate.getTemplateVersionName() + " "; //$NON-NLS-1$
                        return templateWithVersion.isLatest()
                                ? constants.latest()
                                : versionName + "(" //$NON-NLS-1$
                                        + versionTemplate.getTemplateVersionNumber() + ")"; //$NON-NLS-1$
                    }
                },
                new ModeSwitchingVisibilityRenderer(),
                new SuggestionMatcher.ContainsSuggestionMatcher());

        oSTypeEditor = new ListModelListBoxEditor<>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                return AsyncDataProvider.getInstance().getOsName(object);
            }
        }, new ModeSwitchingVisibilityRenderer());

        vmTypeEditor = new ListModelListBoxEditor<>(new EnumRenderer<VmType>(), new ModeSwitchingVisibilityRenderer());

        instanceTypesEditor = new ListModelTypeAheadListBoxEditor<>(
                new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<InstanceType>() {

                    @Override
                    public String getReplacementStringNullSafe(InstanceType data) {
                        return data.getName();
                    }

                    @Override
                    public String getDisplayStringNullSafe(InstanceType data) {
                        return typeAheadNameDescriptionTemplateNullSafe(
                                data.getName(),
                                data.getDescription()
                        );
                    }
                },
                new ModeSwitchingVisibilityRenderer()
        );


        emulatedMachine = new ListModelTypeAheadChangeableListBoxEditor(
                new ListModelTypeAheadChangeableListBoxEditor.NullSafeSuggestBoxRenderer() {

                    @Override
                    public String getDisplayStringNullSafe(String data) {
                        if (data == null || data.trim().isEmpty()) {
                            data = ""; //$NON-NLS
                        }
                        return typeAheadNameTemplateNullSafe(data);
                    }
                },
                false,
                new ModeSwitchingVisibilityRenderer(),
                ""); //$NON-NLS

        customCpu = new ListModelTypeAheadChangeableListBoxEditor(
                new ListModelTypeAheadChangeableListBoxEditor.NullSafeSuggestBoxRenderer() {

                    @Override
                    public String getDisplayStringNullSafe(String data) {
                        if (data == null || data.trim().isEmpty()) {
                            data = getDefaultCpuTypeLabel();
                        }
                        return typeAheadNameTemplateNullSafe(data);
                    }
                },
                false,
                new ModeSwitchingVisibilityRenderer(),
                constants.clusterDefaultOption());

        numOfSocketsEditor = new ListModelListBoxEditor<>(new ModeSwitchingVisibilityRenderer());
        numOfSocketsEditorWithDetachable = new EntityModelDetachableWidgetWithLabel(numOfSocketsEditor);
        corePerSocketEditor = new ListModelListBoxEditor<>(new ModeSwitchingVisibilityRenderer());
        corePerSocketEditorWithDetachable = new EntityModelDetachableWidgetWithLabel(corePerSocketEditor);
        initThreadsPerCore();

        // Pools
        poolTypeEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<EntityModel<VmPoolType>>() {
            @Override
            public String renderNullSafe(EntityModel<VmPoolType> object) {
                return object.getTitle();
            }
        }, new ModeSwitchingVisibilityRenderer());

        timeZoneEditor = new ListModelListBoxOnlyEditor<>(new NullSafeRenderer<TimeZoneModel>() {
            @Override
            public String renderNullSafe(TimeZoneModel timeZone) {
                if (timeZone.isDefault()) {
                    return messages.defaultTimeZoneCaption(timeZone.getDisplayValue());
                } else {
                    return timeZone.getDisplayValue();
                }
            }
        }, new ModeSwitchingVisibilityRenderer());

        EnableableFormLabel label = new EnableableFormLabel();
        label.setText(constants.timeZoneVm());
        timeZoneEditorWithInfo = new EntityModelWidgetWithInfo(label, timeZoneEditor);
        timeZoneEditorWithInfo.setExplanation(templates.italicText(constants.timeZoneInfo()));

        // Console tab
        displayTypeEditor = new ListModelListBoxEditor<>(
                new EnumRenderer<DisplayType>(),
                new ModeSwitchingVisibilityRenderer());

        graphicsTypeEditor = new ListModelListBoxEditor<>(new EnumRenderer<UnitVmModel.GraphicsTypes>());

        consoleDisconnectActionEditor =
                new ListModelListBoxEditor<>(new EnumRenderer<ConsoleDisconnectAction>(), new ModeSwitchingVisibilityRenderer());

        numOfMonitorsEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Integer>() {
            @Override
            public String renderNullSafe(Integer object) {
                return object.toString();
            }
        }, new ModeSwitchingVisibilityRenderer());
        numOfMonitorsEditor.hideLabel();

        vncKeyboardLayoutEditor = new ListModelListBoxEditor<>(new VncKeyMapRenderer(), new ModeSwitchingVisibilityRenderer());

        serialNumberPolicyRenderer = new ClusterDefaultRenderer<>(new EnumRenderer<SerialNumberPolicy>());
        serialNumberPolicyEditor = new ListModelListBoxEditor<>(
                serialNumberPolicyRenderer,
                new ModeSwitchingVisibilityRenderer());

        // Host Tab
        specificHost = new EntityModelRadioButtonEditor("runVmOnHostGroup", new ModeSwitchingVisibilityRenderer()); //$NON-NLS-1$
        isAutoAssignEditor =
                new EntityModelRadioButtonEditor("runVmOnHostGroup", new ModeSwitchingVisibilityRenderer()); //$NON-NLS-1$
        defaultHostEditor = new ListModelMultipleSelectListBoxEditor<>(new NameRenderer<VDS>(),
                new ModeSwitchingVisibilityRenderer());
        defaultHostEditor.hideLabel();

        migrationModeEditor =
                new ListModelListBoxEditor<>(new EnumRenderer<MigrationSupport>(), new ModeSwitchingVisibilityRenderer());
        migrationModeEditor.hideLabel();

        migrationDowntimeEditor = new ListModelTypeAheadListBoxEditor<>(
                new IntegerListModelTypeAheadListBox(getNullMigrationDowntimeString()), new ModeSwitchingVisibilityRenderer());
        migrationDowntimeEditor.hideLabel();

        migrationPolicyRenderer = new ClusterDefaultRenderer<>(new NameRenderer<MigrationPolicy>());
        migrationPolicyEditor = new ListModelListBoxOnlyEditor<>(migrationPolicyRenderer, new ModeSwitchingVisibilityRenderer());

        autoConvergeRenderer = new ClusterDefaultRenderer<>(new BooleanRenderer(constants.autoConverge(), constants.dontAutoConverge()));
        autoConvergeEditor = new ListModelListBoxEditor<>(
                autoConvergeRenderer,
                new ModeSwitchingVisibilityRenderer());

        migrateCompressedRenderer = new ClusterDefaultRenderer<>(new BooleanRenderer(constants.compress(), constants.dontCompress()));
        migrateCompressedEditor = new ListModelListBoxEditor<>(
                migrateCompressedRenderer,
                new ModeSwitchingVisibilityRenderer());

        migrateEncryptedRenderer = new ClusterDefaultRenderer<>(new MigrateEncryptedRenderer());
        migrateEncryptedEditor = new ListModelListBoxEditor<>(
                migrateEncryptedRenderer,
                new ModeSwitchingVisibilityRenderer());

        // Resource Allocation
        provisioningThinEditor =
                new EntityModelRadioButtonEditor("provisioningGroup", new ModeSwitchingVisibilityRenderer()); //$NON-NLS-1$
        provisioningCloneEditor =
                new EntityModelRadioButtonEditor("provisioningGroup", new ModeSwitchingVisibilityRenderer()); //$NON-NLS-1$

        // Boot Options Tab
        firstBootDeviceEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<EntityModel<BootSequence>>() {
            @Override
            public String renderNullSafe(EntityModel<BootSequence> object) {
                return object.getTitle();
            }
        }, new ModeSwitchingVisibilityRenderer());

        secondBootDeviceEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<EntityModel<BootSequence>>() {
            @Override
            public String renderNullSafe(EntityModel<BootSequence> object) {
                return object.getTitle();
            }
        }, new ModeSwitchingVisibilityRenderer());

        cdImageEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<RepoImage>() {
            @Override
            public String renderNullSafe(RepoImage object) {
                // For old ISO images from an ISO domain the image name is empty
                if (StringHelper.isNullOrEmpty(object.getRepoImageName())) {
                    return object.getRepoImageId();
                }
                return object.getRepoImageName();
            }
        }, new ModeSwitchingVisibilityRenderer());
        cdImageEditor.hideLabel();

        cpuProfilesEditor = new ListModelListBoxEditor<>(new NameRenderer<CpuProfile>());

        cpuSharesAmountSelectionEditor =
                new ListModelListBoxOnlyEditor<>(new EnumRenderer<UnitVmModel.CpuSharesAmount>(), new ModeSwitchingVisibilityRenderer());

        cpuPinningPolicyListBox = new CpuPinningPolicyListBox();
        cpuPinningPolicyEditor = new ListModelTypeAheadListBoxEditor<>(
                cpuPinningPolicyListBox,
                new ModeSwitchingVisibilityRenderer()
        );

        virtioScsiMultiQueueSelectionEditor =
                new ListModelListBoxOnlyEditor<>(new EnumRenderer<VirtioMultiQueueType>(), new ModeSwitchingVisibilityRenderer());

        providersEditor = new ListModelListBoxEditor<>(new NameRenderer<Provider<OpenstackNetworkProviderProperties>>());
        providersEditor.setLabel(constants.providerLabel());

        biosTypeRenderer = new BiosTypeRenderer();
        biosTypeEditor = new ListModelListBoxEditor<>(biosTypeRenderer, new ModeSwitchingVisibilityRenderer());
    }

    private String typeAheadNameDescriptionTemplateNullSafe(String name, String description) {
        return templates.typeAheadNameDescription(
                name != null ? name : "",
                description != null ? description : "")
                .asString();
    }

    private String typeAheadNameTemplateNullSafe(String name) {
        if (name != null && !name.trim().isEmpty()) {
            return templates.typeAheadName(name).asString();
        } else {
            return templates.typeAheadEmptyContent().asString();
        }
    }

    @Override
    public void edit(UnitVmModel model) {
        super.edit(model);
        unitVmModel = model;

        super.initializeModeSwitching(generalTab);

        driver.edit(model);
        profilesInstanceTypeEditor.edit(model.getNicsWithLogicalNetworks());
        instanceImagesEditor.edit(model.getInstanceImages());
        customPropertiesSheetEditor.edit(model.getCustomPropertySheet());
        vmInitEditor.edit(model.getVmInitModel());
        affinityGroupSelectionWidget.init(model.getAffinityGroupList());
        affinityLabelSelectionWidget.init(model.getLabelList());
        quotaEditor.setEnabled(!model.isHostedEngine());
        initTabAvailabilityListeners(model);
        initListeners(model);
        hideAlwaysHiddenFields();
        decorateDetachableFields();
        enableNumaSupport(model);
        // Hiding IO threads panel here if needed.
        // When editing an existing VM, the UnitVmModel.IoThreadsEnabled entity
        // is set to a value before this method is called and so the callback
        // that would hide the IO threads panel was not yet created.
        ioThreadsPanel.setVisible(model.getIoThreadsEnabled().getEntity());
    }

    private <T> void initClusterDefaultValueListener(ClusterDefaultRenderer<T> renderer, ListModel<T> model) {
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (CLUSTER_VALUE_EVENT.equals(((PropertyChangedEventArgs) args).propertyName)) {
                renderer.setDefaultValue(((ListModelWithClusterDefault<T>) model).getClusterValue());
                // if the cluster default value has changed we need to redraw the items, thus firing
                // the "Items" change event.
                model.fireItemsChangedEvent();
            }
        });
    }

    private void enableNumaSupport(final UnitVmModel model) {
        numaSupportButton.setCommand(model.getNumaSupportCommand());
        numaPanel.setVisible(false);
        enableNumaFields(false);
        model.getNumaEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> {
            numaPanel.setVisible(true);
            enableNumaFields(model.getNumaEnabled().getEntity());
            setNumaInfoMsg(model.getNumaEnabled().getMessage());
        });
    }

    private void localizeSafeHtmlFields() {
        rngSourceUrandomInfoIcon.setText(SafeHtmlUtils.fromString(constants.vmUrandomInfoIcon()));
        detachableMaxMemorySizeEditor.setExplanation(SafeHtmlUtils.fromString(constants.maxMemoryInfoIcon()));
        detachableMinAllocatedMemoryEditor.setExplanation(SafeHtmlUtils.fromString(constants.physMemGuarInfoIcon()));
    }

    private void setNumaInfoMsg(String message) {
        if (message == null) {
            message = ""; //$NON-NLS-1$
        }
        numaInfoIcon.setText(multiLineItalicSafeHtml(message));
    }

    private void enableNumaFields(boolean enabled) {
        numaNodeCount.setEnabled(enabled);
        numaSupportButton.setEnabled(enabled);
    }

    @UiHandler("refreshButton")
    void handleRefreshButtonClick(ClickEvent event) {
        unitVmModel.getBehavior().refreshCdImages();
    }

    protected void setupCustomPropertiesAvailability(UnitVmModel model) {
        changeApplicationLevelVisibility(customPropertiesTab, model.getIsCustomPropertiesTabAvailable());
    }

    protected void initListeners(final UnitVmModel object) {
        // TODO should be handled by the core framework
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("IsCustomPropertiesTabAvailable".equals(propName)) { //$NON-NLS-1$
                setupCustomPropertiesAvailability(object);
            } else if ("IsDisksAvailable".equals(propName)) { //$NON-NLS-1$
                addDiskAllocation(object);
            }
        });

        object.getIsAutoAssign().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            boolean isAutoAssign = object.getIsAutoAssign().getEntity();
            defaultHostEditor.setEnabled(!isAutoAssign);

            // only this is not bind to the model, so needs to listen to the change explicitly
            specificHost.asRadioButton().setValue(!isAutoAssign);
        });

        object.getProvisioning().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            boolean isProvisioningChangable = object.getProvisioning().getIsChangable();
            provisioningThinEditor.setEnabled(isProvisioningChangable);
            provisioningCloneEditor.setEnabled(isProvisioningChangable);

            boolean isProvisioningAvailable = object.getProvisioning().getIsAvailable();
            changeApplicationLevelVisibility(provisionSelectionPanel, isProvisioningAvailable);

            boolean isDisksAvailable = object.getIsDisksAvailable();
            changeApplicationLevelVisibility(disksAllocationPanel, isDisksAvailable ||
                    object.getIsVirtioScsiEnabled().getIsAvailable());

            changeApplicationLevelVisibility(storageAllocationPanel, isProvisioningAvailable);
        });

        object.getIsVirtioScsiEnabled().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                isVirtioScsiEnabledInfoIcon.setVisible(object.getIsVirtioScsiEnabled().getIsAvailable());
            }
        });

        object.getEditingEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> {
            Boolean enabled = object.getEditingEnabled().getEntity();
            if (Boolean.FALSE.equals(enabled)) {
                disableAllTabs();
                generalWarningMessage.setText(object.getEditingEnabled().getMessage());
                generalWarningMessage.setVisible(true);
            }
        });

        object.getCpuSharesAmountSelection().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                changeApplicationLevelVisibility(cpuSharesEditorRow, object.getCpuSharesAmountSelection().getIsAvailable());
            }
        });

        object.getOSType().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            Integer osType = object.getOSType().getSelectedItem();

            boolean isIgnition = AsyncDataProvider.getInstance().isIgnition(osType);
            if (isIgnition) {
                String ignitionVersion = AsyncDataProvider.getInstance().getIgnitionVersion(osType);
                vmInitEnabledEditor.setLabel(constants.ignition() + " " + ignitionVersion); //$NON-NLS-1$
                return;
            }

            boolean isWindows = AsyncDataProvider.getInstance().isWindowsOsType(osType);
            if (isWindows){
                vmInitEnabledEditor.setLabel(constants.sysprep());
                return;
            }

            vmInitEnabledEditor.setLabel(constants.cloudInit());
        });

        object.getCloudInitEnabled().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (object.getCloudInitEnabled().getEntity() != null) {
                vmInitEditor.setCloudInitContentVisible(object.getCloudInitEnabled().getEntity());
            }
        });

        object.getIgnitionEnabled().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (object.getIgnitionEnabled().getEntity() != null) {
                vmInitEditor.setIgnitionContentVisible(object.getIgnitionEnabled().getEntity());
            }
        });

        object.getSysprepEnabled().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (object.getSysprepEnabled().getEntity() != null) {
                vmInitEditor.setSyspepContentVisible(object.getSysprepEnabled().getEntity());
            }
        });

        object.getIsRngEnabled().getPropertyChangedEvent().addListener((ev, sender, args) -> rngPanel.setVisible(object.getIsRngEnabled().getEntity()));

        object.getDataCenterWithClustersList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            customCpu.setNullReplacementString(getDefaultCpuTypeLabel());
            updateUrandomLabel(object);
            biosTypeRenderer.setArchitectureType(getModel().getSelectedCluster() == null ? null : getModel().getSelectedCluster().getArchitecture());
            getModel().getBiosType().fireItemsChangedEvent();
        });

        object.getCpuPinningPolicy().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (CpuPinningListModel.ITEMS_ENABLED_PROPERTY_CHANGE.equals(args.propertyName)) {
                // re-render the editor to change the colors of enabled/disabled policies properly
                cpuPinningPolicyListBox.render(object.getCpuPinningPolicy().getSelectedItem(), false);
            }
        });

        object.getIoThreadsEnabled().getEntityChangedEvent().addListener(
                (ev, sender, args) -> ioThreadsPanel.setVisible(object.getIoThreadsEnabled().getEntity()));

        object.getCustomCompatibilityVersion().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateUrandomLabel(object));

        object.getIsHeadlessModeEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> {
            boolean isHeadlessEnabled = object.getIsHeadlessModeEnabled().getEntity();
            ssoMethodLabel.setEnabled(!isHeadlessEnabled);
            monitorsLabel.setEnabled(!isHeadlessEnabled);
            spiceProxyEnabledCheckboxWithInfoIcon.setEnabled(!isHeadlessEnabled);
        });

        object.getMultiQueues().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                multiQueuesLabel.setVisible(object.getMultiQueues().getIsAvailable());
                multiQueuesInfo.setVisible(object.getMultiQueues().getIsAvailable());
            }
        });

        object.getMigrationDowntime().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                migrationDowntimeLabel.setVisible(object.getMigrationDowntime().getIsAvailable());
                migrationDowntimeInfoIcon.setVisible(object.getMigrationDowntime().getIsAvailable());
                overrideMigrationDowntimeEditorWithDetachable.setVisible(object.getMigrationDowntime().getIsAvailable());
            }

            if ("IsChangable".equals(args.propertyName)) { //$NON-NLS-1$
                migrationDowntimeLabel.setEnabled(object.getMigrationDowntime().getIsChangable());
            }
        });

        initClusterDefaultValueListener(serialNumberPolicyRenderer, getModel().getSerialNumberPolicy());
        initClusterDefaultValueListener(migrateEncryptedRenderer, getModel().getMigrateEncrypted());
        initClusterDefaultValueListener(autoConvergeRenderer, getModel().getAutoConverge());
        initClusterDefaultValueListener(migrateCompressedRenderer, getModel().getMigrateCompressed());
        initClusterDefaultValueListener(migrationPolicyRenderer, getModel().getMigrationPolicies());
    }

    private void updateUrandomLabel(UnitVmModel model) {
        final Version effectiveVersion = model.getCompatibilityVersion();
        if (effectiveVersion == null) {
            return;
        }
        final String urandomSourceLabel = effectiveVersion.greaterOrEquals(VmRngDevice.Source.FIRST_URANDOM_VERSION)
                ? constants.rngSourceUrandom()
                : constants.rngSourceRandom();
        rngSourceUrandom.setLabel(urandomSourceLabel);
    }

    private String getDefaultCpuTypeLabel() {
        Cluster cluster = getModel().getSelectedCluster();
        String newClusterCpuModel = constants.clusterDefaultOption();
        if (cluster != null) {
            String cpuName = (cluster.getCpuName() == null) ? "" : cluster.getCpuName(); //$NON-NLS-1$
            newClusterCpuModel += "(" + cpuName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return newClusterCpuModel;
    }

    private void addDiskAllocation(UnitVmModel model) {
        disksAllocationView.edit(model.getDisksAllocationModel());
        model.getDisksAllocationModel().setDisks(model.getDisks());
    }

    private void initTabAvailabilityListeners(final UnitVmModel vm) {
        // TODO should be handled by the core framework
        vm.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("IsHighlyAvailable".equals(propName)) { //$NON-NLS-1$
                changeApplicationLevelVisibility(highAvailabilityTab, vm.getIsHighlyAvailable().getEntity());
            } else if ("IsDisksAvailable".equals(propName)) { //$NON-NLS-1$
                boolean isDisksAvailable = vm.getIsDisksAvailable();
                changeApplicationLevelVisibility(disksPanel, isDisksAvailable);

                boolean isProvisioningAvailable = vm.getProvisioning().getIsAvailable();
                changeApplicationLevelVisibility(storageAllocationPanel, isProvisioningAvailable);

                changeApplicationLevelVisibility(disksAllocationPanel, isDisksAvailable ||
                        vm.getIsVirtioScsiEnabled().getIsAvailable());

                if (isDisksAvailable) {
                    // Update warning message by disks status
                    updateDisksWarningByImageStatus(vm.getDisks(), ImageStatus.ILLEGAL);
                    updateDisksWarningByImageStatus(vm.getDisks(), ImageStatus.LOCKED);
                } else {
                    // Clear warning message
                    generalWarningMessage.setText(""); //$NON-NLS-1$
                    generalWarningMessage.setVisible(false);
                }
            }

        });

        // TODO: Move to a more appropriate method
        vm.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("IsLinuxOS".equals(propName)) { //$NON-NLS-1$
                changeApplicationLevelVisibility(linuxBootOptionsPanel, vm.getIsLinuxOS());
            }
        });

        defaultHostEditor.setEnabled(false);
        specificHost.asRadioButton().addValueChangeHandler(event -> {
            defaultHostEditor.setEnabled(specificHost.asRadioButton().getValue());
            ValueChangeEvent.fire(isAutoAssignEditor.asRadioButton(), false);
        });

        rngSourceUrandom.asRadioButton().addValueChangeHandler(booleanValueChangeEvent -> {
            vm.getRngSourceUrandom().setEntity(true);
            vm.getRngSourceHwrng().setEntity(false);
        });

        rngSourceHwrng.asRadioButton().addValueChangeHandler(booleanValueChangeEvent -> {
            vm.getRngSourceHwrng().setEntity(true);
            vm.getRngSourceUrandom().setEntity(false);
        });

        vm.getIsAutoAssign().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (!isAutoAssignEditor.asRadioButton().getValue()) {
                    specificHost.asRadioButton().setValue(true, true);
            }
        });

        ssoMethodGuestAgent.asRadioButton().addValueChangeHandler(event -> {
            if (Boolean.TRUE.equals(event.getValue())) {
                ValueChangeEvent.fire(ssoMethodNone.asRadioButton(), false);
            }
        });

        ssoMethodNone.asRadioButton().addValueChangeHandler(event -> {
            if (Boolean.TRUE.equals(event.getValue())) {
                ValueChangeEvent.fire(ssoMethodGuestAgent.asRadioButton(), false);
            }
        });
    }

    private void updateDisksWarningByImageStatus(List<DiskModel> disks, ImageStatus imageStatus) {
        ArrayList<String> disksAliases =
                getDisksAliasesByImageStatus(disks, imageStatus);

        if (!disksAliases.isEmpty()) {
            generalWarningMessage.setText(messages.disksStatusWarning(
                    EnumTranslator.getInstance().translate(imageStatus),
                    String.join(", ", disksAliases))); //$NON-NLS-1$
            generalWarningMessage.setVisible(true);
        }
    }

    private ArrayList<String> getDisksAliasesByImageStatus(List<DiskModel> disks, ImageStatus status) {
        ArrayList<String> disksAliases = new ArrayList<>();

        if (disks == null) {
            return disksAliases;
        }

        for (DiskModel diskModel : disks) {
            Disk disk = diskModel.getDisk();
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                    ((DiskImage) disk).getImageStatus() == status) {

                disksAliases.add(disk.getDiskAlias());
            }
        }

        return disksAliases;
    }

    @Override
    public UnitVmModel flush() {
        profilesInstanceTypeEditor.flush();
        vmInitEditor.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
        if (unitVmModel != null) {
            unitVmModel.cleanup();
            unitVmModel = null;
        }
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

    @Override
    public int setTabIndexes(int nextTabIndex) {
        // ==General Tab==
        nextTabIndex = generalTab.setTabIndexes(nextTabIndex);
        oSTypeEditor.setTabIndex(nextTabIndex++);
        baseTemplateEditor.setTabIndex(nextTabIndex++);
        instanceTypesEditor.setTabIndexes(nextTabIndex++);
        templateWithVersionEditor.setTabIndexes(nextTabIndex++);
        quotaEditor.setTabIndex(nextTabIndex++);

        nameEditor.setTabIndex(nextTabIndex++);
        templateVersionNameEditor.setTabIndex(nextTabIndex++);
        descriptionEditor.setTabIndex(nextTabIndex++);
        commentEditor.setTabIndex(nextTabIndex++);
        vmIdEditor.setTabIndex(nextTabIndex++);
        isStatelessEditor.setTabIndex(nextTabIndex++);
        isRunAndPauseEditor.setTabIndex(nextTabIndex++);
        isDeleteProtectedEditor.setTabIndex(nextTabIndex++);
        isSealedEditor.setTabIndex(nextTabIndex++);
        copyTemplatePermissionsEditor.setTabIndex(nextTabIndex++);

        numOfVmsEditor.setTabIndex(nextTabIndex++);
        prestartedVmsEditor.setTabIndex(nextTabIndex++);
        editPrestartedVmsEditor.setTabIndex(nextTabIndex++);
        increaseNumOfVmsEditor.setTabIndex(nextTabIndex++);
        maxAssignedVmsPerUserEditor.setTabIndex(nextTabIndex++);
        editMaxAssignedVmsPerUserEditor.setTabIndex(nextTabIndex++);

        // ==System Tab==
        nextTabIndex = systemTab.setTabIndexes(nextTabIndex);
        memSizeEditor.setTabIndex(nextTabIndex++);
        maxMemorySizeEditor.setTabIndex(nextTabIndex++);
        minAllocatedMemoryEditor.setTabIndex(nextTabIndex++);
        totalvCPUsEditor.setTabIndex(nextTabIndex++);

        nextTabIndex = vcpusAdvancedParameterExpander.setTabIndexes(nextTabIndex);
        numOfSocketsEditor.setTabIndex(nextTabIndex++);
        corePerSocketEditor.setTabIndex(nextTabIndex++);
        threadsPerCoreEditor.setTabIndex(nextTabIndex++);
        biosTypeEditor.setTabIndex(nextTabIndex++);
        emulatedMachine.setTabIndex(nextTabIndex++);
        customCpu.setTabIndex(nextTabIndex++);
        serialNumberPolicyEditor.setTabIndex(nextTabIndex++);
        customSerialNumberEditor.setTabIndex(nextTabIndex++);

        // == Pools ==
        nextTabIndex = poolTab.setTabIndexes(nextTabIndex);
        poolTypeEditor.setTabIndex(nextTabIndex++);
        poolStatefulEditor.setTabIndex(nextTabIndex++);

        // ==Initial run Tab==
        nextTabIndex = initialRunTab.setTabIndexes(nextTabIndex);
        timeZoneEditor.setTabIndex(nextTabIndex++);

        // ==Console Tab==
        nextTabIndex = consoleTab.setTabIndexes(nextTabIndex);
        isHeadlessModeEnabledEditor.setTabIndex(nextTabIndex++);
        displayTypeEditor.setTabIndex(nextTabIndex++);
        graphicsTypeEditor.setTabIndex(nextTabIndex++);
        vncKeyboardLayoutEditor.setTabIndex(nextTabIndex++);
        isUsbEnabledEditor.setTabIndex(nextTabIndex++);
        consoleDisconnectActionEditor.setTabIndexes(nextTabIndex++);
        numOfMonitorsEditor.setTabIndex(nextTabIndex++);
        consoleDisconnectActionDelayEditor.setTabIndexes(nextTabIndex++);
        isSmartcardEnabledEditor.setTabIndex(nextTabIndex++);
        ssoMethodNone.setTabIndex(nextTabIndex++);
        ssoMethodGuestAgent.setTabIndex(nextTabIndex++);
        nextTabIndex = expander.setTabIndexes(nextTabIndex);
        allowConsoleReconnectEditor.setTabIndex(nextTabIndex++);
        isSoundcardEnabledEditor.setTabIndex(nextTabIndex++);
        isConsoleDeviceEnabledEditor.setTabIndex(nextTabIndex++);
        spiceProxyOverrideEnabledEditor.setTabIndex(nextTabIndex++);
        spiceProxyEditor.setTabIndex(nextTabIndex++);
        spiceFileTransferEnabledEditor.setTabIndex(nextTabIndex++);
        spiceCopyPasteEnabledEditor.setTabIndex(nextTabIndex++);

        // ==Host Tab==
        nextTabIndex = hostTab.setTabIndexes(nextTabIndex);
        isAutoAssignEditor.setTabIndex(nextTabIndex++);
        specificHost.setTabIndex(nextTabIndex++);
        defaultHostEditor.setTabIndex(nextTabIndex++);
        migrationModeEditor.setTabIndex(nextTabIndex++);
        migrationPolicyEditor.setTabIndex(nextTabIndex++);
        migrationDowntimeEditor.setTabIndex(nextTabIndex++);
        autoConvergeEditor.setTabIndex(nextTabIndex++);
        migrateCompressedEditor.setTabIndex(nextTabIndex++);
        migrateEncryptedEditor.setTabIndex(nextTabIndex++);
        hostCpuEditor.setTabIndex(nextTabIndex++);
        tscFrequencyEditor.setTabIndexes(nextTabIndex++);
        customCompatibilityVersionEditor.setTabIndex(nextTabIndex++);

        numaNodeCount.setTabIndex(nextTabIndex++);
        // ==High Availability Tab==
        nextTabIndex = highAvailabilityTab.setTabIndexes(nextTabIndex);
        isHighlyAvailableEditor.setTabIndex(nextTabIndex++);
        priorityEditor.setTabIndex(nextTabIndex++);

        watchdogModelEditor.setTabIndex(nextTabIndex++);
        watchdogActionEditor.setTabIndex(nextTabIndex++);

        // ==Resource Allocation Tab==
        nextTabIndex = resourceAllocationTab.setTabIndexes(nextTabIndex);
        cpuProfilesEditor.setTabIndex(nextTabIndex++);
        provisioningThinEditor.setTabIndex(nextTabIndex++);
        provisioningCloneEditor.setTabIndex(nextTabIndex++);
        cpuPinningPolicyEditor.setTabIndex(nextTabIndex++);
        cpuPinning.setTabIndex(nextTabIndex++);
        cpuSharesAmountEditor.setTabIndex(nextTabIndex++);
        tpmEnabledEditor.setTabIndex(nextTabIndex++);
        numOfIoThreadsEditor.setTabIndex(nextTabIndex++);
        multiQueues.setTabIndex(nextTabIndex++);
        numOfVirtioScsiMultiQueuesEditor.setTabIndex(nextTabIndex++);
        nextTabIndex = disksAllocationView.setTabIndexes(nextTabIndex);

        // ==Boot Options Tab==
        nextTabIndex = bootOptionsTab.setTabIndexes(nextTabIndex);
        firstBootDeviceEditor.setTabIndex(nextTabIndex++);
        secondBootDeviceEditor.setTabIndex(nextTabIndex++);
        cdAttachedEditor.setTabIndex(nextTabIndex++);
        cdImageEditor.setTabIndex(nextTabIndex++);
        bootMenuEnabledEditor.setTabIndex(nextTabIndex++);
        kernel_pathEditor.setTabIndex(nextTabIndex++);
        initrd_pathEditor.setTabIndex(nextTabIndex++);
        kernel_parametersEditor.setTabIndex(nextTabIndex++);

        // ==Rng Tab==
        nextTabIndex = rngDeviceTab.setTabIndexes(nextTabIndex);
        isRngEnabledEditor.setTabIndex(nextTabIndex++);
        rngPeriodEditor.setTabIndex(nextTabIndex++);
        rngBytesEditor.setTabIndex(nextTabIndex++);
        rngSourceUrandom.setTabIndex(nextTabIndex++);
        rngSourceHwrng.setTabIndex(nextTabIndex++);

        // ==Custom Properties Tab==
        nextTabIndex = customPropertiesTab.setTabIndexes(nextTabIndex);

        // ==Icon Tab==
        nextTabIndex = iconTab.setTabIndexes(nextTabIndex);
        iconEditorWidget.setTabIndex(nextTabIndex++);

        // ==Foreman Tab==
        nextTabIndex = foremanTab.setTabIndexes(nextTabIndex);

        // ==Affinity Labels Tab==
        nextTabIndex = affinityTab.setTabIndexes(nextTabIndex);

        return nextTabIndex;
    }

    @Override
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        return super.createWidgetConfiguration().
                putAll(allTabs(), simpleField().visibleInAdvancedModeOnly()).
                putAll(advancedFieldsFromGeneralTab(), simpleField().visibleInAdvancedModeOnly()).
                putAll(consoleTabWidgets(), simpleField().visibleInAdvancedModeOnly()).
                update(consoleTab, simpleField()).
                update(numOfMonitorsEditor, simpleField()).
                putOne(isSoundcardEnabledEditor, simpleField().visibleInAdvancedModeOnly()).
                putOne(isConsoleDeviceEnabledEditor, simpleField().visibleInAdvancedModeOnly()).
                putOne(spiceFileTransferEnabledEditor, simpleField().visibleInAdvancedModeOnly()).
                putOne(spiceCopyPasteEnabledEditor, simpleField().visibleInAdvancedModeOnly()).
                putOne(instanceImagesEditor, hiddenField());
    }

    protected List<Widget> consoleTabWidgets() {
        return Arrays.asList(
                isHeadlessModeEnabledEditor,
                isHeadlessModeEnabledInfoIcon,
                displayTypeEditor,
                graphicsTypeEditor,
                isUsbEnabledEditor,
                consoleDisconnectActionEditor,
                consoleDisconnectActionDelayEditor,
                isSmartcardEnabledEditor,
                expander,
                numOfMonitorsEditor,
                vncKeyboardLayoutEditor,
                ssoMethodLabel,
                ssoMethodNone,
                ssoMethodGuestAgent,
                serialConsoleOptionsVmPopupLabel
        );
    }

    protected List<Widget> poolSpecificFields() {
        return Arrays.asList(
                numOfVmsEditor,
                newPoolEditVmsRow,
                editPoolEditVmsRow,
                editPoolIncreaseNumOfVmsRow,
                poolTab.getTabListItem(),
                prestartedVmsEditor,
                poolNameIcon,
                newPoolEditMaxAssignedVmsPerUserRow,
                editPoolEditMaxAssignedVmsPerUserRow,
                spiceProxyEditor,
                spiceProxyEnabledCheckboxWithInfoIcon,
                spiceProxyOverrideEnabledEditor
                );
    }

    protected List<Widget> allTabs() {
        return Arrays.asList(
                initialRunTab.getTabListItem(),
                consoleTab.getTabListItem(),
                hostTab.getTabListItem(),
                resourceAllocationTab.getTabListItem(),
                bootOptionsTab.getTabListItem(),
                customPropertiesTab.getTabListItem(),
                rngDeviceTab.getTabListItem(),
                highAvailabilityTab.getTabListItem(),
                poolTab.getTabListItem(),
                systemTab.getTabListItem(),
                iconTab.getTabListItem(),
                foremanTab.getTabListItem(),
                affinityTab.getTabListItem()
                );
    }

    protected List<Widget> advancedFieldsFromGeneralTab() {
        return Arrays.asList(
                memSizeEditor,
                maxMemorySizeEditor,
                totalvCPUsEditor,
                vcpusAdvancedParameterExpander,
                copyTemplatePermissionsEditor,
                vmIdEditor
        );
    }

    protected List<Widget> detachableWidgets() {
        return Arrays.asList(
                totalvCPUsEditorWithInfoIcon,
                numOfSocketsEditorWithDetachable,
                corePerSocketEditorWithDetachable,
                threadsPerCoreEditorWithInfoIcon,
                isHighlyAvailableEditorWithDetachable,
                isMemoryBalloonEnabledDetachable,
                isIoThreadsEnabledDetachable,
                detachablePriorityEditor,
                migrationModeEditorWithDetachable,
                detachableMinAllocatedMemoryEditor,
                detachableMemSizeEditor,
                detachableMaxMemorySizeEditor,
                detachableInstanceTypesEditor,
                overrideMigrationDowntimeEditorWithDetachable,
                overrideMigrationPolicyEditorWithDetachable
        );
    }

    protected List<Widget> adminOnlyWidgets() {
        return Arrays.asList(
                // general tab
                vmIdEditor,

                // system tab
                detachableMemSizeEditor,
                detachableMaxMemorySizeEditor,
                totalvCPUsEditorWithInfoIcon,
                vcpusAdvancedParameterExpander,
                serialNumberPolicyEditor,
                customSerialNumberEditor,

                // console tab
                isUsbEnabledEditor,
                consoleDisconnectActionEditor,
                consoleDisconnectActionDelayEditor,
                monitors,
                ssoMethodLabel,
                ssoMethodNone,
                ssoMethodGuestAgent,
                expander,
                spiceProxyEnabledCheckboxWithInfoIcon,
                spiceProxyEditor,

                // rest of the tabs
                initialRunTab.getTabListItem(),
                hostTab.getTabListItem(),
                highAvailabilityTab.getTabListItem(),
                resourceAllocationTab.getTabListItem(),
                customPropertiesTab.getTabListItem(),
                rngDeviceTab.getTabListItem()
        );
    }

    protected List<Widget> managedOnlyWidgets() {
        return Arrays.asList(
                // global
                oSTypeEditor,
                vmTypeEditor,
                detachableInstanceTypesEditor,

                // general tab
                vmIdEditor,
                isStatelessEditor,
                isRunAndPauseEditor,
                isDeleteProtectedEditor,
                isSealedEditor,
                copyTemplatePermissionsEditor,
                logicalNetworksEditorRow,

                // system tab
                biosTypeEditor,
                emulatedMachine,
                generalLabel,
                customCpu,
                customCompatibilityVersionEditor,
                timeZoneEditorWithInfo,
                serialNumberPolicyEditor,
                customSerialNumberEditor,

                // whole tabs
                consoleTab.getTabListItem(),
                affinityTab.getTabListItem(),
                foremanTab.getTabListItem(),
                initialRunTab.getTabListItem(),
                hostTab.getTabListItem(),
                highAvailabilityTab.getTabListItem(),
                resourceAllocationTab.getTabListItem(),
                bootOptionsTab.getTabListItem(),
                rngDeviceTab.getTabListItem(),
                customPropertiesTab.getTabListItem(),
                iconTab.getTabListItem()
                );
    }

    protected void disableAllTabs() {
        allDialogTabs().forEach(DialogTab::disableContent);

        oSTypeEditor.setEnabled(false);
        quotaEditor.setEnabled(false);
        dataCenterWithClusterEditor.setEnabled(false);
        baseTemplateEditor.setEnabled(false);
        templateWithVersionEditor.setEnabled(false);
        vmTypeEditor.setEnabled(false);
        detachableInstanceTypesEditor.setEnabled(false);
        emulatedMachine.setEnabled(false);
        customCpu.setEnabled(false);
    }

    private List<DialogTab> allDialogTabs() {
        return Arrays.asList(
            generalTab,
            poolTab,
            initialRunTab,
            consoleTab,
            hostTab,
            highAvailabilityTab,
            resourceAllocationTab,
            bootOptionsTab,
            customPropertiesTab,
            systemTab,
            rngDeviceTab,
            iconTab,
            foremanTab,
            affinityTab
        );
    }

    protected void updateOrAddToWidgetConfiguration(PopupWidgetConfigMap configuration, List<Widget> widgets, UnaryOperator<PopupWidgetConfig> updater) {
        for (Widget widget: widgets) {
            if (configuration.containsKey(widget)) {
                configuration.update(widget, updater.apply(configuration.get(widget)));
            } else {
                configuration.putOne(widget, updater.apply(simpleField()));
            }
        }
    }

    protected void decorateDetachableFields() {
        for (Widget decoratedWidget : getWidgetConfiguration().getDetachables().keySet()) {
            if (decoratedWidget instanceof HasDetachable) {
                ((HasDetachable) decoratedWidget).setDetachableIconVisible(true);
            }
        }
    }

    public void switchAttachToInstanceType(boolean attached) {
        for (Widget detachable : getWidgetConfiguration().getDetachables().keySet()) {
            if (detachable instanceof HasDetachable) {
                ((HasDetachable) detachable).setAttached(attached);
            }
        }
    }

    public void initCreateInstanceMode() {
        setCreateInstanceMode(true);
        for (Widget adminOnlyField : getWidgetConfiguration().getVisibleForAdminOnly().keySet()) {
            adminOnlyField.setVisible(false);
        }
    }

    public List<HasValidation> getInvalidWidgets() {
        List<HasValidation> hasValidations = new ArrayList<>();
        for (DialogTab dialogTab : allDialogTabs()) {
            hasValidations.addAll(dialogTab.getInvalidWidgets());
        }

        return hasValidations;
    }

    @Override
    public DialogTabPanel getTabPanel() {
        return mainTabPanel;
    }

    @Override
    public Map<TabName, OvirtTabListItem> getTabNameMapping() {
        return tabMap;
    }

    public UiCommandButton getNumaSupportButton() {
        return numaSupportButton;
    }

    public UnitVmModel getModel() {
        return unitVmModel;
    }

    public HasClickHandlers getAddAffinityGroupButton() {
        return affinityGroupSelectionWidget.getSelectionWidget().getAddSelectedItemButton();
    }

    public HasClickHandlers getAddAffinityLabelButton() {
        return affinityLabelSelectionWidget.getSelectionWidget().getAddSelectedItemButton();
    }

    private class MigrateEncryptedRenderer extends BooleanRenderer {
        @Override
        public String render(Boolean vmMigrateEncrypted) {
            if (vmMigrateEncrypted) {
                return constants.encrypt();
            } else {
                return constants.dontEncrypt();
            }
        }
    }

    private String getNullMigrationDowntimeString() {
        return constants.systemDefaultOption() + " (" + AsyncDataProvider.getInstance().getMigrationDowntime() + ")";//$NON-NLS-1$ //$NON-NLS-2$
    }
}
