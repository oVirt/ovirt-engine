package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;

public final class GetDisksListReturn extends StatusReturn {
    private static final String DISKS = "disks";

    public Map<String, Object> disks;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(disks, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public GetDisksListReturn(Map<String, Object> innerMap) {
        super(innerMap);
        disks = (Map<String, Object>) innerMap.get(DISKS);
    }

    public Map<String, Object> getDisks() {
        return disks;
    }
}
