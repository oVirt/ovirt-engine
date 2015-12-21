package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DataCenterPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<DataCenterModel, DataCenterPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<DataCenterModel> {
    }

    @Inject
    public DataCenterPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
