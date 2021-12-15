package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import java.util.List;

import org.gwtbootstrap3.client.ui.Row;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.FipsMode;
import org.ovirt.engine.core.common.businessentities.LogMaxMemoryUsedThresholdType;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.network.FirewallType;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractTabbedModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.HasEnabledWithHints;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelCheckBoxGroup;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelRadioGroupEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.renderer.BiosTypeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.BooleanRenderer;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.MigrationPolicyNameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.SystemDefaultRenderer;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.MacPoolModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.MigrationPoliciesTranslator;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool.MacPoolWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.client.NumberFormatRenderer;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ClusterPopupView extends AbstractTabbedModelBoundPopupView<ClusterModel> implements ClusterPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ClusterModel, ClusterPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ClusterPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    DialogTabPanel tabPanel;

    @UiField
    WidgetStyle style;

    @UiField
    Row dataCenterRow;

    @UiField
    @WithElementId
    DialogTab generalTab;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId
    ListModelListBoxEditor<StoragePool> dataCenterEditor;

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "comment.entity")
    @WithElementId
    StringEntityModelTextBoxEditor commentEditor;

    @UiField(provided = true)
    @Path(value = "managementNetwork.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Network> managementNetworkEditor;

    @UiField(provided = true)
    @Path(value = "CPU.selectedItem")
    @WithElementId
    ListModelListBoxEditor<ServerCpu> cpuEditor;

    @UiField(provided = true)
    InfoIcon biosTypeEditorInfoIcon;

    BiosTypeRenderer biosTypeRenderer;

    @UiField(provided = true)
    @Path(value = "biosType.selectedItem")
    @WithElementId
    ListModelListBoxOnlyEditor<BiosType> biosTypeEditor;

    @UiField(provided = true)
    @Path(value = "changeToQ35.entity")
    @WithElementId
    EntityModelCheckBoxEditor changeToQ35Editor;

    @UiField(provided = true)
    @Path(value = "version.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Version> versionEditor;

    @UiField(provided = true)
    @Path(value = "switchType.selectedItem")
    @WithElementId
    ListModelListBoxEditor<SwitchType> switchTypeEditor;

    @UiField(provided = true)
    @Path(value = "firewallType.selectedItem")
    @WithElementId
    ListModelListBoxEditor<FirewallType> firewallTypeEditor;

    @UiField(provided = true)
    @Path(value = "defaultNetworkProvider.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Provider> defaultNetworkProviderEditor;

    @UiField(provided = true)
    @Path(value = "logMaxMemoryUsedThresholdType.selectedItem")
    @WithElementId
    ListModelListBoxEditor<LogMaxMemoryUsedThresholdType> logMaxMemoryUsedThresholdTypeEditor;

    @UiField
    @Ignore
    Label logMaxMemoryUsedThresholdLabel;

    @UiField(provided = true)
    @Ignore
    InfoIcon logMaxMemoryUsedThresholdInfoIcon;

    @UiField
    @Path(value = "logMaxMemoryUsedThreshold.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor logMaxMemoryUsedThresholdEditor;

    @UiField(provided = true)
    @Path(value = "architecture.selectedItem")
    @WithElementId
    ListModelListBoxEditor<ArchitectureType> architectureEditor;

    @UiField(provided = true)
    @Path(value = "enableOvirtService.entity")
    @WithElementId("enableOvirtService")
    EntityModelCheckBoxEditor enableOvirtServiceEditor;

    @UiField(provided = true)
    @Path(value = "enableGlusterService.entity")
    @WithElementId("enableGlusterService")
    EntityModelCheckBoxEditor enableGlusterServiceEditor;

    @UiField(provided = true)
    @Path(value = "enableOvirtService.entity")
    @WithElementId("enableOvirtServiceOption")
    EntityModelRadioButtonEditor enableOvirtServiceOptionEditor;

    @UiField(provided = true)
    @Path(value = "enableGlusterService.entity")
    @WithElementId("enableGlusterServiceOption")
    EntityModelRadioButtonEditor enableGlusterServiceOptionEditor;

    @UiField(provided = true)
    @Path(value = "isImportGlusterConfiguration.entity")
    @WithElementId("isImportGlusterConfiguration")
    EntityModelCheckBoxEditor importGlusterConfigurationEditor;

    @UiField
    @Ignore
    Label importGlusterExplanationLabel;

    @UiField
    @Path(value = "glusterHostAddress.entity")
    @WithElementId
    StringEntityModelTextBoxEditor glusterHostAddressEditor;

    @UiField
    @Path(value = "glusterHostSshPublicKey.entity")
    @WithElementId
    StringEntityModelTextAreaLabelEditor glusterHostSshPublicKeyEditor;

    @UiField
    @Path(value = "glusterHostPassword.entity")
    @WithElementId
    StringEntityModelPasswordBoxEditor glusterHostPasswordEditor;

    @UiField
    @Ignore
    Label rngLabel;

    @UiField(provided = true)
    @Path(value = "rngHwrngSourceRequired.entity")
    @WithElementId
    EntityModelCheckBoxEditor rngHwrngSourceRequired;

    @UiField
    @Path(value = "glusterTunedProfile.selectedItem")
    @WithElementId
    ListModelListBoxEditor<String> glusterTunedProfileEditor;

    @UiField(provided = true)
    @Path(value = "additionalClusterFeatures.selectedItem")
    @WithElementId
    ListModelCheckBoxGroup<AdditionalFeature> additionalFeaturesEditor;

    @UiField
    @Ignore
    AdvancedParametersExpander additionalFeaturesExpander;

    @UiField
    @Ignore
    FlowPanel additionalFeaturesExpanderContent;

    @UiField
    @WithElementId
    DialogTab optimizationTab;

    @UiField
    @Ignore
    Label memoryOptimizationPanelTitle;

    @UiField(provided = true)
    InfoIcon memoryOptimizationInfo;

    @UiField(provided = true)
    InfoIcon allowOverbookingInfoIcon;

    @UiField(provided = true)
    @Path(value = "optimizationNone_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationNoneEditor;

    @UiField(provided = true)
    @Path(value = "optimizationForServer_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationForServerEditor;

    @UiField(provided = true)
    @Path(value = "optimizationForDesktop_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationForDesktopEditor;

    @UiField(provided = true)
    @Path(value = "optimizationCustom_IsSelected.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizationCustomEditor;

    @UiField
    @Ignore
    Label cpuThreadsPanelTitle;

    @UiField(provided = true)
    InfoIcon cpuThreadsInfo;

    @UiField(provided = true)
    @Path(value = "countThreadsAsCores.entity")
    @WithElementId
    EntityModelCheckBoxEditor countThreadsAsCoresEditor;

    @UiField
    @Ignore
    Label clusterPopupCpuSmtTitle;

    @UiField(provided = true)
    @Path(value = "smtDisabled.entity")
    EntityModelCheckBoxEditor smtDisabledEditor;

    @UiField
    InfoIcon clusterPopupCpuSmtInfoIcon;

    @UiField
    @WithElementId
    DialogTab migrationTab;

    @UiField
    InfoIcon clusterPopupResiliencePolicyInfoIcon;

    @UiField(provided = true)
    @Path(value = "migrateOnErrorOption_YES.entity")
    @WithElementId
    EntityModelRadioButtonEditor migrateOnErrorOption_YESEditor;

    @UiField(provided = true)
    @Path(value = "migrateOnErrorOption_HA_ONLY.entity")
    @WithElementId
    EntityModelRadioButtonEditor migrateOnErrorOption_HA_ONLYEditor;

    @UiField(provided = true)
    @Path(value = "migrateOnErrorOption_NO.entity")
    @WithElementId
    EntityModelRadioButtonEditor migrateOnErrorOption_NOEditor;

    @UiField(provided = true)
    @Path(value = "migrationBandwidthLimitType.selectedItem")
    @WithElementId
    ListModelListBoxEditor<MigrationBandwidthLimitType> migrationBandwidthLimitTypeEditor;

    @UiField
    InfoIcon migrationBandwidthLimitInfoIcon;

    @UiField
    @Path(value = "customMigrationNetworkBandwidth.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor customMigrationBandwidthLimitEditor;

    @UiField
    @WithElementId
    DialogTab clusterPolicyTab;

    @UiField
    @Ignore
    Label additionPropsPanelTitle;

    @UiField(provided = true)
    @Path(value = "enableTrustedService.entity")
    @WithElementId
    EntityModelCheckBoxEditor enableTrustedServiceEditor;

    @UiField(provided = true)
    @Path(value = "enableHaReservation.entity")
    @WithElementId
    EntityModelCheckBoxEditor enableHaReservationEditor;

    @UiField(provided = true)
    @Path(value = "clusterPolicy.selectedItem")
    @WithElementId
    ListModelListBoxEditor<ClusterPolicy> clusterPolicyEditor;

    @UiField
    @Ignore
    protected KeyValueWidget<KeyValueModel> customPropertiesSheetEditor;

    @UiField(provided = true)
    @Path(value = "enableKsm.entity")
    @WithElementId
    EntityModelCheckBoxEditor enableKsm;

    @UiField(provided = true)
    @Path(value = "enableBallooning.entity")
    @WithElementId
    EntityModelCheckBoxEditor enableBallooning;

    @UiField
    @Ignore
    Label schedulerOptimizationPanelTitle;

    @UiField(provided = true)
    InfoIcon schedulerOptimizationInfoIcon;

    @UiField(provided = true)
    @Path(value = "optimizeForUtilization.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizeForUtilizationEditor;

    @UiField(provided = true)
    @Path(value = "optimizeForSpeed.entity")
    @WithElementId
    EntityModelRadioButtonEditor optimizeForSpeedEditor;

    @UiField
    Row allowOverbookingRow;

    @UiField(provided = true)
    @Path(value = "guarantyResources.entity")
    @WithElementId
    EntityModelRadioButtonEditor guarantyResourcesEditor;

    @UiField(provided = true)
    @Path(value = "allowOverbooking.entity")
    @WithElementId
    EntityModelRadioButtonEditor allowOverbookingEditor;

    @UiField(provided = true)
    @Path(value = "serialNumberPolicy.selectedItem")
    @WithElementId
    ListModelListBoxEditor<SerialNumberPolicy> serialNumberPolicyEditor;

    @UiField
    @Path("customSerialNumber.entity")
    public StringEntityModelTextBoxEditor customSerialNumberEditor;

    @UiField(provided = true)
    @Path(value = "migrateEncrypted.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Boolean> migrateEncryptedEditor;

    @UiField(provided = true)
    @Path("autoConverge.selectedItem")
    @WithElementId("autoConverge")
    ListModelListBoxEditor<Boolean> autoConvergeEditor;

    @UiField(provided = true)
    @Path("migrateCompressed.selectedItem")
    @WithElementId("migrateCompressed")
    ListModelListBoxEditor<Boolean> migrateCompressedEditor;

    @UiField
    @Ignore
    DialogTab consoleTab;

    @UiField(provided = true)
    @Path(value = "migrationPolicies.selectedItem")
    @WithElementId
    ListModelListBoxEditor<MigrationPolicy> migrationPolicyEditor;

    @UiField
    @Ignore
    @WithElementId
    HTML migrationPolicyDetails;

    @UiField
    @Path(value = "spiceProxy.entity")
    @WithElementId
    StringEntityModelTextBoxEditor spiceProxyEditor;

    @Path(value = "spiceProxyEnabled.entity")
    @WithElementId
    @UiField(provided = true)
    EntityModelCheckBoxEditor spiceProxyOverrideEnabled;

    @UiField(provided = true)
    @Path(value = "vncEncryptionEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor vncEncryptionEnabled;

    @UiField(provided = true)
    InfoIcon vncEncryptionEnabledInfoIcon;


    @UiField(provided = true)
    InfoIcon isVirtioScsiEnabledInfoIcon;

    @UiField
    @Ignore
    DialogTab fencingPolicyTab;

    @UiField(provided = true)
    InfoIcon fencingEnabledInfo;

    @UiField(provided = true)
    @Path(value = "fencingEnabledModel.entity")
    @WithElementId
    EntityModelCheckBoxEditor fencingEnabledCheckBox;

    @UiField(provided = true)
    InfoIcon skipFencingIfSDActiveInfo;

    @UiField(provided = true)
    @Path(value = "skipFencingIfSDActiveEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor skipFencingIfSDActiveCheckBox;

    @UiField(provided = true)
    InfoIcon skipFencingIfConnectivityBrokenInfo;

    @UiField(provided = true)
    @Path(value = "skipFencingIfConnectivityBrokenEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor skipFencingIfConnectivityBrokenCheckBox;

    @UiField(provided = true)
    @Path(value = "hostsWithBrokenConnectivityThreshold.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Integer> hostsWithBrokenConnectivityThresholdEditor;

    @UiField(provided = true)
    InfoIcon skipFencingIfGlusterBricksUpInfo;

    @UiField(provided = true)
    @Path(value = "skipFencingIfGlusterBricksUp.entity")
    @WithElementId
    EntityModelCheckBoxEditor skipFencingIfGlusterBricksUpCheckBox;

    @UiField(provided = true)
    InfoIcon skipFencingIfGlusterQuorumNotMetInfo;

    @UiField(provided = true)
    @Path(value = "skipFencingIfGlusterQuorumNotMet.entity")
    @WithElementId
    EntityModelCheckBoxEditor skipFencingIfGlusterQuorumNotMetCheckBox;

    @UiField(provided = true)
    @Path(value = "fipsMode.selectedItem")
    @WithElementId
    ListModelListBoxEditor<FipsMode> fipsModeEditor;

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    @Path(value = "ksmPolicyForNumaSelection.selectedItem")
    public ListModelRadioGroupEditor<ClusterModel.KsmPolicyForNuma> ksmPolicyForNumaEditor;

    @UiField(provided = true)
    @Path(value = "macPoolListModel.selectedItem")
    @WithElementId
    ListModelListBoxEditor<MacPool> macPoolListEditor;

    @UiField
    @Ignore
    @WithElementId
    MacPoolWidget macPoolWidget;

    @UiField
    @WithElementId
    DialogTab macPoolTab;

    @UiField
    UiCommandButton addMacPoolButton;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();
    private static final MigrationPoliciesTranslator translator = MigrationPoliciesTranslator.getInstance();

    @Inject
    public ClusterPopupView(EventBus eventBus) {
        super(eventBus);
        initListBoxEditors();
        initRadioButtonEditors();
        initCheckBoxEditors();
        initInfoIcons();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initAdditionalFeaturesExpander();

        customMigrationBandwidthLimitEditor.hideLabel();
        logMaxMemoryUsedThresholdTypeEditor.hideLabel();
        logMaxMemoryUsedThresholdEditor.hideLabel();
        hostsWithBrokenConnectivityThresholdEditor.hideLabel();

        addStyles();
        driver.initialize(this);
        applyModeCustomizations();
        setVisibilities();
        localizeInfoIcons();

        additionalFeaturesEditor.clearAllSelections();
    }

    private void localizeInfoIcons() {
        migrationBandwidthLimitInfoIcon.setText(SafeHtmlUtils.fromString(constants.migrationBandwidthLimit()));
        clusterPopupResiliencePolicyInfoIcon.setText(
                SafeHtmlUtils.fromString(constants.clusterPopupResiliencePolicyInfo()));
        clusterPopupCpuSmtInfoIcon.setText(
                SafeHtmlUtils.fromString(constants.clusterCpuSmtInfo())
        );
    }

    private void setVisibilities() {
        rngLabel.setVisible(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
    }

    @Override
    protected void populateTabMap() {
        getTabNameMapping().put(TabName.GENERAL_TAB, this.generalTab.getTabListItem());
        getTabNameMapping().put(TabName.CONSOLE_TAB, this.consoleTab.getTabListItem());
        getTabNameMapping().put(TabName.CLUSTER_POLICY_TAB, this.clusterPolicyTab.getTabListItem());
        getTabNameMapping().put(TabName.OPTIMIZATION_TAB, this.optimizationTab.getTabListItem());
        getTabNameMapping().put(TabName.MIGRATION_TAB, this.migrationTab.getTabListItem());
        getTabNameMapping().put(TabName.MAC_POOL_TAB, macPoolTab.getTabListItem());
    }

    private void addStyles() {
        importGlusterConfigurationEditor.addContentWidgetContainerStyleName(style.editorContentWidget());
        migrateOnErrorOption_NOEditor.addContentWidgetContainerStyleName(style.label());
        migrateOnErrorOption_YESEditor.addContentWidgetContainerStyleName(style.label());
        migrateOnErrorOption_HA_ONLYEditor.addContentWidgetContainerStyleName(style.label());

        additionalFeaturesExpanderContent.setStyleName(style.additionalFeaturesExpanderContent());
    }

    private void initRadioButtonEditors() {
        enableOvirtServiceOptionEditor = new EntityModelRadioButtonEditor("service"); //$NON-NLS-1$
        enableGlusterServiceOptionEditor = new EntityModelRadioButtonEditor("service"); //$NON-NLS-1$

        optimizationNoneEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationForServerEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationForDesktopEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        optimizationCustomEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$

        migrateOnErrorOption_YESEditor = new EntityModelRadioButtonEditor("2"); //$NON-NLS-1$
        migrateOnErrorOption_HA_ONLYEditor = new EntityModelRadioButtonEditor("2"); //$NON-NLS-1$
        migrateOnErrorOption_NOEditor = new EntityModelRadioButtonEditor("2"); //$NON-NLS-1$

        optimizeForUtilizationEditor = new EntityModelRadioButtonEditor("3"); //$NON-NLS-1$
        optimizeForSpeedEditor = new EntityModelRadioButtonEditor("3"); //$NON-NLS-1$

        guarantyResourcesEditor = new EntityModelRadioButtonEditor("4"); //$NON-NLS-1$
        allowOverbookingEditor = new EntityModelRadioButtonEditor("4"); //$NON-NLS-1$
    }

    private void initListBoxEditors() {
        dataCenterEditor = new ListModelListBoxEditor<>(new NameRenderer<StoragePool>());

        managementNetworkEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Network>() {
            @Override
            protected String renderNullSafe(Network network) {
                return network.getName();
            }
        });

        cpuEditor = new ListModelListBoxEditor<>(new AbstractRenderer<ServerCpu>() {
            @Override
            public String render(ServerCpu object) {
                return object != null && object.getCpuName().length() > 0
                        ?
                        object.getCpuName()
                        : constants.autoDetect();
            }
        });

        biosTypeRenderer = new BiosTypeRenderer(constants.autoDetect());
        biosTypeEditor = new ListModelListBoxOnlyEditor<>(biosTypeRenderer);

        versionEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Version>() {
            @Override
            public String renderNullSafe(Version object) {
                return object.toString();
            }
        });

        switchTypeEditor = new ListModelListBoxEditor<>(new EnumRenderer<SwitchType>());
        firewallTypeEditor = new ListModelListBoxEditor<>(new EnumRenderer<FirewallType>());
        logMaxMemoryUsedThresholdTypeEditor = new ListModelListBoxEditor<>(new EnumRenderer<LogMaxMemoryUsedThresholdType>());

        defaultNetworkProviderEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Provider>() {
            @Override
            protected String renderNullSafe(Provider provider) {
                return provider.getName();
            }
        });

        architectureEditor = new ListModelListBoxEditor<>(new EnumRenderer<ArchitectureType>() {
            @Override
            public String render(ArchitectureType object) {
                if (object == null || object == ArchitectureType.undefined) {
                    // only localize the 'undefined' enum value
                    return super.render(object);
                } else {
                    // all other (concrete) architectures should be displayed directly
                    return object.toString();
                }
            }
        });

        clusterPolicyEditor = new ListModelListBoxEditor<>(new NameRenderer<ClusterPolicy>());
        hostsWithBrokenConnectivityThresholdEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Integer>() {
            @Override
            public String renderNullSafe(Integer object) {
                if (object == null) {
                    return "";
                }
                NumberFormatRenderer renderer = new NumberFormatRenderer(NumberFormat.getPercentFormat());
                //Since this is a percentage renderer, you need to divide by 100 to get the right values to show.
                return renderer.render(object.doubleValue() / 100);
            }
        });

        serialNumberPolicyEditor = new ListModelListBoxEditor<>(
                new SystemDefaultRenderer<SerialNumberPolicy>(
                        new EnumRenderer<SerialNumberPolicy>(),
                        AsyncDataProvider.getInstance().getSerialNumberPolicy()));

        migrateEncryptedEditor = new ListModelListBoxEditor<>(
                new SystemDefaultRenderer<Boolean>(
                        new BooleanRenderer(constants.encrypt(), constants.dontEncrypt()),
                        AsyncDataProvider.getInstance().getMigrateEncrypted()));

        autoConvergeEditor = new ListModelListBoxEditor<>(
                new SystemDefaultRenderer<Boolean>(
                        new BooleanRenderer(constants.autoConverge(), constants.dontAutoConverge()),
                        AsyncDataProvider.getInstance().getAutoConverge()));

        migrateCompressedEditor = new ListModelListBoxEditor<>(
                new SystemDefaultRenderer<Boolean>(
                        new BooleanRenderer(constants.compress(), constants.dontCompress()),
                        AsyncDataProvider.getInstance().getMigrateCompressed()));

        migrationBandwidthLimitTypeEditor = new ListModelListBoxEditor<>(new EnumRenderer<MigrationBandwidthLimitType>());
        migrationBandwidthLimitTypeEditor.hideLabel();
        migrationPolicyEditor = new ListModelListBoxEditor<>(new MigrationPolicyNameRenderer());
        migrationPolicyEditor.hideLabel();
        macPoolListEditor = new ListModelListBoxEditor<>(new NameRenderer<MacPool>());
        macPoolListEditor.setLabel(constants.clusterPopupMacPoolLabel());
        fipsModeEditor = new ListModelListBoxEditor<>(new EnumRenderer<FipsMode>());
    }

    private void initCheckBoxEditors() {
        changeToQ35Editor = new EntityModelCheckBoxEditor(Align.RIGHT);
        enableOvirtServiceEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        enableGlusterServiceEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        importGlusterConfigurationEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        smtDisabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        countThreadsAsCoresEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        enableTrustedServiceEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        enableHaReservationEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        enableKsm = new EntityModelCheckBoxEditor(Align.RIGHT);
        enableKsm.getContentWidgetContainer().setWidth("350px"); //$NON-NLS-1$

        enableBallooning = new EntityModelCheckBoxEditor(Align.RIGHT);
        enableBallooning.getContentWidgetContainer().setWidth("350px"); //$NON-NLS-1$

        rngHwrngSourceRequired = new EntityModelCheckBoxEditor(Align.RIGHT);

        fencingEnabledCheckBox = new EntityModelCheckBoxEditor(Align.RIGHT);

        skipFencingIfSDActiveCheckBox = new EntityModelCheckBoxEditor(Align.RIGHT);

        skipFencingIfConnectivityBrokenCheckBox = new EntityModelCheckBoxEditor(Align.RIGHT);

        skipFencingIfGlusterBricksUpCheckBox = new EntityModelCheckBoxEditor(Align.RIGHT);
        skipFencingIfGlusterQuorumNotMetCheckBox = new EntityModelCheckBoxEditor(Align.RIGHT);

        spiceProxyOverrideEnabled = new EntityModelCheckBoxEditor(Align.RIGHT);
        vncEncryptionEnabled = new EntityModelCheckBoxEditor(Align.RIGHT);

        additionalFeaturesEditor = new ListModelCheckBoxGroup<>(new AbstractRenderer<AdditionalFeature>() {
            @Override
            public String render(AdditionalFeature feature) {
                return feature.getDescription();
            }
        });
    }

    private void initInfoIcons() {
        biosTypeEditorInfoIcon = new InfoIcon(templates.italicText(constants.clusterPopupBiosTypeInfoIcon()));

        memoryOptimizationInfo = new InfoIcon(templates.italicText(constants.clusterPopupMemoryOptimizationInfo()));

        cpuThreadsInfo = new InfoIcon(templates.italicText(constants.clusterPopupCpuThreadsInfo()));

        schedulerOptimizationInfoIcon = new InfoIcon(SafeHtmlUtils.EMPTY_SAFE_HTML);
        allowOverbookingInfoIcon = new InfoIcon(SafeHtmlUtils.EMPTY_SAFE_HTML);

        fencingEnabledInfo = new InfoIcon(
                templates.italicText(constants.fencingEnabledInfo()));
        skipFencingIfSDActiveInfo = new InfoIcon(
                templates.italicText(constants.skipFencingIfSDActiveInfo()));

        skipFencingIfConnectivityBrokenInfo = new InfoIcon(
                templates.italicText(constants.skipFencingWhenConnectivityBrokenInfo()));

        skipFencingIfGlusterBricksUpInfo = new InfoIcon(
                templates.italicText(constants.skipFencingIfGlusterBricksUpInfo()));
        skipFencingIfGlusterBricksUpInfo.setVisible(false);
        skipFencingIfGlusterQuorumNotMetInfo = new InfoIcon(
                templates.italicText(constants.skipFencingIfGlusterQuorumNotMetInfo()));
        skipFencingIfGlusterQuorumNotMetInfo.setVisible(false);

        isVirtioScsiEnabledInfoIcon = new InfoIcon(templates.italicText("")); //$NON-NLS-1$
        vncEncryptionEnabledInfoIcon = new InfoIcon(templates.italicText(constants.vncEncryptionEnabledHelpMessage()));

        logMaxMemoryUsedThresholdInfoIcon =
                new InfoIcon(templates.italicText(constants.logMaxMemoryUsedThresholdLabelHelpMessage()));
    }

    @Override
    public void setSpiceProxyOverrideExplanation(String explanation) {
        isVirtioScsiEnabledInfoIcon.setText(templates.italicText(explanation));
    }

    private void applyModeCustomizations() {
        if (ApplicationModeHelper.getUiMode() == ApplicationMode.GlusterOnly) {
            optimizationTab.setVisible(false);
            migrationTab.setVisible(false);
            clusterPolicyTab.setVisible(false);
            consoleTab.setVisible(false);
            fencingPolicyTab.setVisible(false);
            dataCenterRow.removeStyleName(style.generalTabTopDecorator());
        }
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final ClusterModel object) {
        driver.edit(object);
        updateMacPool(object.getMacPoolModel());
        customPropertiesSheetEditor.edit(object.getCustomPropertySheet());

        enableOvirtServiceEditor.setVisible(object.getAllowClusterWithVirtGlusterEnabled());
        enableGlusterServiceEditor.setVisible(object.getAllowClusterWithVirtGlusterEnabled());

        enableOvirtServiceOptionEditor.setVisible(!object.getAllowClusterWithVirtGlusterEnabled());
        enableGlusterServiceOptionEditor.setVisible(!object.getAllowClusterWithVirtGlusterEnabled());

        optimizationForServerFormatter(object);
        optimizationForDesktopFormatter(object);
        optimizationCustomFormatter(object);

        object.getOptimizationForServer().getEntityChangedEvent().addListener((ev, sender, args) -> optimizationForServerFormatter(object));

        object.getOptimizationForDesktop().getEntityChangedEvent().addListener((ev, sender, args) -> optimizationForDesktopFormatter(object));

        object.getOptimizationCustom_IsSelected().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (object.getOptimizationCustom_IsSelected().getEntity()) {
                optimizationCustomFormatter(object);
                optimizationCustomEditor.setVisible(true);
            }
        });

        object.getDataCenter().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            migrationTab.setVisible(object.isMigrationTabAvailable());
            applyModeCustomizations();
        });

        object.getEnableOvirtService().getEntityChangedEvent().addListener((ev, sender, args) -> updateGlusterFencingPolicyVisibility(object));

        object.getEnableGlusterService().getEntityChangedEvent().addListener((ev, sender, args) -> {
            importGlusterExplanationLabel.setVisible(object.getEnableGlusterService().getEntity()
                    && object.getIsNew());
            updateGlusterFencingPolicyVisibility(object);
        });
        updateGlusterFencingPolicyVisibility(object);
        importGlusterExplanationLabel.setVisible(object.getEnableGlusterService().getEntity()
                && object.getIsNew());

        schedulerOptimizationInfoIcon.setText(SafeHtmlUtils.fromTrustedString(
                templates.italicText(object.getSchedulerOptimizationInfoMessage()).asString()
                        .replaceAll("(\r\n|\n)", "<br />"))); //$NON-NLS-1$ //$NON-NLS-2$
        allowOverbookingInfoIcon.setText(SafeHtmlUtils.fromTrustedString(
                templates.italicText(object.getAllowOverbookingInfoMessage()).asString()
                        .replaceAll("(\r\n|\n)", "<br />"))); //$NON-NLS-1$ //$NON-NLS-2$
        allowOverbookingRow.setVisible(allowOverbookingEditor.isVisible());

        object.getVersion().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (object.getVersion().getSelectedItem() != null) {
                Version clusterVersion = object.getVersion().getSelectedItem();
                migrationPolicyDetails.setVisible(true);
            }
        });

        object.getAdditionalClusterFeatures().getItemsChangedEvent().addListener((ev, sender, args) -> {
            List<List<AdditionalFeature>> items = (List<List<AdditionalFeature>>) object.getAdditionalClusterFeatures().getItems();
            // Hide the fields if there is no feature to show
            additionalFeaturesExpander.setVisible(!items.get(0).isEmpty());
            additionalFeaturesExpanderContent.setVisible(!items.get(0).isEmpty());
        });

        object.getMigrationPolicies().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            MigrationPolicy selectedPolicy = object.getMigrationPolicies().getSelectedItem();
            if (selectedPolicy != null) {
                migrationPolicyDetails.setHTML(
                        templates.migrationPolicyDetails(translator.getName(selectedPolicy), translator.getDescription(selectedPolicy))
                );
            } else {
                migrationPolicyDetails.setText(""); //$NON-NLS-1$
            }
        });

        object.getArchitecture().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            biosTypeRenderer.setArchitectureType(object.getArchitecture().getSelectedItem());
            object.getBiosType().fireItemsChangedEvent();
        });
    }

    private void updateGlusterFencingPolicyVisibility(ClusterModel object) {
        skipFencingIfGlusterBricksUpInfo
                .setVisible(object.getEnableGlusterService().getEntity() && object.getEnableOvirtService().getEntity());
        skipFencingIfGlusterQuorumNotMetInfo
                .setVisible(object.getEnableGlusterService().getEntity() && object.getEnableOvirtService().getEntity());
    }

    @Override
    public void updateMacPool(MacPoolModel macPoolModel) {
        macPoolWidget.edit(macPoolModel);
    }

    @Override
    public UiCommandButton getMacPoolButton() {
        return addMacPoolButton;
    }

    @Override
    public void makeMacPoolButtonInvisible() {
        addMacPoolButton.setVisible(false);
    }

    private void optimizationForServerFormatter(ClusterModel object) {
        if (object.getOptimizationForServer() != null
                && object.getOptimizationForServer().getEntity() != null) {
            optimizationForServerEditor.setLabel(messages.clusterPopupMemoryOptimizationForServerLabel(
                            object.getOptimizationForServer().getEntity().toString()));
        }
    }

    private void optimizationForDesktopFormatter(ClusterModel object) {
        if (object.getOptimizationForDesktop() != null
                && object.getOptimizationForDesktop().getEntity() != null) {
            optimizationForDesktopEditor.setLabel(messages.clusterPopupMemoryOptimizationForDesktopLabel(
                            object.getOptimizationForDesktop().getEntity().toString()));
        }
    }

    private void optimizationCustomFormatter(ClusterModel object) {
        if (object.getOptimizationCustom() != null
                && object.getOptimizationCustom().getEntity() != null) {
            // Use current value because object.getOptimizationCustom.getEntity() can be null
            optimizationCustomEditor.setLabel(messages.clusterPopupMemoryOptimizationCustomLabel(
                    String.valueOf(object.getMemoryOverCommit())));
        }
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
    }

    @Override
    public ClusterModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void allowClusterWithVirtGlusterEnabled(boolean value) {
        enableOvirtServiceEditor.setVisible(value);
        enableGlusterServiceEditor.setVisible(value);
        enableOvirtServiceOptionEditor.setVisible(!value);
        enableGlusterServiceOptionEditor.setVisible(!value);
    }

    interface WidgetStyle extends CssResource {
        String label();

        String editorContentWidget();

        String timeTextBoxEditorWidget();

        String generalTabTopDecorator();

        String additionalFeaturesExpanderContent();
    }

    @Override
    public DialogTabPanel getTabPanel() {
        return tabPanel;
    }

    private void initAdditionalFeaturesExpander() {
        additionalFeaturesExpander.initWithContent(additionalFeaturesExpanderContent.getElement());
    }

    @Override
    public HasEnabledWithHints getMigrationBandwidthLimitTypeEditor() {
        return migrationBandwidthLimitTypeEditor;
    }

    @Override
    public HasEnabledWithHints getCustomMigrationBandwidthLimitEditor() {
        return customMigrationBandwidthLimitEditor;
    }
}
