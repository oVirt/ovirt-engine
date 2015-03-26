package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
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
        getIsScsiPassthrough().setEntity(getDisk().isScsiPassthrough());
        getIsSgIoUnfiltered().setEntity(getDisk().getSgio() == ScsiGenericIO.UNFILTERED);
        getIsReadOnly().setEntity(getDisk().getReadOnly());

        switch (getDisk().getDiskStorageType()) {
            case IMAGE:
                DiskImage diskImage = (DiskImage) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.IMAGE);
                getSize().setEntity((int) diskImage.getSizeInGigabytes());
                getVolumeType().setSelectedItem(diskImage.getVolumeType());

                boolean isExtendImageSizeEnabled = getVm() != null && !diskImage.isDiskSnapshot() &&
                        VdcActionUtils.canExecute(Arrays.asList(getVm()), VM.class, VdcActionType.ExtendImageSize);
                getSizeExtend().setIsChangable(isExtendImageSizeEnabled);
                break;
            case LUN:
                LunDisk lunDisk = (LunDisk) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.LUN);
                getSize().setEntity(lunDisk.getLun().getDeviceSize());
                getSizeExtend().setIsAvailable(false);
                break;
            case CINDER:
                CinderDisk cinderDisk = (CinderDisk) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.CINDER);
                getSize().setEntity((int) cinderDisk.getSizeInGigabytes());
                getSizeExtend().setIsChangable(true);
                break;
        }

        updateReadOnlyChangeability();
        updateWipeAfterDeleteChangeability();
    }

    @Override
    protected void datacenter_SelectedItemChanged() {
        super.datacenter_SelectedItemChanged();
        // this needs to be executed after the data center is loaded because the update quota needs both values
        if (getDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
            Guid storageDomainId = ((DiskImage) getDisk()).getStorageIds().get(0);
            AsyncDataProvider.getInstance().getStorageDomainById(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    DiskModel diskModel = (DiskModel) target;
                    StorageDomain storageDomain = (StorageDomain) returnValue;
                    diskModel.getStorageDomain().setSelectedItem(storageDomain);
                }
            }), storageDomainId);
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
    public void setDefaultInterface() {
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
    protected CinderDisk getCinderDisk() {
        return (CinderDisk) getDisk();
    }

    @Override
    public void store(IFrontendActionAsyncCallback callback) {
        if (getProgress() != null || !validate()) {
            return;
        }

        startProgress(null);

        UpdateVmDiskParameters parameters = new UpdateVmDiskParameters(getVmId(), getDisk().getId(), getDisk());
        IFrontendActionAsyncCallback onFinished = callback != null ? callback : new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                EditDiskModel diskModel = (EditDiskModel) result.getState();
                diskModel.stopProgress();
                diskModel.cancel();
            }
        };
        Frontend.getInstance().runAction(VdcActionType.UpdateVmDisk, parameters, onFinished, this);
    }

    @Override
    public boolean validate() {
        getSizeExtend().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new NonNegativeLongNumberValidation()
        });
        return super.validate() && getSizeExtend().getIsValid();
    }

    private void disableNonChangeableEntities() {
        getStorageDomain().setIsChangable(false);
        getHost().setIsChangable(false);
        getStorageType().setIsChangable(false);
        getDataCenter().setIsChangable(false);
        getVolumeType().setIsChangable(false);
        getSize().setIsChangable(false);
        getCinderVolumeType().setIsChangable(false);

        if (!isEditEnabled()) {
            getIsShareable().setIsChangable(false);
            getIsBootable().setIsChangable(false);
            getDiskInterface().setIsChangable(false);
            getIsReadOnly().setIsChangable(false);
        }
    }

    private Guid getStorageDomainId() {
        switch (getDisk().getDiskStorageType()) {
            case IMAGE:
                return  getDiskImage().getStorageIds().get(0);
            case CINDER:
                return  getCinderDisk().getStorageIds().get(0);
        }
        return null;
    }

    @Override
    protected void updateStorageDomains(final StoragePool datacenter) {
        AsyncDataProvider.getInstance().getStorageDomainById(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                DiskModel diskModel = (DiskModel) target;
                StorageDomain storageDomain = (StorageDomain) returnValue;
                diskModel.getStorageDomain().setSelectedItem(storageDomain);
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
