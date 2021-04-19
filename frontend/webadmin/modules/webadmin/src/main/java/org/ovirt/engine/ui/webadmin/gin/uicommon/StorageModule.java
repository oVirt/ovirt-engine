package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDR;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
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
import org.ovirt.engine.ui.uicommonweb.models.profiles.DiskProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportCloneModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDRListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageIsoListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageLeaseListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterDiskImageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.TemplateBackupModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.VmBackupModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.DiskProfilePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.DisksAllocationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindMultiDcPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindSingleDcPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.ImportExportImagePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.RegisterTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.RegisterVmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageDRPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageDestroyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageForceCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.UploadImagePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportCloneDialogPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportVmFromExportDomainPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.StorageMainSelectedItems;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

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
            final Provider<StorageListModel> modelProvider) {
        MainViewModelProvider<StorageDomain, StorageListModel> result =
                new MainViewModelProvider<StorageDomain, StorageListModel>(eventBus, defaultConfirmPopupProvider) {
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
                        } else if (lastExecutedCommand.getName().equals("OnSave") //$NON-NLS-1$
                                  || lastExecutedCommand.getName().equals("OnImport")) { //$NON-NLS-1$
                            return forceCreateConfirmPopupProvider.get();
                        } else if (lastExecutedCommand.getName().equals("HandleISOForNFS") //$NON-NLS-1$
                                || lastExecutedCommand.getName().equals("HandleISOForPosix")) { //$NON-NLS-1$
                            return defaultConfirmPopupProvider.get();
                        } else {
                            return super.getConfirmModelPopup(source, lastExecutedCommand);
                        }
                    }
                };
        result.setModelProvider(modelProvider);
        return result;
    }

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<StorageDomain, StorageListModel, StorageDataCenterListModel> getStorageDataCenterListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<FindSingleDcPopupPresenterWidget> singlePopupProvider,
            final Provider<FindMultiDcPopupPresenterWidget> multiPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<StorageDataCenterListModel> modelProvider) {
        SearchableDetailTabModelProvider<StorageDomain, StorageListModel, StorageDataCenterListModel> result =
                new SearchableDetailTabModelProvider<StorageDomain, StorageListModel, StorageDataCenterListModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
    public SearchableDetailModelProvider<RepoImage, StorageListModel, StorageIsoListModel> getStorageIsoListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ImportExportImagePopupPresenterWidget> importExportPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<StorageIsoListModel> modelProvider) {

        SearchableDetailTabModelProvider<RepoImage, StorageListModel, StorageIsoListModel> result =
                new SearchableDetailTabModelProvider<RepoImage, StorageListModel, StorageIsoListModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Disk, StorageListModel, StorageDiskListModel> getStorageDiskListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VmDiskRemovePopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<UploadImagePopupPresenterWidget> uploadImagePopupProvider,
            final Provider<DisksAllocationPopupPresenterWidget> movePopupProvider,
            final Provider<DisksAllocationPopupPresenterWidget> copyPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<StorageDiskListModel> modelProvider) {

        SearchableDetailTabModelProvider<Disk, StorageListModel, StorageDiskListModel> result =
                new SearchableDetailTabModelProvider<Disk, StorageListModel, StorageDiskListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(StorageDiskListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getUploadCommand() || lastExecutedCommand == getModel().getResumeUploadCommand()) {
                            return uploadImagePopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getMoveCommand()) {
                            return movePopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getCopyCommand()) {
                            return copyPopupProvider.get();
                        }
                        return super.getModelPopup(source, lastExecutedCommand, windowModel);
                    }

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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Disk, StorageListModel, StorageRegisterDiskImageListModel> getStorageRegisterDiskImageListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<StorageRegisterDiskImageListModel> modelProvider,
            final Provider<DisksAllocationPopupPresenterWidget> registerDiskPopupProvider) {

        SearchableDetailTabModelProvider<Disk, StorageListModel, StorageRegisterDiskImageListModel> result =
                new SearchableDetailTabModelProvider<Disk, StorageListModel, StorageRegisterDiskImageListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(
                            StorageRegisterDiskImageListModel source, UICommand lastExecutedCommand) {
                        return super.getConfirmModelPopup(source, lastExecutedCommand);
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(
                            StorageRegisterDiskImageListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand == getModel().getRegisterCommand()) {
                            return registerDiskPopupProvider.get();
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
    public SearchableDetailModelProvider<Disk, StorageListModel, StorageSnapshotListModel> getStorageSnapshotListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<StorageSnapshotListModel> modelProvider) {

        SearchableDetailTabModelProvider<Disk, StorageListModel, StorageSnapshotListModel> result =
                new SearchableDetailTabModelProvider<Disk, StorageListModel, StorageSnapshotListModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VmTemplate, StorageListModel, TemplateBackupModel> getTemplateBackupProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ImportTemplatePopupPresenterWidget> importTemplatePopupProvider,
            final Provider<ImportCloneDialogPresenterWidget> importClonePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<TemplateBackupModel> modelProvider) {

        SearchableDetailTabModelProvider<VmTemplate, StorageListModel, TemplateBackupModel> result =
                new SearchableDetailTabModelProvider<VmTemplate, StorageListModel, TemplateBackupModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, StorageListModel, StorageRegisterVmListModel> getStorageRegisterVmListProvider(
            EventBus eventBus, Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RegisterVmPopupPresenterWidget> registerEntityPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<StorageRegisterVmListModel> modelProvider) {

        SearchableDetailTabModelProvider<VM, StorageListModel, StorageRegisterVmListModel> result =
                new SearchableDetailTabModelProvider<VM, StorageListModel, StorageRegisterVmListModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageRegisterTemplateListModel> getStorageRegisterTemplateListProvider(
            EventBus eventBus, Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RegisterTemplatePopupPresenterWidget> registerEntityPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<StorageRegisterTemplateListModel> modelProvider) {

        SearchableDetailTabModelProvider<VmTemplate, StorageListModel, StorageRegisterTemplateListModel> result =
                new SearchableDetailTabModelProvider<VmTemplate, StorageListModel, StorageRegisterTemplateListModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, StorageListModel, VmBackupModel> getVmBackupProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ImportVmFromExportDomainPopupPresenterWidget> importVmPopupProvider,
            final Provider<ImportCloneDialogPresenterWidget> importClonePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<VmBackupModel> modelProvider) {

        SearchableDetailTabModelProvider<VM, StorageListModel, VmBackupModel> result =
                new SearchableDetailTabModelProvider<VM, StorageListModel, VmBackupModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<AuditLog, StorageListModel, StorageEventListModel> getStorageEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> eventPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<StorageEventListModel> modelProvider) {

        SearchableDetailTabModelProvider<AuditLog, StorageListModel, StorageEventListModel> result =
                new SearchableDetailTabModelProvider<AuditLog, StorageListModel, StorageEventListModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<DiskProfile, StorageListModel, DiskProfileListModel> getStorageDiskProfileListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<DiskProfilePopupPresenterWidget> newProfilePopupProvider,
            final Provider<DiskProfilePopupPresenterWidget> editProfilePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<DiskProfileListModel> modelProvider) {
        SearchableDetailTabModelProvider<DiskProfile, StorageListModel, DiskProfileListModel> result =
                new SearchableDetailTabModelProvider<DiskProfile, StorageListModel, DiskProfileListModel>(eventBus,
                        defaultConfirmPopupProvider) {
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
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<StorageDomainDR, StorageListModel, StorageDRListModel> getStorageDRListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<StorageDRPopupPresenterWidget> storageDRPopupProvider,
            final Provider<StorageListModel> mainModelProvider,
            final Provider<StorageDRListModel> modelProvider) {

        SearchableDetailTabModelProvider<StorageDomainDR, StorageListModel, StorageDRListModel> result =
                new SearchableDetailTabModelProvider<StorageDomainDR, StorageListModel, StorageDRListModel>(
                        eventBus, defaultConfirmPopupProvider) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(StorageDRListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand == getModel().getNewCommand()) {
                    return storageDRPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getEditCommand()) {
                    return storageDRPopupProvider.get();
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
        bind(StorageListModel.class).in(Singleton.class);
        bind(StorageGeneralModel.class).in(Singleton.class);
        bind(StorageDataCenterListModel.class).in(Singleton.class);
        bind(StorageIsoListModel.class).in(Singleton.class);
        bind(StorageDiskListModel.class).in(Singleton.class);
        bind(StorageRegisterDiskImageListModel.class).in(Singleton.class);
        bind(StorageSnapshotListModel.class).in(Singleton.class);
        bind(StorageTemplateListModel.class).in(Singleton.class);
        bind(StorageVmListModel.class).in(Singleton.class);
        bind(TemplateBackupModel.class).in(Singleton.class);
        bind(StorageRegisterVmListModel.class).in(Singleton.class);
        bind(StorageRegisterTemplateListModel.class).in(Singleton.class);
        bind(VmBackupModel.class).in(Singleton.class);
        bind(StorageEventListModel.class).in(Singleton.class);
        bind(DiskProfileListModel.class).in(Singleton.class);
        bind(StorageDRListModel.class).in(Singleton.class);
        bind(StorageLeaseListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<StorageDomain>>(){}).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<DiskProfile>>(){}).in(Singleton.class);
        bind(StorageMainSelectedItems.class).asEagerSingleton();

        // Form Detail Models
        bind(new TypeLiteral<DetailModelProvider<StorageListModel, StorageGeneralModel>>(){})
            .to(new TypeLiteral<DetailTabModelProvider<StorageListModel, StorageGeneralModel>>(){}).in(Singleton.class);
        // Search-able Detail Models
        bind(new TypeLiteral<SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageTemplateListModel>>(){})
           .to(new TypeLiteral<SearchableDetailTabModelProvider<VmTemplate, StorageListModel, StorageTemplateListModel>>(){})
           .in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<VM, StorageListModel, StorageVmListModel>>(){})
           .to(new TypeLiteral<SearchableDetailTabModelProvider<VM, StorageListModel, StorageVmListModel>>(){})
           .in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<VmBase, StorageListModel, StorageLeaseListModel>>(){})
                .to(new TypeLiteral<SearchableDetailTabModelProvider<VmBase, StorageListModel, StorageLeaseListModel>>(){})
                .in(Singleton.class);
        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, StorageListModel, PermissionListModel<StorageDomain>>>(){})
           .to(new TypeLiteral<PermissionModelProvider<StorageDomain, StorageListModel>>(){}).in(Singleton.class);
        // Permission Disk Profiles
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, DiskProfileListModel, PermissionListModel<DiskProfile>>>(){})
           .to(new TypeLiteral<PermissionModelProvider<DiskProfile, DiskProfileListModel>>(){}).in(Singleton.class);
    }

}
