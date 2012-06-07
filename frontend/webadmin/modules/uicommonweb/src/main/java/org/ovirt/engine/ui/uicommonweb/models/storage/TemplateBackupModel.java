package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.ImportTemplateModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class TemplateBackupModel extends ManageBackupModel
{

    private ArrayList<Map.Entry<VmTemplate, DiskImageList>> extendedItems;

    public TemplateBackupModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().templateImportTitle());
        setHashName("template_import"); //$NON-NLS-1$
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
        model.setTitle(ConstantsManager.getInstance().getConstants().removeBackedUpTemplatesTitle());
        model.setHashName("remove_backed_up_template"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().templatesMsg());
        ArrayList<String> items = new ArrayList<String>();
        for (Object a : getSelectedItems())
        {
            VmTemplate template = (VmTemplate) a;
            items.add(template.getname());
        }
        model.setItems(items);

        model.setNote(ConstantsManager.getInstance().getConstants().noteTheDeletedItemsMightStillAppearOntheSubTab());

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
        AsyncDataProvider.GetDataCentersByStorageDomain(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                TemplateBackupModel templateBackupModel = (TemplateBackupModel) model;
                ArrayList<storage_pool> pools = (ArrayList<storage_pool>) returnValue;
                if (pools != null && pools.size() > 0) {
                    storage_pool pool = pools.get(0);
                    ArrayList<VdcActionParametersBase> prms =
                            new ArrayList<VdcActionParametersBase>();
                    for (Object a : templateBackupModel.getSelectedItems())
                    {
                        VmTemplate template = (VmTemplate) a;
                        prms.add(new VmTemplateImportExportParameters(template.getId(),
                                getEntity().getId(),
                                pool.getId()));
                    }

                    Frontend.RunMultipleAction(VdcActionType.RemoveVmTemplateFromImportExport, prms);
                }
            }
        }),
                getEntity().getId());
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
        model.setTitle(ConstantsManager.getInstance().getConstants().importTemplatesTitle());
        model.setHashName("import_template"); //$NON-NLS-1$
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.Model = this;
        _asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object returnModel, Object returnValue) {
                TemplateBackupModel templateBackupModel = (TemplateBackupModel) returnModel;
                ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;

                ImportTemplateModel iTemplateModel = (ImportTemplateModel) templateBackupModel.getWindow();
                iTemplateModel.getCluster().setItems(clusters);
                iTemplateModel.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));

                iTemplateModel.setSourceStorage(templateBackupModel.getEntity().getStorageStaticData());

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
                        ImportTemplateModel iTemplateModel1 = (ImportTemplateModel) tempalteBackupModel1.getWindow();
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
                                ArrayList<storage_domains> destStorages =
                                        new ArrayList<storage_domains>();
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
                                            .add("Cannot import Template."); //$NON-NLS-1$

                                    iTemplateModel2.setMessage(ConstantsManager.getInstance()
                                            .getConstants()
                                            .thereIsNoDataStorageDomainToImportTemplateIntoMsg());

                                    UICommand tempVar = new UICommand("Cancel", tempalteBackupModel2); //$NON-NLS-1$
                                    tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
                                    tempVar.setIsDefault(true);
                                    tempVar.setIsCancel(true);
                                    iTemplateModel2.getCommands().add(tempVar);
                                }
                                else
                                {
                                    UICommand tempVar2 = new UICommand("OnRestore", tempalteBackupModel2); //$NON-NLS-1$
                                    tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
                                    tempVar2.setIsDefault(true);
                                    iTemplateModel2.getCommands().add(tempVar2);
                                    UICommand tempVar3 = new UICommand("Cancel", tempalteBackupModel2); //$NON-NLS-1$
                                    tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
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
                        templateBackupModel.getEntity().getId());
            }
        };

        AsyncDataProvider.GetClusterListByStorageDomain(_asyncQuery,
                getEntity().getId());
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
        ArrayList<VdcActionParametersBase> prms = new ArrayList<VdcActionParametersBase>();
        for (Object object : getSelectedItems())
        {
            VmTemplate template = (VmTemplate) object;
            storage_domains destinationStorage = ((storage_domains) model.getDestinationStorage().getSelectedItem());
            boolean isSingleDestStorage = (Boolean) model.getIsSingleDestStorage().getEntity();
            Guid destinationStorageId = destinationStorage != null && isSingleDestStorage ?
                    destinationStorage.getId() : Guid.Empty;

            ImprotVmTemplateParameters improtVmTemplateParameters =
                    new ImprotVmTemplateParameters(model.getStoragePool().getId(),
                            model.getSourceStorage().getId(), destinationStorageId,
                            ((VDSGroup) model.getCluster().getSelectedItem()).getId(),
                            template);

            if (!(Boolean) model.getIsSingleDestStorage().getEntity()) {
                HashMap<Guid, Guid> map = model.getDiskStorageMap().get(template.getId());
                improtVmTemplateParameters.setImageToDestinationDomainMap(map);
            }

            if ((Boolean) model.getCloneAllTemplates().getEntity()
                    || ((Boolean) model.getCloneOnlyDuplicateTemplates().getEntity()
                    && model.isObjectInSetup(template))) {
                improtVmTemplateParameters.setImportAsNewEntity(true);
                template.setname(template.getname() + model.getCloneTemplatesSuffix().getEntity());
            }

            prms.add(improtVmTemplateParameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ImportVmTemplate, prms,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        TemplateBackupModel templateBackupModel = (TemplateBackupModel) result.getState();
                        templateBackupModel.getWindow().StopProgress();
                        templateBackupModel.Cancel();
                        ArrayList<VdcReturnValueBase> retVals =
                                (ArrayList<VdcReturnValueBase>) result.getReturnValue();
                        if (retVals != null && templateBackupModel.getSelectedItems().size() == retVals.size())
                        {

                            String importedTemplates = ""; //$NON-NLS-1$
                            int counter = 0;
                            boolean toShowConfirmWindow = false;
                            for (Object a : templateBackupModel.getSelectedItems())
                            {
                                VmTemplate template = (VmTemplate) a;
                                if (retVals.get(counter) != null && retVals.get(counter).getCanDoAction()) {
                                    importedTemplates += template.getname() + ", "; //$NON-NLS-1$
                                    toShowConfirmWindow = true;
                                }
                                counter++;
                            }
                            if (toShowConfirmWindow) {
                                ConfirmationModel confirmModel = new ConfirmationModel();
                                templateBackupModel.setConfirmWindow(confirmModel);
                                confirmModel.setTitle(ConstantsManager.getInstance()
                                        .getConstants()
                                        .importTemplatesTitle());
                                confirmModel.setHashName("import_template"); //$NON-NLS-1$
                                StringHelper.trimEnd(importedTemplates.trim(), ',');
                                confirmModel.setMessage(ConstantsManager.getInstance()
                                        .getMessages()
                                        .importProcessHasBegunForTemplates(importedTemplates));
                                UICommand tempVar = new UICommand("CancelConfirm", templateBackupModel); //$NON-NLS-1$
                                tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
                                tempVar.setIsDefault(true);
                                tempVar.setIsCancel(true);
                                confirmModel.getCommands().add(tempVar);
                            }
                        }

                    }
                },
                this);
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("storage_domain_shared_status")) //$NON-NLS-1$
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
                    ArrayList<storage_pool> list = (ArrayList<storage_pool>) ReturnValue;
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
                                ArrayList<Map.Entry<VmTemplate, DiskImageList>> items =
                                        new ArrayList<Map.Entry<VmTemplate, DiskImageList>>();
                                HashMap<VmTemplate, DiskImageList> dictionary =
                                        (HashMap<VmTemplate, DiskImageList>) ((VdcQueryReturnValue) ReturnValue1).getReturnValue();

                                ArrayList<VmTemplate> list = new ArrayList<VmTemplate>();
                                for (Map.Entry<VmTemplate, DiskImageList> item : dictionary.entrySet())
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
                                        .getId());
                        tempVar.setGetAll(true);
                        Frontend.RunQuery(VdcQueryType.GetTemplatesFromExportDomain, tempVar, _asyncQuery1);
                    }
                }
            };
            AsyncDataProvider.GetDataCentersByStorageDomain(_asyncQuery, getEntity().getId());
        }
    }

    protected void setExtendedItems(ArrayList<Map.Entry<VmTemplate, DiskImageList>> items) {
        this.extendedItems = items;
    }

    public ArrayList<Map.Entry<VmTemplate, DiskImageList>> getExtendedItems() {
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

        if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRestore")) //$NON-NLS-1$
        {
            OnRestore();
        }
    }

    @Override
    protected String getListName() {
        return "TemplateBackupModel"; //$NON-NLS-1$
    }
}
