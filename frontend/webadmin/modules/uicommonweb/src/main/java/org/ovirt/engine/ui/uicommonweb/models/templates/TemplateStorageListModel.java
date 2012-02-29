package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDomainModel;

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

    ArrayList<StorageDomainModel> storageDomainModels;
    Iterable value;

    public TemplateStorageListModel()
    {
        setTitle("Storage");

        setRemoveCommand(new UICommand("Remove", this));

        UpdateActionAvailability();

        setIsTimerDisabled(true);
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

    @Override
    public void setItems(Iterable value)
    {
        if (storageDomainModels != null) {
            super.setItems(storageDomainModels);
            storageDomainModels = null;
        }
        else
        {
            this.value = value;

            VmTemplate template = (VmTemplate) getEntity();
            AsyncDataProvider.GetTemplateDiskList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {
                            TemplateStorageListModel templateStorageListModel = (TemplateStorageListModel) target;
                            ArrayList<DiskImage> diskImages = (ArrayList<DiskImage>) returnValue;

                            ArrayList<storage_domains> storageDomains =
                                    Linq.<storage_domains> Cast(templateStorageListModel.value);
                            ArrayList<StorageDomainModel> storageDomainModels = new ArrayList<StorageDomainModel>();

                            for (storage_domains storageDomain : storageDomains) {
                                StorageDomainModel storageDomainModel = new StorageDomainModel();
                                storageDomainModel.setStorageDomain(storageDomain);

                                ArrayList<DiskImage> disks = new ArrayList<DiskImage>();
                                for (DiskImage diskImage : diskImages) {
                                    if (diskImage.getstorage_ids().contains(storageDomain.getId())) {
                                        disks.add(diskImage);
                                    }
                                }

                                storageDomainModel.setDisks(disks);
                                storageDomainModels.add(storageDomainModel);
                            }

                            templateStorageListModel.storageDomainModels = storageDomainModels;
                            setItems(templateStorageListModel.value);
                        }
                    }),
                    template.getId());
        }
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
            ids.add(a.getId());
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
