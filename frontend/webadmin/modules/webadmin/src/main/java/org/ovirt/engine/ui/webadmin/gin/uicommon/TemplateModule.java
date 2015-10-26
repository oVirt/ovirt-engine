package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.AttachDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.EditDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewDiskModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.ChangeQuotaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.DisksAllocationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateEditPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.SingleSelectionVmDiskAttachPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmExportPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.TemplateMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class TemplateModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<VmTemplate, TemplateListModel> getTemplateListProvider(EventBus eventBus,
            final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<TemplateEditPresenterWidget> popupProvider,
            final Provider<VmExportPopupPresenterWidget> exportPopupProvider,
            final Provider<VmPopupPresenterWidget> createVmPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<TemplateListModel> modelProvider,
            final Provider<VmDiskPopupPresenterWidget> newDiskPopupProvider,
            final Provider<SingleSelectionVmDiskAttachPopupPresenterWidget> attachDiskPopupProvider,
            final Provider<CommonModel> commonModelProvider) {

        MainTabModelProvider<VmTemplate, TemplateListModel> result =
                new MainTabModelProvider<VmTemplate, TemplateListModel>(eventBus, defaultConfirmPopupProvider,
                        commonModelProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(TemplateListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        TemplateListModel model = getModel();

                        if (lastExecutedCommand == model.getEditCommand()) {
                            return popupProvider.get();
                        } else if (lastExecutedCommand == getModel().getExportCommand()) {
                            return exportPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getCreateVmFromTemplateCommand()) {
                            if (windowModel instanceof AttachDiskModel) {
                                return attachDiskPopupProvider.get();
                            } else if ((windowModel instanceof NewDiskModel) || (windowModel instanceof EditDiskModel)) {
                                return newDiskPopupProvider.get();
                            } else {
                                return createVmPopupProvider.get();
                            }
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(TemplateListModel source,
                            UICommand lastExecutedCommand) {
                        if (lastExecutedCommand == getModel().getRemoveCommand()) {
                            return removeConfirmPopupProvider.get();
                        } else if ("OnSave".equals(lastExecutedCommand.getName())) { //$NON-NLS-1$
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
    public SearchableDetailModelProvider<DiskModel, TemplateListModel, TemplateDiskListModel> getTemplateDiskListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<DisksAllocationPopupPresenterWidget> copyPopupProvider,
            final Provider<ChangeQuotaPopupPresenterWidget> changeQutoaPopupProvider,
            final Provider<TemplateListModel> mainModelProvider,
            final Provider<TemplateDiskListModel> modelProvider) {

        SearchableDetailTabModelProvider<DiskModel, TemplateListModel, TemplateDiskListModel> result =
                new SearchableDetailTabModelProvider<DiskModel, TemplateListModel, TemplateDiskListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(TemplateDiskListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand == getModel().getCopyCommand()) {
                            return copyPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getChangeQuotaCommand()) {
                            return changeQutoaPopupProvider.get();
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
    public SearchableDetailModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel> getTemplateInterfaceListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<TemplateInterfacePopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<TemplateListModel> mainModelProvider,
            final Provider<TemplateInterfaceListModel> modelProvider) {

        SearchableDetailTabModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel> result =
                new SearchableDetailTabModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(TemplateInterfaceListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if ((lastExecutedCommand == getModel().getNewCommand())
                                || (lastExecutedCommand == getModel().getEditCommand())) {
                            return popupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(TemplateInterfaceListModel source,
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
    public SearchableDetailModelProvider<StorageDomain, TemplateListModel, TemplateStorageListModel> getTemplateStorageListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<TemplateListModel> mainModelProvider,
            final Provider<TemplateStorageListModel> modelProvider) {

        SearchableDetailTabModelProvider<StorageDomain, TemplateListModel, TemplateStorageListModel> result =
                new SearchableDetailTabModelProvider<StorageDomain, TemplateListModel, TemplateStorageListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(TemplateStorageListModel source,
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
    public SearchableDetailModelProvider<AuditLog, TemplateListModel, TemplateEventListModel> getTemplateEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> eventPopupProvider,
            final Provider<TemplateListModel> mainModelProvider,
            final Provider<TemplateEventListModel> modelProvider) {

        SearchableDetailTabModelProvider<AuditLog, TemplateListModel, TemplateEventListModel> result =
                new SearchableDetailTabModelProvider<AuditLog, TemplateListModel, TemplateEventListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(TemplateEventListModel source,
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

    @Override
    protected void configure() {
        bind(TemplateListModel.class).in(Singleton.class);
        bind(TemplateGeneralModel.class).in(Singleton.class);
        bind(TemplateDiskListModel.class).in(Singleton.class);
        bind(TemplateInterfaceListModel.class).in(Singleton.class);
        bind(TemplateStorageListModel.class).in(Singleton.class);
        bind(TemplateVmListModel.class).in(Singleton.class);
        bind(TemplateEventListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<VmTemplate>>(){}).in(Singleton.class);
        bind(TemplateMainTabSelectedItems.class).asEagerSingleton();

        // Form Detail Models
        bind(new TypeLiteral<DetailModelProvider<TemplateListModel, TemplateGeneralModel>>(){})
            .to(new TypeLiteral<DetailTabModelProvider<TemplateListModel, TemplateGeneralModel>>(){}).in(Singleton.class);
        // Search-able Detail Models
        bind(new TypeLiteral<SearchableDetailModelProvider<VM, TemplateListModel, TemplateVmListModel>>(){})
           .to(new TypeLiteral<SearchableDetailTabModelProvider<VM, TemplateListModel, TemplateVmListModel>>(){})
           .in(Singleton.class);
        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, TemplateListModel, PermissionListModel<VmTemplate>>>(){})
           .to(new TypeLiteral<PermissionModelProvider<VmTemplate, TemplateListModel>>(){}).in(Singleton.class);
    }

}
