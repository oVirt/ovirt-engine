package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminImageButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class ClusterActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, Cluster, ClusterListModel<Void>> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<Void, Cluster> newButtonDefinition;

    @Inject
    public ClusterActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, Cluster> view,
            MainModelProvider<Cluster, ClusterListModel<Void>> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<Void, Cluster>(constants.newCluster()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        };
        addActionButton(newButtonDefinition);
        addActionButton(new WebAdminButtonDefinition<Void, Cluster>(constants.editCluster()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<Void, Cluster>(constants.removeCluster()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        addDividerToKebab();

        addMenuListItem(new WebAdminImageButtonDefinition<Void, Cluster>(constants.guideMeCluster(), IconType.SUPPORT, true) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getGuideCommand();
            }
        });

        addMenuListItem(new WebAdminButtonDefinition<Void, Cluster>(constants.resetClusterEmulatedMachine()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getResetEmulatedMachineCommand();
            }
        });
    }

    public WebAdminButtonDefinition<Void, Cluster> getNewButtonDefinition() {
        return newButtonDefinition;
    }
}
