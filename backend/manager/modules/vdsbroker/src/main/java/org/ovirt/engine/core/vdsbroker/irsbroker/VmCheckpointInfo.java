package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public final class VmCheckpointInfo extends StatusReturn {
    public List<String> checkpointsIds;
    public VDSError error;

    @SuppressWarnings("unchecked")
    public VmCheckpointInfo(Map<String, Object> innerMap) {
        super(innerMap);
        if (innerMap.containsKey(VdsProperties.jobError)) {
            Map<String, Object> errorInfo = (Map<String, Object>) innerMap.get(VdsProperties.jobError);
            Integer code = (Integer) errorInfo.get(VdsProperties.jobErrorCode);
            String message = (String) errorInfo.get(VdsProperties.jobErrorMessage);
            error = new VDSError(EngineError.forValue(code), message);
        }
        Object[] tempObj = (Object[]) innerMap.get(VdsProperties.CHECKPOINT_IDS);
        if (tempObj != null) {
            checkpointsIds = Arrays.stream(tempObj).map(s -> (String) s).collect(Collectors.toList());
        }
    }

    public List<String> getCheckpointsIds() {
        return checkpointsIds;
    }

    public void setCheckpointsIds(List<String> checkpointsIds) {
        this.checkpointsIds = checkpointsIds;
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
        ObjectDescriptor.toStringBuilder(checkpointsIds, builder);
        ObjectDescriptor.toStringBuilder(error, builder);
        return builder.toString();
    }
}
