package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
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
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.ChangeQuotaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.DisksAllocationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.ImportExportImagePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.UploadImagePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.DiskMainSelectedItems;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class DiskModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<Disk, DiskListModel> getDiskListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VmDiskPopupPresenterWidget> newPopupProvider,
            final Provider<VmDiskRemovePopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<UploadImagePopupPresenterWidget> uploadImagePopupProvider,
            final Provider<DisksAllocationPopupPresenterWidget> moveOrCopyPopupProvider,
            final Provider<ChangeQuotaPopupPresenterWidget> changeQutoaPopupProvider,
            final Provider<ImportExportImagePopupPresenterWidget> importExportImagePopupPresenterWidgetProvider,
            final Provider<DiskListModel> modelProvider) {

        MainViewModelProvider<Disk, DiskListModel> result =
                new MainViewModelProvider<Disk, DiskListModel>(eventBus, defaultConfirmPopupProvider) {

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(DiskListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getNewCommand()
                                || lastExecutedCommand == getModel().getEditCommand()) {
                            return newPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getMoveCommand()
                                || lastExecutedCommand == getModel().getCopyCommand()) {
                            return moveOrCopyPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getChangeQuotaCommand()) {
                            return changeQutoaPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getExportCommand()) {
                            return importExportImagePopupPresenterWidgetProvider.get();
                        } else if (lastExecutedCommand == getModel().getUploadCommand()
                                || lastExecutedCommand == getModel().getResumeUploadCommand()) {
                            return uploadImagePopupProvider.get();
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
                };
        result.setModelProvider(modelProvider);
        return result;
    }

    @Override
    protected void configure() {
        bind(DiskListModel.class).in(Singleton.class);
        bind(DiskGeneralModel.class).in(Singleton.class);
        bind(DiskVmListModel.class).in(Singleton.class);
        bind(DiskTemplateListModel.class).in(Singleton.class);
        bind(DiskStorageListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<Disk>>(){}).in(Singleton.class);
        bind(DiskMainSelectedItems.class).asEagerSingleton();

        // Form Detail Models
        bind(new TypeLiteral<DetailModelProvider<DiskListModel, DiskGeneralModel>>(){})
            .to(new TypeLiteral<DetailTabModelProvider<DiskListModel, DiskGeneralModel>>(){}).in(Singleton.class);
        // Search-able Detail Models
        bind(new TypeLiteral<SearchableDetailModelProvider<VM, DiskListModel, DiskVmListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<VM, DiskListModel, DiskVmListModel>>(){})
            .in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<VmTemplate, DiskListModel, DiskTemplateListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<VmTemplate, DiskListModel, DiskTemplateListModel>>(){})
            .in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<StorageDomain, DiskListModel, DiskStorageListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<StorageDomain, DiskListModel, DiskStorageListModel>>(){})
            .in(Singleton.class);
        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, DiskListModel, PermissionListModel<Disk>>>(){})
            .to(new TypeLiteral<PermissionModelProvider<Disk, DiskListModel>>(){}).in(Singleton.class);
    }

}
