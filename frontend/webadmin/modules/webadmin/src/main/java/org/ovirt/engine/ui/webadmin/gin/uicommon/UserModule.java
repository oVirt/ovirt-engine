package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
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
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventNotifierListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGroup;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserQuotaListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AssignTagsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.user.ManageEventsPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class UserModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<DbUser, UserListModel> getUserListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<AssignTagsPopupPresenterWidget> assignTagsPopupProvider,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new MainTabModelProvider<DbUser, UserListModel>(eventBus, defaultConfirmPopupProvider, UserListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UserListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                UserListModel model = getModel();

                if (lastExecutedCommand == model.getAssignTagsCommand()) {
                    return assignTagsPopupProvider.get();
                } else if (lastExecutedCommand == model.getAddCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UserListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<UserListModel, UserGeneralModel> getUserGeneralProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new DetailTabModelProvider<UserListModel, UserGeneralModel>(
                eventBus, defaultConfirmPopupProvider,
                UserListModel.class,
                UserGeneralModel.class);
    }

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<event_subscriber, UserListModel, UserEventNotifierListModel> getUserEventNotifierListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ManageEventsPopupPresenterWidget> manageEventsPopupProvider) {
        return new SearchableDetailTabModelProvider<event_subscriber, UserListModel, UserEventNotifierListModel>(
                eventBus, defaultConfirmPopupProvider,
                UserListModel.class,
                UserEventNotifierListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UserEventNotifierListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getManageEventsCommand()) {
                    return manageEventsPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Permissions, UserListModel, UserPermissionListModel> getPermissionListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<Permissions, UserListModel, UserPermissionListModel>(
                eventBus, defaultConfirmPopupProvider,
                UserListModel.class,
                UserPermissionListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UserPermissionListModel source,
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
    public SearchableDetailModelProvider<Quota, UserListModel, UserQuotaListModel> getUserQuotaListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<Quota, UserListModel, UserQuotaListModel>(
                eventBus, defaultConfirmPopupProvider,
                UserListModel.class,
                UserQuotaListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<AuditLog, UserListModel, UserEventListModel> getUserEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> eventPopupProvider) {
        return new SearchableDetailTabModelProvider<AuditLog, UserListModel, UserEventListModel>(
                eventBus, defaultConfirmPopupProvider,
                UserListModel.class,
                UserEventListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UserEventListModel source,
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
    public SearchableDetailModelProvider<UserGroup, UserListModel, UserGroupListModel> getUserGroupListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<UserGroup, UserListModel, UserGroupListModel>(
                eventBus, defaultConfirmPopupProvider,
                UserListModel.class,
                UserGroupListModel.class);
    }

    @Override
    protected void configure() {
    }

}
