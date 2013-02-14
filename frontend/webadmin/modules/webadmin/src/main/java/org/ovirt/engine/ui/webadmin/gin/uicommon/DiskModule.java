package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
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
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskVmListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.ChangeQuotaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.DisksAllocationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageForceCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DiskModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<Disk, DiskListModel> getDiskListProvider(ClientGinjector ginjector,
            final Provider<VmDiskPopupPresenterWidget> newPopupProvider,
            final Provider<VmDiskRemovePopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<StorageForceCreatePopupPresenterWidget> forceCreateConfirmPopupProvider,
            final Provider<DisksAllocationPopupPresenterWidget> moveOrCopyPopupProvider,
            final Provider<ChangeQuotaPopupPresenterWidget> changeQutoaPopupProvider) {
        return new MainTabModelProvider<Disk, DiskListModel>(ginjector, DiskListModel.class) {

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DiskListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getNewCommand()) {
                    return newPopupProvider.get();
                }
                else if (lastExecutedCommand == getModel().getMoveCommand()
                        || lastExecutedCommand == getModel().getCopyCommand()) {
                    return moveOrCopyPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getChangeQuotaCommand()) {
                    return changeQutoaPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DiskListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else if (lastExecutedCommand.getName().equals("OnSave")) { //$NON-NLS-1$
                    return forceCreateConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }

            @Override
            public void onMainTabSelected() {
                super.onMainTabSelected();
                getModel().getDiskViewType().setEntity(DiskStorageType.IMAGE);
            }
        };
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<DiskListModel, DiskGeneralModel> getDiskGeneralProvider(ClientGinjector ginjector) {
        return new DetailTabModelProvider<DiskListModel, DiskGeneralModel>(ginjector,
                DiskListModel.class,
                DiskGeneralModel.class);
    }

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, DiskListModel, DiskVmListModel> getDiskVmModelProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VM, DiskListModel, DiskVmListModel>(ginjector,
                DiskListModel.class,
                DiskVmListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VmTemplate, DiskListModel, DiskTemplateListModel> getDiskTemplateModelProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VmTemplate, DiskListModel, DiskTemplateListModel>(ginjector,
                DiskListModel.class,
                DiskTemplateListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<StorageDomain, DiskListModel, DiskStorageListModel> getDiskStorageModelProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<StorageDomain, DiskListModel, DiskStorageListModel>(ginjector,
                DiskListModel.class,
                DiskStorageListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<permissions, DiskListModel, PermissionListModel> getDiskPermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, DiskListModel, PermissionListModel>(ginjector,
                DiskListModel.class,
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

    @Override
    protected void configure() {
    }

}
