package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EnumRadioEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NetworkAttachmentModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.NetworkAttachmentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.qos.HostNetworkQosWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;


public class NetworkAttachmentPopupView extends AbstractModelBoundPopupView<NetworkAttachmentModel> implements NetworkAttachmentPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<NetworkAttachmentModel, NetworkAttachmentPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, NetworkAttachmentPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "name.entity")
    StringEntityModelLabelEditor nameEditor;

    @UiField(provided = true)
    EnumRadioEditor<Ipv4BootProtocol> ipv4BootProtocol;

    @UiField
    @Ignore
    StringEntityModelLabelEditor ipv4BootProtocolLabel;

    @UiField
    @Path(value = "ipv4Address.entity")
    StringEntityModelTextBoxEditor ipv4Address;

    @UiField
    @Path(value = "ipv4Subnet.entity")
    StringEntityModelTextBoxEditor ipv4Subnet;

    @UiField
    @Path(value = "ipv4Gateway.entity")
    StringEntityModelTextBoxEditor ipv4Gateway;

    @UiField(provided = true)
    @Path(value = "qosOverridden.entity")
    EntityModelCheckBoxEditor qosOverridden;

    @UiField(provided = true)
    @Ignore
    HostNetworkQosWidget qosWidget;

    @UiField
    Panel customPropertiesPanel;

    @UiField
    @Ignore
    StringEntityModelLabelEditor customPropertiesLabel;

    @UiField(provided = true)
    @Ignore
    KeyValueWidget<KeyValueModel> customPropertiesWidget;

    @UiField(provided = true)
    @Path(value = "isToSync.entity")
    EntityModelCheckBoxEditor isToSync;

    @UiField(provided = true)
    InfoIcon isToSyncInfo;

    @UiField
    @Ignore
    VerticalPanel mainPanel;

    @UiField
    Style style;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public NetworkAttachmentPopupView(EventBus eventBus) {

        super(eventBus);

        ipv4BootProtocol = new EnumRadioEditor<>(Ipv4BootProtocol.class);
        qosWidget = new HostNetworkQosWidget();
        customPropertiesWidget = new KeyValueWidget<>("320px", "160px"); //$NON-NLS-1$ //$NON-NLS-2$

        qosOverridden = new org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor(Align.RIGHT);
        isToSync = new EntityModelCheckBoxEditor(Align.RIGHT);
        isToSyncInfo = new InfoIcon(templates.italicTwoLines(constants.syncNetworkInfoPart1(), constants.syncNetworkInfoPart2()));

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        // Set Styles
        ipv4BootProtocolLabel.asValueBox().setVisible(false);
        qosOverridden.setContentWidgetContainerStyleName(style.syncInfo());
        customPropertiesLabel.asValueBox().setVisible(false);
        isToSync.setContentWidgetContainerStyleName(style.syncInfo());
        mainPanel.getElement().setPropertyString("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

        // Localize
        nameEditor.setLabel(constants.nameHostPopup() + ":"); //$NON-NLS-1$
        ipv4BootProtocolLabel.setLabel(constants.bootProtocolHostPopup() +":"); //$NON-NLS-1$
        ipv4BootProtocolLabel.asEditor().getSubEditor().setValue("   "); //$NON-NLS-1$
        ipv4Address.setLabel(constants.ipHostPopup() + ":"); //$NON-NLS-1$
        ipv4Subnet.setLabel(constants.subnetMaskHostPopup() + ":"); //$NON-NLS-1$
        ipv4Gateway.setLabel(constants.gwHostPopup() + ":"); //$NON-NLS-1$
        qosOverridden.setLabel(constants.qosOverrideLabel());
        customPropertiesLabel.setLabel(constants.customPropertiesHostPopup());
        isToSync.setLabel(constants.syncNetwork());

        driver.initialize(this);
    }

    @Override
    public void edit(final NetworkAttachmentModel object) {
        driver.edit(object);
        qosWidget.edit(object.getQosModel());

        enableDisableByBootProtocol(object);
        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev,
                    Object sender,
                    PropertyChangedEventArgs args) {
                NetworkAttachmentModel model = (NetworkAttachmentModel) sender;
                String propertyName = args.propertyName;
                if ("BootProtocolsAvailable".equals(propertyName) || "NoneBootProtocolAvailable".equals(propertyName)) { //$NON-NLS-1$ //$NON-NLS-2$
                    enableDisableByBootProtocol(model);
                }
            }
        });

        if (object.getIsToSync().getIsChangable()) {
            isToSyncInfo.setVisible(true);
        }

        customPropertiesPanel.setVisible(object.getCustomPropertiesModel().getIsAvailable());
        customPropertiesWidget.edit(object.getCustomPropertiesModel());
        customPropertiesLabel.setEnabled(object.getCustomPropertiesModel().getIsChangable());
    }

    protected void enableDisableByBootProtocol(NetworkAttachmentModel model) {
        boolean bootProtocolsAvailable = model.getBootProtocolsAvailable();
        ipv4BootProtocolLabel.setEnabled(bootProtocolsAvailable);
        ipv4BootProtocol.setEnabled(bootProtocolsAvailable);
        ipv4BootProtocol.setEnabled(Ipv4BootProtocol.NONE, model.getNoneBootProtocolAvailable());
    }

    @Override
    public NetworkAttachmentModel flush() {
        qosWidget.flush();
        return driver.flush();
    }

    @Override
    public void focusInput() {
        ipv4BootProtocol.setFocus(true);
    }

    interface Style extends CssResource {

        String syncInfo();
    }

}
