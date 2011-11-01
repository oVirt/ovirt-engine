package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.Align;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.EnumRadioEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.renderer.NullSafeRenderer;

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

public class HostInterfacePopupView extends AbstractModelBoundPopupView<HostInterfaceModel> implements HostInterfacePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostInterfaceModel, HostInterfacePopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostInterfacePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "name.entity")
    EntityModelLabelEditor nameEditor;

    @UiField(provided = true)
    @Path(value = "network.selectedItem")
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
    public HostInterfacePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);

        networkEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {

            @Override
            protected String renderNullSafe(Object object) {
                return ((network) object).getname();
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
        bootProtocol = new EnumRadioEditor<NetworkBootProtocol>(NetworkBootProtocol.class);

        checkConnectivity = new EntityModelCheckBoxEditor(Align.RIGHT);
        commitChanges = new EntityModelCheckBoxEditor(Align.RIGHT);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        nameEditor.setLabel("Name:");
        networkEditor.setLabel("Network:");
        bondingModeEditor.setLabel("Bonding Mode:");
        customEditor.setLabel("Custom mode:");
        address.setLabel("IP:");
        subnet.setLabel("Subnet Mask:");
        checkConnectivity.setLabel("Check Connectivity:");
        info.setHTML("<I>Changes done to the Networking configuration are temporary until explicitly saved.<BR>" +
                "Check the check-box below to make the changes persistent.</I>");
        commitChanges.setLabel("Save network configuration");

        Driver.driver.initialize(this);
    }

    @Override
    public void edit(final HostInterfaceModel object) {
        Driver.driver.edit(object);
        object.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                HostInterfaceModel model = (HostInterfaceModel) sender;
                if ("BootProtocolsAvailable".equals(((PropertyChangedEventArgs) args).PropertyName)) {
                    boolean bootProtocolsAvailable = model.getBootProtocolsAvailable();
                    bootProtocol.setEnabled(bootProtocolsAvailable);
                    checkConnectivity.setEnabled(bootProtocolsAvailable);
                }
                if ("NoneBootProtocolAvailable".equals(((PropertyChangedEventArgs) args).PropertyName)) {
                    bootProtocol.setEnabled(NetworkBootProtocol.None, model.getNoneBootProtocolAvailable());
                }
                if ("Message".equals(((PropertyChangedEventArgs) args).PropertyName)) {
                    message.setText(model.getMessage());
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
    public HostInterfaceModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focus() {
        networkEditor.setFocus(true);
    }

    @Override
    public void setMessage(String message) {
    }

}
