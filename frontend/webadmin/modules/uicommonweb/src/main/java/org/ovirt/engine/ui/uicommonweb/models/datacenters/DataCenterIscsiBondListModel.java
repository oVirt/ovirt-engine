package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveIscsiBondParameters;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class DataCenterIscsiBondListModel extends SearchableListModel<StoragePool, IscsiBond> {

    private UICommand addCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    public DataCenterIscsiBondListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().iscsiBondsTitle());
        setHelpTag(HelpTag.iscsi_bundles);
        setHashName("iscsi_bundles"); //$NON-NLS-1$

        setAddCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    public UICommand getAddCommand() {
        return addCommand;
    }

    public void setAddCommand(UICommand addCommand) {
        this.addCommand = addCommand;
    }

    public UICommand getEditCommand() {
        return editCommand;
    }

    private void setEditCommand(UICommand value) {
        editCommand = value;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    public void setEntity(StoragePool value) {
        super.setEntity(value);
    }

    private void updateActionAvailability() {
        boolean atLeastOneItemSelected =  getSelectedItems() != null && !getSelectedItems().isEmpty();

        getEditCommand().setIsExecutionAllowed(atLeastOneItemSelected);
        getRemoveCommand().setIsExecutionAllowed(atLeastOneItemSelected);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getAddCommand()) {
            add();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if ("Cancel".equalsIgnoreCase(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("OnRemove".equalsIgnoreCase(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
    }

    private void add() {
        if (getWindow() != null) {
            return;
        }

        IscsiBondModel model = new IscsiBondModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().addIscsiBondTitle());
        model.setHelpTag(HelpTag.new_iscsi_bundle);
        model.setHashName("new_iscsi_bundle"); //$NON-NLS-1$
        model.setStoragePool(getEntity());
        setWindow(model);

        model.setCancelCommand(createCancelCommand());
        model.initialize();
    }

    public void edit() {
        if (getWindow() != null) {
            return;
        }

        IscsiBondModel model = new IscsiBondModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().editIscsiBondTitle());
        model.setHelpTag(HelpTag.edit_iscsi_bundle);
        model.setHashName("edit_iscsi_bundle"); //$NON-NLS-1$
        model.setIscsiBond(getSelectedItem());
        model.setStoragePool(getEntity());
        setWindow(model);

        model.setCancelCommand(createCancelCommand());
        model.initialize();
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().removeIscsiBondTitle());
        model.setHelpTag(HelpTag.remove_iscsi_bundle);
        model.setHashName("remove_iscsi_bundle"); //$NON-NLS-1$
        setWindow(model);

        model.getLatch().setEntity(false);

        ArrayList<String> items = new ArrayList<>();
        for (IscsiBond selected : getSelectedItems()) {
            items.add(selected.getName());
        }
        model.setItems(items);

        UICommand removeCommand = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(removeCommand);

        model.getCommands().add(createCancelCommand());
    }

    private void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();
        ArrayList<ActionParametersBase> params = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            params.add(new RemoveIscsiBondParameters(((IscsiBond) item).getId()));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveIscsiBond, params,
                result -> {
                    DataCenterIscsiBondListModel localModel = (DataCenterIscsiBondListModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, this);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
        updateActionAvailability();
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

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        IdQueryParameters params = new IdQueryParameters(getEntity().getId());
        params.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(QueryType.GetIscsiBondsByStoragePoolId, params, new SetItemsAsyncQuery());
    }

    @Override
    protected String getListName() {
        return "DataCenterIscsiBundleListModel"; //$NON-NLS-1$
    }

    private UICommand createCancelCommand() {
        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        return cancelCommand;
    }

    private void cancel() {
        setWindow(null);
    }
}
