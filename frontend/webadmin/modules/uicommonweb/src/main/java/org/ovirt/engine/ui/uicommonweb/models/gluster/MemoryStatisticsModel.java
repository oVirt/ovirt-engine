package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.MallInfo;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class MemoryStatisticsModel extends Model {

    private EntityModel<Integer> totalAllocated;
    private EntityModel<Integer> freeBlocks;
    private EntityModel<Integer> freeFastbin;
    private EntityModel<Integer> mmappedBlocks;
    private EntityModel<Integer> spaceAllocatedMmapped;
    private EntityModel<Integer> maxTotalAllocated;
    private EntityModel<Integer> spaceFreedFastbin;
    private EntityModel<Integer> totalAllocatedSpace;
    private EntityModel<Integer> totalFreeSpace;
    private EntityModel<Integer> releasableFreeSpace;

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

    public EntityModel<Integer> getTotalAllocated() {
        return totalAllocated;
    }

    public void setTotalAllocated(EntityModel<Integer> totalAllocated) {
        this.totalAllocated = totalAllocated;
    }

    public EntityModel<Integer> getFreeBlocks() {
        return freeBlocks;
    }

    public void setFreeBlocks(EntityModel<Integer> freeBlocks) {
        this.freeBlocks = freeBlocks;
    }

    public EntityModel<Integer> getFreeFastbin() {
        return freeFastbin;
    }

    public void setFreeFastbin(EntityModel<Integer> freeFastbin) {
        this.freeFastbin = freeFastbin;
    }

    public EntityModel<Integer> getMmappedBlocks() {
        return mmappedBlocks;
    }

    public void setMmappedBlocks(EntityModel<Integer> mmappedBlocks) {
        this.mmappedBlocks = mmappedBlocks;
    }

    public EntityModel<Integer> getSpaceAllocatedMmapped() {
        return spaceAllocatedMmapped;
    }

    public void setSpaceAllocatedMmapped(EntityModel<Integer> spaceAllocatedMmapped) {
        this.spaceAllocatedMmapped = spaceAllocatedMmapped;
    }

    public EntityModel<Integer> getMaxTotalAllocated() {
        return maxTotalAllocated;
    }

    public void setMaxTotalAllocated(EntityModel<Integer> maxTotalAllocated) {
        this.maxTotalAllocated = maxTotalAllocated;
    }

    public EntityModel<Integer> getSpaceFreedFastbin() {
        return spaceFreedFastbin;
    }

    public void setSpaceFreedFastbin(EntityModel<Integer> spaceFreedFastbin) {
        this.spaceFreedFastbin = spaceFreedFastbin;
    }

    public EntityModel<Integer> getTotalAllocatedSpace() {
        return totalAllocatedSpace;
    }

    public void setTotalAllocatedSpace(EntityModel<Integer> totalAllocatedSpace) {
        this.totalAllocatedSpace = totalAllocatedSpace;
    }

    public EntityModel<Integer> getTotalFreeSpace() {
        return totalFreeSpace;
    }

    public void setTotalFreeSpace(EntityModel<Integer> totalFreeSpace) {
        this.totalFreeSpace = totalFreeSpace;
    }

    public EntityModel<Integer> getReleasableFreeSpace() {
        return releasableFreeSpace;
    }

    public void setReleasableFreeSpace(EntityModel<Integer> releasableFreeSpace) {
        this.releasableFreeSpace = releasableFreeSpace;
    }

}
