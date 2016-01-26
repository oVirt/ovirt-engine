package org.ovirt.engine.ui.webadmin.section.main.view.popup.qos;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.HostNetworkQosParametersModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;

public class HostNetworkQosWidget extends QosWidget<HostNetworkQos, HostNetworkQosParametersModel> {

    interface Style extends CssResource {
        String valueBox();
    }

    interface Driver extends SimpleBeanEditorDriver<HostNetworkQosParametersModel, HostNetworkQosWidget> {
    }

    interface WidgetUiBinder extends UiBinder<FlowPanel, HostNetworkQosWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<HostNetworkQosWidget> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    @UiField
    @Ignore
    StringEntityModelLabelEditor qosOutLabel;

    @UiField
    @Path(value="outAverageLinkshare.entity")
    @WithElementId
    StringEntityModelTextBoxEditor outAverageLinkshare;

    @UiField
    @Path(value="outAverageUpperlimit.entity")
    @WithElementId
    StringEntityModelTextBoxEditor outAverageUpperlimit;

    @UiField
    @Path(value="outAverageRealtime.entity")
    @WithElementId
    StringEntityModelTextBoxEditor outAverageRealtime;

    @UiField
    Style style;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public HostNetworkQosWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);

        qosOutLabel.asValueBox().setVisible(false);
        outAverageLinkshare.addContentWidgetContainerStyleName(style.valueBox());
        outAverageUpperlimit.addContentWidgetContainerStyleName(style.valueBox());
        outAverageRealtime.addContentWidgetContainerStyleName(style.valueBox());

        qosOutLabel.setLabel(constants.hostNetworkQosOutLabel());
        outAverageLinkshare.setLabel(constants.hostNetworkQosPopupOutAverageLinkshare());
        outAverageUpperlimit.setLabel(constants.hostNetworkQosPopupOutAverageUpperlimit());
        outAverageRealtime.setLabel(constants.hostNetworkQosPopupOutAverageRealtime());

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
