package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;

public class InterfaceEditor extends Composite implements HasEditorDriver<NetworkInterfaceModel> {

    interface Driver extends SimpleBeanEditorDriver<NetworkInterfaceModel, InterfaceEditor> {
    }

    interface ViewUiBinder extends UiBinder<Widget, InterfaceEditor> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Ignore
    Label addressLabel;

    @UiField
    @Path(value = "entity.address")
    TextBox addressEditor;

    @UiField
    @Ignore
    Label subnetLabel;

    @UiField
    @Path(value = "entity.subnet")
    TextBox subnetEditor;

    @UiField
    @Ignore
    Label gatewayLabel;

    @UiField
    @Path(value = "entity.gateway")
    TextBox gatewayEditor;

    @UiField
    @Ignore
    Label protocolLabel;

    @UiField(provided = true)
    @Path(value = "entity.bootProtocol")
    ValueListBox<NetworkBootProtocol> protocolEditor;

    private final Driver driver = GWT.create(Driver.class);

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    public InterfaceEditor() {
        protocolEditor = new ValueListBox<NetworkBootProtocol>(new EnumRenderer<NetworkBootProtocol>());
        protocolEditor.setAcceptableValues(Arrays.asList(NetworkBootProtocol.class.getEnumConstants()));
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        addressLabel.setText(constants.addressInterfaceEditor());
        subnetLabel.setText(constants.subnetInterfaceEditor());
        gatewayLabel.setText(constants.gatewayInterfaceEditor());
        protocolLabel.setText(constants.protocolInterfaceEditor());
        driver.initialize(this);
    }

    @Override
    public void edit(NetworkInterfaceModel nic) {
        driver.edit(nic);
    }

    @Override
    public NetworkInterfaceModel flush() {
        return driver.flush();
    }

}
