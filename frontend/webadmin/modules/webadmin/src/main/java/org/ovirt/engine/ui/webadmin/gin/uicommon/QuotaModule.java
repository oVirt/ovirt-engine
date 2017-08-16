package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RolePermissionsRemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainViewModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaUserListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.QuotaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.QuotaMainSelectedItems;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class QuotaModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<Quota, QuotaListModel> getQuotaListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<QuotaPopupPresenterWidget> quotaPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<QuotaListModel> modelProvider) {
        MainViewModelProvider<Quota, QuotaListModel> result =
                new MainViewModelProvider<Quota, QuotaListModel>(eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(
                            QuotaListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand.equals(getModel().getCreateCommand())
                                || lastExecutedCommand.equals(getModel().getEditCommand())
                                || lastExecutedCommand.equals(getModel().getCloneCommand())) {
                            return quotaPopupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?>
                        getConfirmModelPopup(QuotaListModel source,
                            UICommand lastExecutedCommand) {
                        if (lastExecutedCommand.equals(getModel().getRemoveCommand())) {
                            return removeConfirmPopupProvider.get();
                        } else {
                            return super.getConfirmModelPopup(source, lastExecutedCommand);
                        }
                    }
                };
        result.setModelProvider(modelProvider);
        return result;
    }

    // Search-able Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Permission, QuotaListModel, QuotaUserListModel>
        getQuotaUserListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<QuotaListModel> mainModelProvider,
            final Provider<QuotaUserListModel> modelProvider) {
        SearchableDetailTabModelProvider<Permission, QuotaListModel, QuotaUserListModel> result =
                new SearchableDetailTabModelProvider<Permission, QuotaListModel, QuotaUserListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(
                            QuotaUserListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getAddCommand()) {
                            return popupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?>
                        getConfirmModelPopup(QuotaUserListModel source,
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
    public SearchableDetailModelProvider<Permission, QuotaListModel, QuotaPermissionListModel>
        getQuotaPermissionListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<QuotaListModel> mainModelProvider,
            final Provider<QuotaPermissionListModel> modelProvider) {
        SearchableDetailTabModelProvider<Permission, QuotaListModel, QuotaPermissionListModel> result =
                new SearchableDetailTabModelProvider<Permission, QuotaListModel, QuotaPermissionListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?>
                        getModelPopup(QuotaPermissionListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand == getModel().getAddCommand()) {
                            return popupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?>
                        getConfirmModelPopup(QuotaPermissionListModel source,
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
    public SearchableDetailModelProvider<AuditLog, QuotaListModel, QuotaEventListModel>
        getQuotaEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> eventPopupProvider,
            final Provider<QuotaListModel> mainModelProvider,
            final Provider<QuotaEventListModel> modelProvider) {
        SearchableDetailTabModelProvider<AuditLog, QuotaListModel, QuotaEventListModel> result =
                new SearchableDetailTabModelProvider<AuditLog, QuotaListModel, QuotaEventListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?>
                        getModelPopup(QuotaEventListModel source,
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
        bind(QuotaListModel.class).in(Singleton.class);
        bind(QuotaClusterListModel.class).in(Singleton.class);
        bind(QuotaStorageListModel.class).in(Singleton.class);
        bind(QuotaVmListModel.class).in(Singleton.class);
        bind(QuotaTemplateListModel.class).in(Singleton.class);
        bind(QuotaUserListModel.class).in(Singleton.class);
        bind(QuotaPermissionListModel.class).in(Singleton.class);
        bind(QuotaEventListModel.class).in(Singleton.class);
        bind(QuotaMainSelectedItems.class).asEagerSingleton();

        // Search-able Detail Models
        bind(new TypeLiteral<SearchableDetailModelProvider<QuotaCluster, QuotaListModel,
                QuotaClusterListModel>>(){}).to(new TypeLiteral<SearchableDetailTabModelProvider<QuotaCluster,
                        QuotaListModel, QuotaClusterListModel>>(){}).in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<QuotaStorage, QuotaListModel,
                QuotaStorageListModel>>(){}).to(new TypeLiteral<SearchableDetailTabModelProvider<QuotaStorage,
                        QuotaListModel, QuotaStorageListModel>>(){}).in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<VM, QuotaListModel, QuotaVmListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<VM, QuotaListModel, QuotaVmListModel>>(){})
            .in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<VmTemplate, QuotaListModel,
                QuotaTemplateListModel>>(){}).to(new TypeLiteral<SearchableDetailTabModelProvider<VmTemplate,
                        QuotaListModel, QuotaTemplateListModel>>(){}).in(Singleton.class);
    }

}
