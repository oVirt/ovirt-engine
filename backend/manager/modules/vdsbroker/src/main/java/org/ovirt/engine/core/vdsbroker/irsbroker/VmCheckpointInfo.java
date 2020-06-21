package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public final class VmCheckpointInfo extends StatusReturn {
    public String checkpoint;

    @SuppressWarnings("unchecked")
    public VmCheckpointInfo(Map<String, Object> innerMap) {
        super(innerMap);
        Object checkpointXML = innerMap.get(VdsProperties.CHECKPOINT);
        if (checkpointXML != null) {
            checkpoint = (String) checkpointXML;
        }
    }

    public String getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(String checkpoint) {
        this.checkpoint = checkpoint;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(checkpoint, builder);
        return builder.toString();
    }
}
