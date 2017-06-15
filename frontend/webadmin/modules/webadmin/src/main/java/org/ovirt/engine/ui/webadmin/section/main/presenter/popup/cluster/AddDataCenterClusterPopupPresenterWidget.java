package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class AddDataCenterClusterPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget
    <ListModel<EntityModel<StoragePool>>, AddDataCenterClusterPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ListModel<EntityModel<StoragePool>>> {
    }

    @Inject
    public AddDataCenterClusterPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
