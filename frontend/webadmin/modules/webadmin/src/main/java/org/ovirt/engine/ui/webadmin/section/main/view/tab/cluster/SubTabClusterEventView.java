package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabEventView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;

public class SubTabClusterEventView extends AbstractSubTabEventView<Cluster, ClusterListModel<Void>, ClusterEventListModel>
        implements SubTabClusterEventPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterEventView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabClusterEventView(SearchableDetailModelProvider<AuditLog, ClusterListModel<Void>, ClusterEventListModel> modelProvider,
                                  EventBus eventBus,
                                  ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
