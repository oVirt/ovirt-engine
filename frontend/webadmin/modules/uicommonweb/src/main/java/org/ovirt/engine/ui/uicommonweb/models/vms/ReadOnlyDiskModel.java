package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class ReadOnlyDiskModel extends AbstractDiskModel {

    @Override
    public void initialize() {
        super.initialize();

        getSizeExtend().setIsAvailable(false);

        getAlias().setEntity(getDisk().getDiskAlias());
        getDescription().setEntity(getDisk().getDiskDescription());
        getIsShareable().setEntity(getDisk().isShareable());
        getIsWipeAfterDelete().setEntity(getDisk().isWipeAfterDelete());
        getIsScsiPassthrough().setEntity(getDisk().isScsiPassthrough());
        getIsSgIoUnfiltered().setEntity(getDisk().getSgio() == ScsiGenericIO.UNFILTERED);
        getIsReadOnly().setEntity(getVm() != null && getDisk().getReadOnly());

        switch (getDisk().getDiskStorageType()) {
            case IMAGE:
                DiskImage diskImage = (DiskImage) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.IMAGE);
                getSize().setEntity((int) diskImage.getSizeInGigabytes());
                getVolumeType().setSelectedItem(diskImage.getVolumeType());
                break;
            case LUN:
                LunDisk lunDisk = (LunDisk) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.LUN);
                getStorageType().setIsAvailable(false);
                getSize().setEntity(lunDisk.getLun().getDeviceSize());
                break;
            case CINDER:
                CinderDisk cinderDisk = (CinderDisk) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.CINDER);
                getSize().setEntity((int) cinderDisk.getSizeInGigabytes());
                break;
        }
    }

    @Override
    protected void datacenter_SelectedItemChanged() {
        super.datacenter_SelectedItemChanged();
        getIsModelDisabled().setEntity(true);
        getDataCenter().setIsChangeable(false);

        // this needs to be executed after the data center is loaded because the update quota needs both values
        if (getDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
            Guid storageDomainId = ((DiskImage) getDisk()).getStorageIds().get(0);
            AsyncDataProvider.getInstance().getStorageDomainById(new AsyncQuery<>(new AsyncCallback<StorageDomain>() {
                @Override
                public void onSuccess(StorageDomain storageDomain) {
                    getStorageDomain().setSelectedItem(storageDomain);
                }
            }), storageDomainId);
        } else if (getDisk().getDiskStorageType() == DiskStorageType.LUN) {
            LunDisk lunDisk = (LunDisk) getDisk();
            getDiskStorageType().setEntity(DiskStorageType.LUN);
            getSize().setEntity(lunDisk.getLun().getDeviceSize());
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
    protected CinderDisk getCinderDisk() {
        return (CinderDisk) getDisk();
    }

    @Override
    public void store(IFrontendActionAsyncCallback callback) {
        return;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    protected void updateStorageDomains(final StoragePool datacenter) {
        AsyncDataProvider.getInstance().getStorageDomainById(new AsyncQuery<>(new AsyncCallback<StorageDomain>() {
            @Override
            public void onSuccess(StorageDomain storageDomain) {
                getStorageDomain().setSelectedItem(storageDomain);
            }
        }), getStorageDomainId());
    }

    @Override
    protected void updateVolumeType(StorageType storageType) {
        // do nothing
    }

    @Override
    protected void updateCinderVolumeTypes() {
        getCinderVolumeType().setSelectedItem(getDisk().getCinderVolumeType());
    }
}
