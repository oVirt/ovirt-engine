package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.ClusterAffinityLabelListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterAffinityLabelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabAffinityLabelsView;

import com.google.gwt.core.client.GWT;

public class SubTabClusterAffinityLabelView extends AbstractSubTabAffinityLabelsView<Cluster, ClusterListModel<Void>, ClusterAffinityLabelListModel>
        implements SubTabClusterAffinityLabelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterAffinityLabelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabClusterAffinityLabelView(SearchableDetailModelProvider<Label, ClusterListModel<Void>, ClusterAffinityLabelListModel> modelProvider) {
        super(modelProvider);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
