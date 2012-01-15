package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.action.ImprotVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.ImportTemplateModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class TemplateBackupModel extends ManageBackupModel
{

    @Override
    public java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> getSelectedItem()
    {
        return (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>) super.getSelectedItem();
    }

    public void setSelectedItem(java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> value)
    {
        super.setSelectedItem(value);
    }

    public TemplateBackupModel()
    {
        setTitle("Template Import");
    }

    @Override
    protected void remove()
    {
        super.remove();

        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Remove Backed up Template(s)");
        model.setHashName("remove_backed_up_template");
        model.setMessage("Template(s)");
        // model.Items = SelectedItems.Cast<KeyValuePair<VmTemplate, List<DiskImage>>>().Select(a => a.getKey().name);

        java.util.ArrayList<String> items = new java.util.ArrayList<String>();
        for (Object a : getSelectedItems())
        {
            java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> item =
                    (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>) a;
            VmTemplate template = item.getKey();
            items.add(template.getname());
        }
        model.setItems(items);

        model.setNote("Note: The deleted items might still appear on the sub-tab, since the remove operation might be long. Use the Refresh button, to get the updated status.");

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
        storage_pool pool = DataProvider.GetFirstStoragePoolByStorageDomain(getEntity().getid());
        // Frontend.RunMultipleActions(VdcActionType.RemoveVmTemplateFromImportExport,
        // SelectedItems.Cast<KeyValuePair<VmTemplate, List<DiskImage>>>()
        // .Select(a => (VdcActionParametersBase)new VmTemplateImportExportParameters(a.getKey().vmt_guid, Entity.id,
        // pool.id))
        // .ToList()
        // );
        java.util.ArrayList<VdcActionParametersBase> prms = new java.util.ArrayList<VdcActionParametersBase>();
        for (Object a : getSelectedItems())
        {
            java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> item =
                    (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>) a;
            VmTemplate template = item.getKey();
            prms.add(new VmTemplateImportExportParameters(template.getId(), getEntity().getid(), pool.getId()));
        }

        Frontend.RunMultipleAction(VdcActionType.RemoveVmTemplateFromImportExport, prms);

        Cancel();
        OnEntityChanged();
    }

    @Override
    protected void Restore()
    {
        super.Restore();

        if (getWindow() != null)
        {
            return;
        }

        ImportTemplateModel model = new ImportTemplateModel();
        setWindow(model);
        model.setTitle("Import Template(s)");
        model.setHashName("import_template");
        java.util.ArrayList<VDSGroup> clusters = DataProvider.GetClusterListByStorageDomain(getEntity().getid());

        model.getCluster().setItems(clusters);
        model.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));

        model.setSourceStorage(getEntity().getStorageStaticData());
        model.setStoragePool(DataProvider.GetFirstStoragePoolByStorageDomain(getEntity().getStorageStaticData().getId()));

        // var destStorages = DataProvider.GetDataDomainsListByDomain(Entity.id)
        // .Where(a => (a.storage_domain_type == StorageDomainType.Data || a.storage_domain_type ==
        // StorageDomainType.Master)
        // && a.status.HasValue && a.status.Value == StorageDomainStatus.Active)
        // .ToList();

        java.util.ArrayList<storage_domains> destStorages = new java.util.ArrayList<storage_domains>();
        for (storage_domains domain : DataProvider.GetDataDomainsListByDomain(getEntity().getid()))
        {
            if ((domain.getstorage_domain_type() == StorageDomainType.Data || domain.getstorage_domain_type() == StorageDomainType.Master)
                    && domain.getstatus() != null && domain.getstatus() == StorageDomainStatus.Active)
            {
                destStorages.add(domain);
            }
        }

        model.getDestinationStorage().setItems(destStorages);
        model.getDestinationStorage().setSelectedItem(Linq.FirstOrDefault(destStorages));

        model.setItems(getSelectedItems());

        if (destStorages.isEmpty())
        {
            model.getDestinationStorage().setIsChangable(false);
            model.getDestinationStorage().getChangeProhibitionReasons().add("Cannot import Template.");

            model.setMessage("There is no Data Storage Domain to import the Template into. Please attach a Data Storage Domain to the Template's Data Center.");

            UICommand tempVar = new UICommand("Cancel", this);
            tempVar.setTitle("Close");
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnRestore", this);
            tempVar2.setTitle("OK");
            tempVar2.setIsDefault(true);
            model.getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", this);
            tempVar3.setTitle("Cancel");
            tempVar3.setIsCancel(true);
            model.getCommands().add(tempVar3);
        }
    }

    private void OnRestore()
    {
        ImportTemplateModel model = (ImportTemplateModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        // List<VdcReturnValueBase> ret = Frontend.RunMultipleActions(VdcActionType.ImportVmTemplate,
        // SelectedItems.Cast<KeyValuePair<VmTemplate, List<DiskImage>>>()
        // .Select(a => (VdcActionParametersBase)new ImprotVmTemplateParameters(model.StoragePool.id,
        // model.SourceStorage.id,
        // model.DestinationStorage.ValueAs<storage_domains>().id,
        // model.Cluster.ValueAs<VDSGroup>().ID,
        // a.getKey())
        // )
        // .ToList()
        // );
        java.util.ArrayList<VdcActionParametersBase> prms = new java.util.ArrayList<VdcActionParametersBase>();
        for (Object a : getSelectedItems())
        {
            java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> item =
                    (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>) a;
            prms.add(new ImprotVmTemplateParameters(model.getStoragePool().getId(),
                    model.getSourceStorage().getId(),
                    ((storage_domains) model.getDestinationStorage().getSelectedItem()).getid(),
                    ((VDSGroup) model.getCluster().getSelectedItem()).getID(),
                    item.getKey()));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ImportVmTemplate, prms,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        TemplateBackupModel templateBackupModel = (TemplateBackupModel) result.getState();
                        templateBackupModel.getWindow().StopProgress();
                        templateBackupModel.Cancel();
                        java.util.ArrayList<VdcReturnValueBase> retVals =
                                (java.util.ArrayList<VdcReturnValueBase>) result.getReturnValue();
                        if (retVals != null && templateBackupModel.getSelectedItems().size() == retVals.size())
                        {
                            ConfirmationModel confirmModel = new ConfirmationModel();
                            templateBackupModel.setConfirmWindow(confirmModel);
                            confirmModel.setTitle("Import Template(s)");
                            confirmModel.setHashName("import_template");
                            String importedTemplates = "";
                            int counter = 0;
                            for (Object a : templateBackupModel.getSelectedItems())
                            {
                                java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> item =
                                        (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>) a;
                                VmTemplate template = item.getKey();
                                if (retVals.get(counter) != null && retVals.get(counter).getSucceeded()) {
                                    importedTemplates += template.getname() + ", ";
                                }
                                counter++;
                            }
                            StringHelper.trimEnd(importedTemplates.trim(), ',');
                            confirmModel.setMessage(StringFormat.format("Import process has begun for Template(s): %1$s.\nYou can check import status in the 'Events' tab of the specific destination storage domain, or in the main 'Events' tab",
                                    importedTemplates));
                            UICommand tempVar = new UICommand("CancelConfirm", templateBackupModel);
                            tempVar.setTitle("Close");
                            tempVar.setIsDefault(true);
                            tempVar.setIsCancel(true);
                            confirmModel.getCommands().add(tempVar);
                        }

                    }
                },
                this);
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("storage_domain_shared_status"))
        {
            getSearchCommand().Execute();
        }
    }

    @Override
    protected void SyncSearch()
    {
        super.SyncSearch();

        if (getEntity() == null || getEntity().getstorage_domain_type() != StorageDomainType.ImportExport
                || getEntity().getstorage_domain_shared_status() != StorageDomainSharedStatus.Active)
        {
            setItems(null);
        }
        else
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object ReturnValue)
                {
                    TemplateBackupModel backupModel = (TemplateBackupModel) model;
                    java.util.ArrayList<storage_pool> list = (java.util.ArrayList<storage_pool>) ReturnValue;
                    if (list != null && list.size() > 0)
                    {
                        storage_pool dataCenter = list.get(0);
                        AsyncQuery _asyncQuery1 = new AsyncQuery();
                        _asyncQuery1.setModel(backupModel);
                        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object model1, Object ReturnValue1)
                            {
                                TemplateBackupModel backupModel1 = (TemplateBackupModel) model1;
                                java.util.ArrayList<java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>> items =
                                        new java.util.ArrayList<java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>>();
                                java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>> dictionary =
                                        (java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>>) ((VdcQueryReturnValue) ReturnValue1).getReturnValue();

                                for (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> item : dictionary.entrySet())
                                {
                                    items.add(item);
                                }
                                backupModel1.setItems(items);
                            }
                        };
                        GetAllFromExportDomainQueryParamenters tempVar =
                                new GetAllFromExportDomainQueryParamenters(dataCenter.getId(), backupModel.getEntity()
                                        .getid());
                        tempVar.setGetAll(true);
                        Frontend.RunQuery(VdcQueryType.GetTemplatesFromExportDomain, tempVar, _asyncQuery1);
                    }
                }
            };
            AsyncDataProvider.GetDataCentersByStorageDomain(_asyncQuery, getEntity().getid());
        }
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();
        SyncSearch();
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRestore"))
        {
            OnRestore();
        }
    }

    @Override
    protected String getListName() {
        return "TemplateBackupModel";
    }
}
