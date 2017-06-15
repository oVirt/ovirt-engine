package org.ovirt.engine.ui.webadmin.section.main.view.popup.qos;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.CpuQosParametersModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.QosModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.CpuQosPopupPresenterWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class CpuQosPopupView extends QosPopupView<CpuQos, CpuQosParametersModel> implements CpuQosPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<QosModel<CpuQos, CpuQosParametersModel>, CpuQosPopupView> {
    }

    @Inject
    public CpuQosPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    protected void createQosWidget() {
        qosWidget = new CpuQosWidget();
    }

    @Override
    protected UiCommonEditorDriver<QosModel<CpuQos, CpuQosParametersModel>, QosPopupView<CpuQos, CpuQosParametersModel>> createDriver() {
        return GWT.create(Driver.class);
    }

}
