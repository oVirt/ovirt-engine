package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParamenters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
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
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class VmBackupModel extends ManageBackupModel {

    @Override
    public VM getSelectedItem() {
        return (VM) super.getSelectedItem();
    }

    public void setSelectedItem(VM value) {
        super.setSelectedItem(value);
    }

    private VmAppListModel privateAppListModel;

    public VmAppListModel getAppListModel() {
        return privateAppListModel;
    }

    private void setAppListModel(VmAppListModel value) {
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
        getAppListModel().setEntity(getSelectedItem());
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
            items.add(vm.getvm_name());
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

    public void OnRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

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

                    backupModel.StartProgress(null);

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

        ImportVmModel model = new ImportVmModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().importVirtualMachinesTitle());
        model.setHashName("import_virtual_machine"); //$NON-NLS-1$

        model.getDestinationStorage().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                OnDestinationStorageItemsChanged();
            }
        });

        AsyncQuery _AsyncQuery = new AsyncQuery();
        _AsyncQuery.Model = this;
        _AsyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object returnModel, Object returnValue) {
                VmBackupModel vmBackupModel = (VmBackupModel) returnModel;
                ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                ImportVmModel iVmModel = (ImportVmModel) vmBackupModel.getWindow();
                iVmModel.getCluster().setItems(clusters);
                iVmModel.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));
                iVmModel.setSourceStorage(vmBackupModel.getEntity().getStorageStaticData());

                AsyncQuery _asyncQuery1 = new AsyncQuery();
                _asyncQuery1.Model = vmBackupModel;
                _asyncQuery1.asyncCallback = new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object returnModel1,
                            Object returnValue1) {
                        ArrayList<storage_pool> pools = (ArrayList<storage_pool>) returnValue1;
                        storage_pool pool = null;
                        if (pools != null && pools.size() > 0) {
                            pool = pools.get(0);
                        }
                        VmBackupModel vmBackupModel1 = (VmBackupModel) returnModel1;
                        ImportVmModel iVmModel1 = (ImportVmModel) vmBackupModel1.getWindow();
                        iVmModel1.setStoragePool(pool);

                        iVmModel1.setItems(vmBackupModel1.getSelectedItems());
                        iVmModel1.setSelectedVMsCount(((List) vmBackupModel1.getItems()).size());

                        UICommand restoreCommand;
                        restoreCommand = new UICommand("OnRestore", vmBackupModel1); //$NON-NLS-1$
                        restoreCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
                        restoreCommand.setIsDefault(true);
                        iVmModel1.getCommands().add(restoreCommand);
                        UICommand tempVar3 = new UICommand("Cancel", vmBackupModel1); //$NON-NLS-1$
                        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                        tempVar3.setIsCancel(true);
                        iVmModel1.getCommands().add(tempVar3);
                    }

                };
                AsyncDataProvider.GetDataCentersByStorageDomain(_asyncQuery1,
                        vmBackupModel.getEntity().getId());
            }
        };

        AsyncDataProvider.GetClusterListByStorageDomain(_AsyncQuery,
                getEntity().getId());
    }

    private void OnDestinationStorageItemsChanged() {
        ImportVmModel iVmModel = (ImportVmModel) getWindow();

        if (((List) iVmModel.getItems()).size() == 0) {
            return;
        }

        if (((List) iVmModel.getDestinationStorage().getItems()).size() == 0) {
            iVmModel.getDestinationStorage().setIsChangable(false);
            iVmModel.getIsSingleDestStorage().setIsChangable(false);
            iVmModel.getIsSingleDestStorage().setEntity(false);
        }
        else {
            iVmModel.getIsSingleDestStorage().setIsChangable(true);
        }
    }

    public void OnRestore() {
        ImportVmModel model = (ImportVmModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.Validate()) {
            return;
        }

        ArrayList<VdcActionParametersBase> prms = new ArrayList<VdcActionParametersBase>();

        for (Object item : model.getItems()) {
            VM vm = (VM) item;

            storage_domains destinationStorage = ((storage_domains) model.getDestinationStorage().getSelectedItem());
            boolean isSingleDestStorage = (Boolean) model.getIsSingleDestStorage().getEntity();
            Guid destinationStorageId = destinationStorage != null && isSingleDestStorage ?
                    destinationStorage.getId() : Guid.Empty;

            ImportVmParameters prm = new ImportVmParameters(vm, model.getSourceStorage().getId(),
                    destinationStorageId, model.getStoragePool().getId(),
                    ((VDSGroup) model.getCluster().getSelectedItem()).getId());
            prm.setForceOverride(true);

            prm.setCopyCollapse((Boolean) model.getCollapseSnapshots().getEntity());
            HashMap<Guid, DiskImageBase> diskDictionary = new HashMap<Guid, DiskImageBase>();

            for (Map.Entry<Guid, Disk> entry : vm.getDiskMap().entrySet()) {
                Guid key = entry.getKey();
                DiskImage disk = (DiskImage) entry.getValue();

                HashMap<Guid, Guid> map = model.getDiskStorageMap().get(vm.getId());
                storage_domains domain = (Boolean) model.getIsSingleDestStorage().getEntity() ?
                        (storage_domains) model.getDestinationStorage().getSelectedItem()
                        : model.getStorageById(map.get(disk.getId()));
                disk.setvolume_format(DataProvider.GetDiskVolumeFormat(disk.getvolume_type(), domain.getstorage_type()));

                diskDictionary.put(key, disk);
            }

            prm.setDiskInfoList(diskDictionary);

            if (!(Boolean) model.getIsSingleDestStorage().getEntity()) {
                HashMap<Guid, Guid> map = model.getDiskStorageMap().get(vm.getId());
                prm.setImageToDestinationDomainMap(map);
            }

            if ((Boolean) model.getCloneAllVMs().getEntity()
                    || ((Boolean) model.getCloneOnlyDuplicateVMs().getEntity()
                    && model.isObjectInSetup(vm))) {
                prm.setImportAsNewEntity(true);
                prm.setCopyCollapse(true);
                prm.getVm().setvm_name(prm.getVm().getvm_name() + model.getCloneVMsSuffix().getEntity());
            }

            prms.add(prm);

        }

        model.StartProgress(null);

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
                                    importedVms += vm.getvm_name() + ", "; //$NON-NLS-1$
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
                                confirmModel.setMessage(StringFormat
                                        .format(ConstantsManager.getInstance()
                                                .getMessages()
                                                .importProcessHasBegunForVms(importedVms)));
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
                        tempVar.setGetAll(true);
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

        if (StringHelper.stringsEqual(command.getName(), "OnRemove")) { //$NON-NLS-1$
            OnRemove();
        } else if (StringHelper.stringsEqual(command.getName(), "OnRestore")) { //$NON-NLS-1$
            OnRestore();
        }
    }

    @Override
    protected String getListName() {
        return "VmBackupModel"; //$NON-NLS-1$
    }

}
