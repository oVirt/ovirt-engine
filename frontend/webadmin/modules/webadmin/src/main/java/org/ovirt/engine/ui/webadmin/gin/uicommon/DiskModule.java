package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
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
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.ChangeQuotaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.DisksAllocationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.ImportExportImagePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DiskModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<Disk, DiskListModel> getDiskListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VmDiskPopupPresenterWidget> newPopupProvider,
            final Provider<VmDiskRemovePopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<DisksAllocationPopupPresenterWidget> moveOrCopyPopupProvider,
            final Provider<ChangeQuotaPopupPresenterWidget> changeQutoaPopupProvider,
            final Provider<ImportExportImagePopupPresenterWidget> importExportImagePopupPresenterWidgetProvider) {
        return new MainTabModelProvider<Disk, DiskListModel>(eventBus, defaultConfirmPopupProvider, DiskListModel.class) {

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DiskListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getNewCommand()) {
                    return newPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getMoveCommand()
                        || lastExecutedCommand == getModel().getCopyCommand()) {
                    return moveOrCopyPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getChangeQuotaCommand()) {
                    return changeQutoaPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getExportCommand()) {
                    return importExportImagePopupPresenterWidgetProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(DiskListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }

            @Override
            public void onMainTabSelected() {
                super.onMainTabSelected();
                getModel().getDiskViewType().setEntity(null);
            }
        };
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<DiskListModel, DiskGeneralModel> getDiskGeneralProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new DetailTabModelProvider<DiskListModel, DiskGeneralModel>(
                eventBus, defaultConfirmPopupProvider,
                DiskListModel.class,
                DiskGeneralModel.class);
    }

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, DiskListModel, DiskVmListModel> getDiskVmModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VM, DiskListModel, DiskVmListModel>(
                eventBus, defaultConfirmPopupProvider,
                DiskListModel.class,
                DiskVmListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VmTemplate, DiskListModel, DiskTemplateListModel> getDiskTemplateModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VmTemplate, DiskListModel, DiskTemplateListModel>(
                eventBus, defaultConfirmPopupProvider,
                DiskListModel.class,
                DiskTemplateListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<StorageDomain, DiskListModel, DiskStorageListModel> getDiskStorageModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<StorageDomain, DiskListModel, DiskStorageListModel>(
                eventBus, defaultConfirmPopupProvider,
                DiskListModel.class,
                DiskStorageListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Permissions, DiskListModel, PermissionListModel> getDiskPermissionListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<Permissions, DiskListModel, PermissionListModel>(
                eventBus, defaultConfirmPopupProvider,
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
