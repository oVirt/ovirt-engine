package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EnumRadioEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.networkQoS.NetworkQosWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;


public class HostInterfacePopupView extends AbstractModelBoundPopupView<HostInterfaceModel> implements HostInterfacePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostInterfaceModel, HostInterfacePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostInterfacePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "name.entity")
    StringEntityModelLabelEditor nameEditor;

    @UiField(provided = true)
    @Path(value = "network.selectedItem")
    ListModelListBoxEditor<Network> networkEditor;

    @UiField(provided = true)
    @Path(value = "interface.selectedItem")
    ListModelListBoxEditor<VdsNetworkInterface> interfaceEditor;

    @UiField(provided = true)
    @Path(value = "bondingOptions.selectedItem")
    ListModelListBoxEditor<Map.Entry<String, EntityModel<String>>> bondingModeEditor;

    @UiField
    @Ignore
    StringEntityModelTextBoxEditor customEditor;

    @UiField(provided = true)
    EnumRadioEditor<NetworkBootProtocol> bootProtocol;

    @UiField
    @Ignore
    StringEntityModelLabelEditor bootProtocolLabel;

    @UiField
    @Path(value = "address.entity")
    StringEntityModelTextBoxEditor address;

    @UiField
    @Path(value = "subnet.entity")
    StringEntityModelTextBoxEditor subnet;

    @UiField
    @Path(value = "gateway.entity")
    StringEntityModelTextBoxEditor gateway;

    @UiField(provided = true)
    @Path(value = "qosOverridden.entity")
    EntityModelCheckBoxEditor qosOverridden;

    @UiField(provided = true)
    @Ignore
    NetworkQosWidget qosWidget;

    @UiField
    Panel customPropertiesPanel;

    @UiField
    @Ignore
    StringEntityModelLabelEditor customPropertiesLabel;

    @UiField(provided = true)
    @Ignore
    KeyValueWidget<KeyValueModel> customPropertiesWidget;

    @UiField(provided = true)
    @Path(value = "checkConnectivity.entity")
    EntityModelCheckBoxEditor checkConnectivity;

    @UiField(provided = true)
    @Path(value = "isToSync.entity")
    EntityModelCheckBoxEditor isToSync;

    @UiField(provided = true)
    InfoIcon isToSyncInfo;

    @UiField
    @Ignore
    Label message;

    @UiField
    @Ignore
    Label displayNetworkChangeWarning;

    @UiField
    @Ignore
    DockLayoutPanel layoutPanel;

    @UiField
    @Ignore
    VerticalPanel mainPanel;

    @UiField
    @Ignore
    VerticalPanel infoPanel;

    @UiField
    @Ignore
    HTML info;

    @UiField(provided = true)
    @Path(value = "commitChanges.entity")
    EntityModelCheckBoxEditor commitChanges;

    @UiField
    Style style;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public HostInterfacePopupView(EventBus eventBus,
            ApplicationResources resources,
            final ApplicationConstants constants,
            final ApplicationTemplates templates) {

        super(eventBus, resources);

        networkEditor = new ListModelListBoxEditor<Network>(new NullSafeRenderer<Network>() {
            @Override
            protected String renderNullSafe(Network network) {
                return network.getName();
            }

        });
        interfaceEditor = new ListModelListBoxEditor<VdsNetworkInterface>(new NullSafeRenderer<VdsNetworkInterface>() {
            @Override
            protected String renderNullSafe(VdsNetworkInterface network) {
                return network.getName();
            }

        });
        bondingModeEditor = new ListModelListBoxEditor<Map.Entry<String, EntityModel<String>>>(new NullSafeRenderer<Map.Entry<String, EntityModel<String>>>() {
            @Override
            protected String renderNullSafe(Map.Entry<String, EntityModel<String>> pair) {
                String key = pair.getKey();
                String value = pair.getValue().getEntity();
                if ("custom".equals(key)) { //$NON-NLS-1$
                    return constants.customHostPopup() + ": " + value; //$NON-NLS-1$
                }
                return value;
            }
        });
        bootProtocol = new EnumRadioEditor<NetworkBootProtocol>(NetworkBootProtocol.class);
        qosWidget = new NetworkQosWidget(constants);
        customPropertiesWidget = new KeyValueWidget<KeyValueModel>("320px", "160px"); //$NON-NLS-1$ $NON-NLS-2$

        qosOverridden = new org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor(Align.RIGHT);
        checkConnectivity = new EntityModelCheckBoxEditor(Align.RIGHT);
        commitChanges = new EntityModelCheckBoxEditor(Align.RIGHT);
        isToSync = new EntityModelCheckBoxEditor(Align.RIGHT);
        isToSyncInfo = new InfoIcon(templates.italicTwoLines(constants.syncNetworkInfoPart1(), constants.syncNetworkInfoPart2()), resources);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        // Set Styles
        bootProtocolLabel.asValueBox().setVisible(false);
        checkConnectivity.setContentWidgetStyleName(style.checkCon());
        qosOverridden.setContentWidgetStyleName(style.syncInfo());
        customPropertiesLabel.asValueBox().setVisible(false);
        isToSync.setContentWidgetStyleName(style.syncInfo());
        mainPanel.getElement().setPropertyString("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

        // Localize
        nameEditor.setLabel(constants.nameHostPopup() + ":"); //$NON-NLS-1$
        networkEditor.setLabel(constants.networkHostPopup() + ":"); //$NON-NLS-1$
        interfaceEditor.setLabel(constants.intefaceHostPopup() + ":"); //$NON-NLS-1$
        bondingModeEditor.setLabel(constants.bondingModeHostPopup() + ":"); //$NON-NLS-1$
        bootProtocolLabel.setLabel(constants.bootProtocolHostPopup() +":"); //$NON-NLS-1$
        bootProtocolLabel.asEditor().getSubEditor().setValue("   "); //$NON-NLS-1$
        customEditor.setLabel(constants.customModeHostPopup() + ":"); //$NON-NLS-1$
        address.setLabel(constants.ipHostPopup() + ":"); //$NON-NLS-1$
        subnet.setLabel(constants.subnetMaskHostPopup() + ":"); //$NON-NLS-1$
        gateway.setLabel(constants.gwHostPopup() + ":"); //$NON-NLS-1$
        qosOverridden.setLabel(constants.qosOverrideLabel());
        customPropertiesLabel.setLabel(constants.customPropertiesHostPopup());
        checkConnectivity.setLabel(constants.checkConHostPopup() + ":"); //$NON-NLS-1$
        info.setHTML(constants.changesTempHostPopup());
        isToSync.setLabel(constants.syncNetwork());
        commitChanges.setLabel(constants.saveNetConfigHostPopup());
        displayNetworkChangeWarning.setText(constants.changeDisplayNetworkWarning());

        driver.initialize(this);
    }

    @Override
    public void edit(final HostInterfaceModel object) {
        driver.edit(object);
        qosWidget.edit(object.getQosModel());

        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                HostInterfaceModel model = (HostInterfaceModel) sender;
                String propertyName = ((PropertyChangedEventArgs) args).propertyName;
                if ("BootProtocolsAvailable".equals(propertyName)) { //$NON-NLS-1$
                    enableDisableByBootProtocol(model);
                    checkConnectivity.setEnabled(model.getBootProtocolsAvailable());
                }
                if ("NoneBootProtocolAvailable".equals(propertyName)) { //$NON-NLS-1$
                    bootProtocol.setEnabled(NetworkBootProtocol.NONE, model.getNoneBootProtocolAvailable());
                }
                if ("Message".equals(propertyName)) { //$NON-NLS-1$
                    message.setText(model.getMessage());
                }
            }
        });

        object.getBondingOptions().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                @SuppressWarnings("unchecked")
                ListModel<Map.Entry<String, EntityModel<String>>> list =
                        (ListModel<Map.Entry<String, EntityModel<String>>>) sender;
                Map.Entry<String, EntityModel<String>> pair = list.getSelectedItem();
                if ("custom".equals(pair.getKey())) { //$NON-NLS-1$
                    customEditor.setVisible(true);
                    String entity = pair.getValue().getEntity();
                    customEditor.asEditor().getSubEditor().setValue(entity == null ? "" : entity); //$NON-NLS-1$
                } else {
                    customEditor.setVisible(false);
                }
            }
        });

        customEditor.asValueBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                for (Map.Entry<String, EntityModel<String>> item : object.getBondingOptions().getItems()) {
                    if ("custom".equals(item.getKey())) { //$NON-NLS-1$
                        item.getValue().setEntity(event.getValue());
                    }
                }
            }
        });

        bondingModeEditor.setVisible(true);
        bondingModeEditor.asWidget().setVisible(true);

        isToSync.setVisible(false);
        isToSyncInfo.setVisible(false);
        displayNetworkChangeWarning.setVisible(false);
    }

    protected void enableDisableByBootProtocol(HostInterfaceModel model) {
        boolean bootProtocolsAvailable = model.getBootProtocolsAvailable();
        bootProtocolLabel.setEnabled(bootProtocolsAvailable);
        bootProtocol.setEnabled(bootProtocolsAvailable);
        bootProtocol.setEnabled(NetworkBootProtocol.NONE, model.getNoneBootProtocolAvailable());
    }

    @Override
    public HostInterfaceModel flush() {
        qosWidget.flush();
        return driver.flush();
    }

    @Override
    public void focusInput() {
        networkEditor.setFocus(true);
    }

    interface Style extends CssResource {

        String checkCon();
        String syncInfo();
    }

}
