package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RolePermissionsRemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;

public class PermissionModelProvider<M extends ListWithDetailsModel> extends SearchableDetailTabModelProvider<Permissions, M, PermissionListModel> {

    private final Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;
    private final Provider<PermissionsPopupPresenterWidget> permissionPopupProvider;

    public PermissionModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            Provider<PermissionsPopupPresenterWidget> permissionPopupProvider,
            Class<M> mainModelClass) {
        super(eventBus, defaultConfirmPopupProvider, mainModelClass, PermissionListModel.class);
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
        this.permissionPopupProvider = permissionPopupProvider;
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

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(PermissionListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand == getModel().getAddCommand()) {
            return permissionPopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }
}
