package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDettachVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class VmGuideModel extends GuideModel
{
    public final String VmConfigureNetworkInterfacesAction = ConstantsManager.getInstance()
            .getConstants()
            .vmConfigureNetworkInterfacesAction();
    public final String VmAddAnotherNetworkInterfaceAction = ConstantsManager.getInstance()
            .getConstants()
            .vmAddAnotherNetworkInterfaceAction();
    public final String VmConfigureVirtualDisksAction = ConstantsManager.getInstance()
            .getConstants()
            .vmConfigureVirtualDisksAction();
    public final String VmAddAnotherVirtualDiskAction = ConstantsManager.getInstance()
            .getConstants()
            .vmAddAnotherVirtualDiskAction();

    private ArrayList<VmNetworkInterface> nics;
    private ArrayList<Disk> disks;
    private StorageDomain storage;
    private VDSGroup cluster;

    @Override
    public VM getEntity()
    {
        return (VM) super.getEntity();
    }

    @Override
    public void setEntity(Object value) {
        super.setEntity(value);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        UpdateOptions();
    }

    private void UpdateOptionsData() {
        nics = null;
        disks = null;
        AsyncDataProvider.GetVmNicList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        ArrayList<VmNetworkInterface> nics =
                                (ArrayList<VmNetworkInterface>) returnValue;
                        vmGuideModel.nics = nics;
                        vmGuideModel.UpdateOptionsPostData();
                    }
                }), getEntity().getId());

        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                        vmGuideModel.disks = disks;
                        vmGuideModel.UpdateOptionsPostData();
                    }
                }), getEntity().getId());
    }

    private void UpdateOptionsPostData() {
        if (nics == null || disks == null) {
            return;
        }

        // Add NIC action.
        UICommand addNicAction = new UICommand("AddNetwork", this); //$NON-NLS-1$

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
        UICommand addDiskAction = new UICommand("AddDisk", this); //$NON-NLS-1$

        if (disks.isEmpty())
        {
            addDiskAction.setTitle(VmConfigureVirtualDisksAction);
            getCompulsoryActions().add(addDiskAction);
        }
        else
        {
            int ideDiskCount = 0;
            for (Disk a : disks)
            {
                if (a.getDiskInterface() == DiskInterface.IDE)
                {
                    ideDiskCount++;
                }

            }
            if (!(getEntity().getVmOs() == VmOsType.WindowsXP && ideDiskCount > 2))
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

    public void ResetData() {
        nics = null;
        disks = null;
        storage = null;
        cluster = null;
    }

    private void AddNetworkUpdateData() {
        nics = null;
        AsyncDataProvider.GetVmNicList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        ArrayList<VmNetworkInterface> nics =
                                (ArrayList<VmNetworkInterface>) returnValue;
                        vmGuideModel.nics = nics;
                        vmGuideModel.AddNetworkPostData();
                    }
                }), getEntity().getId());
    }

    private void AddNetworkPostData() {
        if (nics == null) {
            return;
        }

        VmInterfaceModel model =
                NewVmInterfaceModel.createInstance(getEntity().getStaticData(),
                        getEntity().getVdsGroupCompatibilityVersion(),
                        nics,
                        this);
        setWindow(model);

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

    private void AddDiskUpdateData() {
        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                        vmGuideModel.disks = disks;

                        vmGuideModel.AddDiskPostData();
                    }
                }), getEntity().getId());
    }

    private void AddDiskPostData() {
        if (disks == null) {
            return;
        }

        final DiskModel model = new DiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newVirtualDiskTitle());
        model.setHashName("new_virtual_disk"); //$NON-NLS-1$
        model.setIsNew(true);
        model.setDatacenterId(getEntity().getStoragePoolId());
        model.getIsInVm().setEntity(true);
        model.getIsInternal().setEntity(true);
        model.setVmId(getEntity().getId());

        boolean hasBootableDisk = false;
        for (Disk a : disks)
        {
            if (a.isBoot())
            {
                hasBootableDisk = true;
                break;
            }
        }
        model.getIsBootable().setEntity(!hasBootableDisk);
        if (hasBootableDisk)
        {
            model.getIsBootable().setChangeProhibitionReason("There can be only one bootable disk defined."); //$NON-NLS-1$
            model.getIsBootable().setIsChangable(false);
        }

        AsyncDataProvider.GetNextAvailableDiskAliasNameByVMId(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        String suggestedDiskName = (String) returnValue;
                        VmGuideModel vmGuideModel = (VmGuideModel) model;
                        vmGuideModel.StopProgress();

                        DiskModel diskModel = (DiskModel) vmGuideModel.getWindow();
                        diskModel.getAlias().setEntity(suggestedDiskName);

                        UICommand tempVar2 = new UICommand("OnAddDisk", vmGuideModel); //$NON-NLS-1$
                        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
                        tempVar2.setIsDefault(true);
                        diskModel.getCommands().add(tempVar2);

                        UICommand tempVar3 = new UICommand("Cancel", vmGuideModel); //$NON-NLS-1$
                        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                        tempVar3.setIsCancel(true);
                        diskModel.getCommands().add(tempVar3);
                    }
                }), getEntity().getId());
    }

    public void AddDisk()
    {
        if (getEntity() != null)
        {
            StartProgress(null);
            disks = null;

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

            if ((Boolean) model.getAttachDisk().getEntity())
            {
                OnAttachDisks();
                return;
            }

            // Save changes.
            StorageDomain storageDomain = (StorageDomain) model.getStorageDomain().getSelectedItem();

            Disk disk;
            if ((Boolean) model.getIsInternal().getEntity()) {
                DiskImage diskImage = new DiskImage();
                diskImage.setSizeInGigabytes(Integer.parseInt(model.getSize().getEntity().toString()));
                diskImage.setVolumeType((VolumeType) model.getVolumeType().getSelectedItem());
                diskImage.setvolumeFormat(model.getVolumeFormat());
                if (model.getQuota().getSelectedItem() != null && model.getQuota().getIsAvailable()) {
                    diskImage.setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
                }

                disk = diskImage;
            }
            else {
                LUNs luns = (LUNs) model.getSanStorageModel().getAddedLuns().get(0).getEntity();
                luns.setLunType((StorageType) model.getStorageType().getSelectedItem());

                LunDisk lunDisk = new LunDisk();
                lunDisk.setLun(luns);

                disk = lunDisk;
            }

            disk.setDiskAlias((String) model.getAlias().getEntity());
            disk.setDiskDescription((String) model.getDescription().getEntity());
            disk.setDiskInterface((DiskInterface) model.getInterface().getSelectedItem());
            disk.setWipeAfterDelete((Boolean) model.getWipeAfterDelete().getEntity());
            disk.setBoot((Boolean) model.getIsBootable().getEntity());
            disk.setShareable((Boolean) model.getIsShareable().getEntity());
            disk.setPlugged((Boolean) model.getIsPlugged().getEntity());
            disk.setPropagateErrors(PropagateErrors.Off);

            model.StartProgress(null);

            AddDiskParameters tempVar2 = new AddDiskParameters(getEntity().getId(), disk);
            tempVar2.setStorageDomainId(storageDomain.getId());
            Frontend.RunAction(VdcActionType.AddDisk, tempVar2,
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

    private void OnAttachDisks()
    {
        VM vm = getEntity();
        DiskModel model = (DiskModel) getWindow();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();

        ArrayList<EntityModel> disksToAttach = (Boolean) model.getIsInternal().getEntity() ?
                (ArrayList<EntityModel>) model.getInternalAttachableDisks().getSelectedItems() :
                (ArrayList<EntityModel>) model.getExternalAttachableDisks().getSelectedItems();

        for (EntityModel item : disksToAttach)
        {
            DiskModel disk = (DiskModel) item.getEntity();
            AttachDettachVmDiskParameters parameters = new AttachDettachVmDiskParameters(
                    vm.getId(), disk.getDisk().getId(), (Boolean) model.getIsPlugged().getEntity());
            paramerterList.add(parameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.AttachDiskToVm, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        VmGuideModel localModel = (VmGuideModel) result.getState();
                        localModel.getWindow().StopProgress();
                        Cancel();
                    }
                },
                this);
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

        if (StringHelper.stringsEqual(command.getName(), "AddNetwork")) //$NON-NLS-1$
        {
            AddNetwork();
        }
        if (StringHelper.stringsEqual(command.getName(), "AddDisk")) //$NON-NLS-1$
        {
            AddDisk();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddDisk")) //$NON-NLS-1$
        {
            OnAddDisk();
        }
        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }
}
