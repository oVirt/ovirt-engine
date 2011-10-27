package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class StoragePoolInfoReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String INFO = "info";
    private static final String DOM_INFO = "dominfo";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("info")]
    public XmlRpcStruct mStoragePoolInfo;
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("dominfo")]
    public XmlRpcStruct mDomainsList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(mStoragePoolInfo, builder);
        XmlRpcObjectDescriptor.ToStringBuilder(mDomainsList, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public StoragePoolInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(INFO);
        if (temp != null) {
            mStoragePoolInfo = new XmlRpcStruct((Map<String, Object>) temp);
        }
        temp = innerMap.get(DOM_INFO);
        if (temp != null) {
            mDomainsList = new XmlRpcStruct((Map<String, Object>) temp);
        }
    }

}
