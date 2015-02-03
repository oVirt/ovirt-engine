package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.ExternalEntityBase;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractTabbedModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.HasLabel;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.provider.HostNetworkProviderWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class HostPopupView extends AbstractTabbedModelBoundPopupView<HostModel> implements HostPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostModel, HostPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostPopupView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HostPopupView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    DialogTabPanel tabPanel;

    @UiField
    Style style;

    @UiField
    @WithElementId
    DialogTab generalTab;

    @UiField
    @WithElementId
    DialogTab powerManagementTab;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId("dataCenter")
    ListModelListBoxEditor<StoragePool> dataCenterEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    @WithElementId("cluster")
    ListModelListBoxEditor<VDSGroup> clusterEditor;

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "userName.entity")
    @WithElementId("userName")
    StringEntityModelTextBoxEditor userNameEditor;

    @UiField
    @Path(value = "fetchSshFingerprint.entity")
    @WithElementId("fetchSshFingerprint")
    StringEntityModelTextBoxEditor fetchSshFingerprint;

    @UiField
    @Ignore
    @WithElementId("fetchResult")
    Label fetchResult;

    @UiField
    @Path(value = "comment.entity")
    @WithElementId("comment")
    StringEntityModelTextBoxEditor commentEditor;

    @UiField
    @Path(value = "providerSearchFilter.entity")
    @WithElementId("providerSearchFilter")
    StringEntityModelTextBoxEditor providerSearchFilterEditor;

    @UiField(provided = true)
    @Path(value = "externalHostName.selectedItem")
    @WithElementId("externalHostName")
    ListModelListBoxEditor<VDS> externalHostNameEditor;

    @UiField(provided = true)
    @Path(value = "providers.selectedItem")
    @WithElementId("providers")
    ListModelListBoxEditor<Provider> providersEditor;

    @UiField(provided = true)
    @Path(value = "externalDiscoveredHosts.selectedItem")
    @WithElementId("externalDiscoveredHosts")
    ListModelTypeAheadListBoxEditor<ExternalEntityBase> externalDiscoveredHostsEditor;

    @UiField(provided = true)
    @Path(value = "externalHostGroups.selectedItem")
    @WithElementId("externalHostGroups")
    ListModelTypeAheadListBoxEditor<ExternalEntityBase> externalHostGroupsEditor;

    @UiField(provided = true)
    @Path(value = "externalComputeResource.selectedItem")
    @WithElementId("externalComputeResource")
    ListModelTypeAheadListBoxEditor<ExternalEntityBase> externalComputeResourceEditor;

    @UiField
    @Path(value = "host.entity")
    @WithElementId("host")
    StringEntityModelTextBoxEditor hostAddressEditor;

    @UiField
    @Path(value = "authSshPort.entity")
    @WithElementId("authSshPort")
    IntegerEntityModelTextBoxEditor authSshPortEditor;

    @UiField
    @Path(value = "userPassword.entity")
    @WithElementId("userPassword")
    StringEntityModelPasswordBoxEditor passwordEditor;

    @UiField(provided = true)
    @Path(value = "publicKey.entity")
    @WithElementId("publicKey")
    StringEntityModelTextAreaLabelEditor publicKeyEditor;

    @UiField
    @Path(value = "overrideIpTables.entity")
    @WithElementId("overrideIpTables")
    EntityModelCheckBoxEditor overrideIpTablesEditor;

    @UiField
    @Path(value = "protocol.entity")
    @WithElementId("protocol")
    EntityModelCheckBoxEditor protocolEditor;

    @UiField(provided = true)
    @Path(value = "isPm.entity")
    @WithElementId("isPm")
    EntityModelCheckBoxEditor pmEnabledEditor;

    @UiField(provided = true)
    @Path(value = "pmVariants.selectedItem")
    @WithElementId("pmVariants")
    ListModelListBoxOnlyEditor<String> pmVariantsEditor;

    @UiField
    @Path(value = "pmSecondaryConcurrent.entity")
    @WithElementId("pmSecondaryConcurrent")
    EntityModelCheckBoxEditor pmSecondaryConcurrentEditor;

    @UiField
    FlowPanel pmPrimaryPanel;

    @UiField
    @Path(value = "managementIp.entity")
    @WithElementId("managementIp")
    StringEntityModelTextBoxEditor pmAddressEditor;

    @UiField
    @Path(value = "pmUserName.entity")
    @WithElementId("pmUserName")
    StringEntityModelTextBoxEditor pmUserNameEditor;

    @UiField
    @Path(value = "pmPassword.entity")
    @WithElementId("pmPassword")
    StringEntityModelPasswordBoxEditor pmPasswordEditor;

    @UiField(provided = true)
    @Path(value = "pmType.selectedItem")
    @WithElementId("pmType")
    ListModelListBoxEditor<String> pmTypeEditor;

    @UiField
    @Path(value = "pmPort.entity")
    @WithElementId("pmPort")
    StringEntityModelTextBoxEditor pmPortEditor;

    @UiField
    @Path(value = "pmSlot.entity")
    @WithElementId("pmSlot")
    StringEntityModelTextBoxEditor pmSlotEditor;

    @UiField
    @Path(value = "pmOptions.entity")
    @WithElementId("pmOptions")
    StringEntityModelTextBoxEditor pmOptionsEditor;

    @UiField
    @Ignore
    Label pmOptionsExplanationLabel;

    @UiField
    @Path(value = "pmSecure.entity")
    @WithElementId("pmSecure")
    EntityModelCheckBoxEditor pmSecureEditor;

    @UiField(provided = true)
    @Path(value = "externalHostProviderEnabled.entity")
    @WithElementId("externalHostProviderEnabled")
    EntityModelCheckBoxEditor externalHostProviderEnabledEditor;

    @UiField
    FlowPanel pmSecondaryPanel;

    @UiField
    @Path(value = "pmSecondaryIp.entity")
    @WithElementId("pmSecondaryIp")
    StringEntityModelTextBoxEditor pmSecondaryAddressEditor;

    @UiField
    @Path(value = "pmSecondaryUserName.entity")
    @WithElementId("pmSecondaryUserName")
    StringEntityModelTextBoxEditor pmSecondaryUserNameEditor;

    @UiField
    @Path(value = "pmSecondaryPassword.entity")
    @WithElementId("pmSecondaryPassword")
    StringEntityModelPasswordBoxEditor pmSecondaryPasswordEditor;

    @UiField(provided = true)
    @Path(value = "pmSecondaryType.selectedItem")
    @WithElementId("pmSecondaryType")
    ListModelListBoxEditor<String> pmSecondaryTypeEditor;

    @UiField
    @Path(value = "pmSecondaryPort.entity")
    @WithElementId("pmSecondaryPort")
    StringEntityModelTextBoxEditor pmSecondaryPortEditor;

    @UiField
    @Path(value = "pmSecondarySlot.entity")
    @WithElementId("pmSecondarySlot")
    StringEntityModelTextBoxEditor pmSecondarySlotEditor;

    @UiField
    @Path(value = "pmSecondaryOptions.entity")
    @WithElementId("pmSecondaryOptions")
    StringEntityModelTextBoxEditor pmSecondaryOptionsEditor;

    @UiField
    @Ignore
    Label pmSecondaryOptionsExplanationLabel;

    @UiField
    @Path(value = "pmSecondarySecure.entity")
    @WithElementId("pmSecondarySecure")
    EntityModelCheckBoxEditor pmSecondarySecureEditor;

    @UiField
    @Path(value = "disableAutomaticPowerManagement.entity")
    @WithElementId("disableAutomaticPowerManagementEditor")
    EntityModelCheckBoxEditor disableAutomaticPowerManagementEditor;

    @UiField
    @Path(value = "pmKdumpDetection.entity")
    @WithElementId("pmKdumpDetection")
    EntityModelCheckBoxEditor pmKdumpDetectionEditor;

    @UiField
    UiCommandButton testButton;

    @UiField
    UiCommandButton upButton;

    @UiField
    UiCommandButton downButton;

    @UiField
    @Ignore
    SimplePanel fetchPanel;

    @UiField
    Image updateHostsButton;

    @UiField
    @Ignore
    Label testMessage;

    @UiField
    @Ignore
    Label sourceLabel;

    @UiField
    ListBox proxyListBox;

    @UiField
    @Ignore
    DialogTab spmTab;

    @UiField
    @Ignore
    DialogTab consoleTab;

    @UiField
    @Ignore
    DialogTab networkProviderTab;

    @UiField
    @Ignore
    VerticalPanel spmPanel;

    @UiField
    @Path(value = "pkSection.entity")
    @WithElementId("pkSection")
    HorizontalPanel pkSection;

    @UiField
    @Path(value = "passwordSection.entity")
    @WithElementId("passwordSection")
    HorizontalPanel passwordSection;

    @UiField
    @Path(value = "provisionedHostSection.entity")
    @WithElementId
    HorizontalPanel provisionedHostSection;

    @UiField
    @Path(value = "discoveredHostSection.entity")
    @WithElementId
    HorizontalPanel discoveredHostSection;

    @UiField(provided = true)
    @Ignore
    @WithElementId("rbPublicKey")
    public RadioButton rbPublicKey;

    @UiField(provided = true)
    @Ignore
    @WithElementId("rbPassword")
    public RadioButton rbPassword;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    public RadioButton rbProvisionedHost;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    public RadioButton rbDiscoveredHost;

    @UiField
    @Ignore
    Label authLabel;

    @UiField
    @Ignore
    Label fingerprintLabel;

    @UiField(provided = true)
    @Ignore
    InfoIcon consoleAddressInfoIcon;

    @UiField(provided = true)
    @Ignore
    InfoIcon discoveredHostInfoIcon;

    @UiField(provided = true)
    @Ignore
    InfoIcon provisionedHostInfoIcon;

    @UiField
    @Ignore
    Label consoleAddressLabel;

    @UiField
    @Path(value = "consoleAddress.entity")
    @WithElementId
    StringEntityModelTextBoxEditor consoleAddress;

    @UiField
    @Path(value = "providerSearchFilterLabel.entity")
    StringEntityModelTextBoxEditor providerSearchFilterLabel;

    @UiField
    @Path(value = "consoleAddressEnabled.entity")
    EntityModelCheckBoxEditor consoleAddressEnabled;

    @UiField(provided = true)
    InfoIcon providerSearchInfoIcon;

    @UiField
    @Ignore
    HostNetworkProviderWidget networkProviderWidget;

    @UiField
    @Ignore
    AdvancedParametersExpander expander;

    @UiField
    @Ignore
    FlowPanel expanderContent;

    @UiField
    FlowPanel searchProviderPanel;

    @UiField
    FlowPanel discoveredHostsPanel;

    private final Driver driver = GWT.create(Driver.class);

    private final CommonApplicationTemplates applicationTemplates;

    private final ApplicationResources resources;
    private final ApplicationConstants constants;

    private final ApplicationConstants appConstants;

    @Inject
    public HostPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            CommonApplicationTemplates applicationTemplates) {
        super(eventBus, resources);

        // Inject a reference to the messages:
        appConstants = constants;
        this.resources = resources;
        this.constants = constants;
        this.applicationTemplates = applicationTemplates;
        initEditors();
        initInfoIcon(constants);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initExpander();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        addStyles();
        driver.initialize(this);
        applyModeCustomizations();
    }

    private void initInfoIcon(ApplicationConstants constants) {
        consoleAddressInfoIcon =
                new InfoIcon(applicationTemplates.italicText(constants.enableConsoleAddressOverrideHelpMessage()),
                        resources);
        providerSearchInfoIcon =
                new InfoIcon(applicationTemplates.italicText(constants.providerSearchInfo()), resources);
        provisionedHostInfoIcon =
                new InfoIcon(applicationTemplates.italicText(constants.provisionedHostInfo()), resources);
        discoveredHostInfoIcon =
                new InfoIcon(applicationTemplates.italicText(constants.discoveredHostInfoIcon()), resources);
    }

    private void addStyles() {
        overrideIpTablesEditor.addContentWidgetContainerStyleName(style.overrideIpStyle());
        protocolEditor.addContentWidgetContainerStyleName(style.protocolStyle());
        externalHostProviderEnabledEditor.addContentWidgetContainerStyleName(style.externalHostProviderEnabledEditorContent());
        providerSearchFilterEditor.addContentWidgetContainerStyleName(style.searchFilter());
        providerSearchFilterEditor.setStyleName(style.searchFilterLabel());
        providerSearchFilterEditor.setLabelStyleName(style.emptyEditor());
        providerSearchFilterLabel.addContentWidgetContainerStyleName(style.emptyEditor());
        providerSearchFilterLabel.setStyleName(style.searchFilterLabel());
        fetchSshFingerprint.addContentWidgetContainerStyleName(style.fingerprintEditor());
        expanderContent.setStyleName(style.expanderContent());
        publicKeyEditor.setCustomStyle(style.pkStyle());
        tabPanel.addBarStyle(style.bar());
    }

    private void initEditors() {
        publicKeyEditor = new StringEntityModelTextAreaLabelEditor();

        // List boxes
        dataCenterEditor = new ListModelListBoxEditor<StoragePool>(new NullSafeRenderer<StoragePool>() {
            @Override
            public String renderNullSafe(StoragePool storagePool) {
                return storagePool.getName();
            }
        });

        clusterEditor = new ListModelListBoxEditor<VDSGroup>(new NullSafeRenderer<VDSGroup>() {
            @Override
            public String renderNullSafe(VDSGroup vdsGroup) {
                return vdsGroup.getName();
            }
        });

        externalHostNameEditor = new ListModelListBoxEditor<VDS>(new NullSafeRenderer<VDS>() {
            @Override
            public String renderNullSafe(VDS vds) {
                return vds.getName();
            }
        });

        providersEditor = new ListModelListBoxEditor<Provider>(new NullSafeRenderer<Provider>() {
            @Override
            public String renderNullSafe(Provider provider) {
                return provider.getName();
            }
        });

        pmVariantsEditor = new ListModelListBoxOnlyEditor<String>(new StringRenderer<String>());

        pmTypeEditor = new ListModelListBoxEditor<String>(new StringRenderer<String>());

        pmSecondaryTypeEditor = new ListModelListBoxEditor<String>(new StringRenderer<String>());

        externalDiscoveredHostsEditor = getListModelTypeAheadListBoxEditor();
        externalHostGroupsEditor = getListModelTypeAheadListBoxEditor();
        externalComputeResourceEditor = getListModelTypeAheadListBoxEditor();

        // Check boxes
        pmEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        externalHostProviderEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        rbPassword = new RadioButton("1"); //$NON-NLS-1$
        rbPublicKey = new RadioButton("1"); //$NON-NLS-1$
        rbDiscoveredHost = new RadioButton("2"); //$NON-NLS-1$
        rbProvisionedHost = new RadioButton("2"); //$NON-NLS-1$
    }

    private ListModelTypeAheadListBoxEditor<ExternalEntityBase> getListModelTypeAheadListBoxEditor() {
        return new ListModelTypeAheadListBoxEditor<ExternalEntityBase>(
                new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<ExternalEntityBase>() {
                    @Override
                    public String getReplacementStringNullSafe(ExternalEntityBase data) {
                        return data.getName();
                    }

                    @Override
                    public String getDisplayStringNullSafe(ExternalEntityBase data) {
                        return typeAheadNameDescriptionTemplateNullSafe(
                                data.getName(),
                                data.getDescription()
                        );
                    }
                });
    }

    private String typeAheadNameDescriptionTemplateNullSafe(String name, String description) {
        return applicationTemplates.typeAheadNameDescription(
                name != null ? name : constants.empty(),
                description != null ? description : constants.empty())
                .asString();
    }

    void localize(ApplicationConstants constants) {
        // General tab
        generalTab.setLabel(constants.hostPopupGeneralTabLabel());
        dataCenterEditor.setLabel(constants.hostPopupDataCenterLabel());
        clusterEditor.setLabel(constants.hostPopupClusterLabel());
        nameEditor.setLabel(constants.hostPopupNameLabel());
        userNameEditor.setLabel(constants.hostPopupUsernameLabel());
        commentEditor.setLabel(constants.commentLabel());
        hostAddressEditor.setLabel(constants.hostPopupHostAddressLabel());
        authSshPortEditor.setLabel(constants.hostPopupPortLabel());
        authLabel.setText(constants.hostPopupAuthLabel());
        rbPassword.setText(constants.hostPopupPasswordLabel());
        rbPublicKey.setText(constants.hostPopupPublicKeyLable());
        rbProvisionedHost.setText(constants.provisionedHostsLabel());
        rbDiscoveredHost.setText(constants. discoveredHostsLabel());
        fingerprintLabel.setText(constants.hostPopupHostFingerprintLabel());
        overrideIpTablesEditor.setLabel(constants.hostPopupOverrideIpTablesLabel());
        protocolEditor.setLabel(constants.hostPopupProtocolLabel());
        externalHostProviderEnabledEditor.setLabel(constants.hostPopupEnableExternalHostProvider());
        externalHostNameEditor.setLabel(constants.hostPopupExternalHostName());
        providerSearchFilterLabel.setLabel(constants.hostPopupProviderSearchFilter());
        publicKeyEditor.setTitle(constants.publicKeyUsage());

        // Power Management tab
        powerManagementTab.setLabel(constants.hostPopupPowerManagementTabLabel());
        pmEnabledEditor.setLabel(constants.hostPopupPmEnabledLabel());
        pmSecondaryConcurrentEditor.setLabel(constants.hostPopupPmConcurrent());
        testButton.setLabel(constants.hostPopupTestButtonLabel());
        upButton.setLabel(constants.hostPopupUpButtonLabel());
        downButton.setLabel(constants.hostPopupDownButtonLabel());
        sourceLabel.setText(constants.hostPopupSourceText());

        // Primary
        pmAddressEditor.setLabel(constants.hostPopupPmAddressLabel());
        pmUserNameEditor.setLabel(constants.hostPopupPmUserNameLabel());
        pmPasswordEditor.setLabel(constants.hostPopupPmPasswordLabel());
        pmTypeEditor.setLabel(constants.hostPopupPmTypeLabel());
        pmPortEditor.setLabel(constants.hostPopupPmPortLabel());
        pmSlotEditor.setLabel(constants.hostPopupPmSlotLabel());
        pmOptionsEditor.setLabel(constants.hostPopupPmOptionsLabel());
        pmOptionsExplanationLabel.setText(constants.hostPopupPmOptionsExplanationLabel());
        pmSecureEditor.setLabel(constants.hostPopupPmSecureLabel());

        // Secondary
        pmSecondaryAddressEditor.setLabel(constants.hostPopupPmAddressLabel());
        pmSecondaryUserNameEditor.setLabel(constants.hostPopupPmUserNameLabel());
        pmSecondaryPasswordEditor.setLabel(constants.hostPopupPmPasswordLabel());
        pmSecondaryTypeEditor.setLabel(constants.hostPopupPmTypeLabel());
        pmSecondaryPortEditor.setLabel(constants.hostPopupPmPortLabel());
        pmSecondarySlotEditor.setLabel(constants.hostPopupPmSlotLabel());
        pmSecondaryOptionsEditor.setLabel(constants.hostPopupPmOptionsLabel());
        pmSecondaryOptionsExplanationLabel.setText(constants.hostPopupPmOptionsExplanationLabel());
        pmSecondarySecureEditor.setLabel(constants.hostPopupPmSecureLabel());
        consoleAddress.setLabel(constants.consoleAddress());
        consoleAddressLabel.setText(constants.enableConsoleAddressOverride());

        // Auto PM
        disableAutomaticPowerManagementEditor.setLabel(constants.hostPopupPmDisableAutoPM());
        pmKdumpDetectionEditor.setLabel(constants.hostPopupPmKdumpDetection());

        // SPM tab
        spmTab.setLabel(constants.spmTestButtonLabel());
        consoleTab.setLabel(constants.consoleButtonLabel());

        // Network Provider Tab
        networkProviderTab.setLabel(constants.networkProviderButtonLabel());

        externalDiscoveredHostsEditor.setLabel(constants.discoveredHostsLabel());
        externalHostGroupsEditor.setLabel(constants.hostGroupsLabel());
        externalComputeResourceEditor.setLabel(constants.computeResourceLabel());
    }

    private void applyModeCustomizations() {
        if (ApplicationModeHelper.getUiMode() == ApplicationMode.GlusterOnly) {
            spmTab.setVisible(false);
            powerManagementTab.setVisible(false);
            consoleTab.setVisible(false);
            networkProviderTab.setVisible(false);
        }

    }

    @Override
    public void setMessage(String message) {
        testMessage.setText(message);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void edit(final HostModel object) {
        driver.edit(object);
        setTabIndexes(0);

        object.getFetchResult().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                String fetchResultText = object.getFetchResult().getEntity();
                if (ConstantsManager.getInstance().getConstants().errorLoadingFingerprint().equals(fetchResultText)) {
                    fetchResult.addStyleName(style.fetchResultErrorLabel());
                } else {
                    fetchResult.removeStyleName(style.fetchResultErrorLabel());
                }
                fetchResult.setText(fetchResultText);
            }
        });

        object.getPkSection().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if (args.propertyName == "IsAvailable") { //$NON-NLS-1$
                    setPkPasswordSectionVisiblity(false);
                }
            }
        });

        object.getProviders().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                object.updateHosts();
            }
        });

        object.getExternalHostProviderEnabled().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                boolean showForemanProviders = object.getExternalHostProviderEnabled().getEntity();
                providersEditor.setVisible(showForemanProviders);

                // showing or hiding radio buttons
                provisionedHostSection.setVisible(showForemanProviders);
                discoveredHostSection.setVisible(showForemanProviders);

                // disabling ip and name textbox when using provisioned hosts
                hostAddressEditor.setEnabled(!showForemanProviders);

                if (showForemanProviders) {
                    object.updateHosts();
                } else {
                    object.cleanHostParametersFields();
                    hideProviderWidgets(object);
                }
            }
        });

        object.getIsDiscoveredHosts().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (Boolean.TRUE.equals(object.getIsDiscoveredHosts().getEntity())) {
                    rbDiscoveredHost.setValue(true);
                    showDiscoveredHostsWidgets(true);
                } else if (Boolean.FALSE.equals(object.getIsDiscoveredHosts().getEntity())) {
                    rbProvisionedHost.setValue(true);
                    showProvisionedHostsWidgets(true);
                }
            }
        });

        nameEditor.asValueBox().addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (object.getExternalHostProviderEnabled().getEntity() &&
                                Boolean.TRUE.equals(object.getIsDiscoveredHosts().getEntity())) {
                            ExternalHostGroup dhg =
                                    (ExternalHostGroup) object.getExternalHostGroups().getSelectedItem();
                            if (dhg != null) {
                                String base = nameEditor.asEditor().getSubEditor().getValue();
                                if (base == null) {
                                    base = constants.empty();
                                }
                                String generatedHostName = base + "." + //$NON-NLS-1$
                                        (dhg.getDomainName() != null ? dhg.getDomainName() : constants.empty());
                                object.getHost().setEntity(generatedHostName);
                            }
                        }
                    }
                });
            }
        });

        rbPassword.setValue(true);
        rbPassword.setFocus(true);

        displayPassPkWindow(true);
        fetchSshFingerprint.hideLabel();
        object.setAuthenticationMethod(AuthenticationMethod.Password);

        rbPassword.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                object.setAuthenticationMethod(AuthenticationMethod.Password);
                displayPassPkWindow(true);
            }
        });

        rbPublicKey.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                object.setAuthenticationMethod(AuthenticationMethod.PublicKey);
                displayPassPkWindow(false);
            }
        });

        testButton.setCommand(object.getTestCommand());

        // Bind proxy commands.
        upButton.setCommand(object.getProxyUpCommand());
        upButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                object.getProxyUpCommand().execute();
            }
        });

        downButton.setCommand(object.getProxyDownCommand());
        downButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                object.getProxyDownCommand().execute();
            }
        });

        updateHostsButton.setResource(resources.searchButtonImage());

        // Bind proxy list.
        object.getPmProxyPreferencesList().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                proxyListBox.clear();

                for (Object item : object.getPmProxyPreferencesList().getItems()) {
                    proxyListBox.addItem((String) item);
                }
            }
        });

        object.getPmProxyPreferencesList().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {

                List items = (List) object.getPmProxyPreferencesList().getItems();
                int selectedItemIndex = items.indexOf(object.getPmProxyPreferencesList().getSelectedItem());

                proxyListBox.setSelectedIndex(selectedItemIndex);
            }
        });

        object.getPmProxyPreferencesList().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if (args.propertyName == "IsChangable") { //$NON-NLS-1$
                    proxyListBox.setEnabled(object.getPmProxyPreferencesList().getIsChangable());
                }
            }
        });
        proxyListBox.setEnabled(object.getPmProxyPreferencesList().getIsChangable());

        proxyListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                List<String> items = (List<String>) object.getPmProxyPreferencesList().getItems();

                String selectedItem = proxyListBox.getSelectedIndex() >= 0
                        ? items.get(proxyListBox.getSelectedIndex())
                        : null;

                object.getPmProxyPreferencesList().setSelectedItem(selectedItem);
            }
        });

        // Create SPM related controls.
        IEventListener<EventArgs> spmListener = new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {

                createSpmControls(object);
            }
        };

        object.getSpmPriority().getItemsChangedEvent().addListener(spmListener);
        object.getSpmPriority().getSelectedItemChangedEvent().addListener(spmListener);

        createSpmControls(object);

        // Wire events on power management related controls.
        object.getPmVariants().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {

                ListModel model = (ListModel) sender;
                List items = (List) model.getItems();
                Object selectedItem = model.getSelectedItem();

                updatePmPanelsVisibility(items.indexOf(selectedItem) == 0);
            }
        });

        updatePmPanelsVisibility(true);
        initExternalHostProviderWidgets(object.showExternalProviderPanel());
        // TODO: remove setIsChangable when configured ssh username is enabled
        userNameEditor.setEnabled(false);

        networkProviderTab.setVisible(object.showNetworkProviderTab());
        networkProviderWidget.edit(object.getNetworkProviderModel());

        addTextAndLinkAlert(fetchPanel, appConstants.fetchingHostFingerprint(), object.getSSHFingerPrint());
        nameEditor.setFocus(true);
    }

    private void showDiscoveredHostsWidgets(boolean enabled) {
        usualFormToDiscover(enabled);
        showExternalDiscoveredHost(enabled);
        showExternalProvisionedHosts(!enabled);
    }

    private void showProvisionedHostsWidgets(boolean enabled) {
        usualFormToDiscover(!enabled);
        showExternalDiscoveredHost(!enabled);
        showExternalProvisionedHosts(enabled);
    }

    private void hideProviderWidgets(final HostModel object) {
        rbProvisionedHost.setValue(false);
        rbDiscoveredHost.setValue(false);
        usualFormToDiscover(false);
        showExternalDiscoveredHost(false);
        showExternalProvisionedHosts(false);
        object.getIsDiscoveredHosts().setEntity(null);
    }

    private void initExternalHostProviderWidgets(boolean isAvailable) {
        // When the widgets should be enabled, only the "enable/disable" one should appear.
        // All the rest shouldn't be visible
        externalHostProviderEnabledEditor.setVisible(isAvailable);
        provisionedHostSection.setVisible(false);
        discoveredHostSection.setVisible(false);
        providersEditor.setVisible(false);
        showExternalDiscoveredHost(false);
        showExternalProvisionedHosts(false);
    }

    private void displayPassPkWindow(boolean isPasswordVisible) {
        if (isPasswordVisible) {
            passwordEditor.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            publicKeyEditor.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        } else {
            passwordEditor.getElement().getStyle().setVisibility(Visibility.HIDDEN);
            publicKeyEditor.getElement().getStyle().setVisibility(Visibility.VISIBLE);
        }
    }

    private void initExpander() {
        expander.initWithContent(expanderContent.getElement());
    }

    private void updatePmPanelsVisibility(boolean primary) {

        pmPrimaryPanel.setVisible(primary);
        pmSecondaryPanel.setVisible(!primary);
    }

    private void showExternalDiscoveredHost(boolean enabled) {
        discoveredHostsPanel.setVisible(enabled);
    }
    private void showExternalProvisionedHosts(boolean enabled) {
        searchProviderPanel.setVisible(enabled);
    }

    private void usualFormToDiscover(boolean isDiscovered) {
        if (isDiscovered) {
            authLabel.setText(constants.hostPopupAuthLabelForExternalHost());
        } else {
            authLabel.setText(constants.hostPopupAuthLabel());
            displayPassPkWindow(true);
        }
        rbPublicKey.setVisible(!isDiscovered);
        rbPassword.setVisible(!isDiscovered);
        expanderContent.setVisible(!isDiscovered);
        publicKeyEditor.setVisible(!isDiscovered);
        authSshPortEditor.setVisible(!isDiscovered);
        userNameEditor.setVisible(!isDiscovered);
    }

    private void createSpmControls(final HostModel object) {

        spmPanel.clear();

        Iterable<?> items = object.getSpmPriority().getItems();
        if (items == null) {
            return;
        }

        // Recreate SPM related controls.
        for (Object item : items) {

            final EntityModel model = (EntityModel) item;

            RadioButton rb = new RadioButton("spm"); // $//$NON-NLS-1$
            rb.setText(model.getTitle());
            rb.setValue(object.getSpmPriority().getSelectedItem() == model);
            rb.addStyleName(style.radioButton());

            rb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> e) {
                    object.getSpmPriority().setSelectedItem(model);
                }
            });

            spmPanel.add(rb);
        }
    }

    @Override
    public HostModel flush() {
        networkProviderWidget.flush();
        return driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public HasUiCommandClickHandlers getTestButton() {
        return testButton;
    }

    @Override
    public HasClickHandlers getUpdateHostsButton() {
        return updateHostsButton;
    }

    @Override
    public void showPowerManagement() {
        getTabPanel().switchTab(powerManagementTab);
    }

    /**
     * Create a widget containing text and a link that triggers the execution of a command.
     *
     * @param view
     *            the view where the alert should be added
     * @param text
     *            the text content of the alert
     * @param command
     *            the command that should be executed when the link is clicked
     */
    private void addTextAndLinkAlert(SimplePanel view, final String text, final UICommand command) {
        // Find the open and close positions of the link within the message:
        final int openIndex = text.indexOf("<a>"); //$NON-NLS-1$
        final int closeIndex = text.indexOf("</a>"); //$NON-NLS-1$
        if (openIndex == -1 || closeIndex == -1 || closeIndex < openIndex) {
            return;
        }

        // Extract the text before, inside and after the tags:
        final String beforeText = text.substring(0, openIndex);
        final String betweenText = text.substring(openIndex + 3, closeIndex);
        final String afterText = text.substring(closeIndex + 4);

        // Create a flow panel containing the text and the link:
        final FlowPanel alertPanel = new FlowPanel();

        // Create the label for the text before the tag:
        final Label beforeLabel = new Label(beforeText);
        beforeLabel.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        alertPanel.add(beforeLabel);

        // Create the anchor:
        final Anchor betweenAnchor = new Anchor(betweenText);
        betweenAnchor.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        betweenAnchor.getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
        alertPanel.add(betweenAnchor);

        // Add a listener to the anchor so that the command is executed when
        // it is clicked:
        betweenAnchor.addClickHandler(
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        command.execute();
                    }
                }
                );

        // Create the label for the text after the tag:
        final Label afterLabel = new Label(afterText);
        afterLabel.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        alertPanel.add(afterLabel);

        // Add the alert to the view:
        view.add(alertPanel);
    }

    interface Style extends CssResource {

        String radioButton();

        String overrideIpStyle();

        String protocolStyle();

        String checkBox();

        String searchFilter();

        String searchFilterLabel();

        String emptyEditor();

        String fingerprintEditor();

        String expanderContent();

        String pkStyle();

        String fetchResultErrorLabel();

        String bar();

        String externalHostProviderEnabledEditorContent();
    }

    public void setPkPasswordSectionVisiblity(boolean visible) {
        pkSection.setVisible(visible);
        passwordSection.setVisible(visible);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        // ==General Tab==
        dataCenterEditor.setTabIndex(nextTabIndex++);
        clusterEditor.setTabIndex(nextTabIndex++);
        externalHostProviderEnabledEditor.setTabIndex(nextTabIndex++);
        providersEditor.setTabIndex(nextTabIndex++);
        rbProvisionedHost.setTabIndex(nextTabIndex++);
        rbDiscoveredHost.setTabIndex(nextTabIndex++);
        externalDiscoveredHostsEditor.setTabIndex(nextTabIndex++);
        externalHostGroupsEditor.setTabIndex(nextTabIndex++);
        externalComputeResourceEditor.setTabIndex(nextTabIndex++);
        providerSearchFilterLabel.setTabIndex(nextTabIndex++);
        nameEditor.setTabIndex(nextTabIndex++);
        commentEditor.setTabIndex(nextTabIndex++);
        hostAddressEditor.setTabIndex(nextTabIndex++);
        authSshPortEditor.setTabIndex(nextTabIndex++);
        userNameEditor.setTabIndex(nextTabIndex++);
        rbPassword.setTabIndex(nextTabIndex++);
        passwordEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    @Override
    public void setHostProviderVisibility(boolean visible) {
        searchProviderPanel.setVisible(visible);
    }

    @Override
    public void updatePrimaryPmSlotLabelText(boolean ciscoUcsSelected) {
        updatePmSlotLabelText(pmSlotEditor, ciscoUcsSelected);
    }

    @Override
    public void updateSecondaryPmSlotLabelText(boolean ciscoUcsSelected) {
        updatePmSlotLabelText(pmSecondarySlotEditor, ciscoUcsSelected);
    }

    void updatePmSlotLabelText(HasLabel widget, boolean ciscoUcsSelected) {
        widget.setLabel(ciscoUcsSelected ? constants.hostPopupPmCiscoUcsSlotLabel() : constants.hostPopupPmSlotLabel());
    }

    @Override
    protected void populateTabMap() {
        getTabNameMapping().put(TabName.GENERAL_TAB, this.generalTab);
        getTabNameMapping().put(TabName.POWER_MANAGEMENT_TAB, this.powerManagementTab);
        getTabNameMapping().put(TabName.NETWORK_PROVIDER_TAB, this.networkProviderTab);
        getTabNameMapping().put(TabName.CONSOLE_TAB, this.consoleTab);
        getTabNameMapping().put(TabName.SPM_TAB, this.spmTab);
    }

    @Override
    public DialogTabPanel getTabPanel() {
        return tabPanel;
    }
}
