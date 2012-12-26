package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
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
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.AddBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.BrickAdvancedDetailsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.ReplaceBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeParameterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumePopupPresenterWidget;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class VolumeModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<GlusterVolumeEntity, VolumeListModel> getVolumeListProvider(ClientGinjector ginjector,
            final Provider<VolumePopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new MainTabModelProvider<GlusterVolumeEntity, VolumeListModel>(ginjector, VolumeListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VolumeListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getCreateVolumeCommand()) {
                    return popupProvider.get();
                } else {
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
    public DetailModelProvider<VolumeListModel, VolumeGeneralModel> getVolumeGeneralProvider(ClientGinjector ginjector) {
        return new DetailTabModelProvider<VolumeListModel, VolumeGeneralModel>(ginjector,
                VolumeListModel.class,
                VolumeGeneralModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> getVolumeBrickListProvider(ClientGinjector ginjector,
            final Provider<AddBrickPopupPresenterWidget> addBrickPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ReplaceBrickPopupPresenterWidget> replaceBrickPopupProvider,
            final Provider<BrickAdvancedDetailsPopupPresenterWidget> brickDetailsPopupProvider) {
        return new SearchableDetailTabModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel>(ginjector,
                VolumeListModel.class,
                VolumeBrickListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VolumeBrickListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand == getModel().getAddBricksCommand()) {
                    return addBrickPopupProvider.get();
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
    public SearchableDetailModelProvider<GlusterVolumeOptionEntity, VolumeListModel, VolumeParameterListModel> getVolumeParameterListProvider(ClientGinjector ginjector,
            final Provider<VolumeParameterPopupPresenterWidget> addParameterPopupProvider,
            final Provider<VolumeParameterPopupPresenterWidget> editParameterPopupProvider) {
        return new SearchableDetailTabModelProvider<GlusterVolumeOptionEntity, VolumeListModel, VolumeParameterListModel>(ginjector,
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
    public SearchableDetailModelProvider<permissions, VolumeListModel, PermissionListModel> getVolumePermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, VolumeListModel, PermissionListModel>(ginjector,
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
    public SearchableDetailModelProvider<AuditLog, VolumeListModel, VolumeEventListModel> getVolumeEventListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<AuditLog, VolumeListModel, VolumeEventListModel>(ginjector,
                VolumeListModel.class,
                VolumeEventListModel.class);
    }



    @Override
    protected void configure() {
    }

}
