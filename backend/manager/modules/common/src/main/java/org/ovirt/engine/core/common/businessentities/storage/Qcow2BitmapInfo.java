package org.ovirt.engine.core.common.businessentities.storage;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class Qcow2BitmapInfo {

    private Guid name;
    private long granularity;
    private List<Qcow2BitmapInfoFlags> flags;


    public Guid getName() {
        return name;
    }

    public void setName(Guid name) {
        this.name = name;
    }

    public long getGranularity() {
        return granularity;
    }

    public void setGranularity(long granularity) {
        this.granularity = granularity;
    }

    public List<Qcow2BitmapInfoFlags> getFlags() {
        return flags;
    }

    public void setFlags(List<Qcow2BitmapInfoFlags> flags) {
        this.flags = flags;
    }
}
