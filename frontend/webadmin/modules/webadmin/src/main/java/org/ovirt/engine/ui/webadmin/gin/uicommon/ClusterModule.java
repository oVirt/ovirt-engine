package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterPolicyModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterManageNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterNewNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPolicyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.GuidePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailTabModelProvider;

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
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new MainTabModelProvider<VDSGroup, ClusterListModel>(ginjector, ClusterListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getNewCommand()
                        || lastExecutedCommand == getModel().getEditCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getGuideCommand()) {
                    return guidePopupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<ClusterListModel, ClusterPolicyModel> getClusterPolicyProvider(ClientGinjector ginjector,
            final Provider<ClusterPolicyPopupPresenterWidget> popupProvider) {
        return new DetailTabModelProvider<ClusterListModel, ClusterPolicyModel>(ginjector,
                ClusterListModel.class,
                ClusterPolicyModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getEditCommand()) {
                    return popupProvider.get();
                }
                else {
                    return super.getModelPopup(lastExecutedCommand);
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
    public SearchableDetailModelProvider<network, ClusterListModel, ClusterNetworkListModel> getClusterNetworkListProvider(ClientGinjector ginjector,
            final Provider<ClusterNewNetworkPopupPresenterWidget> popupProvider,
            final Provider<ClusterManageNetworkPopupPresenterWidget> managePopupProvider) {
        return new SearchableDetailTabModelProvider<network, ClusterListModel, ClusterNetworkListModel>(ginjector,
                ClusterListModel.class,
                ClusterNetworkListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getNewNetworkCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getManageCommand()) {
                    return managePopupProvider.get();
                }
                else {
                    return super.getModelPopup(lastExecutedCommand);
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
    public SearchableDetailModelProvider<permissions, ClusterListModel, PermissionListModel> getPermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, ClusterListModel, PermissionListModel>(ginjector,
                ClusterListModel.class,
                PermissionListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                PermissionListModel model = getModel();

                if (lastExecutedCommand == model.getAddCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    @Override
    protected void configure() {
    }

}
