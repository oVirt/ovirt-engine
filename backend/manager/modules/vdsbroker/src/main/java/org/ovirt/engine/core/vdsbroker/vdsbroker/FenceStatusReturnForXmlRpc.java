package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public final class FenceStatusReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String POWER = "power";
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("power")]
    public String Power;

    public FenceStatusReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Power = (String) innerMap.get(POWER);
    }
}
