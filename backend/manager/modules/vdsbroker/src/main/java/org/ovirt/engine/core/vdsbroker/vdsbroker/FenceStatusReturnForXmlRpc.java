package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public final class FenceStatusReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String POWER = "power";
    private static final String OPERATION_STATUS = "operationStatus";
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("power")]
    public String power;
    public String operationStatus;

    public FenceStatusReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        power = (String) innerMap.get(POWER);
        operationStatus = (String) innerMap.get(OPERATION_STATUS);
    }
}
