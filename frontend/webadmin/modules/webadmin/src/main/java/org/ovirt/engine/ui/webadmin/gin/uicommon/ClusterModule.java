package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.ModelBoundPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.ReportCommand;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGlusterHookListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterServiceModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ReportPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterManageNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.GlusterHookContentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.GlusterHookResolveConflictsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ManageGlusterSwiftPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.NewClusterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.DetachGlusterHostsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.GuidePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.MultipleHostsPopupPresenterWidget;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ClusterModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<VDSGroup, ClusterListModel> getClusterListProvider(ClientGinjector ginjector,
            final Provider<ClusterPopupPresenterWidget> popupProvider,
            final Provider<GuidePopupPresenterWidget> guidePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ReportPresenterWidget> reportWindowProvider,
            final Provider<MultipleHostsPopupPresenterWidget> addMultipleHostsPopupProvider) {
        return new MainTabModelProvider<VDSGroup, ClusterListModel>(ginjector, ClusterListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ClusterListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getNewCommand()
                        || lastExecutedCommand == getModel().getEditCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getGuideCommand()) {
                    return guidePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getAddMultipleHostsCommand()) {
                    return addMultipleHostsPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(ClusterListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }

            @Override
            protected ModelBoundPresenterWidget<? extends Model> getModelBoundWidget(UICommand lastExecutedCommand) {
                if (lastExecutedCommand instanceof ReportCommand) {
                    return reportWindowProvider.get();
                } else {
                    return super.getModelBoundWidget(lastExecutedCommand);
                }
            }
        };
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<ClusterListModel, ClusterGeneralModel> getClusterGeneralProvider(ClientGinjector ginjector,
            final Provider<MultipleHostsPopupPresenterWidget> multipleHostsProvider,
            final Provider<DetachGlusterHostsPopupPresenterWidget> detachHostsProvider,
            final Provider<ManageGlusterSwiftPopupPresenterWidget> manageGlusterSwiftProvider) {
        return new DetailTabModelProvider<ClusterListModel, ClusterGeneralModel>(ginjector,
                ClusterListModel.class,
                ClusterGeneralModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ClusterGeneralModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getImportNewGlusterHostsCommand()) {
                    return multipleHostsProvider.get();
                }
                else if (lastExecutedCommand == getModel().getDetachNewGlusterHostsCommand()) {
                    return detachHostsProvider.get();
                }
                else if (lastExecutedCommand == getModel().getManageGlusterSwiftCommand()) {
                    return manageGlusterSwiftProvider.get();
                }
                else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }
        };
    }

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VDS, ClusterListModel, ClusterHostListModel> getClusterHostListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VDS, ClusterListModel, ClusterHostListModel>(ginjector,
                ClusterListModel.class,
                ClusterHostListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Network, ClusterListModel, ClusterNetworkListModel> getClusterNetworkListProvider(ClientGinjector ginjector,
            final Provider<NewClusterNetworkPopupPresenterWidget> popupProvider,
            final Provider<ClusterManageNetworkPopupPresenterWidget> managePopupProvider) {
        return new SearchableDetailTabModelProvider<Network, ClusterListModel, ClusterNetworkListModel>(ginjector,
                ClusterListModel.class,
                ClusterNetworkListModel.class) {

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ClusterNetworkListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand == getModel().getNewNetworkCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getManageCommand()) {
                    return managePopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, ClusterListModel, ClusterVmListModel> getClusterVmListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VM, ClusterListModel, ClusterVmListModel>(ginjector,
                ClusterListModel.class,
                ClusterVmListModel.class);
    }

    @Provides
    @Singleton
    public DetailModelProvider<ClusterListModel, ClusterServiceModel> getClusterServiceProvider(ClientGinjector ginjector) {
        return new DetailTabModelProvider<ClusterListModel, ClusterServiceModel>(ginjector,
                ClusterListModel.class,
                ClusterServiceModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<GlusterHookEntity, ClusterListModel, ClusterGlusterHookListModel> getClusterGlusterHookListProvider(ClientGinjector ginjector,
            final Provider<DefaultConfirmationPopupPresenterWidget> confirmPopupProvider,
            final Provider<GlusterHookContentPopupPresenterWidget> contentPopupProvider,
            final Provider<GlusterHookResolveConflictsPopupPresenterWidget> resolveConflictsPopupProvider) {
        return new SearchableDetailTabModelProvider<GlusterHookEntity, ClusterListModel, ClusterGlusterHookListModel>(ginjector,
                ClusterListModel.class,
                ClusterGlusterHookListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup( ClusterGlusterHookListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getViewHookCommand()) {
                    return contentPopupProvider.get();
                }
                else if (lastExecutedCommand == getModel().getResolveConflictsCommand()) {
                    return resolveConflictsPopupProvider.get();
                }
                else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(ClusterGlusterHookListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getDisableHookCommand()) {
                    return confirmPopupProvider.get();
                }
                else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<permissions, ClusterListModel, PermissionListModel> getPermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, ClusterListModel, PermissionListModel>(ginjector,
                ClusterListModel.class,
                PermissionListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(PermissionListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getAddCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(PermissionListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Override
    protected void configure() {
    }

}
