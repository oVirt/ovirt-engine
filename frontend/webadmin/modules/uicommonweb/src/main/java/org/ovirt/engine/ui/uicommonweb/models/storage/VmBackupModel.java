package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParamenters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class VmBackupModel extends ManageBackupModel {

    private VmAppListModel privateAppListModel;
    protected Map<Guid, Object> objectsInSetupMap;
    protected Map<Guid, Object> cloneObjectMap;
    protected ImportVmModel importModel;

    public VmAppListModel getAppListModel() {
        return privateAppListModel;
    }

    protected void setAppListModel(VmAppListModel value) {
        privateAppListModel = value;
    }

    public VmBackupModel() {
        setTitle(ConstantsManager.getInstance().getConstants().vmImportTitle());
        setHashName("vm_import"); // //$NON-NLS-1$

        setAppListModel(new VmAppListModel());
    }

    @Override
    protected void OnSelectedItemChanged() {
        super.OnSelectedItemChanged();
        if (getAppListModel() != null) {
            getAppListModel().setEntity(getSelectedItem());
        }
    }

    @Override
    protected void remove() {
        super.remove();

        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeBackedUpVMsTitle());
        model.setHashName("remove_backed_up_vm"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().vmsMsg());

        ArrayList<String> items = new ArrayList<String>();
        for (Object item : getSelectedItems()) {
            VM vm = (VM) item;
            items.add(vm.getVmName());
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

    private void OnRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        model.StartProgress(null);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.Model = this;
        _asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                ArrayList<storage_pool> pools = (ArrayList<storage_pool>) returnValue;
                if (pools != null && pools.size() > 0) {
                    storage_pool pool = pools.get(0);
                    VmBackupModel backupModel = (VmBackupModel) model;
                    ArrayList<VdcActionParametersBase> list =
                            new ArrayList<VdcActionParametersBase>();
                    for (Object item : backupModel.getSelectedItems()) {
                        VM vm = (VM) item;
                        list.add(new RemoveVmFromImportExportParamenters(vm,
                                backupModel.getEntity().getId(), pool.getId()));
                    }

                    Frontend.RunMultipleAction(
                            VdcActionType.RemoveVmFromImportExport, list,
                            new IFrontendMultipleActionAsyncCallback() {
                                @Override
                                public void Executed(
                                        FrontendMultipleActionAsyncResult result) {

                                    ConfirmationModel localModel = (ConfirmationModel) result
                                            .getState();
                                    localModel.StopProgress();
                                    Cancel();
                                    OnEntityChanged();

                                }
                            }, backupModel.getWindow());
                }
            }
        };
        AsyncDataProvider.GetDataCentersByStorageDomain(_asyncQuery,
                getEntity().getId());
    }

    @Override
    protected void Restore() {
        super.Restore();

        if (getWindow() != null) {
            return;
        }

        ImportVmModel model = getImportModel();
        setWindow(model);
        model.StartProgress(null);
        UICommand restoreCommand;
        restoreCommand = new UICommand("OnRestore", this); //$NON-NLS-1$
        restoreCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        restoreCommand.setIsDefault(true);
        model.getCommands().add(restoreCommand);
        UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar3.setIsCancel(true);
        model.getCommands().add(tempVar3);
        model.setItems(getSelectedItems());
        model.init(getEntity().getId());

        // Add 'Close' command
        UICommand closeCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        closeCommand.setTitle(ConstantsManager.getInstance().getConstants().close());
        closeCommand.setIsDefault(true);
        closeCommand.setIsCancel(true);
        model.setCloseCommand(closeCommand);
    }

    protected ImportVmModel getImportModel() {
        ImportVmModel model = new ImportVmModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().importVirtualMachinesTitle());
        model.setHashName("import_virtual_machine"); //$NON-NLS-1$
        model.setEntity(getEntity());
        return model;
    }

    public void OnRestore() {
        importModel = (ImportVmModel) getWindow();

        if (importModel.getProgress() != null) {
            return;
        }

        if (!importModel.Validate()) {
            return;
        }
        cloneObjectMap = new HashMap<Guid, Object>();

        objectsInSetupMap = new HashMap<Guid, Object>();
        for (Object object : (ArrayList<Object>) importModel.getItems()) {
            if (importModel.isObjectInSetup(object) || (Boolean) importModel.getCloneAll().getEntity()) {
                objectsInSetupMap.put((Guid) ((IVdcQueryable) object).getQueryableId(), object);
            }
        }
        executeImportClone();
    }

    private void executeImportClone() {
        //TODO: support running numbers (for suffix)
        if (objectsInSetupMap.size() == 0) {
            executeImport();
            return;
        }
        ImportCloneModel entity = new ImportCloneModel();
        Object object = objectsInSetupMap.values().toArray()[0];
        entity.setEntity(object);
        entity.setTitle(ConstantsManager.getInstance().getConstants().importConflictTitle());
        entity.setHashName("import_conflict"); //$NON-NLS-1$
        UICommand command = new UICommand("onClone", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        entity.getCommands().add(command);
        command = new UICommand("closeClone", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        entity.getCommands().add(command);

        setConfirmWindow(entity);

    }

    private void onClone() {
        ImportCloneModel cloneModel = (ImportCloneModel) getConfirmWindow();
        if ((Boolean) cloneModel.getApplyToAll().getEntity()) {
            if (!(Boolean) cloneModel.getNoClone().getEntity()) {
                String suffix = (String) cloneModel.getSuffix().getEntity();
                if (!validateSuffix(suffix, cloneModel.getSuffix())) {
                    return;
                }
                for (Object object : objectsInSetupMap.values()) {
                    setObjectName(object, suffix, true);
                    cloneObjectMap.put((Guid) ((IVdcQueryable) object).getQueryableId(), object);
                }
            }
            objectsInSetupMap.clear();
        } else {
            Object object = cloneModel.getEntity();
            if (!(Boolean) cloneModel.getNoClone().getEntity()) {
                String vmName = (String) cloneModel.getName().getEntity();
                if (!validateName(vmName, object, cloneModel.getName())) {
                    return;
                }
                setObjectName(object, vmName, false);
                cloneObjectMap.put((Guid) ((IVdcQueryable) object).getQueryableId(), object);
            }
            objectsInSetupMap.remove(((IVdcQueryable) object).getQueryableId());
        }

        setConfirmWindow(null);
        executeImportClone();
    }

    protected void setObjectName(Object object, String name, boolean isSuffix) {
        VM vm = (VM) object;
        if (isSuffix) {
            vm.setVmName(vm.getVmName() + name);
        } else {
            vm.setVmName(name);
        }
    }

    protected boolean validateSuffix(String suffix, EntityModel entityModel) {
        for (Object object : objectsInSetupMap.values()) {
            VM vm = (VM) object;
            if (!validateName(vm.getVmName() + suffix, vm, entityModel)) {
                return false;
            }
        }
        return true;
    }

    protected boolean validateName(String newVmName, Object object, EntityModel entity) {
        VM vm = (VM) object;
        VmOsType osType = vm.getOs();
        EntityModel temp = new EntityModel();
        temp.setIsValid(true);
        temp.setEntity(newVmName);
        final int length = AsyncDataProvider.IsWindowsOsType(osType) ? UnitVmModel.WINDOWS_VM_NAME_MAX_LIMIT
                : UnitVmModel.NON_WINDOWS_VM_NAME_MAX_LIMIT;
        temp.ValidateEntity(
                new IValidation[] {
                        new NotEmptyValidation(),
                        new LengthValidation(length),
                        new I18NNameValidation() {
                            @Override
                            protected String composeMessage() {
                                return ConstantsManager.getInstance()
                                        .getMessages()
                                        .newNameWithSuffixCannotContainBlankOrSpecialChars(length);
                            };
                        }
                });
        if (!temp.getIsValid()) {
            entity.setInvalidityReasons(temp.getInvalidityReasons());
            entity.setIsValid(false);
        }

        return temp.getIsValid();
    }

    private void closeClone() {
        setConfirmWindow(null);
    }

    protected void executeImport() {
        ArrayList<VdcActionParametersBase> prms = new ArrayList<VdcActionParametersBase>();

        for (Object item : importModel.getItems()) {
            VM vm = (VM) item;

            storage_domains destinationStorage =
                    ((storage_domains) importModel.getStorage().getSelectedItem());

            ImportVmParameters prm = new ImportVmParameters(vm, getEntity().getId(),
                    Guid.Empty, importModel.getStoragePool().getId(),
                    ((VDSGroup) importModel.getCluster().getSelectedItem()).getId());

            if (importModel.getClusterQuota().getSelectedItem() != null &&
                    importModel.getClusterQuota().getIsAvailable()) {
                prm.setQuotaId(((Quota) importModel.getClusterQuota().getSelectedItem()).getId());
            }

            prm.setForceOverride(true);
            prm.setCopyCollapse((Boolean) importModel.getCollapseSnapshots().getEntity());

            Map<Guid, Guid> map = new HashMap<Guid, Guid>();
            for (Map.Entry<Guid, Disk> entry : vm.getDiskMap().entrySet()) {
                Guid key = entry.getKey();
                DiskImage disk = (DiskImage) entry.getValue();
                map.put(disk.getId(), importModel.getDiskImportData(disk.getId()).getSelectedStorageDomain().getId());
                disk.setvolume_format(
                        AsyncDataProvider.GetDiskVolumeFormat(
                                importModel.getDiskImportData(disk.getId()).getSelectedVolumeType(),
                                importModel.getDiskImportData(
                                        disk.getId()).getSelectedStorageDomain().getstorage_type()));
                disk.setvolume_type(importModel.getDiskImportData(disk.getId()).getSelectedVolumeType());

                if (importModel.getDiskImportData(disk.getId()).getSelectedQuota() != null) {
                    disk.setQuotaId(
                            importModel.getDiskImportData(disk.getId()).getSelectedQuota().getId());
                }
            }

            prm.setImageToDestinationDomainMap(map);

            if (importModel.isObjectInSetup(vm) ||
                    (Boolean) importModel.getCloneAll().getEntity()) {
                if (!cloneObjectMap.containsKey(vm.getId())) {
                    continue;
                }
                prm.setImportAsNewEntity(true);
                prm.setCopyCollapse(true);
                prm.getVm().setVmName(((VM) cloneObjectMap.get(vm.getId())).getVmName());
            }

            prms.add(prm);

        }

        importModel.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ImportVm, prms,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(
                            FrontendMultipleActionAsyncResult result) {

                        VmBackupModel vmBackupModel = (VmBackupModel) result
                                .getState();
                        vmBackupModel.getWindow().StopProgress();
                        vmBackupModel.Cancel();
                        ArrayList<VdcReturnValueBase> retVals =
                                (ArrayList<VdcReturnValueBase>) result
                                        .getReturnValue();
                        if (retVals != null
                                && vmBackupModel.getSelectedItems().size() == retVals
                                        .size()) {
                            String importedVms = ""; //$NON-NLS-1$
                            int counter = 0;
                            boolean toShowConfirmWindow = false;
                            for (Object item : vmBackupModel.getSelectedItems()) {
                                VM vm = (VM) item;
                                if (retVals.get(counter) != null
                                        && retVals.get(counter).getCanDoAction()) {
                                    importedVms += vm.getVmName() + ", "; //$NON-NLS-1$
                                    toShowConfirmWindow = true;
                                }
                                counter++;
                            }
                            // show the confirm window only if the import has been successfully started for at least one
                            // VM
                            if (toShowConfirmWindow) {
                                ConfirmationModel confirmModel = new ConfirmationModel();
                                vmBackupModel.setConfirmWindow(confirmModel);
                                confirmModel.setTitle(ConstantsManager.getInstance()
                                        .getConstants()
                                        .importVirtualMachinesTitle());
                                confirmModel.setHashName("import_virtual_machine"); //$NON-NLS-1$
                                importedVms = StringHelper.trimEnd(importedVms.trim(), ',');
                                confirmModel.setMessage(ConstantsManager.getInstance().getMessages().importProcessHasBegunForVms(importedVms));
                                UICommand tempVar2 = new UICommand("CancelConfirm", //$NON-NLS-1$
                                        vmBackupModel);
                                tempVar2.setTitle(ConstantsManager.getInstance().getConstants().close());
                                tempVar2.setIsDefault(true);
                                tempVar2.setIsCancel(true);
                                confirmModel.getCommands().add(tempVar2);
                            }
                        }

                    }
                },
                this);
    }

    @Override
    protected void EntityPropertyChanged(Object sender,
            PropertyChangedEventArgs e) {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("storage_domain_shared_status")) { //$NON-NLS-1$
            getSearchCommand().Execute();
        }
    }

    @Override
    protected void SyncSearch() {
        super.SyncSearch();

        if (getEntity() == null
                || getEntity().getstorage_domain_type() != StorageDomainType.ImportExport
                || getEntity().getstorage_domain_shared_status() != StorageDomainSharedStatus.Active) {
            setItems(Collections.emptyList());
        } else {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object ReturnValue) {
                    VmBackupModel backupModel = (VmBackupModel) model;
                    ArrayList<storage_pool> list = (ArrayList<storage_pool>) ReturnValue;
                    if (list != null && list.size() > 0) {
                        storage_pool dataCenter = list.get(0);
                        AsyncQuery _asyncQuery1 = new AsyncQuery();
                        _asyncQuery1.setModel(backupModel);
                        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object model1,
                                    Object ReturnValue1) {
                                VmBackupModel backupModel1 = (VmBackupModel) model1;

                                backupModel1
                                        .setItems((ArrayList<VM>) ((VdcQueryReturnValue) ReturnValue1)
                                                .getReturnValue());
                            }
                        };
                        GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(
                                dataCenter.getId(), backupModel.getEntity()
                                        .getId());
                        Frontend.RunQuery(VdcQueryType.GetVmsFromExportDomain,
                                tempVar, _asyncQuery1);
                    }
                }
            };
            AsyncDataProvider.GetDataCentersByStorageDomain(_asyncQuery,
                    getEntity().getId());
        }
    }

    @Override
    protected void AsyncSearch() {
        super.AsyncSearch();
        SyncSearch();
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);

        if (command.getName().equals("OnRemove")) { //$NON-NLS-1$
            OnRemove();
        } else if (command.getName().equals("OnRestore")) { //$NON-NLS-1$
            OnRestore();
        } else if (command.getName().equals("onClone")) { //$NON-NLS-1$
            onClone();
        } else if (command.getName().equals("closeClone")) { //$NON-NLS-1$
            closeClone();
        }
    }

    @Override
    protected String getListName() {
        return "VmBackupModel"; //$NON-NLS-1$
    }

}
