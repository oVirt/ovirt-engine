package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class EditDiskModel extends AbstractDiskModel
{
    public EditDiskModel() {
    }

    @Override
    public void Initialize() {
        super.Initialize();

        getStorageDomain().setIsChangable(false);
        getHost().setIsChangable(false);
        getStorageType().setIsChangable(false);
        getDataCenter().setIsChangable(false);
        getVolumeType().setIsChangable(false);
        getSize().setIsChangable(false);
        getSize().setEntity(getDisk().getDiskStorageType() == DiskStorageType.IMAGE ?
                ((DiskImage) getDisk()).getSizeInGigabytes() :
                ((LunDisk) getDisk()).getLun().getDeviceSize());

        getIsInternal().setEntity(getDisk().getDiskStorageType() == DiskStorageType.IMAGE);
        getAlias().setEntity(getDisk().getDiskAlias());
        getDescription().setEntity(getDisk().getDiskDescription());
        getIsShareable().setEntity(getDisk().isShareable());
        getIsWipeAfterDelete().setEntity(getDisk().isWipeAfterDelete());

        if (getDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) getDisk();

            getVolumeType().setSelectedItem(diskImage.getVolumeType());
            setVolumeFormat(diskImage.getVolumeFormat());
            Guid storageDomainId = diskImage.getStorageIds().get(0);

            AsyncDataProvider.GetStorageDomainById(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel diskModel = (DiskModel) target;
                    StorageDomain storageDomain = (StorageDomain) returnValue;

                    diskModel.getStorageDomain().setSelectedItem(storageDomain);
                }
            }, getHash()), storageDomainId);
        }
    }

    @Override
    public boolean getIsNew() {
        return false;
    }

    @Override
    protected boolean isDatacenterAvailable(storage_pool dataCenter) {
        return true;
    }

    @Override
    protected void updateWipeAfterDelete(StorageType storageType) {
        if (storageType.isFileDomain()) {
            getIsWipeAfterDelete().setChangeProhibitionReason(CONSTANTS.wipeAfterDeleteNotSupportedForFileDomains());
            getIsWipeAfterDelete().setIsChangable(false);
        }
        else {
            getIsWipeAfterDelete().setIsChangable(true);
        }

        getIsWipeAfterDelete().setEntity(getDisk().isWipeAfterDelete());
    }

    @Override
    public void updateInterface(storage_pool datacenter) {
        getDiskInterface().setItems(AsyncDataProvider.GetDiskInterfaceList());
        getDiskInterface().setSelectedItem(getDisk().getDiskInterface());
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
    public void onSave() {
        if (getProgress() != null || !validate()) {
            return;
        }
        super.onSave();
        StartProgress(null);

        UpdateVmDiskParameters parameters = new UpdateVmDiskParameters(getVmId(), getDisk().getId(), getDisk());
        Frontend.RunAction(VdcActionType.UpdateVmDisk, parameters, new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {
                EditDiskModel diskModel = (EditDiskModel) result.getState();
                diskModel.StopProgress();
                diskModel.cancel();
            }
        }, this);
    }

    @Override
    public boolean validate() {
        return super.validate();
    }
}
