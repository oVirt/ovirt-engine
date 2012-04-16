package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RoleListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.RolePopupPresenterWidget;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class RoleModelProvider extends SearchableTabModelProvider<roles, RoleListModel> {

    private final Provider<RolePopupPresenterWidget> rolePopupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    @Inject
    public RoleModelProvider(ClientGinjector ginjector,
            final Provider<RolePopupPresenterWidget> rolePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(ginjector);
        this.rolePopupProvider = rolePopupProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
    }

    @Override
    public RoleListModel getModel() {
        return getCommonModel().getRoleListModel();
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(RoleListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand.equals(getModel().getNewCommand())
                || lastExecutedCommand.equals(getModel().getEditCommand())
                || lastExecutedCommand.equals(getModel().getCloneCommand())) {
            return rolePopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(RoleListModel source,
            UICommand lastExecutedCommand) {
        if (lastExecutedCommand.equals(getModel().getRemoveCommand())) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }

}
