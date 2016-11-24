package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

@SuppressWarnings("unchecked")
public final class QemuImageInfoReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String INFO = "info";

    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("info")]
    public Map<String, Object> qemuImageInfo;

    public QemuImageInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        qemuImageInfo = (Map<String, Object>) innerMap.get(INFO);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(qemuImageInfo, builder);
        return builder.toString();
    }

    public Map<String, Object> getQemuImageInfo() {
        return qemuImageInfo;
    }
}

