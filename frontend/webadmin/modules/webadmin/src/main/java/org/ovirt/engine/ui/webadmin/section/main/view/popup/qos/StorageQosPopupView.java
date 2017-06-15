package org.ovirt.engine.ui.webadmin.section.main.view.popup.qos;

import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.QosModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.StorageQosParametersModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.StorageQosPopupPresenterWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class StorageQosPopupView extends QosPopupView<StorageQos, StorageQosParametersModel> implements StorageQosPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<QosModel<StorageQos, StorageQosParametersModel>, StorageQosPopupView> {
    }

    @Inject
    public StorageQosPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    protected void createQosWidget() {
        qosWidget = new StorageQosWidget();
    }

    @Override
    protected UiCommonEditorDriver<QosModel<StorageQos, StorageQosParametersModel>, QosPopupView<StorageQos, StorageQosParametersModel>> createDriver() {
        return GWT.create(Driver.class);
    }

}
