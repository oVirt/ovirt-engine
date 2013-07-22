package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.List;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.provider.NeutronAgentWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.NeutronAgentModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

public class HostPopupView extends AbstractModelBoundPopupView<HostModel> implements HostPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostModel, HostPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostPopupView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HostPopupView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    Style style;

    @UiField
    DialogTabPanel tabPanel;

    @UiField
    @WithElementId
    DialogTab generalTab;

    @UiField
    @WithElementId
    DialogTab powerManagementTab;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId("dataCenter")
    ListModelListBoxEditor<Object> dataCenterEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    @WithElementId("cluster")
    ListModelListBoxEditor<Object> clusterEditor;

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "userName.entity")
    @WithElementId("userName")
    EntityModelTextBoxEditor userNameEditor;

    @UiField
    @Path(value = "fetchSshFingerprint.entity")
    @WithElementId("fetchSshFingerprint")
    EntityModelTextBoxEditor fetchSshFingerprint;

    @UiField
    @Ignore
    @WithElementId("fetchResult")
    Label fetchResult;

    @UiField
    @Path(value = "comment.entity")
    @WithElementId("comment")
    EntityModelTextBoxEditor commentEditor;

    @UiField
    @Path(value = "providerSearchFilter.entity")
    @WithElementId("providerSearchFilter")
    EntityModelTextBoxEditor providerSearchFilterEditor;

    @UiField(provided = true)
    @Path(value = "externalHostName.selectedItem")
    @WithElementId("externalHostName")
    ListModelListBoxEditor<Object> externalHostNameEditor;

    @UiField(provided = true)
    @Path(value = "providers.selectedItem")
    @WithElementId("providers")
    ListModelListBoxEditor<Object> providersEditor;

    @UiField
    @Path(value = "host.entity")
    @WithElementId("host")
    EntityModelTextBoxEditor hostAddressEditor;

    @UiField
    @Path(value = "authSshPort.entity")
    @WithElementId("authSshPort")
    EntityModelTextBoxEditor authSshPortEditor;

    @UiField
    @Path(value = "userPassword.entity")
    @WithElementId("userPassword")
    EntityModelPasswordBoxEditor passwordEditor;

    @UiField(provided = true)
    @Path(value = "publicKey.entity")
    @WithElementId("publicKey")
    EntityModelTextAreaLabelEditor publicKeyEditor;

    @UiField
    @Path(value = "overrideIpTables.entity")
    @WithElementId("overrideIpTables")
    EntityModelCheckBoxEditor overrideIpTablesEditor;

    @UiField(provided = true)
    @Path(value = "isPm.entity")
    @WithElementId("isPm")
    EntityModelCheckBoxEditor pmEnabledEditor;

    @UiField(provided = true)
    @Path(value = "pmVariants.selectedItem")
    @WithElementId("pmVariants")
    ListModelListBoxOnlyEditor<Object> pmVariantsEditor;

    @UiField
    @Path(value = "pmSecondaryConcurrent.entity")
    @WithElementId("pmSecondaryConcurrent")
    EntityModelCheckBoxEditor pmSecondaryConcurrentEditor;

    @UiField
    FlowPanel pmPrimaryPanel;

    @UiField
    @Path(value = "managementIp.entity")
    @WithElementId("managementIp")
    EntityModelTextBoxEditor pmAddressEditor;

    @UiField
    @Path(value = "pmUserName.entity")
    @WithElementId("pmUserName")
    EntityModelTextBoxEditor pmUserNameEditor;

    @UiField
    @Path(value = "pmPassword.entity")
    @WithElementId("pmPassword")
    EntityModelPasswordBoxEditor pmPasswordEditor;

    @UiField(provided = true)
    @Path(value = "pmType.selectedItem")
    @WithElementId("pmType")
    ListModelListBoxEditor<Object> pmTypeEditor;

    @UiField
    @Path(value = "pmPort.entity")
    @WithElementId("pmPort")
    EntityModelTextBoxEditor pmPortEditor;

    @UiField
    @Path(value = "pmSlot.entity")
    @WithElementId("pmSlot")
    EntityModelTextBoxEditor pmSlotEditor;

    @UiField
    @Path(value = "pmOptions.entity")
    @WithElementId("pmOptions")
    EntityModelTextBoxEditor pmOptionsEditor;

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
    EntityModelTextBoxEditor pmSecondaryAddressEditor;

    @UiField
    @Path(value = "pmSecondaryUserName.entity")
    @WithElementId("pmSecondaryUserName")
    EntityModelTextBoxEditor pmSecondaryUserNameEditor;

    @UiField
    @Path(value = "pmSecondaryPassword.entity")
    @WithElementId("pmSecondaryPassword")
    EntityModelPasswordBoxEditor pmSecondaryPasswordEditor;

    @UiField(provided = true)
    @Path(value = "pmSecondaryType.selectedItem")
    @WithElementId("pmSecondaryType")
    ListModelListBoxEditor<Object> pmSecondaryTypeEditor;

    @UiField
    @Path(value = "pmSecondaryPort.entity")
    @WithElementId("pmSecondaryPort")
    EntityModelTextBoxEditor pmSecondaryPortEditor;

    @UiField
    @Path(value = "pmSecondarySlot.entity")
    @WithElementId("pmSecondarySlot")
    EntityModelTextBoxEditor pmSecondarySlotEditor;

    @UiField
    @Path(value = "pmSecondaryOptions.entity")
    @WithElementId("pmSecondaryOptions")
    EntityModelTextBoxEditor pmSecondaryOptionsEditor;

    @UiField
    @Ignore
    Label pmSecondaryOptionsExplanationLabel;

    @UiField
    @Path(value = "pmSecondarySecure.entity")
    @WithElementId("pmSecondarySecure")
    EntityModelCheckBoxEditor pmSecondarySecureEditor;

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

    @UiField(provided = true)
    @Ignore
    @WithElementId("rbPublicKey")
    public RadioButton rbPublicKey;

    @UiField(provided = true)
    @Ignore
    @WithElementId("rbPassword")
    public RadioButton rbPassword;

    @UiField
    @Ignore
    Label authLabel;

    @UiField
    @Ignore
    Label fingerprintLabel;

    @UiField(provided=true)
    @Ignore
    InfoIcon consoleAddressInfoIcon;

    @UiField
    @Ignore
    Label consoleAddressLabel;

    @UiField
    @Path(value = "consoleAddress.entity")
    @WithElementId
    EntityModelTextBoxEditor consoleAddress;

    @UiField
    @Path(value = "providerSearchFilterLabel.entity")
    EntityModelTextBoxEditor providerSearchFilterLabel;

    @UiField
    @Path(value = "consoleAddressEnabled.entity")
    EntityModelCheckBoxEditor consoleAddressEnabled;

    @UiField(provided = true)
    InfoIcon providerSearchInfoIcon;

    @UiField(provided = true)
    @WithElementId("networkProvider")
    public EntityModelWidgetWithInfo networkProvider;

    @Ignore
    @WithElementId("networkProviderLabel")
    public EntityModelLabel networkProviderLabel;

    @Path(value = "networkProviders.selectedItem")
    @WithElementId("networkProviderEditor")
    public ListModelListBoxOnlyEditor<Object> networkProviderEditor;

    @UiField(provided = true)
    @Path(value = "networkProviderType.selectedItem")
    @WithElementId("networkProviderType")
    public ListModelListBoxEditor<Object> networkProviderTypeEditor;

    @UiField
    @Path(value = "providerPluginType.selectedItem")
    @WithElementId("providerPluginType")
    public ListModelSuggestBoxEditor providerPluginTypeEditor;

    @UiField
    FlowPanel neutronAgentPanel;

    @UiField
    @Ignore
    NeutronAgentWidget neutronAgentWidget;

    @UiField
    @Ignore
    AdvancedParametersExpander expander;

    @UiField
    @Ignore
    FlowPanel expanderContent;

    @UiField
    FlowPanel searchProviderPanel;

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
    }

    private void addStyles() {
        overrideIpTablesEditor.addContentWidgetStyleName(style.overrideIpStyle());
        externalHostProviderEnabledEditor.addContentWidgetStyleName(style.checkBox());
        providerSearchFilterEditor.addContentWidgetStyleName(style.searchFilter());
        providerSearchFilterEditor.setStyleName(style.searchFilterLabel());
        providerSearchFilterEditor.setLabelStyleName(style.emptyEditor());
        providerSearchFilterLabel.addContentWidgetStyleName(style.emptyEditor());
        providerSearchFilterLabel.setStyleName(style.searchFilterLabel());
        fetchSshFingerprint.addContentWidgetStyleName(style.fingerprintEditor());
        expanderContent.setStyleName(style.expanderContent());
        publicKeyEditor.setCustomStyle(style.pkStyle());
    }

    private void initEditors() {
        publicKeyEditor = new EntityModelTextAreaLabelEditor();

        // List boxes
        dataCenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((StoragePool) object).getName();
            }
        });

        clusterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDSGroup) object).getName();
            }
        });

        externalHostNameEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDS) object).getName();
            }
        });

        providersEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Provider) object).getName();
            }
        });

        pmVariantsEditor = new ListModelListBoxOnlyEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            protected String renderNullSafe(Object object) {
                return (String) object;
            }
        });

        pmTypeEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            protected String renderNullSafe(Object object) {
                return (String) object;
            }
        });

        pmSecondaryTypeEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            protected String renderNullSafe(Object object) {
                return (String) object;
            }
        });

        // Check boxes
        pmEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        externalHostProviderEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        networkProviderLabel = new EntityModelLabel();
        networkProviderEditor = new ListModelListBoxOnlyEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Provider) object).getName();
            }
        });
        networkProvider = new EntityModelWidgetWithInfo(networkProviderLabel, networkProviderEditor);
        networkProviderTypeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());
        rbPassword = new RadioButton("1"); //$NON-NLS-1$
        rbPublicKey = new RadioButton("1"); //$NON-NLS-1$
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

        fingerprintLabel.setText(constants.hostPopupHostFingerprintLabel());
        overrideIpTablesEditor.setLabel(constants.hostPopupOverrideIpTablesLabel());
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

        // SPM tab
        spmTab.setLabel(constants.spmTestButtonLabel());
        consoleTab.setLabel(constants.consoleButtonLabel());

        // Network Provider Tab
        networkProviderTab.setLabel(constants.networkProviderButtonLabel());
        networkProviderLabel.setText(constants.externalNetworkProviderLabel());
        networkProvider.setExplanation(applicationTemplates.italicText(constants.externalProviderExplanation()));
        networkProviderTypeEditor.setLabel(constants.typeProvider());
        providerPluginTypeEditor.setLabel(constants.pluginType());
    }

    private void applyModeCustomizations() {
        if (ApplicationModeHelper.getUiMode() == ApplicationMode.GlusterOnly)
        {
            spmTab.setVisible(false);
            powerManagementTab.setVisible(false);
            consoleTab.setVisible(false);
        }
    }

    @Override
    public void setMessage(String message) {
        testMessage.setText(message);
    }

    @Override
    public void edit(final HostModel object) {
        driver.edit(object);
        setTabIndexes(0);

        // TODO should be handled in a more generic way
        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;

                if ("IsGeneralTabValid".equals(propName)) { //$NON-NLS-1$
                    if (object.getIsGeneralTabValid()) {
                        generalTab.markAsValid();
                    } else {
                        generalTab.markAsInvalid(null);
                    }
                } else if ("IsPowerManagementTabValid".equals(propName)) { //$NON-NLS-1$
                    if (object.getIsPowerManagementTabValid()) {
                        powerManagementTab.markAsValid();
                    } else {
                        powerManagementTab.markAsInvalid(null);
                    }
                }
            }
        });

        object.getFetchResult().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                fetchResult.setText((String) object.getFetchResult().getEntity());
            }
        });

        object.getPkSection().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs e = (PropertyChangedEventArgs) args;
                if (e.PropertyName == "IsAvailable") { //$NON-NLS-1$
                    setPkPasswordSectionVisiblity(false);
                }
            }
        });

        rbPassword.setValue(true);
        displayPassPkWindow(true);
        fetchSshFingerprint.hideLabel();
        object.setAuthenticationMethod(AuthenticationMethod.Password);

        rbPassword.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                object.setAuthenticationMethod(AuthenticationMethod.Password);
                displayPassPkWindow(true);
            }
        });

        rbPublicKey.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
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
        object.getPmProxyPreferencesList().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                proxyListBox.clear();

                for (Object item : object.getPmProxyPreferencesList().getItems()) {
                    proxyListBox.addItem((String) item);
                }
            }
        });

        object.getPmProxyPreferencesList().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

                List items = (List) object.getPmProxyPreferencesList().getItems();
                int selectedItemIndex = items.indexOf(object.getPmProxyPreferencesList().getSelectedItem());

                proxyListBox.setSelectedIndex(selectedItemIndex);
            }
        });

        object.getPmProxyPreferencesList().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs e = (PropertyChangedEventArgs) args;
                if (e.PropertyName == "IsChangable") { //$NON-NLS-1$
                    proxyListBox.setEnabled(object.getPmProxyPreferencesList().getIsChangable());
                }
            }
        });
        proxyListBox.setEnabled(object.getPmProxyPreferencesList().getIsChangable());

        proxyListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                List items = (List) object.getPmProxyPreferencesList().getItems();

                Object selectedItem = proxyListBox.getSelectedIndex() >= 0
                        ? items.get(proxyListBox.getSelectedIndex())
                        : null;

                object.getPmProxyPreferencesList().setSelectedItem(selectedItem);
            }
        });

        // Create SPM related controls.
        IEventListener spmListener = new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

                createSpmControls(object);
            }
        };

        object.getSpmPriority().getItemsChangedEvent().addListener(spmListener);
        object.getSpmPriority().getSelectedItemChangedEvent().addListener(spmListener);

        createSpmControls(object);

        // Wire events on power management related controls.
        object.getPmVariants().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

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
        final NeutronAgentModel model = object.getNeutronAgentModel();
        neutronAgentWidget.edit(model);
        neutronAgentPanel.setVisible((Boolean) model.isPluginConfigurationAvailable().getEntity());
        model.isPluginConfigurationAvailable().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                neutronAgentPanel.setVisible((Boolean) model.isPluginConfigurationAvailable().getEntity());
            }
        });

        addTextAndLinkAlert(fetchPanel, appConstants.fetchingHostFingerprint(), object.getSSHFingerPrint());
        nameEditor.setFocus(true);
    }

    private void initExternalHostProviderWidgets(boolean isAvailable) {
        // When the widgets should be enabled, only the "enable/disable" one should appear.
        // All the rest shouldn't be visible
        externalHostProviderEnabledEditor.setVisible(isAvailable);
        externalHostNameEditor.setVisible(false);
        providersEditor.setVisible(false);
        searchProviderPanel.setVisible(false);
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
        tabPanel.switchTab(powerManagementTab);
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

        String checkBox();

        String searchFilter();

        String searchFilterLabel();

        String emptyEditor();

        String fingerprintEditor();

        String expanderContent();

        String pkStyle();
    }

    public void setPkPasswordSectionVisiblity(boolean visible) {
        pkSection.setVisible(visible);
        passwordSection.setVisible(visible);
    }

    public int setTabIndexes(int nextTabIndex) {
        // ==General Tab==
        dataCenterEditor.setTabIndex(nextTabIndex++);
        clusterEditor.setTabIndex(nextTabIndex++);
        externalHostProviderEnabledEditor.setTabIndex(nextTabIndex++);
        providersEditor.setTabIndex(nextTabIndex++);
        providerSearchFilterLabel.setTabIndex(nextTabIndex++);
        nameEditor.setTabIndex(nextTabIndex++);
        hostAddressEditor.setTabIndex(nextTabIndex++);
        authSshPortEditor.setTabIndex(nextTabIndex++);
        userNameEditor.setTabIndex(nextTabIndex++);
        rbPassword.setTabIndex(nextTabIndex++);
        passwordEditor.setTabIndex(nextTabIndex++);
        fetchSshFingerprint.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    @Override
    public void setHostProviderVisibility(boolean visible) {
        searchProviderPanel.setVisible(visible);
    }
}
