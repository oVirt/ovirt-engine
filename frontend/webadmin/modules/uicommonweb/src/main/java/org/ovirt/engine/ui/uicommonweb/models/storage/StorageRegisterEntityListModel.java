package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class StorageRegisterEntityListModel extends SearchableListModel {

    private UICommand importCommand;

    public UICommand getImportCommand()
    {
        return importCommand;
    }

    private void setImportCommand(UICommand value)
    {
        importCommand = value;
    }

    public StorageRegisterEntityListModel() {
        setIsTimerDisabled(true);

        setImportCommand(new UICommand("Import", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    abstract RegisterEntityModel createRegisterEntityModel();

    abstract ImportEntityData createImportEntityData(Object entity);

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

        getImportCommand().setIsExecutionAllowed(isItemSelected && isDomainActive);
    }

    @Override
    public StorageDomain getEntity() {
        return (StorageDomain) super.getEntity();
    }

    public void setEntity(StorageDomain value)
    {
        super.setEntity(value);
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getImportCommand()) {
            restore();
        } else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            cancel();
        }
    }

    protected List<ImportEntityData> getImportEntities() {
        List<ImportEntityData> entities = new ArrayList<ImportEntityData>();
        for (Object item : getSelectedItems()) {
            entities.add(createImportEntityData(item));
        }
        Collections.sort(entities, new Linq.ImportEntityComparator());

        return entities;
    }

    protected void restore() {
        if (getWindow() != null) {
            return;
        }

        RegisterEntityModel model = createRegisterEntityModel();
        model.setStorageDomainId(getEntity().getId());
        setWindow(model);

        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.setCancelCommand(cancelCommand);

        model.getEntities().setItems(getImportEntities());
        model.initialize();
    }

    private void cancel() {
        setWindow(null);
    }
}
