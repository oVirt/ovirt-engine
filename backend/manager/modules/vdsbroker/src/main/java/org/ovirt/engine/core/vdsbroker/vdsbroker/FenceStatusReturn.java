package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class FenceStatusReturn extends StatusReturn {
    private static final String POWER = "power";
    private static final String OPERATION_STATUS = "operationStatus";
    // [MissingMapping(MappingAction.Ignore), Member("power")]
    public String power;
    public String operationStatus;

    public FenceStatusReturn(Map<String, Object> innerMap) {
        super(innerMap);
        power = (String) innerMap.get(POWER);
        operationStatus = (String) innerMap.get(OPERATION_STATUS);
    }
}
