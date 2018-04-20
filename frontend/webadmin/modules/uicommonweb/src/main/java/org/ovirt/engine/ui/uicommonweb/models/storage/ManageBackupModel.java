package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public abstract class ManageBackupModel<T extends BusinessEntity<?>> extends SearchableListModel<StorageDomain, T> {

    private UICommand restoreCommand;
    private UICommand removeCommand;
    private boolean refreshing;

    protected static final String CANCEL_COMMAND = "Cancel"; //$NON-NLS-1$
    protected static final String CANCEL_CONFIRMATION_COMMAND = "CancelConfirm"; //$NON-NLS-1$

    public UICommand getRestoreCommand() {
        return restoreCommand;
    }

    private void setRestoreCommand(UICommand value) {
        restoreCommand = value;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    public boolean getIsRefreshing() {
        return refreshing;
    }

    public void setIsRefreshing(boolean value) {
        if (refreshing != value) {
            refreshing = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsRefreshing")); //$NON-NLS-1$
        }
    }

    protected ManageBackupModel() {
        setRestoreCommand(new UICommand("Restore", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
    }

    protected abstract void remove();

    protected abstract void restore();

    protected abstract ArchitectureType getArchitectureFromItem(T item);

    protected void cancel() {
        cancelConfirm();
        setWindow(null);
    }

    protected void cancelConfirm() {
        setConfirmWindow(null);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("storage_domain_shared_status")) { //$NON-NLS-1$
            checkStorageStatus();
        }
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        checkStorageStatus();
        updateActionAvailability();

        getSearchCommand().execute();
    }

    private void checkStorageStatus() {
        if (getEntity() == null) {
            return;
        }

        if (getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Inactive
                || getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Mixed) {
            setMessage(ConstantsManager.getInstance().getConstants().theExportDomainIsInactiveMsg());
        } else if (getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached) {
            setMessage(ConstantsManager.getInstance().getConstants().exportDomainIsNotAttachedToAnyDcMsg());
        } else {
            setMessage(null);
        }
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

    private void updateActionAvailability() {
        getRestoreCommand().setIsExecutionAllowed(getEntity() != null && getSelectedItems() != null
                && getSelectedItems().size() > 0
                && getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Active);

        getRemoveCommand().setIsExecutionAllowed(getEntity() != null && getSelectedItems() != null
                && getSelectedItems().size() > 0
                && getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Active);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRestoreCommand()) {
            restore();
            return;
        }

        if (command == getRemoveCommand()) {
            remove();
            return;
        }

        switch (command.getName()) {
        case CANCEL_COMMAND:
            cancel();
            break;
        case CANCEL_CONFIRMATION_COMMAND:
            cancelConfirm();
            break;
        }
    }

    protected boolean validateSingleArchitecture() {
     // Checks if there are selected VMs of multiple architectures
        ArchitectureType firstArch = null;
        boolean multipleArchs = false;

        for (T item : getSelectedItems()) {
            ArchitectureType arch = getArchitectureFromItem(item);

            if (firstArch == null) {
                firstArch = arch;
            } else {
                if (!firstArch.equals(arch)) {
                    multipleArchs = true;
                    break;
                }
            }
        }

        if (multipleArchs) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().invalidImportTitle());
            confirmModel.setHelpTag(HelpTag.multiple_archs_dialog);
            confirmModel.setHashName("multiple_archs_dialog"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance().getConstants().invalidImportMsg());

            UICommand command = UICommand.createDefaultOkUiCommand("multipleArchsOK", this); //$NON-NLS-1$
            confirmModel.getCommands().add(command);

            setConfirmWindow(confirmModel);

            return false;
        }

        return true;
    }
}
