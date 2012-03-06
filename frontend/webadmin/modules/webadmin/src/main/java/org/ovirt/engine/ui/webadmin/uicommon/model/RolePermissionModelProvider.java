package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RolePermissionListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class RolePermissionModelProvider extends SearchableTabModelProvider<permissions, RolePermissionListModel> {

    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    @Inject
    public RolePermissionModelProvider(ClientGinjector ginjector,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(ginjector);
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
    }

    @Override
    public RolePermissionListModel getModel() {
        return (RolePermissionListModel) getCommonModel().getRoleListModel().getDetailModels().get(0);
    }

    @Override
    protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        }
        return super.getConfirmModelPopup(lastExecutedCommand);
    }
}
