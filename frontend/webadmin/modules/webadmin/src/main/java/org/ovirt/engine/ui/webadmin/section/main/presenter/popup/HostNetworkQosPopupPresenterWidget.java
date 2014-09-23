package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.HostNetworkQosParametersModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.QosModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostNetworkQosPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<QosModel<HostNetworkQos, HostNetworkQosParametersModel>, HostNetworkQosPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<QosModel<HostNetworkQos, HostNetworkQosParametersModel>> {
    }

    @Inject
    public HostNetworkQosPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
