package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkManageModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterManageNetworkPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ClusterNetworkManageModel, ClusterManageNetworkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ClusterNetworkManageModel> {
    }

    @Inject
    public ClusterManageNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(ClusterNetworkManageModel model) {
        super.init(model);
    }
}
