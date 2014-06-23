package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
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
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
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
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.ClusterAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ReportPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterManageNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.GlusterHookContentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.GlusterHookResolveConflictsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ManageGlusterSwiftPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.NewClusterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.DetachGlusterHostsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.GuidePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.MultipleHostsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.CpuProfilePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling.AffinityGroupPopupPresenterWidget;
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
    public MainModelProvider<VDSGroup, ClusterListModel> getClusterListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ClusterPopupPresenterWidget> popupProvider,
            final Provider<GuidePopupPresenterWidget> guidePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ReportPresenterWidget> reportWindowProvider,
            final Provider<MultipleHostsPopupPresenterWidget> addMultipleHostsPopupProvider,
            final Provider<ClusterListModel> clusterProvider,
            final Provider<CommonModel> commonModelProvider) {
        MainTabModelProvider<VDSGroup, ClusterListModel> result = new MainTabModelProvider<VDSGroup, ClusterListModel>
                (eventBus, defaultConfirmPopupProvider, commonModelProvider) {
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
        result.setModelProvider(clusterProvider);
        return result;
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<ClusterListModel, ClusterGeneralModel> getClusterGeneralProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<MultipleHostsPopupPresenterWidget> multipleHostsProvider,
            final Provider<DetachGlusterHostsPopupPresenterWidget> detachHostsProvider,
            final Provider<ManageGlusterSwiftPopupPresenterWidget> manageGlusterSwiftProvider,
            final Provider<ClusterListModel> clusterProvider,
            final Provider<ClusterGeneralModel> detailProvider) {
        DetailTabModelProvider<ClusterListModel, ClusterGeneralModel> result =
                new DetailTabModelProvider<ClusterListModel, ClusterGeneralModel>(
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
    public SearchableDetailModelProvider<Network, ClusterListModel, ClusterNetworkListModel> getClusterNetworkListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<NewClusterNetworkPopupPresenterWidget> popupProvider,
            final Provider<ClusterManageNetworkPopupPresenterWidget> managePopupProvider,
            final Provider<ClusterListModel> mainModelProvider,
            final Provider<ClusterNetworkListModel> modelProvider) {
        SearchableDetailTabModelProvider<Network, ClusterListModel, ClusterNetworkListModel> result =
                new SearchableDetailTabModelProvider<Network, ClusterListModel, ClusterNetworkListModel>(
                        eventBus, defaultConfirmPopupProvider) {

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
        result.setModelProvider(modelProvider);
        result.setMainModelProvider(mainModelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<GlusterHookEntity, ClusterListModel, ClusterGlusterHookListModel> getClusterGlusterHookListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<DefaultConfirmationPopupPresenterWidget> confirmPopupProvider,
            final Provider<GlusterHookContentPopupPresenterWidget> contentPopupProvider,
            final Provider<GlusterHookResolveConflictsPopupPresenterWidget> resolveConflictsPopupProvider,
            final Provider<ClusterListModel> mainModelProvider,
            final Provider<ClusterGlusterHookListModel> modelProvider) {
        SearchableDetailTabModelProvider<GlusterHookEntity, ClusterListModel, ClusterGlusterHookListModel> result =
                new SearchableDetailTabModelProvider<GlusterHookEntity, ClusterListModel, ClusterGlusterHookListModel>(
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
    public SearchableDetailModelProvider<AffinityGroup, ClusterListModel, ClusterAffinityGroupListModel> getAffinityGroupListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<AffinityGroupPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ClusterListModel> mainModelProvider,
            final Provider<ClusterAffinityGroupListModel> modelProvider) {
        SearchableDetailTabModelProvider<AffinityGroup, ClusterListModel, ClusterAffinityGroupListModel> result =
                new SearchableDetailTabModelProvider<AffinityGroup, ClusterListModel, ClusterAffinityGroupListModel>(
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
    public SearchableDetailModelProvider<CpuProfile, ClusterListModel, CpuProfileListModel> getStorageCpuProfileListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<CpuProfilePopupPresenterWidget> profilePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ClusterListModel> mainModelProvider,
            final Provider<CpuProfileListModel> modelProvider) {
        SearchableDetailTabModelProvider<CpuProfile, ClusterListModel, CpuProfileListModel> result =
                new SearchableDetailTabModelProvider<CpuProfile, ClusterListModel, CpuProfileListModel>(eventBus,
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
                        if (lastExecutedCommand == getModel().getRemoveCommand()) { //$NON-NLS-1$
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
        bind(ClusterListModel.class).in(Singleton.class);
        bind(ClusterGeneralModel.class).in(Singleton.class);
        bind(ClusterHostListModel.class).in(Singleton.class);
        bind(ClusterNetworkListModel.class).in(Singleton.class);
        bind(ClusterVmListModel.class).in(Singleton.class);
        bind(ClusterServiceModel.class).in(Singleton.class);
        bind(ClusterGlusterHookListModel.class).in(Singleton.class);
        bind(ClusterAffinityGroupListModel.class).in(Singleton.class);
        bind(CpuProfileListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<ClusterListModel>>(){}).in(Singleton.class);

        // Form Detail Models
        bind(new TypeLiteral<DetailModelProvider<ClusterListModel, ClusterServiceModel>>(){})
            .to(new TypeLiteral<DetailTabModelProvider<ClusterListModel, ClusterServiceModel>>(){}).in(Singleton.class);
        // Search-able Detail Models
        bind(new TypeLiteral<SearchableDetailModelProvider<VDS, ClusterListModel, ClusterHostListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<VDS, ClusterListModel, ClusterHostListModel>>(){})
            .in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<VM, ClusterListModel, ClusterVmListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<VM, ClusterListModel, ClusterVmListModel>>(){})
            .in(Singleton.class);
        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permissions, ClusterListModel, PermissionListModel<ClusterListModel>>>(){})
            .to(new TypeLiteral<PermissionModelProvider<ClusterListModel>>(){}).in(Singleton.class);

    }

}
