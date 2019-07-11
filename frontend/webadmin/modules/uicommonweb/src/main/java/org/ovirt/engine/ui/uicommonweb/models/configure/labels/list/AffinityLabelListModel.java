package org.ovirt.engine.ui.uicommonweb.models.configure.labels.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.model.AffinityLabelModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.model.EditAffinityLabelModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.model.NewAffinityLabelModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;

public abstract class AffinityLabelListModel<E extends BusinessEntity<Guid>> extends SearchableListModel<E, Label> {
    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;
    private final QueryType queryType;
    private final EntityModel<Map<Guid, String>> entitiesNameMap = new EntityModel<>();

    public AffinityLabelListModel(QueryType queryType) {
        this.queryType = queryType;
        setTitle(ConstantsManager.getInstance().getConstants().affinityLabelsTitle());
        setHelpTag(HelpTag.affinity_groups);
        setHashName("affinity_labels"); // $//$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        entitiesNameMap.setEntity(new HashMap<>());

        updateActionAvailability();

        loadEntitiesNameMap();

        getItemsChangedEvent().addListener((ev, sender, args) -> loadEntitiesNameMap());
        getSelectedItemsChangedEvent().addListener((ev, sender, args) -> loadEntitiesNameMap());
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
        loadEntitiesNameMap();
    }

    protected abstract Guid getClusterId();

    protected abstract String getClusterName();

    protected abstract Version getClusterCompatibilityVersion();

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
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

    public void loadEntitiesNameMap() {
        AsyncDataProvider.getInstance().getEntitiesNameMap(new AsyncQuery<>(nameMap -> {
            entitiesNameMap.getEntity().clear();
            entitiesNameMap.getEntity().putAll(nameMap);
            entitiesNameMap.getEntityChangedEvent().raise(this, EventArgs.EMPTY);
        }));
    }

    public EntityModel<Map<Guid, String>> getEntitiesNameMap() {
        return entitiesNameMap;
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

        AffinityLabelModel model = new NewAffinityLabelModel(getNewAffinityLabel(),
                this,
                getClusterId(),
                getClusterName(),
                getClusterCompatibilityVersion().lessOrEquals(Version.v4_3));

        model.init();
        setWindow(model);
    }

    protected Label getNewAffinityLabel() {
        return new LabelBuilder().build();
    }

    private void edit() {
        if (getWindow() != null) {
            return;
        }
        Label affinityLabel = getSelectedItem();
        if (affinityLabel == null) {
            return;
        }
        AffinityLabelModel model = new EditAffinityLabelModel(affinityLabel,
                this,
                getClusterId(),
                getClusterName(),
                getClusterCompatibilityVersion().lessOrEquals(Version.v4_3));

        model.init();
        setWindow(model);
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeAffinityLabelsTitle());
        model.setHelpTag(HelpTag.remove_affinity_groups);
        model.setHashName("remove_affinity_labels"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        for (Label affinityLabel : getSelectedItems()) {
            list.add(affinityLabel.getName());
        }
        model.setItems(list);

        model.getCommands().add(UICommand.createDefaultOkUiCommand("OnRemove", this)); //$NON-NLS-1$
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (Label affinityLabel : getSelectedItems()) {
            parameters.add(new LabelActionParameters(affinityLabel));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveLabel, parameters,
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
        return "AffinityLabelListModel"; //$NON-NLS-1$
    }

}
