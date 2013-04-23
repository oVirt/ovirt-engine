package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskByAliasComparer;
import org.ovirt.engine.ui.uicommonweb.Linq.StorageDomainModelByNameComparer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

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
        setTitle(ConstantsManager.getInstance().getConstants().storageTitle());
        setHashName("storage"); //$NON-NLS-1$

        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        UpdateActionAvailability();
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
        }

        UpdateActionAvailability();
    }

    @Override
    protected void asyncSearch()
    {
        super.asyncSearch();

        VmTemplate template = (VmTemplate) getEntity();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                new GetStorageDomainsByVmTemplateIdQueryParameters(template.getId())));
        setItems(getAsyncResult().getData());
    }

    @Override
    protected void syncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.syncSearch();

        VmTemplate template = (VmTemplate) getEntity();
        super.syncSearch(VdcQueryType.GetStorageDomainsByVmTemplateId,
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
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
            storageDomainModels = null;
        }
        else
        {
            this.value = value;
            VmTemplate template = (VmTemplate) getEntity();
            AsyncDataProvider.GetTemplateDiskList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            TemplateStorageListModel templateStorageListModel = (TemplateStorageListModel) target;
                            ArrayList<DiskImage> diskImages = (ArrayList<DiskImage>) returnValue;

                            ArrayList<StorageDomain> storageDomains =
                                    Linq.<StorageDomain> Cast(templateStorageListModel.value);
                            ArrayList<StorageDomainModel> storageDomainModels = new ArrayList<StorageDomainModel>();

                            for (StorageDomain storageDomain : storageDomains) {
                                StorageDomainModel storageDomainModel = new StorageDomainModel();
                                storageDomainModel.setStorageDomain(storageDomain);

                                ArrayList<DiskImage> disks = new ArrayList<DiskImage>();
                                for (DiskImage diskImage : diskImages) {
                                    if (diskImage.getStorageIds().contains(storageDomain.getId())) {
                                        disks.add(diskImage);
                                    }
                                }

                                Linq.Sort(disks, new DiskByAliasComparer());
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
        model.setTitle(ConstantsManager.getInstance().getConstants().removeTemplateDisksTitle());
        model.setHashName("remove_template_disks"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().templateDisksMsg());

        ArrayList<DiskModel> disks =
                getSelectedItems() != null ? Linq.<DiskModel> Cast(getSelectedItems()) : new ArrayList<DiskModel>();
        ArrayList<String> items = new ArrayList<String>();
        for (DiskModel diskModel : disks)
        {
            items.add(ConstantsManager.getInstance().getMessages().templateDiskDescription(
                    diskModel.getDisk().getDiskAlias(),
                    ((StorageDomain) diskModel.getStorageDomain().getSelectedItem()).getStorageName()));
        }
        model.setItems(items);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
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
                    new RemoveDiskParameters(diskModel.getDisk().getId(),
                            ((StorageDomain) diskModel.getStorageDomain().getSelectedItem()).getId());
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
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
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
            if (((DiskImage) disk.getDisk()).getImageStatus() == ImageStatus.LOCKED
                    || ((DiskImage) disk.getDisk()).getStorageIds().size() == 1)
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
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
    }

    @Override
    protected String getListName() {
        return "TemplateStorageListModel"; //$NON-NLS-1$
    }
}
