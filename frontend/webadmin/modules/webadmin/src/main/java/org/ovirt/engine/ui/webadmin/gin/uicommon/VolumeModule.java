package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RolePermissionsRemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
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
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.AddBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.BrickAdvancedDetailsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeGeoRepActionConfirmPopUpViewPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.RemoveBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.RemoveBrickStatusPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.ReplaceBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeParameterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeProfileStatisticsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeRebalanceStatusPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class VolumeModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<GlusterVolumeEntity, VolumeListModel> getVolumeListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VolumePopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<VolumeRebalanceStatusPopupPresenterWidget> rebalanceStatusPopupProvider,
            final Provider<VolumeProfileStatisticsPopupPresenterWidget> volumeProfileStatsPopupProvider) {
        return new MainTabModelProvider<GlusterVolumeEntity, VolumeListModel>(eventBus, defaultConfirmPopupProvider, VolumeListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VolumeListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getNewVolumeCommand()) {
                    return popupProvider.get();
                }
                else if (lastExecutedCommand == getModel().getStatusRebalanceCommand() || lastExecutedCommand.getName().equals("onStopRebalance")) {//$NON-NLS-1$
                    return rebalanceStatusPopupProvider.get();
                }
                else if(lastExecutedCommand == getModel().getShowVolumeProfileDetailsCommand() || lastExecutedCommand.getName().equals("showProfileDetails")) {//$NON-NLS-1$
                    return volumeProfileStatsPopupProvider.get();
                }
                else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(VolumeListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getStopCommand()
                        || lastExecutedCommand == getModel().getRemoveVolumeCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public DetailModelProvider<VolumeListModel, VolumeGeneralModel> getVolumeGeneralProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new DetailTabModelProvider<VolumeListModel, VolumeGeneralModel>(
                eventBus, defaultConfirmPopupProvider,
                VolumeListModel.class,
                VolumeGeneralModel.class);
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
            final Provider<BrickAdvancedDetailsPopupPresenterWidget> brickDetailsPopupProvider) {
        return new SearchableDetailTabModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel>(
                eventBus, defaultConfirmPopupProvider,
                VolumeListModel.class,
                VolumeBrickListModel.class) {
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
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel> getVolumeGeoRepListProvider(EventBus eventBus, final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider, final Provider<GlusterVolumeGeoRepActionConfirmPopUpViewPresenterWidget> geoRepActionConfirmationPopupProvider) {
        return new SearchableDetailTabModelProvider<GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel>(eventBus, defaultConfirmPopupProvider, VolumeListModel.class, VolumeGeoRepListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VolumeGeoRepListModel source, UICommand lastExecutedCommand, Model windowModel) {
                if(lastExecutedCommand == getModel().getStartSessionCommand() || lastExecutedCommand == getModel().getStopSessionCommand() || lastExecutedCommand == getModel().getPauseSessionCommand() || lastExecutedCommand == getModel().getResumeSessionCommand()) {
                    return geoRepActionConfirmationPopupProvider.get();
                } else {
                    return defaultConfirmPopupProvider.get();
                }
            }
        };
    }

    public SearchableDetailModelProvider<GlusterVolumeSnapshotEntity, VolumeListModel, GlusterVolumeSnapshotListModel> getVolumeSnapshotListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VolumeListModel> mainModelProvider,
            final Provider<GlusterVolumeSnapshotListModel> modelProvider) {
        return new SearchableDetailTabModelProvider<GlusterVolumeSnapshotEntity, VolumeListModel, GlusterVolumeSnapshotListModel>(eventBus,
                defaultConfirmPopupProvider,
                VolumeListModel.class,
                GlusterVolumeSnapshotListModel.class) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(GlusterVolumeSnapshotListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        return super.getModelPopup(source, lastExecutedCommand, windowModel);
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(GlusterVolumeSnapshotListModel source,
                            UICommand lastExecutedCommand) {
                        return super.getConfirmModelPopup(source, lastExecutedCommand);
                    }
                };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<GlusterVolumeOptionEntity, VolumeListModel, VolumeParameterListModel> getVolumeParameterListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VolumeParameterPopupPresenterWidget> addParameterPopupProvider,
            final Provider<VolumeParameterPopupPresenterWidget> editParameterPopupProvider) {
        return new SearchableDetailTabModelProvider<GlusterVolumeOptionEntity, VolumeListModel, VolumeParameterListModel>(
                eventBus, defaultConfirmPopupProvider,
                VolumeListModel.class,
                VolumeParameterListModel.class) {
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
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Permissions, VolumeListModel, PermissionListModel> getVolumePermissionListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<Permissions, VolumeListModel, PermissionListModel>(
                eventBus, defaultConfirmPopupProvider,
                VolumeListModel.class,
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
    public SearchableDetailModelProvider<AuditLog, VolumeListModel, VolumeEventListModel> getVolumeEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> eventPopupProvider) {
        return new SearchableDetailTabModelProvider<AuditLog, VolumeListModel, VolumeEventListModel>(
                eventBus, defaultConfirmPopupProvider,
                VolumeListModel.class,
                VolumeEventListModel.class) {
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
    }

    @Override
    protected void configure() {
        bind(GlusterVolumeSnapshotListModel.class).in(Singleton.class);
    }

}
