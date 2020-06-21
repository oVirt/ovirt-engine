package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public final class VmCheckpointIds extends StatusReturn {
    public List<String> checkpointIds;
    public VDSError error;

    @SuppressWarnings("unchecked")
    public VmCheckpointIds(Map<String, Object> innerMap) {
        super(innerMap);
        if (innerMap.containsKey(VdsProperties.jobError)) {
            Map<String, Object> errorInfo = (Map<String, Object>) innerMap.get(VdsProperties.jobError);
            Integer code = (Integer) errorInfo.get(VdsProperties.jobErrorCode);
            String message = (String) errorInfo.get(VdsProperties.jobErrorMessage);
            error = new VDSError(EngineError.forValue(code), message);
        }
        Object[] definedCheckpointIds = (Object[]) innerMap.get(VdsProperties.CHECKPOINT_IDS);
        if (definedCheckpointIds != null) {
            checkpointIds = Arrays.stream(definedCheckpointIds).map(s -> (String) s).collect(Collectors.toList());
        }
    }

    public List<String> getCheckpointIds() {
        return checkpointIds;
    }

    public void setCheckpointIds(List<String> checkpointIds) {
        this.checkpointIds = checkpointIds;
    }

    public VDSError getError() {
        return error;
    }

    public void setError(VDSError error) {
        this.error = error;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(checkpointIds, builder);
        ObjectDescriptor.toStringBuilder(error, builder);
        return builder.toString();
    }
}
