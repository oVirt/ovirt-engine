package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.widget.uicommon.NetworkFilterParameterEditor;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.NetworkFilterParameterModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;


public class NetworkFilterParameterWidget extends AddRemoveRowWidget<ListModel<NetworkFilterParameterModel>, NetworkFilterParameterModel, NetworkFilterParameterEditor> {

    interface WidgetUiBinder extends UiBinder<Widget, NetworkFilterParameterWidget> {
        WidgetUiBinder binder = GWT.create(WidgetUiBinder.class);
    }

    public NetworkFilterParameterWidget() {
        initWidget(WidgetUiBinder.binder.createAndBindUi(this));
    }

    @Override
    protected NetworkFilterParameterEditor createWidget(NetworkFilterParameterModel value) {
        NetworkFilterParameterEditor editor = new NetworkFilterParameterEditor();
        editor.edit(value);
        return editor;
    }


    @Override
    protected NetworkFilterParameterModel createGhostValue() {
        return new NetworkFilterParameterModel();
    }

    @Override
    protected boolean isGhost(NetworkFilterParameterModel value) {
        String name = value.getName().getEntity();
        return name == null || name.isEmpty();
    }
}
