package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.CacheModeType;
import org.ovirt.engine.core.common.businessentities.RaidType;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.BrickMountPointValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.i18n.client.NumberFormat;

public class CreateBrickModel extends Model {
    EntityModel<String> lvName;
    EntityModel<String> size;
    EntityModel<String> mountPoint;
    EntityModel<String> defaultMountFolder;
    EntityModel<Integer> stripeSize;
    EntityModel<Integer> noOfPhysicalDisksInRaidVolume;
    ListModel<RaidType> raidTypeList;
    private ListModel<StorageDevice> storageDevices;
    ListModel<StorageDevice> cacheDevicePathTypeList;
    ListModel<CacheModeType> cacheModeTypeList;
    EntityModel<Integer> cacheSize;

    public CreateBrickModel() {
        setLvName(new EntityModel<String>());
        setStorageDevices(new ListModel<StorageDevice>());
        setSize(new EntityModel<String>());
        setStripeSize(new EntityModel<Integer>());
        setNoOfPhysicalDisksInRaidVolume(new EntityModel<Integer>());
        setRaidTypeList(new ListModel<RaidType>());
        setMountPoint(new EntityModel<String>());
        setDefaultMountFolder(new EntityModel<String>());
        setCacheDevicePathTypeList(new ListModel<StorageDevice>());
        setCacheModeTypeList(new ListModel<CacheModeType>());
        setCacheSize(new EntityModel<Integer>());
        List<RaidType> list = Arrays.asList(RaidType.values());
        getRaidTypeList().setItems(list);
        getNoOfPhysicalDisksInRaidVolume().setIsAvailable(false);
        getStripeSize().setIsAvailable(false);
        initSize();
        getStorageDevices().getSelectedItemsChangedEvent().addListener((ev, sender, args) -> updateBrickSize());

        getLvName().getEntityChangedEvent().addListener((ev, sender, args) -> {
            String mountPoint = getDefaultMountFolder().getEntity() + "/" + getLvName().getEntity(); //$NON-NLS-1$
            getMountPoint().setEntity(mountPoint);
        });

        getRaidTypeList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            if (getRaidTypeList().getSelectedItem() == RaidType.RAID6) {
                getNoOfPhysicalDisksInRaidVolume().setIsAvailable(true);
                getStripeSize().setIsAvailable(true);
                getStripeSize().setEntity(128);
            } else if (getRaidTypeList().getSelectedItem() == RaidType.RAID10) {
                getNoOfPhysicalDisksInRaidVolume().setIsAvailable(true);
                getStripeSize().setIsAvailable(true);
                getStripeSize().setEntity(256);
            } else {
                getNoOfPhysicalDisksInRaidVolume().setIsAvailable(false);
                getStripeSize().setIsAvailable(false);
            }
            onPropertyChanged(new PropertyChangedEventArgs("raidTypeChanged")); //$NON-NLS-1$
        });

        getCacheModeTypeList().setItems(null);
        getCacheModeTypeList().setItems(Arrays.asList(CacheModeType.values()));
        getCacheSize().setEntity(10);
        getCacheSize().setIsAvailable(true);
    }

    private void updateBrickSize() {
        long totalSize = 0;
        // Calculate and show the total size of all selected device so that user can get an idea of what will be his
        // brick
        // capacity.
        if (getStorageDevices().getSelectedItems() != null) {
            for (StorageDevice storageDevice : getStorageDevices().getSelectedItems()) {
                totalSize += storageDevice.getSize();
            }
        }

        Pair<SizeUnit, Double> convertedSize = SizeConverter.autoConvert(totalSize, SizeUnit.MiB);
        setBrickSize(convertedSize);
    }

    public EntityModel<String> getLvName() {
        return lvName;
    }

    public EntityModel<String> getSize() {
        return size;
    }

    private void initSize() {
        setBrickSize(new Pair<>(SizeUnit.BYTES, 0D));
    }

    private void setBrickSize(Pair<SizeUnit, Double> size) {
        String sizeString = getSizeString(size);
        getSize().setEntity(sizeString);
    }

    private String getSizeString(Pair<SizeUnit, Double> size) {
        return formatSize(size.getSecond()) + " " + size.getFirst().toString();//$NON-NLS-1$
    }

    public ListModel<StorageDevice> getStorageDevices() {
        return storageDevices;
    }

    public void setLvName(EntityModel<String> lvName) {
        this.lvName = lvName;
    }

    public void setSize(EntityModel<String> size) {
        this.size = size;
    }

    protected void setSelectedDevices(List<StorageDevice> selectedDevices) {
        getStorageDevices().setSelectedItems(selectedDevices);
    }

    public void setStorageDevices(ListModel<StorageDevice> storageDevices) {
        this.storageDevices = storageDevices;
    }

    public ListModel<StorageDevice> getCacheDevicePathTypeList() {
        return cacheDevicePathTypeList;
    }

    public void setCacheDevicePathTypeList(ListModel<StorageDevice> cacheDevicePathTypeList) {
        this.cacheDevicePathTypeList = cacheDevicePathTypeList;
    }

    public ListModel<CacheModeType> getCacheModeTypeList() {
        return cacheModeTypeList;
    }

    public void setCacheModeTypeList(ListModel<CacheModeType> cacheModeTypeList) {
        this.cacheModeTypeList = cacheModeTypeList;
    }

    public EntityModel<Integer> getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(EntityModel<Integer> cacheSize) {
        this.cacheSize = cacheSize;
    }

    public boolean validate() {

        getLvName().validateEntity(new IValidation[] { new NotEmptyValidation(), new LengthValidation(50),
                new AsciiNameValidation() });

        if (!getLvName().getIsValid()) {
            return false;
        }

        getMountPoint().validateEntity(new IValidation[] { new NotEmptyValidation(), new BrickMountPointValidation() });
        if (!getMountPoint().getIsValid()) {
            return false;
        }

        IntegerValidation noOfPhysicalDiscsValidation = new IntegerValidation();
        noOfPhysicalDiscsValidation.setMinimum(1);
        getNoOfPhysicalDisksInRaidVolume().validateEntity(new IValidation[] { new NotEmptyValidation(),
                noOfPhysicalDiscsValidation });
        if (!getNoOfPhysicalDisksInRaidVolume().getIsValid()) {
            return false;
        }

        IntegerValidation stripSizeValidation = new IntegerValidation();
        stripSizeValidation.setMinimum(1);
        getStripeSize().validateEntity(new IValidation[] { new NotEmptyValidation(),
                stripSizeValidation });
        if (!getStripeSize().getIsValid()) {
            return false;
        }
        return true;


    }

    public String formatSize(double size) {
        return NumberFormat.getFormat("#.##").format(size);//$NON-NLS-1$
    }

    public EntityModel<Integer> getStripeSize() {
        return stripeSize;
    }

    public EntityModel<Integer> getNoOfPhysicalDisksInRaidVolume() {
        return noOfPhysicalDisksInRaidVolume;
    }

    public void setStripeSize(EntityModel<Integer> stripeSize) {
        this.stripeSize = stripeSize;
    }

    public void setNoOfPhysicalDisksInRaidVolume(EntityModel<Integer> noOfPhysicalDisksInRaidVolume) {
        this.noOfPhysicalDisksInRaidVolume = noOfPhysicalDisksInRaidVolume;
    }

    public ListModel<RaidType> getRaidTypeList() {
        return raidTypeList;
    }

    public void setRaidTypeList(ListModel<RaidType> raidTypeList) {
        this.raidTypeList = raidTypeList;
    }

    public EntityModel<String> getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(EntityModel<String> mountPoint) {
        this.mountPoint = mountPoint;
    }

    public EntityModel<String> getDefaultMountFolder() {
        return defaultMountFolder;
    }

    public void setDefaultMountFolder(EntityModel<String> defaultMountFolder) {
        this.defaultMountFolder = defaultMountFolder;
    }
}
