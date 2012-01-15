package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

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
    public storage_domains getEntity()
    {
        return (storage_domains) super.getEntity();
    }

    public void setEntity(storage_domains value)
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsRefreshing"));
        }
    }

    protected ManageBackupModel()
    {
        setRestoreCommand(new UICommand("Restore", this));
        setRemoveCommand(new UICommand("Remove", this));
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
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("storage_domain_shared_status"))
        {
            CheckStorageStatus();
        }
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        CheckStorageStatus();
        UpdateActionAvailability();

        getSearchCommand().Execute();
    }

    private void CheckStorageStatus()
    {
        if (getEntity() != null)
        {
            if (getEntity().getstorage_domain_shared_status() == StorageDomainSharedStatus.InActive
                    || getEntity().getstorage_domain_shared_status() == StorageDomainSharedStatus.Mixed)
            {
                setMessage("The Export Domain is inactive. Data can be retrieved only when the Domain is activated");
            }
            else if (getEntity().getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached)
            {
                setMessage("Export Domain is not attached to any Data Center. Data can be retrieved only when the Domain is attached to a Data Center and is active");
            }
            else
            {
                setMessage(null);
            }
        }
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    protected void UpdateItems()
    {
    }

    private void UpdateActionAvailability()
    {
        getRestoreCommand().setIsExecutionAllowed(getEntity() != null && getSelectedItems() != null
                && getSelectedItems().size() > 0
                && getEntity().getstorage_domain_shared_status() == StorageDomainSharedStatus.Active);

        getRemoveCommand().setIsExecutionAllowed(getEntity() != null && getSelectedItems() != null
                && getSelectedItems().size() > 0
                && getEntity().getstorage_domain_shared_status() == StorageDomainSharedStatus.Active);
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
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirm"))
        {
            CancelConfirm();
        }
    }
}
