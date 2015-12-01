package org.ovirt.engine.ui.uicommonweb.models.configure;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class SystemPermissionListModel extends SearchableListModel {

    private UICommand privateAddCommand;

    public UICommand getAddCommand() {
        return privateAddCommand;
    }

    private void setAddCommand(UICommand value) {
        privateAddCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    public SystemPermissionListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().systemPermissionTitle());

        setAddCommand(new UICommand("Add", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        setSearchPageSize(1000);

        updateActionAvailability();
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                SystemPermissionListModel systemPermissionListModel = (SystemPermissionListModel) model;
                systemPermissionListModel.setItems((Collection) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };
        VdcQueryParametersBase params = new VdcQueryParametersBase();
        params.setRefresh(false);

        Frontend.getInstance().runQuery(VdcQueryType.GetSystemPermissions, params, _asyncQuery);
    }

    private void updateActionAvailability() {
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
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

    private void add() {
        if (getWindow() != null) {
            return;
        }

        AdElementListModel model = new AdElementListModel();
        model.getIsEveryoneSelectionHidden().setEntity(true);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().addSystemPermissionToUserTitle());
        model.setHelpTag(HelpTag.add_system_permission_to_user);
        model.setHashName("add_system_permission_to_user"); //$NON-NLS-1$
        // model.Role.IsAvailable = true;
        // model.ExcludeItems = Items;

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnAttach", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onAttach() {
        AdElementListModel model = (AdElementListModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (model.getSelectedItems() == null) {
            cancel();
            return;
        }

        ArrayList<DbUser> items = new ArrayList<>();
        for (Object item : model.getItems()) {
            EntityModel entityModel = (EntityModel) item;
            if (entityModel.getIsSelected()) {
                items.add((DbUser) entityModel.getEntity());
            }
        }

        Role role = model.getRole().getSelectedItem();

        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (DbUser user : items) {
            Permission perm = new Permission(user.getId(), role.getId(), null, null);

            if (user.isGroup()) {
                DbGroup group = new DbGroup();
                group.setId(user.getId());
                group.setName(user.getFirstName());
                group.setDomain(user.getDomain());
                group.setExternalId(user.getExternalId());
                group.setNamespace(user.getNamespace());
                PermissionsOperationsParameters tempVar2 = new PermissionsOperationsParameters();
                tempVar2.setPermission(perm);
                tempVar2.setGroup(group);
                list.add(tempVar2);
            }
            else {
                PermissionsOperationsParameters tempVar3 = new PermissionsOperationsParameters();
                tempVar3.setPermission(perm);
                tempVar3.setUser(user);
                list.add(tempVar3);
            }
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.AddSystemPermission, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        AdElementListModel localModel = (AdElementListModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    private void onSave() {
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
        model.setTitle(ConstantsManager.getInstance().getConstants().removeSystemPermissionsTitle());
        model.setHelpTag(HelpTag.remove_system_permission);
        model.setHashName("remove_system_permission"); //$NON-NLS-1$
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

            Frontend.getInstance().runMultipleAction(VdcActionType.RemoveSystemPermission, list,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void executed(FrontendMultipleActionAsyncResult result) {

                            ConfirmationModel localModel = (ConfirmationModel) result.getState();
                            localModel.stopProgress();
                            cancel();

                        }
                    }, model);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getAddCommand()) {
            add();
        }
        else if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
        else if ("OnAttach".equals(command.getName())) { //$NON-NLS-1$
            onAttach();
            getForceRefreshCommand().execute();
        }
        else if (command == getRemoveCommand()) {
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
        return "SystemPermissionListModel"; //$NON-NLS-1$
    }
}
