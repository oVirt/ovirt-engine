package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParamenters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
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
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
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
        setTitle("VM Import");

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
        model.setTitle("Remove Backed up VM(s)");
        model.setHashName("remove_backed_up_vm");
        model.setMessage("VM(s)");

        java.util.ArrayList<String> items = new java.util.ArrayList<String>();
        for (Object item : getSelectedItems()) {
            VM vm = (VM) item;
            items.add(vm.getvm_name());
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
                    java.util.ArrayList<VdcActionParametersBase> list =
                            new java.util.ArrayList<VdcActionParametersBase>();
                    for (Object item : backupModel.getSelectedItems()) {
                        VM vm = (VM) item;
                        list.add(new RemoveVmFromImportExportParamenters(vm,
                                backupModel.getEntity().getid(), pool.getId()));
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
                getEntity().getid());
    }

    @Override
    protected void Restore() {
        super.Restore();

        if (getWindow() != null) {
            return;
        }

        ImportVmModel model = new ImportVmModel();
        setWindow(model);
        model.setTitle("Import Virtual Machine(s)");
        model.setHashName("import_virtual_machine");
        AsyncQuery _AsyncQuery = new AsyncQuery();
        _AsyncQuery.Model = this;
        _AsyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object returnModel, Object returnValue) {
                VmBackupModel vmBackupModel = (VmBackupModel) returnModel;
                java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) returnValue;
                ImportVmModel iVmModel = (ImportVmModel) vmBackupModel
                        .getWindow();
                iVmModel.getCluster().setItems(clusters);
                iVmModel.getCluster().setSelectedItem(
                        Linq.FirstOrDefault(clusters));

                iVmModel.setSourceStorage(vmBackupModel.getEntity()
                        .getStorageStaticData());

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
                        ImportVmModel iVmModel1 = (ImportVmModel) vmBackupModel1
                                .getWindow();
                        iVmModel1.setStoragePool(pool);

                        iVmModel1.setItems(vmBackupModel1.getSelectedItems());
                        iVmModel1
                                .setSelectedVMsCount(((java.util.List) vmBackupModel1
                                        .getItems()).size());
                        iVmModel1.OnCollapseSnapshotsChanged(new AsyncQuery(
                                vmBackupModel1, new INewAsyncCallback() {

                                    @Override
                                    public void OnSuccess(Object returnModel2,
                                            Object returnValue2) {
                                        VmBackupModel vmBackupModel2 = (VmBackupModel) returnModel2;
                                        ImportVmModel iVmModel2 = (ImportVmModel) vmBackupModel2
                                                .getWindow();
                                        if (((java.util.List) iVmModel2
                                                .getDestinationStorage()
                                                .getItems()).size() == 0) {
                                            iVmModel2.getDestinationStorage()
                                                    .setIsChangable(false);
                                            iVmModel2
                                                    .getDestinationStorage()
                                                    .getChangeProhibitionReasons()
                                                    .add("Cannot import Virtual Machine.");

                                            iVmModel2
                                                    .setMessage("There is no target Data Storage Domain that contains all the Templates which the selected VMs are based on."
                                                            + "\r\n"
                                                            + "Suggested solutions:"
                                                            + "\r\n"
                                                            + "1. Preserving Template-Based structure:"
                                                            + "\r\n"
                                                            + "   a. Make sure the relevant Templates (on which the VMs are based) exist on the relevant Data-Center."
                                                            + "\r\n"
                                                            + "   b. Import the VMs one by one"
                                                            + "\r\n"
                                                            + "2. Using non-Template-Based structure (less optimal storage-wise):"
                                                            + "\r\n"
                                                            + "   a. Export the VMs again using the Collapse Snapshots option"
                                                            + "\r\n"
                                                            + "   b. Import the VMs.");

                                            UICommand tempVar = new UICommand(
                                                    "Cancel", vmBackupModel2);
                                            tempVar.setTitle("Close");
                                            tempVar.setIsDefault(true);
                                            tempVar.setIsCancel(true);
                                            iVmModel2.getCommands()
                                                    .add(tempVar);
                                        } else {
                                            UICommand tempVar2 = new UICommand(
                                                    "OnRestore", vmBackupModel2);
                                            tempVar2.setTitle("OK");
                                            tempVar2.setIsDefault(true);
                                            iVmModel2.getCommands().add(
                                                    tempVar2);
                                            UICommand tempVar3 = new UICommand(
                                                    "Cancel", vmBackupModel2);
                                            tempVar3.setTitle("Cancel");
                                            tempVar3.setIsCancel(true);
                                            iVmModel2.getCommands().add(
                                                    tempVar3);
                                        }
                                    }
                                }));
                    }

                };
                AsyncDataProvider.GetDataCentersByStorageDomain(_asyncQuery1,
                        vmBackupModel.getEntity().getid());
            }
        };

        AsyncDataProvider.GetClusterListByStorageDomain(_AsyncQuery,
                getEntity().getid());
    }

    public void OnRestore() {
        ImportVmModel model = (ImportVmModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.Validate()) {
            return;
        }

        java.util.ArrayList<VdcActionParametersBase> prms = new java.util.ArrayList<VdcActionParametersBase>();

        for (Object item : model.getItems()) {
            VM vm = (VM) item;
            ImportVmParameters tempVar = new ImportVmParameters(vm, model
                    .getSourceStorage().getId(), ((storage_domains) model
                    .getDestinationStorage().getSelectedItem()).getid(), model
                    .getStoragePool().getId(), ((VDSGroup) model.getCluster()
                    .getSelectedItem()).getID());
            tempVar.setForceOverride(true);
            ImportVmParameters prm = tempVar;

            prm.setCopyCollapse((Boolean) model.getCollapseSnapshots()
                    .getEntity());
            java.util.HashMap<String, DiskImageBase> diskDictionary = new java.util.HashMap<String, DiskImageBase>();

            // vm.DiskMap.Each(a => prm.DiskInfoList.Add(a.getKey(), a.Value));
            for (java.util.Map.Entry<String, DiskImage> a : vm.getDiskMap()
                    .entrySet()) {
                diskDictionary.put(a.getKey(), a.getValue());
            }

            prm.setDiskInfoList(diskDictionary);

            // prm.DiskInfoList.Each(a=>a.Value.volume_format =
            // DataProvider.GetDiskVolumeFormat(a.Value.volume_type,
            // model.DestinationStorage.ValueAs<storage_domains>().storage_type));
            for (java.util.Map.Entry<String, DiskImageBase> a : prm
                    .getDiskInfoList().entrySet()) {
                DiskImageBase disk = a.getValue();
                storage_domains domain = (storage_domains) model
                        .getDestinationStorage().getSelectedItem();
                disk.setvolume_format(DataProvider.GetDiskVolumeFormat(
                        disk.getvolume_type(), domain.getstorage_type()));
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
                        java.util.ArrayList<VdcReturnValueBase> retVals =
                                (java.util.ArrayList<VdcReturnValueBase>) result
                                        .getReturnValue();
                        if (retVals != null
                                && vmBackupModel.getSelectedItems().size() == retVals
                                        .size()) {
                            String importedVms = "";
                            int counter = 0;
                            boolean toShowConfirmWindow = false;
                            for (Object item : vmBackupModel.getSelectedItems()) {
                                VM vm = (VM) item;
                                if (retVals.get(counter) != null
                                        && retVals.get(counter).getCanDoAction()) {
                                    importedVms += vm.getvm_name() + ", ";
                                    toShowConfirmWindow = true;
                                }
                                counter++;
                            }
                            // show the confirm window only if the import has been successfully started for at least one VM
                            if (toShowConfirmWindow) {
                                ConfirmationModel confirmModel = new ConfirmationModel();
                                vmBackupModel.setConfirmWindow(confirmModel);
                                confirmModel.setTitle("Import Virtual Machine(s)");
                                confirmModel.setHashName("import_virtual_machine");
                                importedVms = StringHelper.trimEnd(importedVms.trim(), ',');
                                confirmModel.setMessage(StringFormat
                                        .format("Import process has begun for VM(s): %1$s.\nYou can check import status in the 'Events' tab of the specific destination storage domain, or in the main 'Events' tab",
                                                importedVms));
                                UICommand tempVar2 = new UICommand("CancelConfirm",
                                        vmBackupModel);
                                tempVar2.setTitle("Close");
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

        if (e.PropertyName.equals("storage_domain_shared_status")) {
            getSearchCommand().Execute();
        }
    }

    @Override
    protected void SyncSearch() {
        super.SyncSearch();

        if (getEntity() == null
                || getEntity().getstorage_domain_type() != StorageDomainType.ImportExport
                || getEntity().getstorage_domain_shared_status() != StorageDomainSharedStatus.Active) {
            setItems(null);
        } else {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object ReturnValue) {
                    VmBackupModel backupModel = (VmBackupModel) model;
                    java.util.ArrayList<storage_pool> list = (java.util.ArrayList<storage_pool>) ReturnValue;
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
                                        .setItems((java.util.ArrayList<VM>) ((VdcQueryReturnValue) ReturnValue1)
                                                .getReturnValue());
                            }
                        };
                        GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(
                                dataCenter.getId(), backupModel.getEntity()
                                        .getid());
                        tempVar.setGetAll(true);
                        Frontend.RunQuery(VdcQueryType.GetVmsFromExportDomain,
                                tempVar, _asyncQuery1);
                    }
                }
            };
            AsyncDataProvider.GetDataCentersByStorageDomain(_asyncQuery,
                    getEntity().getid());
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

        if (StringHelper.stringsEqual(command.getName(), "OnRemove")) {
            OnRemove();
        } else if (StringHelper.stringsEqual(command.getName(), "OnRestore")) {
            OnRestore();
        }
    }

    @Override
    protected String getListName() {
        return "VmBackupModel";
    }

}
