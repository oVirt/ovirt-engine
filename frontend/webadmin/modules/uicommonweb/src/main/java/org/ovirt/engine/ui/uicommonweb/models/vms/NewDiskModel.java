package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class NewDiskModel extends AbstractDiskModel
{
    public NewDiskModel() {
    }

    public NewDiskModel(SystemTreeItemModel systemTreeSelectedItem) {
        setSystemTreeSelectedItem(systemTreeSelectedItem);
    }

    @Override
    public void initialize() {
        super.initialize();

        if (getVm() != null) {
            updateSuggestedDiskAlias();
            getIsPlugged().setIsAvailable(true);
        } else {
            // Read only disk can be created only in the scope of VM.
            getIsReadOnly().setIsAvailable(false);
            getIsPlugged().setEntity(false);
        }

        getSizeExtend().setIsAvailable(false);
    }

    private void updateSuggestedDiskAlias() {
        AsyncDataProvider.getInstance().getNextAvailableDiskAliasNameByVMId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                String suggestedDiskAlias = (String) returnValue;
                DiskModel diskModel = (DiskModel) model;
                diskModel.getAlias().setEntity(suggestedDiskAlias);
            }
        }, getHash()), getVm().getId());
    }

    private void onAttachDisks()
    {
        ArrayList<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();
        ArrayList<IFrontendActionAsyncCallback> callbacks = new ArrayList<IFrontendActionAsyncCallback>();

        IFrontendActionAsyncCallback onFinishCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                NewDiskModel diskModel = (NewDiskModel) result.getState();
                diskModel.stopProgress();
                diskModel.cancel();
                postSave();
            }
        };

        ArrayList<EntityModel<DiskModel>> disksToAttach = getIsInternal().getEntity() ?
                (ArrayList<EntityModel<DiskModel>>) getInternalAttachableDisks().getSelectedItems() :
                (ArrayList<EntityModel<DiskModel>>) getExternalAttachableDisks().getSelectedItems();

        for (int i = 0; i < disksToAttach.size(); i++) {
            DiskModel disk = disksToAttach.get(i).getEntity();
            // Disk is attached to VM as read only or not, null is applicable only for floating disks
            // but this is not a case here.
            AttachDetachVmDiskParameters parameters = new AttachDetachVmDiskParameters(
                    getVm().getId(), disk.getDisk().getId(), getIsPlugged().getEntity(),
                    Boolean.TRUE.equals(disk.getDisk().getReadOnly()));

            actionTypes.add(VdcActionType.AttachDiskToVm);
            paramerterList.add(parameters);
            callbacks.add(i == disksToAttach.size() - 1 ? onFinishCallback : null);
        }

        startProgress(null);

        Frontend.getInstance().runMultipleActions(actionTypes, paramerterList, callbacks, null, this);
    }

    @Override
    public boolean getIsNew() {
        return true;
    }

    @Override
    protected boolean isDatacenterAvailable(StoragePool dataCenter) {
        boolean isStatusUp = dataCenter.getStatus() == StoragePoolStatus.Up;

        boolean isInTreeContext = true;
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
        {
            switch (getSystemTreeSelectedItem().getType())
            {
            case DataCenter:
                StoragePool selectedDataCenter = (StoragePool) getSystemTreeSelectedItem().getEntity();
                isInTreeContext = selectedDataCenter.getId().equals(dataCenter.getId());
            default:
                break;
            }
        }

        return isStatusUp && isInTreeContext;
    }

    @Override
    public void setDefaultInterface() {
        getDiskInterface().setSelectedItem(DiskInterface.VirtIO);
    }

    @Override
    protected DiskImage getDiskImage() {
        return new DiskImage();
    }

    @Override
    protected LunDisk getLunDisk() {
        return new LunDisk();
    }

    @Override
    public void onSave() {
        if (getProgress() != null || !validate()) {
            return;
        }

        if (getIsAttachDisk().getEntity()) {
            onAttachDisks();
            return;
        }

        super.onSave();

        boolean isInternal = getIsInternal().getEntity();
        if (isInternal) {
            DiskImage diskImage = (DiskImage) getDisk();
            diskImage.setSizeInGigabytes(getSize().getEntity());
            diskImage.setVolumeType(getVolumeType().getSelectedItem());
            diskImage.setvolumeFormat(getVolumeFormat());
        }
        else {
            LunDisk lunDisk = (LunDisk) getDisk();
            LUNs luns = (LUNs) getSanStorageModel().getAddedLuns().get(0).getEntity();
            luns.setLunType(getStorageType().getSelectedItem());
            lunDisk.setLun(luns);
        }

        startProgress(null);

        AddDiskParameters parameters = new AddDiskParameters(getVmId(), getDisk());
        parameters.setPlugDiskToVm(getIsPlugged().getEntity());
        if (getIsInternal().getEntity()) {
            StorageDomain storageDomain = getStorageDomain().getSelectedItem();
            parameters.setStorageDomainId(storageDomain.getId());
        }

        Frontend.getInstance().runAction(VdcActionType.AddDisk, parameters, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                NewDiskModel diskModel = (NewDiskModel) result.getState();
                diskModel.stopProgress();
                diskModel.cancel();
                postSave();
            }
        }, this);
    }

    protected void postSave() {
        // empty by default
    }

    @Override
    public boolean validate() {
        if (getIsAttachDisk().getEntity()) {
            if (isSelectionsEmpty(getInternalAttachableDisks()) && isSelectionsEmpty(getExternalAttachableDisks())) {
                getInvalidityReasons().add(CONSTANTS.noDisksSelected());
                setIsValid(false);
                return false;
            }

            return true;
        }

        if (!getIsInternal().getEntity() && getSanStorageModel() != null) {
            getSanStorageModel().validate();
            if (!getSanStorageModel().getIsValid()) {
                return false;
            }

            ArrayList<String> partOfSdLunsMessages = getSanStorageModel().getPartOfSdLunsMessages();
            if (!partOfSdLunsMessages.isEmpty() && !getSanStorageModel().isForce()) {
                forceCreationWarning(partOfSdLunsMessages);
                return false;
            }
        }

        StorageType storageType = getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN
                : getStorageDomain().getSelectedItem().getStorageType();
        IntegerValidation sizeValidation = new IntegerValidation();
        sizeValidation.setMinimum(1);
        if (storageType.isBlockDomain()) {
            sizeValidation.setMaximum((Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.MaxBlockDiskSize));
        }
        getSize().validateEntity(new IValidation[] { new NotEmptyValidation(), sizeValidation });
        getStorageDomain().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return super.validate() && getSize().getIsValid() && getStorageDomain().getIsValid();
    }

    private boolean isSelectionsEmpty(ListModel listModel) {
        return listModel.getSelectedItems() == null || listModel.getSelectedItems().isEmpty();
    }

    @Override
    protected void updateVolumeType(StorageType storageType) {
        getVolumeType().setSelectedItem(storageType.isBlockDomain() ? VolumeType.Preallocated : VolumeType.Sparse);
        volumeType_SelectedItemChanged();
    }
}
