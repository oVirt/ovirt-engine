package org.ovirt.engine.ui.webadmin.gin.uicommon;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.HostMaintenanceConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.HostRestartConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.HostUpgradePopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.numa.NumaSupportPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainViewModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.HostErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.HostErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.HostAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterStorageDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterSwiftListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostBricksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHardwareGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHooksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NetworkAttachmentModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.SetupNetworksBondModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.SetupNetworksLabelModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsConfigModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.HostDeviceListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AssignTagsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.HostErrataListWithDetailsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.CreateBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ConfigureLocalStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInstallPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostSetupNetworksPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ManualFencePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.NetworkAttachmentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksBondPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.VfsConfigPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.label.AffinityLabelPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.SetupNetworksLabelPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.FenceAgentModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.FenceProxyModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

/**
 * Gin configuration module for Hosts tabs and popups.
 */
public class HostModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<VDS, HostListModel<Void>> getHostListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<HostPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<HostMaintenanceConfirmationPopupPresenterWidget> hostMaintenanceConfirmationPopupProvider,
            final Provider<ManualFencePopupPresenterWidget> manualFenceConfirmPopupProvider,
            final Provider<AssignTagsPopupPresenterWidget> assignTagsPopupProvider,
            final Provider<ConfigureLocalStoragePopupPresenterWidget> configureLocalStoragePopupProvider,
            final Provider<HostInstallPopupPresenterWidget> installPopupProvider,
            final Provider<NumaSupportPopupPresenterWidget> numaSupportPopupProvider,
            final Provider<HostListModel<Void>> modelProvider,
            final Provider<HostUpgradePopupPresenterWidget> hostUpgradePopupPresenterWidgetProvider,
            final Provider<HostRestartConfirmationPopupPresenterWidget> hostRestartPopupPresenterWidgetProvider) {
        MainViewModelProvider<VDS, HostListModel<Void>> result =
                new MainViewModelProvider<VDS, HostListModel<Void>>(eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(HostListModel<Void> source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getNewCommand()
                                || lastExecutedCommand == getModel().getEditCommand()
                                || lastExecutedCommand == getModel().getEditWithPMemphasisCommand()
                                || lastExecutedCommand == getModel().getApproveCommand()) {
                            return popupProvider.get();
                        } else if (lastExecutedCommand == getModel().getInstallCommand()
                                || lastExecutedCommand == getModel().getUpgradeCommand()) {
                            return installPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getAssignTagsCommand()) {
                            return assignTagsPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getConfigureLocalStorageCommand()) {
                            return configureLocalStoragePopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getNumaSupportCommand()) {
                            return numaSupportPopupProvider.get();
                        }
                        return super.getModelPopup(source, lastExecutedCommand, windowModel);
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(HostListModel<Void> source,
                            UICommand lastExecutedCommand) {
                        if (lastExecutedCommand == getModel().getRemoveCommand()) {
                            return removeConfirmPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getMaintenanceCommand()) {
                            return hostMaintenanceConfirmationPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getManualFenceCommand()) {
                            return manualFenceConfirmPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getUpgradeCommand()) {
                            return hostUpgradePopupPresenterWidgetProvider.get();
                        } else if (lastExecutedCommand == getModel().getRestartCommand()) {
                            return hostRestartPopupPresenterWidgetProvider.get();
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
    public DetailModelProvider<HostListModel<Void>, HostGeneralModel> getHostGeneralProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<HostListModel<Void>> mainModelProvider,
            final Provider<HostGeneralModel> modelProvider,
            final Provider<HostInstallPopupPresenterWidget> installPopupProvider,
            final Provider<HostUpgradePopupPresenterWidget> hostUpgradePopupPresenterWidgetProvider) {
        DetailTabModelProvider<HostListModel<Void>, HostGeneralModel> result =
                new DetailTabModelProvider<HostListModel<Void>, HostGeneralModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(HostGeneralModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getUpgradeHostCommand()) {
                            return installPopupProvider.get();
                        }

                        return super.getModelPopup(source, lastExecutedCommand, windowModel);
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(HostGeneralModel source,
                            UICommand lastExecutedCommand) {
                        if (lastExecutedCommand == getModel().getUpgradeHostCommand()) {
                            return hostUpgradePopupPresenterWidgetProvider.get();
                        }
                        return super.getConfirmModelPopup(source, lastExecutedCommand);
                    }
                };
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    // Search-able Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<HostInterfaceLineModel, HostListModel<Void>, HostInterfaceListModel> getHostInterfaceListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<NetworkAttachmentPopupPresenterWidget> setupNetworksInterfacePopupProvider,
            final Provider<SetupNetworksBondPopupPresenterWidget> setupNetworksBondPopupProvider,
            final Provider<VfsConfigPopupPresenterWidget> vfsConfigPopupProvider,
            final Provider<SetupNetworksLabelPopupPresenterWidget> setupNetworksLabelPopupProvider,
            final Provider<HostSetupNetworksPopupPresenterWidget> hostSetupNetworksPopupProvider,
            final Provider<HostListModel<Void>> mainModelProvider,
            final Provider<HostInterfaceListModel> modelProvider) {
        SearchableDetailTabModelProvider<HostInterfaceLineModel, HostListModel<Void>, HostInterfaceListModel> result =
                new SearchableDetailTabModelProvider<HostInterfaceLineModel, HostListModel<Void>, HostInterfaceListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(HostInterfaceListModel source,
                            UICommand lastExecutedCommand) {
                        return super.getConfirmModelPopup(source, lastExecutedCommand);
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(HostInterfaceListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (source.getWindow() instanceof HostSetupNetworksModel) {
                            // Resolve by dialog model
                            if (windowModel instanceof SetupNetworksBondModel) {
                                return setupNetworksBondPopupProvider.get();
                            } else if (windowModel instanceof NetworkAttachmentModel) {
                                return setupNetworksInterfacePopupProvider.get();
                            } else if (windowModel instanceof VfsConfigModel) {
                                return vfsConfigPopupProvider.get();
                            } else if (windowModel instanceof SetupNetworksLabelModel) {
                                return setupNetworksLabelPopupProvider.get();
                            }
                        }

                        // Resolve by last executed command
                        if (lastExecutedCommand == getModel().getSetupNetworksCommand()) {
                            return hostSetupNetworksPopupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    protected void updateData() {
                        // Pass empty data to data provider, since Host NIC table is used as header-only table
                        updateDataProvider(new ArrayList<HostInterfaceLineModel>());
                    }
                };
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, HostListModel<Void>, HostVmListModel> getHostVmListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<HostListModel<Void>> mainModelProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<HostVmListModel> modelProvider) {
        SearchableDetailTabModelProvider<VM, HostListModel<Void>, HostVmListModel> result =
                new SearchableDetailTabModelProvider<VM, HostListModel<Void>, HostVmListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(
                            HostVmListModel sourceModel,
                            UICommand lastExecutedCommand) {
                        if (lastExecutedCommand == getModel().getStopCommand() ||
                                lastExecutedCommand == getModel().getShutdownCommand()) {
                            return removeConfirmPopupProvider.get();
                        }
                        return super.getConfirmModelPopup(sourceModel, lastExecutedCommand);
                    }
                };
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<StorageDevice, HostListModel<Void>, HostGlusterStorageDevicesListModel> getGlusterHostStorageDeviceProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<CreateBrickPopupPresenterWidget> createBrickPopupProvider,
            final Provider<HostListModel<Void>> mainModelProvider,
            final Provider<HostGlusterStorageDevicesListModel> modelProvider) {
        SearchableDetailTabModelProvider<StorageDevice, HostListModel<Void>, HostGlusterStorageDevicesListModel> result =
                new SearchableDetailTabModelProvider<StorageDevice, HostListModel<Void>, HostGlusterStorageDevicesListModel>(
                eventBus, defaultConfirmPopupProvider) {
            @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(HostGlusterStorageDevicesListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand == getModel().getCreateBrickCommand()) {
                    return createBrickPopupProvider.get();
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
    public SearchableDetailModelProvider<AuditLog, HostListModel<Void>, HostEventListModel> getHostEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> eventPopupProvider,
            final Provider<HostListModel<Void>> mainModelProvider,
            final Provider<HostEventListModel> modelProvider) {
        SearchableDetailTabModelProvider<AuditLog, HostListModel<Void>, HostEventListModel> result =
                new SearchableDetailTabModelProvider<AuditLog, HostListModel<Void>, HostEventListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(HostEventListModel source,
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
    public SearchableDetailModelProvider<Erratum, HostListModel<Void>, HostErrataListModel> getHostErrataListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<HostListModel<Void>> mainModelProvider,
            final Provider<HostErrataListModel> modelProvider,
            final Provider<HostErrataCountModel> countModelProvider) {

        SearchableDetailTabModelProvider<Erratum, HostListModel<Void>, HostErrataListModel> result =
                new SearchableDetailTabModelProvider<>(eventBus, defaultConfirmPopupProvider);
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);

        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Label, HostListModel<Void>, HostAffinityLabelListModel> getAffinityLabelListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<AffinityLabelPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<HostListModel<Void>> mainModelProvider,
            final Provider<HostAffinityLabelListModel> modelProvider) {
        SearchableDetailTabModelProvider<Label, HostListModel<Void>, HostAffinityLabelListModel> result =
                new SearchableDetailTabModelProvider<Label, HostListModel<Void>, HostAffinityLabelListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(HostAffinityLabelListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand == getModel().getNewCommand()
                                || lastExecutedCommand == getModel().getEditCommand()) {
                            return popupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(HostAffinityLabelListModel source,
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

    @Provides
    @Singleton
    public DetailTabModelProvider<HostListModel<Void>, HostErrataCountModel> getHostErrataCountProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<HostErrataListWithDetailsPopupPresenterWidget> errataPopupProvider,
            final Provider<HostErrataListModel> listModelProvider,
            final Provider<HostListModel<Void>> mainModelProvider,
            final Provider<HostErrataCountModel> modelProvider) {

        DetailTabModelProvider<HostListModel<Void>, HostErrataCountModel> result = new DetailTabModelProvider<HostListModel<Void>, HostErrataCountModel>(
                eventBus, defaultConfirmPopupProvider) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(HostErrataCountModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {

                return errataPopupProvider.get();
            }
        };
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);

        return result;
    }

    @Override
    protected void configure() {
        bind(new TypeLiteral<HostListModel<Void>>() {}).in(Singleton.class);
        bind(HostGeneralModel.class).in(Singleton.class);
        bind(HostHardwareGeneralModel.class).in(Singleton.class);
        bind(HostHooksListModel.class).in(Singleton.class);
        bind(HostBricksListModel.class).in(Singleton.class);
        bind(HostGlusterStorageDevicesListModel.class).in(Singleton.class);
        bind(HostInterfaceListModel.class).in(Singleton.class);
        bind(HostVmListModel.class).in(Singleton.class);
        bind(HostGlusterSwiftListModel.class).in(Singleton.class);
        bind(HostEventListModel.class).in(Singleton.class);
        bind(HostDeviceListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<VDS>>(){}).in(Singleton.class);
        bind(FenceAgentModelProvider.class).in(Singleton.class);
        bind(FenceProxyModelProvider.class).in(Singleton.class);
        bind(HostErrataCountModel.class).in(Singleton.class);
        bind(HostErrataListModel.class).in(Singleton.class);
        bind(HostAffinityLabelListModel.class).in(Singleton.class);
        bind(HostMainSelectedItems.class).asEagerSingleton();


        // Form Detail Models
        bind(new TypeLiteral<DetailModelProvider<HostListModel<Void>, HostHardwareGeneralModel>>(){})
            .to(new TypeLiteral<DetailTabModelProvider<HostListModel<Void>, HostHardwareGeneralModel>>(){}).in(Singleton.class);
        // Search-able Detail Models
        bind(new TypeLiteral<SearchableDetailModelProvider<Map<String, String>, HostListModel<Void>, HostHooksListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<Map<String, String>, HostListModel<Void>, HostHooksListModel>>(){})
            .in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<GlusterBrickEntity, HostListModel<Void>, HostBricksListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<GlusterBrickEntity, HostListModel<Void>, HostBricksListModel>>(){})
            .in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<GlusterServerService, HostListModel<Void>, HostGlusterSwiftListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<GlusterServerService, HostListModel<Void>, HostGlusterSwiftListModel>>(){})
            .in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<HostDeviceView, HostListModel<Void>, HostDeviceListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<HostDeviceView, HostListModel<Void>, HostDeviceListModel>>(){})
            .in(Singleton.class);
        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, HostListModel<Void>, PermissionListModel<VDS>>>(){})
            .to(new TypeLiteral<PermissionModelProvider<VDS, HostListModel<Void>>>() {}).in(Singleton.class);
    }

}
