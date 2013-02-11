package org.ovirt.engine.ui.common.widget.network;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.TakesConstrainedValueEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.vms.NicWithLogicalNetworks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LogicalNetworkEditor extends Composite implements IsEditor<TakesValueEditor<NicWithLogicalNetworks>>, TakesValue<NicWithLogicalNetworks>, HasElementId {

    interface WidgetUiBinder extends UiBinder<Widget, LogicalNetworkEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided = true)
    ListModelListBoxEditor<Network> logicalNetworkEditor;
    private NicWithLogicalNetworks nicWithNetworks;

    private String elementId;

    public LogicalNetworkEditor() {
        logicalNetworkEditor = new ListModelListBoxEditor<Network>(
                new NullSafeRenderer<Network>() {
                    @Override
                    public String renderNullSafe(Network network) {
                        // the null network is automatically rendered as empty string by the parent renderer
                        return network.getName();
                    }
                }
                );

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public void setValue(NicWithLogicalNetworks nicWithNetworks) {
        this.nicWithNetworks = nicWithNetworks;
        logicalNetworkEditor.setLabel(nicWithNetworks.getNetworkInterface().getName());
        logicalNetworkEditor.asEditor().getSubEditor().setValue((Network) nicWithNetworks.getSelectedItem());
        ((TakesConstrainedValueEditor<Network>) logicalNetworkEditor.asEditor().getSubEditor()).setAcceptableValues((Collection<Network>) nicWithNetworks.getItems());

        logicalNetworkEditor.setElementId(ElementIdUtils.createElementId(elementId, nicWithNetworks.getNetworkInterface().getName()));
    }

    @Override
    public NicWithLogicalNetworks getValue() {
        // flush
        Network network = logicalNetworkEditor.asEditor().getSubEditor().getValue();
        nicWithNetworks.getNetworkInterface().setNetworkName(network != null ? network.getName() : null);
        return nicWithNetworks;
    }

    @Override
    public TakesValueEditor<NicWithLogicalNetworks> asEditor() {
        return TakesValueEditor.of(this);
    }

}
