package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainViewModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGlusterHookListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterServiceModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterWarningsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.ClusterAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.ClusterAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.NewHostNetworkQosModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.HostNetworkQosPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterManageNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterWarningsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.GlusterHookContentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.GlusterHookResolveConflictsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ManageGlusterSwiftPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.NewClusterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.DetachGlusterHostsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.GuidePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.MultipleHostsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.label.AffinityLabelPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.macpool.SharedMacPoolPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.CpuProfilePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling.AffinityGroupPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.ClusterMainSelectedItems;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class ClusterModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<Cluster, ClusterListModel<Void>> getClusterListProvider(EventBus eventBus,
            final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ClusterPopupPresenterWidget> popupProvider,
            final Provider<GuidePopupPresenterWidget> guidePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<MultipleHostsPopupPresenterWidget> addMultipleHostsPopupProvider,
            final Provider<SharedMacPoolPopupPresenterWidget> macPoolPopupProvider,
            final Provider<ClusterListModel<Void>> clusterProvider,
            final Provider<ClusterWarningsPopupPresenterWidget> clusterWarningsPopupProvider) {
        MainViewModelProvider<Cluster, ClusterListModel<Void>> result = new MainViewModelProvider<Cluster, ClusterListModel<Void>>
                (eventBus, defaultConfirmPopupProvider) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ClusterListModel<Void> source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getNewCommand()
                        || lastExecutedCommand == getModel().getEditCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getGuideCommand()) {
                    return guidePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getAddMacPoolCommand()) {
                    return macPoolPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getAddMultipleHostsCommand()) {
                    return addMultipleHostsPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(ClusterListModel<Void> source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getResetEmulatedMachineCommand()) {
                    return defaultConfirmPopupProvider.get();
                } else if (source.getConfirmWindow() instanceof ClusterWarningsModel) {
                    return clusterWarningsPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
        result.setModelProvider(clusterProvider);
        return result;
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<ClusterListModel<Void>, ClusterGeneralModel> getClusterGeneralProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<MultipleHostsPopupPresenterWidget> multipleHostsProvider,
            final Provider<DetachGlusterHostsPopupPresenterWidget> detachHostsProvider,
            final Provider<ManageGlusterSwiftPopupPresenterWidget> manageGlusterSwiftProvider,
            final Provider<ClusterListModel<Void>> clusterProvider,
            final Provider<ClusterGeneralModel> detailProvider) {
        DetailTabModelProvider<ClusterListModel<Void>, ClusterGeneralModel> result =
                new DetailTabModelProvider<ClusterListModel<Void>, ClusterGeneralModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ClusterGeneralModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getImportNewGlusterHostsCommand()) {
                            return multipleHostsProvider.get();
                        } else if (lastExecutedCommand == getModel().getDetachNewGlusterHostsCommand()) {
                            return detachHostsProvider.get();
                        } else if (lastExecutedCommand == getModel().getManageGlusterSwiftCommand()) {
                            return manageGlusterSwiftProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }
                };
        result.setModelProvider(detailProvider);
        result.setMainModelProvider(clusterProvider);
        return result;
    }

    // Search-able Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Network, ClusterListModel<Void>, ClusterNetworkListModel> getClusterNetworkListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<NewClusterNetworkPopupPresenterWidget> popupProvider,
            final Provider<ClusterManageNetworkPopupPresenterWidget> managePopupProvider,
            final Provider<ClusterListModel<Void>> mainModelProvider,
            final Provider<ClusterNetworkListModel> modelProvider,
            final Provider<HostNetworkQosPopupPresenterWidget> addQosPopupProvider) {
        SearchableDetailTabModelProvider<Network, ClusterListModel<Void>, ClusterNetworkListModel> result =
                new SearchableDetailTabModelProvider<Network, ClusterListModel<Void>, ClusterNetworkListModel>(
                        eventBus, defaultConfirmPopupProvider) {

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ClusterNetworkListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (windowModel instanceof NewHostNetworkQosModel) {
                            return addQosPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getNewNetworkCommand()) {
                            return popupProvider.get();
                        } else if (lastExecutedCommand == getModel().getManageCommand()) {
                            return managePopupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }
                };
        result.setModelProvider(modelProvider);
        result.setMainModelProvider(mainModelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<GlusterHookEntity, ClusterListModel<Void>, ClusterGlusterHookListModel> getClusterGlusterHookListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<DefaultConfirmationPopupPresenterWidget> confirmPopupProvider,
            final Provider<GlusterHookContentPopupPresenterWidget> contentPopupProvider,
            final Provider<GlusterHookResolveConflictsPopupPresenterWidget> resolveConflictsPopupProvider,
            final Provider<ClusterListModel<Void>> mainModelProvider,
            final Provider<ClusterGlusterHookListModel> modelProvider) {
        SearchableDetailTabModelProvider<GlusterHookEntity, ClusterListModel<Void>, ClusterGlusterHookListModel> result =
                new SearchableDetailTabModelProvider<GlusterHookEntity, ClusterListModel<Void>, ClusterGlusterHookListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ClusterGlusterHookListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getViewHookCommand()) {
                            return contentPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getResolveConflictsCommand()) {
                            return resolveConflictsPopupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(ClusterGlusterHookListModel source,
                            UICommand lastExecutedCommand) {
                        if (lastExecutedCommand == getModel().getDisableHookCommand()) {
                            return confirmPopupProvider.get();
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
    public SearchableDetailModelProvider<AffinityGroup, ClusterListModel<Void>, ClusterAffinityGroupListModel> getAffinityGroupListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<AffinityGroupPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ClusterListModel<Void>> mainModelProvider,
            final Provider<ClusterAffinityGroupListModel> modelProvider) {
        SearchableDetailTabModelProvider<AffinityGroup, ClusterListModel<Void>, ClusterAffinityGroupListModel> result =
                new SearchableDetailTabModelProvider<AffinityGroup, ClusterListModel<Void>, ClusterAffinityGroupListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ClusterAffinityGroupListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getNewCommand()
                                || lastExecutedCommand == getModel().getEditCommand()) {
                            return popupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(ClusterAffinityGroupListModel source,
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
    public SearchableDetailModelProvider<Label, ClusterListModel<Void>, ClusterAffinityLabelListModel> getAffinityLabelListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<AffinityLabelPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ClusterListModel<Void>> mainModelProvider,
            final Provider<ClusterAffinityLabelListModel> modelProvider) {
        SearchableDetailTabModelProvider<Label, ClusterListModel<Void>, ClusterAffinityLabelListModel> result =
                new SearchableDetailTabModelProvider<Label, ClusterListModel<Void>, ClusterAffinityLabelListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ClusterAffinityLabelListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getNewCommand()
                                || lastExecutedCommand == getModel().getEditCommand()) {
                            return popupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(ClusterAffinityLabelListModel source,
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
    public SearchableDetailModelProvider<CpuProfile, ClusterListModel<Void>, CpuProfileListModel> getStorageCpuProfileListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<CpuProfilePopupPresenterWidget> profilePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ClusterListModel<Void>> mainModelProvider,
            final Provider<CpuProfileListModel> modelProvider) {
        SearchableDetailTabModelProvider<CpuProfile, ClusterListModel<Void>, CpuProfileListModel> result =
                new SearchableDetailTabModelProvider<CpuProfile, ClusterListModel<Void>, CpuProfileListModel>(eventBus,
                        defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(CpuProfileListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand == getModel().getNewCommand()
                                || lastExecutedCommand == getModel().getEditCommand()) {
                            return profilePopupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(CpuProfileListModel source,
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
    public SearchableDetailModelProvider<AuditLog, ClusterListModel<Void>, ClusterEventListModel> getClusterEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> eventPopupProvider,
            final Provider<ClusterListModel<Void>> mainModelProvider,
            final Provider<ClusterEventListModel> modelProvider) {
        SearchableDetailTabModelProvider<AuditLog, ClusterListModel<Void>, ClusterEventListModel> result =
                new SearchableDetailTabModelProvider<AuditLog, ClusterListModel<Void>, ClusterEventListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ClusterEventListModel source,
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
        bind(new TypeLiteral<ClusterListModel<Void>> (){}).in(Singleton.class);
        bind(ClusterGeneralModel.class).in(Singleton.class);
        bind(ClusterHostListModel.class).in(Singleton.class);
        bind(ClusterNetworkListModel.class).in(Singleton.class);
        bind(ClusterVmListModel.class).in(Singleton.class);
        bind(ClusterServiceModel.class).in(Singleton.class);
        bind(ClusterGlusterHookListModel.class).in(Singleton.class);
        bind(ClusterAffinityGroupListModel.class).in(Singleton.class);
        bind(CpuProfileListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<Cluster>>(){}).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<CpuProfile>>(){}).in(Singleton.class);
        bind(ClusterAffinityLabelListModel.class).in(Singleton.class);
        bind(ClusterEventListModel.class).in(Singleton.class);
        bind(ClusterMainSelectedItems.class).asEagerSingleton();

        // Form Detail Models
        bind(new TypeLiteral<DetailModelProvider<ClusterListModel<Void>, ClusterServiceModel>>(){})
            .to(new TypeLiteral<DetailTabModelProvider<ClusterListModel<Void>, ClusterServiceModel>>(){}).in(Singleton.class);
        // Search-able Detail Models
        bind(new TypeLiteral<SearchableDetailModelProvider<VDS, ClusterListModel<Void>, ClusterHostListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<VDS, ClusterListModel<Void>, ClusterHostListModel>>(){})
            .in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<VM, ClusterListModel<Void>, ClusterVmListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<VM, ClusterListModel<Void>, ClusterVmListModel>>(){})
            .in(Singleton.class);
        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, ClusterListModel<Void>, PermissionListModel<Cluster>>>(){})
            .to(new TypeLiteral<PermissionModelProvider<Cluster, ClusterListModel<Void>>>(){}).in(Singleton.class);
        // Cpu Profile permission list model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, CpuProfileListModel, PermissionListModel<CpuProfile>>>(){})
            .to(new TypeLiteral<PermissionModelProvider<CpuProfile, CpuProfileListModel>>(){}).in(Singleton.class);
    }

}
