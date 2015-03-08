package org.ovirt.engine.ui.webadmin.gin.uicommon;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.ModelBoundPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.numa.NumaSupportPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.ReportCommand;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterSwiftListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostBondInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostBricksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHardwareGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHooksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostManagementNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostNicModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ReportPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AssignTagsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.DetachConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ConfigureLocalStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostBondPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInstallPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostManagementConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostManagementPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostNicPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostSetupNetworksPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ManualFencePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksBondPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksManagementPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmMigratePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class HostModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<VDS, HostListModel<Void>> getHostListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<HostPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ManualFencePopupPresenterWidget> manualFenceConfirmPopupProvider,
            final Provider<AssignTagsPopupPresenterWidget> assignTagsPopupProvider,
            final Provider<ReportPresenterWidget> reportWindowProvider,
            final Provider<ConfigureLocalStoragePopupPresenterWidget> configureLocalStoragePopupProvider,
            final Provider<HostInstallPopupPresenterWidget> installPopupProvider,
            final Provider<NumaSupportPopupPresenterWidget> numaSupportPopupProvider,
            final Provider<HostListModel<Void>> modelProvider,
            final Provider<CommonModel> commonModelProvider) {
        MainTabModelProvider<VDS, HostListModel<Void>> result =
                new MainTabModelProvider<VDS, HostListModel<Void>>(eventBus, defaultConfirmPopupProvider,
                        commonModelProvider) {
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
                        if (lastExecutedCommand == getModel().getRemoveCommand() || lastExecutedCommand == getModel().getMaintenanceCommand()) {
                            return removeConfirmPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getManualFenceCommand()) {
                            return manualFenceConfirmPopupProvider.get();
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
        result.setModelProvider(modelProvider);
        return result;
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<HostListModel<Void>, HostGeneralModel> getHostGeneralProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<HostListModel<Void>> mainModelProvider,
            final Provider<HostGeneralModel> modelProvider) {
        DetailTabModelProvider<HostListModel<Void>, HostGeneralModel> result =
                new DetailTabModelProvider<HostListModel<Void>, HostGeneralModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(HostGeneralModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        return super.getModelPopup(source, lastExecutedCommand, windowModel);
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
            final Provider<DetachConfirmationPopupPresenterWidget> detachConfirmPopupProvider,
            final Provider<HostManagementConfirmationPopupPresenterWidget> hostManagementConfirmationdetachConfirmPopupProvider,
            final Provider<HostInterfacePopupPresenterWidget> hostInterfacePopupProvider,
            final Provider<SetupNetworksInterfacePopupPresenterWidget> setupNetworksInterfacePopupProvider,
            final Provider<HostManagementPopupPresenterWidget> hostManagementPopupProvider,
            final Provider<SetupNetworksManagementPopupPresenterWidget> setupNetworksManagementPopupProvider,
            final Provider<HostBondPopupPresenterWidget> hostBondPopupProvider,
            final Provider<SetupNetworksBondPopupPresenterWidget> setupNetworksBondPopupProvider,
            final Provider<HostNicPopupPresenterWidget> hostNicPopupProvider,
            final Provider<HostSetupNetworksPopupPresenterWidget> hostSetupNetworksPopupProvider,
            final Provider<HostListModel<Void>> mainModelProvider,
            final Provider<HostInterfaceListModel> modelProvider) {
        SearchableDetailTabModelProvider<HostInterfaceLineModel, HostListModel<Void>, HostInterfaceListModel> result =
                new SearchableDetailTabModelProvider<HostInterfaceLineModel, HostListModel<Void>, HostInterfaceListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(HostInterfaceListModel source,
                            UICommand lastExecutedCommand) {
                        if ("OnEditManagementNetworkConfirmation".equals(lastExecutedCommand.getName())) { //$NON-NLS-1$
                            return hostManagementConfirmationdetachConfirmPopupProvider.get();
                        }
                        return super.getConfirmModelPopup(source, lastExecutedCommand);
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(HostInterfaceListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (source.getWindow() instanceof HostSetupNetworksModel) {
                            // Resolve by dialog model
                            if (windowModel instanceof HostBondInterfaceModel) {
                                return setupNetworksBondPopupProvider.get();
                            } else if (windowModel instanceof HostManagementNetworkModel) {
                                HostManagementNetworkModel hostManagementNetworkModel = (HostManagementNetworkModel) windowModel;

                                if (hostManagementNetworkModel.isSetupNetworkMode()) {
                                    return setupNetworksManagementPopupProvider.get();
                                } else {
                                    return hostManagementPopupProvider.get();
                                }
                            } else if (windowModel instanceof HostInterfaceModel) {
                                HostInterfaceModel hostInterfaceModel = (HostInterfaceModel) windowModel;

                                if (hostInterfaceModel.isSetupNetworkMode()) {
                                    return setupNetworksInterfacePopupProvider.get();
                                } else {
                                    return hostInterfacePopupProvider.get();
                                }
                            } else if (windowModel instanceof HostNicModel) {
                                return hostNicPopupProvider.get();
                            }
                        }

                        // Resolve by last executed command
                        if (lastExecutedCommand == getModel().getEditCommand()) {
                            return hostInterfacePopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getEditManagementNetworkCommand()) {
                            return hostManagementPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getBondCommand()) {
                            return hostBondPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getSetupNetworksCommand()) {
                            return hostSetupNetworksPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getDetachCommand()) {
                            return detachConfirmPopupProvider.get();
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
            final Provider<VmMigratePopupPresenterWidget> migratePopupProvider,
            final Provider<HostListModel<Void>> mainModelProvider,
            final Provider<HostVmListModel> modelProvider) {
        SearchableDetailTabModelProvider<VM, HostListModel<Void>, HostVmListModel> result =
                new SearchableDetailTabModelProvider<VM, HostListModel<Void>, HostVmListModel>(
                        eventBus, defaultConfirmPopupProvider) {

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(HostVmListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getMigrateCommand()) {
                            return migratePopupProvider.get();
                        }
                        return super.getModelPopup(source, lastExecutedCommand, windowModel);
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

    @Override
    protected void configure() {
        bind(new TypeLiteral<HostListModel<Void>>() {}).in(Singleton.class);
        bind(HostGeneralModel.class).in(Singleton.class);
        bind(HostHardwareGeneralModel.class).in(Singleton.class);
        bind(HostHooksListModel.class).in(Singleton.class);
        bind(HostBricksListModel.class).in(Singleton.class);
        bind(HostInterfaceListModel.class).in(Singleton.class);
        bind(HostVmListModel.class).in(Singleton.class);
        bind(HostGlusterSwiftListModel.class).in(Singleton.class);
        bind(HostEventListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<VDS>>(){}).in(Singleton.class);

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
        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permissions, HostListModel<Void>, PermissionListModel<VDS>>>(){})
            .to(new TypeLiteral<PermissionModelProvider<VDS, HostListModel<Void>>>() {
            }).in(Singleton.class);
    }

}
