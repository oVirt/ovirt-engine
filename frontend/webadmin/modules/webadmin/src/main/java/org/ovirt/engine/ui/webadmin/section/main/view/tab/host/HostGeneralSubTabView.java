package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import static org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel.SUPPORTED_CPUS_PROPERTY_CHANGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.WidgetWithInfo;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.BooleanTextBoxLabel;
import org.ovirt.engine.ui.common.widget.label.EnumTextBoxLabel;
import org.ovirt.engine.ui.common.widget.label.MemorySizeTextBoxLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipWidth;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHardwareGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostGeneralSubTabPresenter;
import org.ovirt.engine.ui.webadmin.widget.alert.InLineAlertWidget;
import org.ovirt.engine.ui.webadmin.widget.alert.InLineAlertWidget.AlertType;
import org.ovirt.engine.ui.webadmin.widget.label.DetailsTextBoxLabel;
import org.ovirt.engine.ui.webadmin.widget.label.FullDateTimeLabel;
import org.ovirt.engine.ui.webadmin.widget.label.NullableNumberValueLabel;
import org.ovirt.engine.ui.webadmin.widget.label.PercentTextBoxLabel;
import org.ovirt.engine.ui.webadmin.widget.label.VersionValueLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HostGeneralSubTabView extends AbstractSubTabFormView<VDS, HostListModel<Void>, HostGeneralModel>
    implements HostGeneralSubTabPresenter.ViewDef, Editor<HostGeneralModel> {

    interface Driver extends UiCommonEditorDriver<HostGeneralModel, HostGeneralSubTabView> {
    }

    interface ViewIdHandler extends ElementIdHandler<HostGeneralSubTabView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    @Path("IScsiInitiatorName")
    StringValueLabel iScsiInitiatorName = new StringValueLabel();
    PercentTextBoxLabel<Integer> sharedMemory = new PercentTextBoxLabel<>();
    BooleanTextBoxLabel memoryPageSharing = new BooleanTextBoxLabel(constants.active(), constants.inactive());
    NullableNumberValueLabel<Integer> activeVms = new NullableNumberValueLabel<>();
    NullableNumberValueLabel<Integer> logicalCores = new NullableNumberValueLabel<>();
    StringValueLabel onlineCores = new StringValueLabel();
    StringValueLabel hostName = new StringValueLabel();
    StringValueLabel spmPriority = new StringValueLabel();
    StringValueLabel hostedEngineHa = new StringValueLabel();
    FullDateTimeLabel bootTime = new FullDateTimeLabel();
    StringValueLabel kdumpStatus = new StringValueLabel();
    StringValueLabel selinuxEnforceMode = new StringValueLabel();
    StringValueLabel clusterCompatibilityVersion = new StringValueLabel();
    StringValueLabel hugePages = new StringValueLabel();
    BooleanTextBoxLabel vncEncryptionEnabled = new BooleanTextBoxLabel(constants.enabled(), constants.disabled());
    BooleanTextBoxLabel fipsEnabled = new BooleanTextBoxLabel(constants.enabled(), constants.disabled());
    BooleanTextBoxLabel ovnConfigured = new BooleanTextBoxLabel(constants.yes(), constants.no());

    MemorySizeTextBoxLabel<Integer> physicalMemory = new MemorySizeTextBoxLabel<>();
    MemorySizeTextBoxLabel<Integer> usedMemory = new MemorySizeTextBoxLabel<>();
    MemorySizeTextBoxLabel<Integer> freeMemory = new MemorySizeTextBoxLabel<>();

    MemorySizeTextBoxLabel<Long> swapTotal = new MemorySizeTextBoxLabel<>();
    MemorySizeTextBoxLabel<Long> usedSwap = new MemorySizeTextBoxLabel<>();
    MemorySizeTextBoxLabel<Long> swapFree = new MemorySizeTextBoxLabel<>();
    MemorySizeTextBoxLabel<java.lang.Float> maxSchedulingMemory = new MemorySizeTextBoxLabel<>();

    BooleanTextBoxLabel hostDevicePassthroughSupport = new BooleanTextBoxLabel(constants.enabled(), constants.disabled());

    // We don't want to pass any of the hardware info to the editor as we will manually update them.
    @Ignore
    StringValueLabel hardwareManufacturer = new StringValueLabel();
    @Ignore
    StringValueLabel hardwareProductName = new StringValueLabel();
    @Ignore
    StringValueLabel hardwareSerialNumber = new StringValueLabel();
    @Ignore
    StringValueLabel hardwareVersion = new StringValueLabel();
    @Ignore
    StringValueLabel hardwareUUID = new StringValueLabel();
    @Ignore
    StringValueLabel hardwareFamily = new StringValueLabel();
    @Ignore
    StringValueLabel cpuType = new StringValueLabel();
    @Ignore
    StringValueLabel cpuModel = new StringValueLabel();
    @Ignore
    NullableNumberValueLabel<Integer> numberOfSockets = new NullableNumberValueLabel<>(constants.unknown());
    @Ignore
    StringValueLabel coresPerSocket = new StringValueLabel();
    @Ignore
    StringValueLabel threadsPerCore = new StringValueLabel();
    @Ignore
    StringValueLabel tscFrequency = new StringValueLabel();

    @Path("OS")
    StringValueLabel oS = new StringValueLabel();
    StringValueLabel osPrettyName = new StringValueLabel();
    StringValueLabel kvmVersion = new StringValueLabel();
    VersionValueLabel libvirtVersion = new VersionValueLabel();
    StringValueLabel spiceVersion = new StringValueLabel();
    StringValueLabel kernelVersion = new StringValueLabel();
    VersionValueLabel glusterVersion = new VersionValueLabel();
    VersionValueLabel vdsmVersion = new VersionValueLabel();
    VersionValueLabel librbdVersion = new VersionValueLabel();
    StringValueLabel kernelFeatures = new StringValueLabel();
    VersionValueLabel ovsVersion = new VersionValueLabel();
    VersionValueLabel nmstateVersion = new VersionValueLabel();

    @Ignore
    DetailsTextBoxLabel<ArrayList<ValueLabel<Integer>>, Integer> physicalMemoryDetails =
            new DetailsTextBoxLabel<>(constants.total(), constants.used(), constants.free());

    @Ignore
    DetailsTextBoxLabel<ArrayList<ValueLabel<Long>>, Long> swapSizeDetails =
            new DetailsTextBoxLabel<>(constants.total(), constants.used(), constants.free());

    @Ignore
    EnumTextBoxLabel<VdsTransparentHugePagesState> automaticLargePage = new EnumTextBoxLabel<>();

    @UiField
    @WithElementId
    GeneralFormPanel generalFormPanel;

    @UiField
    @WithElementId
    GeneralFormPanel hardwareFormPanel;

    @UiField
    @WithElementId
    GeneralFormPanel softwareFormPanel;

    @UiField
    FlowPanel hbaInventory;

    FormBuilder generalFormBuilder;
    FormBuilder hardwareFormBuilder;
    FormBuilder softwareFormBuilder;

    // This is the panel containing the action items label and the
    // potential list of action items, this way we can hide the panel
    // completely (including the label) if there are no action items
    // to present:
    @UiField
    HTMLPanel alertsPanel;

    // This is the list of action items inside the panel, so that we
    // can clear and add elements inside without affecting the panel:
    @UiField
    FlowPanel alertsList;

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<Widget, HostGeneralSubTabView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private final DetailModelProvider<HostListModel<Void>, HostHardwareGeneralModel> hardWareModelProvider;

    @Inject
    public HostGeneralSubTabView(DetailModelProvider<HostListModel<Void>, HostGeneralModel> modelProvider,
            DetailModelProvider<HostListModel<Void>, HostHardwareGeneralModel> hardWareModelProvider) {
        super(modelProvider);
        this.hardWareModelProvider = hardWareModelProvider;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        generateIds();

        boolean virtSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly);

        populateGeneralFormPanel(virtSupported);
        generateHardwareFormPanel();
        generateSoftwareFormPanel();
    }

    private void generateSoftwareFormPanel() {
        boolean virtSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly);
        boolean glusterSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly);

        // Build a form using the FormBuilder
        softwareFormBuilder = new FormBuilder(softwareFormPanel, 1, 15);
        softwareFormBuilder.setRelativeColumnWidth(0, 12);
        softwareFormBuilder.addFormItem(new FormItem(constants.osVersionHostGeneral(), oS, 0).withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.osPrettyName(), osPrettyName, 0).withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.kernelVersionHostGeneral(), kernelVersion,
                0).withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.kvmVersionHostGeneral(), kvmVersion, 0,
                virtSupported).withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.libvirtVersionHostGeneral(), libvirtVersion, 0,
                virtSupported).withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.vdsmVersionHostGeneral(), vdsmVersion,
                0).withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.spiceVersionHostGeneral(), spiceVersion, 0,
                virtSupported).withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.glusterVersionHostGeneral(), glusterVersion, 0,
                glusterSupported).withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.cephVersionHostGeneral(), librbdVersion, 0,
                virtSupported).withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.ovsVersionGeneral(), ovsVersion, 0).withAutoPlacement(),
                2,
                10);
        softwareFormBuilder.addFormItem(new FormItem(constants.nmstateVersionGeneral(), nmstateVersion, 0).withAutoPlacement(),
                2,
                10);
        softwareFormBuilder.addFormItem(new FormItem(constants.kernelFeatures(), kernelFeatures, 0, true)
                .withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.vncEncryptionLabel(), vncEncryptionEnabled, 0).withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.fipsEnabledLabel(), fipsEnabled, 0).withAutoPlacement(), 2, 10);
        softwareFormBuilder.addFormItem(new FormItem(constants.ovnConfiguredLabel(), ovnConfigured, 0).withAutoPlacement(), 2, 10);
    }

    private void generateHardwareFormPanel() {
        // Build a form using the FormBuilder
        hardwareFormBuilder = new FormBuilder(hardwareFormPanel, 3, 4);
        hardwareFormBuilder.setRelativeColumnWidth(0, 4);
        hardwareFormBuilder.setRelativeColumnWidth(1, 5);
        hardwareFormBuilder.setRelativeColumnWidth(2, 3);
        hardwareFormBuilder.addFormItem(new FormItem(constants.hardwareManufacturerGeneral(), hardwareManufacturer, 0, 0), 5, 7);
        hardwareFormBuilder.addFormItem(new FormItem(constants.hardwareVersionGeneral(), hardwareVersion, 1, 0), 5, 7);
        hardwareFormBuilder.addFormItem(new FormItem(constants.cpuModelHostGeneral(), cpuModel, 2, 0), 5, 7);
        hardwareFormBuilder.addFormItem(new FormItem(constants.numOfCoresPerSocketHostGeneral(), coresPerSocket, 3, 0), 5, 7);

        hardwareFormBuilder.addFormItem(new FormItem(constants.hardwareFamilyGeneral(), hardwareFamily, 0, 1), 4, 8);
        hardwareFormBuilder.addFormItem(new FormItem(constants.hardwareUUIDGeneral(), hardwareUUID, 1, 1), 4, 8);
        hardwareFormBuilder.addFormItem(new FormItem(constants.cpuTypeHostGeneral(), createCpuType(), 2, 1), 4, 8);
        hardwareFormBuilder.addFormItem(new FormItem(constants.numOfThreadsPerCoreHostGeneral(), threadsPerCore, 3, 1), 4, 8);

        hardwareFormBuilder.addFormItem(new FormItem(constants.hardwareProductNameGeneral(), hardwareProductName, 0, 2), 4, 8);
        hardwareFormBuilder.addFormItem(new FormItem(constants.hardwareSerialNumberGeneral(), hardwareSerialNumber, 1, 2), 4, 8);
        hardwareFormBuilder.addFormItem(new FormItem(constants.numOfSocketsHostGeneral(), numberOfSockets, 2, 2), 4, 8);
        hardwareFormBuilder.addFormItem(new FormItem(constants.tscFrequency(), tscFrequency, 3, 2), 4, 8);
    }

    private void populateGeneralFormPanel(boolean virtSupported) {
        // Build a form using the FormBuilder
        generalFormBuilder = new FormBuilder(generalFormPanel, 3, 7);
        generalFormBuilder.setRelativeColumnWidth(0, 3);
        generalFormBuilder.setRelativeColumnWidth(1, 4);
        generalFormBuilder.setRelativeColumnWidth(2, 5);

        generalFormBuilder.addFormItem(new FormItem(constants.ipHost(), hostName,  0, 0).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.spmPriority(), spmPriority, 0, virtSupported).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.activeVmsHostGeneral(), activeVms, 0, virtSupported).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.logicalCores(), logicalCores, 0).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.onlineCores(), onlineCores, 0).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.bootTimeHostGeneral(), bootTime, 0).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.hostedEngineHaHostGeneral(), hostedEngineHa, 0,
                virtSupported).withAutoPlacement());

        generalFormBuilder.addFormItem(new FormItem(constants.isciInitNameHostGeneral(), iScsiInitiatorName, 0, 1,
                virtSupported).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.kdumpStatus(), kdumpStatus, 1).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.physMemHostGeneral(), physicalMemoryDetails, 1).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.swapSizeHostGeneral(), swapSizeDetails, 1).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.sharedMemHostGeneral(), sharedMemory, 1).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.hostDevicePassthroughHostGeneral(), hostDevicePassthroughSupport, 1).withAutoPlacement());

        generalFormBuilder.addFormItem(new FormItem(constants.maxSchedulingMemory(), maxSchedulingMemory, 0, 2, virtSupported).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.memPageSharingHostGeneral(), memoryPageSharing, 2).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.autoLargePagesHostGeneral(), automaticLargePage, 2).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.hostHugePages(), hugePages, 2).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.selinuxModeGeneral(), selinuxEnforceMode, 2).withAutoPlacement());
        generalFormBuilder.addFormItem(new FormItem(constants.clusterCompatibilityVersion(), clusterCompatibilityVersion, 2).withAutoPlacement());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainSelectedItem(VDS selectedItem) {
        driver.edit(getDetailModel());

        automaticLargePage.setValue((VdsTransparentHugePagesState) getDetailModel().getAutomaticLargePage());

        ArrayList<ValueLabel<Integer>> physicalMemoryDetailsArray =
                new ArrayList<>(Arrays.<ValueLabel<Integer>>asList(physicalMemory, usedMemory, freeMemory));

        ArrayList<ValueLabel<Long>> swapSizeDetailsArray =
                new ArrayList<>(Arrays.<ValueLabel<Long>>asList(swapTotal, usedSwap, swapFree));

        physicalMemoryDetails.setValue(physicalMemoryDetailsArray);
        swapSizeDetails.setValue(swapSizeDetailsArray);

        maxSchedulingMemory.setValue(selectedItem.getMaxSchedulingMemory());

        generalFormBuilder.update(getDetailModel());

        refreshHBADeviceInfo(selectedItem);

        updateHardwareFormPanel(selectedItem);
    }

    private void updateSoftwareFormPanel(VDS selectedItem) {
        HostGeneralModel model = getDetailModel();
        oS.setValue(model.getOS());
        osPrettyName.setValue(model.getOsPrettyName());
        kvmVersion.setValue(model.getKvmVersion());
        libvirtVersion.setValue(model.getLibvirtVersion());
        spiceVersion.setValue(model.getSpiceVersion());
        kernelVersion.setValue(model.getKernelVersion());
        glusterVersion.setValue(model.getGlusterVersion());
        vdsmVersion.setValue(model.getVdsmVersion());
        librbdVersion.setValue(model.getLibrbdVersion());
        ovsVersion.setValue(model.getOvsVersion());
        nmstateVersion.setValue(model.getNmstateVersion());
    }

    private void updateHardwareFormPanel(VDS selectedItem) {
        // Populate the model
        HostHardwareGeneralModel model = hardWareModelProvider.getModel();
        model.setEntity(selectedItem);

        hardwareManufacturer.setValue(model.getHardwareManufacturer());
        hardwareVersion.setValue(model.getHardwareVersion());
        hardwareProductName.setValue(model.getHardwareProductName());
        hardwareSerialNumber.setValue(model.getHardwareSerialNumber());
        hardwareUUID.setValue(model.getHardwareUUID());
        hardwareFamily.setValue(model.getHardwareFamily());
        cpuType.setValue(model.getCpuType());
        cpuModel.setValue(model.getCpuModel());
        numberOfSockets.setValue(model.getNumberOfSockets());
        coresPerSocket.setValue(model.getCoresPerSocket());
        threadsPerCore.setValue(model.getThreadsPerCore());
        String tscLabel = model.getTscFrequency() +
                " (" +                                                                                 // $NON-NLS-1$
                (model.isTscScalingEnabled() ? constants.tscScalingOn() : constants.tscScalingOff()) +
                ") ";                                                                                  // $NON-NLS-1$
        tscFrequency.setValue(tscLabel);
    }

    @Override
    public void clearAlerts() {
        // Remove all the alert widgets and make the panel invisible:
        alertsList.clear();
        alertsPanel.setVisible(false);
    }

    @Override
    public void addAlert(Widget alertWidget) {
        addAlert(alertWidget, AlertType.ALERT);
    }

    @Override
    public void addAlert(Widget alertWidget, AlertType type) {
        // Add the composite panel to the alerts panel:
        alertsList.add(new InLineAlertWidget(alertWidget, type));

        // Make the panel visible if it wasn't:
        if (!alertsPanel.isVisible()) {
            alertsPanel.setVisible(true);
        }
    }

    private void refreshHBADeviceInfo(VDS selectedItem) {
        /* refresh all the information about Host Bus Adapter (FC, iSCSI) devices */
        hbaInventory.clear();

        if (selectedItem != null && hardWareModelProvider.getModel().getHbaDevices() != null) {

            /*
             * traverse the model and get all the HBAs
             */
            for (EnumMap<HostHardwareGeneralModel.HbaDeviceKeys, String> hbaDevice : hardWareModelProvider.getModel().getHbaDevices()) {
                GeneralFormPanel hbaFormPanel = new GeneralFormPanel() {
                    {
                        getElement().getStyle().setFloat(Float.LEFT);
                        getElement().getStyle().setBorderWidth(1, Unit.PX);
                        getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
                        getElement().getStyle().setBorderColor("black"); //$NON-NLS-1$
                        getElement().getStyle().setMarginLeft(5, Unit.PX);
                        getElement().getStyle().setMarginBottom(5, Unit.PX);
                        getElement().getStyle().setProperty("width", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                };

                StringValueLabel interfaceName = new StringValueLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.MODEL_NAME));
                StringValueLabel interfaceType = new StringValueLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.TYPE));
                StringValueLabel interfaceWWNN = new StringValueLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.WWNN));
                StringValueLabel portWWPNs = new StringValueLabel(
                        hbaDevice.get(HostHardwareGeneralModel.HbaDeviceKeys.WWNPS));

                FormBuilder hbaFormBuilder = new FormBuilder(hbaFormPanel, 1, 4);
                hbaFormBuilder.setRelativeColumnWidth(0, 12);
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaModelName(), interfaceName, 0, 0));
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaDeviceType(), interfaceType, 1, 0));
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaWWNN(), interfaceWWNN, 2, 0));
                hbaFormBuilder.addFormItem(new FormItem(constants.hbaWWPNs(), portWWPNs, 3, 0));
                hbaInventory.add(hbaFormPanel);
            }
        }
    }

    private Widget createCpuType() {
        cpuType.getElement().getStyle().setWidth(90, Unit.PCT);
        cpuType.getElement().getStyle().setPaddingRight(5, Unit.PX);

        WidgetWithInfo cpuTypeWithInfo = new WidgetWithInfo(cpuType);
        updateCpuTypeInfo(cpuTypeWithInfo, getDetailModel().getSupportedCpus());

        getDetailModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args instanceof PropertyChangedEventArgs) {
                String key = ((PropertyChangedEventArgs) args).propertyName;
                if (key.equals(SUPPORTED_CPUS_PROPERTY_CHANGE)) {
                    updateCpuTypeInfo(cpuTypeWithInfo, getDetailModel().getSupportedCpus());
                }
            }
        });
        return cpuTypeWithInfo;
    }

    private void updateCpuTypeInfo(WidgetWithInfo widgetWithInfo, List<String> supportedCpus) {
        if (supportedCpus == null || supportedCpus.isEmpty()) {
            widgetWithInfo.setIconVisible(false);
            return;
        }

        List<String> otherCpus = new ArrayList<>(supportedCpus);
        otherCpus.remove(0);

        String tooltip;
        if (otherCpus.isEmpty()) {
            tooltip = constants.noSupportedCpusInfo();
            widgetWithInfo.setIconTooltipMaxWidth(TooltipWidth.W220);
        } else {
            tooltip = constants.supportedCpusInfo();
            String listItems = otherCpus.stream().map(cpu -> templates.listItem(cpu).asString()).collect(Collectors.joining());
            tooltip = templates.unorderedListWithTitle(tooltip, SafeHtmlUtils.fromTrustedString(listItems)).asString();
            widgetWithInfo.setIconTooltipMaxWidth(TooltipWidth.W520);
        }

        widgetWithInfo.setIconTooltipText(SafeHtmlUtils.fromTrustedString(tooltip));
        widgetWithInfo.setIconVisible(true);
    }
}
