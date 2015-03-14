package org.ovirt.engine.ui.webadmin.section.main.view.popup.qos;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
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
    @Path(value="outAverageLinkshare.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor outAverageLinkshare;

    @UiField
    @Path(value="outAverageUpperlimit.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor outAverageUpperlimit;

    @UiField
    @Path(value="outAverageRealtime.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor outAverageRealtime;

    @UiField
    Style style;

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    public HostNetworkQosWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);

        outAverageLinkshare.addContentWidgetContainerStyleName(style.valueBox());
        outAverageUpperlimit.addContentWidgetContainerStyleName(style.valueBox());
        outAverageRealtime.addContentWidgetContainerStyleName(style.valueBox());

        outAverageLinkshare.setLabel(constants.hostNetworkQosPopupOutAverageLinkshare());
        outAverageUpperlimit.setLabel(constants.hostNetworkQosPopupOutAverageUpperlimit());
        outAverageRealtime.setLabel(constants.hostNetworkQosPopupOutAverageRealtime());

        driver = GWT.create(Driver.class);
        driver.initialize(this);
    }

}
