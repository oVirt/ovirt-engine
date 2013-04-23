package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public abstract class ManageBackupModel extends SearchableListModel
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

    @Override
    public StorageDomain getEntity()
    {
        return (StorageDomain) super.getEntity();
    }

    public void setEntity(StorageDomain value)
    {
        super.setEntity(value);
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

    protected void Restore()
    {
    }

    protected void Cancel()
    {
        CancelConfirm();
        setWindow(null);
    }

    protected void CancelConfirm()
    {
        setConfirmWindow(null);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("storage_domain_shared_status")) //$NON-NLS-1$
        {
            CheckStorageStatus();
        }
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        CheckStorageStatus();
        UpdateActionAvailability();

        getSearchCommand().Execute();
    }

    private void CheckStorageStatus()
    {
        if (getEntity() != null)
        {
            if (getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.InActive
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
        UpdateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        UpdateActionAvailability();
    }

    protected void UpdateItems()
    {
    }

    private void UpdateActionAvailability()
    {
        getRestoreCommand().setIsExecutionAllowed(getEntity() != null && getSelectedItems() != null
                && getSelectedItems().size() > 0
                && getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Active);

        getRemoveCommand().setIsExecutionAllowed(getEntity() != null && getSelectedItems() != null
                && getSelectedItems().size() > 0
                && getEntity().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Active);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getRestoreCommand())
        {
            Restore();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirm")) //$NON-NLS-1$
        {
            CancelConfirm();
        }
    }
}
