package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.CpuQosParametersModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.QosModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class CpuQosPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<QosModel<CpuQos, CpuQosParametersModel>, CpuQosPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<QosModel<CpuQos, CpuQosParametersModel>> {
    }

    @Inject
    public CpuQosPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
