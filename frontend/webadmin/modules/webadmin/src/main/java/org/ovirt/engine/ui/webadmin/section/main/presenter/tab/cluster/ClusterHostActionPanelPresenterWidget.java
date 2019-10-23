package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class ClusterHostActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<Cluster, VDS, ClusterListModel<Void>, ClusterHostListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ClusterHostActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<Cluster, VDS> view,
            SearchableDetailModelProvider<VDS, ClusterListModel<Void>, ClusterHostListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Cluster, VDS>(constants.updateMomPolicyClusterHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getUpdateMomPolicyCommand();
            }
        });
    }

}
