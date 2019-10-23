package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.PermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;

public class SubTabClusterPermissionView extends AbstractSubTabPermissionsView<Cluster, ClusterListModel<Void>>
        implements SubTabClusterPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabClusterPermissionView(SearchableDetailModelProvider<Permission, ClusterListModel<Void>,
            PermissionListModel<Cluster>> modelProvider, EventBus eventBus,
            PermissionActionPanelPresenterWidget<Cluster, ClusterListModel<Void>, PermissionListModel<Cluster>> actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, actionPanel);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
