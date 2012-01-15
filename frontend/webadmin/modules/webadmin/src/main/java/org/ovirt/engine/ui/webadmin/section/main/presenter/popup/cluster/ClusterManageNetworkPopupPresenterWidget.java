package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterManageNetworkPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ListModel, ClusterManageNetworkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ListModel> {
    }

    @Inject
    public ClusterManageNetworkPopupPresenterWidget(EventBus eventBus,
            ViewDef view) {
        super(eventBus, view);
    }

}
