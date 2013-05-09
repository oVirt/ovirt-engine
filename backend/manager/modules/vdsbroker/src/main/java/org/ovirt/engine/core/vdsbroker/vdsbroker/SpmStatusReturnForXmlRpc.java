package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public final class SpmStatusReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String SPM_STATUS = "spm_st";
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("spm_st")]
    public Map<String, Object> spmStatus;

    @SuppressWarnings("unchecked")
    public SpmStatusReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        spmStatus = (Map<String, Object>) innerMap.get(SPM_STATUS);
    }

}
