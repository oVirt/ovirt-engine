package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public final class VmBackupInfo extends StatusReturn {
    public Map<String, Object> disks;
    public String checkpoint;

    @SuppressWarnings("unchecked")
    public VmBackupInfo(Map<String, Object> innerMap) {
        super(innerMap);
        disks = (Map<String, Object>) innerMap.get(VdsProperties.vm_disks);
        Object tempObj = innerMap.get(VdsProperties.CHECKPOINT);
        if (tempObj != null) {
            checkpoint = (String) tempObj;
        }
    }

    public Map<String, Object> getDisks() {
        return disks;
    }

    public String getCheckpoint() {
        return checkpoint;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(disks, builder);
        ObjectDescriptor.toStringBuilder(checkpoint, builder);
        return builder.toString();
    }
}
