package org.ovirt.engine.core.common.action.gluster;

import java.util.List;

import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.RaidType;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.compat.Guid;

/**
 * Command parameters for the "Create Volume" action
 */
public class CreateBrickParameters extends VdsActionParameters {

    private static final long serialVersionUID = 761203751697100144L;

    private String lvName;

    private List<StorageDevice> disks;
    private String mountPoint;
    private RaidType raidType;
    private Integer noOfPhysicalDisksInRaidVolume;
    private Integer stripeSize;

    public CreateBrickParameters() {

    }

    public CreateBrickParameters(Guid hostId,
            String lvName,
            String mountPoint,
            RaidType raidType,
            Integer noOfPhysicalDisksInRaidVolume,
            Integer stripeSize,
            List<StorageDevice> selectedDevices) {
        super(hostId);
        this.lvName = lvName;
        this.mountPoint = mountPoint;
        this.disks = selectedDevices;
        this.raidType = raidType;
        this.noOfPhysicalDisksInRaidVolume = noOfPhysicalDisksInRaidVolume;
        this.stripeSize = stripeSize;

    }

    public String getLvName() {
        return lvName;
    }

    public List<StorageDevice> getDisks() {
        return disks;
    }

    public void setLvName(String lvName) {
        this.lvName = lvName;
    }

    public void setDisks(List<StorageDevice> disks) {
        this.disks = disks;
    }

    public RaidType getRaidType() {
        return raidType;
    }

    public Integer getNoOfPhysicalDisksInRaidVolume() {
        return noOfPhysicalDisksInRaidVolume;
    }

    public void setRaidType(RaidType raidType) {
        this.raidType = raidType;
    }

    public void setNoOfPhysicalDisksInRaidVolume(Integer noOfPhysicalDisksInRaidVolume) {
        this.noOfPhysicalDisksInRaidVolume = noOfPhysicalDisksInRaidVolume;
    }

    public Integer getStripeSize() {
        return stripeSize;
    }

    public void setStripeSize(Integer stripeSize) {
        this.stripeSize = stripeSize;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

}
