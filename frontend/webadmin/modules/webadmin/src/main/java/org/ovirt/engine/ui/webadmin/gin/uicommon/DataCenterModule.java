package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.ModelBoundPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RolePermissionsRemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.ReportCommand;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterIscsiBondListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkQoSListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterQuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterStorageQosListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ReportPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.NetworkQoSPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.StorageQosPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterForceRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.EditDataCenterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindMultiStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindSingleStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.IscsiBondPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.NewDataCenterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.RecoveryStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.GuidePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.macpool.SharedMacPoolPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.QuotaPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DataCenterModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<StoragePool, DataCenterListModel> getDataCenterListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<DataCenterPopupPresenterWidget> popupProvider,
            final Provider<GuidePopupPresenterWidget> guidePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<RecoveryStoragePopupPresenterWidget> recoveryStorageConfirmPopupProvider,
            final Provider<ReportPresenterWidget> reportWindowProvider,
            final Provider<DataCenterForceRemovePopupPresenterWidget> forceRemovePopupProvider,
            final Provider<SharedMacPoolPopupPresenterWidget> macPoolPopupProvider) {
        return new MainTabModelProvider<StoragePool, DataCenterListModel>(eventBus, defaultConfirmPopupProvider, DataCenterListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getNewCommand()
                        || lastExecutedCommand == getModel().getEditCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getGuideCommand()) {
                    return guidePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getAddMacPoolCommand()) {
                    return macPoolPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DataCenterListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getForceRemoveCommand()) {
                    return forceRemovePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getRecoveryStorageCommand()) {
                    return recoveryStorageConfirmPopupProvider.get();
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

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VDSGroup, DataCenterListModel, DataCenterClusterListModel> getDataCenterClusterListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VDSGroup, DataCenterListModel, DataCenterClusterListModel>(
                eventBus, defaultConfirmPopupProvider,
                DataCenterListModel.class,
                DataCenterClusterListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Network, DataCenterListModel, DataCenterNetworkListModel> getDataCenterNetworkListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<NewDataCenterNetworkPopupPresenterWidget> newNetworkPopupProvider,
            final Provider<EditDataCenterNetworkPopupPresenterWidget> editNetworkPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<Network, DataCenterListModel, DataCenterNetworkListModel>(
                eventBus, defaultConfirmPopupProvider,
                DataCenterListModel.class,
                DataCenterNetworkListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterNetworkListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getNewCommand()) {
                    return newNetworkPopupProvider.get();
                }else if (lastExecutedCommand == getModel().getEditCommand()) {
                    return editNetworkPopupProvider.get();
                }else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DataCenterNetworkListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()
                        || lastExecutedCommand.getName().equals("Apply")) { //$NON-NLS-1$
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<StorageDomain, DataCenterListModel, DataCenterStorageListModel> getDataCenterStorageListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<FindSingleStoragePopupPresenterWidget> singlePopupProvider,
            final Provider<FindMultiStoragePopupPresenterWidget> multiPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<StorageDomain, DataCenterListModel, DataCenterStorageListModel>(
                eventBus, defaultConfirmPopupProvider,
                DataCenterListModel.class,
                DataCenterStorageListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterStorageListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                DataCenterStorageListModel model = getModel();

                if (lastExecutedCommand == model.getAttachStorageCommand()) {
                    return multiPopupProvider.get();
                } else if (lastExecutedCommand == model.getAttachISOCommand()
                        || lastExecutedCommand == model.getAttachBackupCommand()) {
                    return singlePopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DataCenterStorageListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getDetachCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Quota, DataCenterListModel, DataCenterQuotaListModel> getDataCenterQuotaListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<QuotaPopupPresenterWidget> quotaPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<Quota, DataCenterListModel, DataCenterQuotaListModel>(
                eventBus, defaultConfirmPopupProvider,
                DataCenterListModel.class,
                DataCenterQuotaListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterQuotaListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand.equals(getModel().getCreateCommand())
                        || lastExecutedCommand.equals(getModel().getEditCommand())
                        || lastExecutedCommand.equals(getModel().getCloneCommand())) {
                    return quotaPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DataCenterQuotaListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand.equals(getModel().getRemoveCommand())) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel> getDataCenterNetworkQoSListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<NetworkQoSPopupPresenterWidget> networkQoSPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel>(
                eventBus, defaultConfirmPopupProvider,
                DataCenterListModel.class,
                DataCenterNetworkQoSListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterNetworkQoSListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand.equals(getModel().getNewCommand())
                        || lastExecutedCommand.equals(getModel().getEditCommand())) {
                    return networkQoSPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DataCenterNetworkQoSListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand.equals(getModel().getRemoveCommand())) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<StorageQos, DataCenterListModel, DataCenterStorageQosListModel> getDataCenterStorageQosListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<StorageQosPopupPresenterWidget> storageQosPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<StorageQos, DataCenterListModel, DataCenterStorageQosListModel>(
                eventBus, defaultConfirmPopupProvider,
                DataCenterListModel.class,
                DataCenterStorageQosListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterStorageQosListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand.equals(getModel().getNewCommand())
                        || lastExecutedCommand.equals(getModel().getEditCommand())) {
                    return storageQosPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DataCenterStorageQosListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand.equals(getModel().getRemoveCommand())) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Permissions, DataCenterListModel, PermissionListModel> getPermissionListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<Permissions, DataCenterListModel, PermissionListModel>(
                eventBus, defaultConfirmPopupProvider,
                DataCenterListModel.class,
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

    @Provides
    @Singleton
    public SearchableDetailModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel> getDataCenterEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> eventPopupProvider) {
        return new SearchableDetailTabModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel>(
                eventBus, defaultConfirmPopupProvider,
                DataCenterListModel.class,
                DataCenterEventListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterEventListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand.equals(getModel().getDetailsCommand())) {
                    return eventPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<IscsiBond, DataCenterListModel, DataCenterIscsiBondListModel> getDataCenterIscsiBondListProvider(
            EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<IscsiBondPopupPresenterWidget> iscsiBondPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {

        return new SearchableDetailTabModelProvider<IscsiBond, DataCenterListModel, DataCenterIscsiBondListModel>(
                eventBus, defaultConfirmPopupProvider, DataCenterListModel.class, DataCenterIscsiBondListModel.class) {

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterIscsiBondListModel source,
                                                                                            UICommand lastExecutedCommand,
                                                                                            Model windowModel) {
                if (lastExecutedCommand == getModel().getAddCommand() ||
                        lastExecutedCommand == getModel().getEditCommand()) {
                    return iscsiBondPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DataCenterIscsiBondListModel source,
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
