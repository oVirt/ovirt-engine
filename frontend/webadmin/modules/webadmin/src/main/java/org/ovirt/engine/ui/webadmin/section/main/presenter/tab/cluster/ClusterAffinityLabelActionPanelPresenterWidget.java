package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.ClusterAffinityLabelListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.AffinityLabelsActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class ClusterAffinityLabelActionPanelPresenterWidget
    extends AffinityLabelsActionPanelPresenterWidget<Cluster, ClusterListModel<Void>, ClusterAffinityLabelListModel> {

    @Inject
    public ClusterAffinityLabelActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<Cluster, Label> view,
            SearchableDetailModelProvider<Label, ClusterListModel<Void>, ClusterAffinityLabelListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        super.initializeButtons();
        addActionButton(new WebAdminButtonDefinition<Cluster, Label>(constants.affinityLabelsSubTabDeleteButton()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }
}
