package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskByInternalDriveMappingComparer;
import org.ovirt.engine.ui.uicommonweb.Linq.StorageDomainModelByNameComparer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

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
        if (storageDomainModels != null)
        {
            Linq.Sort(storageDomainModels, new StorageDomainModelByNameComparer());
            ItemsChanging(value, items);
            items = storageDomainModels;
            ItemsChanged();
            getItemsChangedEvent().raise(this, EventArgs.Empty);
            OnPropertyChanged(new PropertyChangedEventArgs("Items"));
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

                                Linq.Sort(disks, new DiskByInternalDriveMappingComparer());
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

    private void Remove()
    {
        VmTemplate template = (VmTemplate) getEntity();

        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Remove Template Disk(s)");
        model.setHashName("remove_template_disks");
        model.setMessage("Template Disk(s)");

        ArrayList<DiskModel> disks =
                getSelectedItems() != null ? Linq.<DiskModel> Cast(getSelectedItems()) : new ArrayList<DiskModel>();
        ArrayList<String> items = new ArrayList<String>();
        for (DiskModel diskModel : disks)
        {
            items.add(StringFormat.format("%1$s (from Stroage Domain %2$s)",
                    diskModel.getDiskImage().getDiskAlias(),
                    ((storage_domains) diskModel.getStorageDomain().getSelectedItem()).getstorage_name()));
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
        ConfirmationModel model = (ConfirmationModel) getWindow();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        ArrayList<DiskModel> disks = (ArrayList<DiskModel>) getSelectedItems();

        for (DiskModel diskModel : disks)
        {
            RemoveDiskParameters params =
                    new RemoveDiskParameters(diskModel.getDiskImage().getImageId(),
                            ((storage_domains) diskModel.getStorageDomain().getSelectedItem()).getId());
            parameters.add(params);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveDisk, parameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();
                    }
                }, this);

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
        ArrayList<StorageDomainModel> selectedItems = getSelectedItems() != null ?
                Linq.<StorageDomainModel> Cast(getSelectedItems()) : new ArrayList<StorageDomainModel>();

        getRemoveCommand().setIsExecutionAllowed(isRemoveCommandAvailable());
    }

    private boolean isRemoveCommandAvailable()
    {
        ArrayList<DiskModel> disks =
                getSelectedItems() != null ? Linq.<DiskModel> Cast(getSelectedItems()) : new ArrayList<DiskModel>();

        if (disks.isEmpty())
        {
            return false;
        }

        for (DiskModel disk : disks)
        {
            if (disk.getDiskImage().getimageStatus() == ImageStatus.LOCKED
                    || disk.getDiskImage().getstorage_ids().size() == 1)
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getRemoveCommand())
        {
            Remove();
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
