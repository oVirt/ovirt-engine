package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

public class GlusterVDOVolume implements Serializable {
    private static final long serialVersionUID = -1134758927239004412L;

    private String name;
    private String device;
    private Long size;
    private Long free;
    private Long logicalBlocks;
    private Long physicalBlocks;

    public GlusterVDOVolume() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Number size) {
        this.size = size.longValue();
    }

    public Long getFree() {
        return free;
    }

    public void setFree(Number free) {
        this.free = free.longValue();
    }

    public Long getLogicalBlocks() {
        return logicalBlocks;
    }

    public void setLogicalBlocks(Number logicalBlocks) {
        this.logicalBlocks = logicalBlocks.longValue();
    }

    public Long getPhysicalBlocks() {
        return physicalBlocks;
    }

    public void setPhysicalBlocks(Number physicalBlocks) {
        this.physicalBlocks = physicalBlocks.longValue();
    }
}
