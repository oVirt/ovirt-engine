package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.hosts.ValueEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class NewDiskModel extends AbstractDiskModel {
    private boolean descriptionDerivedFromLunId;

    private IEventListener lunSelectionChangedEventListener = new IEventListener <ValueEventArgs<LunModel>>() {
        @Override
        public void eventRaised(Event<? extends ValueEventArgs<LunModel>> ev,
                Object sender,
                ValueEventArgs<LunModel> args) {
            String description = getDescription().getEntity();
            if (description == null || description.isEmpty() || descriptionDerivedFromLunId) {
                LunModel selectedLunModel = args.getValue();
                if (selectedLunModel.getLunId() != null) {
                    int numOfChars = (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.PopulateDirectLUNDiskDescriptionWithLUNId);
                    if (numOfChars == 0) {
                        return;
                    }
                    String newDescription;
                    if (numOfChars <= -1 || numOfChars >= selectedLunModel.getLunId().length()) {
                        newDescription = selectedLunModel.getLunId();
                    }
                    else {
                        newDescription = selectedLunModel.getLunId().substring(selectedLunModel.getLunId().length() - numOfChars);
                    }
                    getDescription().setEntity(newDescription);
                    descriptionDerivedFromLunId = true;
                }
            }
        }
    };

    public NewDiskModel() {
    }

    public NewDiskModel(SystemTreeItemModel systemTreeSelectedItem) {
        setSystemTreeSelectedItem(systemTreeSelectedItem);
    }

    @Override
    public void initialize() {
        super.initialize();
        setDiskVmElement(new DiskVmElement(new VmDeviceId(null, getIsFloating() ? null : getVm().getId())));

        if (!getIsFloating()) {
            updateSuggestedDiskAliasFromServer();
            getIsPlugged().setIsAvailable(true);
        } else {
            // Read only disk can be created only in the scope of VM.
            getIsReadOnly().setIsAvailable(false);
            getIsPlugged().setEntity(false);
            getIsBootable().setIsAvailable(false);
            getDiskInterface().setIsAvailable(false);

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
        AsyncDataProvider.getInstance().getNextAvailableDiskAliasNameByVMId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                String suggestedDiskAlias = (String) returnValue;
                DiskModel diskModel = (DiskModel) model;
                diskModel.getAlias().setEntity(suggestedDiskAlias);
            }
        }), getVm().getId());
    }

    @Override
    public boolean getIsNew() {
        return true;
    }

    @Override
    protected boolean isDatacenterAvailable(StoragePool dataCenter) {
        boolean isStatusUp = dataCenter.getStatus() == StoragePoolStatus.Up;

        boolean isInTreeContext = true;
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() != SystemTreeItemType.System) {
            switch (getSystemTreeSelectedItem().getType()) {
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
    protected DiskImage getDiskImage() {
        return new DiskImage();
    }

    @Override
    protected LunDisk getLunDisk() {
        return new LunDisk();
    }

    @Override
    protected CinderDisk getCinderDisk() {
        return new CinderDisk();
    }

    @Override
    public void flush() {
        if (!validate()) {
            return;
        }
        super.flush();
        switch (getDiskStorageType().getEntity()) {
            case LUN:
                LunDisk lunDisk = (LunDisk) getDisk();
                LUNs luns = getSanStorageModel().getAddedLuns().get(0).getEntity();
                luns.setLunType(getStorageType().getSelectedItem());
                lunDisk.setLun(luns);
                break;
            case CINDER:
                CinderDisk cinderDisk = (CinderDisk) getDisk();
                cinderDisk.setSizeInGigabytes(getSize().getEntity());
                cinderDisk.setVolumeFormat(VolumeFormat.RAW);
                cinderDisk.setCinderVolumeType(getCinderVolumeType().getSelectedItem());
                break;
            default:
                DiskImage diskImage = (DiskImage) getDisk();
                if (getSize() != null && getSize().getEntity() != null) {
                    diskImage.setSizeInGigabytes(getSize().getEntity());
                }
                diskImage.setVolumeType(getVolumeType().getSelectedItem());
                diskImage.setVolumeFormat(AsyncDataProvider.getInstance().getDiskVolumeFormat(diskImage.getVolumeType(), getStorageDomain().getSelectedItem().getStorageType()));
                break;
        }
    }

    @Override
    public void store(IFrontendActionAsyncCallback callback) {
        if (getProgress() != null || !validate()) {
            return;
        }

        startProgress();

        AddDiskParameters parameters = new AddDiskParameters(getDiskVmElement(), getDisk());
        parameters.setPlugDiskToVm(getIsPlugged().getEntity());
        if (getDiskStorageType().getEntity() == DiskStorageType.IMAGE ||
                getDiskStorageType().getEntity() == DiskStorageType.CINDER) {
            StorageDomain storageDomain = getStorageDomain().getSelectedItem();
            parameters.setStorageDomainId(storageDomain.getId());
        }

        IFrontendActionAsyncCallback onFinished = callback != null ? callback : new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                NewDiskModel diskModel = (NewDiskModel) result.getState();
                diskModel.stopProgress();
                diskModel.cancel();
                postSave();
            }
        };

        Frontend.getInstance().runAction(VdcActionType.AddDisk, parameters, onFinished, this);
    }

    protected void postSave() {
        // empty by default
    }

    @Override
    public boolean validate() {
        if (getDiskStorageType().getEntity() == DiskStorageType.LUN && getSanStorageModel() != null) {
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
        sizeValidation.setMinimum(getMinimumDiskSize());
        if (storageType.isBlockDomain()) {
            sizeValidation.setMaximum((Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.MaxBlockDiskSize));
        }
        getSize().validateEntity(new IValidation[] { new NotEmptyValidation(), sizeValidation });
        getStorageDomain().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return super.validate() && getSize().getIsValid() && getStorageDomain().getIsValid();
    }

    protected int getMinimumDiskSize() {
        return 1;
    }

    @Override
    protected void updateVolumeType(StorageType storageType) {
        getVolumeType().setSelectedItem(storageType.isBlockDomain() ? VolumeType.Preallocated : VolumeType.Sparse);
        volumeType_SelectedItemChanged();
    }

    @Override
    public void setSanStorageModel(SanStorageModel sanStorageModel) {
        super.setSanStorageModel(sanStorageModel);

        if (!sanStorageModel.getLunSelectionChangedEvent().getListeners().contains(lunSelectionChangedEventListener)) {
            sanStorageModel.getLunSelectionChangedEvent().addListener(lunSelectionChangedEventListener);
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
