package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public abstract class ManageBackupModel extends SearchableListModel<StorageDomain, Object>
{

    private UICommand privateRestoreCommand;

    public UICommand getRestoreCommand()
    {
        return privateRestoreCommand;
    }

    private void setRestoreCommand(UICommand value)
    {
        privateRestoreCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    private boolean isRefreshing;

    public boolean getIsRefreshing()
    {
        return isRefreshing;
    }

    public void setIsRefreshing(boolean value)
    {
        if (isRefreshing != value)
        {
            isRefreshing = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsRefreshing")); //$NON-NLS-1$
        }
    }

    protected ManageBackupModel()
    {
        setRestoreCommand(new UICommand("Restore", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
    }

    protected void remove()
    {
    }

    protected void restore()
    {
    }

    protected void cancel()
    {
        cancelConfirm();
        setWindow(null);
    }

    protected void cancelConfirm()
    {
        setConfirmWindow(null);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("storage_domain_shared_status")) //$NON-NLS-1$
        {
            checkStorageStatus();
        }
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        checkStorageStatus();
        updateActionAvailability();

        getSearchCommand().execute();
    }

    private void checkStorageStatus()
    {
        if (getEntity() != null)
        {
            if (getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Inactive
                    || getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Mixed)
            {
                setMessage(ConstantsManager.getInstance().getConstants().theExportDomainIsInactiveMsg());
            }
            else if (getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached)
            {
                setMessage(ConstantsManager.getInstance().getConstants().ExportDomainIsNotAttachedToAnyDcMsg());
            }
            else
            {
                setMessage(null);
            }
        }
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    protected void updateItems()
    {
    }

    private void updateActionAvailability()
    {
        getRestoreCommand().setIsExecutionAllowed(getEntity() != null && getSelectedItems() != null
                && getSelectedItems().size() > 0
                && getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Active);

        getRemoveCommand().setIsExecutionAllowed(getEntity() != null && getSelectedItems() != null
                && getSelectedItems().size() > 0
                && getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Active);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getRestoreCommand())
        {
            restore();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
        else if ("CancelConfirm".equals(command.getName())) //$NON-NLS-1$
        {
            cancelConfirm();
        }
    }
}
