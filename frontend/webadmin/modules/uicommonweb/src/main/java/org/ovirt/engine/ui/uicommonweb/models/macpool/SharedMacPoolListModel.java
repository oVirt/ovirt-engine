package org.ovirt.engine.ui.uicommonweb.models.macpool;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveMacPoolByIdParameters;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;

public class SharedMacPoolListModel extends ListWithSimpleDetailsModel<Void, MacPool> {

    private static final String CMD_REMOVE = "OnRemove"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$

    private final UICommand newCommand;
    private final UICommand editCommand;
    private final UICommand removeCommand;

    public UICommand getNewCommand() {
        return newCommand;
    }

    @Override
    public UICommand getEditCommand() {
        return editCommand;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    @Inject
    public SharedMacPoolListModel(final PermissionListModel<MacPool> permissionListModel) {
        setDetailList(permissionListModel);
        newCommand = new UICommand("New", this); //$NON-NLS-1$
        editCommand = new UICommand("Edit", this); //$NON-NLS-1$
        removeCommand = new UICommand("Remove", this); //$NON-NLS-1$
        setComparator(Linq.SharedMacPoolComparator);

        updateActionAvailability();
    }

    private void setDetailList(final PermissionListModel<MacPool> permissionListModel) {
        List<HasEntity<MacPool>> list = new ArrayList<>();
        list.add(permissionListModel);

        setDetailModels(list);
    }

    @Override
    protected String getListName() {
        return "SharedMacPoolListModel"; //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        super.syncSearch(QueryType.GetAllMacPools, new QueryParametersBase());
    }

    private void updateActionAvailability() {
        getEditCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1);

        boolean removeAllowed = true;
        if (getSelectedItems() == null || getSelectedItems().isEmpty()) {
            removeAllowed = false;
        } else {
            for (MacPool macPool : getSelectedItems()) {
                if (macPool.isDefaultPool()) {
                    removeAllowed = false;
                    break;
                }
            }
        }
        getRemoveCommand().setIsExecutionAllowed(removeAllowed);
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void newMacPool() {
        SharedMacPoolModel model = new NewSharedMacPoolModel(this);
        model.setEntity(new MacPool());
        setWindow(model);
    }

    private void editMacPool() {
        SharedMacPoolModel model = new SharedMacPoolModel(this, ActionType.UpdateMacPool);
        model.setTitle(ConstantsManager.getInstance().getConstants().editSharedMacPoolTitle());
        model.setHashName("edit_shared_mac_pool"); //$NON-NLS-1$
        model.setHelpTag(HelpTag.edit_shared_mac_pool);
        model.setEntity(getSelectedItem());
        setWindow(model);
    }

    private void removeMacPools() {
        ConfirmationModel model = new ConfirmationModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().removeSharedMacPoolsTitle());
        model.setHashName("remove_shared_mac_pools"); //$NON-NLS-1$
        model.setHelpTag(HelpTag.remove_shared_mac_pools);

        UICommand tempVar = UICommand.createDefaultOkUiCommand(CMD_REMOVE, this);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand(CMD_CANCEL, this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);

        List<String> macPoolNames = new ArrayList<>();
        for (MacPool macPool : getSelectedItems()) {
            macPoolNames.add(macPool.getName());
        }
        model.setItems(macPoolNames);

        setConfirmWindow(model);
    }

    private void cancel() {
        setConfirmWindow(null);
    }

    private void onRemove() {
        cancel();
        ArrayList<ActionParametersBase> params = new ArrayList<>();
        for (MacPool macPool : getSelectedItems()) {
            params.add(new RemoveMacPoolByIdParameters(macPool.getId()));
        }
        Frontend.getInstance().runMultipleAction(ActionType.RemoveMacPool, params);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newMacPool();
        } else if (command == getEditCommand()) {
            editMacPool();
        } else if (command == getRemoveCommand()) {
            removeMacPools();
        } else if (CMD_REMOVE.equals(command.getName())) {
            onRemove();
        } else if (CMD_CANCEL.equals(command.getName())) {
            cancel();
        }
    }
}
