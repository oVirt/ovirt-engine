package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;

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
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
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
import org.ovirt.engine.ui.uicommonweb.models.templates.ImportTemplateModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class TemplateBackupModel extends ManageBackupModel
{

    private ArrayList<Entry<VmTemplate, DiskImageList>> extendedItems;

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
        java.util.ArrayList<String> items = new java.util.ArrayList<String>();
        for (Object a : getSelectedItems())
        {
            VmTemplate template = (VmTemplate) a;
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
        AsyncDataProvider.GetDataCentersByStorageDomain(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                TemplateBackupModel templateBackupModel = (TemplateBackupModel) model;
                ArrayList<storage_pool> pools = (ArrayList<storage_pool>) returnValue;
                if (pools != null && pools.size() > 0) {
                    storage_pool pool = pools.get(0);
                    java.util.ArrayList<VdcActionParametersBase> prms =
                            new java.util.ArrayList<VdcActionParametersBase>();
                    for (Object a : templateBackupModel.getSelectedItems())
                    {
                        VmTemplate template = (VmTemplate) a;
                        prms.add(new VmTemplateImportExportParameters(template.getId(),
                                getEntity().getid(),
                                pool.getId()));
                    }

                    Frontend.RunMultipleAction(VdcActionType.RemoveVmTemplateFromImportExport, prms);
                }
            }
        }),
                getEntity().getid());
        Cancel();
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
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.Model = this;
        _asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object returnModel, Object returnValue) {
                TemplateBackupModel templateBackupModel = (TemplateBackupModel) returnModel;
                java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) returnValue;

                ImportTemplateModel iTemplateModel = (ImportTemplateModel) templateBackupModel
                        .getWindow();
                iTemplateModel.getCluster().setItems(clusters);
                iTemplateModel.getCluster().setSelectedItem(
                        Linq.FirstOrDefault(clusters));

                iTemplateModel.setSourceStorage(templateBackupModel.getEntity()
                        .getStorageStaticData());

                AsyncQuery _asyncQuery1 = new AsyncQuery();
                _asyncQuery1.Model = templateBackupModel;
                _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object returnModel1,
                            Object returnValue1) {
                        ArrayList<storage_pool> pools = (ArrayList<storage_pool>) returnValue1;
                        storage_pool pool = null;
                        if (pools != null && pools.size() > 0) {
                            pool = pools.get(0);
                        }
                        TemplateBackupModel tempalteBackupModel1 = (TemplateBackupModel) returnModel1;
                        ImportTemplateModel iTemplateModel1 = (ImportTemplateModel) tempalteBackupModel1
                                .getWindow();
                        iTemplateModel1.setStoragePool(pool);

                        AsyncQuery _asyncQuery2 = new AsyncQuery();
                        _asyncQuery2.Model = tempalteBackupModel1;
                        _asyncQuery2.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object returnModel2,
                                    Object returnValue2) {
                                TemplateBackupModel tempalteBackupModel2 = (TemplateBackupModel) returnModel2;
                                ImportTemplateModel iTemplateModel2 =
                                        (ImportTemplateModel) tempalteBackupModel2.getWindow();
                                java.util.ArrayList<storage_domains> destStorages =
                                        new java.util.ArrayList<storage_domains>();
                                ArrayList<storage_domains> list = (ArrayList<storage_domains>) returnValue2;
                                for (storage_domains domain : list)
                                {
                                    if ((domain.getstorage_domain_type() == StorageDomainType.Data || domain.getstorage_domain_type() == StorageDomainType.Master)
                                            && domain.getstatus() != null
                                            && domain.getstatus() == StorageDomainStatus.Active)
                                    {
                                        destStorages.add(domain);
                                    }
                                }

                                iTemplateModel2.getDestinationStorage().setItems(destStorages);
                                iTemplateModel2.getDestinationStorage()
                                        .setSelectedItem(Linq.FirstOrDefault(destStorages));

                                iTemplateModel2.setItems(getSelectedItems());
                                iTemplateModel2.setExtendedItems(getExtendedItems());
                                if (destStorages.isEmpty())
                                {
                                    iTemplateModel2.getDestinationStorage().setIsChangable(false);
                                    iTemplateModel2.getDestinationStorage()
                                            .getChangeProhibitionReasons()
                                            .add("Cannot import Template.");

                                    iTemplateModel2.setMessage("There is no Data Storage Domain to import the Template into. Please attach a Data Storage Domain to the Template's Data Center.");

                                    UICommand tempVar = new UICommand("Cancel", tempalteBackupModel2);
                                    tempVar.setTitle("Close");
                                    tempVar.setIsDefault(true);
                                    tempVar.setIsCancel(true);
                                    iTemplateModel2.getCommands().add(tempVar);
                                }
                                else
                                {
                                    UICommand tempVar2 = new UICommand("OnRestore", tempalteBackupModel2);
                                    tempVar2.setTitle("OK");
                                    tempVar2.setIsDefault(true);
                                    iTemplateModel2.getCommands().add(tempVar2);
                                    UICommand tempVar3 = new UICommand("Cancel", tempalteBackupModel2);
                                    tempVar3.setTitle("Cancel");
                                    tempVar3.setIsCancel(true);
                                    iTemplateModel2.getCommands().add(tempVar3);
                                }
                            }
                        };
                        AsyncDataProvider.GetDataDomainsListByDomain(_asyncQuery2, iTemplateModel1
                                .getSourceStorage().getId());

                    }

                };
                AsyncDataProvider.GetDataCentersByStorageDomain(_asyncQuery1,
                        templateBackupModel.getEntity().getid());
            }
        };

        AsyncDataProvider.GetClusterListByStorageDomain(_asyncQuery,
                getEntity().getid());
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
        java.util.ArrayList<VdcActionParametersBase> prms = new java.util.ArrayList<VdcActionParametersBase>();
        for (Object a : getSelectedItems())
        {
            VmTemplate item = (VmTemplate) a;
            prms.add(new ImprotVmTemplateParameters(model.getStoragePool().getId(),
                    model.getSourceStorage().getId(),
                    ((storage_domains) model.getDestinationStorage().getSelectedItem()).getid(),
                    ((VDSGroup) model.getCluster().getSelectedItem()).getID(),
                    item));
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
                                VmTemplate template = (VmTemplate) a;
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
            setItems(Collections.emptyList());
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
                                java.util.ArrayList<java.util.Map.Entry<VmTemplate, DiskImageList>> items =
                                        new java.util.ArrayList<java.util.Map.Entry<VmTemplate, DiskImageList>>();
                                java.util.HashMap<VmTemplate, DiskImageList> dictionary =
                                        (java.util.HashMap<VmTemplate, DiskImageList>) ((VdcQueryReturnValue) ReturnValue1).getReturnValue();

                                ArrayList<VmTemplate> list = new ArrayList<VmTemplate>();
                                for (java.util.Map.Entry<VmTemplate, DiskImageList> item : dictionary.entrySet())
                                {
                                    items.add(item);
                                    VmTemplate template = item.getKey();
                                    template.setDiskList(new ArrayList<DiskImage>());
                                    for (DiskImage diskImage : item.getValue().getDiskImages()) {
                                        template.getDiskList().add(diskImage);
                                    }
                                    list.add(template);
                                }
                                backupModel1.setItems(list);
                                backupModel1.setExtendedItems(items);
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

    protected void setExtendedItems(ArrayList<Entry<VmTemplate, DiskImageList>> items) {
        this.extendedItems = items;
    }

    public ArrayList<Entry<VmTemplate, DiskImageList>> getExtendedItems() {
        return extendedItems;
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
