package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import java.util.List;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkManageModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterManageNetworkPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ListModel, ClusterManageNetworkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ListModel> {

        void setDisplayNetwork(ClusterNetworkManageModel displayNetwork);

    }

    @Inject
    public ClusterManageNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(ListModel model) {
        super.init(model);

        setDisplayNetwork((List<ClusterNetworkManageModel>) model.getItems());
    }

    void setDisplayNetwork(List<ClusterNetworkManageModel> items) {
        for (ClusterNetworkManageModel clusterNetworkManageModel : items) {
            if (clusterNetworkManageModel.isDisplayNetwork()) {
                getView().setDisplayNetwork(clusterNetworkManageModel);
            }
        }
    }

}
