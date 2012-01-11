package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.SystemPermissionListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.RemoveConfirmationPopupPresenterWidget;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class SystemPermissionModelProvider extends SearchableTabModelProvider<permissions, SystemPermissionListModel> {

    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;
    private final Provider<PermissionsPopupPresenterWidget> permissionPopupProvider;

    @Inject
    public SystemPermissionModelProvider(ClientGinjector ginjector,
            Provider<PermissionsPopupPresenterWidget> permissionPopupProvider,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(ginjector);
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
        this.permissionPopupProvider = permissionPopupProvider;
    }

    @Override
    public SystemPermissionListModel getModel() {
        return getCommonModel().getSystemPermissionListModel();
    }

    @Override
    protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getAddCommand()) {
            return permissionPopupProvider.get();
        }
        return super.getModelPopup(lastExecutedCommand);
    }

    @Override
    protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        }
        return super.getConfirmModelPopup(lastExecutedCommand);
    }
}
