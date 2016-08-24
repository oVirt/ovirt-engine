package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class RemoveDeviceFromSANStorageDomainCommandParameters extends StorageDomainParametersBase {
    private String deviceId;
    private OperationStage operationStage = OperationStage.MOVE;
    private List<String> destinationDevices;

    public RemoveDeviceFromSANStorageDomainCommandParameters() {
    }

    public RemoveDeviceFromSANStorageDomainCommandParameters(Guid storageDomainId, String deviceId) {
        this(storageDomainId, deviceId, null);
    }

    public RemoveDeviceFromSANStorageDomainCommandParameters(Guid storageDomainId,
            String deviceId,
            List<String> destinationDevices) {
        super(storageDomainId);
        this.deviceId = deviceId;
        this.destinationDevices = destinationDevices;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<String> getDestinationDevices() {
        return destinationDevices;
    }

    public void setDestinationDevices(List<String> destinationDevices) {
        this.destinationDevices = destinationDevices;
    }

    public OperationStage getOperationStage() {
        return operationStage;
    }

    public void setOperationStage(OperationStage operationStage) {
        this.operationStage = operationStage;
    }

    public enum OperationStage {
        MOVE,
        REDUCE
    }
}
