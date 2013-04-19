package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
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
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportCloneModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageIsoListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.TemplateBackupModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.VmBackupModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ReportPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindMultiDcPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindSingleDcPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageDestroyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageForceCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportCloneDialogPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportVmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class StorageModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<StorageDomain, StorageListModel> getStorageListProvider(ClientGinjector ginjector,
            final Provider<StoragePopupPresenterWidget> popupProvider,
            final Provider<StorageRemovePopupPresenterWidget> removePopupProvider,
            final Provider<StorageDestroyPopupPresenterWidget> destroyConfirmPopupProvider,
            final Provider<StorageForceCreatePopupPresenterWidget> forceCreateConfirmPopupProvider,
            final Provider<ReportPresenterWidget> reportWindowProvider) {
        return new MainTabModelProvider<StorageDomain, StorageListModel>(ginjector, StorageListModel.class) {
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
                else if (lastExecutedCommand.getName().equals("OnSave")) { //$NON-NLS-1$
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
    public DetailModelProvider<StorageListModel, StorageGeneralModel> getStorageGeneralProvider(ClientGinjector ginjector) {
        return new DetailTabModelProvider<StorageListModel, StorageGeneralModel>(ginjector,
                StorageListModel.class,
                StorageGeneralModel.class);
    }

    // Searchable Detail Models
    @Provides
    @Singleton
    public SearchableDetailModelProvider<permissions, StorageListModel, PermissionListModel> getPermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, StorageListModel, PermissionListModel>(ginjector,
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
    public SearchableDetailModelProvider<StorageDomain, StorageListModel, StorageDataCenterListModel> getStorageDataCenterListProvider(ClientGinjector ginjector,
            final Provider<FindSingleDcPopupPresenterWidget> singlePopupProvider,
            final Provider<FindMultiDcPopupPresenterWidget> multiPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<StorageDomain, StorageListModel, StorageDataCenterListModel>(ginjector,
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
                if (lastExecutedCommand == getModel().getDetachCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<EntityModel, StorageListModel, StorageIsoListModel> getStorageIsoListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<EntityModel, StorageListModel, StorageIsoListModel>(ginjector,
                StorageListModel.class,
                StorageIsoListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Disk, StorageListModel, StorageDiskListModel> getStorageDiskListProvider(ClientGinjector ginjector,
            final Provider<VmDiskRemovePopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<Disk, StorageListModel, StorageDiskListModel>(ginjector,
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
    public SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageTemplateListModel> getStorageTemplateListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VmTemplate, StorageListModel, StorageTemplateListModel>(ginjector,
                StorageListModel.class,
                StorageTemplateListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, StorageListModel, StorageVmListModel> getStorageVmListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VM, StorageListModel, StorageVmListModel>(ginjector,
                StorageListModel.class,
                StorageVmListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VmTemplate, StorageListModel, TemplateBackupModel> getTemplateBackupProvider(ClientGinjector ginjector,
            final Provider<ImportTemplatePopupPresenterWidget> importTemplatePopupProvider,
            final Provider<ImportCloneDialogPresenterWidget> importClonePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VmTemplate, StorageListModel, TemplateBackupModel>(ginjector,
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
    public SearchableDetailModelProvider<VM, StorageListModel, VmBackupModel> getVmBackupProvider(ClientGinjector ginjector,
            final Provider<ImportVmPopupPresenterWidget> importVmPopupProvider,
            final Provider<ImportCloneDialogPresenterWidget> importClonePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VM, StorageListModel, VmBackupModel>(ginjector,
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
    public SearchableDetailModelProvider<AuditLog, StorageListModel, StorageEventListModel> getStorageEventListProvider(ClientGinjector ginjector,
            final Provider<EventPopupPresenterWidget> eventPopupProvider) {
        return new SearchableDetailTabModelProvider<AuditLog, StorageListModel, StorageEventListModel>(ginjector,
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

    @Override
    protected void configure() {
    }

}
