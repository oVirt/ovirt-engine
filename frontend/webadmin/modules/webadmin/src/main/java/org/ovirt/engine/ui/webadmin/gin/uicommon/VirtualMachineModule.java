package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.ModelBoundPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.ReportCommand;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ReportPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AssignTagsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.GuidePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.DisksAllocationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmChangeCDPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmClonePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDesktopNewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmExportPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmMakeTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmMigratePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmRunOncePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmServerNewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmSnapshotCreatePopupPresenterWidget;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class VirtualMachineModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<VM, VmListModel> getVmListProvider(ClientGinjector ginjector,
            final Provider<AssignTagsPopupPresenterWidget> assignTagsPopupProvider,
            final Provider<VmMakeTemplatePopupPresenterWidget> makeTemplatePopupProvider,
            final Provider<VmRunOncePopupPresenterWidget> runOncePopupProvider,
            final Provider<VmChangeCDPopupPresenterWidget> changeCDPopupProvider,
            final Provider<VmExportPopupPresenterWidget> exportPopupProvider,
            final Provider<VmMigratePopupPresenterWidget> migratePopupProvider,
            final Provider<DisksAllocationPopupPresenterWidget> movePopupProvider,
            final Provider<VmDesktopNewPopupPresenterWidget> newDesktopVmPopupProvider,
            final Provider<VmServerNewPopupPresenterWidget> newServerVmPopupProvider,
            final Provider<GuidePopupPresenterWidget> guidePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ReportPresenterWidget> reportWindowProvider) {
        return new MainTabModelProvider<VM, VmListModel>(ginjector, VmListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getAssignTagsCommand()) {
                    return assignTagsPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getNewTemplateCommand()) {
                    return makeTemplatePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getRunOnceCommand()) {
                    return runOncePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getChangeCdCommand()) {
                    return changeCDPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getExportCommand()) {
                    return exportPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getMigrateCommand()) {
                    return migratePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getMoveCommand()) {
                    return movePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getNewDesktopCommand()) {
                    return newDesktopVmPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getNewServerCommand()) {
                    return newServerVmPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getEditCommand()) {
                    UnitVmModel vm = (UnitVmModel) getModel().getWindow();
                    if (vm.getVmType().equals(VmType.Desktop)) {
                        return newDesktopVmPopupProvider.get();
                    } else {
                        return newServerVmPopupProvider.get();
                    }
                } else if (lastExecutedCommand == getModel().getGuideCommand()) {
                    return guidePopupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand() ||
                        lastExecutedCommand == getModel().getStopCommand() ||
                        lastExecutedCommand == getModel().getShutdownCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
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

    @Provides
    @Singleton
    public DetailModelProvider<VmListModel, VmGeneralModel> getVmGeneralProvider(ClientGinjector ginjector) {
        return new DetailTabModelProvider<VmListModel, VmGeneralModel>(ginjector,
                VmListModel.class,
                VmGeneralModel.class);
    }

    // Searchable Detail Models
    @Provides
    @Singleton
    public SearchableDetailModelProvider<permissions, VmListModel, PermissionListModel> getPermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, VmListModel, PermissionListModel>(ginjector,
                VmListModel.class,
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
    public SearchableDetailModelProvider<DiskImage, VmListModel, VmDiskListModel> getVmDiskListProvider(ClientGinjector ginjector,
            final Provider<VmDiskPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<DisksAllocationPopupPresenterWidget> movePopupProvider) {
        return new SearchableDetailTabModelProvider<DiskImage, VmListModel, VmDiskListModel>(ginjector,
                VmListModel.class,
                VmDiskListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                VmDiskListModel model = getModel();

                if (lastExecutedCommand == model.getNewCommand() || lastExecutedCommand == model.getEditCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getMoveCommand()) {
                    return movePopupProvider.get();
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
    public SearchableDetailModelProvider<VmNetworkInterface, VmListModel, VmInterfaceListModel> getVmInterfaceListProvider(ClientGinjector ginjector,
            final Provider<VmInterfacePopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VmNetworkInterface, VmListModel, VmInterfaceListModel>(ginjector,
                VmListModel.class,
                VmInterfaceListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                VmInterfaceListModel model = getModel();

                if (lastExecutedCommand == model.getNewCommand() || lastExecutedCommand == model.getEditCommand()) {
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
    public SearchableDetailModelProvider<AuditLog, VmListModel, VmEventListModel> getVmEventListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<AuditLog, VmListModel, VmEventListModel>(ginjector,
                VmListModel.class,
                VmEventListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<String, VmListModel, VmAppListModel> getVmAppsProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<String, VmListModel, VmAppListModel>(ginjector,
                VmListModel.class,
                VmAppListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Snapshot, VmListModel, VmSnapshotListModel> getVmSnapshotListProvider(ClientGinjector ginjector,
            final Provider<VmSnapshotCreatePopupPresenterWidget> createPopupProvider,
            final Provider<VmClonePopupPresenterWidget> cloneVmPopupProvider) {
        return new SearchableDetailTabModelProvider<Snapshot, VmListModel, VmSnapshotListModel>(ginjector,
                VmListModel.class, VmSnapshotListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getNewCommand()) {
                    return createPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getCloneVmCommand()) {
                    getModel().setSystemTreeSelectedItem(this.getMainModel().getSystemTreeSelectedItem());
                    return cloneVmPopupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    @Override
    protected void configure() {
    }

}
