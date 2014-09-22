package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.QosModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.StorageQosParametersModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class StorageQosPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<QosModel<StorageQos, StorageQosParametersModel>, StorageQosPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<QosModel<StorageQos, StorageQosParametersModel>> {
    }

    @Inject
    public StorageQosPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
