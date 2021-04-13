package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UpdateDiskParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.validation.DiskExtendSizeValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NonNegativeLongNumberValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class EditDiskModel extends AbstractDiskModel {
    public EditDiskModel() {
    }

    @Override
    public void initialize() {
        super.initialize();
        initDiskVmElement();
        disableNonChangeableEntities();

        getAlias().setEntity(getDisk().getDiskAlias());
        getDescription().setEntity(getDisk().getDiskDescription());
        getIsShareable().setEntity(getDisk().isShareable());
        getIsWipeAfterDelete().setEntity(getDisk().isWipeAfterDelete());
        getIsScsiPassthrough().setEntity(getDisk().isScsiPassthrough());
        getIsSgIoUnfiltered().setEntity(getDisk().getSgio() == ScsiGenericIO.UNFILTERED);
        getIsBootable().setIsAvailable(false);
        getDiskInterface().setIsAvailable(false);

        switch (getDisk().getDiskStorageType()) {
            case IMAGE:
                DiskImage diskImage = (DiskImage) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.IMAGE);
                getSize().setEntity((int) diskImage.getSizeInGigabytes());
                getVolumeType().setSelectedItem(diskImage.getVolumeType());
                updateIncrementalBackupChangeability(diskImage);
                getIsIncrementalBackup().setEntity(diskImage.getBackup() == DiskBackup.Incremental);
                getSizeExtend().setIsChangeable(isSizeExtendChangeable(diskImage));
                break;
            case LUN:
                LunDisk lunDisk = (LunDisk) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.LUN);
                getStorageType().setIsAvailable(false);
                getSize().setEntity(lunDisk.getLun().getDeviceSize());
                getSizeExtend().setIsAvailable(false);
                getHost().setIsAvailable(false);
                break;
            case MANAGED_BLOCK_STORAGE:
                ManagedBlockStorageDisk managedBlockDisk = (ManagedBlockStorageDisk) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.MANAGED_BLOCK_STORAGE);
                getSize().setEntity((int) managedBlockDisk.getSizeInGigabytes());
                getSizeExtend().setIsChangeable(true);
                break;
        }
    }

    protected boolean isSizeExtendChangeable(DiskImage diskImage) {
        return true;
    }

    private void updateIncrementalBackupChangeability(DiskImage diskImage) {
        // Allow editing "enable backup" checkbox only for qcow2 disks.
        if (diskImage.getVolumeFormat() == VolumeFormat.RAW) {
            getIsIncrementalBackup().setIsChangeable(false, constants.incrementalBackupNotSupportedForRawDisks());
        } else {
            getIsIncrementalBackup().setIsChangeable(true);
        }
    }

    protected void initDiskVmElement() {
        setDiskVmElement(new DiskVmElement(new VmDeviceId()));
    }

    @Override
    protected void datacenter_SelectedItemChanged() {
        super.datacenter_SelectedItemChanged();
        // this needs to be executed after the data center is loaded because the update quota needs both values
        if (getDisk().getDiskStorageType() == DiskStorageType.IMAGE
                || getDisk().getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
            Guid storageDomainId = ((DiskImage) getDisk()).getStorageIds().get(0);
            AsyncDataProvider.getInstance().getStorageDomainById(new AsyncQuery<>(storageDomain -> getStorageDomain().setSelectedItem(storageDomain)), storageDomainId);
        } else if (getDisk().getDiskStorageType() == DiskStorageType.LUN) {
            LunDisk lunDisk = (LunDisk) getDisk();
            getDiskStorageType().setEntity(DiskStorageType.LUN);
            getSize().setEntity(lunDisk.getLun().getDeviceSize());
            getSizeExtend().setIsAvailable(false);
        }
    }

    @Override
    public boolean getIsNew() {
        return false;
    }

    @Override
    protected boolean isDatacenterAvailable(StoragePool dataCenter) {
        return true;
    }

    @Override
    protected DiskImage getDiskImage() {
        return (DiskImage) getDisk();
    }

    @Override
    protected LunDisk getLunDisk() {
        return (LunDisk) getDisk();
    }

    @Override
    protected ManagedBlockStorageDisk getManagedBlockDisk() {
        return (ManagedBlockStorageDisk) getDisk();
    }

    @Override
    public void store(IFrontendActionAsyncCallback callback) {
        if (getProgress() != null) {
            return;
        }

        startProgress();

        UpdateDiskParameters parameters = new UpdateDiskParameters(getDisk());
        IFrontendActionAsyncCallback onFinished = callback != null ? callback : result -> {
            EditDiskModel diskModel = (EditDiskModel) result.getState();
            diskModel.stopProgress();
            diskModel.cancel();
        };
        Frontend.getInstance().runAction(ActionType.UpdateDisk, parameters, onFinished, this);
    }

    @Override
    public boolean validate() {
        StorageType storageType = getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN
                : getStorageDomain().getSelectedItem().getStorageType();
        DiskExtendSizeValidation sizeValidation = new DiskExtendSizeValidation();
        if (storageType.isBlockDomain()) {
            Integer maxBlockDiskSize =
                    (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.MaxBlockDiskSizeInGibiBytes);
            sizeValidation.setMaximum(maxBlockDiskSize - getSize().getEntity());
        }
        getSizeExtend().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new NonNegativeLongNumberValidation(),
                sizeValidation
        });
        return super.validate() && getSizeExtend().getIsValid();
    }

    private void disableNonChangeableEntities() {
        getStorageDomain().setIsChangeable(false);
        getHost().setIsChangeable(false);
        getStorageType().setIsChangeable(false);
        getDataCenter().setIsChangeable(false);
        getVolumeType().setIsChangeable(false);
        getSize().setIsChangeable(false);

        if (!isEditEnabled()) {
            getIsShareable().setIsChangeable(false);
            getIsBootable().setIsChangeable(false);
            getDiskInterface().setIsChangeable(false);
            getPassDiscard().setIsChangeable(false);
            getIsReadOnly().setIsChangeable(false);
        }
    }

    @Override
    protected void updateStorageDomains(final StoragePool datacenter) {
        AsyncDataProvider.getInstance().getStorageDomainById(new AsyncQuery<>(storageDomain -> getStorageDomain().setSelectedItem(storageDomain)), getStorageDomainId());
    }

    @Override
    protected void updateVolumeType(StorageType storageType) {
        // do nothing
    }

}
