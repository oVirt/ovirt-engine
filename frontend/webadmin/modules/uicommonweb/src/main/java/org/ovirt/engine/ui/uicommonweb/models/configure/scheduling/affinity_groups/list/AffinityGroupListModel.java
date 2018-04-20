package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.model.AffinityGroupModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.model.EditAffinityGroupModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.model.NewAffinityGroupModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class AffinityGroupListModel<E extends BusinessEntity<Guid>> extends SearchableListModel<E, AffinityGroup> {
    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;
    private final QueryType queryType;

    public AffinityGroupListModel(QueryType queryType) {
        this.queryType = queryType;
        setTitle(ConstantsManager.getInstance().getConstants().affinityGroupsTitle());
        setHelpTag(HelpTag.affinity_groups);
        setHashName("affinity_groups"); // $//$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    protected abstract Guid getClusterId();

    protected abstract String getClusterName();

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        getNewCommand().setIsExecutionAllowed(true);

        boolean hasSelectedItems = getSelectedItems() != null && getSelectedItems().size() > 0;
        getEditCommand().setIsExecutionAllowed(hasSelectedItems && getSelectedItems().size() == 1);
        getRemoveCommand().setIsExecutionAllowed(hasSelectedItems);
    }

    @Override
    protected void syncSearch() {
        if (getEntity() != null) {
            super.syncSearch(queryType, new IdQueryParameters(getEntity().getId()));
        }
    }

    public UICommand getNewCommand() {
        return newCommand;
    }

    private void setNewCommand(UICommand newCommand) {
        this.newCommand = newCommand;
    }

    @Override
    public UICommand getEditCommand() {
        return editCommand;
    }

    private void setEditCommand(UICommand editCommand) {
        this.editCommand = editCommand;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand removeCommand) {
        this.removeCommand = removeCommand;
    }

    private void newEntity() {
        if (getWindow() != null) {
            return;
        }

        AffinityGroupModel model =
                new NewAffinityGroupModel(getNewAffinityGroup(), this, getClusterId(), getClusterName());
        model.init();
        setWindow(model);
    }

    protected AffinityGroup getNewAffinityGroup() {
        return new AffinityGroup();
    }

    private void edit() {
        if (getWindow() != null) {
            return;
        }
        AffinityGroup affinityGroup = getSelectedItem();
        if (affinityGroup == null) {
            return;
        }
        AffinityGroupModel model = new EditAffinityGroupModel(affinityGroup, this, getClusterId(), getClusterName());
        model.init();
        setWindow(model);
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeAffinityGroupsTitle());
        model.setHelpTag(HelpTag.remove_affinity_groups);
        model.setHashName("remove_affinity_groups"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        for (AffinityGroup affinityGroup : getSelectedItems()) {
            list.add(affinityGroup.getName());
        }
        model.setItems(list);

        UICommand command = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(command);
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (AffinityGroup affinityGroup : getSelectedItems()) {
            parameters.add(new AffinityGroupCRUDParameters(affinityGroup.getId(), affinityGroup));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveAffinityGroup, parameters,
                result -> {

                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);
    }

    private void cancel() {
        setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newEntity();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "AffinityGroupListModel"; //$NON-NLS-1$
    }

}
