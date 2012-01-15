package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterPolicyModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterPolicyPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ClusterPolicyModel, ClusterPolicyPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ClusterPolicyModel> {
    }

    private final ClusterListModel clusterListModel;

    @Inject
    public ClusterPolicyPopupPresenterWidget(EventBus eventBus, ViewDef view, ClusterListModel clusterListModel) {
        super(eventBus, view);
        this.clusterListModel = clusterListModel;
    }

    @Override
    protected void onHide() {
        super.onHide();
        clusterListModel.ForceRefresh();
    }

}
