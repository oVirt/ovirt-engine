package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;


/**
 * The gluster volume brick properties.
 *
 */
public class BrickProperties implements Serializable {

    private static final long serialVersionUID = 7690222172327373695L;

    private Guid brickId;

    private int port;

    private int pid;

    private GlusterStatus status;

    private double totalSize;

    private double freeSize;

    private Double confirmedFreeSize;

    private Double confirmedTotalSize;

    private Integer vdoSavings;

    private String device;

    private int blockSize;

    private String mntOptions;

    private String fsName;

    private int rdmaPort;

    public Guid getBrickId() {
        return brickId;
    }

    public void setBrickId(Guid brickId) {
        this.brickId = brickId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public GlusterStatus getStatus() {
        return status;
    }

    public void setStatus(GlusterStatus shdStatus) {
        this.status = shdStatus;
    }

    public double getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(double totalSize) {
        this.totalSize = totalSize;
    }

    public double getFreeSize() {
        return freeSize;
    }

    public void setFreeSize(double freeSize) {
        this.freeSize = freeSize;
    }

    public Double getConfirmedFreeSize() {
        return confirmedFreeSize;
    }

    public void setConfirmedFreeSize(Double confirmedFreeSize) {
        this.confirmedFreeSize = confirmedFreeSize;
    }

    public Double getConfirmedTotalSize() {
        return confirmedTotalSize;
    }

    public void setConfirmedTotalSize(Double confirmedTotalSize) {
        this.confirmedTotalSize = confirmedTotalSize;
    }

    public Integer getVdoSavings() {
        return vdoSavings;
    }

    public void setVdoSavings(Integer vdoSavings) {
        this.vdoSavings = vdoSavings;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public String getMntOptions() {
        return mntOptions;
    }

    public void setMntOptions(String mntOptions) {
        this.mntOptions = mntOptions;
    }

    public String getFsName() {
        return fsName;
    }

    public void setFsName(String fsName) {
        this.fsName = fsName;
    }

    public int getRdmaPort() {
        return rdmaPort;
    }

    public void setRdmaPort(int rdmaPort) {
        this.rdmaPort = rdmaPort;
    }

}
