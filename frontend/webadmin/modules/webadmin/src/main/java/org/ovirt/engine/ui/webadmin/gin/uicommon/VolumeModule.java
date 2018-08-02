package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
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
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeoRepListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.AddBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.BrickAdvancedDetailsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterClusterSnapshotConfigureOptionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeGeoRepActionConfirmPopUpViewPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeGeoRepCreateSessionPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeGeoReplicationSessionConfigPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeSnapshotConfigureOptionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeSnapshotCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.RemoveBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.RemoveBrickStatusPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.ReplaceBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.ResetBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeGeoRepSessionDetailsPopUpPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeParameterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeProfileStatisticsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeRebalanceStatusPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.VolumeMainSelectedItems;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class VolumeModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<GlusterVolumeEntity, VolumeListModel> getVolumeListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VolumePopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<VolumeRebalanceStatusPopupPresenterWidget> rebalanceStatusPopupProvider,
            final Provider<VolumeProfileStatisticsPopupPresenterWidget> volumeProfileStatsPopupProvider,
            final Provider<VolumeListModel> modelProvider,
            final Provider<GlusterVolumeSnapshotConfigureOptionsPopupPresenterWidget> volumeSnapshotConfigOptionsPopupProvider,
            final Provider<GlusterClusterSnapshotConfigureOptionsPopupPresenterWidget> clusterSnapshotConfigOptionsPopupProvider,
            final Provider<GlusterVolumeSnapshotCreatePopupPresenterWidget> snapshotPopupProvider,
            final Provider<GlusterVolumeGeoRepCreateSessionPopupPresenterWidget> createGeoRepSessionPopupProvider) {
        MainViewModelProvider<GlusterVolumeEntity, VolumeListModel> result =
                new MainViewModelProvider<GlusterVolumeEntity, VolumeListModel>(eventBus, defaultConfirmPopupProvider) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VolumeListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getNewVolumeCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getStatusRebalanceCommand() || lastExecutedCommand.getName().equals("onStopRebalance")) {//$NON-NLS-1$
                    return rebalanceStatusPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getShowVolumeProfileDetailsCommand() || lastExecutedCommand.getName().equals("showProfileDetails")) {//$NON-NLS-1$
                    return volumeProfileStatsPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getConfigureVolumeSnapshotOptionsCommand()) {
                    return volumeSnapshotConfigOptionsPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getConfigureClusterSnapshotOptionsCommand()) {
                    return clusterSnapshotConfigOptionsPopupProvider.get();
                }  else if (lastExecutedCommand == getModel().getCreateSnapshotCommand()) {
                    return snapshotPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getEditSnapshotScheduleCommand()) {
                    return snapshotPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getNewGeoRepSessionCommand()) {
                            return createGeoRepSessionPopupProvider.get();
                        } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(VolumeListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getStopCommand()
                                || lastExecutedCommand == getModel().getRemoveVolumeCommand()
                                || lastExecutedCommand == getModel().getStartCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> getVolumeBrickListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<AddBrickPopupPresenterWidget> addBrickPopupProvider,
            final Provider<RemoveBrickPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<RemoveBrickPopupPresenterWidget> removeBrickPopupProvider,
            final Provider<RemoveBrickStatusPopupPresenterWidget> removeBricksStatusPopupProvider,
            final Provider<ReplaceBrickPopupPresenterWidget> replaceBrickPopupProvider,
            final Provider<BrickAdvancedDetailsPopupPresenterWidget> brickDetailsPopupProvider,
            final Provider<ResetBrickPopupPresenterWidget> resetBrickPopupProvider,
            final Provider<VolumeListModel> mainModelProvider,
            final Provider<VolumeBrickListModel> modelProvider) {
        SearchableDetailTabModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> result =
                new SearchableDetailTabModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel>(
                eventBus, defaultConfirmPopupProvider) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VolumeBrickListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand == getModel().getAddBricksCommand()) {
                    return addBrickPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getRemoveBricksCommand()) {
                    return removeBrickPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getStatusRemoveBricksCommand()) {
                    return removeBricksStatusPopupProvider.get();
                }else if (lastExecutedCommand.getName().equals("OnStopRemoveBricks")) {  //$NON-NLS-1$
                    return removeBricksStatusPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getReplaceBrickCommand()) {
                    return replaceBrickPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getBrickAdvancedDetailsCommand()) {
                    return brickDetailsPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getResetBrickCommand()) {
                    return resetBrickPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(VolumeBrickListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveBricksCommand()) {
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
    public SearchableDetailModelProvider<GlusterVolumeSnapshotEntity, VolumeListModel, GlusterVolumeSnapshotListModel> getVolumeSnapshotListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VolumeListModel> mainModelProvider,
            final Provider<GlusterVolumeSnapshotListModel> modelProvider,
            final Provider<GlusterVolumeSnapshotCreatePopupPresenterWidget> snapshotPopupProvider) {
        SearchableDetailTabModelProvider<GlusterVolumeSnapshotEntity, VolumeListModel, GlusterVolumeSnapshotListModel> result =
                new SearchableDetailTabModelProvider<GlusterVolumeSnapshotEntity, VolumeListModel, GlusterVolumeSnapshotListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(GlusterVolumeSnapshotListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand == getModel().getCreateSnapshotCommand()) {
                            return snapshotPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getEditSnapshotScheduleCommand()) {
                            return snapshotPopupProvider.get();
                        }
                        return super.getModelPopup(source, lastExecutedCommand, windowModel);
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(GlusterVolumeSnapshotListModel source,
                            UICommand lastExecutedCommand) {
                        return super.getConfirmModelPopup(source, lastExecutedCommand);
                    }
                };
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<GlusterVolumeOptionEntity, VolumeListModel, VolumeParameterListModel> getVolumeParameterListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VolumeParameterPopupPresenterWidget> addParameterPopupProvider,
            final Provider<VolumeParameterPopupPresenterWidget> editParameterPopupProvider,
            final Provider<VolumeListModel> mainModelProvider,
            final Provider<VolumeParameterListModel> modelProvider) {
        SearchableDetailTabModelProvider<GlusterVolumeOptionEntity, VolumeListModel, VolumeParameterListModel> result =
                new SearchableDetailTabModelProvider<GlusterVolumeOptionEntity, VolumeListModel, VolumeParameterListModel>(
                eventBus, defaultConfirmPopupProvider) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VolumeParameterListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand == getModel().getAddParameterCommand()) {
                    return addParameterPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getEditParameterCommand()) {
                    return editParameterPopupProvider.get();
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
    public SearchableDetailModelProvider<AuditLog, VolumeListModel, VolumeEventListModel> getVolumeEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> eventPopupProvider,
            final Provider<VolumeListModel> mainModelProvider,
            final Provider<VolumeEventListModel> modelProvider) {
        SearchableDetailTabModelProvider<AuditLog, VolumeListModel, VolumeEventListModel> result =
                new SearchableDetailTabModelProvider<AuditLog, VolumeListModel, VolumeEventListModel>(
                eventBus, defaultConfirmPopupProvider) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VolumeEventListModel source,
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
    public SearchableDetailModelProvider<GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel> getVolumeGeoRepListProvider(EventBus eventBus,
            final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<GlusterVolumeGeoRepActionConfirmPopUpViewPresenterWidget> geoRepActionConfirmationPopupProvider,
            final Provider<GlusterVolumeGeoReplicationSessionConfigPopupPresenterWidget> geoRepConfigPopupProvider,
            final Provider<GlusterVolumeGeoRepCreateSessionPopupPresenterWidget> geoRepSessionCreatePopupProvider,
            final Provider<VolumeGeoRepSessionDetailsPopUpPresenterWidget> geoRepSessionDetailsProvider,
            final Provider<VolumeListModel> mainModelProvider,
            final Provider<VolumeGeoRepListModel> modelProvider) {
        SearchableDetailTabModelProvider<GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel> result =
                new SearchableDetailTabModelProvider<GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel>(eventBus,
                        defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VolumeGeoRepListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand == getModel().getStartSessionCommand()
                                || lastExecutedCommand == getModel().getStopSessionCommand()
                                || lastExecutedCommand == getModel().getPauseSessionCommand()
                                || lastExecutedCommand == getModel().getResumeSessionCommand()
                                || lastExecutedCommand == getModel().getRemoveSessionCommand()) {
                            return geoRepActionConfirmationPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getSessionOptionsCommand()) {
                            return geoRepConfigPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getNewSessionCommand()) {
                            return geoRepSessionCreatePopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getViewSessionDetailsCommand()) {
                            return geoRepSessionDetailsProvider.get();
                        } else {
                            return geoRepActionConfirmationPopupProvider.get();
                        }
                    }
                };
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Override
    protected void configure() {
        bind(VolumeListModel.class).in(Singleton.class);
        bind(VolumeGeneralModel.class).in(Singleton.class);
        bind(VolumeBrickListModel.class).in(Singleton.class);
        bind(VolumeParameterListModel.class).in(Singleton.class);
        bind(VolumeEventListModel.class).in(Singleton.class);
        bind(VolumeGeoRepListModel.class).in(Singleton.class);
        bind(GlusterVolumeSnapshotListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<GlusterVolumeEntity>>(){}).in(Singleton.class);
        bind(VolumeMainSelectedItems.class).asEagerSingleton();

        // Form Detail Models
        bind(new TypeLiteral<DetailModelProvider<VolumeListModel, VolumeGeneralModel>>(){})
            .to(new TypeLiteral<DetailTabModelProvider<VolumeListModel, VolumeGeneralModel>>(){}).in(Singleton.class);
        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, VolumeListModel, PermissionListModel<GlusterVolumeEntity>>>(){})
           .to(new TypeLiteral<PermissionModelProvider<GlusterVolumeEntity, VolumeListModel>>(){}).in(Singleton.class);
    }
}
