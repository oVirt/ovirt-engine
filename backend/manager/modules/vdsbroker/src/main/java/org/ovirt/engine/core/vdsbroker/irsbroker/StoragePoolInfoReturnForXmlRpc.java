package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.ObjectDescriptor;

public final class StoragePoolInfoReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String INFO = "info";
    private static final String DOM_INFO = "dominfo";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("info")]
    public Map<String, Object> storagePoolInfo;
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("dominfo")]
    public Map<String, Object> domainsList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(storagePoolInfo, builder);
        ObjectDescriptor.toStringBuilder(domainsList, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public StoragePoolInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        storagePoolInfo = (Map<String, Object>) innerMap.get(INFO);
        domainsList = (Map<String, Object>) innerMap.get(DOM_INFO);
    }

}
