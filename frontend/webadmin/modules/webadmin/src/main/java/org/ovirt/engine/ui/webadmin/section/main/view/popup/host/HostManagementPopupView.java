package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostManagementNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostManagementPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.editor.EnumRadioEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class HostManagementPopupView extends AbstractModelBoundPopupView<HostManagementNetworkModel> implements HostManagementPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostManagementNetworkModel, HostManagementPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostManagementPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Ignore
    EntityModelLabelEditor nameEditor;

    @UiField(provided = true)
    @Path(value = "interface.selectedItem")
    ListModelListBoxEditor<Object> networkEditor;

    @UiField(provided = true)
    @Path(value = "bondingOptions.selectedItem")
    ListModelListBoxEditor<Object> bondingModeEditor;

    @UiField
    @Ignore
    EntityModelTextBoxEditor customEditor;

    @UiField(provided = true)
    EnumRadioEditor<NetworkBootProtocol> bootProtocol;

    @UiField
    @Path(value = "address.entity")
    EntityModelTextBoxEditor address;

    @UiField
    @Path(value = "subnet.entity")
    EntityModelTextBoxEditor subnet;

    @UiField
    @Path(value = "gateway.entity")
    EntityModelTextBoxEditor gateway;

    @UiField(provided = true)
    @Path(value = "checkConnectivity.entity")
    EntityModelCheckBoxEditor checkConnectivity;

    @UiField
    @Ignore
    Label message;

    @UiField
    @Ignore
    HTML info;

    @UiField(provided = true)
    @Path(value = "commitChanges.entity")
    EntityModelCheckBoxEditor commitChanges;

    @Inject
    public HostManagementPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);

        networkEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            protected String renderNullSafe(Object object) {
                return ((VdsNetworkInterface) object).getName();
            }

        });
        bondingModeEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            protected String renderNullSafe(Object object) {
                KeyValuePairCompat<String, EntityModel> pair = (KeyValuePairCompat<String, EntityModel>) object;
                String key = pair.getKey();
                if ("custom".equals(key)) {
                    return "Custom:";
                }
                EntityModel value = pair.getValue();
                return (String) value.getEntity();
            }
        });
        bootProtocol = new EnumRadioEditor<NetworkBootProtocol>(NetworkBootProtocol.class, eventBus);

        checkConnectivity = new EntityModelCheckBoxEditor(Align.RIGHT);
        commitChanges = new EntityModelCheckBoxEditor(Align.RIGHT);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        nameEditor.setLabel("Network Name:");
        networkEditor.setLabel("Interface:");
        bondingModeEditor.setLabel("Bonding Mode:");
        customEditor.setLabel("Custom mode:");
        address.setLabel("IP:");
        subnet.setLabel("Subnet Mask:");
        gateway.setLabel("Default Gateway:");
        checkConnectivity.setLabel("Check Connectivity:");
        info.setHTML("<I>Changes done to the Networking configuration are temporary until explicitly saved.<BR>" +
                "Check the check-box below to make the changes persistent.</I>");
        commitChanges.setLabel("Save network configuration");

        Driver.driver.initialize(this);
    }

    @Override
    public void edit(final HostManagementNetworkModel object) {
        Driver.driver.edit(object);

        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                HostManagementNetworkModel model = (HostManagementNetworkModel) sender;
                String propertyName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("NoneBootProtocolAvailable".equals(propertyName)) {
                    bootProtocol.setEnabled(NetworkBootProtocol.None, model.getNoneBootProtocolAvailable());
                }
                else if ("Message".equals(propertyName)) {
                    message.setText(model.getMessage());
                }
                else if ("Entity".equals(propertyName)) {
                    nameEditor.asEditor().getSubEditor().setValue(object.getEntity().getname());
                }
            }
        });

        object.getBondingOptions().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                ListModel list = (ListModel) sender;
                @SuppressWarnings("unchecked")
                KeyValuePairCompat<String, EntityModel> pair =
                        (KeyValuePairCompat<String, EntityModel>) list.getSelectedItem();
                if ("custom".equals(pair.getKey())) {
                    customEditor.setVisible(true);
                    Object entity = pair.getValue().getEntity();
                    customEditor.asEditor().getSubEditor().setValue(entity == null ? "" : entity);
                } else {
                    customEditor.setVisible(false);
                }
            }
        });

        customEditor.asValueBox().addValueChangeHandler(new ValueChangeHandler<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onValueChange(ValueChangeEvent<Object> event) {
                for (Object item : object.getBondingOptions().getItems()) {
                    KeyValuePairCompat<String, EntityModel> pair = (KeyValuePairCompat<String, EntityModel>) item;
                    if ("custom".equals(pair.getKey())) {
                        pair.getValue().setEntity(event.getValue());
                    }
                }
            }
        });

        bondingModeEditor.setVisible(true);
        bondingModeEditor.asWidget().setVisible(true);
    }

    @Override
    public HostManagementNetworkModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focusInput() {
        networkEditor.setFocus(true);
    }

    @Override
    public void setMessage(String message) {
    }

}
