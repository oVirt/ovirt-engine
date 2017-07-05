package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.CpuProfilesActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterCpuProfilePresenter;
import org.ovirt.engine.ui.webadmin.uicommon.model.CpuProfilePermissionModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabClusterCpuProfileView extends AbstractSubTabTableWidgetView<Cluster, CpuProfile, ClusterListModel<Void>, CpuProfileListModel>
        implements SubTabClusterCpuProfilePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterCpuProfileView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabClusterCpuProfileView(SearchableDetailModelProvider<CpuProfile, ClusterListModel<Void>, CpuProfileListModel> modelProvider,
            CpuProfilePermissionModelProvider permissionModelProvider,
            CpuProfilesActionPanelPresenterWidget actionPanel,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(new CpuProfilesListModelTable(modelProvider,
                permissionModelProvider,
                eventBus,
                actionPanel,
                clientStorage));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

    @Override
    public void addModelListeners() {
        getModelBoundTableWidget().addModelListeners();
    }
}
