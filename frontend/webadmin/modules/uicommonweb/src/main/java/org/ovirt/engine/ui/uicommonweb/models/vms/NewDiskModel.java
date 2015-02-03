package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk;
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
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.hosts.ValueEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class NewDiskModel extends AbstractDiskModel
{
    private boolean descriptionDerivedFromLunId;

    private IEventListener lunSelectionChangedEventListener = new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            String description = getDescription().getEntity();
            if (description == null || description.isEmpty() ||
                    (!description.isEmpty() && descriptionDerivedFromLunId)) {
                LunModel selectedLunModel = ((ValueEventArgs<LunModel>) args).getValue();
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
    public void flush() {
        super.flush();
        if (getDiskStorageType().getEntity() == Disk.DiskStorageType.IMAGE) {
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
    }

    @Override
    public void store(IFrontendActionAsyncCallback callback) {
        if (getProgress() != null || !validate()) {
            return;
        }

        startProgress(null);

        AddDiskParameters parameters = new AddDiskParameters(getVmId(), getDisk());
        parameters.setPlugDiskToVm(getIsPlugged().getEntity());
        if (getDiskStorageType().getEntity() == Disk.DiskStorageType.IMAGE) {
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
        if (getDiskStorageType().getEntity() == Disk.DiskStorageType.LUN && getSanStorageModel() != null) {
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
