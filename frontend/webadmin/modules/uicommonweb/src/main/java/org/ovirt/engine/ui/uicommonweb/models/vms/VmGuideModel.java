package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.AddDiskToVmParameters;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
public class VmGuideModel extends GuideModel
{
    public final String VmConfigureNetworkInterfacesAction = "Configure Network Interfaces";
    public final String VmAddAnotherNetworkInterfaceAction = "Add another Network Interface";
    public final String VmConfigureVirtualDisksAction = "Configure Virtual Disks";
    public final String VmAddAnotherVirtualDiskAction = "Add another Virtual Disk";

    private java.util.ArrayList<VmNetworkInterface> nics;
    private java.util.ArrayList<DiskImage> disks;
    private java.util.ArrayList<network> networks;
    private java.util.ArrayList<storage_domains> attachedStorageDomains;
    private boolean storage_available;
    private storage_domains storage;
    private VDSGroup cluster;

    @Override
    public VM getEntity()
    {
        return (VM) super.getEntity();
    }

    public void setEntity(VM value)
    {
        super.setEntity(value);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        UpdateOptions();
    }

    private void UpdateOptionsData() {
        AsyncDataProvider.GetVmNicList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        java.util.ArrayList<VmNetworkInterface> nics =
                                (java.util.ArrayList<VmNetworkInterface>) returnValue;
                        vmGuideModel.nics = nics;
                        vmGuideModel.UpdateOptionsPostData();
                    }
                }), getEntity().getvm_guid());

        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        java.util.ArrayList<DiskImage> disks = (java.util.ArrayList<DiskImage>) returnValue;
                        vmGuideModel.disks = disks;
                        vmGuideModel.UpdateOptionsPostData();
                    }
                }), getEntity().getvm_guid());
    }

    private void UpdateOptionsPostData() {
        if (nics == null || disks == null) {
            return;
        }

        // Add NIC action.
        UICommand addNicAction = new UICommand("AddNetwork", this);

        if (nics.isEmpty())
        {
            addNicAction.setTitle(VmConfigureNetworkInterfacesAction);
            getCompulsoryActions().add(addNicAction);
        }
        else
        {
            addNicAction.setTitle(VmAddAnotherNetworkInterfaceAction);
            getOptionalActions().add(addNicAction);
        }

        // Add disk action.
        UICommand addDiskAction = new UICommand("AddDisk", this);

        if (disks.isEmpty())
        {
            addDiskAction.setTitle(VmConfigureVirtualDisksAction);
            getCompulsoryActions().add(addDiskAction);
        }
        else
        {
            int ideDiskCount = 0;
            for (DiskImage a : disks)
            {
                if (a.getdisk_interface() == DiskInterface.IDE)
                {
                    ideDiskCount++;
                }

            }
            if (!(getEntity().getvm_os() == VmOsType.WindowsXP && ideDiskCount > 2))
            {
                addDiskAction.setTitle(VmAddAnotherVirtualDiskAction);
                getOptionalActions().add(addDiskAction);
            }
        }

        StopProgress();
    }

    private void UpdateOptions()
    {
        getCompulsoryActions().clear();
        getOptionalActions().clear();

        if (getEntity() != null)
        {
            StartProgress(null);

            UpdateOptionsData();
        }
    }

    private void ResetData() {
        nics = null;
        disks = null;
        networks = null;
        attachedStorageDomains = null;
        storage_available = false;
        storage = null;
        cluster = null;
    }

    private void AddNetworkUpdateData() {
        AsyncDataProvider.GetVmNicList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        java.util.ArrayList<VmNetworkInterface> nics =
                                (java.util.ArrayList<VmNetworkInterface>) returnValue;
                        vmGuideModel.nics = nics;
                        vmGuideModel.AddNetworkPostData();
                    }
                }), getEntity().getvm_guid());

        AsyncDataProvider.GetClusterNetworkList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        java.util.ArrayList<network> networks = (java.util.ArrayList<network>) returnValue;
                        vmGuideModel.networks = networks;
                        vmGuideModel.AddNetworkPostData();
                    }
                }), getEntity().getvds_group_id());
    }

    private void AddNetworkPostData() {
        if (nics == null || networks == null) {
            return;
        }

        int nicCount = nics.size();
        String newNicName = DataProvider.GetNewNicName(nics);

        java.util.ArrayList<network> operationalNetworks = new java.util.ArrayList<network>();
        for (network a : networks)
        {
            if (a.getStatus() == NetworkStatus.Operational)
            {
                operationalNetworks.add(a);
            }
        }

        VmInterfaceModel model = new VmInterfaceModel();
        setWindow(model);
        model.setTitle("New Network Interface");
        model.setHashName("new_network_interface_vms_guide");
        model.setIsNew(true);
        model.getNetwork().setItems(operationalNetworks);
        model.getNetwork().setSelectedItem(operationalNetworks.size() > 0 ? operationalNetworks.get(0) : null);
        model.getNicType().setItems(DataProvider.GetNicTypeList(getEntity().getvm_os(), false));
        model.getNicType().setSelectedItem(DataProvider.GetDefaultNicType(getEntity().getvm_os()));
        model.getName().setEntity(newNicName);
        model.getMAC().setIsChangable(false);

        UICommand tempVar = new UICommand("OnAddNetwork", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);

        StopProgress();
    }

    public void AddNetwork()
    {
        if (getEntity() != null)
        {
            StartProgress(null);

            AddNetworkUpdateData();
        }
    }

    private void OnAddNetwork()
    {
        if (getEntity() != null)
        {
            VmInterfaceModel model = (VmInterfaceModel) getWindow();

            if (model.getProgress() != null)
            {
                return;
            }

            if (!model.Validate())
            {
                return;
            }

            // Save changes.
            Integer _type;
            if (model.getNicType().getSelectedItem() == null)
            {
                _type = null;
            }
            else
            {
                _type = ((VmInterfaceType) model.getNicType().getSelectedItem()).getValue();
            }

            VmNetworkInterface vmNetworkInterface = new VmNetworkInterface();
            vmNetworkInterface.setName((String) model.getName().getEntity());
            vmNetworkInterface.setNetworkName(((network) model.getNetwork().getSelectedItem()).getname());
            vmNetworkInterface.setType(_type);
            vmNetworkInterface.setMacAddress(model.getMAC().getIsChangable() ? (model.getMAC().getEntity() == null ? null
                    : ((String) (model.getMAC().getEntity())).toLowerCase())
                    : "");

            AddVmInterfaceParameters parameters =
                    new AddVmInterfaceParameters(getEntity().getvm_guid(), vmNetworkInterface);

            model.StartProgress(null);

            Frontend.RunAction(VdcActionType.AddVmInterface, parameters,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            VmGuideModel vmGuideModel = (VmGuideModel) result.getState();
                            vmGuideModel.getWindow().StopProgress();
                            VdcReturnValueBase returnValueBase = result.getReturnValue();
                            if (returnValueBase != null && returnValueBase.getSucceeded())
                            {
                                vmGuideModel.Cancel();
                                vmGuideModel.PostAction();
                            }

                        }
                    }, this);
        }
        else
        {
            Cancel();
        }
    }

    private void AddDiskUpdateData() {
        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        java.util.ArrayList<DiskImage> disks = (java.util.ArrayList<DiskImage>) returnValue;
                        vmGuideModel.disks = disks;

                        if (!disks.isEmpty()) {
                            Guid storageDomainId = disks.get(0).getstorage_id().getValue();

                            AsyncDataProvider.GetStorageDomainById(new AsyncQuery(vmGuideModel,
                                    new INewAsyncCallback() {
                                        @Override
                                        public void OnSuccess(Object target, Object returnValue) {
                                            VmGuideModel vmGuideModel = (VmGuideModel) target;
                                            vmGuideModel.storage = (storage_domains) returnValue;
                                            vmGuideModel.AddDiskPostData();
                                        }
                                    }), storageDomainId);
                        }
                        else {
                            vmGuideModel.AddDiskPostData();
                        }
                    }
                }), getEntity().getvm_guid());

        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        java.util.ArrayList<storage_domains> storageDomains =
                                (java.util.ArrayList<storage_domains>) returnValue;
                        vmGuideModel.attachedStorageDomains = storageDomains;
                        vmGuideModel.AddDiskPostData();
                    }
                }), getEntity().getstorage_pool_id());

        AsyncDataProvider.GetClusterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        vmGuideModel.cluster = (VDSGroup) returnValue;
                        vmGuideModel.AddDiskPostData();
                    }
                }), getEntity().getvds_group_id());
    }

    private void AddDiskPostData() {
        if (attachedStorageDomains == null || disks == null || cluster == null || (!disks.isEmpty() && storage == null)) {
            return;
        }

        DiskModel model = new DiskModel();
        setWindow(model);
        model.setTitle("New Virtual Disk");
        model.setHashName("new_virtual_disk");
        model.setIsNew(true);

        java.util.ArrayList<storage_domains> storageDomains = new java.util.ArrayList<storage_domains>();
        for (storage_domains a : attachedStorageDomains)
        {
            if (a.getstorage_domain_type() != StorageDomainType.ISO
                    && a.getstorage_domain_type() != StorageDomainType.ImportExport
                    && a.getstatus() == StorageDomainStatus.Active)
            {
                storageDomains.add(a);
            }
        }
        model.getStorageDomain().setItems(storageDomains);

        boolean hasDisks = !disks.isEmpty();
        if (hasDisks)
        {
            // the StorageDomain value should be the one that all other Disks are on
            // (although this field is not-available, we use its value in the 'OnSave' method):
            if (storage != null && Linq.IsSDItemExistInList(storageDomains, storage.getid()))
            {
                storage_available = true;
            }
        }
        else // first disk -> just choose the first from the list of available storage-domains:
        {
            storage = Linq.<storage_domains> FirstOrDefault(storageDomains);
            storage_available = true;
        }

        model.getStorageDomain().setSelectedItem(storage);
        model.getStorageDomain().setIsAvailable(!hasDisks);

        if (model.getStorageDomain() != null && model.getStorageDomain().getSelectedItem() != null)
        {
            StorageType selectedStorageType =
                    ((storage_domains) model.getStorageDomain().getSelectedItem()).getstorage_type();
            UpdateWipeAfterDelete(selectedStorageType, model.getWipeAfterDelete(), true);
        }

        VmType vmType = getEntity().getvm_type();
        StorageType storageType = model.getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN
                : storage.getstorage_type();

        AsyncDataProvider.GetDiskPresetList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        java.util.ArrayList<DiskImageBase> presets = (java.util.ArrayList<DiskImageBase>) returnValue;
                        vmGuideModel.AddDiskPostGetDiskPresets(presets);
                    }
                }), vmType, storageType);
    }

    private void AddDiskPostGetDiskPresets(java.util.ArrayList<DiskImageBase> presets) {
        DiskModel model = (DiskModel) getWindow();
        model.getPreset().setItems(presets);
        // model.getPreset().setSelectedItem(null);

        boolean hasDisks = !disks.isEmpty();
        for (DiskImageBase a : presets)
        {
            if ((hasDisks && a.getdisk_type() == DiskType.Data)
                    || (!hasDisks && a.getdisk_type() == DiskType.System))
            {
                model.getPreset().setSelectedItem(a);
                break;
            }
        }

        model.getInterface().setItems(DataProvider.GetDiskInterfaceList(getEntity().getvm_os(),
                cluster.getcompatibility_version()));
        model.getInterface().setSelectedItem(DataProvider.GetDefaultDiskInterface(getEntity().getvm_os(), disks));

        boolean hasBootableDisk = false;
        for (DiskImage a : disks)
        {
            if (a.getboot())
            {
                hasBootableDisk = true;
                break;
            }
        }
        model.getIsBootable().setEntity(!hasBootableDisk);
        if (hasBootableDisk)
        {
            model.getIsBootable().setIsChangable(false);
            model.getIsBootable().getChangeProhibitionReasons().add("There can be only one bootable disk defined.");
        }

        if (storage == null || storage_available == false)
        {
            String cantCreateMessage =
                    "There is no active Storage Domain to create the Disk in. Please activate a Storage Domain.";
            if (hasDisks)
            {
                cantCreateMessage = "Error in retrieving the relevant Storage Domain.";
                if (storage != null && storage.getstorage_name() != null)
                {
                    cantCreateMessage =
                            StringFormat.format("'%1$s' Storage Domain is not active. Please activate it.",
                                    storage.getstorage_name());
                }
            }

            model.setMessage(cantCreateMessage);

            UICommand tempVar = new UICommand("Cancel", this);
            tempVar.setTitle("Close");
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnAddDisk", this);
            tempVar2.setTitle("OK");
            tempVar2.setIsDefault(true);
            model.getCommands().add(tempVar2);

            UICommand tempVar3 = new UICommand("Cancel", this);
            tempVar3.setTitle("Cancel");
            tempVar3.setIsCancel(true);
            model.getCommands().add(tempVar3);
        }

        StopProgress();
    }

    public void AddDisk()
    {
        if (getEntity() != null)
        {
            StartProgress(null);

            AddDiskUpdateData();
        }
    }

    public void OnAddDisk()
    {
        if (getEntity() != null)
        {
            DiskModel model = (DiskModel) getWindow();

            if (model.getProgress() != null)
            {
                return;
            }

            if (!model.Validate())
            {
                return;
            }

            // Save changes.
            storage_domains storageDomain = (storage_domains) model.getStorageDomain().getSelectedItem();

            DiskImage tempVar = new DiskImage();
            tempVar.setSizeInGigabytes(Integer.parseInt(model.getSize().getEntity().toString()));
            tempVar.setdisk_type(((DiskImageBase) model.getPreset().getSelectedItem()).getdisk_type());
            tempVar.setdisk_interface((DiskInterface) model.getInterface().getSelectedItem());
            tempVar.setvolume_type((VolumeType) model.getVolumeType().getSelectedItem());
            tempVar.setvolume_format(model.getVolumeFormat());
            tempVar.setwipe_after_delete((Boolean) model.getWipeAfterDelete().getEntity());
            tempVar.setboot((Boolean) model.getIsBootable().getEntity());
            tempVar.setpropagate_errors(PropagateErrors.Off);
            DiskImage disk = tempVar;

            model.StartProgress(null);

            AddDiskToVmParameters tempVar2 = new AddDiskToVmParameters(getEntity().getvm_guid(), disk);
            tempVar2.setStorageDomainId(storageDomain.getid());
            Frontend.RunAction(VdcActionType.AddDiskToVm, tempVar2,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            VmGuideModel vmGuideModel = (VmGuideModel) result.getState();
                            vmGuideModel.getWindow().StopProgress();
                            VdcReturnValueBase returnValueBase = result.getReturnValue();
                            if (returnValueBase != null && returnValueBase.getSucceeded())
                            {
                                vmGuideModel.Cancel();
                                vmGuideModel.PostAction();
                            }

                        }
                    }, this);
        }
        else
        {
            Cancel();
        }
    }

    private void UpdateWipeAfterDelete(StorageType storageType, EntityModel wipeAfterDeleteModel, boolean isNew)
    {
        if (storageType == StorageType.NFS || storageType == StorageType.LOCALFS)
        {
            wipeAfterDeleteModel.setIsChangable(false);
        }
        else
        {
            wipeAfterDeleteModel.setIsChangable(true);
            if (isNew)
            {
                AsyncQuery _asyncQuery = new AsyncQuery();
                _asyncQuery.setModel(getWindow());
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object result)
                    {
                        DiskModel diskModel = (DiskModel) model;
                        diskModel.getWipeAfterDelete().setEntity(result);
                    }
                };
                AsyncDataProvider.GetSANWipeAfterDelete(_asyncQuery);
            }
        }
    }

    public void PostAction()
    {
        ResetData();
        UpdateOptions();
    }

    public void Cancel()
    {
        ResetData();
        setWindow(null);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "AddNetwork"))
        {
            AddNetwork();
        }
        if (StringHelper.stringsEqual(command.getName(), "AddDisk"))
        {
            AddDisk();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddNetwork"))
        {
            OnAddNetwork();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddDisk"))
        {
            OnAddDisk();
        }
        if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
    }
}
