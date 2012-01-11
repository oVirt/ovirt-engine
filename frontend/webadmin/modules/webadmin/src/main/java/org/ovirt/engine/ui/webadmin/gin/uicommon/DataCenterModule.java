package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterStorageListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindMultiStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindSingleStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.GuidePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailTabModelProvider;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DataCenterModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<storage_pool, DataCenterListModel> getDataCenterListProvider(ClientGinjector ginjector,
            final Provider<DataCenterPopupPresenterWidget> popupProvider,
            final Provider<GuidePopupPresenterWidget> guidePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new MainTabModelProvider<storage_pool, DataCenterListModel>(ginjector, DataCenterListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getNewCommand()
                        || lastExecutedCommand == getModel().getEditCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getGuideCommand()) {
                    return guidePopupProvider.get();
                }
                else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()
                        || lastExecutedCommand == getModel().getForceRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    // Form Detail Models

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VDSGroup, DataCenterListModel, DataCenterClusterListModel> getDataCenterClusterListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VDSGroup, DataCenterListModel, DataCenterClusterListModel>(ginjector,
                DataCenterListModel.class,
                DataCenterClusterListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<network, DataCenterListModel, DataCenterNetworkListModel> getDataCenterNetworkListProvider(ClientGinjector ginjector,
            final Provider<DataCenterNetworkPopupPresenterWidget> networkPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<network, DataCenterListModel, DataCenterNetworkListModel>(ginjector,
                DataCenterListModel.class,
                DataCenterNetworkListModel.class) {

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getEditCommand()
                        || lastExecutedCommand == getModel().getNewCommand()) {
                    return networkPopupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()
                        || lastExecutedCommand.getName().equals("DetachClusters")) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<storage_domains, DataCenterListModel, DataCenterStorageListModel> getDataCenterStorageListProvider(ClientGinjector ginjector,
            final Provider<FindSingleStoragePopupPresenterWidget> singlePopupProvider,
            final Provider<FindMultiStoragePopupPresenterWidget> multiPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<storage_domains, DataCenterListModel, DataCenterStorageListModel>(ginjector,
                DataCenterListModel.class,
                DataCenterStorageListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                DataCenterStorageListModel model = getModel();
                if (lastExecutedCommand == model.getAttachStorageCommand()) {
                    return multiPopupProvider.get();
                }
                if (lastExecutedCommand == model.getAttachISOCommand()
                        || lastExecutedCommand == model.getAttachBackupCommand()) {
                    return singlePopupProvider.get();

                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getDetachCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }

        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<permissions, DataCenterListModel, PermissionListModel> getPermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, DataCenterListModel, PermissionListModel>(ginjector,
                DataCenterListModel.class,
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

    @Provides
    @Singleton
    public SearchableDetailModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel> getDataCenterEventListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel>(ginjector,
                DataCenterListModel.class,
                DataCenterEventListModel.class);
    }

    @Override
    protected void configure() {
    }

}
