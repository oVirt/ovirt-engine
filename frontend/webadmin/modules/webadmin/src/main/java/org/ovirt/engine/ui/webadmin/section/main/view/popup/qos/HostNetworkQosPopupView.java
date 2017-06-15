package org.ovirt.engine.ui.webadmin.section.main.view.popup.qos;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.HostNetworkQosParametersModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.QosModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.HostNetworkQosPopupPresenterWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostNetworkQosPopupView extends QosPopupView<HostNetworkQos, HostNetworkQosParametersModel> implements HostNetworkQosPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<QosModel<HostNetworkQos, HostNetworkQosParametersModel>, HostNetworkQosPopupView> {
    }

    @Inject
    public HostNetworkQosPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    protected void createQosWidget() {
        qosWidget = new HostNetworkQosWidget();
    }

    @Override
    protected UiCommonEditorDriver<QosModel<HostNetworkQos, HostNetworkQosParametersModel>, QosPopupView<HostNetworkQos, HostNetworkQosParametersModel>> createDriver() {
        return GWT.create(Driver.class);
    }

}
