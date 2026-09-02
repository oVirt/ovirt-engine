package org.ovirt.engine.core.common.businessentities.storage;

import java.util.ArrayList;
import java.util.List;

public class Qcow2BitmapInfo {

    // Not a Guid: qemu allows arbitrary bitmap names, and bitmaps not created by the engine may be present.
    private String name;
    private long granularity;
    private List<Qcow2BitmapInfoFlags> flags = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
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
        this.flags = flags == null ? new ArrayList<>() : flags;
    }

    /**
     * A bitmap is usable for an incremental backup only when it is enabled ('auto') and was flushed properly by the
     * last qemu process that had it open (no 'in-use'). Mirrors the rules VDSM applies in vdsm.storage.bitmaps.
     */
    public boolean isValid() {
        return flags.contains(Qcow2BitmapInfoFlags.AUTO) && !flags.contains(Qcow2BitmapInfoFlags.IN_USE);
    }

    @Override
    public String toString() {
        return String.format("Qcow2BitmapInfo:{name='%s', granularity='%s', flags='%s'}", name, granularity, flags);
    }
}
