package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.ImportTemplateModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateImportDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class TemplateBackupModel extends VmBackupModel
{
    private ArrayList<Map.Entry<VmTemplate, DiskImageList>> extendedItems;

    public TemplateBackupModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().templateImportTitle());
        setHashName("template_import"); //$NON-NLS-1$
    }

    @Override
    protected void setAppListModel(VmAppListModel value) {
    }

    @Override
    protected void remove()
    {
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
            items.add(template.getName());
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
    protected ImportVmModel getImportModel() {
        ImportTemplateModel model = new ImportTemplateModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().importTemplatesTitle());
        model.setHashName("import_template"); //$NON-NLS-1$
        return model;
    }

    @Override
    protected String getObjectName(Object object) {
        return ((ImportTemplateData) object).getTemplate().getName();
    }

    @Override
    protected void setObjectName(Object object, String name) {
        ((ImportTemplateData) object).getTemplate().setName(name);
    }

    @Override
    protected boolean validateSuffix(String suffix, EntityModel entityModel) {
        for (Object object : objectsToClone) {
            VmTemplate template = ((ImportTemplateData) object).getTemplate();
            if (!validateName(template.getName() + suffix, entityModel, getClonedAppendedNameValidators(null))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected int getMaxClonedNameLength(Object object) {
        return UnitVmModel.VM_TEMPLATE_NAME_MAX_LIMIT;
    }

    @Override
    protected String getAlreadyAssignedClonedNameMessage() {
        return ConstantsManager.getInstance()
                .getMessages()
                .alreadyAssignedClonedTemplateName();
    }

    @Override
    protected String getSuffixCauseToClonedNameCollisionMessage(String existingName) {
        return ConstantsManager.getInstance()
                .getMessages()
                .suffixCauseToClonedTemplateNameCollision(existingName);
    }

    @Override
    protected void executeImport() {
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
        for (Object object : importModel.getItems())
        {
            ImportTemplateData importData = (ImportTemplateData) object;
            VmTemplate template = importData.getTemplate();

            ImportVmTemplateParameters importVmTemplateParameters =
                    new ImportVmTemplateParameters(model.getStoragePool().getId(),
                            getEntity().getId(), Guid.Empty,
                            ((VDSGroup) model.getCluster().getSelectedItem()).getId(),
                            template);
            if (importModel.getClusterQuota().getSelectedItem() != null &&
                    importModel.getClusterQuota().getIsAvailable()) {
                importVmTemplateParameters.setQuotaId(((Quota) importModel.getClusterQuota().getSelectedItem()).getId());
            }

            Map<Guid, Guid> map = new HashMap<Guid, Guid>();
            for (DiskImage disk : template.getDiskList()) {
                map.put(disk.getId(), importModel.getDiskImportData(disk.getId()).getSelectedStorageDomain().getId());

                if (importModel.getDiskImportData(disk.getId()).getSelectedQuota() != null) {
                    disk.setQuotaId(importModel.getDiskImportData(disk.getId()).getSelectedQuota().getId());
                }
            }

            importVmTemplateParameters.setImageToDestinationDomainMap(map);

            if (importData.isExistsInSystem() || (Boolean) importData.getClone().getEntity()) {
                if (!cloneObjectMap.containsKey(template.getId())) {
                    continue;
                }
                importVmTemplateParameters.setImportAsNewEntity(true);
                importVmTemplateParameters.getVmTemplate()
                        .setName(((ImportTemplateData) cloneObjectMap.get(template.getId())).getTemplate().getName());
            }

            prms.add(importVmTemplateParameters);
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

                            String importedTemplates = ""; //$NONNLS1$
                            int counter = 0;
                            boolean toShowConfirmWindow = false;
                            for (Object a : templateBackupModel.getSelectedItems())
                            {
                                VmTemplate template = (VmTemplate) a;
                                if (retVals.get(counter) != null && retVals.get(counter).getCanDoAction()) {
                                    importedTemplates += template.getName() + ", "; //$NON-NLS-1$
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
                                importedTemplates = StringHelper.trimEnd(importedTemplates.trim(), ',');
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
        if (getEntity() == null || getEntity().getStorageDomainType() != StorageDomainType.ImportExport
                || getEntity().getStorageDomainSharedStatus() != StorageDomainSharedStatus.Active)
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
                                TemplateBackupModel.this.extendedItems = items;
                            }
                        };
                        GetAllFromExportDomainQueryParameters tempVar =
                                new GetAllFromExportDomainQueryParameters(dataCenter.getId(), backupModel.getEntity()
                                        .getId());
                        Frontend.RunQuery(VdcQueryType.GetTemplatesFromExportDomain, tempVar, _asyncQuery1);
                    }
                }
            };
            AsyncDataProvider.GetDataCentersByStorageDomain(_asyncQuery, getEntity().getId());
        }
    }

    @Override
    protected void Restore() {
        super.Restore();
        ((TemplateImportDiskListModel) ((ImportTemplateModel) getWindow()).getImportDiskListModel()).setExtendedItems(extendedItems);
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
        if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRestore")) //$NON-NLS-1$
        {
            OnRestore();
        } else {
            super.ExecuteCommand(command);
        }
    }

    @Override
    protected String getListName() {
        return "TemplateBackupModel"; //$NON-NLS-1$
    }
}
