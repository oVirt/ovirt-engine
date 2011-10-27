package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class SpmStatusReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String SPM_STATUS = "spm_st";
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("spm_st")]
    public XmlRpcStruct spmStatus;

    @SuppressWarnings("unchecked")
    public SpmStatusReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = (Object) innerMap.get(SPM_STATUS);
        if (temp != null) {
            spmStatus = new XmlRpcStruct((Map<String, Object>) temp);
        }
    }

}
