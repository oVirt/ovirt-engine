package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractTabbedModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.EnumRadioEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NetworkAttachmentModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.NetworkAttachmentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.qos.HostNetworkQosWidget;
import org.ovirt.engine.ui.webadmin.widget.provider.DnsServersWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class NetworkAttachmentPopupView extends AbstractTabbedModelBoundPopupView<NetworkAttachmentModel> implements NetworkAttachmentPopupPresenterWidget.ViewDef {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private final Driver driver = GWT.create(Driver.class);

    @UiField
    DialogTabPanel tabPanel;

    @UiField
    @WithElementId
    DialogTab ipv4Tab;

    @UiField
    @WithElementId
    DialogTab ipv6Tab;

    @UiField
    @WithElementId
    DialogTab qosTab;

    @UiField
    @WithElementId
    DialogTab customPropertiesTab;

    @UiField
    @WithElementId
    DialogTab dnsConfigurationTab;

    @UiField(provided = true)
    EnumRadioEditor<Ipv4BootProtocol> ipv4BootProtocol;

    @UiField
    @Ignore
    EnableableFormLabel ipv4BootProtocolLabel;

    @UiField(provided = true)
    EnumRadioEditor<Ipv6BootProtocol> ipv6BootProtocol;

    @UiField
    @Ignore
    EnableableFormLabel ipv6BootProtocolLabel;

    @UiField
    @Path(value = "ipv4Address.entity")
    StringEntityModelTextBoxEditor ipv4Address;

    @UiField
    @Path(value = "ipv4Subnet.entity")
    StringEntityModelTextBoxEditor ipv4Subnet;

    @UiField
    @Path(value = "ipv4Gateway.entity")
    StringEntityModelTextBoxEditor ipv4Gateway;

    @UiField
    @Path(value = "ipv6Address.entity")
    StringEntityModelTextBoxEditor ipv6Address;

    @UiField
    @Path(value = "ipv6Prefix.entity")
    IntegerEntityModelTextBoxEditor ipv6Prefix;

    @UiField
    @Path(value = "ipv6Gateway.entity")
    StringEntityModelTextBoxEditor ipv6Gateway;

    @UiField(provided = true)
    @Path(value = "qosOverridden.entity")
    EntityModelCheckBoxEditor qosOverridden;

    @UiField(provided = true)
    @Ignore
    HostNetworkQosWidget qosWidget;

    @UiField
    @Ignore
    EnableableFormLabel customPropertiesLabel;

    @UiField
    @Ignore
    KeyValueWidget<KeyValueModel> customPropertiesWidget;

    @UiField(provided = true)
    @Path(value = "isToSync.entity")
    EntityModelCheckBoxEditor isToSync;

    @UiField(provided = true)
    InfoIcon isToSyncInfo;

    @UiField(provided = true)
    @Path(value = "dnsConfigurationModel.shouldSetDnsConfiguration.entity")
    @WithElementId
    public EntityModelCheckBoxEditor shouldSetDnsConfigurationEditor;

    @UiField
    @Ignore
    @WithElementId
    public DnsServersWidget dnsServersWidget;

    @UiField(provided = true)
    InfoIcon ipv6AutoconfAvailabilityInfo;

    @Inject
    public NetworkAttachmentPopupView(EventBus eventBus) {

        super(eventBus);

        ipv4BootProtocol = new EnumRadioEditor<>(Ipv4BootProtocol.class);
        ipv6BootProtocol = new EnumRadioEditor<>(Ipv6BootProtocol.class);
        qosWidget = new HostNetworkQosWidget();

        qosOverridden = new org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor(Align.RIGHT);
        shouldSetDnsConfigurationEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isToSync = new EntityModelCheckBoxEditor(Align.RIGHT);
        isToSyncInfo = new InfoIcon(
                templates.italicTwoLines(constants.syncNetworkInfoPart1(), constants.syncNetworkInfoPart2()));
        ipv6AutoconfAvailabilityInfo = new InfoIcon(templates.italicText(constants.ipv6AutoconfAvailabilityInfo()));
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        driver.initialize(this);
    }

    @Override
    protected void populateTabMap() {
        getTabNameMapping().put(TabName.IPV4_TAB, this.ipv4Tab.getTabListItem());
        getTabNameMapping().put(TabName.IPV6_TAB, this.ipv6Tab.getTabListItem());
        getTabNameMapping().put(TabName.QOS_TAB, this.qosTab.getTabListItem());
        getTabNameMapping().put(TabName.CUSTOM_PROPERTIES_TAB, this.customPropertiesTab.getTabListItem());
        getTabNameMapping().put(TabName.DNS_CONFIGURATION_TAB, this.dnsConfigurationTab.getTabListItem());
    }

    @Override
    public void edit(final NetworkAttachmentModel object) {
        driver.edit(object);
        qosWidget.edit(object.getQosModel());

        enableDisableByBootProtocol(object);
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            NetworkAttachmentModel model = (NetworkAttachmentModel) sender;
            String propertyName = args.propertyName;
            if ("BootProtocolsAvailable".equals(propertyName)) { //$NON-NLS-1$
                enableDisableByBootProtocol(model);
            }
        });

        if (object.getIsToSync().getIsChangable()) {
            isToSyncInfo.setVisible(true);
        }

        customPropertiesTab.setVisible(object.getCustomPropertiesModel().getIsAvailable());
        customPropertiesWidget.edit(object.getCustomPropertiesModel());
        customPropertiesLabel.setEnabled(object.getCustomPropertiesModel().getIsChangable());

        dnsServersWidget.edit(object.getDnsConfigurationModel().getNameServerModelListModel());
    }

    private void enableDisableByBootProtocol(NetworkAttachmentModel model) {
        boolean bootProtocolsAvailable = model.getBootProtocolsAvailable();

        ipv4BootProtocolLabel.setEnabled(bootProtocolsAvailable);
        ipv4BootProtocol.setEnabled(bootProtocolsAvailable);

        ipv6BootProtocolLabel.setEnabled(bootProtocolsAvailable);
        ipv6BootProtocol.setEnabled(bootProtocolsAvailable);
        ipv6BootProtocol.asWidget().setEnabled(Ipv6BootProtocol.AUTOCONF, model.getIpv6AutoconfAvailable());
        ipv6AutoconfAvailabilityInfo.setVisible(!model.getIpv6AutoconfAvailable());
    }

    @Override
    public NetworkAttachmentModel flush() {
        qosWidget.flush();
        dnsServersWidget.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        qosWidget.cleanup();
        driver.cleanup();
    }

    @Override
    public void focusInput() {
        ipv4BootProtocol.setFocus(true);
    }

    @Override
    public DialogTabPanel getTabPanel() {
        return tabPanel;
    }

    interface Driver extends UiCommonEditorDriver<NetworkAttachmentModel, NetworkAttachmentPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, NetworkAttachmentPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }
}
