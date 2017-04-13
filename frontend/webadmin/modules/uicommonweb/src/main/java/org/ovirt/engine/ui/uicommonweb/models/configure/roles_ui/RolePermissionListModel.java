package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class RolePermissionListModel extends SearchableListModel<Role, Permission> {

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    public RolePermissionListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().rolesPermissionsTitle());

        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        setSearchPageSize(1000);

        updateActionAvailability();
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();

        IdQueryParameters tempVar = new IdQueryParameters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetPermissionByRoleId, tempVar, new SetItemsAsyncQuery());
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        search();
    }

    private void updateActionAvailability() {
        getRemoveCommand().setIsExecutionAllowed(getSelectedItem() != null
                || (getSelectedItems() != null && getSelectedItems().size() > 0));

    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    public void cancel() {
        setWindow(null);
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removePermissionTitle());
        model.setHelpTag(HelpTag.remove_permission);
        model.setHashName("remove_permission"); //$NON-NLS-1$
        model.setItems(getSelectedItems());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onRemove() {
        if (getSelectedItems() != null && getSelectedItems().size() > 0) {
            ConfirmationModel model = (ConfirmationModel) getWindow();

            if (model.getProgress() != null) {
                return;
            }

            ArrayList<VdcActionParametersBase> list = new ArrayList<>();
            for (Object perm : getSelectedItems()) {
                PermissionsOperationsParameters tempVar = new PermissionsOperationsParameters();
                tempVar.setPermission((Permission) perm);
                list.add(tempVar);
            }

            model.startProgress();

            Frontend.getInstance().runMultipleAction(VdcActionType.RemovePermission, list,
                    result -> {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }, model);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRemoveCommand()) {
            remove();
        }
        else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "RolePermissionListModel"; //$NON-NLS-1$
    }
}
