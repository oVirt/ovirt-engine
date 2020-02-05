package org.ovirt.engine.ui.webadmin.section.main.view.popup.qos;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.HostNetworkQosParametersModel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;

public class HostNetworkQosWidget extends QosWidget<HostNetworkQos, HostNetworkQosParametersModel> {

    interface Driver extends UiCommonEditorDriver<HostNetworkQosParametersModel, HostNetworkQosWidget> {
    }

    interface WidgetUiBinder extends UiBinder<FlowPanel, HostNetworkQosWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<HostNetworkQosWidget> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    @UiField
    @Ignore
    EnableableFormLabel qosOutLabel;

    @UiField(provided = true)
    @Path(value="outAverageLinkshare.entity")
    @WithElementId
    StringEntityModelTextBoxEditor outAverageLinkshare;

    @UiField(provided = true)
    @Path(value="outAverageUpperlimit.entity")
    @WithElementId
    StringEntityModelTextBoxEditor outAverageUpperlimit;

    @UiField(provided = true)
    @Path(value="outAverageRealtime.entity")
    @WithElementId
    StringEntityModelTextBoxEditor outAverageRealtime;

    public HostNetworkQosWidget() {
        outAverageLinkshare = StringEntityModelTextBoxEditor.newTrimmingEditor();
        outAverageUpperlimit = StringEntityModelTextBoxEditor.newTrimmingEditor();
        outAverageRealtime = StringEntityModelTextBoxEditor.newTrimmingEditor();

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);

        driver = GWT.create(Driver.class);
        driver.initialize(this);
    }

    @Override
    protected void updateChangeability(boolean enabled) {
        super.updateChangeability(enabled);
        qosOutLabel.setEnabled(enabled);
        outAverageLinkshare.setEnabled(enabled);
        outAverageUpperlimit.setEnabled(enabled);
        outAverageRealtime.setEnabled(enabled);
    }
}
