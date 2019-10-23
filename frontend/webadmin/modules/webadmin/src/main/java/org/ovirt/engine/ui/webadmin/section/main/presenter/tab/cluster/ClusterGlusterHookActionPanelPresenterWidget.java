package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGlusterHookListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class ClusterGlusterHookActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<Cluster, GlusterHookEntity, ClusterListModel<Void>, ClusterGlusterHookListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ClusterGlusterHookActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<Cluster, GlusterHookEntity> view,
            SearchableDetailModelProvider<GlusterHookEntity, ClusterListModel<Void>,
                ClusterGlusterHookListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Cluster, GlusterHookEntity>(constants.enableHook()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEnableHookCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Cluster, GlusterHookEntity>(constants.disableHook()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDisableHookCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Cluster, GlusterHookEntity>(constants.viewHookContent()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getViewHookCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Cluster, GlusterHookEntity>(constants.resolveConflictsGlusterHook()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getResolveConflictsCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Cluster, GlusterHookEntity>(constants.syncWithServersGlusterHook()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSyncWithServersCommand();
            }
        });
    }
}
