package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Arrays;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
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

public class EditDiskModel extends AbstractDiskModel {
    public EditDiskModel() {
    }

    @Override
    public void initialize() {
        super.initialize();

        setDiskVmElement(getDisk().getDiskVmElementForVm(getVm().getId()));

        disableNonChangeableEntities();

        getAlias().setEntity(getDisk().getDiskAlias());
        getDescription().setEntity(getDisk().getDiskDescription());
        getIsShareable().setEntity(getDisk().isShareable());
        getIsWipeAfterDelete().setEntity(getDisk().isWipeAfterDelete());
        getIsScsiPassthrough().setEntity(getDisk().isScsiPassthrough());
        getIsSgIoUnfiltered().setEntity(getDisk().getSgio() == ScsiGenericIO.UNFILTERED);
        getIsReadOnly().setEntity(getDisk().getReadOnly());
        getIsBootable().setEntity(getDiskVmElement().isBoot());

        switch (getDisk().getDiskStorageType()) {
            case IMAGE:
                DiskImage diskImage = (DiskImage) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.IMAGE);
                getSize().setEntity((int) diskImage.getSizeInGigabytes());
                getVolumeType().setSelectedItem(diskImage.getVolumeType());

                boolean isExtendImageSizeEnabled = getVm() != null && !diskImage.isDiskSnapshot() &&
                        VdcActionUtils.canExecute(Arrays.asList(getVm()), VM.class, VdcActionType.ExtendImageSize);
                getSizeExtend().setIsChangeable(isExtendImageSizeEnabled);
                break;
            case LUN:
                LunDisk lunDisk = (LunDisk) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.LUN);
                getStorageType().setIsAvailable(false);
                getSize().setEntity(lunDisk.getLun().getDeviceSize());
                getSizeExtend().setIsAvailable(false);
                break;
            case CINDER:
                CinderDisk cinderDisk = (CinderDisk) getDisk();
                getDiskStorageType().setEntity(DiskStorageType.CINDER);
                getSize().setEntity((int) cinderDisk.getSizeInGigabytes());
                getSizeExtend().setIsChangeable(true);
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
        } else if (getDisk().getDiskStorageType() == DiskStorageType.LUN) {
            LunDisk lunDisk = (LunDisk) getDisk();
            getDiskStorageType().setEntity(DiskStorageType.LUN);
            getSize().setEntity(lunDisk.getLun().getDeviceSize());
            getSizeExtend().setIsAvailable(false);
            getIsUsingScsiReservation().setEntity(lunDisk.isUsingScsiReservation());
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
        if (getProgress() != null || !validate()) {
            return;
        }

        startProgress();

        VmDiskOperationParameterBase parameters = new VmDiskOperationParameterBase(getDiskVmElement(), getDisk());
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
        getStorageDomain().setIsChangeable(false);
        getHost().setIsChangeable(false);
        getStorageType().setIsChangeable(false);
        getDataCenter().setIsChangeable(false);
        getVolumeType().setIsChangeable(false);
        getSize().setIsChangeable(false);
        getCinderVolumeType().setIsChangeable(false);
        getDiskStorageType().setIsChangeable(false);

        if (!isEditEnabled()) {
            getIsShareable().setIsChangeable(false);
            getIsBootable().setIsChangeable(false);
            getDiskInterface().setIsChangeable(false);
            getIsReadOnly().setIsChangeable(false);
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
