package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class ReadOnlyDiskModel extends EditVmDiskModel {

    @Override
    public void initialize() {
        commonInitialize();

        getSizeExtend().setIsAvailable(false);
        getAlias().setEntity(getDisk().getDiskAlias());
        getDescription().setEntity(getDisk().getDiskDescription());
        getIsShareable().setEntity(getDisk().isShareable());
        getIsWipeAfterDelete().setEntity(getDisk().isWipeAfterDelete());
        getIsScsiPassthrough().setEntity(getDisk().isScsiPassthrough());
        getIsSgIoUnfiltered().setEntity(getDisk().getSgio() == ScsiGenericIO.UNFILTERED);
        getIsReadOnly().setEntity(getVm() != null && getDiskVmElement().isReadOnly());

        switch (getDisk().getDiskStorageType()) {
            case IMAGE:
                DiskImage diskImage = (DiskImage) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.IMAGE);
                getSize().setEntity((int) diskImage.getSizeInGigabytes());
                getVolumeType().setSelectedItem(diskImage.getVolumeType());
                break;
        }
    }

    @Override
    protected void datacenter_SelectedItemChanged() {
        super.datacenter_SelectedItemChanged();

        getIsModelDisabled().setEntity(true);
        getDataCenter().setIsChangeable(false);
    }

    @Override
    public void store(IFrontendActionAsyncCallback callback) {
        // do nothing
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    protected void updateStorageDomains(final StoragePool datacenter) {
        AsyncDataProvider.getInstance().getStorageDomainById(new AsyncQuery<>(storageDomain -> getStorageDomain().setSelectedItem(storageDomain)), getStorageDomainId());
    }
}
