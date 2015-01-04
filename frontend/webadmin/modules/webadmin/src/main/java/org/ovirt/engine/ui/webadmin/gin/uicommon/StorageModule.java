package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.ModelBoundPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RolePermissionsRemoveConfirmationPopupPresenterWidget;
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
import org.ovirt.engine.ui.uicommonweb.models.profiles.DiskProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportCloneModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageIsoListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.TemplateBackupModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.VmBackupModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ReportPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.DiskProfilePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindMultiDcPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindSingleDcPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.ImportExportImagePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.RegisterTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.RegisterVmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageDestroyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageForceCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportCloneDialogPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportVmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class StorageModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<StorageDomain, StorageListModel> getStorageListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<StoragePopupPresenterWidget> popupProvider,
            final Provider<StorageRemovePopupPresenterWidget> removePopupProvider,
            final Provider<StorageDestroyPopupPresenterWidget> destroyConfirmPopupProvider,
            final Provider<StorageForceCreatePopupPresenterWidget> forceCreateConfirmPopupProvider,
            final Provider<ReportPresenterWidget> reportWindowProvider) {
        return new MainTabModelProvider<StorageDomain, StorageListModel>(eventBus, defaultConfirmPopupProvider, StorageListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(StorageListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getNewDomainCommand()
                        || lastExecutedCommand == getModel().getImportDomainCommand()
                        || lastExecutedCommand == getModel().getEditCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removePopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(StorageListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getDestroyCommand()) {
                    return destroyConfirmPopupProvider.get();
                }
                else if (lastExecutedCommand.getName().equals("OnSave") //$NON-NLS-1$
                    || lastExecutedCommand.getName().equals("OnImport")) { //$NON-NLS-1$
                    return forceCreateConfirmPopupProvider.get();
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

    @Provides
    @Singleton
    public DetailModelProvider<StorageListModel, StorageGeneralModel> getStorageGeneralProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new DetailTabModelProvider<StorageListModel, StorageGeneralModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                StorageGeneralModel.class);
    }

    // Searchable Detail Models
    @Provides
    @Singleton
    public SearchableDetailModelProvider<Permissions, StorageListModel, PermissionListModel> getPermissionListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<Permissions, StorageListModel, PermissionListModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
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
    public SearchableDetailModelProvider<StorageDomain, StorageListModel, StorageDataCenterListModel> getStorageDataCenterListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<FindSingleDcPopupPresenterWidget> singlePopupProvider,
            final Provider<FindMultiDcPopupPresenterWidget> multiPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<StorageDomain, StorageListModel, StorageDataCenterListModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                StorageDataCenterListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(StorageDataCenterListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                StorageDataCenterListModel model = getModel();

                if (lastExecutedCommand == model.getAttachCommand()) {
                    if (model.getAttachMultiple()) {
                        return multiPopupProvider.get();
                    } else {
                        return singlePopupProvider.get();
                    }
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(StorageDataCenterListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getDetachCommand() ||
                    lastExecutedCommand.getName().equals("OnAttach")) { //$NON-NLS-1$) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<RepoImage, StorageListModel, StorageIsoListModel> getStorageIsoListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ImportExportImagePopupPresenterWidget> importExportPopupProvider) {
        return new SearchableDetailTabModelProvider<RepoImage, StorageListModel, StorageIsoListModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                StorageIsoListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(StorageIsoListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getImportImagesCommand()) {
                    return importExportPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Disk, StorageListModel, StorageDiskListModel> getStorageDiskListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VmDiskRemovePopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<Disk, StorageListModel, StorageDiskListModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                StorageDiskListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(StorageDiskListModel source,
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
    public SearchableDetailModelProvider<Disk, StorageListModel, StorageSnapshotListModel> getStorageSnapshotListProvider(EventBus eventBus,
          Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
          final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<Disk, StorageListModel, StorageSnapshotListModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                StorageSnapshotListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(StorageSnapshotListModel source,
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
    public SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageTemplateListModel> getStorageTemplateListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VmTemplate, StorageListModel, StorageTemplateListModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                StorageTemplateListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, StorageListModel, StorageVmListModel> getStorageVmListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VM, StorageListModel, StorageVmListModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                StorageVmListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VmTemplate, StorageListModel, TemplateBackupModel> getTemplateBackupProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ImportTemplatePopupPresenterWidget> importTemplatePopupProvider,
            final Provider<ImportCloneDialogPresenterWidget> importClonePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VmTemplate, StorageListModel, TemplateBackupModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                TemplateBackupModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(TemplateBackupModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getRestoreCommand()) {
                    return importTemplatePopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(TemplateBackupModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else if (source.getConfirmWindow() instanceof ImportCloneModel) {
                    return importClonePopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, StorageListModel, StorageRegisterVmListModel> getStorageRegisterVmListProvider(
            EventBus eventBus, Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RegisterVmPopupPresenterWidget> registerEntityPopupProvider) {
        return new SearchableDetailTabModelProvider<VM, StorageListModel, StorageRegisterVmListModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                StorageRegisterVmListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(
                    StorageRegisterVmListModel source, UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getImportCommand()) {
                    return registerEntityPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageRegisterTemplateListModel> getStorageRegisterTemplateListProvider(
            EventBus eventBus, Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RegisterTemplatePopupPresenterWidget> registerEntityPopupProvider) {
        return new SearchableDetailTabModelProvider<VmTemplate, StorageListModel, StorageRegisterTemplateListModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                StorageRegisterTemplateListModel.class) {
        @Override
        public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(
                StorageRegisterTemplateListModel source, UICommand lastExecutedCommand, Model windowModel) {
            if (lastExecutedCommand == getModel().getImportCommand()) {
                return registerEntityPopupProvider.get();
            } else {
                return super.getModelPopup(source, lastExecutedCommand, windowModel);
            }
        }
    };
}

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, StorageListModel, VmBackupModel> getVmBackupProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ImportVmPopupPresenterWidget> importVmPopupProvider,
            final Provider<ImportCloneDialogPresenterWidget> importClonePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VM, StorageListModel, VmBackupModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                VmBackupModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VmBackupModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getRestoreCommand()) {
                    return importVmPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(VmBackupModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else if (source.getConfirmWindow() instanceof ImportCloneModel) {
                    return importClonePopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<AuditLog, StorageListModel, StorageEventListModel> getStorageEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> eventPopupProvider) {
        return new SearchableDetailTabModelProvider<AuditLog, StorageListModel, StorageEventListModel>(
                eventBus, defaultConfirmPopupProvider,
                StorageListModel.class,
                StorageEventListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(StorageEventListModel source,
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
    public SearchableDetailModelProvider<DiskProfile, StorageListModel, DiskProfileListModel> getStorageDiskProfileListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<DiskProfilePopupPresenterWidget> newProfilePopupProvider,
            final Provider<DiskProfilePopupPresenterWidget> editProfilePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<DiskProfile, StorageListModel, DiskProfileListModel>(eventBus,
                defaultConfirmPopupProvider,
                StorageListModel.class,
                DiskProfileListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DiskProfileListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand == getModel().getNewCommand()) {
                    return newProfilePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getEditCommand()) {
                    return editProfilePopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DiskProfileListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) { //$NON-NLS-1$
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
