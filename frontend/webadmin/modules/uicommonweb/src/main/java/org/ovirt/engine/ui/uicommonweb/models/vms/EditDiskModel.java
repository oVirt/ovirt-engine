package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NonNegativeLongNumberValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

import java.util.Arrays;

public class EditDiskModel extends AbstractDiskModel
{
    public EditDiskModel() {
    }

    @Override
    public void initialize() {
        super.initialize();

        disableNonChangeableEntities();

        getAlias().setEntity(getDisk().getDiskAlias());
        getDescription().setEntity(getDisk().getDiskDescription());
        getIsShareable().setEntity(getDisk().isShareable());
        getIsWipeAfterDelete().setEntity(getDisk().isWipeAfterDelete());
        getIsSgIoUnfiltered().setEntity(getDisk().getSgio() == ScsiGenericIO.UNFILTERED);

        if (getDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) getDisk();
            getSize().setEntity(diskImage.getSizeInGigabytes());
            getIsInternal().setEntity(true);
            getVolumeType().setSelectedItem(diskImage.getVolumeType());
            setVolumeFormat(diskImage.getVolumeFormat());

            boolean isExtendImageSizeEnabled = getVm() != null &&
                    VdcActionUtils.CanExecute(Arrays.asList(getVm()), VM.class, VdcActionType.ExtendImageSize);
            getSizeExtend().setIsChangable(isExtendImageSizeEnabled);

            Guid storageDomainId = diskImage.getStorageIds().get(0);
            AsyncDataProvider.getStorageDomainById(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    DiskModel diskModel = (DiskModel) target;
                    StorageDomain storageDomain = (StorageDomain) returnValue;
                    diskModel.getStorageDomain().setSelectedItem(storageDomain);
                }
            }, getHash()), storageDomainId);
        } else {
            LunDisk lunDisk = (LunDisk) getDisk();
            getSize().setEntity(lunDisk.getLun().getDeviceSize());
            getIsInternal().setEntity(false);
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
    public void updateInterface(Version clusterVersion) {
        super.updateInterface(clusterVersion);
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
        startProgress(null);

        UpdateVmDiskParameters parameters = new UpdateVmDiskParameters(getVmId(), getDisk().getId(), getDisk());
        Frontend.RunAction(VdcActionType.UpdateVmDisk, parameters, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                EditDiskModel diskModel = (EditDiskModel) result.getState();
                diskModel.stopProgress();
                diskModel.cancel();
            }
        }, this);
    }

    @Override
    public boolean validate() {
        super.validate();
        getSizeExtend().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new NonNegativeLongNumberValidation()
        });
        return getSizeExtend().getIsValid();
    }

    private void disableNonChangeableEntities() {
        getStorageDomain().setIsChangable(false);
        getHost().setIsChangable(false);
        getStorageType().setIsChangable(false);
        getDataCenter().setIsChangable(false);
        getVolumeType().setIsChangable(false);
        getSize().setIsChangable(false);

        if (getVm() == null || !getVm().isDown()) {
            getDescription().setIsChangable(false);
            getAlias().setIsChangable(false);
            getIsShareable().setIsChangable(false);
            getIsBootable().setIsChangable(false);
            getIsWipeAfterDelete().setIsChangable(false);
            getDiskInterface().setIsChangable(false);
        }
    }
}
