package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainViewModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
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
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterCpuQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterHostNetworkQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterStorageQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.NewHostNetworkQosModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.CpuQosPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.HostNetworkQosPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.NetworkQoSPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.StorageQosPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.StorageQosRemovePopupPresenterWidget;
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
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.QuotaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterMainSelectedItems;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

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
            final Provider<DataCenterForceRemovePopupPresenterWidget> forceRemovePopupProvider,
            final Provider<DataCenterListModel> modelProvider) {
        MainViewModelProvider<StoragePool, DataCenterListModel> result =
                new MainViewModelProvider<StoragePool, DataCenterListModel>(eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getNewCommand()
                                || lastExecutedCommand == getModel().getEditCommand()) {
                            return popupProvider.get();
                        } else if (lastExecutedCommand == getModel().getGuideCommand()) {
                            return guidePopupProvider.get();
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
                };
        result.setModelProvider(modelProvider);
        return result;
    }

    // Form Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Network, DataCenterListModel, DataCenterNetworkListModel> getDataCenterNetworkListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<NewDataCenterNetworkPopupPresenterWidget> newNetworkPopupProvider,
            final Provider<EditDataCenterNetworkPopupPresenterWidget> editNetworkPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<DataCenterListModel> mainModelProvider,
            final Provider<DataCenterNetworkListModel> modelProvider,
            final Provider<HostNetworkQosPopupPresenterWidget> addQosPopupProvider) {
        SearchableDetailTabModelProvider<Network, DataCenterListModel, DataCenterNetworkListModel> result =
                new SearchableDetailTabModelProvider<Network, DataCenterListModel, DataCenterNetworkListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterNetworkListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {

                        if (windowModel instanceof NewHostNetworkQosModel) {
                            return addQosPopupProvider.get();
                        }

                        if (lastExecutedCommand == getModel().getNewCommand()) {
                            return newNetworkPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getEditCommand()) {
                            return editNetworkPopupProvider.get();
                        } else {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    // Search-able Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<StorageDomain, DataCenterListModel, DataCenterStorageListModel> getDataCenterStorageListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<FindSingleStoragePopupPresenterWidget> singlePopupProvider,
            final Provider<FindMultiStoragePopupPresenterWidget> multiPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<DataCenterListModel> mainModelProvider,
            final Provider<DataCenterStorageListModel> modelProvider) {
        SearchableDetailTabModelProvider<StorageDomain, DataCenterListModel, DataCenterStorageListModel> result =
                new SearchableDetailTabModelProvider<StorageDomain, DataCenterListModel, DataCenterStorageListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterStorageListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
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
                        if (lastExecutedCommand == getModel().getDetachCommand() ||
                                lastExecutedCommand.getName().equals("OnAttach")) { //$NON-NLS-1$ ) {
                            return removeConfirmPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getMaintenanceCommand() ||
                                lastExecutedCommand.getName().equals("OnMaintenance")) { //$NON-NLS-1$) {
                            return removeConfirmPopupProvider.get();
                        } else {
                            return super.getConfirmModelPopup(source, lastExecutedCommand);
                        }
                    }
                };
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Quota, DataCenterListModel, DataCenterQuotaListModel> getDataCenterQuotaListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<QuotaPopupPresenterWidget> quotaPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<DataCenterListModel> mainModelProvider,
            final Provider<DataCenterQuotaListModel> modelProvider) {

        SearchableDetailTabModelProvider<Quota, DataCenterListModel, DataCenterQuotaListModel> result =
                new SearchableDetailTabModelProvider<Quota, DataCenterListModel, DataCenterQuotaListModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel> getDataCenterNetworkQoSListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<NetworkQoSPopupPresenterWidget> networkQoSPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<DataCenterListModel> mainModelProvider,
            final Provider<DataCenterNetworkQoSListModel> modelProvider) {

        SearchableDetailTabModelProvider<NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel> result =
                new SearchableDetailTabModelProvider<NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<HostNetworkQos, DataCenterListModel, DataCenterHostNetworkQosListModel> getDataCenterHostNetworkQosListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<HostNetworkQosPopupPresenterWidget> hostNetworkQosPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<DataCenterListModel> mainModelProvider,
            final Provider<DataCenterHostNetworkQosListModel> modelProvider) {
        SearchableDetailTabModelProvider<HostNetworkQos, DataCenterListModel, DataCenterHostNetworkQosListModel> result =
                new SearchableDetailTabModelProvider<HostNetworkQos, DataCenterListModel, DataCenterHostNetworkQosListModel>(eventBus,
                        defaultConfirmPopupProvider) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterHostNetworkQosListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand.equals(getModel().getNewCommand())
                        || lastExecutedCommand.equals(getModel().getEditCommand())) {
                    return hostNetworkQosPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DataCenterHostNetworkQosListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand.equals(getModel().getRemoveCommand())) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<StorageQos, DataCenterListModel, DataCenterStorageQosListModel> getDataCenterStorageQosListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<StorageQosPopupPresenterWidget> storageQosPopupProvider,
            final Provider<StorageQosRemovePopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<DataCenterListModel> mainModelProvider,
            final Provider<DataCenterStorageQosListModel> modelProvider) {

        SearchableDetailTabModelProvider<StorageQos, DataCenterListModel, DataCenterStorageQosListModel> result =
                new SearchableDetailTabModelProvider<StorageQos, DataCenterListModel, DataCenterStorageQosListModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<CpuQos, DataCenterListModel, DataCenterCpuQosListModel> getDataCenterCpuQosListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<CpuQosPopupPresenterWidget> cpuQosPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<DataCenterListModel> mainModelProvider,
            final Provider<DataCenterCpuQosListModel> modelProvider) {
        SearchableDetailTabModelProvider<CpuQos, DataCenterListModel, DataCenterCpuQosListModel> result =
                new SearchableDetailTabModelProvider<CpuQos, DataCenterListModel, DataCenterCpuQosListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DataCenterCpuQosListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand.equals(getModel().getNewCommand())
                                || lastExecutedCommand.equals(getModel().getEditCommand())) {
                            return cpuQosPopupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DataCenterCpuQosListModel source,
                            UICommand lastExecutedCommand) {
                        if (lastExecutedCommand.equals(getModel().getRemoveCommand())) {
                            return removeConfirmPopupProvider.get();
                        } else {
                            return super.getConfirmModelPopup(source, lastExecutedCommand);
                        }
                    }
                };
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel> getDataCenterEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> eventPopupProvider,
            final Provider<DataCenterListModel> mainModelProvider,
            final Provider<DataCenterEventListModel> modelProvider) {

        SearchableDetailTabModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel> result =
                new SearchableDetailTabModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<IscsiBond, DataCenterListModel, DataCenterIscsiBondListModel> getDataCenterIscsiBondListProvider(
            EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<IscsiBondPopupPresenterWidget> iscsiBondPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<DataCenterListModel> mainModelProvider,
            final Provider<DataCenterIscsiBondListModel> modelProvider) {

        SearchableDetailTabModelProvider<IscsiBond, DataCenterListModel, DataCenterIscsiBondListModel> result =
                new SearchableDetailTabModelProvider<IscsiBond, DataCenterListModel, DataCenterIscsiBondListModel>(
                        eventBus, defaultConfirmPopupProvider) {

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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Override
    protected void configure() {
        bind(DataCenterListModel.class).in(Singleton.class);
        bind(DataCenterClusterListModel.class).in(Singleton.class);
        bind(DataCenterNetworkListModel.class).in(Singleton.class);
        bind(DataCenterStorageListModel.class).in(Singleton.class);
        bind(DataCenterQuotaListModel.class).in(Singleton.class);
        bind(DataCenterNetworkQoSListModel.class).in(Singleton.class);
        bind(DataCenterHostNetworkQosListModel.class).in(Singleton.class);
        bind(DataCenterEventListModel.class).in(Singleton.class);
        bind(DataCenterIscsiBondListModel.class).in(Singleton.class);
        bind(DataCenterStorageQosListModel.class).in(Singleton.class);
        bind(DataCenterCpuQosListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<StoragePool>>(){}).in(Singleton.class);
        bind(DataCenterMainSelectedItems.class).asEagerSingleton();

        // Search-able Detail Models
        bind(new TypeLiteral<SearchableDetailModelProvider<Cluster, DataCenterListModel, DataCenterClusterListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<Cluster, DataCenterListModel, DataCenterClusterListModel>>(){})
            .in(Singleton.class);
        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, DataCenterListModel, PermissionListModel<StoragePool>>>(){})
            .to(new TypeLiteral<PermissionModelProvider<StoragePool, DataCenterListModel>>(){}).in(Singleton.class);
    }
}
