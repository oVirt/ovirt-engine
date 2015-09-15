package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;

@SuppressWarnings("unchecked")
public final class VolumeInfoReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String INFO = "info";

    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("info")]
    public Map<String, Object> volumeInfo;

    public VolumeInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        volumeInfo = (Map<String, Object>) innerMap.get(INFO);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.toStringBuilder(volumeInfo, builder);
        return builder.toString();
    }
}

