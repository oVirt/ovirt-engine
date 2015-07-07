package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RolePermissionsRemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.UserPortalPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.permissions.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TemplatePermissionListModelProvider
        extends UserPortalSearchableDetailModelProvider<Permission, UserPortalTemplateListModel,
        UserPortalPermissionListModel<VmTemplate>> {

    private final Provider<PermissionsPopupPresenterWidget> permissionPopupProvider;
    private final Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    @Inject
    public TemplatePermissionListModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            Provider<PermissionsPopupPresenterWidget> permissionPopupProvider,
            Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider, user);
        this.permissionPopupProvider = permissionPopupProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(
            UserPortalPermissionListModel<VmTemplate> source,
            UICommand lastExecutedCommand,
            Model windowModel) {
        if (lastExecutedCommand == getModel().getAddCommand()) {
            return permissionPopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(
            UserPortalPermissionListModel<VmTemplate> source,
            UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }

}
