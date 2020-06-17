package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;

/**
 * @param <T> business entity type
 * @param <D> an <code>ImportEntityData</code> that wraps the business entity
 */
public abstract class StorageRegisterEntityListModel<T extends Queryable, D extends ImportEntityData<T>>
        extends SearchableListModel<StorageDomain, T> {

    private UICommand importCommand;
    private UICommand removeCommand;

    public UICommand getImportCommand() {
        return importCommand;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setImportCommand(UICommand value) {
        importCommand = value;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    public StorageRegisterEntityListModel() {
        setImportCommand(new UICommand("Import", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    abstract RegisterEntityModel<T, D> createRegisterEntityModel();
    abstract ConfirmationModel createRemoveEntityModel();

    abstract D createImportEntityData(T entity);
    abstract List<String> getSelectedItemsNames();
    abstract List<ActionParametersBase> getRemoveUnregisteredEntityParams(Guid storagePoolId);
    abstract ActionType getRemoveActionType();


    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        getSearchCommand().execute();

        if (getEntity() != null) {
            updateActionAvailability();
        }
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        boolean isItemSelected = getSelectedItems() != null && getSelectedItems() != null
                && getSelectedItems().size() > 0;

        boolean isDomainActive =
                getEntity() != null && getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Active;

        boolean isExecutionAllowed = isItemSelected && isDomainActive;
        getImportCommand().setIsExecutionAllowed(isExecutionAllowed);
        getRemoveCommand().setIsExecutionAllowed(isExecutionAllowed);
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    protected void syncSearch(QueryType queryType, final Comparator<? super T> comparator) {
        if (getEntity() == null) {
            return;
        }

        IdQueryParameters parameters = new IdQueryParameters(getEntity().getId());
        parameters.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(queryType, parameters, new SetSortedItemsAsyncQuery(comparator));
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getImportCommand()) {
            restore();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if (command.getName().equals("OnRemove")) { //$NON-NLS-1$
            onRemove();
        } else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            cancel();
        }
    }

    protected List<D> getImportEntities() {
        List<D> entities = new ArrayList<>();
        for (T item : getSelectedItems()) {
            entities.add(createImportEntityData(item));
        }
        Collections.sort(entities, Comparator.comparing(ImportEntityData::getName, new LexoNumericComparator()));

        return entities;
    }

    protected void restore() {
        if (getWindow() != null) {
            return;
        }

        RegisterEntityModel<T, D> model = createRegisterEntityModel();
        model.setStorageDomainId(getEntity().getId());
        setWindow(model);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.setCancelCommand(cancelCommand);

        model.getEntities().setItems(getImportEntities());
        model.initialize();
    }

    protected void remove() {
        if (getWindow() != null) {
            return;
        }
        ConfirmationModel window = createRemoveEntityModel();
        setWindow(window);

        List<String> items = getSelectedItemsNames();
        window.setItems(items);

        UICommand onRemoveUiCommand = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        window.getCommands().add(onRemoveUiCommand);
        UICommand cancelUiCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        window.getCommands().add(cancelUiCommand);
    }

    private void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }
        model.startProgress();

        List<ActionParametersBase> removeUnregisteredEntityParams =
                getRemoveUnregisteredEntityParams(getEntity().getStoragePoolId());

        Frontend.getInstance().runMultipleAction(
                getRemoveActionType(),
                removeUnregisteredEntityParams,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                },
                model);
    }

    protected void cancel() {
        setWindow(null);
    }
}
