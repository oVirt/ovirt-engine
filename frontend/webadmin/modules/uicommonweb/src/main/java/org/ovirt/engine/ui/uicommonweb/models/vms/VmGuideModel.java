package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
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
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForStorageParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
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
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

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
    private ArrayList<DiskImage> disks;
    private ArrayList<network> networks;
    private ArrayList<storage_domains> attachedStorageDomains;
    private storage_domains storage;
    private VDSGroup cluster;
    private QuotaEnforcementTypeEnum quotaEnforcementType = null;

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
                        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) returnValue;
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
            for (DiskImage a : disks)
            {
                if (a.getDiskInterface() == DiskInterface.IDE)
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
        storage = null;
        cluster = null;
    }

    private void AddNetworkUpdateData() {
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

        AsyncDataProvider.GetClusterNetworkList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        ArrayList<network> networks = (ArrayList<network>) returnValue;
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

        ArrayList<network> operationalNetworks = new ArrayList<network>();
        for (network a : networks)
        {
            if (a.getStatus() == NetworkStatus.Operational)
            {
                operationalNetworks.add(a);
            }
        }

        VmInterfaceModel model = new VmInterfaceModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newNetworkInterfaceTitle());
        model.setHashName("new_network_interface_vms_guide"); //$NON-NLS-1$
        model.setIsNew(true);
        model.getNetwork().setItems(operationalNetworks);
        model.getNetwork().setSelectedItem(operationalNetworks.size() > 0 ? operationalNetworks.get(0) : null);
        model.getNicType().setItems(DataProvider.GetNicTypeList(getEntity().getvm_os(), false));
        model.getNicType().setSelectedItem(DataProvider.GetDefaultNicType(getEntity().getvm_os()));
        model.getName().setEntity(newNicName);
        model.getMAC().setIsChangable(false);

        UICommand tempVar = new UICommand("OnAddNetwork", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
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
                    : ""); //$NON-NLS-1$

            AddVmInterfaceParameters parameters =
                    new AddVmInterfaceParameters(getEntity().getId(), vmNetworkInterface);

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
        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        ArrayList<storage_domains> storageDomains =
                                (ArrayList<storage_domains>) returnValue;
                        Linq.Sort(storageDomains, new Linq.StorageDomainByNameComparer());
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

        AsyncDataProvider.GetDataCenterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        storage_pool dataCenter = (storage_pool) returnValue;
                        vmGuideModel.quotaEnforcementType =
                                dataCenter != null ? dataCenter.getQuotaEnforcementType()
                                        : QuotaEnforcementTypeEnum.DISABLED;
                        vmGuideModel.AddDiskPostData();
                    }
                }), getEntity().getstorage_pool_id());

        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmGuideModel vmGuideModel = (VmGuideModel) target;
                        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) returnValue;
                        vmGuideModel.disks = disks;

                        if (disks != null && !disks.isEmpty()) {
                            AsyncDataProvider.GetStorageDomainById(new AsyncQuery(vmGuideModel,
                                    new INewAsyncCallback() {
                                        @Override
                                        public void OnSuccess(Object target, Object returnValue) {
                                            VmGuideModel vmGuideModel1 = (VmGuideModel) target;
                                            vmGuideModel1.storage = (storage_domains) returnValue;
                                            vmGuideModel1.AddDiskPostData();
                                        }
                                    }), disks.get(0).getstorage_ids().get(0));
                        }
                        else
                        {
                            vmGuideModel.AddDiskPostData();
                        }
                    }
                }), getEntity().getId());
    }

    private void AddDiskPostData() {
        if (attachedStorageDomains == null || disks == null || cluster == null || (!disks.isEmpty() && storage == null)
                || quotaEnforcementType == null) {
            return;
        }

        final DiskModel model = new DiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newVirtualDiskTitle());
        model.setHashName("new_virtual_disk"); //$NON-NLS-1$
        model.setIsNew(true);

        ArrayList<storage_domains> storageDomains = new ArrayList<storage_domains>();
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
        storage = Linq.<storage_domains> FirstOrDefault(storageDomains);
        model.getStorageDomain().setSelectedItem(storage);
        updateQuota(model);

        if (!quotaEnforcementType.equals(QuotaEnforcementTypeEnum.DISABLED)) {
            model.getQuota().setIsAvailable(true);
            model.getStorageDomain().getSelectedItemChangedEvent().addListener(new IEventListener() {
                @Override
                public void eventRaised(Event ev, Object sender, EventArgs args) {
                    updateQuota(model);
                }
            });
        }

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
                        ArrayList<DiskImageBase> presets = (ArrayList<DiskImageBase>) returnValue;
                        vmGuideModel.AddDiskPostGetDiskPresets(presets);
                    }
                }), vmType, storageType);
    }

    private void updateQuota(final DiskModel model) {
        storage_domains storageDomain = (storage_domains) model.getStorageDomain().getSelectedItem();
        if (storageDomain == null) {
            return;
        }
        Frontend.RunQuery(VdcQueryType.GetAllRelevantQuotasForStorage,
                new GetAllRelevantQuotasForStorageParameters(storageDomain.getId()),
                new AsyncQuery(this,
                        new INewAsyncCallback() {

                            @Override
                            public void OnSuccess(Object innerModel, Object innerReturnValue) {
                                ArrayList<Quota> list =
                                        (ArrayList<Quota>) ((VdcQueryReturnValue) innerReturnValue).getReturnValue();
                                if (list != null) {
                                    model.getQuota().setItems(list);
                                    if (getEntity().getQuotaId() != null) {
                                        for (Quota quota : list) {
                                            if (quota.getId().equals(getEntity().getQuotaId())) {
                                                model.getQuota().setSelectedItem(quota);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }));
    }

    private void AddDiskPostGetDiskPresets(ArrayList<DiskImageBase> presets) {
        DiskModel model = (DiskModel) getWindow();
        boolean hasDisks = !disks.isEmpty();

        model.getPreset().setItems(presets);
        for (DiskImageBase a : presets)
        {
            if ((hasDisks && !a.isBoot()) || (!hasDisks && a.isBoot()))
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
            if (a.isBoot())
            {
                hasBootableDisk = true;
                break;
            }
        }
        model.getIsBootable().setEntity(!hasBootableDisk);
        if (hasBootableDisk)
        {
            model.getIsBootable().setIsChangable(false);
            model.getIsBootable().getChangeProhibitionReasons().add("There can be only one bootable disk defined."); //$NON-NLS-1$
        }

        AsyncDataProvider.GetNextAvailableDiskAliasNameByVMId(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        String suggestedDiskName = (String) returnValue;
                        VmGuideModel vmGuideModel = (VmGuideModel) model;
                        DiskModel diskModel = (DiskModel) vmGuideModel.getWindow();
                        diskModel.getAlias().setEntity(suggestedDiskName);
                        vmGuideModel.AddDiskPostGetNextAvailableDiskAlias();
                    }
                }), getEntity().getId());
    }

    private void AddDiskPostGetNextAvailableDiskAlias() {
        DiskModel model = (DiskModel) getWindow();
        boolean hasDisks = !disks.isEmpty();

        if (storage == null)
        {
            String cantCreateMessage =
                    ConstantsManager.getInstance().getConstants().thereIsNoActiveStorageDomainCreateDiskInMsg();
            if (hasDisks)
            {
                cantCreateMessage =
                        ConstantsManager.getInstance().getConstants().errorRetrievingRelevantStorageDomainMsg();
                if (storage != null && storage.getstorage_name() != null)
                {
                    cantCreateMessage =
                            ConstantsManager.getInstance()
                                    .getMessages()
                                    .storageDomainIsNotActive(storage.getstorage_name());
                }
            }

            model.setMessage(cantCreateMessage);

            UICommand tempVar = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnAddDisk", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar2.setIsDefault(true);
            model.getCommands().add(tempVar2);

            UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
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

            // Save changes.
            storage_domains storageDomain = (storage_domains) model.getStorageDomain().getSelectedItem();

            DiskImage tempVar = new DiskImage();
            tempVar.setSizeInGigabytes(Integer.parseInt(model.getSize().getEntity().toString()));
            tempVar.setDiskAlias((String) model.getAlias().getEntity());
            tempVar.setDiskDescription((String) model.getDescription().getEntity());
            tempVar.setDiskInterface((DiskInterface) model.getInterface().getSelectedItem());
            tempVar.setvolume_type((VolumeType) model.getVolumeType().getSelectedItem());
            tempVar.setvolume_format(model.getVolumeFormat());
            tempVar.setWipeAfterDelete((Boolean) model.getWipeAfterDelete().getEntity());
            tempVar.setBoot((Boolean) model.getIsBootable().getEntity());
            tempVar.setShareable((Boolean) model.getIsShareable().getEntity());
            tempVar.setPropagateErrors(PropagateErrors.Off);
            if (model.getQuota().getIsAvailable()) {
                tempVar.setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
            }
            DiskImage disk = tempVar;

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

        if (StringHelper.stringsEqual(command.getName(), "AddNetwork")) //$NON-NLS-1$
        {
            AddNetwork();
        }
        if (StringHelper.stringsEqual(command.getName(), "AddDisk")) //$NON-NLS-1$
        {
            AddDisk();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddNetwork")) //$NON-NLS-1$
        {
            OnAddNetwork();
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
