package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public final class StorageStatusReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String STORAGE_STATUS = "storageStatus";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore),
    // XmlRpcMember("storageStatus")]
    public String storageStatus;

    public StorageStatusReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        storageStatus = (String) innerMap.get(STORAGE_STATUS);
    }

}
