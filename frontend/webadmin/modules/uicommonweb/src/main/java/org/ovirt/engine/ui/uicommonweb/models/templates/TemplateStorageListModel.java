package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

@SuppressWarnings("unused")
public class TemplateStorageListModel extends SearchableListModel
{

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    public TemplateStorageListModel()
    {
        setTitle("Storage");

        setRemoveCommand(new UICommand("Remove", this));

        UpdateActionAvailability();
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
        }

        UpdateActionAvailability();
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        VmTemplate template = (VmTemplate) getEntity();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                new GetStorageDomainsByVmTemplateIdQueryParameters(template.getId())));
        setItems(getAsyncResult().getData());
    }

    @Override
    protected void SyncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.SyncSearch();

        VmTemplate template = (VmTemplate) getEntity();
        super.SyncSearch(VdcQueryType.GetStorageDomainsByVmTemplateId,
                new GetStorageDomainsByVmTemplateIdQueryParameters(template.getId()));
    }

    private void remove()
    {
        VmTemplate template = (VmTemplate) getEntity();

        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Remove Template from Storage Domain");
        model.setHashName("remove_template_from_storage_domains");
        model.setMessage(StringFormat.format("Are you sure you want to remove the Template %1$s from the following Storage Domain(s)?",
                template.getname()));

        // Show warning if template is going to be removed from all storage domains it exist on.
        if (getSelectedItems().size() == ((java.util.List) getItems()).size())
        {
            model.setNote("Note: This action will remove the Template permanently from all Storage Domains.");
        }

        java.util.ArrayList<String> items = new java.util.ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            storage_domains a = (storage_domains) item;
            items.add(a.getstorage_name());
        }
        model.setItems(items);

        UICommand tempVar = new UICommand("OnRemove", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnRemove()
    {
        VmTemplate template = (VmTemplate) getEntity();

        java.util.ArrayList<Guid> ids = new java.util.ArrayList<Guid>();
        for (Object item : getSelectedItems())
        {
            storage_domains a = (storage_domains) item;
            ids.add(a.getid());
        }

        VmTemplateParametersBase tempVar = new VmTemplateParametersBase(template.getId());
        tempVar.setStorageDomainsList(ids);
        Frontend.RunActionAsyncroniousely(VdcActionType.RemoveVmTemplate, tempVar);

        Cancel();
    }

    private void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("status"))
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        VmTemplate template = (VmTemplate) getEntity();
        java.util.ArrayList<storage_domains> selectedItems =
                getSelectedItems() != null ? Linq.<storage_domains> Cast(getSelectedItems())
                        : new java.util.ArrayList<storage_domains>();

        getRemoveCommand().setIsExecutionAllowed(template != null && template.getstatus() == VmTemplateStatus.OK
                && selectedItems.size() > 0);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getRemoveCommand())
        {
            remove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
        {
            OnRemove();
        }
    }

    @Override
    protected String getListName() {
        return "TemplateStorageListModel";
    }
}
