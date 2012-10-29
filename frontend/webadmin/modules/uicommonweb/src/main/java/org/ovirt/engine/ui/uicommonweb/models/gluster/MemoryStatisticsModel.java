package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.MallInfo;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class MemoryStatisticsModel extends Model {

    private EntityModel totalAllocated;
    private EntityModel freeBlocks;
    private EntityModel freeFastbin;
    private EntityModel mmappedBlocks;
    private EntityModel spaceAllocatedMmapped;
    private EntityModel maxTotalAllocated;
    private EntityModel spaceFreedFastbin;
    private EntityModel totalAllocatedSpace;
    private EntityModel totalFreeSpace;
    private EntityModel releasableFreeSpace;

    public MemoryStatisticsModel() {
        setTotalAllocated(new EntityModel());
        setFreeBlocks(new EntityModel());
        setFreeFastbin(new EntityModel());
        setMmappedBlocks(new EntityModel());
        setSpaceAllocatedMmapped(new EntityModel());
        setMaxTotalAllocated(new EntityModel());
        setSpaceFreedFastbin(new EntityModel());
        setTotalAllocatedSpace(new EntityModel());
        setTotalFreeSpace(new EntityModel());
        setReleasableFreeSpace(new EntityModel());
    }

    public void updateMemoryStatistics(MallInfo mallInfo) {
        getTotalAllocated().setEntity(mallInfo.getArena());
        getFreeBlocks().setEntity(mallInfo.getOrdblks());
        getFreeFastbin().setEntity(mallInfo.getSmblks());
        getMmappedBlocks().setEntity(mallInfo.getHblks());
        getSpaceAllocatedMmapped().setEntity(mallInfo.getHblkhd());
        getMaxTotalAllocated().setEntity(mallInfo.getUsmblks());
        getSpaceFreedFastbin().setEntity(mallInfo.getFsmblks());
        getTotalAllocatedSpace().setEntity(mallInfo.getUordblks());
        getTotalFreeSpace().setEntity(mallInfo.getFordblks());
        getReleasableFreeSpace().setEntity(mallInfo.getKeepcost());
    }

    public EntityModel getTotalAllocated() {
        return totalAllocated;
    }

    public void setTotalAllocated(EntityModel totalAllocated) {
        this.totalAllocated = totalAllocated;
    }

    public EntityModel getFreeBlocks() {
        return freeBlocks;
    }

    public void setFreeBlocks(EntityModel freeBlocks) {
        this.freeBlocks = freeBlocks;
    }

    public EntityModel getFreeFastbin() {
        return freeFastbin;
    }

    public void setFreeFastbin(EntityModel freeFastbin) {
        this.freeFastbin = freeFastbin;
    }

    public EntityModel getMmappedBlocks() {
        return mmappedBlocks;
    }

    public void setMmappedBlocks(EntityModel mmappedBlocks) {
        this.mmappedBlocks = mmappedBlocks;
    }

    public EntityModel getSpaceAllocatedMmapped() {
        return spaceAllocatedMmapped;
    }

    public void setSpaceAllocatedMmapped(EntityModel spaceAllocatedMmapped) {
        this.spaceAllocatedMmapped = spaceAllocatedMmapped;
    }

    public EntityModel getMaxTotalAllocated() {
        return maxTotalAllocated;
    }

    public void setMaxTotalAllocated(EntityModel maxTotalAllocated) {
        this.maxTotalAllocated = maxTotalAllocated;
    }

    public EntityModel getSpaceFreedFastbin() {
        return spaceFreedFastbin;
    }

    public void setSpaceFreedFastbin(EntityModel spaceFreedFastbin) {
        this.spaceFreedFastbin = spaceFreedFastbin;
    }

    public EntityModel getTotalAllocatedSpace() {
        return totalAllocatedSpace;
    }

    public void setTotalAllocatedSpace(EntityModel totalAllocatedSpace) {
        this.totalAllocatedSpace = totalAllocatedSpace;
    }

    public EntityModel getTotalFreeSpace() {
        return totalFreeSpace;
    }

    public void setTotalFreeSpace(EntityModel totalFreeSpace) {
        this.totalFreeSpace = totalFreeSpace;
    }

    public EntityModel getReleasableFreeSpace() {
        return releasableFreeSpace;
    }

    public void setReleasableFreeSpace(EntityModel releasableFreeSpace) {
        this.releasableFreeSpace = releasableFreeSpace;
    }

}
