package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.hosts.ValueEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class NewDiskModel extends AbstractDiskModel {
    private boolean descriptionDerivedFromLunId;

    private IEventListener<ValueEventArgs<LunModel>> lunSelectionChangedEventListener =
            new IEventListener<ValueEventArgs<LunModel>> () {
        @Override
        public void eventRaised(Event<? extends ValueEventArgs<LunModel>> ev,
                Object sender,
                ValueEventArgs<LunModel> args) {
            String description = getDescription().getEntity();
            if (description == null || description.isEmpty() || descriptionDerivedFromLunId) {
                LunModel selectedLunModel = args.getValue();
                if (selectedLunModel.getLunId() != null) {
                    int numOfChars = (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.PopulateDirectLUNDiskDescriptionWithLUNId);
                    if (numOfChars == 0) {
                        return;
                    }
                    String newDescription;
                    if (numOfChars <= -1 || numOfChars >= selectedLunModel.getLunId().length()) {
                        newDescription = selectedLunModel.getLunId();
                    } else {
                        newDescription = selectedLunModel.getLunId().substring(selectedLunModel.getLunId().length() - numOfChars);
                    }
                    getDescription().setEntity(newDescription);
                    descriptionDerivedFromLunId = true;
                }
            }
            updatePassDiscardChangeability();
        }
    };

    @Override
    public void initialize() {
        super.initialize();
        setDiskVmElement(new DiskVmElement(new VmDeviceId(null, getIsFloating() ? null : getVm().getId())));

        if (!getIsFloating()) {
            if (getIsBootable().getIsChangable()) {
                getIsBootable().setEntity(true);
            }
            updateSuggestedDiskAliasFromServer();
            getIsPlugged().setIsAvailable(true);
        } else {
            // Read only disk can be created only in the scope of VM.
            getIsReadOnly().setIsAvailable(false);
            getIsPlugged().setEntity(false);
            getIsBootable().setIsAvailable(false);
            getDiskInterface().setIsAvailable(false);
            getPassDiscard().setIsAvailable(false);

            // set using scsi reservation to be invisible
            getIsUsingScsiReservation().setIsAvailable(false);
            getIsUsingScsiReservation().setEntity(false);
        }

        getSizeExtend().setIsAvailable(false);
    }

    @Override
    public void initialize(List<DiskModel> currentDisks) {
        super.initialize(currentDisks);
        setDiskVmElement(new DiskVmElement(new VmDeviceId(null, getIsFloating() ? null : getVm().getId())));
    }

    public void updateSuggestedDiskAliasFromServer() {
        AsyncDataProvider.getInstance().getNextAvailableDiskAliasNameByVMId(new AsyncQuery<>(suggestedDiskAlias -> getAlias().setEntity(suggestedDiskAlias)), getVm().getId());
    }

    @Override
    public boolean getIsNew() {
        return true;
    }

    @Override
    protected boolean isDatacenterAvailable(StoragePool dataCenter) {
        return dataCenter.getStatus() == StoragePoolStatus.Up;
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
    protected ManagedBlockStorageDisk getManagedBlockDisk() {
        return new ManagedBlockStorageDisk();
    }

    @Override
    public void flush() {
        super.flush();
        switch (getDiskStorageType().getEntity()) {
            case LUN:
                LunDisk lunDisk = (LunDisk) getDisk();
                LUNs luns = getSanStorageModelBase().getAddedLuns().get(0).getEntity();
                luns.setLunType(getStorageType().getSelectedItem());
                lunDisk.setLun(luns);
                break;
            default:
                DiskImage diskImage = (DiskImage) getDisk();
                if (getSize() != null && getSize().getEntity() != null) {
                    diskImage.setSizeInGigabytes(getSize().getEntity());
                }
                diskImage.setVolumeType(getVolumeType().getSelectedItem());
                // Incremental backup can be enabled only for COW VolumeFormat
                VolumeFormat volumeFormat = getIsIncrementalBackup().getEntity() ? VolumeFormat.COW :
                        AsyncDataProvider.getInstance().getDiskVolumeFormat(
                                diskImage.getVolumeType(), getStorageDomain().getSelectedItem().getStorageType());
                diskImage.setVolumeFormat(volumeFormat);
                break;
        }
    }

    @Override
    public void store(IFrontendActionAsyncCallback callback) {
        if (getProgress() != null) {
            return;
        }

        startProgress();

        AddDiskParameters parameters = new AddDiskParameters(getDiskVmElement(), getDisk());
        parameters.setPlugDiskToVm(getIsPlugged().getEntity());
        if (getDiskStorageType().getEntity().isInternal()) {
            StorageDomain storageDomain = getStorageDomain().getSelectedItem();
            parameters.setStorageDomainId(storageDomain.getId());
        }

        IFrontendActionAsyncCallback onFinished = callback != null ? callback : result -> {
            NewDiskModel diskModel = (NewDiskModel) result.getState();
            diskModel.stopProgress();
            diskModel.cancel();
            postSave();
        };

        Frontend.getInstance().runAction(ActionType.AddDisk, parameters, onFinished, this);
    }

    protected void postSave() {
        // empty by default
    }

    @Override
    public boolean validate() {
        if (getDiskStorageType().getEntity() == DiskStorageType.LUN && getSanStorageModelBase() != null) {
            getSanStorageModelBase().validate();
            if (!getSanStorageModelBase().getIsValid()) {
                return false;
            }

            ArrayList<String> partOfSdLunsMessages = getSanStorageModelBase().getPartOfSdLunsMessages();
            if (!partOfSdLunsMessages.isEmpty() && !getSanStorageModelBase().isForce()) {
                forceCreationWarning(partOfSdLunsMessages);
                return false;
            }
        }

        StorageType storageType = getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN
                : getStorageDomain().getSelectedItem().getStorageType();
        IntegerValidation sizeValidation = new IntegerValidation();
        sizeValidation.setMinimum(getMinimumDiskSize());
        if (storageType.isBlockDomain()) {
            sizeValidation.setMaximum((Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.MaxBlockDiskSizeInGibiBytes));
        }
        getSize().validateEntity(new IValidation[] { new NotEmptyValidation(), sizeValidation });
        getStorageDomain().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return super.validate() && getSize().getIsValid() && getStorageDomain().getIsValid();
    }

    public int getMinimumDiskSize() {
        return 1;
    }

    @Override
    protected void updateVolumeType(StorageType storageType) {
        // In case the user didn't select any specific allocation policy, it will be change according to the storage
        // domain type
        if (storageType.isManagedBlockStorage()) {
            getVolumeType().setSelectedItem(VolumeType.Preallocated);
            getVolumeType().setIsChangeable(false);
        } else if (!isUserSelectedVolumeType) {
            VolumeType volumeType;

            // Block based storage domain default volume type is preallocated.
            // Due to bug 1644159 Gluster based storage domain default volume type
            // should be preallocated also.
            if (storageType.isBlockDomain() || storageType == StorageType.GLUSTERFS) {
                volumeType = VolumeType.Preallocated;
            } else {
                volumeType = VolumeType.Sparse;
            }
            getVolumeType().setSelectedItem(volumeType);
            getVolumeType().setIsChangeable(true);
        }
    }

    @Override
    public void setSanStorageModelBase(SanStorageModelBase sanStorageModelBase) {
        super.setSanStorageModelBase(sanStorageModelBase);

        if (!sanStorageModelBase.getLunSelectionChangedEvent().getListeners().contains(lunSelectionChangedEventListener)) {
            sanStorageModelBase.getLunSelectionChangedEvent().addListener(lunSelectionChangedEventListener);
        }
    }

    @Override
    protected void diskStorageType_EntityChanged() {
        super.diskStorageType_EntityChanged();
        if (descriptionDerivedFromLunId) {
            getDescription().setEntity(null);
        }
        descriptionDerivedFromLunId = false;
    }
}
