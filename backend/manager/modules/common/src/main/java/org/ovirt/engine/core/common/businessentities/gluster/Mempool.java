package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;


/**
 * The gluster volume memory status Mem pool.
 *
 */
public class Mempool implements Serializable {

    private static final long serialVersionUID = 4426819375609665363L;

    private String name;

    private int hotCount;

    private int coldCount;

    private int padddedSize;

    private int allocCount;

    private int maxAlloc;

    private int poolMisses;

    private int maxStdAlloc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHotCount() {
        return hotCount;
    }

    public void setHotCount(int hotCount) {
        this.hotCount = hotCount;
    }

    public int getColdCount() {
        return coldCount;
    }

    public void setColdCount(int coldCount) {
        this.coldCount = coldCount;
    }

    public int getPadddedSize() {
        return padddedSize;
    }

    public void setPadddedSize(int padddedSize) {
        this.padddedSize = padddedSize;
    }

    public int getAllocCount() {
        return allocCount;
    }

    public void setAllocCount(int allocCount) {
        this.allocCount = allocCount;
    }

    public int getMaxAlloc() {
        return maxAlloc;
    }

    public void setMaxAlloc(int maxAlloc) {
        this.maxAlloc = maxAlloc;
    }

    public int getPoolMisses() {
        return poolMisses;
    }

    public void setPoolMisses(int poolMisses) {
        this.poolMisses = poolMisses;
    }

    public int getMaxStdAlloc() {
        return maxStdAlloc;
    }

    public void setMaxStdAlloc(int maxStdAlloc) {
        this.maxStdAlloc = maxStdAlloc;
    }
}
